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

    // F-DTO1-fieldorder: equal-type duplicates must pick the smallest-position
    // entry under legacy DFS emission (`fields.head` in
    // `StructuralQueriesImpl.NonContradictive`), not the deepest-distance one.
    // See `DomainScalaStruct.fromFlat` for the full rationale — the C#
    // translator has the same regression for the same reason (Fe4 "max
    // distance" rule diverges from legacy DFS on diamond inheritance).
    //
    // The covariant-narrowing branch below is C#-specific (C# does not
    // support covariant property overrides) and remains unchanged: it picks
    // the deepest entry and stores the variance chain for the renderer to
    // emit the commented-out workaround.
    val dfsPos: Map[(TypeId, String), Int] = legacyDfsPosition(id, domain)
    def posOf(ef: ExtendedField): Int =
      dfsPos.getOrElse((ef.defn.definedBy, ef.field.name), Int.MaxValue)
    val deduped: List[ExtendedField] = {
      val byName = scala.collection.mutable.LinkedHashMap.empty[String, scala.collection.mutable.ListBuffer[ExtendedField]]
      extendedRaw.foreach { f =>
        val buf = byName.getOrElseUpdate(f.field.name, scala.collection.mutable.ListBuffer.empty)
        buf += f
      }
      byName.values.map { occurrences =>
        val typesEqual = occurrences.map(_.field).toSet.size == 1
        if (typesEqual) occurrences.minBy(posOf)
        else {
          // Covariant field-type narrowing: legacy C# goldens predate
          // covariant override propagation into the parent interface's
          // structure — the legacy typer registered only the parent's
          // field (`Field: Covariant`) on the iface's flat struct and
          // dropped the iface-level narrowed override (`Field: CovariantA`).
          // To keep the generated C# compilable against the parent
          // interface's property contract (C# does not support covariant
          // property overrides), pick the deepest (largest-distance =
          // parent) occurrence as the canonical field-type, and store
          // the full type-narrowing chain in `defn.variance`. The C#
          // interface renderer reads `f.defn.variance.nonEmpty` to switch
          // to the commented-out covariance workaround (legacy `:371-372`).
          val sorted = occurrences.sortBy(_.defn.distance)
          val best   = sorted.last
          best.copy(defn = best.defn.copy(variance = sorted.map(_.field).toList))
        }
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

  /** Replicate legacy `FieldExtractor.extractFields` DFS emission order for
    * `id` and return a positional index per `(origin, fieldName)`. Mirrors
    * `DomainScalaStruct.legacyDfsPosition` — same legacy algorithm, the
    * underlying typespace question is language-agnostic.
    */
  private def legacyDfsPosition(id: StructureId, domain: Domain): Map[(TypeId, String), Int] = {
    val buf     = scala.collection.mutable.LinkedHashMap.empty[(TypeId, String), Int]
    val visited = scala.collection.mutable.LinkedHashSet.empty[TypeId]
    def fieldsOfStruct(tid: TypeId): Option[(List[Field], Super)] = {
      domain.userTypes.get(tid) match {
        case Some(d: NewTypeDef.Dto)       => Some((d.struct.fields, d.struct.superclasses))
        case Some(i: NewTypeDef.Interface) => Some((i.struct.fields, i.struct.superclasses))
        case _ =>
          domain.members.get(tid) match {
            case Some(Member.Ephemeral(eph)) => Some((eph.struct.fields, eph.struct.superclasses))
            case _                           => None
          }
      }
    }
    def walk(tid: TypeId): Unit = {
      if (!visited.add(tid)) return
      fieldsOfStruct(tid) match {
        case Some((thisFields, sup)) =>
          sup.interfaces.foreach(walk)
          sup.concepts.foreach(walk)
          thisFields.foreach { f =>
            val key = (tid, f.name)
            if (!buf.contains(key)) buf.update(key, buf.size)
          }
        case None => ()
      }
    }
    walk(id)
    buf.toMap
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
