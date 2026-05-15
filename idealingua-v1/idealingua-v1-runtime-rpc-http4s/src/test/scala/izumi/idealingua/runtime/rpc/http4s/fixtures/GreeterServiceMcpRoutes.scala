package izumi.idealingua.runtime.rpc.http4s.fixtures

import _root_.cats.effect.Async
import _root_.io.circe.{Json, parser}
import izumi.functional.bio.{Error2, Exit, IO2}
import izumi.idealingua.runtime.rpc.{
  IRTDecodingException,
  IRTGenericFailure,
  IRTLimitReachedException,
  IRTMethodId,
  IRTMethodName,
  IRTMissingHandlerException,
  IRTServerMultiplexor,
  IRTServiceId,
  IRTTypeMismatchException,
  IRTUnathorizedRequestContextException,
  IRTUnparseableDataException,
}
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

/** Mb4-A test fixture: hand-authored MCP/HTTP4s bridge for the runtime test
  * domain's `GreeterService` (hand-written under `idealingua-v1-test-defs`,
  * not from an IDL). This file mirrors the generator output of
  * `DomainServiceMcpRenderer` byte-for-byte (modulo service name + tool list)
  * so an integration spec can exercise the bridge end-to-end against the
  * existing `TestServices.Client.buzzerMultiplexor` (`greet`, `alternative`).
  *
  * Why hand-authored rather than auto-generated for this test:
  *   - `GreeterService` has no IDL definition — it lives as Scala source in
  *     `idealingua-v1-test-defs` and is the only service the http4s runtime
  *     module's existing test fixtures expose with a populated mux.
  *   - The five corpus-driven goldens under
  *     `idealingua-v1-test-defs/golden/scala-mcp/` ARE byte-equal-asserted
  *     against the generator (`McpBridgeEmissionSpec` Layer A), so they
  *     transitively pin the same shape this fixture replicates.
  *   - Generating from a fresh IDL inside this module would force a new
  *     compile-step + golden-comparison loop here; the integration value is
  *     "real HTTP round-trip", not "another generator-equivalence test".
  *
  * Wrap-policy spot check (matches `OutputWrapPolicy.isWrapped`):
  *   - `greet`       → `Singular(string)` → `wrap = true`
  *   - `alternative` → `Alternative`      → `wrap = true`
  */
object GreeterServiceMcpRoutes {

  private lazy val toolsListJson: Json = {
    val stream = getClass.getClassLoader.getResourceAsStream("mcp/GreeterService.mcp.json")
    require(stream != null, "Missing classpath resource: mcp/GreeterService.mcp.json")
    try parser.parse(scala.io.Source.fromInputStream(stream, "UTF-8").mkString).toTry.get
    finally stream.close()
  }

  private val toolName_greet       = "izumi.r2.idealingua.test.generated.GreeterService.greet"
  private val methodId_greet       = IRTMethodId(IRTServiceId("GreeterService"), IRTMethodName("greet"))
  private val toolName_alternative = "izumi.r2.idealingua.test.generated.GreeterService.alternative"
  private val methodId_alternative = IRTMethodId(IRTServiceId("GreeterService"), IRTMethodName("alternative"))

  def routes[F[+_, +_]: IO2: Error2, C](
    mux: IRTServerMultiplexor[F, C],
    extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
    dsl: Http4sDsl[F[Throwable, _]],
  )(implicit AT: Async[F[Throwable, _]]
  ): HttpRoutes[F[Throwable, _]] = {
    import dsl._
    HttpRoutes.of[F[Throwable, _]] {
      case GET -> Root / "mcp" / "tools" / "list" =>
        Ok(toolsListJson)

      case req @ POST -> Root / "mcp" / "tools" / "call" =>
        req.as[Json].flatMap { body =>
          val nameOpt  = body.hcursor.downField("name").as[String].toOption
          val argsJson = body.hcursor.downField("arguments").as[Json].getOrElse(Json.obj())
          nameOpt match {
            case Some(`toolName_greet`) =>
              call(req, argsJson, methodId_greet, wrap = true, extractCtx, mux, dsl)
            case Some(`toolName_alternative`) =>
              call(req, argsJson, methodId_alternative, wrap = true, extractCtx, mux, dsl)
            case Some(other) if other.startsWith("izumi.r2.idealingua.test.generated.GreeterService.") =>
              Ok(mcpError(-32601, s"Method not found: $other"))
            case _ =>
              NotFound()
          }
        }
    }
  }

  private def call[F[+_, +_]: IO2: Error2, C](
    req: Request[F[Throwable, _]],
    args: Json,
    methodId: IRTMethodId,
    wrap: Boolean,
    extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
    mux: IRTServerMultiplexor[F, C],
    dsl: Http4sDsl[F[Throwable, _]],
  )(implicit AT: Async[F[Throwable, _]]
  ): F[Throwable, Response[F[Throwable, _]]] = {
    import dsl._
    (for {
      ctx <- extractCtx(req)
      raw <- mux.invokeMethod(methodId)(ctx, args)
    } yield {
      val shaped = if (wrap) Json.obj("result" -> raw) else raw
      Json.obj(
        "content" -> Json.arr(Json.obj(
          "type" -> Json.fromString("text"),
          "text" -> Json.fromString(shaped.noSpaces),
        )),
        "structuredContent" -> shaped,
        "isError"           -> Json.False,
      )
    }).sandboxExit.flatMap {
      case Exit.Success(j) => Ok(j)
      case Exit.Error(_: IRTMissingHandlerException, _) =>
        Ok(mcpError(-32601, s"Method not found: ${methodId.service.value}.${methodId.methodId.value}"))
      case Exit.Error(e: IRTUnparseableDataException, _) =>
        Ok(mcpError(-32700, s"Parse error: ${safeMsg(e)}"))
      case Exit.Error(e: IRTTypeMismatchException, _) =>
        Ok(mcpError(-32602, s"Invalid arguments (type mismatch): ${safeMsg(e)}"))
      case Exit.Error(e: IRTDecodingException, _) =>
        Ok(mcpError(-32602, s"Invalid arguments: ${safeMsg(e)}"))
      case Exit.Error(e: _root_.io.circe.Error, _) =>
        Ok(mcpError(-32602, s"Invalid arguments: ${safeMsg(e)}"))
      case Exit.Error(e: IRTLimitReachedException, _) =>
        Ok(mcpError(-32000, s"Rate limit exceeded: ${safeMsg(e)}"))
      case Exit.Error(_: IRTUnathorizedRequestContextException, _) =>
        Ok(mcpError(-32001, "Unauthorized"))
      case Exit.Error(_: IRTGenericFailure, _) =>
        Ok(mcpError(-32603, "Internal error"))
      case _ =>
        Ok(mcpError(-32603, "Internal error"))
    }
  }

  private def safeMsg(t: Throwable): String = {
    val m = t.getMessage
    if (m == null) t.getClass.getSimpleName else m
  }

  private def mcpError(code: Int, msg: String): Json = Json.obj(
    "content"           -> Json.arr(Json.obj(
      "type" -> Json.fromString("text"),
      "text" -> Json.fromString(msg),
    )),
    "structuredContent" -> Json.obj(
      "error" -> Json.obj(
        "code"    -> Json.fromInt(code),
        "message" -> Json.fromString(msg),
      )
    ),
    "isError"           -> Json.True,
  )
}
