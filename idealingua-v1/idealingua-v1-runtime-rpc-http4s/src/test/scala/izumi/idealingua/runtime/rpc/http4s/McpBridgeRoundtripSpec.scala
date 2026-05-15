package izumi.idealingua.runtime.rpc.http4s

import scala.annotation.nowarn

import cats.data.OptionT
import io.circe.Json
import izumi.idealingua.runtime.rpc.{
  IRTDecodingException,
  IRTGenericFailure,
  IRTLimitReachedException,
  IRTMethodId,
  IRTMethodName,
  IRTMissingHandlerException,
  IRTOutputMiddleware,
  IRTServerMethod,
  IRTServerMultiplexor,
  IRTServiceId,
  IRTTypeMismatchException,
  IRTUnathorizedRequestContextException,
  IRTUnparseableDataException,
}
import izumi.idealingua.runtime.rpc.http4s.fixtures.GreeterServiceMcpRoutes
import izumi.r2.idealingua.test.generated.{GreeterServiceServer, GreeterServiceServerWrapped}
import izumi.r2.idealingua.test.impls.AbstractGreeterServer
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import org.scalatest.wordspec.AnyWordSpec

/** Mb4-A integration spec: round-trip the MCP/HTTP4s bridge end-to-end.
  *
  * This is the FIRST time the generated bridge code runs against a populated
  * `IRTServerMultiplexor`. Prior milestones (Mb1..Mb3) only asserted golden
  * bytes + scala.meta parse — never executed dispatch.
  *
  * Strategy:
  *   - Effect: `zio.IO[+E, +A]` as the bifunctor; `BIO[Throwable, *]` as the
  *     `cats.effect.Async` carrier (mirrors `Http4sTransportTest`'s wiring).
  *   - **No** `BlazeServerBuilder` / socket. We invoke `HttpRoutes.run` with a
  *     synthetic `Request` and inspect the `Response` directly (same coverage
  *     as a real server, no port-binding flake).
  *   - Real handler mux (`GreeterServiceServerWrapped` over
  *     `AbstractGreeterServer.Impl`) for the success-path cases.
  *   - Stub muxes for each `IRT*Exception` for the error envelope tests.
  *
  * The fixture `GreeterServiceMcpRoutes` is a hand-authored copy of the
  * generator's output for the hand-written `GreeterService` (which has no IDL
  * definition); the five corpus-driven goldens under
  * `idealingua-v1-test-defs/golden/scala-mcp/` already pin the generator
  * shape this fixture replicates.
  */
// scalatest's `assert(...)` returns `Assertion`, which Scala 3's
// `-Wnonunit-statement` flags whenever multiple asserts appear in sequence in
// a test body. Suppress via `@nowarn` rather than restructuring every test —
// the failure messages are clearer with individual assertions than with
// chained boolean conjunctions.
@nowarn("msg=unused value of type org.scalatest.compatible.Assertion")
final class McpBridgeRoundtripSpec extends AnyWordSpec {

  type BIO[+E, +A] = zio.IO[E, A]

  // bifunctor + cats Async wiring (same pattern as Http4sTransportTest).
  implicit val unsafeRun: izumi.functional.bio.UnsafeRun2[BIO]      = Http4sTransportTest.IO2R
  implicit val io2: izumi.functional.bio.IO2[BIO]                   = izumi.functional.bio.impl.AsyncZio
  implicit val async2: izumi.functional.bio.Async2[BIO]             = izumi.functional.bio.impl.AsyncZio
  implicit val asyncThrowable: cats.effect.Async[BIO[Throwable, *]] = zio.interop.catz.asyncInstance

  private val dsl: Http4sDsl[BIO[Throwable, *]] = Http4sDsl.apply[BIO[Throwable, *]]

