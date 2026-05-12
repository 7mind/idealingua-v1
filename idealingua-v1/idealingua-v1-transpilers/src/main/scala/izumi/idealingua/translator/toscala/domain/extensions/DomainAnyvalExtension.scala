package izumi.idealingua.translator.toscala.domain.extensions

import izumi.idealingua.model.JavaType
import izumi.idealingua.model.common.TypeId.{AdtId, AliasId, EnumId, IdentifierId, InterfaceId, DTOId}
import izumi.idealingua.model.common.{Builtin, Generic, TypeId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.types.ScalaStruct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.annotation.{nowarn, tailrec}
import scala.collection.immutable.HashSet
import scala.meta.*

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

  /** AnyVal bases for a DTO or interface-impl composite. */
  def withAnyvalForComposite(ctx: DomainSTContext, dto: NewTypeDef.Dto): List[Init] =
    doModify(ctx, "AnyVal", structCanBeAnyVal(ctx, dto))

  /** Any bases for a structural interface (trait). */
  def withAnyForInterface(ctx: DomainSTContext, i: NewTypeDef.Interface): List[Init] = {
    val flat = ctx.domain.flattenedStructs.get(i.id)
    val canBeAny = flat match {
      case Some(fs) =>
        val all          = fs.fields.map(_.field)
        val scalarOrEmpty = all.size <= 1
        scalarOrEmpty && all.forall(f => canBeAnyValField(ctx, f.typeId))
      case None => false
    }
    doModify(ctx, "Any", canBeAny)
  }

  /** Any bases for an arbitrary trait built from a `ScalaStruct`.
    *
    * Legacy parity: `AnyvalExtension.handleTrait` runs on every trait built
    * via `InterfaceRenderer.mkTrait`, including the mirror `Defn` trait
    * synthesised for DTO companions inside `CompositeRenderer.defns`. The
    * predicate matches the legacy `withAny(Struct)` arm — single-or-empty
    * scalar carrier whose every field qualifies as an `AnyVal`-eligible
    * type.
    */
  def withAnyForStruct(ctx: DomainSTContext, struct: ScalaStruct): List[Init] = {
    val all           = struct.all.map(_.field.field)
    val scalarOrEmpty = all.size <= 1
    val canBeAny      = scalarOrEmpty && all.forall(f => canBeAnyValField(ctx, f.typeId))
    doModify(ctx, "Any", canBeAny)
  }

  /** AnyVal bases for an Identifier. */
  def withAnyvalForIdentifier(ctx: DomainSTContext, id: NewTypeDef.Identifier): List[Init] =
    doModify(ctx, "AnyVal", id.fields.size == 1)

  /** Public predicate reused by Circe (Scala 3 forProduct1 path). */
  def structCanBeAnyVal(ctx: DomainSTContext, dto: NewTypeDef.Dto): Boolean = {
    val fs = ctx.domain.flattenedStructs.get(dto.id)
    fs.exists { struct =>
      val all = struct.fields.map(_.field)
      all.size == 1 && all.forall(f => canBeAnyValField(ctx, f.typeId))
    }
  }

  private def doModify(ctx: DomainSTContext, base: String, modify: Boolean): List[Init] = {
    if (modify) List(ctx.conv.toScala(JavaType(Seq.empty, base)).init())
    else List.empty
  }

  private def canBeAnyValField(ctx: DomainSTContext, typeId: TypeId): Boolean =
    canBeAnyValField(ctx, typeId, HashSet.empty)

  // After F16/Option A1 widening, Service/Buzzer/Streams IDs extend `TypeId` —
  // but they cannot appear in struct fields, so the existing branches stay
  // semantically exhaustive (only the compiler's checker needs a hint).
  @nowarn("msg=match may not be exhaustive")
  @tailrec
  private def canBeAnyValField(ctx: DomainSTContext, typeId: TypeId, seen: HashSet[TypeId]): Boolean = {
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
      case d: DTOId =>
        // legacy: "struct.isComposite" — composites cannot be AnyVal-wrapped.
        // For an interface-impl DTO mirror this is always true.
        val flat = ctx.domain.flattenedStructs.get(d)
        flat.exists(_.fields.size > 1)
      case i: InterfaceId =>
        val flat = ctx.domain.flattenedStructs.get(i)
        flat.exists(_.fields.size > 1)
      case t: IdentifierId =>
        // legacy: `struct.all.size > 1`. New IR exposes Identifier.fields directly
        // via `userTypes`; lookup, fall back to `false` when absent.
        ctx.domain.userTypes.get(t) match {
          case Some(NewTypeDef.Identifier(_, fields, _)) => fields.size > 1
          case _                                          => false
        }
    }
  }
}
