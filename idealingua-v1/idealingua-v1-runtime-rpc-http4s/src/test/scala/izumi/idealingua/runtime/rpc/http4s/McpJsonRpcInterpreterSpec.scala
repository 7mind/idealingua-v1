package izumi.idealingua.runtime.rpc.http4s

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.nowarn

import cats.data.OptionT
import io.circe.Json
import izumi.idealingua.runtime.rpc.{
  IRTMethodId,
  IRTMethodName,
  IRTServerMethod,
  IRTServerMultiplexor,
  IRTServiceId,
}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.scalatest.wordspec.AnyWordSpec

/** Single-service interpreter spec.
  *
  * Constructs the data-driven [[McpJsonRpcRoutes]] over a single in-memory
  * [[IRTServerMultiplexor]] holding ONE recording method, issues a `tools/call`
  * JSON-RPC request for one fully-qualified tool name, and asserts:
  *   (a) dispatch reaches `mux.invokeMethod(methodId)(ctx, args)` for the
  *       expected `IRTMethodId` (verified via a recording stub that captures
  *       the methodId, context, and parsed body it was invoked with), and
  *   (b) the returned `CallToolResult` envelope (content / structuredContent /
  *       isError) is shaped correctly per that tool's static `wrap` flag —
  *       both `wrap = true` (raw under `structuredContent.result`) and
  *       `wrap = false` (raw IS `structuredContent`).
  *
  * The BIO + cats `Async` wiring mirrors `McpBridgeRoundtripSpec`.
  */
@nowarn("msg=unused value of type org.scalatest.compatible.Assertion")
final class McpJsonRpcInterpreterSpec extends AnyWordSpec {

  type BIO[+E, +A] = zio.IO[E, A]

  implicit val unsafeRun: izumi.functional.bio.UnsafeRun2[BIO]      = Http4sTransportTest.IO2R
  implicit val io2: izumi.functional.bio.IO2[BIO]                   = izumi.functional.bio.impl.AsyncZio
  implicit val async2: izumi.functional.bio.Async2[BIO]             = izumi.functional.bio.impl.AsyncZio
  implicit val asyncThrowable: cats.effect.Async[BIO[Throwable, *]] = zio.interop.catz.asyncInstance

  private val dsl: Http4sDsl[BIO[Throwable, *]] = Http4sDsl.apply[BIO[Throwable, *]]

  private val ctxToken                          = "ctx-token"
  private val extractCtx: Request[BIO[Throwable, *]] => BIO[Throwable, String] =
    _ => izumi.functional.bio.F.pure(ctxToken)

  private val svcId    = IRTServiceId("Calc")
  private val methodId = IRTMethodId(svcId, IRTMethodName("inc"))
  private val toolName = "test.demo.Calc.inc"

  /** Recording method: captures the (context, parsedBody) it was invoked with
    * and returns a fixed raw result, so the test can assert dispatch reached
    * the expected `invokeMethod(methodId)(ctx, args)`. */
  private final class RecordingMethod(
    val mid: IRTMethodId,
    rawResult: Json,
  ) extends IRTServerMethod[BIO, String] {
    val recorded: AtomicReference[Option[(IRTMethodId, String, Json)]] = new AtomicReference(None)
    override def methodId: IRTMethodId = mid
    override def invoke(context: String, parsedBody: Json): BIO[Throwable, Json] = {
      recorded.set(Some((mid, context, parsedBody)))
      izumi.functional.bio.F.pure(rawResult)
    }
  }

  private val rawResult: Json = Json.obj("value" -> Json.fromInt(42))

  private def buildInterpreter(wrap: Boolean): (McpJsonRpcRoutes[BIO, String], RecordingMethod) = {
    val method = new RecordingMethod(methodId, rawResult)
    val mux: IRTServerMultiplexor[BIO, String] =
      new IRTServerMultiplexor.FromMethods[BIO, String](Map(methodId -> method))
    val dispatch: Map[String, (IRTMethodId, Boolean)] = Map(toolName -> (methodId, wrap))
    val interp = new McpJsonRpcRoutes[BIO, String](
      mux             = mux,
      toolsListJson   = Json.obj("tools" -> Json.arr(Json.obj("name" -> Json.fromString(toolName)))),
      dispatch        = dispatch,
      extractCtx      = extractCtx,
      serverName      = "test-server",
      serverVersion   = "0.0.1",
      protocolVersion = McpJsonRpcRoutes.DefaultProtocolVersion,
      mountPath       = "/mcp",
      dsl             = dsl,
    )
    (interp, method)
  }

  private def runUnsafe[A](io: BIO[Throwable, A]): A =
    unsafeRun.unsafeRunSync(io) match {
      case izumi.functional.bio.Exit.Success(v)    => v
      case f: izumi.functional.bio.Exit.Failure[?] => throw f.trace.toThrowable
    }

