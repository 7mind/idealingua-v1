package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{DomainId, Primitive, StructureId, TypeId}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.typer.ir._

import scala.collection.mutable

/** Phase 6 — `StructuralFlattener`.
  *
  * Computes:
  *   - `parents: Map[TypeId, Set[InterfaceId]]` — transitive interface
  *     supertypes of every DTO/Interface (`struct.superclasses.interfaces`).
  *   - `implementingDtos: Map[InterfaceId, Set[DTOId]]` — inversion of
  *     `parents` restricted to DTO leaves.
  *   - `flattenedStructs: Map[StructureId, FlatStruct]` — BFS-flattened
  *     field list with `FlatField(field, origin, distance)`, conflict
  *     classification (hard vs soft), and propagated `removedFields`.
  *
  * Field-order invariant (C12/L3): preserved via `mutable.ListBuffer` →
  * `.toList` throughout; no `groupBy` followed by `.toMap` without sort-key
  * restoration.
  *
  * Diagnostics:
  *   - `FieldNameConflict` — two fields with same name + incompatible types.
  *   - `MissingMixin` — supertype reference points at a non-user type.
  *
  * Covariant overrides (F2 / IMPL-7a.2): when the same field name appears
  * on multiple ancestors with non-identical types, the closest (smallest
  * BFS distance) declaration wins iff its type is a subtype of every
  * other candidate's type — i.e. `typeId` is in the transitive parent
  * closure of the closest declaration. This matches the legacy
  * `StructuralQueriesImpl.NonContradictive` rule
  * (`StructuralQueriesImpl.scala:72-99`) so a child can refine a parent
  * field's type to a more derived structural type. Primitives admit no
  * subtyping (legacy `isParent` requires equality).
  *
  * Per C8/L1, never throws on user input.
  */
object StructuralFlattener {

  /** Owner-resolved view of a structural type for BFS flattening: its
    * direct fields, its declared removed fields, and its supertype list.
    * Used so the BFS walker can treat user DTOs/Interfaces and synthesized
    * ephemeral DTOs uniformly (F8 fix — IMPL-7a.2).
    */
  private final case class StructView(
    fields: List[Field],
    removedFields: List[Field],
    supers: List[StructureId],
    removedConcepts: List[StructureId],
  )

  /** Back-compat overload for tests and call sites that don't have a
    * cross-domain `FamilyIndex` handy.  Cross-domain mixin fields will not be
    * flattened (legacy single-domain behaviour); use `apply(rd, family)` from
    * the production pipeline so foreign mixin structs are walked too (PR-02
    * IMPL-7a.2-Fj).
    */
  def apply(rd: ResolvedDomain): ResolvedDomain = apply(rd, None)

  /** Production overload: cross-domain mixin fields are flattened by
    * resolving foreign domain meshes through `family` (PR-02 IMPL-7a.2-Fj).
    *
    * For each direct supertype `S` of any struct in `rd` whose
    * `S.path.domain != rd.id`, the flattener runs `ScopeBuilder` +
    * `NameResolver` on the foreign mesh (cached per-domain via
    * `family.domains(...)`) and harvests its `Dto`/`Interface` structs into
    * the BFS `views` map. Transitive foreign-of-foreign mixins are followed
    * in the same way. Foreign-domain diagnostics are discarded — the foreign
    * domain runs its own typer pass and surfaces them through its own
    * pipeline result, so re-emitting them here would only double-report.
    */
  def apply(rd: ResolvedDomain, family: FamilyIndex): ResolvedDomain =
    apply(rd, Some(family))

