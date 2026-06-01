package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import io.circe.{Json, JsonObject}
import izumi.functional.bio.{Error2, IO2}
import org.http4s.{EntityDecoder, Headers, HttpRoutes, Method, Request, Uri}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.typelevel.ci.CIString

/** MCP-spec-compliant JSON-RPC 2.0 transport adapter wrapping the
  * `<ServiceName>McpRoutes` emitted by `DomainServiceMcpRenderer`.
  *
  * The generator's per-service routes expose an MCP-shaped REST API
  * (`GET /mcp/tools/list`, `POST /mcp/tools/call`) but not the MCP
  * **transport** (JSON-RPC 2.0 envelope, capability handshake, request
  * id correlation). Real MCP clients — Claude Desktop, Claude Code,
  * `mcp-python` — speak JSON-RPC over HTTP+SSE or stdio with an
  * `initialize` handshake, so they cannot connect to the bare REST
  * dialect.
  *
  * This adapter:
  *   - Listens on a single POST endpoint (configurable via `mountPath`).
  *   - Parses incoming JSON-RPC 2.0 requests + notifications.
  *   - Handles core methods directly (`initialize`, `tools/list`,
  *     `ping`, `notifications/initialized`).
  *   - Forwards `tools/call` to the inner REST routes by synthesising
  *     the equivalent REST POST body, then re-wraps the REST response
  *     payload as the JSON-RPC `result` (the body shape — `content`,
  *     `structuredContent`, `isError` — is already
  *     spec-compliant `CallToolResult`).
  *
  * Streamable HTTP (single endpoint, request/response over POST) is the
  * supported transport per MCP 2025-06-18; SSE upgrade for server-
  * initiated messages is NOT implemented (the bridge has no
  * notifications to send, so it's unnecessary).
  *
  * Re-use across services: pass the inner routes + the
  * `tools/list` JSON envelope (loaded from
  * `mcp/<ServiceName>.mcp.json` resource by the per-service routes
  * already). Service-prefix routing is delegated to the inner routes
  * — they own the per-method match arms.
  */
final class McpJsonRpcRoutes[F[+_, +_]: IO2: Error2, C](
  innerRoutes: HttpRoutes[F[Throwable, *]],
  toolsListJson: Json,
  serverName: String,
  serverVersion: String,
  protocolVersion: String,
  mountPath: String,
  dsl: Http4sDsl[F[Throwable, *]],
)(implicit AT: Async[F[Throwable, *]]) {

  import dsl._
  private implicit val jd: EntityDecoder[F[Throwable, *], Json] = jsonDecoder[F[Throwable, *]]

  /** The single JSON-RPC endpoint. Compose with the inner REST routes
    * if you also want the legacy REST surface exposed: `(jsonRpc <+> innerRoutes)`.
    */
  def routes: HttpRoutes[F[Throwable, *]] = HttpRoutes.of[F[Throwable, *]] {
    case req @ POST -> path if path.renderString == mountPath =>
      // Carry the inbound request headers (Authorization, X-Forwarded-For, …)
      // into the synthesized inner REST call so the inner routes' context
      // extractor / authenticator can still see the bearer token. Without
      // this the JSON-RPC transport silently strips all auth.
      req.as[Json].flatMap(body => dispatch(body, req.headers))
  }

  private val initializeResult: Json = Json.obj(
    "protocolVersion" -> Json.fromString(protocolVersion),
    "capabilities" -> Json.obj(
      "tools" -> Json.obj("listChanged" -> Json.False),
    ),
    "serverInfo" -> Json.obj(
      "name"    -> Json.fromString(serverName),
      "version" -> Json.fromString(serverVersion),
    ),
  )

  private def dispatch(body: Json, headers: Headers): F[Throwable, org.http4s.Response[F[Throwable, *]]] = {
    val c               = body.hcursor
    val method          = c.get[String]("method").toOption
    val id              = c.downField("id").focus
    val params          = c.downField("params").focus.getOrElse(Json.obj())
    val isNotification  = id.isEmpty

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
        // Synthesize a REST POST /mcp/tools/call to the inner routes and
        // unwrap whatever they return as the JSON-RPC `result`.
        forwardToInnerRest(name, args, id, headers)

      case Some(other) =>
        Ok(jsonRpcError(id, -32601, s"Method not found: $other"))

      case None =>
        Ok(jsonRpcError(id, -32600, "Invalid request: missing 'method'"))
    }
  }

  private def forwardToInnerRest(toolName: String, args: Json, id: Option[Json], headers: Headers): F[Throwable, org.http4s.Response[F[Throwable, *]]] = {
    val callBody = Json.obj(
      "name"      -> Json.fromString(toolName),
      "arguments" -> args,
    )
    implicit val je: org.http4s.EntityEncoder[F[Throwable, *], Json] = jsonEncoderOf[F[Throwable, *], Json]
    // Drop entity headers describing the *original* JSON-RPC body —
    // `withEntity` recomputes Content-Type/Content-Length for `callBody`.
    // Everything else (Authorization, X-Forwarded-For, …) is forwarded so
    // the inner routes can authenticate.
    val forwardedHeaders = headers.transform(_.filterNot { h =>
      h.name == CIString("Content-Type") || h.name == CIString("Content-Length")
    })
    val innerReq = Request[F[Throwable, *]](Method.POST, Uri.unsafeFromString("/mcp/tools/call"), headers = forwardedHeaders).withEntity(callBody)
    innerRoutes.run(innerReq).value.flatMap {
      case Some(resp) =>
        resp.as[Json].flatMap(payload => Ok(jsonRpcSuccess(id, payload)))
      case None =>
        Ok(jsonRpcError(id, -32601, s"Method not found: $toolName"))
    }
  }

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
    val fields = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    fields += "jsonrpc" -> Json.fromString("2.0")
    fields += "id"      -> id.getOrElse(Json.Null)
    fields += "error"   -> Json.fromJsonObject(errWithData)
    Json.fromFields(fields)
  }
}

object McpJsonRpcRoutes {
  /** Default MCP protocol version we advertise — matches the schema
    * pinned at `idealingua-v1-test-defs/schema/mcp-2025-06-18.json`. */
  val DefaultProtocolVersion: String = "2025-06-18"
}
