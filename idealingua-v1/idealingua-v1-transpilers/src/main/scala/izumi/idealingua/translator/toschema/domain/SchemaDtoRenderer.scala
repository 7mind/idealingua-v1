package izumi.idealingua.translator.toschema.domain

import io.circe.Json
import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.typer.ir.{Domain, FlatField, Member, TypeDef}

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
    val fields = domain.flattenedStructs.get(dto.id).map(_.fields).getOrElse(Nil)
    renderFromFlat(dto.id.wireId, fields, dto.meta.doc)
  }

  /** Renders a flat-object schema from a pre-flattened field list. Used for
    * interface-mirror ephemerals (`<Iface>.Struct`), whose flat struct lives
    * in `domain.flattenedStructs` but which are not in `userTypes`.
    */
  def renderFromFlat(wireId: String, fields: List[FlatField], doc: Option[String]): Json = {
    val orderedFields = orderForEmission(fields)

    val propsList = orderedFields.map { case (name, field) =>
      name -> resolver.schemaFor(field.typeId)
    }
    val required  = orderedFields.collect {
      case (name, field) if !resolver.isOptional(field.typeId) => Json.fromString(name)
    }

    val base = scala.collection.mutable.LinkedHashMap.empty[String, Json]
    base += "type"  -> Json.fromString("object")
    base += "title" -> Json.fromString(wireId)
    doc.foreach(d => base += "description" -> Json.fromString(d))
    base += "properties"           -> Json.fromFields(propsList)
    base += "required"             -> Json.fromValues(required)
    base += "additionalProperties" -> Json.False

    Json.fromFields(base.toList)
  }

  /** Apply the canonical Scala-leg sort + dedup so emitted property order
    * matches case-class constructor parameter order. Mirrors
    * `DomainScalaStruct.fromFlat`'s sort/dedup logic — see that method for the
    * authoritative rationale.
    */
  private def orderForEmission(fields: List[FlatField]): List[(String, Field)] = {
    // 1. Recover `definedWithIndex` per field (position in the originating
    //    type's declared `Struct.fields`). Fall back to the per-origin
    //    declaration order within `flat.fields` when the origin can't be
    //    located in `userTypes`/`members`.
    val seenPerOrigin = scala.collection.mutable.LinkedHashMap.empty[TypeId, Int]
    val annotated: List[OrderedField] = fields.map { ff =>
      val perOriginIdx = {
        val n = seenPerOrigin.getOrElse(ff.origin, 0)
        seenPerOrigin.update(ff.origin, n + 1)
        n
      }
      val idx = originIndex(ff.origin, ff.field.name).getOrElse(perOriginIdx)
      OrderedField(ff.field, ff.origin, ff.distance, idx)
    }

    // 2. Same-name dedup matching legacy `NonContradictive` behaviour:
    //    - identical-Field across occurrences: keep the deepest-distance one
    //      (legacy emission order put parent declarations first);
    //    - true covariant overrides: keep the closest declaration (smallest
    //      distance) so the type-refined primary wins.
    val deduped: List[OrderedField] = {
      val byName = scala.collection.mutable.LinkedHashMap
        .empty[String, scala.collection.mutable.ListBuffer[OrderedField]]
      annotated.foreach { f =>
        val buf = byName.getOrElseUpdate(f.field.name, scala.collection.mutable.ListBuffer.empty)
        buf += f
      }
      byName.values.map { occurrences =>
        val typesEqual = occurrences.map(_.field).toSet.size == 1
        if (typesEqual) occurrences.maxBy(_.distance)
        else occurrences.minBy(_.distance)
      }.toList
    }

    // 3. Apply the legacy sort key — same as `DomainScalaStruct.fromFlat`.
    val sorted: List[OrderedField] =
      deduped
        .sortBy(f => (f.distance, f.definedBy.toString, -f.definedWithIndex))
        .reverse

    sorted.map(of => of.field.name -> of.field)
  }

  private def originIndex(origin: TypeId, fieldName: String): Option[Int] = {
    def indexIn(fs: List[Field]): Option[Int] = {
      val idx = fs.indexWhere(_.name == fieldName)
      if (idx < 0) None else Some(idx)
    }
    import izumi.idealingua.typer.ir.{TypeDef => NTD}
    domain.userTypes.get(origin) match {
      case Some(d: NTD.Dto)        => indexIn(d.struct.fields)
      case Some(i: NTD.Interface)  => indexIn(i.struct.fields)
      case _ =>
        domain.members.get(origin) match {
          case Some(Member.Ephemeral(eph)) => indexIn(eph.struct.fields)
          case _                           => None
        }
    }
  }

  private case class OrderedField(
    field: Field,
    definedBy: TypeId,
    distance: Int,
    definedWithIndex: Int,
  )
}
