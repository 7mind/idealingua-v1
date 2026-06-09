package izumi.idealingua.runtime.rpc.http4s

import scala.annotation.nowarn

import io.circe.Json
import izumi.idealingua.runtime.rpc.{
  IRTMethodId,
  IRTOutputMiddleware,
  IRTServerMultiplexor,
  McpServiceMeta,
  McpToolMeta,
}
import izumi.r2.idealingua.test.generated.{
  GreeterServiceMethods,
  GreeterServiceServerWrapped,
  PrivateTestService,
  PrivateTestServiceServer,
  PrivateTestServiceWrappedServer,
}
import izumi.r2.idealingua.test.impls.AbstractGreeterServer
import org.scalatest.wordspec.AnyWordSpec

@nowarn("msg=unused value of type org.scalatest.compatible.Assertion")
final class McpServiceAssemblerSpec extends AnyWordSpec {

  type BIO[+E, +A] = zio.IO[E, A]

  implicit val io2: izumi.functional.bio.IO2[BIO] = izumi.functional.bio.impl.AsyncZio

  // ---------------------------------------------------------------------------
  // Fixtures: real IRTWrappedService instances from idealingua-v1-test-defs
  // ---------------------------------------------------------------------------

  private val greeterService: GreeterServiceServerWrapped[BIO, Unit] =
    new GreeterServiceServerWrapped[BIO, Unit](new AbstractGreeterServer.Impl[BIO, Unit])

  private val privateService: PrivateTestServiceWrappedServer[BIO, Unit] =
    new PrivateTestServiceWrappedServer[BIO, Unit](
      new PrivateTestServiceServer[BIO, Unit] {
        def test(ctx: Unit, str: String): BIO[Nothing, String] = izumi.functional.bio.F.pure(s"ok:$str")
      }
    )

  // ---------------------------------------------------------------------------
  // McpServiceMeta fixtures matching the real IRTMethodId toString values
  // ---------------------------------------------------------------------------

  // GreeterService: serviceId = "GreeterService"; methods: greet, alternative
  // IRTMethodId.toString == "<svc>.<method>", e.g. "GreeterService.greet"
  // Fully-qualified tool names use the package prefix the renderer would emit:
  // "izumi.r2.idealingua.test.generated.GreeterService.<method>"
  private val greeterMeta: McpServiceMeta = McpServiceMeta(
    serviceId = "GreeterService",
    tools = List(
      McpToolMeta(
        toolName    = "izumi.r2.idealingua.test.generated.GreeterService.greet",
        description = "Greet a person",
        inputSchema  = Json.obj(),
        outputSchema = Json.obj(),
        wireInput   = "greet.Input",
        wireOutput  = "greet.Output",
        kind        = "Singular",
        wrap        = true,
      ),
      McpToolMeta(
        toolName    = "izumi.r2.idealingua.test.generated.GreeterService.alternative",
        description = "Alternative method",
        inputSchema  = Json.obj(),
        outputSchema = Json.obj(),
        wireInput   = "alternative.Input",
        wireOutput  = "alternative.Output",
        kind        = "Alternative",
        wrap        = true,
      ),
    ),
  )

  // PrivateTestService: serviceId = "PrivateTestService"; method: test
  private val privateMeta: McpServiceMeta = McpServiceMeta(
    serviceId = "PrivateTestService",
    tools = List(
      McpToolMeta(
        toolName    = "izumi.r2.idealingua.test.generated.PrivateTestService.test",
        description = "Private test method",
        inputSchema  = Json.obj(),
        outputSchema = Json.obj(),
        wireInput   = "test.Input",
        wireOutput  = "test.Output",
        kind        = "Singular",
        wrap        = false,
      ),
    ),
  )

