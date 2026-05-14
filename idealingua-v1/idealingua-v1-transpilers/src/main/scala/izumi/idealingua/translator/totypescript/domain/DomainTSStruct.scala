package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.common.{ExtendedField, FieldDef, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}
import izumi.idealingua.typer.ir.{Domain, FlatStruct, Member, TypeDef => NewTypeDef}

/** Adapter that surfaces the legacy `Struct`-shape (`fields`, `superclasses`,
  * `all`, `unambigious`, `ambigious`) from the new IR's `FlatStruct`.
  *
  * IMPL-7b Phase B M2: TypeScript counterpart of `DomainScalaStruct`. The
  * structural TS renderers (`DomainTSIdRenderer`,
  * `DomainTSCompositeRenderer`, `DomainTSInterfaceRenderer`) consume the
  * legacy `Struct` shape because `TypeScriptTypeConverter` and the rest of
  * the legacy plumbing is reused unchanged; rather than port every consumer
  * site to a parallel shape, this adapter constructs an equivalent legacy
  * `typespace.structures.Struct` from the new IR's `FlatStruct` + the
  * originating `TypeDef.Dto/Interface.struct.superclasses`.
  *
  * Sort key and dedup logic mirror `DomainScalaStruct.fromFlat` 1:1 — the
  * legacy `StructuralQueriesImpl.scala:41` sort is language-agnostic, it
  * encodes what `typespace.structure.structure(...)` returns regardless of
  * the consumer.
  */
object DomainTSStruct {

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
    // See `DomainScalaStruct.fromFlat` for the full rationale.
    val dfsPos: Map[(TypeId, String), Int] = legacyDfsPosition(id, domain)
    def posOf(ef: ExtendedField): Int =
      dfsPos.getOrElse((ef.defn.definedBy, ef.field.name), Int.MaxValue)
    val deduped: List[ExtendedField] = {
      val byName  = scala.collection.mutable.LinkedHashMap.empty[String, scala.collection.mutable.ListBuffer[ExtendedField]]
      extendedRaw.foreach { f =>
        val buf = byName.getOrElseUpdate(f.field.name, scala.collection.mutable.ListBuffer.empty)
        buf += f
      }
      byName.values.map { occurrences =>
        val typesEqual = occurrences.map(_.field).toSet.size == 1
        if (typesEqual) occurrences.minBy(posOf)
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

  /** Replicate legacy `FieldExtractor.extractFields` DFS emission order for
    * `id` — see `DomainScalaStruct.legacyDfsPosition`.
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
      case Some(d: NewTypeDef.Dto)        => indexIn(d.struct.fields)
      case Some(i: NewTypeDef.Interface)  => indexIn(i.struct.fields)
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

  /** Domain-direct port of `Typespace.inheritance.parentsInherited(id)`.
    *
    * Replicates the legacy depth-first traversal from
    * `InheritanceQueriesImpl.safeParentsInherited`: for an `InterfaceId i`,
    * the result is `List(i) ++ parents.flatMap(parentsInherited)`; for a
    * `DTOId d`, the result is `parents.flatMap(parentsInherited)`. Other
    * `TypeId` kinds (identifier, enum, alias, adt, service-family) return
    * `Nil`. `parents` for both DTO and Interface comes from
    * `struct.superclasses.interfaces` in declaration order — the new-IR
    * `TypeDef.Dto/Interface` preserves that exact list.
    *
    * Cycle protection is omitted because the new-typer
    * `CycleDetector` rejects cyclic inheritance before assembly (and the
    * legacy `checkCycles` would have thrown anyway). The `excluded`
    * parameter in the legacy walk is therefore unreachable for any valid
    * domain.
    *
    * Used by `DomainTSInterfaceRenderer` and `DomainTSCompositeRenderer`
    * for the `.register(...)` chain and the `to<Iface>Serialized` /
    * `load<Iface>` helper enumeration. The order matters for byte parity —
    * `distinctBy(_.name)` on the call site preserves the first occurrence.
    */
  def parentsInherited(domain: Domain, id: izumi.idealingua.model.common.TypeId): List[InterfaceId] = {
    id match {
      case i: InterfaceId =>
        val parents = domain.userTypes.get(i) match {
          case Some(iface: NewTypeDef.Interface) => iface.struct.superclasses.interfaces
          case _                                 => List.empty
        }
        List(i) ++ parents.flatMap(parentsInherited(domain, _))
      case d: izumi.idealingua.model.common.TypeId.DTOId =>
        val parents = domain.userTypes.get(d) match {
          case Some(dto: NewTypeDef.Dto) => dto.struct.superclasses.interfaces
          case _                         => List.empty
        }
        parents.flatMap(parentsInherited(domain, _))
      case _ => List.empty
    }
  }

  /** Domain-direct port of `Typespace.structure.structure(id)` for the
    * single use site in `DomainTSCompositeRenderer.renderDto*Interface*`
    * (queries the *interface*'s structure, not the DTO's). Builds the
    * legacy `Struct` shape from `Domain.flattenedStructs(i.id)` plus the
    * interface's declared superclasses — see `fromFlat` for the per-step
    * rationale.
    *
    * Cross-domain fallback (PR-02 IMPL-10b-fix): when `i` lives in another
    * domain (e.g. the `M2` interface aliased into `idltest.aliases#D1`
    * via `idltest.aliases2#M2`), the local `flattenedStructs` map has no
    * entry — the foreign interface's `FlatStruct` is owned by its own
    * domain's flattener pass. `StructuralFlattener` mirrors the foreign
    * harvest into `Domain.crossDomainFlattenedStructs`; consult it here so
    * the emitted `to<Iface>Serialized` body contains the foreign mixin's
    * fields instead of being empty (the visible defect was `D1.toM2Serialized`
    * dropping `f2`). Foreign supers are reconstructed empty when the
    * foreign `Interface` def isn't in the local `userTypes` map — the
    * superclasses list is only used to compute the `extends ...` clause for
    * `<Iface>StructSerialized`, which the cross-domain caller never emits
    * (it only reads `Struct.all`).
    */
  def structureOf(domain: Domain, i: InterfaceId): izumi.idealingua.model.typespace.structures.Struct = {
    val flat = domain.flattenedStructs.get(i)
      .orElse(domain.crossDomainFlattenedStructs.get(i))
      .getOrElse(FlatStruct(i, List.empty, List.empty, List.empty))
    val supers = domain.userTypes.get(i) match {
      case Some(iface: NewTypeDef.Interface) => iface.struct.superclasses
      case _ => izumi.idealingua.model.il.ast.typed.Super.empty
    }
    fromFlat(i, flat, supers, domain)
  }
}
