package izumi.idealingua.runtime.rpc.http4s

import scala.annotation.nowarn
import scala.concurrent.duration._

import io.circe.Json
import izumi.functional.bio.Exit
import izumi.idealingua.runtime.rpc.{IRTOutputMiddleware, IRTWrappedService}
import mcpdemo.{
  CalcServer,
  CalcServerImpl,
  CalcWrappedServer,
  ShapesServer,
  ShapesServerImpl,
  ShapesWrappedServer,
}
import org.http4s.{EntityDecoder, EntityEncoder, Method, Request, Status, Uri}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Server
import org.scalatest.wordspec.AnyWordSpec

/** Multi-service runtime spec.
  *
  * Mounts TWO services on ONE real Blaze socket through the data-driven
  * [[McpJsonRpcRoutes]] interpreter assembled by [[McpServiceAssembler]]:
  * `Shapes` (13 tools, every `DefMethod.Output` shape — Void / Singular
  * primitive / Singular DTO / Struct / Algebraic / Alternative-Singular /
  * Alternative-Void) and `Calc` (3 arithmetic tools). The single `/mcp`
  * JSON-RPC endpoint is served on one `bindHttp(0)` Blaze socket and driven
  * over a `BlazeClientBuilder` client.
  *
  * Assertions:
  *   (a) one `tools/list` returns the UNION of all tools — the 13
  *       `mcpdemo.Shapes.*` plus the 3 `mcpdemo.Calc.*` names, exact set
  *       equality (count == 16);
  *   (b) a `tools/call` succeeds against ≥1 method of EACH service
  *       (representative per-Output-variant Shapes calls AND `Calc.add`),
  *       returning the correct result;
  *   (c) no cross-service tool-name collision — union size == sum of
  *       per-service counts (13 + 3 == 16).
  *
  * Wrap policy observed on the wire (per `OutputWrapPolicy.isWrapped`):
  *   - Singular (any inner type)  → `structuredContent = { "result": <raw> }`.
  *   - Void / Struct / Algebraic / Alternative → `structuredContent = <raw>`.
  */
@nowarn("msg=unused value of type org.scalatest.compatible.Assertion")
final class McpBridgeRealServerSpec extends AnyWordSpec {

  type BIO[+E, +A] = zio.IO[E, A]

  implicit val unsafeRun: izumi.functional.bio.UnsafeRun2[BIO]      = Http4sTransportTest.IO2R
  implicit val io2: izumi.functional.bio.IO2[BIO]                   = izumi.functional.bio.impl.AsyncZio
  implicit val async2: izumi.functional.bio.Async2[BIO]             = izumi.functional.bio.impl.AsyncZio
  implicit val asyncThrowable: cats.effect.Async[BIO[Throwable, *]] = zio.interop.catz.asyncInstance

  private val dsl: Http4sDsl[BIO[Throwable, *]] = Http4sDsl.apply[BIO[Throwable, *]]

  private val shapesService: ShapesServer[BIO, Unit] = new ShapesServerImpl[BIO, Unit]
  private val calcService: CalcServer[BIO, Unit]     = new CalcServerImpl[BIO, Unit]

  // Assemble BOTH services into ONE multiplexor + union tools/list + dispatch
  // map, then build ONE JSON-RPC interpreter over the result. The metas are
  // discovered from the wrapped servers' `mcpResource` pointers rather than
  // naming `*Mcp.resource` directly — the wrapped servers carry the pointer
  // exactly as a generated `*WrappedServer` does under `emitMcpBridge`.
  private val wrappedServers: Seq[IRTWrappedService[BIO, Unit]] = Seq(
    new ShapesWrappedServer[BIO, Unit](shapesService),
    new CalcWrappedServer[BIO, Unit](calcService),
  )
  private val assembly =
    McpServiceAssembler.assemble[BIO, Unit](
      wrappedServers.map(ws => ws -> McpServiceLoader.load(ws.mcpResource.get)),
      IRTOutputMiddleware.empty[BIO, Unit],
    )

  private val extractUnit: Request[BIO[Throwable, *]] => BIO[Throwable, Unit] =
    _ => izumi.functional.bio.F.pure(())

  private val interpreter = new McpJsonRpcRoutes[BIO, Unit](
    mux             = assembly.mux,
    toolsListJson   = assembly.toolsListJson,
    dispatch        = assembly.dispatch,
    extractCtx      = extractUnit,
    serverName      = "mcpdemo-multi",
    serverVersion   = "0.1.0",
    protocolVersion = McpJsonRpcRoutes.DefaultProtocolVersion,
    mountPath       = "/mcp",
    dsl             = dsl,
  )

