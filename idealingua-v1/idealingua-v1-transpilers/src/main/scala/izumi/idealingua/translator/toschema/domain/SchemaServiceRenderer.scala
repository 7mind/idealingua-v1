package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.typer.ir.TypeDef

/** Emits an MCP `ListToolsResult` envelope for a service.
  *
  * Per plan §4 / D15 (MCP spec 2025-06-18). Each RPC method becomes a single
  * tool entry with `name`, `description`, `inputSchema`, `outputSchema`, and
  * the `x-idealingua-*` advisory annotations. Tool name format per D22 is
  * `<package>.<ServiceName>.<methodName>` — the *raw* method name, NOT
  * capitalized (the ephemeral wireId, e.g. `...SimpleInput`, uses the
  * capitalized form and is surfaced via
  * `x-idealingua-wire-type-{input,output}`).
  *
  * The ephemeral wireIds for input/output are reconstructed from the same
  * naming rule used by `EphemeralSynthesizer`:
  *   - input  wireId = `<svcWireId>.<methodCapitalized>Input`
  *   - output wireId = `<svcWireId>.<methodCapitalized>Output`
  *
  * M5.5 annotation split (F-M5-2): the wireId annotation is now named
  * `x-idealingua-wire-type-{input,output}` — it conveys the ephemeral
  * wire-format ground truth (what the bytes are shaped as on the wire),
  * which is *distinct* from `outputSchema` (the post-unwrap shape that
  * MCP consumers actually observe). The annotation is informational only;
  * resolving it against `components.schemas` is the consumer's choice.
  *
  * Methods are emitted in **declaration order** (the IR contract — see
  * `TypeDef.Service` scaladoc). Within the file, byte-stability is preserved
  * by Circe's printer over a `LinkedHashMap`-backed `Json.fromFields`.
  */
final class SchemaServiceRenderer(
  domainId: DomainId,
  output: SchemaMethodOutput,
) {

  private val kindRpc = "rpc"

  def render(service: TypeDef.Service): Json = {
    val tools = service.methods.collect {
      case rpc: DefMethod.RPCMethod => renderMethod(service, rpc)
    }
    Json.obj("tools" -> Json.fromValues(tools))
  }

  private def renderMethod(service: TypeDef.Service, m: DefMethod.RPCMethod): Json = {
    val pkg            = domainId.toPackage.mkString(".")
    val svcName        = service.id.name
    val methodName     = m.name
    val methodCap      = m.name.capitalize
    val toolName       = s"$pkg.$svcName.$methodName"
    val svcWireId      = service.id.wireId
    val inputWireId    = s"$svcWireId.${methodCap}Input"
    val outputWireId   = s"$svcWireId.${methodCap}Output"
    val description    = m.meta.doc.getOrElse("")

    val fields = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    fields += "name"                          -> Json.fromString(toolName)
    fields += "description"                   -> Json.fromString(description)
    fields += "inputSchema"                   -> output.structSchema(m.signature.input)
    fields += "outputSchema"                  -> output.dispatch(m.signature.output)
    fields += "x-idealingua-wire-type-input"  -> Json.fromString(inputWireId)
    fields += "x-idealingua-wire-type-output" -> Json.fromString(outputWireId)
    fields += "x-idealingua-kind"             -> Json.fromString(kindRpc)
    Json.fromFields(fields.toList)
  }
}
