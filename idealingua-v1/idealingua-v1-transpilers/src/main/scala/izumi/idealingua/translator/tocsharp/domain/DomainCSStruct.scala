package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.common.{ExtendedField, FieldDef, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}
import izumi.idealingua.typer.ir.{Domain, FlatStruct, Member, TypeDef => NewTypeDef}

/** Adapter that surfaces the legacy `Struct`-shape (`fields`, `superclasses`,
  * `all`, `unambigious`, `ambigious`) from the new IR's `FlatStruct`.
  *
  * IMPL-7c Phase B M2: C# counterpart of `DomainScalaStruct` /
  * `DomainTSStruct`. The structural C# renderers (`DomainCSIdRenderer`,
  * `DomainCSCompositeRenderer`, `DomainCSInterfaceRenderer`) consume the
  * legacy `Struct` shape because `CSharpClass` / `CSharpField` /
  * `CSharpType` (and the rest of the legacy `tocsharp/types` plumbing) is
  * reused unchanged; rather than port every consumer site to a parallel
  * shape, this adapter constructs an equivalent legacy
  * `typespace.structures.Struct` from the new IR's `FlatStruct` + the
  * originating `TypeDef.Dto/Interface.struct.superclasses`.
  *
  * Sort key and dedup logic mirror `DomainScalaStruct.fromFlat` /
  * `DomainTSStruct.fromFlat` 1:1 — the legacy
  * `StructuralQueriesImpl.scala:41` sort is language-agnostic, it encodes
  * what `typespace.structure.structure(...)` returns regardless of the
  * consumer.
  */
object DomainCSStruct {

  /** Build a legacy `Struct` from the new IR's flat struct plus the source
    * `Super` declaration. See `DomainScalaStruct.fromFlat` for the full
    * rationale (sort key, covariant duplicate-field dedup).
    */
  def fromFlat(id: StructureId, flat: FlatStruct, supers: Super, domain: Domain): izumi.idealingua.model.typespace.structures.Struct = {
    val seenPerOrigin = scala.collection.mutable.LinkedHashMap.empty[TypeId, Int]
    val extendedRaw: List[ExtendedField] = flat.fields.map { ff =>
      val perOriginIdx = {
        val n = seenPerOrigin.getOrElse(ff.origin, 0)
        seenPerOrigin.update(ff.origin, n + 1)
        n
      }
      val idx = originIndex(domain, ff.origin, ff.field.name).getOrElse(perOriginIdx)
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

    val deduped: List[ExtendedField] = {
      val byName = scala.collection.mutable.LinkedHashMap.empty[String, scala.collection.mutable.ListBuffer[ExtendedField]]
      extendedRaw.foreach { f =>
        val buf = byName.getOrElseUpdate(f.field.name, scala.collection.mutable.ListBuffer.empty)
        buf += f
      }
      byName.values.map { occurrences =>
        val typesEqual = occurrences.map(_.field).toSet.size == 1
        if (typesEqual) occurrences.maxBy(_.defn.distance)
        else occurrences.minBy(_.defn.distance)
      }.toList
    }

    val sorted: List[ExtendedField] =
      deduped
        .sortBy(f => (f.defn.distance, f.defn.definedBy.toString, -f.defn.definedWithIndex))
        .reverse

    val ambiguousNames: Set[String] =
      (flat.conflictsSoft.map(_.name) ++ flat.conflictsHard.map(_.name)).toSet

    val unambigious = sorted.filterNot(f => ambiguousNames.contains(f.field.name))
    val ambigious   = sorted.filter(f => ambiguousNames.contains(f.field.name))

    new izumi.idealingua.model.typespace.structures.Struct(
      id           = id,
      superclasses = supers,
      unambigious  = unambigious,
      ambigious    = ambigious,
      all          = sorted,
    )
  }

  /** Recover legacy `definedWithIndex` — see `DomainScalaStruct.originIndex`. */
  private def originIndex(domain: Domain, origin: TypeId, fieldName: String): Option[Int] = {
    def indexIn(fields: List[Field]): Option[Int] = {
      val idx = fields.indexWhere(_.name == fieldName)
      if (idx < 0) None else Some(idx)
    }
    domain.userTypes.get(origin) match {
      case Some(d: NewTypeDef.Dto)       => indexIn(d.struct.fields)
      case Some(i: NewTypeDef.Interface) => indexIn(i.struct.fields)
      case _ =>
        domain.members.get(origin) match {
          case Some(Member.Ephemeral(eph)) => indexIn(eph.struct.fields)
          case _                           => None
        }
    }
  }

  /** Build a synthetic `FlatStruct` for an interface impl id (`<Iface>Struct`)
    * from the interface's flat struct.
    */
  def implFlatStruct(implId: izumi.idealingua.model.common.TypeId.DTOId, ifaceFlat: FlatStruct): FlatStruct = {
    FlatStruct(
      ownerId       = implId,
      fields        = ifaceFlat.fields,
      conflictsHard = ifaceFlat.conflictsHard,
      conflictsSoft = ifaceFlat.conflictsSoft,
    )
  }

  /** Construct an impl `DTOId` for an interface (mirrors legacy
    * `TypespaceToolsImpl.implId`: `DTOId(iface, "Struct")`).
    */
  def implId(id: InterfaceId): izumi.idealingua.model.common.TypeId.DTOId = {
    izumi.idealingua.model.common.TypeId.DTOId(id, "Struct")
  }
}
