package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.typer.ir.TypeDef

/** Emits a string-enum JSON Schema fragment for an `Enum`.
  *
  * Per plan §3.3 Enum: members in declaration order (the IR's `List` already
  * preserves this).
  */
final class SchemaEnumRenderer {

  def render(e: TypeDef.Enum): Json = {
    val members = e.members.map(m => Json.fromString(m.value))
    val base    = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "type"  -> Json.fromString("string")
    base += "title" -> Json.fromString(e.id.wireId)
    e.meta.doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "enum" -> Json.fromValues(members)
    Json.fromFields(base.toList)
  }
}