  private val middleware: IRTOutputMiddleware[BIO, Unit] = IRTOutputMiddleware.empty[BIO, Unit]

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  "McpServiceAssembler" should {

    "produce a union tools/list containing tools from BOTH services" in {
      val assembly = McpServiceAssembler.assemble(
        Seq(greeterService -> greeterMeta, privateService -> privateMeta),
        middleware,
      )
      val tools = assembly.toolsListJson.hcursor
        .downField("tools").focus
        .flatMap(_.asArray)
        .getOrElse(fail("missing tools[]"))
      assert(tools.size == 3, s"expected 3 tools (2 + 1), got ${tools.size}")

      val names = tools.flatMap(_.hcursor.get[String]("name").toOption).toSet
      assert(names == Set(
        "izumi.r2.idealingua.test.generated.GreeterService.greet",
        "izumi.r2.idealingua.test.generated.GreeterService.alternative",
        "izumi.r2.idealingua.test.generated.PrivateTestService.test",
      ))
    }

    "produce a dispatch map covering every tool from both services" in {
      val assembly = McpServiceAssembler.assemble(
        Seq(greeterService -> greeterMeta, privateService -> privateMeta),
        middleware,
      )
      val dispatch = assembly.dispatch
      assert(dispatch.size == 3, s"expected 3 dispatch entries, got ${dispatch.size}")

      val greetEntry = dispatch.get("izumi.r2.idealingua.test.generated.GreeterService.greet")
      assert(greetEntry.isDefined, "greet not in dispatch map")
      assert(greetEntry.get._1 == GreeterServiceMethods.greet.id)
      assert(greetEntry.get._2 == true, "greet wrap should be true")

      val altEntry = dispatch.get("izumi.r2.idealingua.test.generated.GreeterService.alternative")
      assert(altEntry.isDefined, "alternative not in dispatch map")
      assert(altEntry.get._1 == GreeterServiceMethods.alternative.id)
      assert(altEntry.get._2 == true, "alternative wrap should be true")

      val testEntry = dispatch.get("izumi.r2.idealingua.test.generated.PrivateTestService.test")
      assert(testEntry.isDefined, "private test not in dispatch map")
      assert(testEntry.get._1 == PrivateTestService.test.id)
      assert(testEntry.get._2 == false, "private test wrap should be false")
    }

    "throw McpToolNameCollisionException when two metas share a tool name" in {
      val collidingMeta: McpServiceMeta = McpServiceMeta(
        serviceId = "PrivateTestService",
        tools = List(
          McpToolMeta(
            toolName    = "izumi.r2.idealingua.test.generated.GreeterService.greet",
            description = "collision",
            inputSchema  = Json.obj(),
            outputSchema = Json.obj(),
            wireInput   = "x",
            wireOutput  = "x",
            kind        = "Singular",
            wrap        = false,
          ),
        ),
      )

      val ex = intercept[McpToolNameCollisionException] {
        McpServiceAssembler.assemble(
          Seq(greeterService -> greeterMeta, privateService -> collidingMeta),
          middleware,
        )
      }
      assert(
        ex.getMessage.contains("izumi.r2.idealingua.test.generated.GreeterService.greet"),
        s"collision error should name the colliding tool; got: ${ex.getMessage}",
      )
    }

    "build a mux that resolves an IRTMethodId from the GreeterService" in {
      val assembly = McpServiceAssembler.assemble(
        Seq(greeterService -> greeterMeta, privateService -> privateMeta),
        middleware,
      )
      assert(assembly.mux.methods.contains(GreeterServiceMethods.greet.id))
      assert(assembly.mux.methods.contains(GreeterServiceMethods.alternative.id))
    }

    "build a mux that resolves an IRTMethodId from the PrivateTestService" in {
      val assembly = McpServiceAssembler.assemble(
        Seq(greeterService -> greeterMeta, privateService -> privateMeta),
        middleware,
      )
      assert(assembly.mux.methods.contains(PrivateTestService.test.id))
    }

    "throw McpToolResolutionException for an unresolvable tool name" in {
      val badMeta: McpServiceMeta = McpServiceMeta(
        serviceId = "GreeterService",
        tools = List(
          McpToolMeta(
            toolName    = "izumi.r2.idealingua.test.generated.GreeterService.doesNotExist",
            description = "bad tool",
            inputSchema  = Json.obj(),
            outputSchema = Json.obj(),
            wireInput   = "x",
            wireOutput  = "x",
            kind        = "Singular",
            wrap        = false,
          ),
        ),
      )

      intercept[McpToolResolutionException] {
        McpServiceAssembler.assemble(
          Seq(greeterService -> badMeta),
          middleware,
        )
      }
    }
  }

