package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.common.{ExtendedField, FieldDef, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}
import izumi.idealingua.translator.toscala.types.{ScalaField, ScalaStruct, ScalaTypeConverter}
import izumi.idealingua.typer.ir.{Domain, FlatStruct, Member, Struct, TypeDef => NewTypeDef}

/** Adapter that surfaces the legacy `Struct`-shape (`fields`, `superclasses`,
  * `all`, `unambigious`, `ambigious`) from the new IR's `FlatStruct`.
  *
  * IMPL-7a.2 Phase B M3: the structural renderers consume the legacy
  * `ScalaStruct` shape because `ScalaTypeConverter.ConflictsOps.toScala`
  * (and the rest of the legacy plumbing) is reused unchanged. Rather than
  * port every consumer site to a parallel shape, this adapter constructs an
  * equivalent legacy `typespace.structures.Struct` from the new IR's
  * `FlatStruct` + the originating `TypeDef.Dto/Interface.struct.superclasses`.
  *
  * This is intentionally a thin compatibility shim. M4+ may simplify by
  * introducing a parallel `ScalaStruct`-free renderer, but for M3 the legacy
  * `ScalaTypeConverter` integration provides direct structural parity for
  * the field/super-class projection.
  */
object DomainScalaStruct {

  /** Build a legacy `Struct` from the new IR's flat struct plus the source
    * `Super` declaration that lives on `TypeDef.Dto.struct`/`TypeDef.Interface.struct`.
    *
    * Field ordering (IMPL-7a.2-Fa, defect #5): the legacy
    * `StructuralQueriesImpl.scala:41` sorts `conflicts.all` by
    * `(distance ASC, definedBy.toString ASC, -definedWithIndex DESC).reverse`,
    * which produces a deterministic, depth-prefix-stable order distinct from
    * the BFS layer order the new `StructuralFlattener` emits. This sort is
    * applied here so the case-class constructor parameter order matches the
    * legacy translator byte-for-byte. `definedWithIndex` is recovered by
    * indexing the originating type's declared `Struct.fields` list.
    *
    * Covariant duplicate-field dedup (IMPL-7a.2-Fa, defect #7): when a
    * subtype refines an inherited field's type (covariant override,
    * `StructuralFlattener.scala:253-269`), `flat.fields` carries every
    * occurrence — the primary at the smallest distance plus every ancestor
    * candidate. The legacy `Struct.all` is deduplicated by name (one
    * `ExtendedField` per name, keeping the primary). Without this dedup the
    * Scala renderer emits the same field name twice with different types
    * (Scala compile error). The primary is the smallest-distance entry under
    * the legacy sort key — same key used for the field order above.
    */
  def fromFlat(id: StructureId, flat: FlatStruct, supers: Super, domain: Domain): izumi.idealingua.model.typespace.structures.Struct = {
    // `definedWithIndex` per legacy `FieldExtractor.toExtendedFields` is the
    // index of a field inside its declaring type's `Struct.fields` list. We
    // recover it by looking up the originating TypeDef in `domain.userTypes`
    // (or `members` for synthesized ephemerals) and indexing into its
    // declared `fields`. When that lookup fails (test stubs with empty
    // `userTypes`/`members`), fall back to the field's position within the
    // contiguous run of same-origin entries in `flat.fields` — the
    // BFS-flattener emits fields in declaration order per layer, so this is
    // equivalent.
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

    // Defect #7 / IMPL-7a.2-Fe4: field-name dedup matching legacy
    // `StructuralQueriesImpl.NonContradictive`.
    //
    // Legacy `FieldExtractor.extractFields` returns
    // `superFields ++ embeddedFields ++ thisFields`, so parent
    // declarations (deeper distance) appear FIRST in the conflict list.
    // The legacy `NonContradictive` branch for equal-type duplicates
    // returns `Some(fields.head)` — the head is therefore the deepest
    // (parent) occurrence.
    //
    // The new IR's BFS-flattener emits in the opposite order
    // (`thisFields ++ embeddedFields`, self-first), so the equivalent
    // "first-encounter in legacy order" is the entry with the LARGEST
    // distance.  Replicate:
    //   - Group by field name.
    //   - If all occurrences share the same `Field` value, keep the
    //     LARGEST-distance entry (the parent in legacy emission order).
    //   - Otherwise (true covariant override), keep the smallest-
    //     distance entry — the closest declaration is the type-refined
    //     primary.
    val deduped: List[ExtendedField] = {
      val byName  = scala.collection.mutable.LinkedHashMap.empty[String, scala.collection.mutable.ListBuffer[ExtendedField]]
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

    // Defect #5: apply the legacy sort key.
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

  /** Recover legacy `definedWithIndex` for a field defined on `origin`:
    * the index of the field within the originating type's declared
    * `Struct.fields` list. Mirrors `FieldExtractor.toExtendedFields`
    * (`FieldExtractor.scala:47-52`) — `fields.zipWithIndex`.
    *
    * Lookup order: user types first (DTO/Interface/Identifier), then
    * ephemerals (`Member.Ephemeral`) — both can be field origins after F8
    * (synthesized method-input/output DTOs participate in flattening).
    *
    * Returns 0 when the origin can't be located in the domain (test stubs
    * with `userTypes = Map.empty` for example). The sort key then degrades
    * to `(distance, definedBy.toString)` which still produces a stable
    * legacy-compatible order for single-distance, single-origin cases.
    */
  private def originIndex(domain: Domain, origin: TypeId, fieldName: String): Option[Int] = {
    def indexIn(fields: List[Field]): Option[Int] = {
      val idx = fields.indexWhere(_.name == fieldName)
      if (idx < 0) None else Some(idx)
    }
    // Identifier types aren't `StructureId`s and never appear as an origin
    // in `FlatStruct.fields`, so only DTO/Interface user-types plus
    // synthesized ephemerals need handling.
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

  /** Convenience: build a `ScalaStruct` (rendered-form) directly from the
    * new IR flat struct + source `Super`.
    */
  def scalaStruct(
    id: StructureId,
    flat: FlatStruct,
    supers: Super,
    conv: ScalaTypeConverter,
    domain: Domain,
  ): ScalaStruct = {
    val legacyStruct = fromFlat(id, flat, supers, domain)

    def toScalaField(field: ExtendedField): ScalaField = {
      import scala.meta._
      ScalaField(
        Term.Name(field.field.name),
        conv.toScala(field.field.typeId).typeFull,
        field,
      )
    }

    val good = legacyStruct.unambigious.map(toScalaField)
    val soft = legacyStruct.ambigious.map(toScalaField)
    val all  = legacyStruct.all.map(toScalaField)
    new ScalaStruct(legacyStruct, good, soft, all)
  }

  /** Build a synthetic `FlatStruct` for an interface impl id (`<Iface>.Struct`)
    * from the interface's flat struct. New IR does not pre-materialise the
    * `defnId(impl)` flat struct because impl IDs are not first-class user
    * declarations. The fields are the same as the interface; only the
    * `usedBy`/`ownerId` differs.
    */
  def implFlatStruct(implId: izumi.idealingua.model.common.TypeId.DTOId, ifaceFlat: FlatStruct): FlatStruct = {
    FlatStruct(
      ownerId       = implId,
      fields        = ifaceFlat.fields,
      conflictsHard = ifaceFlat.conflictsHard,
      conflictsSoft = ifaceFlat.conflictsSoft,
    )
  }

  /** Extract `Super` from a `TypeDef.Dto`/`TypeDef.Interface`. */
  def superOf(td: NewTypeDef): Super = td match {
    case d: NewTypeDef.Dto       => d.struct.superclasses
    case i: NewTypeDef.Interface => i.struct.superclasses
    case _                       => Super.empty
  }

  /** Extract `Struct` (the new IR's per-type declaration struct) from
    * `TypeDef.Dto`/`TypeDef.Interface`.
    */
  def declStruct(td: NewTypeDef): Option[Struct] = td match {
    case d: NewTypeDef.Dto       => Some(d.struct)
    case i: NewTypeDef.Interface => Some(i.struct)
    case _                       => None
  }

  /** Construct an impl `DTOId` for an interface (mirrors legacy
    * `TypespaceToolsImpl.implId`: `DTOId(iface, "Struct")`).
    */
  def implId(id: InterfaceId): izumi.idealingua.model.common.TypeId.DTOId = {
    izumi.idealingua.model.common.TypeId.DTOId(id, "Struct")
  }

  /** Trivial structural Fields-from-FlatStruct projection used by the
    * Identifier renderer's parser/toString machinery.
    */
  def fieldsOf(flat: FlatStruct): List[Field] = flat.fields.map(_.field)
}
