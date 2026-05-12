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
      val dedupedNames = scala.collection.mutable.LinkedHashSet.empty[String]
      val constructorCode = flat
        .map(_.fields.filter(ff => parentFlat.exists(_.name == ff.field.name)))
        .getOrElse(List.empty)
        .sortBy(_.distance)
        .filter(ff => dedupedNames.add(ff.field.name))
        .map { ff =>
          q""" ${Term.Name(ff.field.name)} = _value.${Term.Name(ff.field.name)} """
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

    val rawParents: Set[StructureId] = ctx.domain.parents.getOrElse(thisId, Set.empty).map(_.asInstanceOf[StructureId])

    // Defect #3 (IMPL-7a.2-Fc): include `thisId` in the cast-up parent
    // closure so a reflexive `T_upcast_T` instance is emitted. Legacy
    // `StructuralQueriesImpl.structuralParents` keeps `id` in
    // `allStructuralParents` — the filter `pFields.diff(thisFlat).isEmpty`
    // trivially admits self because `thisFlat.diff(thisFlat) == ∅`.
    val ancestors = rawParents.toList
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
      .sortBy(_.toString)

    // Self placed FIRST to match legacy emit order (legacy emits
    // `List(id) ++ allParents.sortBy(...)` — id is the head element).
    thisId :: ancestors
  }
}
