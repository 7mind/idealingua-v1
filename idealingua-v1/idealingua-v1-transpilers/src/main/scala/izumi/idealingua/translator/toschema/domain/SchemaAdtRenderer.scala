package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.il.ast.typed.AdtMember
import izumi.idealingua.typer.ir.TypeDef

/** Emits an ADT JSON Schema as a `oneOf` of single-key wrapper objects.
  *
  * Wire-format §4: each branch encodes as `{ "<discriminator>": <inner> }`
  * where the discriminator is the **short** member name —
  * `AdtMember.wireId = memberName.getOrElse(typeId.name)`. The inner value
  * is the encoded branch. We emit a `$ref` to the branch's own component
  * schema, which gives correct recursive composition for ADT-of-Interface:
  * the referenced interface's schema is itself a `oneOf` of full-wireId
  * wrappers, yielding the §4 nested shape
  * `{ "AFace": { "idltest.algebraics.SomeImpl": {...} } }` by construction.
  *
  * Branch order honours declaration order from `TypeDef.Adt.alternatives`.
  */
final class SchemaAdtRenderer {

  def render(adt: TypeDef.Adt): Json = {
    val branches = adt.alternatives.map(branchSchema)

    val base = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "title"                    -> Json.fromString(adt.id.wireId)
    adt.meta.doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "oneOf"                    -> Json.fromValues(branches)
    base += "x-idealingua-kind"        -> Json.fromString("adt")

    Json.fromFields(base.toList)
  }

  private def branchSchema(member: AdtMember): Json = {
    val discriminator = member.wireId
    val ref           = Json.obj(
      "$ref" -> Json.fromString(s"#/components/schemas/${member.typeId.wireId}")
    )
    Json.obj(
      "type"                 -> Json.fromString("object"),
      "properties"           -> Json.obj(discriminator -> ref),
      "required"             -> Json.arr(Json.fromString(discriminator)),
      "additionalProperties" -> Json.False,
    )
  }
}
