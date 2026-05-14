package izumi.idealingua.harness

import com.networknt.schema.{InputFormat, JsonSchema, JsonSchemaFactory, PathType, SchemaValidatorsConfig, SpecVersion}
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

/** PR-04 IMPL-MCP-M5 — Layer B validation.
  *
  * Validates every emitted `<Service>.mcp.json` / `<Buzzer>.mcp.json`
  * against the snapshotted MCP `ListToolsResult` schema for spec 2025-06-18,
  * which lives at `idealingua-v1-test-defs/schema/mcp-2025-06-18.json`.
  *
  * The snapshot is the extract of `definitions/ListToolsResult` + its
  * transitive `Tool` / `ToolAnnotations` / `ToolSchema` shapes from the
  * upstream `modelcontextprotocol/schema/2025-06-18/schema.json` (draft-07).
  *
  * The MCP spec mandates `inputSchema.type == "object"` and
  * `outputSchema.type == "object"` when present. The idealingua emitter
  * currently emits `outputSchema: {"type": "null"}` for buzzers / `Void`
  * outputs (locked at plan D10/D16) — that combination is a known
  * divergence from the MCP spec, recorded below; the test classifies
  * those tools separately so the assertion fails only on UNEXPECTED
  * violations.
  */
final class MCPEnvelopeValidationSpec extends AnyFunSuite {

  private def loadMcpSchema(repoRoot: Path): JsonSchema = {
    val schemaFile = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/schema/mcp-2025-06-18.json")
    assert(Files.exists(schemaFile), s"MCP schema snapshot missing: $schemaFile")
    val schemaText = new String(Files.readAllBytes(schemaFile), StandardCharsets.UTF_8)
    val factory    = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
    val cfg        = SchemaValidatorsConfig.builder().pathType(PathType.JSON_POINTER).build()
    factory.getSchema(schemaText, cfg)
  }

  test("every emitted *.mcp.json validates against MCP ListToolsResult (2025-06-18)") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)
    assert(loaded.nonEmpty, s"no domains loaded from $corpusRoot")

    val options  = HarnessOptions.optionsFor(IDLLanguage.JsonSchema)
    val layouted = new TypespaceCompilerBaseFacade(options).compile(loaded)

    val mcpSchema = loadMcpSchema(repoRoot)

    val mcpModules = layouted.emodules.collect {
      case ExtendedModule.DomainModule(_, m) if m.id.name.endsWith(".mcp.json") => m
    }
    assert(mcpModules.nonEmpty, "no *.mcp.json modules emitted")

    val violations            = scala.collection.mutable.ArrayBuffer.empty[(String, String)]
    val knownDivergence       = scala.collection.mutable.ArrayBuffer.empty[(String, String)]
    var validated             = 0

    mcpModules.foreach { m =>
      val moduleLabel = (m.id.path :+ m.id.name).mkString("/")
      val messages    = mcpSchema.validate(m.content, InputFormat.JSON).asScala
      if (messages.isEmpty) validated += 1
      else
        messages.foreach { msg =>
          val text = msg.getMessage
          // Locked divergences from the MCP spec (recorded for post-M5
          // follow-up; not gating M5 — see plan §11 R10 + tasks.md F-list):
          //   * D10/D16 — buzzers + `Output.Void` emit
          //     `outputSchema: {"type":"null"}` which violates the spec
          //     requirement `outputSchema.type == "object"`.
          //   * D7 — `Output.Algebraic` and `Output.Alternative` emit
          //     `outputSchema: {"oneOf": [...]}` with no top-level `type`
          //     key, also violating the spec requirement.
          val isOutputTypeDivergence =
            text.contains("outputSchema") && (
              text.contains("must be the constant value 'object'") ||
              text.contains("required property 'type' not found")
            )
          if (isOutputTypeDivergence) knownDivergence += ((moduleLabel, text))
          else violations += ((moduleLabel, text))
        }
    }

    val report = new StringBuilder()
    report.append(
      s"mcp-modules=${mcpModules.size} validated=$validated " +
        s"known-divergence=${knownDivergence.size} violations=${violations.size}\n"
    )
    if (knownDivergence.nonEmpty) {
      report.append("\nKNOWN DIVERGENCE (D7/D10/D16 — outputSchema type:null for Void/Buzzers, or oneOf without top-level type for Algebraic/Alternative; informational):\n")
      knownDivergence.take(10).foreach { case (mod, msg) => report.append(s"  - $mod: $msg\n") }
      if (knownDivergence.size > 10) report.append(s"  … and ${knownDivergence.size - 10} more\n")
    }
    if (violations.nonEmpty) {
      report.append("\nUNEXPECTED VIOLATIONS:\n")
      violations.foreach { case (mod, msg) => report.append(s"  - $mod: $msg\n") }
    }
    info(report.toString)

    assert(violations.isEmpty, s"unexpected MCP envelope violations: ${violations.size}")
  }
}
