package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.StructureId
import izumi.idealingua.model.common.TypeId.{DTOId, InterfaceId}
import izumi.idealingua.translator.toscala.domain.{DomainScalaStruct, DomainSTContext}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta.*

/** PR-02 IMPL-7a.2 Phase B M5: new-IR port of `CastUpExtension`.
  *
  * Emits implicit `Cast[ThisType, ParentType]` `object`s into the companion
  * for every structural parent of the structure (interface/DTO), **including
  * a reflexive self-cast `T_upcast_T`** (IMPL-7a.2-Fc, defect #3).
  *
  * Inlined port of `StructuralQueriesImpl.structuralParents` (legacy
  * `StructuralQueriesImpl.scala:106-113`):
  *
  *   1. Take `{id}` ∪ `Domain.parents.getOrElse(id, Set.empty)` — `id`
  *      itself plus every structural ancestor reachable from `id`.
  *   2. Keep only ancestors whose flat-struct field set is a subset of `id`'s
  *      flat-struct field set (`legacy: `_.all.map(_.field).diff(thisStructure.all.map(_.field)).isEmpty`).
  *      The self entry trivially passes.
  *
  * Self-emission rationale (defect #3): legacy emits `T_upcast_T` because
  * `allStructuralParents = List(interface.id) ++ ts.inheritance.allParents(interface.id)`
  * keeps `id` itself in the parent closure. The reflexive cast realises the
  * `T → T` `IRTCast` typeclass instance which downstream code uses uniformly
  * (mirror/non-mirror lookup pathways resolve the same way).
  *
  * Determinism: the result is sorted by `_.toString` (self placed FIRST to
  * match legacy emit order) so emitted converter order is stable across runs.
  */
object DomainCastUpExtension {

  def generateUpcastsForDto(ctx: DomainSTContext, dto: NewTypeDef.Dto): List[Stat] =
    generateUpcasts(ctx, dto.id)

  def generateUpcastsForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[Stat] =
    generateUpcasts(ctx, i.id)