  private val routes = interpreter.routes

  private def runUnsafe[A](io: BIO[Throwable, A]): A =
    unsafeRun.unsafeRunSync(io) match {
      case Exit.Success(v)               => v
      case f: Exit.Failure[?] @unchecked => throw f.trace.toThrowable
    }

  private def withServer[A](body: Uri => A): A = {
    val resource: cats.effect.Resource[BIO[Throwable, *], Server] =
      BlazeServerBuilder[BIO[Throwable, *]]
        .bindHttp(0, "127.0.0.1")
        .withHttpApp(routes.orNotFound)
        .resource
    runUnsafe(resource.use { srv =>
      val base = Uri.unsafeFromString(s"http://127.0.0.1:${srv.address.getPort}/mcp")
      izumi.functional.bio.F.syncThrowable(body(base))
    })
  }

  private def client[A](body: org.http4s.client.Client[BIO[Throwable, *]] => BIO[Throwable, A]): A =
    runUnsafe {
      BlazeClientBuilder[BIO[Throwable, *]]
        .withRequestTimeout(5.seconds)
        .resource
        .use(body)
    }

  private def post(uri: Uri, body: Json): (Status, Json) = {
    implicit val je: EntityEncoder[BIO[Throwable, *], Json] = jsonEncoderOf[BIO[Throwable, *], Json]
    implicit val jd: EntityDecoder[BIO[Throwable, *], Json] = jsonDecoder[BIO[Throwable, *]]
    val req = Request[BIO[Throwable, *]](Method.POST, uri).withEntity(body)
    client(c => c.run(req).use(resp => resp.as[Json].map(j => (resp.status, j))))
  }

  // ---- JSON-RPC envelope helpers --------------------------------------

  private def rpc(method: String, params: Json): Json =
    Json.obj(
      "jsonrpc" -> Json.fromString("2.0"),
      "id"      -> Json.fromInt(1),
      "method"  -> Json.fromString(method),
      "params"  -> params,
    )

  private def callParams(toolName: String, args: Json): Json =
    Json.obj("name" -> Json.fromString(toolName), "arguments" -> args)

  /** Pull the JSON-RPC `result` from a successful response envelope. */
  private def rpcResult(status: Status, body: Json): Json = {
    assert(status == Status.Ok, s"expected 200, got $status; body=$body")
    body.hcursor.downField("result").focus.getOrElse(throw new AssertionError(s"missing JSON-RPC result: $body"))
  }

  /** Successful `CallToolResult` → structuredContent (isError:false). */
  private def assertOk(status: Status, body: Json): Json = {
    val result  = rpcResult(status, body)
    val isError = result.hcursor.get[Boolean]("isError").toOption
    assert(isError.contains(false), s"expected isError:false, got $result")
    result.hcursor.downField("structuredContent").focus.getOrElse(throw new AssertionError(s"missing structuredContent: $result"))
  }

  /** Singular outputs: pull the `result` field. Other shapes: sc as-is. */
  private def resultOf(sc: Json): Json =
    sc.hcursor.downField("result").focus.getOrElse(sc)