  private def apply(rd: ResolvedDomain, family: Option[FamilyIndex]): ResolvedDomain = {
    val diagBuf = mutable.ArrayBuffer.empty[Diagnostic]
    val parentsBuf = mutable.LinkedHashMap.empty[TypeId, Set[InterfaceId]]
    val flatBuf = mutable.LinkedHashMap.empty[StructureId, FlatStruct]

    // ----- Collect every flattenable struct (user DTOs/Interfaces +
    //       synthesized ephemeral DTOs from Phase 7). -----
    //
    // F8 fix (IMPL-7a.2): ephemeral input/output DTOs synthesized by
    // `EphemeralSynthesizer` are kept under `Member.Ephemeral` in
    // `rd.members` (not in `rd.userTypes`). Prior to this fix the
    // flattener only consulted `rd.userTypes` and ephemerals had no
    // `FlatStruct` entry, which made the Scala renderer fall back to
    // an empty field list (`DomainServiceMethodProduct.scala:200-203`)
    // and emit every service method with zero parameters.
    val views = mutable.LinkedHashMap.empty[StructureId, StructView]
    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        views.update(
          dto.id,
          StructView(
            dto.struct.fields,
            dto.struct.removedFields,
            dto.struct.superclasses.interfaces ++ dto.struct.superclasses.concepts,
            dto.struct.superclasses.removedConcepts,
          ),
        )
      case ifc: TypeDef.Interface =>
        views.update(
          ifc.id,
          StructView(
            ifc.struct.fields,
            ifc.struct.removedFields,
            ifc.struct.superclasses.interfaces ++ ifc.struct.superclasses.concepts,
            ifc.struct.superclasses.removedConcepts,
          ),
        )
      case _ => ()
    }
    rd.members.values.foreach {
      case Member.Ephemeral(eph) =>
        views.update(
          eph.id,
          StructView(
            eph.struct.fields,
            eph.struct.removedFields,
            eph.struct.superclasses.interfaces ++ eph.struct.superclasses.concepts,
            eph.struct.superclasses.removedConcepts,
          ),
        )
      case _ => ()
    }

    // ----- Pre-collect direct supertypes -----
    val directSupers = mutable.LinkedHashMap.empty[StructureId, List[StructureId]]
    views.foreach { case (id, v) => directSupers.update(id, v.supers) }

