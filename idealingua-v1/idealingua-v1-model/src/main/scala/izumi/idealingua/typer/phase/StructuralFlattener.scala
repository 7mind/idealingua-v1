package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{StructureId, TypeId}
import izumi.idealingua.model.il.ast.InputPosition
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
  * Per C8/L1, never throws on user input.
  */
object StructuralFlattener {

  def apply(rd: ResolvedDomain): ResolvedDomain = {
    val diagBuf = mutable.ArrayBuffer.empty[Diagnostic]
    val parentsBuf = mutable.LinkedHashMap.empty[TypeId, Set[InterfaceId]]
    val flatBuf = mutable.LinkedHashMap.empty[StructureId, FlatStruct]

    // ----- Pre-collect direct supertypes -----
    val directSupers = mutable.LinkedHashMap.empty[StructureId, List[StructureId]]
    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        directSupers.update(dto.id, dto.struct.superclasses.interfaces ++ dto.struct.superclasses.concepts)
      case ifc: TypeDef.Interface =>
        directSupers.update(ifc.id, ifc.struct.superclasses.interfaces ++ ifc.struct.superclasses.concepts)
      case _ => ()
    }

    // ----- parents map: transitive InterfaceId closure (cycles short-circuit) -----
    def transitiveParents(start: StructureId): Set[InterfaceId] = {
      val acc     = mutable.LinkedHashSet.empty[InterfaceId]
      val visited = mutable.LinkedHashSet.empty[StructureId]
      val queue   = mutable.Queue.empty[StructureId]
      queue.enqueue(start)
      while (queue.nonEmpty) {
        val cur = queue.dequeue()
        if (visited.add(cur)) {
          directSupers.getOrElse(cur, Nil).foreach {
            case iid: InterfaceId =>
              val _ = acc.add(iid)
              queue.enqueue(iid)
            case sid =>
              queue.enqueue(sid)
          }
        }
      }
      acc.toSet
    }

    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        parentsBuf.update(dto.id, transitiveParents(dto.id))
      case ifc: TypeDef.Interface =>
        parentsBuf.update(ifc.id, transitiveParents(ifc.id))
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
    rd.userTypes.values.foreach {
      case dto: TypeDef.Dto =>
        flatBuf.update(dto.id, flatten(dto.id, rd, directSupers, diagBuf))
      case ifc: TypeDef.Interface =>
        flatBuf.update(ifc.id, flatten(ifc.id, rd, directSupers, diagBuf))
      case _ => ()
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

  private def flatten(
    ownerId: StructureId,
    rd: ResolvedDomain,
    directSupers: mutable.LinkedHashMap[StructureId, List[StructureId]],
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
        rd.userTypes.get(cur) match {
          case Some(dto: TypeDef.Dto) =>
            dto.struct.removedFields.foreach(f => removed.add(f.name))
            dto.struct.fields.foreach(f => all += FlatField(f, cur, distance))
            directSupers.getOrElse(cur, Nil).foreach(s => frontier.enqueue(s -> (distance + 1)))
          case Some(ifc: TypeDef.Interface) =>
            ifc.struct.removedFields.foreach(f => removed.add(f.name))
            ifc.struct.fields.foreach(f => all += FlatField(f, cur, distance))
            directSupers.getOrElse(cur, Nil).foreach(s => frontier.enqueue(s -> (distance + 1)))
          case _ => ()
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
            hardConflicts += FieldConflict(name, fields.toList)
            diagBuf += Diagnostic.FieldNameConflict(ownerId, name, types, positionOf(rd, ownerId))
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