  private def post(routes: HttpRoutes[BIO[Throwable, *]], body: Json): Json = {
    implicit val je: EntityEncoder[BIO[Throwable, *], Json] = jsonEncoderOf[BIO[Throwable, *], Json]
    implicit val jd: EntityDecoder[BIO[Throwable, *], Json] = jsonDecoder[BIO[Throwable, *]]
    val req = Request[BIO[Throwable, *]](Method.POST, uri"/mcp").withEntity(body)
    runUnsafe {
      OptionT(routes.run(req).value)
        .getOrElseF(izumi.functional.bio.F.pure(Response[BIO[Throwable, *]](Status.NotFound)))
        .flatMap(resp => resp.as[Json])
    }
  }

  private def toolsCall(args: Json): Json =
    Json.obj(
      "jsonrpc" -> Json.fromString("2.0"),
      "id"      -> Json.fromInt(1),
      "method"  -> Json.fromString("tools/call"),
      "params"  -> Json.obj("name" -> Json.fromString(toolName), "arguments" -> args),
    )

  "McpJsonRpcRoutes (data-driven interpreter)" should {

    "dispatch tools/call DIRECTLY to mux.invokeMethod for the expected IRTMethodId" in {
      val (interp, method) = buildInterpreter(wrap = false)
      val args             = Json.obj("n" -> Json.fromInt(41))
      val _                = post(interp.routes, toolsCall(args))

      val recorded = method.recorded.get
      assert(recorded.isDefined, "invokeMethod was never reached")
      val (gotMid, gotCtx, gotBody) = recorded.get
      assert(gotMid == methodId, s"dispatched to wrong methodId: $gotMid")
      assert(gotCtx == ctxToken, s"context not threaded through extractCtx: $gotCtx")
      assert(gotBody == args, s"parsed body not forwarded verbatim: $gotBody")
    }

    "shape CallToolResult with wrap=false (raw IS structuredContent)" in {
      val (interp, _) = buildInterpreter(wrap = false)
      val body        = post(interp.routes, toolsCall(Json.obj()))

      val result = body.hcursor.downField("result").focus.getOrElse(fail(s"missing JSON-RPC result: $body"))
      assert(result.hcursor.get[Boolean]("isError").toOption.contains(false), s"expected isError:false in $result")

      val sc = result.hcursor.downField("structuredContent").focus.getOrElse(fail(s"missing structuredContent: $result"))
      assert(sc == rawResult, s"wrap=false: structuredContent should be the raw result, got $sc")

      val text = result.hcursor.downField("content").downN(0).get[String]("text").toOption
        .getOrElse(fail(s"missing content[0].text in $result"))
      assert(text == rawResult.noSpaces, s"content[0].text should mirror structuredContent.noSpaces, got $text")
    }

    "shape CallToolResult with wrap=true (raw under structuredContent.result)" in {
      val (interp, _) = buildInterpreter(wrap = true)
      val body        = post(interp.routes, toolsCall(Json.obj()))

      val result = body.hcursor.downField("result").focus.getOrElse(fail(s"missing JSON-RPC result: $body"))
      assert(result.hcursor.get[Boolean]("isError").toOption.contains(false), s"expected isError:false in $result")

      val sc      = result.hcursor.downField("structuredContent").focus.getOrElse(fail(s"missing structuredContent: $result"))
      val wrapped = Json.obj("result" -> rawResult)
      assert(sc == wrapped, s"wrap=true: structuredContent should be {result: raw}, got $sc")

      val text = result.hcursor.downField("content").downN(0).get[String]("text").toOption
        .getOrElse(fail(s"missing content[0].text in $result"))
      assert(text == wrapped.noSpaces, s"content[0].text should mirror structuredContent.noSpaces, got $text")
    }

    "return -32601 for an unknown tool name (not in dispatch map)" in {
      val (interp, method) = buildInterpreter(wrap = false)
      val req = Json.obj(
        "jsonrpc" -> Json.fromString("2.0"),
        "id"      -> Json.fromInt(2),
        "method"  -> Json.fromString("tools/call"),
        "params"  -> Json.obj("name" -> Json.fromString("test.demo.Calc.nope"), "arguments" -> Json.obj()),
      )
      val body = post(interp.routes, req)
      val err  = body.hcursor.downField("error").focus.getOrElse(fail(s"expected JSON-RPC error: $body"))
      assert(err.hcursor.get[Int]("code").toOption.contains(-32601), s"expected -32601, got $err")
      assert(method.recorded.get.isEmpty, "invokeMethod must NOT be reached for an unknown tool")
    }
  }
}
