package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.StructureId
import izumi.idealingua.model.common.TypeId.{DTOId, InterfaceId}
import izumi.idealingua.translator.toscala.domain.{DomainScalaStruct, DomainSTContext}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta.*

/** PR-02 IMPL-7a.2 Phase B M5: new-IR port of `CastUpExtension`.
  *
  * Emits implicit `Cast[ThisType, ParentType]` `object`s into the companion
  * for every structural parent of the structure (interface/DTO).
  *
  * Inlined port of `StructuralQueriesImpl.structuralParents` (legacy
  * `StructuralQueriesImpl.scala:106-113`):
  *
  *   1. Take `Domain.parents.getOrElse(id, Set.empty)` ∪ `{id}` — every
  *      structural ancestor reachable from `id` plus `id` itself.
  *   2. Keep only ancestors whose flat-struct field set is a subset of `id`'s
  *      flat-struct field set (`legacy: `_.all.map(_.field).diff(thisStructure.all.map(_.field)).isEmpty`).
  *   3. Drop `id` from the result (legacy keeps it but the emitted converter
  *      filters via `name`, here we drop earlier for clarity).
  *
  * Determinism: the result is sorted by `_.toString` so emitted converter
  * order is stable across runs.
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

      val constructorCode = flat
        .map(_.fields.map(_.field).filter(f => parentFlat.contains(f)))
        .getOrElse(List.empty)
        .map { f =>
          q""" ${Term.Name(f.name)} = _value.${Term.Name(f.name)} """
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

    rawParents.toList
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
  }
}
