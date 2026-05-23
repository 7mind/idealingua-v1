package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.common.{ExtendedField, FieldDef, StructureId, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, Super}
import izumi.idealingua.translator.common.{LegacyMapKeyOrder, LegacyStructOrdering}
import izumi.idealingua.translator.toscala.tools.ScalaTextHelpers
import izumi.idealingua.translator.toscala.types.{ScalaField, ScalaStruct, ScalaTypeConverter}
import izumi.idealingua.typer.ir.{Domain, FlatStruct, Struct, TypeDef => NewTypeDef}

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
    // `flat.fields` carries the BFS distance the flattener assigned. For the
    // legacy sort key we instead need the DFS-first-visit depth — see
    // `LegacyStructOrdering.legacyDfsDistance` for the why.
    val dfsDistance: Map[(TypeId, String), Int] = LegacyStructOrdering.legacyDfsDistance(id, domain)
    val seenPerOrigin                           = scala.collection.mutable.LinkedHashMap.empty[TypeId, Int]
    val extendedRaw: List[ExtendedField] = flat.fields.map {
      ff =>
        val perOriginIdx = {
          val n = seenPerOrigin.getOrElse(ff.origin, 0)
          seenPerOrigin.update(ff.origin, n + 1)
          n
        }
        val idx  = LegacyStructOrdering.originIndex(domain, ff.origin, ff.field.name).getOrElse(perOriginIdx)
        val dist = dfsDistance.getOrElse((ff.origin, ff.field.name), ff.distance)
        ExtendedField(
          field = ff.field,
          defn = FieldDef(
            definedBy        = ff.origin,
            definedWithIndex = idx,
            usedBy           = id,
            distance         = dist,
          ),
        )
    }

    // Defect #7 / IMPL-7a.2-Fe4 (revised, F-DTO1-fieldorder): field-name
    // dedup matching legacy `StructuralQueriesImpl.NonContradictive`.
    //
    // Legacy `FieldExtractor.extractFields` emits
    // `superFields ++ embeddedFields ++ thisFields` recursively (DFS), and
    // the `NonContradictive` branch for equal-type duplicates returns
    // `Some(fields.head)` — the FIRST occurrence in that DFS emission order.
    //
    // The new IR's flattener emits BFS, which does NOT in general agree with
    // legacy DFS on which occurrence comes first. Counter-example
    // (`izumi.test.domain02.DTO1` mixing `& TestInterface2` + `& TestInterface3`,
    // where `TestInterface3 & TestInterface1`):
    //   - `sameField` exists on TestInterface2 (distance 1, declared directly
    //     on a sibling parent) AND on TestInterface1 (distance 2, reached
    //     through TestInterface3).
    //   - Legacy DFS visits TestInterface2 first (the first sibling super),
    //     so `head` is the TestInterface2 entry → distance 1.
    //   - Fe4's "max distance" rule picked the TestInterface1 entry
    //     (distance 2), producing a different case-class constructor
    //     parameter order vs v1.4.19's idlc (regression surfaced by
    //     X1 `v1419-vs-head-compat-{scala,csharp}` baselines).
    //
    // Fix: replicate the legacy DFS emission order explicitly via
    // `legacyDfsPosition`, then for equal-type duplicates keep the
    // smallest-position entry (== `fields.head` under legacy emission).
    // For true covariant overrides (different `Field` typeIds across
    // occurrences) keep the smallest-distance entry — the closest
    // declaration is the type-refined primary, matching legacy
    // `NonContradictive`'s `x.head` after `sortBy(_.defn.distance)`.
    val dfsPos: Map[(TypeId, String), Int] = LegacyStructOrdering.legacyDfsPosition(id, domain)
    def posOf(ef: ExtendedField): Int =
      dfsPos.getOrElse((ef.defn.definedBy, ef.field.name), Int.MaxValue)
    val deduped: List[ExtendedField] = {
      val byName = scala.collection.mutable.LinkedHashMap.empty[String, scala.collection.mutable.ListBuffer[ExtendedField]]
      extendedRaw.foreach {
        f =>
          val buf = byName.getOrElseUpdate(f.field.name, scala.collection.mutable.ListBuffer.empty)
          buf += f
      }
      byName.values.map {
        occurrences =>
          val typesEqual = occurrences.map(_.field).toSet.size == 1
          if (typesEqual) occurrences.minBy(posOf)
          else occurrences.minBy(_.defn.distance)
      }.toList
    }

    // Defect #5: apply the legacy sort key.
    val sorted: List[ExtendedField] = LegacyStructOrdering.sortLegacyKey(deduped)

    // Diamond-apex regression (F-1.5.0-apply-args): pre-1.5.0 the legacy
    // `FieldExtractor` was a DFS that stamped each field with
    // `FieldDef.distance = depth-of-discovery`. For an ancestor `A`
    // reachable from `id` via two paths of EQUAL depth the two extracted
    // `ExtendedField`s were structurally identical, so `.distinct`
    // collapsed them and `findConflicts` saw `size=1` → unambigious → `A`
    // emitted as a mixin parameter. For paths of DIFFERENT depths the
    // entries differed in `distance`, `.distinct` kept both and
    // `findConflicts` flagged a soft conflict → ambigious → fields
    // emitted as scalars at the tail of `apply(...)`.
    //
    // The post-#610 BFS flattener visits each ancestor exactly once at
    // its shortest distance, so the asymmetry never surfaces. Replicate
    // the legacy quirk explicitly: an ancestor is a diamond apex iff it
    // is reachable from `id` at two or more distinct depths through
    // `superclasses.{interfaces, concepts}` edges. Routing its fields
    // through `ambiguousNames` makes the emitter use its existing scalar-
    // parameter branch.
    //
    // Only `defn.definedBy` values that actually appear among the
    // inherited fields can become mixin parameters, so we only have to
    // classify those.
    val contributingAncestors: Set[TypeId] = {
      val b = scala.collection.mutable.LinkedHashSet.empty[TypeId]
      extendedRaw.foreach(ef => b += ef.defn.definedBy)
      b.toSet
    }

    // Rebuilt per `fromFlat` call. Cheap at current corpus scale; if
    // codegen ever becomes a bottleneck, lift to a domain-keyed cache.
    def directSupersOf(tid: TypeId): List[TypeId] = {
      def fromSuper(s: Super): List[TypeId] = s.interfaces ++ s.concepts
      domain.userTypes.get(tid) match {
        case Some(d: NewTypeDef.Dto)       => fromSuper(d.struct.superclasses)
        case Some(i: NewTypeDef.Interface) => fromSuper(i.struct.superclasses)
        case _ =>
          domain.members.get(tid) match {
            case Some(izumi.idealingua.typer.ir.Member.Ephemeral(eph)) => fromSuper(eph.struct.superclasses)
            case _                                                     => Nil
          }
      }
    }

    val diamondApexes: Set[TypeId] = {
      // Visited keyed by `(node, depth)` so distinct depths are
      // enumerated. Safe because cyclic inheritance is rejected earlier
      // in the typer pipeline; with a cycle this loop would not
      // terminate.
      val depthsOf = scala.collection.mutable.Map.empty[TypeId, scala.collection.mutable.Set[Int]]
      val visited  = scala.collection.mutable.LinkedHashSet.empty[(TypeId, Int)]
      val queue    = scala.collection.mutable.Queue.empty[(TypeId, Int)]
      queue.enqueue(id -> 0)
      while (queue.nonEmpty) {
        val (cur, depth) = queue.dequeue()
        if (visited.add(cur -> depth)) {
          depthsOf.getOrElseUpdate(cur, scala.collection.mutable.Set.empty[Int]) += depth
          directSupersOf(cur).foreach(p => queue.enqueue(p -> (depth + 1)))
        }
      }
      contributingAncestors.filter(a => depthsOf.get(a).exists(_.size >= 2))
    }

    val diamondApexFieldNames: Set[String] =
      if (diamondApexes.isEmpty) Set.empty
      else
        extendedRaw.iterator.collect {
          case ef if diamondApexes.contains(ef.defn.definedBy) => ef.field.name
        }.toSet

    val ambiguousNames: Set[String] =
      (flat.conflictsSoft.map(_.name) ++ flat.conflictsHard.map(_.name)).toSet ++ diamondApexFieldNames

    val unambigious = sorted.filterNot(f => ambiguousNames.contains(f.field.name))

    // `ambigious` parameter order: the legacy `findConflicts` populated a
    // `LinkedHashMap` by iterating `all.groupBy(_.field.name)`, and the
    // apply emitter consumed those values in insertion order. The Scala
    // stdlib's `immutable.Map` is size-specialised: `Map1`–`Map4`
    // preserve insertion order, `HashMap` (size ≥ 5) uses a CHAMP
    // hash-trie traversal. We could delegate to `Map[String, _]` and
    // get the same ordering for free, but the iteration order is a
    // stdlib implementation detail — a future Scala release that, say,
    // sorts every `Map` by `hashCode` (or switches to a different trie)
    // would silently shift our emitted parameter order on a routine
    // dependency bump. Replicate Scala 2.13's algorithm explicitly so
    // the order is locked to our code, not theirs.
    val ambigious: List[ExtendedField] = {
      val perName: Map[String, ExtendedField] =
        sorted.iterator.map(f => f.field.name -> f).toMap
      val distinctNames: List[String] =
        extendedRaw.iterator.map(_.field.name).distinct.toList
      LegacyMapKeyOrder
        .apply(distinctNames)
        .filter(ambiguousNames.contains)
        .flatMap(perName.get)
    }

    new izumi.idealingua.model.typespace.structures.Struct(
      id           = id,
      superclasses = supers,
      unambigious  = unambigious,
      ambigious    = ambigious,
      all          = sorted,
    )
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

    // F-TextTree M8e..M8f: `ScalaField` is String-native. Scala 3 keyword
    // escape for field identifier names (`package` → `` `package` ``) is
    // applied via `ScalaTextHelpers.escapeIdent(_)`, the same pattern M8c
    // uses inside cast extensions for reserved field names.
    def toScalaField(field: ExtendedField): ScalaField = {
      val name     = field.field.name
      val nameSafe = ScalaTextHelpers.escapeIdent(name)
      val tpe      = conv.toScala(field.field.typeId).typeFull.toString
      ScalaField(name, nameSafe, tpe, field)
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
