package izumi.idealingua.harness

import io.circe.parser.parse
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

/** PR-04 IMPL-MCP-M3+M4 smoke: runs the schema emitter over the `main-tests`
  * corpus and asserts the per-domain `schema.json` + per-service/buzzer
  * `<Name>.mcp.json` files come out in well-formed shape.
  *
  * No golden bytes asserted yet (golden suite lands in M5). The test is a
  * sentinel for the four FROZEN harness contracts: it fails loudly if the
  * schema target stops producing valid JSON or stops covering services /
  * buzzers / output variants.
  */
final class SchemaEmitterSmokeSpec extends AnyFunSuite {

  test("schema emitter produces valid JSON for every main-tests domain") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    assert(loaded.nonEmpty, s"no domains loaded from $corpusRoot")

    val options  = HarnessOptions.optionsFor(IDLLanguage.JsonSchema)
    val layouted = new TypespaceCompilerBaseFacade(options).compile(loaded)

    var schemaCount = 0
    var mcpCount    = 0
    layouted.emodules.foreach {
      case ExtendedModule.DomainModule(_, m) =>
        val parsed = parse(m.content)
        assert(parsed.isRight, s"invalid JSON for module ${m.id.path.mkString("/")}/${m.id.name}: ${parsed.left}")
        if (m.id.name == "schema.json") schemaCount += 1
        else if (m.id.name.endsWith(".mcp.json")) mcpCount += 1
      case _ => ()
    }
    assert(schemaCount > 0, "no schema.json modules emitted")
    assert(mcpCount > 0, "no .mcp.json modules emitted")
  }

  test("idltest.services.TestService.mcp.json shape") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val options    = HarnessOptions.optionsFor(IDLLanguage.JsonSchema)
    val layouted   = new TypespaceCompilerBaseFacade(options).compile(loaded)

    val mcpModule = layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m)
          if m.id.path.endsWith(Seq("idltest", "services")) && m.id.name == "TestService.mcp.json" =>
        m
    }.getOrElse(fail("idltest.services/TestService.mcp.json not emitted"))

    val json = parse(mcpModule.content).toOption.getOrElse(fail("not JSON"))
    val tools = json.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail("missing tools[]"))
    assert(tools.nonEmpty, "tools array empty")

    val toolNames = tools.flatMap(_.hcursor.get[String]("name").toOption)
    assert(
      toolNames.exists(_.endsWith(".simple")),
      s"expected a `simple` tool, got: ${toolNames.mkString(",")}",
    )
    assert(
      toolNames.forall(_.startsWith("idltest.services.TestService.")),
      s"tool names should be fully qualified: $toolNames",
    )

    // M5.5: every outputSchema declares top-level type:"object" per MCP 2025-06-18.
    // Non-object outputs (Void, Singular-primitive, Algebraic, Alternative) are
    // wrapped via SchemaMethodOutput.wrapIfNonObject and carry
    // `x-idealingua-wrapped:true`; the original shape lives under
    // `properties.result`.

    // Spot-check Alternative output: alternativeGeneric is wrapped; the inner
    // shape under properties.result carries x-idealingua-kind:"alternative".
    val alt = tools.find(_.hcursor.get[String]("name").toOption.contains("idltest.services.TestService.alternativeGeneric"))
      .getOrElse(fail("alternativeGeneric tool not found"))
    val altOutType = alt.hcursor.downField("outputSchema").get[String]("type").toOption
    assert(altOutType.contains("object"), s"expected wrapped object, got $altOutType")
    val altWrapped = alt.hcursor.downField("outputSchema").get[Boolean]("x-idealingua-wrapped").toOption
    assert(altWrapped.contains(true), s"expected x-idealingua-wrapped:true, got $altWrapped")
    val altInnerKind = alt.hcursor.downField("outputSchema").downField("properties").downField("result").get[String]("x-idealingua-kind").toOption
    assert(altInnerKind.contains("alternative"), s"expected inner alternative kind, got $altInnerKind")

    // Spot-check Singular unwrap: greetSingularOut returns Output.Singular(string);
    // the wrap envelopes `{type:"string", x-idealingua-unwrap:true}` under
    // properties.result.
    val sing = tools.find(_.hcursor.get[String]("name").toOption.contains("idltest.services.TestService.greetSingularOut"))
      .getOrElse(fail("greetSingularOut tool not found"))
    val singOutType = sing.hcursor.downField("outputSchema").get[String]("type").toOption
    assert(singOutType.contains("object"), s"expected wrapped object, got $singOutType")
    val singWrapped = sing.hcursor.downField("outputSchema").get[Boolean]("x-idealingua-wrapped").toOption
    assert(singWrapped.contains(true), s"expected x-idealingua-wrapped:true, got $singWrapped")
    val unwrap = sing.hcursor.downField("outputSchema").downField("properties").downField("result").get[Boolean]("x-idealingua-unwrap").toOption
    assert(unwrap.contains(true), s"expected inner x-idealingua-unwrap:true, got $unwrap")

    // Spot-check Void output: unitToUnit -> wrap of {type:"null"}.
    val void = tools.find(_.hcursor.get[String]("name").toOption.contains("idltest.services.TestService.unitToUnit"))
      .getOrElse(fail("unitToUnit tool not found"))
    val voidOuter = void.hcursor.downField("outputSchema").get[String]("type").toOption
    assert(voidOuter.contains("object"), s"expected wrapped object, got $voidOuter")
    val voidWrapped = void.hcursor.downField("outputSchema").get[Boolean]("x-idealingua-wrapped").toOption
    assert(voidWrapped.contains(true), s"expected x-idealingua-wrapped:true, got $voidWrapped")
    val voidInner = void.hcursor.downField("outputSchema").downField("properties").downField("result").get[String]("type").toOption
    assert(voidInner.contains("null"), s"expected inner type:null, got $voidInner")

    // Spot-check annotation rename (M5.5 F-M5-2): x-idealingua-wire-type-{input,output}
    val voidWireOut = void.hcursor.get[String]("x-idealingua-wire-type-output").toOption
    assert(voidWireOut.exists(_.endsWith(".UnitToUnitOutput")), s"expected wire-type-output annotation, got $voidWireOut")
    val voidWireIn = void.hcursor.get[String]("x-idealingua-wire-type-input").toOption
    assert(voidWireIn.exists(_.endsWith(".UnitToUnitInput")), s"expected wire-type-input annotation, got $voidWireIn")
  }

  test("idltest.events.TestBuzzer.mcp.json events have wrapped null outputSchema and buzzer kind") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val options    = HarnessOptions.optionsFor(IDLLanguage.JsonSchema)
    val layouted   = new TypespaceCompilerBaseFacade(options).compile(loaded)

    val mcpModule = layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m)
          if m.id.path.endsWith(Seq("idltest", "events")) && m.id.name == "TestBuzzer.mcp.json" =>
        m
    }.getOrElse(fail("idltest.events/TestBuzzer.mcp.json not emitted"))

    val json  = parse(mcpModule.content).toOption.getOrElse(fail("not JSON"))
    val tools = json.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(fail("missing tools[]"))
    assert(tools.nonEmpty, "buzzer tools array empty")

    tools.foreach { t =>
      val kind       = t.hcursor.get[String]("x-idealingua-kind").toOption
      val outerType  = t.hcursor.downField("outputSchema").get[String]("type").toOption
      val wrapped    = t.hcursor.downField("outputSchema").get[Boolean]("x-idealingua-wrapped").toOption
      val innerType  = t.hcursor.downField("outputSchema").downField("properties").downField("result").get[String]("type").toOption
      assert(kind.contains("buzzer"), s"expected buzzer kind, got $kind for ${t.spaces2.take(80)}")
      assert(outerType.contains("object"), s"expected wrapped object outputSchema, got $outerType")
      assert(wrapped.contains(true), s"expected x-idealingua-wrapped:true, got $wrapped")
      assert(innerType.contains("null"), s"expected inner result:null, got $innerType")
    }
  }

  test("schema.json for idltest.services contains method input + output ephemerals") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    val options    = HarnessOptions.optionsFor(IDLLanguage.JsonSchema)
    val layouted   = new TypespaceCompilerBaseFacade(options).compile(loaded)

    val schemaModule = layouted.emodules.collectFirst {
      case ExtendedModule.DomainModule(_, m)
          if m.id.path.endsWith(Seq("idltest", "services")) && m.id.name == "schema.json" =>
        m
    }.getOrElse(fail("idltest.services/schema.json not emitted"))

    val json    = parse(schemaModule.content).toOption.getOrElse(fail("not JSON"))
    val schemas = json.hcursor.downField("components").downField("schemas").focus
      .flatMap(_.asObject).getOrElse(fail("missing components.schemas")).keys.toSet

    assert(
      schemas("idltest.services.TestService.SimpleInput"),
      s"missing SimpleInput in $schemas",
    )
    assert(
      schemas("idltest.services.TestService.SimpleOutput"),
      s"missing SimpleOutput in $schemas",
    )
    assert(
      schemas("idltest.services.TestService.GreetAlgebraicOutOutput"),
      s"missing GreetAlgebraicOutOutput ADT ephemeral in $schemas",
    )
    assert(
      schemas("idltest.services.TestService.AlternativeOutput"),
      s"missing AlternativeOutput ADT ephemeral in $schemas",
    )
  }
}