  private def toolsListNames(base: Uri): Set[String] = {
    val (status, body) = post(base, rpc("tools/list", Json.obj()))
    val result         = rpcResult(status, body)
    val tools = result.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail(s"missing tools[]: $result"))
    tools.flatMap(_.hcursor.get[String]("name").toOption).toSet
  }

  private val shapesTool = "mcpdemo.Shapes."
  private val calcTool   = "mcpdemo.Calc."

  private val shapesNames: Set[String] = Set(
    "ping", "upper", "add", "echo", "divmod", "reverse", "invertMap",
    "maybeUpper", "nextColor", "makeProfile", "pay", "divideSafe", "noteValue",
  ).map(shapesTool + _)

  private val calcNames: Set[String] = Set("add", "sub", "mul").map(calcTool + _)

  "MCP multi-service bridge over real Blaze socket" should {

    "(a) tools/list returns EXACTLY the union of both services' tools (13 Shapes + 3 Calc == 16)" in withServer { base =>
      val names    = toolsListNames(base)
      val expected = shapesNames ++ calcNames
      assert(names == expected, s"tools/list union mismatch — got $names, expected $expected")
      assert(names.size == 16, s"expected 16 union tools, got ${names.size}")
    }

    "(c) no cross-service collision — union size == sum of per-service counts (13 + 3)" in withServer { base =>
      val names = toolsListNames(base)
      assert(shapesNames.size == 13, s"Shapes should expose 13 tools, got ${shapesNames.size}")
      assert(calcNames.size == 3, s"Calc should expose 3 tools, got ${calcNames.size}")
      // Disjoint sets: no name appears in both services.
      assert((shapesNames intersect calcNames).isEmpty, "Shapes and Calc tool names must not collide")
      assert(names.size == shapesNames.size + calcNames.size, s"union ${names.size} != 13 + 3")
    }

    // ---- (b) per-service successful tools/call ------------------------

    "(b) Calc.add (second service, Singular i64 out) returns the sum" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(calcTool + "add", Json.obj("a" -> Json.fromLong(7), "b" -> Json.fromLong(35))))))
      val v  = resultOf(sc).asNumber.flatMap(_.toLong).getOrElse(fail(s"expected number in $sc"))
      assert(v == 42L, s"Calc.add(7,35) should be 42, got $v")
    }

    "(b) Calc.mul (second service) returns the product" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(calcTool + "mul", Json.obj("a" -> Json.fromLong(6), "b" -> Json.fromLong(7))))))
      val v  = resultOf(sc).asNumber.flatMap(_.toLong).getOrElse(fail(s"expected number in $sc"))
      assert(v == 42L, s"Calc.mul(6,7) should be 42, got $v")
    }

    // ---- representative Shapes per-Output-variant calls ---------------

    "Shapes.ping (Void output)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "ping", Json.obj()))))
      assert(sc == Json.obj(), s"expected {}, got $sc")
    }

    "Shapes.upper (Singular str)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "upper", Json.obj("s" -> Json.fromString("hello"))))))
      val v  = resultOf(sc).asString.getOrElse(fail(s"expected string in $sc"))
      assert(v == "HELLO")
    }

    "Shapes.add (multi-primitive in, Singular i64 out)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "add", Json.obj("a" -> Json.fromLong(7), "b" -> Json.fromLong(35))))))
      val v  = resultOf(sc).asNumber.flatMap(_.toLong).getOrElse(fail(s"expected number in $sc"))
      assert(v == 42L)
    }

    "Shapes.echo (DTO in, DTO out)" in withServer { base =>
      val args = Json.obj("req" -> Json.obj("msg" -> Json.fromString("hi"), "n" -> Json.fromInt(3)))
      val sc   = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "echo", args))))
      val r    = resultOf(sc)
      assert(r.hcursor.get[String]("echo").toOption.contains("hi"), s"echo: $r")
      assert(r.hcursor.get[Int]("count").toOption.contains(3),       s"count: $r")
    }

    "Shapes.divmod (Struct out — no .result wrap)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "divmod", Json.obj("a" -> Json.fromLong(17), "b" -> Json.fromLong(5))))))
      assert(sc.hcursor.get[Long]("quotient").toOption.contains(3L),  s"quotient: $sc")
      assert(sc.hcursor.get[Long]("remainder").toOption.contains(2L), s"remainder: $sc")
    }

    "Shapes.reverse (list in/out)" in withServer { base =>
      val args = Json.obj("items" -> Json.arr(Json.fromString("a"), Json.fromString("b"), Json.fromString("c")))
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "reverse", args))))
      val v = resultOf(sc).asArray.map(_.flatMap(_.asString).toList).getOrElse(fail(s"expected array in $sc"))
      assert(v == List("c", "b", "a"))
    }

    "Shapes.invertMap (map in/out)" in withServer { base =>
      val args = Json.obj("m" -> Json.obj("k1" -> Json.fromString("v1"), "k2" -> Json.fromString("v2")))
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "invertMap", args))))
      val v = resultOf(sc).as[Map[String, String]].toOption.getOrElse(fail(s"expected map in $sc"))
      assert(v == Map("v1" -> "k1", "v2" -> "k2"))
    }

    "Shapes.maybeUpper Some (opt in/out)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "maybeUpper", Json.obj("s" -> Json.fromString("ok"))))))
      val r  = resultOf(sc)
      assert(r.asString.contains("OK"), s"expected 'OK', got $r")
    }

    "Shapes.maybeUpper None (opt None in)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "maybeUpper", Json.obj()))))
      val r  = resultOf(sc)
      assert(r == Json.Null, s"expected null, got $r")
    }

    "Shapes.nextColor (enum in, Singular DTO out)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "nextColor", Json.obj("c" -> Json.fromString("Red"))))))
      val r  = resultOf(sc)
      assert(r.hcursor.get[String]("color").toOption.contains("Green"), s"color: $r")
    }

    "Shapes.makeProfile (multi-arg in incl enum, Singular DTO out)" in withServer { base =>
      val args = Json.obj(
        "name"  -> Json.fromString("alice"),
        "age"   -> Json.fromInt(30),
        "color" -> Json.fromString("Blue"),
      )
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "makeProfile", args))))
      val r  = resultOf(sc)
      assert(r.hcursor.get[String]("name").toOption.contains("alice"))
      assert(r.hcursor.get[Int]("age").toOption.contains(30))
      assert(r.hcursor.get[String]("color").toOption.contains("Blue"))
    }

    "Shapes.pay (Algebraic out — PaymentOk branch)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "pay", Json.obj("amount" -> Json.fromLong(100))))))
      val r  = resultOf(sc)
      val ok = r.hcursor.downField("PaymentOk").focus.getOrElse(fail(s"expected PaymentOk branch in $r"))
      assert(ok.hcursor.get[Long]("amount").toOption.contains(100L))
      assert(ok.hcursor.get[String]("txId").toOption.contains("tx-100"))
    }

    "Shapes.pay (Algebraic out — PaymentRejected branch)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "pay", Json.obj("amount" -> Json.fromLong(-5))))))
      val r   = resultOf(sc)
      val rej = r.hcursor.downField("PaymentRejected").focus.getOrElse(fail(s"expected PaymentRejected branch in $r"))
      assert(rej.hcursor.get[Int]("code").toOption.contains(422))
    }

    "Shapes.divideSafe (Alternative Singular!!Singular — success)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "divideSafe", Json.obj("a" -> Json.fromLong(10), "b" -> Json.fromLong(2))))))
      val raw = sc.spaces2
      assert(raw.contains("\"Success\""), s"expected Success branch in $sc")
      assert(raw.contains("5"),           s"expected the quotient 5 in $sc")
    }

    "Shapes.divideSafe (Alternative Singular!!Singular — failure)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "divideSafe", Json.obj("a" -> Json.fromLong(1), "b" -> Json.fromLong(0))))))
      val raw = sc.spaces2
      assert(raw.contains("\"Failure\""), s"expected Failure branch in $sc")
      assert(raw.contains("division by zero"), s"expected fail msg in $sc")
    }

    "Shapes.noteValue (Alternative Void!!Singular — success)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "noteValue", Json.obj("v" -> Json.fromInt(7))))))
      val raw = sc.spaces2
      assert(raw.contains("\"Success\""), s"expected Success branch in $sc")
    }

    "Shapes.noteValue (Alternative Void!!Singular — failure)" in withServer { base =>
      val sc = assertOk.tupled(post(base, rpc("tools/call", callParams(shapesTool + "noteValue", Json.obj("v" -> Json.fromInt(-1))))))
      val raw = sc.spaces2
      assert(raw.contains("\"Failure\""), s"expected Failure branch in $sc")
      assert(raw.contains("negative"),    s"expected fail msg in $sc")
    }

    "every union tool entry exposes inputSchema and outputSchema" in withServer { base =>
      val (status, body) = post(base, rpc("tools/list", Json.obj()))
      val result         = rpcResult(status, body)
      val tools = result.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail("missing tools[]"))
      tools.foreach { t =>
        val name = t.hcursor.get[String]("name").toOption.getOrElse(fail("nameless tool"))
        assert(t.hcursor.downField("inputSchema").focus.isDefined,  s"tool $name missing inputSchema")
        assert(t.hcursor.downField("outputSchema").focus.isDefined, s"tool $name missing outputSchema")
      }
    }

    "unknown tool name returns JSON-RPC -32601" in withServer { base =>
      val (status, body) = post(base, rpc("tools/call", callParams(shapesTool + "doesNotExist", Json.obj())))
      assert(status == Status.Ok)
      val err = body.hcursor.downField("error").focus.getOrElse(fail(s"expected JSON-RPC error envelope: $body"))
      assert(err.hcursor.get[Int]("code").toOption.contains(-32601), s"expected -32601, got $err")
    }
  }
}
