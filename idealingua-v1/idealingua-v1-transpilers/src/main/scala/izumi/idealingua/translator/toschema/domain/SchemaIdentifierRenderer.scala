package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.il.ast.typed.IdField
import izumi.idealingua.typer.ir.TypeDef

/** Emits the string-form JSON Schema for an Identifier per plan §3.3.
  *
  * The pattern is informational — JSON Schema cannot express URL-encoded
  * field-value joining precisely — so the regex matches the general shape
  * `^<TypeName>#<f1>:<f2>:...$` with relaxed character classes. The
  * `x-idealingua-fields` advisory carries the per-field schema for
  * structured-input UIs.
  *
  * Fields are alphabetically sorted by name (per `wire-format.md` §6 and the
  * existing TS/Scala/C# Identifier renderers).
  */
final class SchemaIdentifierRenderer(resolver: SchemaTypeResolver) {

  def render(id: TypeDef.Identifier): Json = {
    val typeName = id.id.name

    val fieldsSorted = id.fields.sortBy(_.name)

    val pattern: String = {
      // Each field segment: URL-encoded value characters (no literal colons,
      // since `:` is the IDL identifier separator).
      val segPattern = "([^:]*)"
      val segments   = fieldsSorted.indices.map(_ => segPattern).mkString(":")
      // TypeName is restricted to identifier chars by the parser, so no
      // additional regex-meta escaping is required for the prefix.
      s"^$typeName#$segments$$"
    }

    val advisoryFields = Json.fromValues(fieldsSorted.map { f =>
      val ts = idFieldType(f)
      Json.obj(
        "name"   -> Json.fromString(f.name),
        "schema" -> resolver.schemaFor(ts),
      )
    })

    val base = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "type"  -> Json.fromString("string")
    base += "title" -> Json.fromString(id.id.wireId)
    id.meta.doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "pattern"                 -> Json.fromString(pattern)
    base += "x-idealingua-kind"       -> Json.fromString("identifier")
    base += "x-idealingua-fields"     -> advisoryFields

    Json.fromFields(base.toList)
  }

  private def idFieldType(f: IdField): izumi.idealingua.model.common.TypeId = f match {
    case IdField.PrimitiveField(typeId, _, _) => typeId
    case IdField.SubId(typeId, _, _)          => typeId
    case IdField.Enum(typeId, _, _)           => typeId
  }
}