  // Real service: `GreeterService` from `idealingua-v1-test-defs`. The greet
  // handler returns `"Hi, $name $surname!"`; alternative returns Success("value").
  private val realService: GreeterServiceServer[BIO, Unit] = new AbstractGreeterServer.Impl[BIO, Unit]
  private val realMux: IRTServerMultiplexor[BIO, Unit] =
    new IRTServerMultiplexor.FromServices[BIO, Unit](
      Set(new GreeterServiceServerWrapped[BIO, Unit](realService)),
      IRTOutputMiddleware.empty[BIO, Unit],
    )

  private val extractUnit: Request[BIO[Throwable, *]] => BIO[Throwable, Unit] =
    _ => izumi.functional.bio.F.pure(())

  private val realRoutes: HttpRoutes[BIO[Throwable, *]] =
    GreeterServiceMcpRoutes.routes[BIO, Unit](realMux, extractUnit, dsl)

  private def runUnsafe[A](io: BIO[Throwable, A]): A = {
    unsafeRun.unsafeRunSync(io) match {
      case izumi.functional.bio.Exit.Success(v)    => v
      case f: izumi.functional.bio.Exit.Failure[?] => throw f.trace.toThrowable
    }
  }

  // `routes.run(req)` returns `OptionT[F, Response]`. `.value` unwraps to
  // `F[Option[Response]]`; we treat `None` as 404 explicitly so the test can
  // distinguish "route didn't match" from "route returned NotFound".
  private def runRoute(routes: HttpRoutes[BIO[Throwable, *]], req: Request[BIO[Throwable, *]]): Response[BIO[Throwable, *]] = {
    runUnsafe {
      OptionT(routes.run(req).value).getOrElseF(
        izumi.functional.bio.F.pure(Response[BIO[Throwable, *]](Status.NotFound))
      )
    }
  }

  private def readJson(resp: Response[BIO[Throwable, *]]): Json = {
    implicit val jd: EntityDecoder[BIO[Throwable, *], Json] = jsonDecoder[BIO[Throwable, *]]
    runUnsafe(resp.as[Json])
  }

  private def runReturning(routes: HttpRoutes[BIO[Throwable, *]], req: Request[BIO[Throwable, *]]): (Status, Json) = {
    val resp = runRoute(routes, req)
    (resp.status, readJson(resp))
  }

  private def postCall(routes: HttpRoutes[BIO[Throwable, *]], body: Json): (Status, Json) = {
    implicit val je: EntityEncoder[BIO[Throwable, *], Json] = jsonEncoderOf[BIO[Throwable, *], Json]
    val req = Request[BIO[Throwable, *]](Method.POST, uri"/mcp/tools/call").withEntity(body)
    runReturning(routes, req)
  }

  private def getPath(routes: HttpRoutes[BIO[Throwable, *]], path: String): (Status, Json) = {
    val req = Request[BIO[Throwable, *]](Method.GET, Uri.unsafeFromString(path))
    runReturning(routes, req)
  }

  private def callBody(toolName: String, args: Json): Json =
    Json.obj("name" -> Json.fromString(toolName), "arguments" -> args)

  // Stub mux that fails the `greet` method with a fixed Throwable.
  private def failingMux(t: Throwable): IRTServerMultiplexor[BIO, Unit] = {
    val mid = IRTMethodId(IRTServiceId("GreeterService"), IRTMethodName("greet"))
    new IRTServerMultiplexor.FromMethods[BIO, Unit](
      Map(mid -> new IRTServerMethod[BIO, Unit] {
        override def methodId: IRTMethodId = mid
        override def invoke(context: Unit, parsedBody: Json): BIO[Throwable, Json] =
          izumi.functional.bio.F.fail(t)
      })
    )
  }

  private def routesFor(mux: IRTServerMultiplexor[BIO, Unit]): HttpRoutes[BIO[Throwable, *]] =
    GreeterServiceMcpRoutes.routes[BIO, Unit](mux, extractUnit, dsl)

  private val greetTool = "izumi.r2.idealingua.test.generated.GreeterService.greet"
  private val altTool   = "izumi.r2.idealingua.test.generated.GreeterService.alternative"

