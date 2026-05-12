package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.StructureId
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta.*

/** PR-02 IMPL-7a.2 Phase B M5: new-IR port of `CastSimilarExtension`.
  *
  * Emits implicit `Cast[ThisType, TargetType]` `object`s into the companion
  * for every structurally-identical sibling DTO. The "sibling" set is the
  * inlined port of `StructuralQueriesImpl.sameSignature` (legacy
  * `StructuralQueriesImpl.scala:115-126`), reading off `Domain` rather
  * than `Typespace`:
  *
  *   1. Compute this struct's signature: sorted-by-name list of (name, typeId)
  *      pairs over the flat-struct fields.
  *   2. Walk every `StructureId` in `Domain.flattenedStructs` whose signature
  *      matches and whose owner is a `DTO` (collect `case t: DTO`).
  *   3. Exclude `tid` itself and any structure that has `tid` among its
  *      parents (`Domain.parents`) — same predicate as legacy
  *      `parentsInherited(id.id).contains(tid)`.
  *
  * Determinism: the result list is sorted by `targetId.toString` so the
  * emitted `implicit object` order is stable across runs. Without this sort
  * the iteration order of `Domain.flattenedStructs` (a `Map`) is unspecified.
  */
object DomainCastSimilarExtension {

  /** Companion-object stats for `cast_into_*` helpers on a DTO. */
  def mkConvertersForDto(ctx: DomainSTContext, dto: NewTypeDef.Dto): List[Stat] =
    mkConverters(ctx, dto.id)

  /** Companion-object stats for `cast_into_*` helpers on an Interface. */
  def mkConvertersForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[Stat] =
    mkConverters(ctx, i.id)

  /** Companion-object stats for `cast_into_*` helpers on the synthesized
    * impl-struct DTO of an interface (`<I>.Struct`). Mirrors legacy
    * `CastSimilarExtension.handleComposite` running on the impl emitted
    * by `CompositeRenderer.defns(_, CsInterface)`. */
  def mkConvertersForImplStruct(ctx: DomainSTContext, implId: izumi.idealingua.model.common.TypeId.DTOId): List[Stat] =
    mkConverters(ctx, implId)

  private def mkConverters(ctx: DomainSTContext, thisId: StructureId): List[Stat] = {
    sameSignature(ctx, thisId).map { same =>
      val flat = ctx.domain.flattenedStructs.get(thisId)
      // Defect #7 (IMPL-7a.2-Fa): dedup covariant duplicates by name; keep
      // smallest-distance entry. See `DomainScalaStruct.fromFlat` for the
      // canonical dedup; this is the same rule applied to the cast-into
      // converter so it cannot emit `Target(name = ..., name = ...)`.
      val seen: scala.collection.mutable.LinkedHashSet[String] = scala.collection.mutable.LinkedHashSet.empty
      val fields: List[Field] = flat
        .map(_.fields.sortBy(_.distance).filter(ff => seen.add(ff.field.name)).map(_.field))
        .getOrElse(List.empty)

      val code = fields.map { f =>
        q""" ${Term.Name(f.name)} = _value.${Term.Name(f.name)} """
      }

      val thisType   = ctx.conv.toScala(thisId)
      val targetType = ctx.conv.toScala(same)

      val name = Term.Name(s"${thisType.termName.value}_cast_into_${same.uniqueDomainName}")

      q"""
         implicit object $name extends ${ctx.rt.Cast.parameterize(List(thisType.typeFull, targetType.typeFull)).init()} {
           override def convert(_value: ${thisType.typeFull}): ${targetType.typeFull} = {
              assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
              ${targetType.termFull}(..$code)
           }
         }
       """
    }
  }

  /** Inlined port of `StructuralQueriesImpl.sameSignature`. Returns a
    * deterministically-ordered list of DTO ids whose flat signature matches
    * `tid` and that are not parents of `tid`.
    */
  private def sameSignature(ctx: DomainSTContext, tid: StructureId): List[DTOId] = {
    // Legacy `StructuralQueriesImpl.sameSignature` does NOT early-return on
    // empty signatures — an empty-fielded mixin (`mixin Empty {}`,
    // `mixin Covariant {}`) still produces a non-trivial peer set because
    // every other empty-fielded DTO in the domain matches. Removing the
    // early-return aligns with legacy and surfaces `Struct_cast_into_*`
    // entries for `<EmptyMixin>.Struct → <other empty DTO>` pairs.
    val sig = signatureOf(ctx, tid)

    val parents: Set[StructureId] = ctx.domain.parents.getOrElse(tid, Set.empty).asInstanceOf[Set[StructureId]]

    val candidates = ctx.domain.flattenedStructs.toList.collect {
      case (id, _) if id != tid =>
        id
    }

    candidates
      .filter(id => signatureOf(ctx, id) == sig)
      .collect { case d: DTOId => d }
      .filterNot(d => parents.contains(d))
      .filterNot { d =>
        // exclude when this type appears in d's parent chain
        ctx.domain.parents.getOrElse(d, Set.empty).asInstanceOf[Set[StructureId]].contains(tid)
      }
      .filterNot { d =>
        // Also exclude where tid is in d's parents (mirror of
        // `parentsInherited(id.id).contains(tid)` from legacy).
        ctx.domain.parents.getOrElse(d, Set.empty).asInstanceOf[Set[StructureId]].contains(tid)
      }
      .distinct
      .sortBy(_.toString)
  }

  /** Sorted-by-name (name, typeId) projection of a flat struct — the
    * `signature` legacy method without the `Field` data shape (we work
    * directly with stable scalars to avoid pulling extra equality contracts).
    */
  private def signatureOf(ctx: DomainSTContext, id: StructureId): List[(String, String)] = {
    ctx.domain.flattenedStructs.get(id) match {
      case Some(fs) =>
        fs.fields.map(ff => (ff.field.name, ff.field.typeId.toString)).sortBy(_._1)
      case None =>
        List.empty
    }
  }

}