  "McpServiceAssembler.assembleOverMux" should {

    // A caller-supplied, hand-built mux over both services — mirrors a
    // pre-built authorized multiplexor. The MCP layer is added over it without
    // rebuilding the mux.
    val handBuiltMux: IRTServerMultiplexor[BIO, Unit] =
      IRTServerMultiplexor.combine(
        Seq(
          new IRTServerMultiplexor.FromServices[BIO, Unit](Set(greeterService), middleware),
          new IRTServerMultiplexor.FromServices[BIO, Unit](Set(privateService), middleware),
        )
      )

    "return the SUPPLIED mux verbatim and the same toolsList/dispatch invariants as assemble" in {
      val over = McpServiceAssembler.assembleOverMux(Seq(greeterMeta, privateMeta), handBuiltMux)

      // The mux is the caller's, not a freshly built one.
      assert(over.mux eq handBuiltMux)

      val tools = over.toolsListJson.hcursor
        .downField("tools").focus
        .flatMap(_.asArray)
        .getOrElse(fail("missing tools[]"))
      assert(tools.size == 3, s"expected 3 tools (2 + 1), got ${tools.size}")
      val names = tools.flatMap(_.hcursor.get[String]("name").toOption).toSet
      assert(names == Set(
        "izumi.r2.idealingua.test.generated.GreeterService.greet",
        "izumi.r2.idealingua.test.generated.GreeterService.alternative",
        "izumi.r2.idealingua.test.generated.PrivateTestService.test",
      ))

      assert(over.dispatch.size == 3, s"expected 3 dispatch entries, got ${over.dispatch.size}")
      val greetEntry = over.dispatch.get("izumi.r2.idealingua.test.generated.GreeterService.greet")
      assert(greetEntry.contains((GreeterServiceMethods.greet.id, true)))
      val testEntry = over.dispatch.get("izumi.r2.idealingua.test.generated.PrivateTestService.test")
      assert(testEntry.contains((PrivateTestService.test.id, false)))

      // Every resolved methodId is served by the supplied mux (bijection over this set).
      assert(over.dispatch.values.forall { case (id, _) => handBuiltMux.methods.contains(id) })
    }

    "throw McpToolNameCollisionException when two metas share a tool name" in {
      val collidingMeta: McpServiceMeta = McpServiceMeta(
        serviceId = "PrivateTestService",
        tools = List(
          McpToolMeta(
            toolName    = "izumi.r2.idealingua.test.generated.GreeterService.greet",
            description = "collision",
            inputSchema  = Json.obj(),
            outputSchema = Json.obj(),
            wireInput   = "x",
            wireOutput  = "x",
            kind        = "Singular",
            wrap        = false,
          ),
        ),
      )
      intercept[McpToolNameCollisionException] {
        McpServiceAssembler.assembleOverMux(Seq(greeterMeta, collidingMeta), handBuiltMux)
      }
    }

    "throw McpToolResolutionException when a tool name is absent from the supplied mux" in {
      val badMeta: McpServiceMeta = McpServiceMeta(
        serviceId = "GreeterService",
        tools = List(
          McpToolMeta(
            toolName    = "izumi.r2.idealingua.test.generated.GreeterService.doesNotExist",
            description = "bad tool",
            inputSchema  = Json.obj(),
            outputSchema = Json.obj(),
            wireInput   = "x",
            wireOutput  = "x",
            kind        = "Singular",
            wrap        = false,
          ),
        ),
      )
      intercept[McpToolResolutionException] {
        McpServiceAssembler.assembleOverMux(Seq(badMeta), handBuiltMux)
      }
    }
  }
}
