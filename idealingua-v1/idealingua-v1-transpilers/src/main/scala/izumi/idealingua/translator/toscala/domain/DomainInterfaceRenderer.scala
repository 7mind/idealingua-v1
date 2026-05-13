package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.il.ast.typed.{Interfaces, TypeDef => LegacyTypeDef}
import izumi.idealingua.translator.toscala.domain.extensions.{
  DomainAnyvalExtension,
  DomainCastSimilarExtension,
  DomainCastUpExtension,
  DomainCirceDerivationTranslatorExtension,
}
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.tools.ScalaTextHelpers
import izumi.idealingua.translator.toscala.types.{ClassSource, ScalaStruct, ScalaType}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Interface` as the same Defns the
  * legacy `InterfaceRenderer.renderInterface` produces (modulo the
  * extension chain).
  *
  * F-TextTree M6..M8f: ported off legacy quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. Type references travel as
  * `ScalaRefHandle.{TypeName, TypeFull, TermFull}` value nodes.
  * Constructor parameters, constructor-call name lists, and the
  * extension-augmented impl Defn set are spliced as pre-rendered text.
  *
  * **Carrier strategy**: M8f flips `CogenProduct` to be constructed from
  * source-text via `CogenProduct.fromTraitTexts` — the carrier owns the
  * String → Defn boundary. `mkTrait` returns a rendered Scala-source
  * string; external call sites (`DomainCompositeRenderer.defns` mirror
  * interface synthesis) splice it as text into the surrounding companion
  * body.
  *
  * **Byte parity**: empty-body pitfall (M5) applies — the legacy
  * `q"trait X extends Y {}".syntax` printer drops empty `{}` but the
  * parser keeps source-level braces. We therefore emit traits with body
  * only when the body is non-empty, and tools implicit class without
  * `{}` for byte parity.
  *
  * IMPL-7a.2 Phase B parity: produces structurally correct Scala —
  * sealed trait extending parent interfaces plus `IDLGeneratedType`,
  * companion object with `apply(...)` factory + nested impl DTO (the
  * `Struct` synthetic), tools implicit class.
  */
final class DomainInterfaceRenderer(ctx: DomainSTContext) {

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  def renderInterface(i: NewTypeDef.Interface): RenderableCogenProduct = {
    val flat   = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      izumi.idealingua.typer.ir.FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val fields = DomainScalaStruct.scalaStruct(i.id, flat, i.struct.superclasses, ctx.conv, ctx.domain)
    val t      = ctx.conv.toScala(i.id)

    val qqInterfaceText: String = mkTrait(i.struct.superclasses.interfaces, t, fields)

    val implId       = DomainScalaStruct.implId(i.id)
    val implFlat     = DomainScalaStruct.implFlatStruct(implId, flat)
    val implFields   = DomainScalaStruct.scalaStruct(
      implId,
      implFlat,
      izumi.idealingua.model.il.ast.typed.Super.empty.copy(interfaces = List(i.id)),
      ctx.conv,
      ctx.domain,
    )
    val implStructure = new DomainCompositeStructure(ctx, implFields)

    // Construct a synthetic `TypeDef.Interface` legacy stub for the
    // ClassSource.CsInterface marker. CompositeRenderer reads only the type
    // (not the inner struct) at the pre-extension layer.
    val ifaceStub = LegacyTypeDef.Interface(
      id     = i.id,
      struct = izumi.idealingua.model.il.ast.typed.Structure(
        fields        = i.struct.fields,
        removedFields = i.struct.removedFields,
        superclasses  = i.struct.superclasses,
      ),
      meta   = i.meta,
    )

    val implRaw = ctx.compositeRenderer.defns(implStructure, ClassSource.CsInterface(ifaceStub))

    // The mirror Struct emitted INSIDE the interface companion is the
    // moral equivalent of the legacy "impl DTO routed through
    // CompositeRenderer + extension chain". Legacy emits both a Circe
    // trait (`StructCirce extends IRTTimeInstances`) and a
    // `Struct_upcast_<Self|Iface>` cast set on the inner companion.
    val scalaVersions = ctx.options.manifest.sbt.scalaVersions
    val structCirce = DomainCirceDerivationTranslatorExtension.emitForImplStruct(ctx, implId, implFlat, scalaVersions)
    // Legacy `defaultExtensions` order: CastSimilar before CastUp.
    val structSims    = DomainCastSimilarExtension.mkConvertersForImplStruct(ctx, implId)
    val structUpcasts = DomainCastUpExtension.generateUpcastsForImplStruct(ctx, i.id, implId, implFlat)

    // F-TextTree M8a: AnyVal-eligibility for the impl DTO surfaces as a
    // String slot entry. The init is the bare base name (`"AnyVal"`)
    // matching the legacy printer's `Init(Type.Name("AnyVal"),
    // Name.Anonymous(), Nil).syntax` output.
    val implAnyvalBases: List[String] = {
      val all = implFields.all.map(_.field.field)
      val canBeAnyVal = all.size == 1 && all.forall(f => structFieldQualifiesForAnyVal(f.typeId))
      if (canBeAnyVal) List("AnyVal")
      else List.empty
    }

    // F-TextTree M8a: the impl-struct extension chain feeds the
    // `CogenProduct` slot machinery. Carrier render order is
    // `[case class, more, siblings, companion]` — pushing the
    // StructCirce trait into `siblings` reproduces the legacy emit order
    // `[case class Struct, trait StructCirce, object Struct extends StructCirce]`.
    val implAugmentedDefns: List[String] = implRaw match {
      case cp: CogenProduct[?] =>
        val typedCp = cp.asInstanceOf[CogenProduct.CompositeProduct]
        val newProduct: CogenProduct.CompositeProduct = typedCp.copy(
          defnAnyvalBases     = implAnyvalBases,
          companionCirceBases = List(structCirce.initText),
          companionCasts      = structSims ++ structUpcasts,
          siblings            = List(structCirce.defnText),
        )
        newProduct.render.map(ScalaTextHelpers.renderTree(_))
      case other =>
        structCirce.defnText :: other.render.map(ScalaTextHelpers.renderTree(_))
    }

    // Companion: `def apply(..decls) = TermName(..names)` factory + impl
    // Defns. Splice decls / names / impl Defns as pre-rendered text.
    val applyDeclsText = implStructure.decls.mkString(", ")
    val applyNamesText = implStructure.names.mkString(", ")
    val implTermNameBare = ctx.conv.toScala(implId).termName.value
    val implAugmentedText = implAugmentedDefns.mkString("\n")

    // The companion's term-name is the interface's bare term name — emit as
    // the bare identifier (declaration site).
    val termNameBare = i.id.name

    val companionTree: TextTree[ScalaRefHandle] =
      q"""object $termNameBare {
         |  def apply($applyDeclsText) = $implTermNameBare($applyNamesText)
         |  ${implAugmentedText}
         |}""".stripMargin

    // Tools implicit class. Same pattern as the composite renderer: extends
    // `IRTConversions[T]` (single base) with no body — drop `{}` for parity.
    val toolsName    = s"${i.id.name}Extensions"
    val tFullTree: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(i.id))
    val toolsBaseText = ScalaTextHelpers.renderTree(ctx.rt.Conversions.parameterize(List(t.typeFull)).init())
    val toolsTree: TextTree[ScalaRefHandle] =
      q"""implicit class $toolsName(override protected val _value: $tFullTree) extends $toolsBaseText"""

    CogenProduct.fromTraitTexts(
      defnTraitText     = qqInterfaceText,
      companionBaseText = companionTree.mapRender(resolver.resolve),
      toolsText         = toolsTree.mapRender(resolver.resolve),
    )
  }

  /** AnyVal-field check mirroring `DomainAnyvalExtension.canBeAnyValField`,
    * inlined because that helper is package-private. */
  private def structFieldQualifiesForAnyVal(typeId: izumi.idealingua.model.common.TypeId): Boolean = typeId match {
    case _: izumi.idealingua.model.common.Generic                          => false
    case _: izumi.idealingua.model.common.Builtin                          => true
    case _: izumi.idealingua.model.common.TypeId.EnumId                    => true
    case _: izumi.idealingua.model.common.TypeId.AdtId                     => false
    case a: izumi.idealingua.model.common.TypeId.AliasId =>
      ctx.domain.aliases.get(a) match {
        case Some(target) => structFieldQualifiesForAnyVal(target)
        case None         => false
      }
    case d: izumi.idealingua.model.common.TypeId.DTOId =>
      ctx.domain.flattenedStructs.get(d).exists(_.fields.size > 1)
    case i: izumi.idealingua.model.common.TypeId.InterfaceId =>
      ctx.domain.flattenedStructs.get(i).exists(_.fields.size > 1)
    case t: izumi.idealingua.model.common.TypeId.IdentifierId =>
      ctx.domain.userTypes.get(t) match {
        case Some(izumi.idealingua.typer.ir.TypeDef.Identifier(_, fields, _)) => fields.size > 1
        case _                                                                 => false
      }
    case _ => false
  }

  /** Build the trait source string for an interface — exposed so the
    * composite renderer's mirror-interface synthesis can call it.
    *
    * F-TextTree M8f: returns rendered Scala source text. The legacy
    * `Defn.Trait` return type is gone — callers
    * (`DomainCompositeRenderer.defns` mirror synthesis,
    * `AnyvalExtension.handleTrait` removed) splice the text directly.
    * The Any base (AnyVal-eligible structs) is pre-spliced into the
    * header text here, so the boundary stays text.
    */
  def mkTrait(supers: Interfaces, t: ScalaType, fields: ScalaStruct): String = {
    // Field decls (legacy `Decl.Def(List.empty, Term.Name(n), List.empty, Type)`.syntax
    // emits `def n: T`; compose directly). The parse-back-then-print
    // pipeline preserves source layout when the input is already
    // canonical (no AST-node construction strips positional info), so we
    // emit the canonical Scala 3 trait body layout up front to avoid a
    // byte-parity drift. The canonical layout is single-line for
    // single-stat bodies and multi-line for multi-stat bodies.
    val declsList = fields.all.map(f => s"def ${f.nameSafe}: ${f.fieldType}")
    val singleLine = declsList.size == 1

    // Trait bases = `IDLGeneratedType` first, then each declared parent
    // interface's qualified shape.
    val ifDeclsText = (ctx.rt.generated +: supers.map(ctx.conv.toScala))
      .map(s => ScalaTextHelpers.renderTree(s.init()))
      .mkString(" with ")

    // The trait's bare declaration name. We use `t.typeName.value` directly
    // (a plain identifier; same as `ScalaRefHandle.TypeName(...)` would
    // resolve to).
    val typeNameBare = t.typeName.value

    // F-TextTree M8f: legacy parity — `AnyvalExtension.handleTrait` ran on
    // EVERY trait built through `mkTrait` (top-level interface + the mirror
    // synthesised inside DTO companions). Splice the Any base into the
    // header text here so the renderer stays text-only.
    val anyBasesText = DomainAnyvalExtension.withAnyForStruct(ctx, fields)
    val allBasesText = (anyBasesText :+ ifDeclsText).filter(_.nonEmpty).mkString(" with ")

    val traitTree: TextTree[ScalaRefHandle] = declsList match {
      case Nil =>
        q"""trait $typeNameBare extends $allBasesText"""
      case _ if singleLine =>
        // Canonical Scala 3 printer collapses a single-stat trait body
        // onto one line: `trait X extends Bs { def a: A }`.
        val onlyDecl = declsList.head
        q"""trait $typeNameBare extends $allBasesText { $onlyDecl }"""
      case _ =>
        val body = declsList.map(d => s"  $d").mkString("\n")
        q"""trait $typeNameBare extends $allBasesText {
           |$body
           |}""".stripMargin
    }

    traitTree.mapRender(resolver.resolve)
  }
}
