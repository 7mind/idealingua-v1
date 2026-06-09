package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import io.circe.{Json, JsonObject}
import izumi.functional.bio.{Exit, IO2}
import izumi.idealingua.runtime.rpc.{IRTDecodingException, IRTGenericFailure, IRTLimitReachedException, IRTMethodId, IRTMissingHandlerException, IRTServerMultiplexor, IRTTypeMismatchException, IRTUnathorizedRequestContextException, IRTUnparseableDataException}
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Request}

/** MCP-spec-compliant JSON-RPC 2.0 transport interpreter over a combined
  * [[IRTServerMultiplexor]].
  *
  * This is a multi-service, data-driven interpreter: it owns a single
  * JSON-RPC endpoint (`POST <mountPath>`) and dispatches `tools/call`
  * DIRECTLY against the multiplexor. All services are folded
  * into one mux (`IRTServerMultiplexor.FromServices` over a
  * `Set[IRTWrappedService]`, or `.combine`), one UNION `tools/list`
  * envelope, and one dispatch map from fully-qualified MCP tool name to the
  * target `(IRTMethodId, wrap)`.
  *
  * Request handling:
  *   - `initialize`              → capability handshake.
  *   - `tools/list`              → the union `tools/list` envelope verbatim.
  *   - `tools/call`              → resolve the FQ tool name in the dispatch
  *     map, extract the context, call `mux.invokeMethod(methodId)(ctx, args)`,
  *     and shape the MCP `CallToolResult` envelope
  *     (`content` / `structuredContent` / `isError`) applying the per-tool
  *     static `wrap` flag.
  *   - `ping`                    → empty result.
  *   - `notifications/initialized` → no response (JSON-RPC notification).
  *
  * Streamable HTTP (single endpoint, request/response over POST) is the
  * supported transport per MCP 2025-06-18; SSE upgrade for server-initiated
  * messages is NOT implemented (the bridge has no notifications to send).
  *
  * @param mux         combined multiplexor holding every service's methods.
  * @param toolsListJson the UNION `tools/list` envelope (assembled from all
  *                    services' `McpServiceMeta` deltas by the builder).
  * @param dispatch    fully-qualified MCP tool name → (target method, wrap flag).
  * @param extractCtx  derives the request context `C` (e.g. authenticated principal).
  */