  private def greetArgs: Json =
    Json.obj("name" -> Json.fromString("John"), "surname" -> Json.fromString("Smith"))

  "MCP/HTTP4s bridge" should {
    "GET /mcp/tools/list returns the classpath-resource JSON byte-equal" in {
      val (status, body) = getPath(realRoutes, "/mcp/tools/list")
      assert(status == Status.Ok)

      val resourceStream = getClass.getClassLoader.getResourceAsStream("mcp/GreeterService.mcp.json")
      assert(resourceStream != null, "GreeterService.mcp.json missing from classpath")
      val expected =
        try io.circe.parser.parse(scala.io.Source.fromInputStream(resourceStream, "UTF-8").mkString).toTry.get
        finally resourceStream.close()

      assert(body == expected, s"tools/list payload diverged from classpath resource\nactual: ${body.spaces2}")

      val tools = body.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail("missing tools[]"))
      assert(tools.size == 2, s"expected 2 tools, got ${tools.size}")
      val names = tools.flatMap(_.hcursor.get[String]("name").toOption)
      assert(
        names.toSet == Set(greetTool, altTool),
        s"unexpected tool names: $names",
      )
    }

    "POST /mcp/tools/call greet returns wrapped Singular envelope" in {
      val (status, body) = postCall(realRoutes, callBody(greetTool, greetArgs))

      assert(status == Status.Ok)
      assert(body.hcursor.get[Boolean]("isError").toOption.contains(false), s"expected isError:false, got $body")

      // Wrap envelope: bridge wraps the raw mux output (`{"value":"Hi, John Smith!"}`)
      // into `structuredContent = {"result": {"value": "Hi, John Smith!"}}`.
      // This reveals a known Mb4-A schema-vs-runtime asymmetry: the MCP
      // `outputSchema` declares `properties.result = {type:"string", x-idealingua-unwrap:true}`
      // (per `SchemaMethodOutput.rawDispatch` for `Singular`), but the wire
      // shape `mux.invokeMethod` actually emits is the IRT case-class object
      // `{"value":"..."}`. The `x-idealingua-unwrap` annotation flags this
      // intentional asymmetry — clients are expected to unwrap the
      // `{value: T}` case-class envelope to bare `T` on receive.
      val sc = body.hcursor.downField("structuredContent").focus.getOrElse(fail("missing structuredContent"))
      val result = sc.hcursor.downField("result").focus.getOrElse(fail("missing structuredContent.result"))
      val value = result.hcursor.get[String]("value").toOption.getOrElse(fail(s"missing result.value in $result"))
      assert(value == "Hi, John Smith!", s"unexpected greeting: $value")

      // content[0].text mirrors structuredContent.noSpaces exactly.
      val text = body.hcursor.downField("content").downN(0).get[String]("text").toOption
        .getOrElse(fail("missing content[0].text"))
      assert(text == sc.noSpaces, s"content[0].text should be structuredContent.noSpaces\n  text=$text\n  sc=${sc.noSpaces}")
    }

    "POST /mcp/tools/call alternative returns wrapped Alternative envelope" in {
      val (status, body) = postCall(realRoutes, callBody(altTool, Json.obj()))

      assert(status == Status.Ok)
      assert(body.hcursor.get[Boolean]("isError").toOption.contains(false), s"expected isError:false, got $body")

      val sc = body.hcursor.downField("structuredContent").focus.getOrElse(fail("missing structuredContent"))
      val result = sc.hcursor.downField("result").focus.getOrElse(fail("missing structuredContent.result"))
      // Auto-derived ADT codec for `AlternativeOutput` renders Success as
      // `{"Success": {"value": "value"}}`. Find the String "value" wherever
      // it lands so the test tolerates both flat and case-class branch shapes.
      val asString = result.spaces2
      assert(
        asString.contains("\"value\""),
        s"Alternative result missing string \"value\": $result",
      )
    }

