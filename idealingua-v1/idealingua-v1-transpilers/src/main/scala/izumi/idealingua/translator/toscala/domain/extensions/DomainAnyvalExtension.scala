package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.common.TypeId.{AdtId, AliasId, EnumId, IdentifierId}
import izumi.idealingua.model.common.{Builtin, Generic, StructureId, TypeId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.types.ScalaStruct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.annotation.{nowarn, tailrec}
import scala.collection.immutable.HashSet

/** PR-02 IMPL-7a.2 Phase B M5: new-IR port of `AnyvalExtension`.
  *
  * Determines whether the case class / trait emitted for a `TypeDef` should
  * extend `AnyVal` (or, for traits, `Any`). The decision mirrors the legacy
  * `AnyvalExtension`:
  *
  *   - A composite (DTO / interface impl) extends `AnyVal` iff its flat
  *     struct is exactly one scalar field whose type is a "scalar carrier"
  *     (primitive, enum, identifier wrapping >1 fields, etc.).
  *   - An identifier extends `AnyVal` iff its declared field set has size 1
  *     (legacy `struct.all.size == 1`).
  *   - A trait extends `Any` iff its flat struct is scalar-or-empty and
  *     every field qualifies.
  *
  * Reads only from `Domain.flattenedStructs` and `Domain.aliases` — no
  * `Typespace` lookup. Cross-domain alias chains are walked through
  * `Domain.aliases` (which already encodes the dealiased target per Phase 3
  * `AliasDealiaser`).
  *
  * The single result type is `List[Init]` to match the legacy
  * `prependBase(...)` API — the production swap (M6) will splice these into
  * the renderer outputs in the same way `ScalaMetaTools.prependBase` does
  * today on the legacy path.
  */
object DomainAnyvalExtension {

  /** AnyVal bases for a DTO or interface-impl composite.
    *
    * F-TextTree M8b..M8f: returns rendered Scala source text directly via
    * string composition. The String slot consumer (DomainScalaTranslator)
    * pushes these into the `defnAnyvalBases` slot on `CogenProduct`. */
  def withAnyvalForComposite(ctx: DomainSTContext, dto: NewTypeDef.Dto): List[String] =
    doModify("AnyVal", structCanBeAnyVal(ctx, dto))

  /** AnyVal bases for a service / buzzer method Input or Output ephemeral
    * DTO. Single-scalar inputs and Singular-output wrappers qualify.
    *
    * F-TextTree M8b: returns rendered Scala source text. */
  def withAnyvalForMethodStruct(ctx: DomainSTContext, flat: izumi.idealingua.typer.ir.FlatStruct): List[String] = {
    val all = dedupByName(flat.fields).map(_.field)
    val ok  = all.size == 1 && all.forall(f => canBeAnyValField(ctx, f.typeId))
    doModify("AnyVal", ok)
  }

  /** Any bases for a structural interface (trait). F-TextTree M8f: returns
    * rendered Scala source text — the `InterfaceRenderer.mkTrait` consumer
    * splices it into the trait header as text. */
  def withAnyForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[String] = {
    val flat = ctx.domain.flattenedStructs.get(i.id)
    val canBeAny = flat match {
      case Some(fs) =>
        val all          = dedupByName(fs.fields).map(_.field)
        val scalarOrEmpty = all.size <= 1
        scalarOrEmpty && all.forall(f => canBeAnyValField(ctx, f.typeId))
      case None => false
    }
    doModify("Any", canBeAny)
  }

  /** Any bases for an arbitrary trait built from a `ScalaStruct`.
    *
    * Legacy parity: `AnyvalExtension.handleTrait` runs on every trait built
    * via `InterfaceRenderer.mkTrait`, including the mirror trait
    * synthesised for DTO companions inside `CompositeRenderer.defns`. The
    * predicate matches the legacy `withAny(Struct)` arm — single-or-empty
    * scalar carrier whose every field qualifies as an `AnyVal`-eligible
    * type.
    *
    * F-TextTree M8f: returns rendered Scala source text — the caller
    * splices it into the trait header as text. The DTO carrier path
    * uses `withAnyvalForComposite` (String slot) instead.
    */
  def withAnyForStruct(ctx: DomainSTContext, struct: ScalaStruct): List[String] = {
    val _             = ctx
    val all           = struct.all.map(_.field.field)
    val scalarOrEmpty = all.size <= 1
    val canBeAny      = scalarOrEmpty && all.forall(f => canBeAnyValField(ctx, f.typeId))
    doModify("Any", canBeAny)
  }

  /** AnyVal bases for an Identifier.
    *
    * F-TextTree M8b..M8f: returns rendered Scala source text. After the
    * Init round-trip went away, `ctx` is no longer consulted — the
    * predicate is purely `id.fields.size == 1`. Parameter kept to
    * preserve the call-site signature (parity with the other
    * `withAnyvalFor*` arms which still need `ctx` for `flattenedStructs`
    * / `aliases` lookups). */
  @nowarn("msg=parameter ctx")
  def withAnyvalForIdentifier(ctx: DomainSTContext, id: NewTypeDef.Identifier): List[String] =
    doModify("AnyVal", id.fields.size == 1)

  /** Public predicate reused by Circe (Scala 3 forProduct1 path). */
  def structCanBeAnyVal(ctx: DomainSTContext, dto: NewTypeDef.Dto): Boolean = {
    val fs = ctx.domain.flattenedStructs.get(dto.id)
    fs.exists { struct =>
      val all = dedupByName(struct.fields).map(_.field)
      all.size == 1 && all.forall(f => canBeAnyValField(ctx, f.typeId))
    }
  }

  /** Deduplicate a `FlatStruct.fields` list by field name. The BFS-flattener
    * preserves every occurrence (parent + child re-declaration with the same
    * type); the legacy `Struct.all` and the emitted case-class parameter list
    * deduplicate them. AnyVal predicates and forProduct1 codec paths must
    * count the deduped set, not the raw flat. */
  private[domain] def dedupByName(
    fields: List[izumi.idealingua.typer.ir.FlatField]
  ): List[izumi.idealingua.typer.ir.FlatField] =
    fields.groupBy(_.field.name).values.map(_.head).toList

  /** F-TextTree M8b..M8f: direct string composition for every base-slot arm
    * (DTO/Identifier/Interface AnyVal + trait Any). The bare base name
    * (`"AnyVal"`, `"Any"`) is the exact text the legacy printer emitted
    * when given `Init(Type.Name(base), Name.Anonymous(), Nil)`. */
  private def doModify(base: String, modify: Boolean): List[String] = {
    if (modify) List(base)
    else List.empty
  }

  private[domain] def canBeAnyValField(ctx: DomainSTContext, typeId: TypeId): Boolean =
    canBeAnyValField(ctx, typeId, HashSet.empty)

  // After F16/Option A1 widening, Service/Buzzer/Streams IDs extend `TypeId` —
  // but they cannot appear in struct fields, so the existing branches stay
  // semantically exhaustive (only the compiler's checker needs a hint).
  @nowarn("msg=match may not be exhaustive")
  @tailrec
  private[domain] def canBeAnyValField(ctx: DomainSTContext, typeId: TypeId, seen: HashSet[TypeId]): Boolean = {
    typeId match {
      case _: Generic =>
        false // https://github.com/scala/bug/issues/11170
      case _: Builtin =>
        true
      case _: EnumId =>
        true
      case _: AdtId =>
        false
      case a: AliasId =>
        ctx.domain.aliases.get(a) match {
          case Some(target) =>
            canBeAnyValField(ctx, target, seen + target)
          case None =>
            throw new IDLException(s"DomainAnyvalExtension: unresolved alias $a")
        }
      case s: StructureId =>
        // Structural composite (DTO or Interface): `findFlatStruct` checks
        // local `flattenedStructs` then `crossDomainFlattenedStructs`; no
        // manual OR needed at the call site.
        ctx.domain.findFlatStruct(s).exists(_.fields.size > 1)
      case t: IdentifierId =>
        // `findUserType` checks local `userTypes` then `crossDomainUserTypes`;
        // foreign identifiers referenced from local field positions are reached
        // without a manual fallback at the call site.
        ctx.domain.findUserType(t) match {
          case Some(NewTypeDef.Identifier(_, fields, _)) => fields.size > 1
          case _ => false
        }
    }
  }
}
