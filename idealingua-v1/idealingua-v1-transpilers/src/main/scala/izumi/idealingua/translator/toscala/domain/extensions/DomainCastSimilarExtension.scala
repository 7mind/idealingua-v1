package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.StructureId
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.translator.toscala.domain.{DomainSTContext, DomainScalaParseBack}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta.Term

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
  *
  * F-TextTree M8c: ported off `scala.meta` quasiquotes — the cast helpers
  * are now composed as plain Scala source strings. The previous
  * `q"implicit object …"` quasiquote → `renderS30(_)` round-trip is gone;
  * the produced strings are still consumed by `CogenProductSplice.applyCompanionStats`
  * which `parseStat`s them at carrier render time (parse-back boundary
  * unchanged).
  */
object DomainCastSimilarExtension {

  /** Companion-object stats for `cast_into_*` helpers on a DTO. */
  def mkConvertersForDto(ctx: DomainSTContext, dto: NewTypeDef.Dto): List[String] =
    mkConverters(ctx, dto.id)

  /** Companion-object stats for `cast_into_*` helpers on an Interface. */
  def mkConvertersForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[String] =
    mkConverters(ctx, i.id)

  /** Companion-object stats for `cast_into_*` helpers on the synthesized
    * impl-struct DTO of an interface (`<I>.Struct`). Mirrors legacy
    * `CastSimilarExtension.handleComposite` running on the impl emitted
    * by `CompositeRenderer.defns(_, CsInterface)`. */
  def mkConvertersForImplStruct(ctx: DomainSTContext, implId: izumi.idealingua.model.common.TypeId.DTOId): List[String] =
    mkConverters(ctx, implId)

  /** Companion-object stats for `cast_into_*` helpers on a service /
    * buzzer method Input or Output ephemeral DTO. Mirrors legacy
    * `CastSimilarExtension.handleComposite` running on the
    * `CompositeRenderer.defns(_, CsMethodInput | CsMethodOutput)` path. */
  def mkConvertersForMethodStruct(ctx: DomainSTContext, dtoId: izumi.idealingua.model.common.TypeId.DTOId): List[String] =
    mkConverters(ctx, dtoId)

  private def mkConverters(ctx: DomainSTContext, thisId: StructureId): List[String] = {
    sameSignature(ctx, thisId).map { same =>
      // Apply the full legacy sort key
      // `(distance, definedBy.toString, -definedWithIndex).reverse`
      // via `DomainScalaStruct.fromFlat` (which folds in defect #7 dedup).
      // Simple `sortBy(_.distance)` puts the closest field first, which
      // is the opposite of legacy `struct.all` (parents-first, closest-
      // last via `.reverse`).
      val supers = ctx.domain.userTypes.get(thisId) match {
        case Some(d: izumi.idealingua.typer.ir.TypeDef.Dto)       => d.struct.superclasses
        case Some(i: izumi.idealingua.typer.ir.TypeDef.Interface) => i.struct.superclasses
        case _                                                     => izumi.idealingua.model.il.ast.typed.Super.empty
      }
      val fields: List[Field] = ctx.domain.flattenedStructs.get(thisId) match {
        case Some(flat) =>
          izumi.idealingua.translator.toscala.domain.DomainScalaStruct
            .fromFlat(thisId, flat, supers, ctx.domain)
            .all
            .map(_.field)
        case None => List.empty
      }

      val thisScala   = ctx.conv.toScala(thisId)
      val targetScala = ctx.conv.toScala(same)
      val thisTypeFull   = thisScala.typeFull.toString
      val targetTypeFull = targetScala.typeFull.toString
      val targetTermFull = targetScala.termFull.toString

      val name = s"${thisScala.termName.value}_cast_into_${same.uniqueDomainName}"

      val castBase = ctx.rt.Cast.parameterize(List(thisScala.typeFull, targetScala.typeFull)).typeFull.toString

      val ctorArgs = fields.map { f =>
        val nm = DomainScalaParseBack.renderS30(Term.Name(f.name))
        s"$nm = _value.$nm"
      }.mkString(", ")

      s"""implicit object $name extends $castBase {
         |  override def convert(_value: $thisTypeFull): $targetTypeFull = {
         |    assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
         |    $targetTermFull($ctorArgs)
         |  }
         |}""".stripMargin
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
    *
    * IMPL-7a.2-Fi1: `Builtin.toString` returns `s"#$name"` only — generic
    * containers (`TList(Package)` and `TList(TString)`) collapse to `"#lst"`
    * which made fields whose types differ ONLY in their type argument
    * (`list[Package]` vs `list[str]`) compare equal in the legacy-style peer
    * scan. That produced cast-similar pairs like
    * `ReturnsListOutput → SimpleMethodWithGenericsOutput` (list[Package] vs
    * list[str]), whose bodies assign `_value.value: List[Package]` into a
    * field declared `List[String]` and fail to type-check. `keyFor` renders
    * generic args recursively so distinct element types stay distinct.
    */
  private def signatureOf(ctx: DomainSTContext, id: StructureId): List[(String, String)] = {
    ctx.domain.flattenedStructs.get(id) match {
      case Some(fs) =>
        fs.fields.map(ff => (ff.field.name, keyFor(ff.field.typeId))).sortBy(_._1)
      case None =>
        List.empty
    }
  }

  private def keyFor(t: izumi.idealingua.model.common.TypeId): String = t match {
    case g: izumi.idealingua.model.common.Generic => s"#${g.name}[${g.args.map(keyFor).mkString(",")}]"
    case _                                         => t.toString
  }

}