  /** Defect #2-Fd (impl-struct `Struct_upcast_*` set): legacy
    * `CompositeRenderer.defns(_, CsInterface)` ran the cast extension on the
    * synthesized impl DTO, emitting `Struct_upcast_Struct` (self) and
    * `Struct_upcast_<Iface>` (the directly-implemented interface), plus any
    * further ancestor interface whose flat fields are a subset. New IR does
    * not register the impl ID in `flattenedStructs`, so the structural parent
    * set is computed locally: `{implId, ifaceId} ∪ {ancestor ifaces ⊆ implFlat}`.
    *
    * Body construction always targets the parent's impl id (`<Parent>.Struct`),
    * matching legacy.
    */
  def generateUpcastsForImplStruct(
    ctx: DomainSTContext,
    ifaceId: InterfaceId,
    implId: DTOId,
    implFlat: izumi.idealingua.typer.ir.FlatStruct,
  ): List[Stat] = {
    // implFlat == iface's flat (constructed in DomainScalaStruct.implFlatStruct).
    val implFieldNames: Set[String] = implFlat.fields.map(_.field.name).toSet

    // Parents = self + interface + transitive interface ancestors whose flat
    // field set is a subset of `implFieldNames`.
    val ifaceAncestors: List[InterfaceId] = {
      val visited = scala.collection.mutable.LinkedHashSet.empty[InterfaceId]
      val queue   = scala.collection.mutable.Queue.empty[InterfaceId]
      queue.enqueue(ifaceId)
      while (queue.nonEmpty) {
        val cur = queue.dequeue()
        if (visited.add(cur)) {
          ctx.domain.userTypes.get(cur) match {
            case Some(ifc: NewTypeDef.Interface) =>
              ifc.struct.superclasses.interfaces.foreach(p => queue.enqueue(p))
            case _ => ()
          }
        }
      }
      visited.toList
    }

    val qualifiedAncestors = ifaceAncestors.filter { p =>
      ctx.domain.flattenedStructs.get(p) match {
        case Some(pfs) => pfs.fields.map(_.field.name).toSet.subsetOf(implFieldNames)
        case None      => true // empty interface trivially admits
      }
    }

    // Order: self FIRST, then ancestors in BFS distance order (closest
    // parent next, deepest ancestor last). Legacy `structuralParents`
    // returns `id :: allParents(id)` in declaration/BFS order, NOT
    // alphabetical — sorting alphabetically inverted `Struct_upcast_*`
    // emission order for chains like
    // NotiWithFileRevision.Struct → {Struct, NotiWithFileRevision,
    // NotiWithFile, NotiBase}.
    val parents: List[StructureId] = implId :: qualifiedAncestors

    parents.map { parentId =>
      val parentImplId: StructureId = parentId match {
        case i: InterfaceId => DomainScalaStruct.implId(i)
        case d: DTOId       => d
      }

      val parentFlatNames: Set[String] = ctx.domain.flattenedStructs.get(parentId).map(_.fields.map(_.field.name).toSet).getOrElse(implFieldNames)
      val keep                          = parentFlatNames
      // Use `DomainScalaStruct.fromFlat` to apply the full legacy sort key
      // `(distance, definedBy.toString, -definedWithIndex)` so within-origin
      // declaration order survives the post-sort `.reverse`. The prior
      // two-key sort omitted `-definedWithIndex` and inverted the within-
      // origin order for self-upcast, producing
      // `Struct(y = …, x = …)` instead of `Struct(x = …, y = …)`.
      val supers = ctx.domain.userTypes.get(implId) match {
        case Some(d: NewTypeDef.Dto)       => d.struct.superclasses
        case Some(i: NewTypeDef.Interface) => i.struct.superclasses
        case _                              => izumi.idealingua.model.il.ast.typed.Super.empty
      }
      val sortedImplStruct = DomainScalaStruct.fromFlat(implId, implFlat, supers, ctx.domain)
      val constructorCode = sortedImplStruct.all
        .filter(f => keep.contains(f.field.name))
        .map { f =>
          q""" ${Term.Name(f.field.name)} = _value.${Term.Name(f.field.name)} """
        }

      val thisType       = ctx.conv.toScala(implId)
      val parentType     = ctx.conv.toScala(parentId)
      val parentImplType = ctx.conv.toScala(parentImplId)

      val name = Term.Name(s"${thisType.termName.value}_upcast_${parentType.termName.value}")

      q"""
         implicit object $name extends ${ctx.rt.Cast.parameterize(List(thisType.typeFull, parentType.typeFull)).init()} {
           override def convert(_value: ${thisType.typeFull}): ${parentType.typeFull} = {
             assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
             ${parentImplType.termFull}(..$constructorCode)
           }
         }
       """
    }
  }

  private def generateUpcasts(ctx: DomainSTContext, thisId: StructureId): List[Stat] = {
    structuralParents(ctx, thisId).map { parentId =>
      val parentImplId: StructureId = parentId match {
        case i: InterfaceId => DomainScalaStruct.implId(i)
        case d: DTOId       => d
      }

      val flat = ctx.domain.flattenedStructs.get(thisId)
      val parentFlat = ctx.domain.flattenedStructs.get(parentId).map(_.fields.map(_.field).toSet).getOrElse(Set.empty)

      // Defect #7 (IMPL-7a.2-Fa): when a child covariantly overrides a parent
      // field, `flat.fields` carries both occurrences (primary + ancestor).
      // The cast-up converter must emit only ONE assignment per name, otherwise
      // we produce `T(field = _value.field, field = _value.field)` (Scala
      // duplicate-named-argument error). Same dedup rule as
      // `DomainScalaStruct.fromFlat`: smallest-distance entry wins. Field
      // membership in `parentFlat` is set-based so the parent's field
      // declaration determines inclusion; the primary's `Field` value matches
      // when the field name is identical and the parent's type is a supertype
      // of the primary's type (covariant rule).
      // Legacy iterates `struct.all` *of the parent* (not the child) at the
      // cast-up site. Replicate by projecting `parentId`'s flat struct
      // through the legacy sort key (`DomainScalaStruct.fromFlat`) and
      // emitting in that order. Defect #2-Fd field-order subfix.
      //
      // For the reflexive `T_upcast_T` self-cast, `parentId == thisId`, so
      // the emit order equals the case-class declaration order (which is
      // already sorted via the legacy key for case-class params).
      val parentFlatFields = ctx.domain.flattenedStructs.get(parentId)
      val constructorCode = parentFlatFields match {
        case Some(pfs) =>
          val parentSuper = ctx.domain.userTypes.get(parentId) match {
            case Some(d: NewTypeDef.Dto)       => d.struct.superclasses
            case Some(i: NewTypeDef.Interface) => i.struct.superclasses
            case _                              => izumi.idealingua.model.il.ast.typed.Super.empty
          }
          val parentStruct = DomainScalaStruct.fromFlat(parentId, pfs, parentSuper, ctx.domain)
          parentStruct.all.map { f =>
            q""" ${Term.Name(f.field.name)} = _value.${Term.Name(f.field.name)} """
          }
        case None =>
          // No flat for the parent (interface impl id case) — fall back to
          // the child's flat fields filtered by the parent's name set.
          flat
            .map(_.fields.filter(ff => parentFlat.exists(_.name == ff.field.name)))
            .getOrElse(List.empty)
            .map { ff =>
              q""" ${Term.Name(ff.field.name)} = _value.${Term.Name(ff.field.name)} """
            }
      }

      val thisType       = ctx.conv.toScala(thisId)
      val parentType     = ctx.conv.toScala(parentId)
      val parentImplType = ctx.conv.toScala(parentImplId)

      val name = Term.Name(s"${thisType.termName.value}_upcast_${parentType.termName.value}")

      q"""
         implicit object $name extends ${ctx.rt.Cast.parameterize(List(thisType.typeFull, parentType.typeFull)).init()} {
           override def convert(_value: ${thisType.typeFull}): ${parentType.typeFull} = {
             assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
             ${parentImplType.termFull}(..$constructorCode)
           }
         }
       """
    }
  }