final class McpJsonRpcRoutes[F[+_, +_]: IO2, C](
  mux: IRTServerMultiplexor[F, C],
  toolsListJson: Json,
  dispatch: Map[String, (IRTMethodId, Boolean)],
  extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
  serverName: String,
  serverVersion: String,
  protocolVersion: String,
  mountPath: String,
  dsl: Http4sDsl[F[Throwable, _]],
)(implicit AT: Async[F[Throwable, _]]
) {

  import dsl.*
  private implicit val jd: EntityDecoder[F[Throwable, _], Json] = jsonDecoder[F[Throwable, _]]

  /** The single JSON-RPC endpoint. */
  def routes: HttpRoutes[F[Throwable, _]] = HttpRoutes.of[F[Throwable, _]] {
    case req @ POST -> path if path.renderString == mountPath =>
      req.as[Json].flatMap(body => dispatchRpc(req, body))
  }

  private val initializeResult: Json = Json.obj(
    "protocolVersion" -> Json.fromString(protocolVersion),
    "capabilities" -> Json.obj(
      "tools" -> Json.obj("listChanged" -> Json.False)
    ),
    "serverInfo" -> Json.obj(
      "name"    -> Json.fromString(serverName),
      "version" -> Json.fromString(serverVersion),
    ),
  )

  private def dispatchRpc(req: Request[F[Throwable, _]], body: Json): F[Throwable, org.http4s.Response[F[Throwable, _]]] = {
    val c              = body.hcursor
    val method         = c.get[String]("method").toOption
    val id             = c.downField("id").focus
    val params         = c.downField("params").focus.getOrElse(Json.obj())
    val isNotification = id.isEmpty

    method match {
      case Some("initialize") =>
        Ok(jsonRpcSuccess(id, initializeResult))

      case Some("notifications/initialized") if isNotification =>
        // No response per JSON-RPC notification rules.
        NoContent()

      case Some("ping") =>
        Ok(jsonRpcSuccess(id, Json.obj()))

      case Some("tools/list") =>
        Ok(jsonRpcSuccess(id, toolsListJson))

      case Some("tools/call") =>
        val name = params.hcursor.get[String]("name").toOption.getOrElse("")
        val args = params.hcursor.downField("arguments").focus.getOrElse(Json.obj())
        dispatch.get(name) match {
          case Some((methodId, wrap)) =>
            invokeTool(req, methodId, wrap, args, id)
          case None =>
            Ok(jsonRpcError(id, -32601, s"Method not found: $name"))
        }

      case Some(other) =>
        Ok(jsonRpcError(id, -32601, s"Method not found: $other"))

      case None =>
        Ok(jsonRpcError(id, -32600, "Invalid request: missing 'method'"))
    }
  }

  /** Dispatch a `tools/call` DIRECTLY against the multiplexor and shape the
    * MCP `CallToolResult` envelope. The wrap flag is the per-tool static
    * policy carried in the dispatch map: when set, the raw mux output is
    * wrapped under `{"result": <raw>}`; otherwise it is used verbatim.
    */
  private def invokeTool(
    req: Request[F[Throwable, _]],
    methodId: IRTMethodId,
    wrap: Boolean,
    args: Json,
    id: Option[Json],
  ): F[Throwable, org.http4s.Response[F[Throwable, _]]] = {
    (for {
      ctx <- extractCtx(req)
      raw <- mux.invokeMethod(methodId)(ctx, args)
    } yield {
      val shaped = if (wrap) Json.obj("result" -> raw) else raw
      Json.obj(
        "content" -> Json.arr(
          Json.obj(
            "type" -> Json.fromString("text"),
            "text" -> Json.fromString(shaped.noSpaces),
          )
        ),
        "structuredContent" -> shaped,
        "isError"           -> Json.False,
      )
    }).sandboxExit.flatMap {
      case Exit.Success(result) =>
        Ok(jsonRpcSuccess(id, result))
      case Exit.Error(_: IRTMissingHandlerException, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32601, s"Method not found: ${methodId.service.value}.${methodId.methodId.value}")))
      case Exit.Error(e: IRTUnparseableDataException, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32700, s"Parse error: ${safeMsg(e)}")))
      case Exit.Error(e: IRTTypeMismatchException, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32602, s"Invalid arguments (type mismatch): ${safeMsg(e)}")))
      case Exit.Error(e: IRTDecodingException, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32602, s"Invalid arguments: ${safeMsg(e)}")))
      case Exit.Error(e: _root_.io.circe.Error, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32602, s"Invalid arguments: ${safeMsg(e)}")))
      case Exit.Error(e: IRTLimitReachedException, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32000, s"Rate limit exceeded: ${safeMsg(e)}")))
      case Exit.Error(_: IRTUnathorizedRequestContextException, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32001, "Unauthorized")))
      case Exit.Error(_: IRTGenericFailure, _) =>
        Ok(jsonRpcSuccess(id, mcpError(-32603, "Internal error")))
      case _ =>
        Ok(jsonRpcSuccess(id, mcpError(-32603, "Internal error")))
    }
  }

  private def safeMsg(t: Throwable): String = {
    val m = t.getMessage
    if (m == null) t.getClass.getSimpleName else m
  }

  /** In-band MCP `CallToolResult` error envelope (`isError = true`). MCP tool
    * errors are carried inside a successful JSON-RPC `result`, not as a
    * JSON-RPC transport error — clients inspect `isError`/`structuredContent`.
    */
  private def mcpError(code: Int, msg: String): Json = Json.obj(
    "content" -> Json.arr(
      Json.obj(
        "type" -> Json.fromString("text"),
        "text" -> Json.fromString(msg),
      )
    ),
    "structuredContent" -> Json.obj(
      "error" -> Json.obj(
        "code"    -> Json.fromInt(code),
        "message" -> Json.fromString(msg),
      )
    ),
    "isError" -> Json.True,
  )

  // ---- JSON-RPC envelope helpers --------------------------------------

  private def jsonRpcSuccess(id: Option[Json], result: Json): Json = {
    val fields = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    fields += "jsonrpc" -> Json.fromString("2.0")
    id.foreach(j => fields += "id" -> j)
    fields += "result" -> result
    Json.fromFields(fields)
  }

  private def jsonRpcError(id: Option[Json], code: Int, message: String, data: Option[Json] = None): Json = {
    val err = JsonObject(
      "code"    -> Json.fromInt(code),
      "message" -> Json.fromString(message),
    )
    val errWithData = data.fold(err)(d => err.add("data", d))
    val fields      = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    fields += "jsonrpc" -> Json.fromString("2.0")
    fields += "id"      -> id.getOrElse(Json.Null)
    fields += "error"   -> Json.fromJsonObject(errWithData)
    Json.fromFields(fields)
  }
}

object McpJsonRpcRoutes {
  /** Default MCP protocol version we advertise — matches the schema
    * pinned at `idealingua-v1-test-defs/schema/mcp-2025-06-18.json`.
    */
  val DefaultProtocolVersion: String = "2025-06-18"
}
