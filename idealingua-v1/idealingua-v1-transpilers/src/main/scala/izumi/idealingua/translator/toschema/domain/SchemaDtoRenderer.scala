package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.typer.ir.{Domain, TypeDef}

/** Emits a flat-object JSON Schema for a DTO.
  *
  * Per plan §3.3 DTO: uses `domain.flattenedStructs(dto.id).fields` so the
  * inheritance chain is already resolved by Phase 6
  * (StructuralFlattener). Field order matches the resolved-inheritance
  * order from the flat struct (ancestors first, declaration order within
  * each level).
  *
  * `required` excludes `TOption[_]` fields (D20).
  */
final class SchemaDtoRenderer(domain: Domain, resolver: SchemaTypeResolver) {

  def render(dto: TypeDef.Dto): Json = {
    val flat = domain.flattenedStructs.get(dto.id)

    val fields = flat.map(_.fields).getOrElse(Nil)

    // Preserve resolved-inheritance order; deduplicate by simple name keeping
    // the first occurrence (legacy Circe deriveEncoder collapses overrides
    // the same way).
    val seen     = scala.collection.mutable.LinkedHashMap.empty[String, izumi.idealingua.model.il.ast.typed.Field]
    fields.foreach { ff =>
      if (!seen.contains(ff.field.name)) seen.put(ff.field.name, ff.field)
    }

    val orderedFields = seen.toList

    val propsList = orderedFields.map { case (name, field) =>
      name -> resolver.schemaFor(field.typeId)
    }
    val required  = orderedFields.collect {
      case (name, field) if !resolver.isOptional(field.typeId) => Json.fromString(name)
    }

    val base = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "type" -> Json.fromString("object")
    base += "title" -> Json.fromString(dto.id.wireId)
    dto.meta.doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "properties"           -> Json.fromFields(propsList)
    base += "required"             -> Json.fromValues(required)
    base += "additionalProperties" -> Json.False

    Json.fromFields(base.toList)
  }
}