  private def structuralParents(ctx: DomainSTContext, thisId: StructureId): List[StructureId] = {
    val thisFlat = ctx.domain.flattenedStructs.get(thisId).map(_.fields.map(_.field).toSet).getOrElse(Set.empty)

    // Compute BFS-order over the structural supertype graph so the emit
    // order respects domain declaration order (legacy `allParents`
    // delegates to `safeParentsInherited` which prepends parents in
    // declared `struct.superclasses.interfaces` order).
    // Alphabetical sort (`sortBy(_.toString)`) was inverting pairs like
    // `Point → {Metadata, IntPair}` (declared `& Metadata + IntPair`)
    // to `IntPair < Metadata`.
    val bfsOrder: List[InterfaceId] = {
      val visited = scala.collection.mutable.LinkedHashSet.empty[InterfaceId]
      val queue   = scala.collection.mutable.Queue.empty[StructureId]
      queue.enqueue(thisId)
      while (queue.nonEmpty) {
        val cur = queue.dequeue()
        val rawSupers: List[izumi.idealingua.model.common.TypeId] = ctx.domain.userTypes.get(cur) match {
          case Some(d: NewTypeDef.Dto)       => d.struct.superclasses.interfaces ++ d.struct.superclasses.concepts
          case Some(i: NewTypeDef.Interface) => i.struct.superclasses.interfaces ++ i.struct.superclasses.concepts
          case _                              =>
            ctx.domain.members.get(cur) match {
              case Some(izumi.idealingua.typer.ir.Member.Ephemeral(eph)) =>
                eph.struct.superclasses.interfaces ++ eph.struct.superclasses.concepts
              case _ => Nil
            }
        }
        rawSupers.foreach {
          case iid: InterfaceId =>
            if (visited.add(iid)) queue.enqueue(iid)
          case sid: StructureId =>
            queue.enqueue(sid)
          case _ => ()
        }
      }
      visited.toList
    }

    // Defect #3 (IMPL-7a.2-Fc): include `thisId` in the cast-up parent
    // closure so a reflexive `T_upcast_T` instance is emitted. Legacy
    // `StructuralQueriesImpl.structuralParents` keeps `id` in
    // `allStructuralParents` — the filter `pFields.diff(thisFlat).isEmpty`
    // trivially admits self because `thisFlat.diff(thisFlat) == ∅`.
    val ancestors: List[StructureId] = bfsOrder
      .filter(p => p != thisId)
      .filter { p =>
        ctx.domain.flattenedStructs.get(p) match {
          case Some(pfs) =>
            val pFields = pfs.fields.map(_.field).toSet
            pFields.diff(thisFlat).isEmpty
          case None =>
            false
        }
      }
      .distinct

    // Self placed FIRST to match legacy emit order (legacy emits
    // `List(id) ++ allParents.sortBy(...)` — id is the head element).
    thisId :: ancestors
  }
}