    // Per-node interface and concept supertype lists (mirrors legacy
    // `safeAllParents` split, `InheritanceQueriesImpl.scala:32-79`).
    val directInterfaces = mutable.LinkedHashMap.empty[StructureId, List[StructureId]]
    val directConcepts   = mutable.LinkedHashMap.empty[StructureId, List[StructureId]]
    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        directInterfaces.update(dto.id, dto.struct.superclasses.interfaces)
        directConcepts.update(dto.id, dto.struct.superclasses.concepts)
      case ifc: TypeDef.Interface =>
        directInterfaces.update(ifc.id, ifc.struct.superclasses.interfaces)
        directConcepts.update(ifc.id, ifc.struct.superclasses.concepts)
      case _ => ()
    }
    rd.members.values.foreach {
      case Member.Ephemeral(eph) =>
        directInterfaces.update(eph.id, eph.struct.superclasses.interfaces)
        directConcepts.update(eph.id, eph.struct.superclasses.concepts)
      case _ => ()
    }

    // ----- Harvest cross-domain mixin structs (PR-02 IMPL-7a.2-Fj) -----
    //
    // The flattener was previously per-domain: cross-domain mixin parents
    // (`& foreignDomain#M`) had no entry in `views`, so their fields were
    // silently dropped from the flattened struct. This left renderer output
    // referencing a `case class D(...)` constructor signature that omitted
    // the foreign-mixin fields (e.g. `idltest.aliases.D1` missed `f2: String`
    // from `idltest.aliases2#M2`), producing compile-gate errors.
    //
    // Fix: for each foreign domain reachable through `directSupers`, run
    // `ScopeBuilder` + `NameResolver` on its mesh (cached per-domain) and
    // harvest its `Dto`/`Interface` structs into `views`/`directSupers`/
    // `directInterfaces`/`directConcepts`. Recurse over foreign-of-foreign
    // supertypes so the BFS walker eventually reaches every transitive
    // ancestor. Foreign diagnostics are dropped — the foreign domain owns
    // its own pipeline run and surfaces its own diagnostics there.
    //
    // Full TypeDef for every foreign user type reachable through field references
    // (not just supers). Populated below alongside the super-driven harvest;
    // consumed by renderer-side AnyVal predicates and other consumers that need
    // to classify cross-domain types (e.g. `DomainAnyvalExtension.canBeAnyValField`).
    val foreignUserTypesBuf = mutable.LinkedHashMap.empty[TypeId, TypeDef]
    family.foreach { fam =>
      val foreignResolved  = mutable.HashMap.empty[DomainId, Option[ResolvedDomain]]
      val indexedDomains   = mutable.HashSet.empty[DomainId]

      def resolveForeign(d: DomainId): Option[ResolvedDomain] =
        foreignResolved.getOrElseUpdate(
          d, {
            if (d == rd.id) None
            else
              fam.domains.get(d).map { mesh =>
                val scoped = ScopeBuilder(d, mesh, fam)
                NameResolver(scoped, fam)
              }
          },
        )

      // Once a foreign domain is resolved, store the full TypeDef for every
      // foreign user type. Consumers query this map to classify cross-domain
      // types that appear in local field positions (e.g.
      // `data D { val: foreign.X#ItemID }`). Widened from Identifier-only to
      // all TypeDef categories so enums, ADTs, DTOs, aliases, etc. are also
      // available for free.
      // D08: memoised via `indexedDomains` — each domain is walked at most once.
      def indexForeignUserTypes(d: DomainId): Unit = {
        if (!indexedDomains.add(d)) return
        resolveForeign(d).foreach { resolved =>
          resolved.userTypes.foreach {
            case (id, td) if id.path.domain != rd.id => foreignUserTypesBuf.update(id, td)
            case _ => ()
          }
        }
      }

      // Helper shared by DTO and Interface branches of harvest: for each field
      // of a freshly-harvested foreign struct, index the field's domain and
      // recursively harvest any foreign StructureId referenced — so third-domain
      // (and deeper) types are reached transitively (D06).
      def harvestFieldTypes(fields: List[Field]): Unit = fields.foreach { f =>
        val tid = f.typeId
        val d   = tid.path.domain
        if (d != rd.id) indexForeignUserTypes(d)
        tid match {
          case sid: StructureId if sid.path.domain != rd.id => harvest(sid)
          case _                                             => ()
        }
      }

      def harvest(id: StructureId): Unit = {
        if (views.contains(id)) return
        val resolved = resolveForeign(id.path.domain).getOrElse(return)
        indexForeignUserTypes(id.path.domain)
        resolved.userTypes.get(id) match {
          case Some(dto: TypeDef.Dto) =>
            views.update(
              id,
              StructView(
                dto.struct.fields,
                dto.struct.removedFields,
                dto.struct.superclasses.interfaces ++ dto.struct.superclasses.concepts,
                dto.struct.superclasses.removedConcepts,
              ),
            )
            directSupers.update(id, dto.struct.superclasses.interfaces ++ dto.struct.superclasses.concepts)
            directInterfaces.update(id, dto.struct.superclasses.interfaces)
            directConcepts.update(id, dto.struct.superclasses.concepts)
            (dto.struct.superclasses.interfaces ++ dto.struct.superclasses.concepts).foreach(harvest)
            harvestFieldTypes(dto.struct.fields)
          case Some(ifc: TypeDef.Interface) =>
            views.update(
              id,
              StructView(
                ifc.struct.fields,
                ifc.struct.removedFields,
                ifc.struct.superclasses.interfaces ++ ifc.struct.superclasses.concepts,
                ifc.struct.superclasses.removedConcepts,
              ),
            )
            directSupers.update(id, ifc.struct.superclasses.interfaces ++ ifc.struct.superclasses.concepts)
            directInterfaces.update(id, ifc.struct.superclasses.interfaces)
            directConcepts.update(id, ifc.struct.superclasses.concepts)
            (ifc.struct.superclasses.interfaces ++ ifc.struct.superclasses.concepts).foreach(harvest)
            harvestFieldTypes(ifc.struct.fields)
          case _ => ()
        }
      }

      // Snapshot keys to avoid concurrent-modification while we mutate views.
      val seedSupers = directSupers.values.flatten.toList
      seedSupers.foreach { sup =>
        if (sup.path.domain != rd.id) harvest(sup)
      }

      // Seed foreign user-type harvest and DTO/Interface struct harvest from
      // first-hop field-type references. With the new transitive harvest in
      // `harvest` itself (D06), this seed only needs to find the *first-hop*
      // foreign references — `harvest` follows deeper hops automatically.
      val foreignFieldDomains = mutable.LinkedHashSet.empty[DomainId]
      // Collect foreign StructureId (DTOId / InterfaceId) references seen in
      // field positions. `harvest(sid)` populates `crossDomainFlattenedStructs`
      // for those structs, which `canBeAnyValField` consults when classifying
      // a local DTO that carries a single foreign DTO/Interface field. The
      // super-driven harvest only follows mixin parents, so a foreign struct
      // referenced purely as a field type (not as a mixin) would otherwise
      // remain absent from `crossDomainFlattenedStructs` and the predicate
      // would return `false`, breaking AnyVal emission for those shapes.
      val foreignFieldStructs = mutable.LinkedHashSet.empty[StructureId]
      def collectDomain(tid: TypeId): Unit = {
        val d = tid.path.domain
        if (d != rd.id) {
          val _ = foreignFieldDomains.add(d)
          tid match {
            case sid: StructureId => val _ = foreignFieldStructs.add(sid)
            case _                => ()
          }
        }
      }
      views.values.foreach(_.fields.foreach(f => collectDomain(f.typeId)))
      rd.userTypes.values.foreach {
        case TypeDef.Identifier(_, fields, _) => fields.foreach(f => collectDomain(f.typeId))
        case TypeDef.Alias(_, target, _)      => collectDomain(target)
        case _                                => ()
      }
      foreignFieldDomains.foreach(indexForeignUserTypes)
      // Second pass: harvest foreign DTO/Interface structs referenced in field
      // positions so `crossDomainFlattenedStructs` is populated for them.
      // `harvest` calls `indexForeignUserTypes` internally, so the full foreign
      // user-type set for any newly-visited domain is indexed for free.
      foreignFieldStructs.foreach(harvest)
    }

    /** Walk only `interfaces` edges; recurses transitively. Returns the
      * interface ancestors reachable purely through `&` declarations
      * (mirrors legacy `safeParentsInherited` *strict ancestors part*,
      * `InheritanceQueriesImpl.scala:36-49`, with self excluded —
      * `Domain.parents` consumers add `{id}` explicitly,
      * `DomainCastUpExtension.scala:19-20`).
      */
    def ancestorInterfaces(start: StructureId): mutable.LinkedHashSet[InterfaceId] = {
      val acc     = mutable.LinkedHashSet.empty[InterfaceId]
      val visited = mutable.LinkedHashSet.empty[StructureId]
      def walk(cur: StructureId): Unit = {
        if (visited.add(cur)) {
          directInterfaces.getOrElse(cur, Nil).foreach {
            case i: InterfaceId => val _ = acc.add(i); walk(i)
            case _              => ()
          }
        }
      }
      walk(start)
      acc
    }

    /** Legacy `safeAllParents` strict-ancestor variant
      * (`InheritanceQueriesImpl.scala:32-34`): union of strict-ancestor
      * `parentsInherited` (interfaces-only chase) and `parentsConcepts`
      * (own concepts walked with `safeAllParents`). The asymmetry —
      * interfaces walked recursively via interface-edges only, concepts
      * walked recursively via *both* edge types — is preserved so
      * `_downcast_extend_*` emissions match legacy `compatibleDtos`.
      */
    def transitiveParents(start: StructureId): Set[InterfaceId] = {
      val acc = mutable.LinkedHashSet.empty[InterfaceId]
      val visited = mutable.LinkedHashSet.empty[StructureId]
      def walk(cur: StructureId): Unit = {
        if (visited.add(cur)) {
          // strict-ancestor interfaces walk (excludes `cur` itself)
          ancestorInterfaces(cur).foreach(i => { val _ = acc.add(i); () })
          // parentsConcepts: own concepts → recurse with safeAllParents.
          // Per legacy, when crossing into a concept C, safeAllParents(C)
          // includes C itself if it's an interface (the parentsInherited
          // self-inclusion arm). Replicate by adding C if interface.
          directConcepts.getOrElse(cur, Nil).foreach {
            case sid: StructureId =>
              sid match {
                case i: InterfaceId => val _ = acc.add(i); ()
                case _              => ()
              }
              walk(sid)
            case _ => ()
          }
        }
      }
      walk(start)
      acc.toSet
    }

    // `parents`/`implementingDtos` cover user-declared structural types
    // plus synthesized interface mirror DTOs (`DTOId(I, "Struct")`).
    // Including the mirror DTOs is required so the
    // `DomainCastDownExpandExtension` ↔ `Typespace.compatibleDtos` parity is
    // preserved: legacy `compatibleDtos(I)` returns the mirror DTO because
    // it `extends I`, which lets legacy emit
    // `I_downcast_extend_<Mirror>` in `I`'s companion. The new IR must do
    // the same. Other ephemerals (method-input/output DTOs, DTO→Interface
    // `Defn` mirrors) stay out of `implementingDtos` since they have no
    // interface parents to surface.
    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        parentsBuf.update(dto.id, transitiveParents(dto.id))
      case ifc: TypeDef.Interface =>
        parentsBuf.update(ifc.id, transitiveParents(ifc.id))
      case _ => ()
    }
    rd.members.values.foreach {
      case Member.Ephemeral(eph) if eph.origin.isInstanceOf[EphemeralOrigin.InterfaceMirror] =>
        parentsBuf.update(eph.id, transitiveParents(eph.id))
      case _ => ()
    }

    // ----- implementingDtos: inversion -----
    val implBuf = mutable.LinkedHashMap.empty[InterfaceId, mutable.LinkedHashSet[DTOId]]
    parentsBuf.foreach {
      case (dtoId: DTOId, ifaces) =>
        ifaces.foreach {
          iface =>
            val set = implBuf.getOrElseUpdate(iface, mutable.LinkedHashSet.empty)
            val _   = set.add(dtoId)
        }
      case _ => ()
    }
    val implementingDtos: Map[InterfaceId, Set[DTOId]] =
      implBuf.view.mapValues(_.toSet).toMap

    // ----- mixin/missing-mixin check -----
    // Only flag local supertypes; cross-domain supertypes (`sup.path.domain
    // != rd.id`) are validated by the foreign domain's own typer pass and
    // their structs are reached through the imports graph at flatten time.
    // Mirrors the legacy recursive `getDomain(domainId(out.path.toPackage))`
    // dispatch in `IDLTyper.fixSimpleId` — same-domain entries go through
    // the local mapping/index, others are delegated to the foreign typer.
    directSupers.foreach {
      case (owner, sups) =>
        sups.foreach {
          sup =>
            if (sup.path.domain == rd.id && !rd.userTypes.contains(sup)) {
              diagBuf += Diagnostic.MissingMixin(owner, sup, positionOf(rd, owner))
            }
        }
    }

    // ----- BFS flatten -----
    // BFS walks every flattenable struct — both user-declared and
    // synthesized ephemerals — so renderer lookups against `flattenedStructs`
    // are total (F8 fix).
    //
    // Cross-domain harvested views (PR-02 IMPL-7a.2-Fj) participate in BFS
    // as ancestors but do NOT receive their own FlatStruct entry in the
    // local domain — they belong to a different domain's `flattenedStructs`
    // map and will be produced there.
    val parentsMap: Map[TypeId, Set[InterfaceId]] = parentsBuf.toMap
    val viewsMap: Map[StructureId, StructView]    = views.toMap
    val localStructIds: Set[StructureId] = {
      val acc = mutable.LinkedHashSet.empty[StructureId]
      rd.userTypes.values.foreach {
        case dto: TypeDef.Dto       => val _ = acc.add(dto.id)
        case ifc: TypeDef.Interface => val _ = acc.add(ifc.id)
        case _                      => ()
      }
      rd.members.values.foreach {
        case Member.Ephemeral(eph) => val _ = acc.add(eph.id)
        case _                     => ()
      }
      acc.toSet
    }
    // Foreign-harvested views also get a flat struct, but in a separate map
    // so local-only iterators (cast-similar peer scan, anyval candidate
    // detection) do not accidentally surface foreign entries. The TS
    // renderer falls back to `Domain.crossDomainFlattenedStructs` when a
    // foreign interface's own structure is required (e.g. the
    // `to<Iface>Serialized` slice body for a cross-domain mixin) —
    // PR-02 IMPL-10b-fix.
    val foreignFlatBuf = mutable.LinkedHashMap.empty[StructureId, FlatStruct]
    // Foreign-domain diagnostics are owned by the foreign domain's pipeline
    // pass (mirrors the foreign-harvest comment above); discard them here to
    // avoid double-reporting.
    val foreignDiagSink = mutable.ArrayBuffer.empty[Diagnostic]
    views.keys.foreach { id =>
      if (localStructIds.contains(id)) {
        flatBuf.update(id, flatten(id, viewsMap, rd, directSupers, parentsMap, diagBuf))
      } else if (id.path.domain != rd.id) {
        foreignFlatBuf.update(id, flatten(id, viewsMap, rd, directSupers, parentsMap, foreignDiagSink))
      }
    }

    rd.copy(
      parents                     = parentsBuf.toMap,
      implementingDtos            = implementingDtos,
      flattenedStructs            = flatBuf.toMap,
      crossDomainFlattenedStructs = foreignFlatBuf.toMap,
      crossDomainUserTypes        = foreignUserTypesBuf.toMap,
      diagnostics                 = rd.diagnostics ++ Diagnostics(diagBuf.toVector),
    )
  }

  private def positionOf(rd: ResolvedDomain, id: TypeId): InputPosition =
    rd.userTypes.get(id).map(_.meta.pos).getOrElse(InputPosition.Undefined)

  /** Subtype predicate matching legacy `StructuralQueriesImpl.isParent`
    * (`StructuralQueriesImpl.scala:93-99`).
    *
    *   - `child == ancestor` always succeeds (reflexive).
    *   - Either side primitive → must be equal (legacy short-circuits on
    *     `Primitive`; no subtyping between builtins).
    *   - Otherwise `ancestor` must appear in `child`'s transitive inherited
    *     parent set — i.e. `parentsMap(child).contains(ancestor)`.
    *
    * `parentsMap` carries `InterfaceId` only (structural supertypes), so an
    * `ancestor` that is not an `InterfaceId` matches only via reflexivity.
    */
  private def isSubtypeOrEqual(child: TypeId, ancestor: TypeId, parentsMap: Map[TypeId, Set[InterfaceId]]): Boolean = {
    if (child == ancestor) true
    else if (child.isInstanceOf[Primitive] || ancestor.isInstanceOf[Primitive]) false
    else
      ancestor match {
        case iid: InterfaceId => parentsMap.getOrElse(child, Set.empty).contains(iid)
        case _                => false
      }
  }

  /** Transitively collect every field name that a `- Concept` subtraction
    * removes when applied at a struct level.  Mirrors legacy
    * `FieldExtractor.extractRemoved` ↔ `extractFields` recursion
    * (`FieldExtractor.scala:78-91` + `:9-41`): the removal expands to the
    * concept's own fields plus all fields contributed by its `interfaces`
    * and `concepts` ancestry.  Cycles short-circuit via `visited`.
    */
  private def removedConceptFieldNames(
    concept: StructureId,
    views: Map[StructureId, StructView],
  ): Set[String] = {
    val acc     = mutable.LinkedHashSet.empty[String]
    val visited = mutable.LinkedHashSet.empty[StructureId]
    val queue   = mutable.Queue.empty[StructureId]
    queue.enqueue(concept)
    while (queue.nonEmpty) {
      val cur = queue.dequeue()
      if (visited.add(cur)) {
        views.get(cur) match {
          case Some(v) =>
            v.fields.foreach(f => acc.add(f.name))
            v.supers.foreach(queue.enqueue)
          case None => ()
        }
      }
    }
    acc.toSet
  }

  private def flatten(
    ownerId: StructureId,
    views: Map[StructureId, StructView],
    rd: ResolvedDomain,
    directSupers: mutable.LinkedHashMap[StructureId, List[StructureId]],
    parentsMap: Map[TypeId, Set[InterfaceId]],
    diagBuf: mutable.ArrayBuffer[Diagnostic],
  ): FlatStruct = {
    val all     = mutable.ListBuffer.empty[FlatField]
    // Removal is keyed by `(typeId, name)` so that a `- name: T1` clause
    // strips only the inherited `name: T1` field, leaving a sibling
    // `name: T2` declaration intact — matches legacy
    // `FieldExtractor.filterFields` (`FieldExtractor.scala:75`,
    // `removable.filterNot(removedFields.contains(_.field))` where
    // `removedFields: Set[Field]` compares full Field identity). Removing by
    // name alone breaks shapes like
    // `data TeamRank { + Rank; - id: UserId; id: TeamId }` — the local
    // redeclaration shares the name but the type differs.
    val removed = mutable.LinkedHashSet.empty[(TypeId, String)]
    // Removed concepts only carry NAMES (the legacy `extractRemoved` walk
    // recurses into a removed concept and flattens its full field list, but
    // we don't carry concept-level type info per name through that walk).
    // Use name-only removal for this path — matches the legacy
    // `extractRemoved` behaviour for `- ConceptId` clauses.
    val removedByName = mutable.LinkedHashSet.empty[String]
    val visited = mutable.LinkedHashSet.empty[StructureId]

    // BFS layer-by-layer; distance increases with each layer.
    val frontier = mutable.Queue.empty[(StructureId, Int)]
    frontier.enqueue(ownerId -> 0)

    while (frontier.nonEmpty) {
      val (cur, distance) = frontier.dequeue()
      if (visited.add(cur)) {
        views.get(cur) match {
          case Some(v) =>
            v.removedFields.foreach(f => removed.add((f.typeId, f.name)))
            // F-subtraction (PR-02 IMPL-7a.2-Fh): expand each removed concept
            // at this level to its transitively flattened field names so
            // `data X { + S; - Y }` strips every field that `Y` would have
            // contributed through `S` (mirrors legacy
            // `FieldExtractor.extractRemoved` + `filterFields`,
            // `FieldExtractor.scala:78-91` + `:61-76`).
            v.removedConcepts.foreach { c =>
              removedConceptFieldNames(c, views).foreach(removedByName.add)
            }
            v.fields.foreach(f => all += FlatField(f, cur, distance))
            directSupers.getOrElse(cur, Nil).foreach(s => frontier.enqueue(s -> (distance + 1)))
          case None => ()
        }
      }
    }

    val filtered = all.toList.filterNot { ff =>
      removed.contains((ff.field.typeId, ff.field.name)) || removedByName.contains(ff.field.name)
    }

    // Group by field name to find conflicts, preserving first-encounter order.
    val byName = mutable.LinkedHashMap.empty[String, mutable.ListBuffer[FlatField]]
    filtered.foreach {
      ff =>
        val buf = byName.getOrElseUpdate(ff.field.name, mutable.ListBuffer.empty)
        buf += ff
    }

    val hardConflicts = mutable.ListBuffer.empty[FieldConflict]
    val softConflicts = mutable.ListBuffer.empty[FieldConflict]

    byName.foreach {
      case (name, fields) =>
        if (fields.size > 1) {
          val types = fields.map(_.field.typeId).toList.distinct
          if (types.size == 1) {
            softConflicts += FieldConflict(name, fields.toList)
          } else {
            // Covariant-override rule (mirrors legacy NonContradictive,
            // `StructuralQueriesImpl.scala:72-91`): sort by BFS distance
            // ascending, take the closest declaration as the primary, and
            // accept the merge iff every other candidate's type is in the
            // primary type's transitive inherited closure (i.e. primary is
            // a subtype of every other). When two candidates share the
            // minimum distance the legacy implementation picks the first
            // by encounter order; `sortBy` is stable, so the same order is
            // preserved here.
            val sorted  = fields.toList.sortBy(_.distance)
            val primary = sorted.head
            val rest    = sorted.tail
            val isCovariant = rest.forall { other =>
              isSubtypeOrEqual(primary.field.typeId, other.field.typeId, parentsMap)
            }
            if (isCovariant) {
              softConflicts += FieldConflict(name, sorted)
            } else {
              hardConflicts += FieldConflict(name, sorted)
              diagBuf += Diagnostic.FieldNameConflict(ownerId, name, types, positionOf(rd, ownerId))
            }
          }
        }
    }

    FlatStruct(
      ownerId       = ownerId,
      fields        = filtered,
      conflictsHard = hardConflicts.toList,
      conflictsSoft = softConflicts.toList,
    )
  }
}
