package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.{ExtendedField, FieldDef, StructureId, TypeId}
import izumi.idealingua.translator.common.LegacyStructOrdering
import izumi.idealingua.typer.ir.{Domain, FlatField, TypeDef}

/** Emits a flat-object JSON Schema for a DTO.
  *
  * Per plan §3.3 DTO: reads `domain.flattenedStructs(dto.id).fields` (the
  * pre-flattened inheritance chain produced by Phase 6 / StructuralFlattener).
  *
  * X2-Fix-C: property emission order matches the generated Scala case-class
  * constructor parameter order. The Scala side uses
  * `DomainScalaStruct.fromFlat`, which applies
  * `(distance ASC, definedBy.toString ASC, definedWithIndex DESC).reverse`
  * plus a same-name dedup pass. We replicate that here so
  * `components.schemas[<wireId>].properties` ordering is consistent with the
  * case-class field order downstream consumers see (`Point.scala` vs
  * `idltest.dtofields.Point` schema, etc.).
  *
  * `required` excludes `TOption[_]` fields (D20).
  */
final class SchemaDtoRenderer(domain: Domain, resolver: SchemaTypeResolver) {

  def render(dto: TypeDef.Dto): Json = {
    // `findFlatStruct` falls back to cross-domain flattened structs, so DTOs
    // imported from other domains (e.g. types pulled into an MCP `$defs` closure)
    // render their fields instead of an empty object. Identical to
    // `flattenedStructs.get` for local DTOs.
    val fields = domain.findFlatStruct(dto.id).map(_.fields).getOrElse(Nil)
    renderFromFlat(dto.id, fields, dto.meta.doc)
  }

  /** Renders a flat-object schema from a pre-flattened field list. Used for
    * interface-mirror ephemerals (`<Iface>.Struct`), whose flat struct lives
    * in `domain.flattenedStructs` but which are not in `userTypes`.
    */
  def renderFromFlat(id: StructureId, fields: List[FlatField], doc: Option[String]): Json = {
    val orderedFields = orderForEmission(id, fields)

    val propsList = orderedFields.map(f => f.field.name -> resolver.schemaFor(f.field.typeId))
    val required  = orderedFields.collect {
      case f if !resolver.isOptional(f.field.typeId) => Json.fromString(f.field.name)
    }

    val base = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "type"  -> Json.fromString("object")
    base += "title" -> Json.fromString(id.wireId)
    doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "properties"           -> Json.fromFields(propsList)
    base += "required"             -> Json.fromValues(required)
    base += "additionalProperties" -> Json.False

    Json.fromFields(base.toList)
  }

  /** Apply the canonical sort + dedup matching `DomainScalaStruct.fromFlat`
    * so emitted property order matches case-class constructor parameter
    * order.
    */
  private def orderForEmission(id: StructureId, fields: List[FlatField]): List[ExtendedField] = {
    val seenPerOrigin = scala.collection.mutable.LinkedHashMap.empty[TypeId, Int]
    val annotated: List[ExtendedField] = fields.map { ff =>
      val perOriginIdx = {
        val n = seenPerOrigin.getOrElse(ff.origin, 0)
        seenPerOrigin.update(ff.origin, n + 1)
        n
      }
      val idx = LegacyStructOrdering.originIndex(domain, ff.origin, ff.field.name).getOrElse(perOriginIdx)
      ExtendedField(
        field = ff.field,
        defn  = FieldDef(
          definedBy        = ff.origin,
          definedWithIndex = idx,
          usedBy           = id,
          distance         = ff.distance,
        ),
      )
    }

    // F-DTO1-fieldorder: equal-type duplicates use legacy DFS emission order
    // (`fields.head` in legacy `NonContradictive`), genuine covariant
    // overrides keep the closest declaration. Same rule as
    // `DomainScalaStruct` / `DomainTSStruct`.
    val dfsPos = LegacyStructOrdering.legacyDfsPosition(id, domain)
    def posOf(ef: ExtendedField): Int =
      dfsPos.getOrElse((ef.defn.definedBy, ef.field.name), Int.MaxValue)
    val deduped: List[ExtendedField] = {
      val byName = scala.collection.mutable.LinkedHashMap
        .empty[String, scala.collection.mutable.ListBuffer[ExtendedField]]
      annotated.foreach { f =>
        val buf = byName.getOrElseUpdate(f.field.name, scala.collection.mutable.ListBuffer.empty)
        buf += f
      }
      byName.values.map { occurrences =>
        val typesEqual = occurrences.map(_.field).toSet.size == 1
        if (typesEqual) occurrences.minBy(posOf)
        else occurrences.minBy(_.defn.distance)
      }.toList
    }

    LegacyStructOrdering.sortLegacyKey(deduped)
  }
}
