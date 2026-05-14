package izumi.idealingua.harness

import com.networknt.schema.{InputFormat, JsonSchema, JsonSchemaFactory, PathType, SchemaValidatorsConfig, SpecVersion}
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

/** PR-04 IMPL-MCP-M5 / M5.5 — Layer B validation.
  *
  * Validates every emitted `<Service>.mcp.json` / `<Buzzer>.mcp.json`
  * against the snapshotted MCP `ListToolsResult` schema for spec 2025-06-18,
  * which lives at `idealingua-v1-test-defs/schema/mcp-2025-06-18.json`.
  *
  * The snapshot is the extract of `definitions/ListToolsResult` + its
  * transitive `Tool` / `ToolAnnotations` / `ToolSchema` shapes from the
  * upstream `modelcontextprotocol/schema/2025-06-18/schema.json` (draft-07).
  *
  * M5.5 (F-M5-3 RESOLVED): every emitted `outputSchema` now declares
  * `type:"object"` per MCP 2025-06-18. Non-object outputs (Void / Buzzers /
  * Algebraic / Alternative / primitive Singular) are wrapped via
  * `SchemaMethodOutput.wrapIfNonObject` and annotated with
  * `x-idealingua-wrapped:true`. The previous known-divergence classifier
  * for `outputSchema.type` violations has been removed; the spec now
  * asserts ZERO violations strictly.
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

    val violations = scala.collection.mutable.ArrayBuffer.empty[(String, String)]
    var validated  = 0

    mcpModules.foreach { m =>
      val moduleLabel = (m.id.path :+ m.id.name).mkString("/")
      val messages    = mcpSchema.validate(m.content, InputFormat.JSON).asScala
      if (messages.isEmpty) validated += 1
      else
        messages.foreach { msg =>
          violations += ((moduleLabel, msg.getMessage))
        }
    }

    val report = new StringBuilder()
    report.append(
      s"mcp-modules=${mcpModules.size} validated=$validated violations=${violations.size}\n"
    )
    if (violations.nonEmpty) {
      report.append("\nVIOLATIONS:\n")
      violations.foreach { case (mod, msg) => report.append(s"  - $mod: $msg\n") }
    }
    info(report.toString)

    assert(violations.isEmpty, s"MCP envelope violations: ${violations.size}")
    assert(validated == mcpModules.size, s"validated=$validated / ${mcpModules.size}")
  }
}
