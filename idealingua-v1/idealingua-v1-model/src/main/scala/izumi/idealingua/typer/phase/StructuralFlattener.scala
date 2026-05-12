package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Primitive, StructureId, TypeId}
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

  def apply(rd: ResolvedDomain): ResolvedDomain = {
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
    val parentsMap: Map[TypeId, Set[InterfaceId]] = parentsBuf.toMap
    val viewsMap: Map[StructureId, StructView]    = views.toMap
    views.keys.foreach { id =>
      flatBuf.update(id, flatten(id, viewsMap, rd, directSupers, parentsMap, diagBuf))
    }

    rd.copy(
      parents          = parentsBuf.toMap,
      implementingDtos = implementingDtos,
      flattenedStructs = flatBuf.toMap,
      diagnostics      = rd.diagnostics ++ Diagnostics(diagBuf.toVector),
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
    val removed = mutable.LinkedHashSet.empty[String]
    val visited = mutable.LinkedHashSet.empty[StructureId]

    // BFS layer-by-layer; distance increases with each layer.
    val frontier = mutable.Queue.empty[(StructureId, Int)]
    frontier.enqueue(ownerId -> 0)

    while (frontier.nonEmpty) {
      val (cur, distance) = frontier.dequeue()
      if (visited.add(cur)) {
        views.get(cur) match {
          case Some(v) =>
            v.removedFields.foreach(f => removed.add(f.name))
            // F-subtraction (PR-02 IMPL-7a.2-Fh): expand each removed concept
            // at this level to its transitively flattened field names so
            // `data X { + S; - Y }` strips every field that `Y` would have
            // contributed through `S` (mirrors legacy
            // `FieldExtractor.extractRemoved` + `filterFields`,
            // `FieldExtractor.scala:78-91` + `:61-76`).
            v.removedConcepts.foreach { c =>
              removedConceptFieldNames(c, views).foreach(removed.add)
            }
            v.fields.foreach(f => all += FlatField(f, cur, distance))
            directSupers.getOrElse(cur, Nil).foreach(s => frontier.enqueue(s -> (distance + 1)))
          case None => ()
        }
      }
    }

    val filtered = all.toList.filterNot(ff => removed.contains(ff.field.name))

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
