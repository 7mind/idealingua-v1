package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.typer.ir.TypeDef

/** Emits an MCP `ListToolsResult` envelope for a buzzer.
  *
  * Per D16 / plan §4 + M5.5 wrap-everywhere (F-M5-3): buzzers expose events as
  * tools whose `outputSchema` is the MCP-conformant wrap
  * `{"type":"object","properties":{"result":{"type":"null"}},"required":["result"],
  *  "additionalProperties":false,"x-idealingua-wrapped":true}` — fire-and-forget
  * semantics, but the schema declaration remains strictly MCP-2025-06-18
  * conformant. `x-idealingua-kind: "buzzer"`. Tool name format per D22:
  * `<package>.<BuzzerName>.<eventName>`.
  *
  * The ephemeral wireIds for input/output mirror the service form and are
  * surfaced via `x-idealingua-wire-type-{input,output}` (M5.5 rename).
  */
final class SchemaBuzzerRenderer(
  domainId: DomainId,
  output: SchemaMethodOutput,
) {

  private val kindBuzzer = "buzzer"

  def render(buzzer: TypeDef.Buzzer): Json = {
    val tools = buzzer.events.collect {
      case rpc: DefMethod.RPCMethod => renderEvent(buzzer, rpc)
    }
    Json.obj("tools" -> Json.fromValues(tools))
  }

  private def renderEvent(buzzer: TypeDef.Buzzer, m: DefMethod.RPCMethod): Json = {
    val pkg            = domainId.toPackage.mkString(".")
    val bzName         = buzzer.id.name
    val eventName      = m.name
    val eventCap       = m.name.capitalize
    val toolName       = s"$pkg.$bzName.$eventName"
    val bzWireId       = buzzer.id.wireId
    val inputWireId    = s"$bzWireId.${eventCap}Input"
    val outputWireId   = s"$bzWireId.${eventCap}Output"
    val description    = m.meta.doc.getOrElse("")

    val fields = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    fields += "name"                          -> Json.fromString(toolName)
    fields += "description"                   -> Json.fromString(description)
    fields += "inputSchema"                   -> output.structSchema(m.signature.input)
    fields += "outputSchema"                  -> output.wrapIfNonObject(
      Json.obj("type" -> Json.fromString("null"))
    )
    fields += "x-idealingua-wire-type-input"  -> Json.fromString(inputWireId)
    fields += "x-idealingua-wire-type-output" -> Json.fromString(outputWireId)
    fields += "x-idealingua-kind"             -> Json.fromString(kindBuzzer)
    Json.fromFields(fields.toList)
  }
}