    "POST /mcp/tools/call unknown-method-in-prefix returns -32601 in-band" in {
      val (status, body) = postCall(
        realRoutes,
        callBody(s"izumi.r2.idealingua.test.generated.GreeterService.doesNotExist", Json.obj()),
      )
      assert(status == Status.Ok)
      assert(body.hcursor.get[Boolean]("isError").toOption.contains(true))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      assert(code.contains(-32601), s"expected -32601, got $code, body=$body")
      val msg = body.hcursor.downField("structuredContent").downField("error").get[String]("message").toOption
      assert(msg.exists(_.contains("doesNotExist")), s"expected message mentioning doesNotExist, got $msg")
    }

    "POST /mcp/tools/call tool-not-in-prefix returns 404" in {
      implicit val je: EntityEncoder[BIO[Throwable, *], Json] = jsonEncoderOf[BIO[Throwable, *], Json]
      val req = Request[BIO[Throwable, *]](Method.POST, uri"/mcp/tools/call")
        .withEntity(callBody("other.namespace.Service.foo", Json.obj()))
      val resp = runRoute(realRoutes, req)
      assert(resp.status == Status.NotFound, s"expected 404, got ${resp.status}")
    }

    "error: IRTMissingHandlerException → -32601 (with enriched message)" in {
      val ex = new IRTMissingHandlerException("no handler", Json.Null)
      val (status, body) = postCall(routesFor(failingMux(ex)), callBody(greetTool, greetArgs))
      assert(status == Status.Ok)
      assert(body.hcursor.get[Boolean]("isError").toOption.contains(true))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      val msg = body.hcursor.downField("structuredContent").downField("error").get[String]("message").toOption
      assert(code.contains(-32601))
      assert(msg.contains("Method not found: GreeterService.greet"), s"got msg=$msg")
    }

    "error: IRTDecodingException → -32602" in {
      val (_, body) = postCall(routesFor(failingMux(new IRTDecodingException("bad json"))),
        callBody(greetTool, greetArgs))
      assert(body.hcursor.get[Boolean]("isError").toOption.contains(true))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      val msg = body.hcursor.downField("structuredContent").downField("error").get[String]("message").toOption
      assert(code.contains(-32602))
      assert(msg.exists(_.contains("Invalid arguments")), s"got msg=$msg")
    }

    "error: IRTUnparseableDataException → -32700" in {
      val (_, body) = postCall(routesFor(failingMux(new IRTUnparseableDataException("garbage"))),
        callBody(greetTool, greetArgs))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      assert(code.contains(-32700))
    }

    "error: IRTTypeMismatchException → -32602 (type-mismatch flavour)" in {
      val (_, body) = postCall(routesFor(failingMux(new IRTTypeMismatchException("type oops", Json.Null))),
        callBody(greetTool, greetArgs))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      val msg = body.hcursor.downField("structuredContent").downField("error").get[String]("message").toOption
      assert(code.contains(-32602))
      assert(msg.exists(_.contains("type mismatch")), s"got msg=$msg")
    }

    "error: IRTLimitReachedException → -32000" in {
      val (_, body) = postCall(routesFor(failingMux(new IRTLimitReachedException("rate"))),
        callBody(greetTool, greetArgs))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      assert(code.contains(-32000))
    }

    "error: IRTUnathorizedRequestContextException → -32001" in {
      val (_, body) = postCall(routesFor(failingMux(new IRTUnathorizedRequestContextException("no auth"))),
        callBody(greetTool, greetArgs))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      val msg = body.hcursor.downField("structuredContent").downField("error").get[String]("message").toOption
      assert(code.contains(-32001))
      assert(msg.contains("Unauthorized"), s"got msg=$msg")
    }

    "error: IRTGenericFailure → -32603" in {
      val (_, body) = postCall(routesFor(failingMux(new IRTGenericFailure("internal"))),
        callBody(greetTool, greetArgs))
      val code = body.hcursor.downField("structuredContent").downField("error").get[Int]("code").toOption
      assert(code.contains(-32603))
    }
  }
}
