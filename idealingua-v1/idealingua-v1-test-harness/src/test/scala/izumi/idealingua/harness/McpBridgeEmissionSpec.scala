package izumi.idealingua.harness

import io.circe.parser.parse
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}
import org.scalatest.funsuite.AnyFunSuite

/** PR-04 MCP Mb1: smoke test for the http4s bridge emitter.
  *
  * Compiles `main-tests` with `ScalaBuildManifest.emitMcpBridge = true` and
  * asserts:
  *   - per-service `<Name>Mcp.scala` source modules are emitted alongside
  *     the regular service source modules,
  *   - per-service `mcp/<Name>.mcp.json` resource modules are emitted (tagged
  *     `meta("resource") == "true"`),
  *   - the default (`emitMcpBridge = false`) path emits NO bridge modules,
  *   - the bridge Scala source contains a `routes[F[+_, +_]: IO2: Error2, C]`
  *     entrypoint and per-method `toolName_/methodId_` constants,
  *   - `Singular`-output methods in the matched fixture dispatch via `call(`
  *     and `Struct`-output methods stub via the Mb1 placeholder.
  *
  * Fixture: `idltest.services.TestService` (chosen because it has Singular,
  * Struct, Algebraic, and Alternative output variants).
  */
final class McpBridgeEmissionSpec extends AnyFunSuite {

  private def options(emitMcpBridge: Boolean): UntypedCompilerOptions = {
    val baseManifest = HarnessOptions.scala.copy(emitMcpBridge = emitMcpBridge)
    UntypedCompilerOptions(
      language        = IDLLanguage.Scala,
      target          = None,
      manifest        = baseManifest,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )
  }

  test("emitMcpBridge=false (default): no McpRoutes or mcp/*.mcp.json modules emitted") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val layouted   = new TypespaceCompilerBaseFacade(options(emitMcpBridge = false)).compile(loaded)

    val mcpScala = layouted.emodules.collect {
      case ExtendedModule.DomainModule(_, m) if m.id.name.endsWith("Mcp.scala") => m.id
    }
    val mcpJson = layouted.emodules.collect {
      case ExtendedModule.DomainModule(_, m) if m.id.path.contains("mcp") && m.id.name.endsWith(".mcp.json") => m.id
    }
    assert(mcpScala.isEmpty, s"unexpected Mcp.scala modules emitted with default flag: $mcpScala")
    assert(mcpJson.isEmpty,  s"unexpected .mcp.json modules emitted with default flag: $mcpJson")
  }

  test("emitMcpBridge=true: per-service bridge + resource modules emitted") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val layouted   = new TypespaceCompilerBaseFacade(options(emitMcpBridge = true)).compile(loaded)

    val bridgeScala = layouted.emodules.collect {
      case ExtendedModule.DomainModule(_, m) if m.id.name == "TestServiceMcp.scala" => m
    }.headOption.getOrElse(fail("TestServiceMcp.scala not emitted"))

    val bridgeJson = layouted.emodules.collect {
      case ExtendedModule.DomainModule(_, m) if m.id.name == "TestService.mcp.json" && m.id.path == Seq("mcp") => m
    }.headOption.getOrElse(fail("mcp/TestService.mcp.json not emitted"))

    // Resource modules carry the `meta("resource") == "true"` tag so the
    // SBT layouter routes them to `src/main/resources/` (and the PLAIN
    // layouter leaves the path verbatim).
    assert(
      bridgeJson.meta.get("resource").contains("true"),
      s"resource module missing meta tag: ${bridgeJson.meta}",
    )

    // Resource JSON must parse.
    val parsedJson = parse(bridgeJson.content).toOption.getOrElse(fail("mcp.json not valid JSON"))
    val tools      = parsedJson.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail("missing tools[]"))
    assert(tools.nonEmpty, "tools array empty")

    // The bridge Scala source must declare the expected scaffolding.
    val src = bridgeScala.content
    assert(src.contains("object TestServiceMcpRoutes"), "missing object scaffold")
    assert(src.contains("def routes[F[+_, +_]: IO2: Error2, C]"), "missing routes entrypoint signature")
    assert(src.contains("(implicit AT: Async[F[Throwable, _]]"), "missing http4s Async edge implicit")
    assert(src.contains("getResourceAsStream(\"mcp/TestService.mcp.json\")"), "missing classpath resource lookup")
    assert(src.contains("private val methodId_simple "), "missing methodId_simple constant")

    // Mb1 dispatch invariants:
    //   - Singular-output method `parameterless` routes via `call(...)`.
    //   - Struct-output method `simple` (`(+Request) => (+Request)`) stubs
    //     with the Mb1 placeholder error.
    assert(
      src.contains("call(req, argsJson, methodId_parameterless"),
      "Singular-output `parameterless` should dispatch via call(...) (Mb1 path)",
    )

    // Struct-output `simple` (input `(+Request)` / output `(+Request)`) stubs
    // until Mb2. The match arm for `simple` should map to the Mb1 placeholder.
    val arms = src.split("case Some\\(`toolName_simple`\\)")
    assert(arms.length == 2, s"expected exactly one match arm for `simple`, got ${arms.length - 1}")
    val simpleArmBody = arms(1).take(200)
    assert(
      simpleArmBody.contains("method not implemented in Mb1"),
      s"Struct-output `simple` should stub in Mb1, got: ${simpleArmBody.linesIterator.take(3).mkString(" / ")}",
    )
  }

  test("TestServiceMcp Layer A golden byte-equal") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val layouted   = new TypespaceCompilerBaseFacade(options(emitMcpBridge = true)).compile(loaded)

    val emittedScala = layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m) if m.id.name == "TestServiceMcp.scala" => m.content
    }.getOrElse(fail("TestServiceMcp.scala not emitted"))

    val emittedJson = layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m) if m.id.name == "TestService.mcp.json" && m.id.path == Seq("mcp") => m.content
    }.getOrElse(fail("mcp/TestService.mcp.json not emitted"))

    val goldenRoot = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/golden/scala-mcp")
    val goldenScala = new String(
      java.nio.file.Files.readAllBytes(goldenRoot.resolve("idltest/services/TestServiceMcp.scala")),
      java.nio.charset.StandardCharsets.UTF_8,
    )
    val goldenJson = new String(
      java.nio.file.Files.readAllBytes(goldenRoot.resolve("mcp/TestService.mcp.json")),
      java.nio.charset.StandardCharsets.UTF_8,
    )

    assert(emittedScala == goldenScala, "emitted TestServiceMcp.scala diverged from golden")
    assert(emittedJson == goldenJson,   "emitted TestService.mcp.json diverged from golden")
  }

  test("emitted bridge source parses as valid Scala 2.13") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val layouted   = new TypespaceCompilerBaseFacade(options(emitMcpBridge = true)).compile(loaded)

    import scala.meta._
    val parseFailures = layouted.emodules.flatMap {
      case ExtendedModule.DomainModule(_, m) if m.id.name.endsWith("Mcp.scala") =>
        dialects.Scala213(m.content).parse[Source] match {
          case _: parsers.Parsed.Success[_] => None
          case e: parsers.Parsed.Error      => Some(s"${m.id.name}: ${e.message}")
        }
      case _ => None
    }

    assert(
      parseFailures.isEmpty,
      s"generated bridge sources failed to parse:\n${parseFailures.mkString("\n")}",
    )
  }
}
