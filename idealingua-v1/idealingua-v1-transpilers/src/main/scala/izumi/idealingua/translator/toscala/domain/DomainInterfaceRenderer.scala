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
import izumi.idealingua.translator.toscala.products.CogenProduct.TraitProduct
import izumi.idealingua.translator.toscala.products.{CogenProduct, RenderableCogenProduct}
import izumi.idealingua.translator.toscala.tools.ScalaMetaTools._
import izumi.idealingua.translator.toscala.types.{ClassSource, ScalaStruct, ScalaType}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import scala.meta.{Decl, Defn, Init}

/** Renders a new-IR `TypeDef.Interface` as the same scala.meta `Defn`s the
  * legacy `InterfaceRenderer.renderInterface` produces (modulo the
  * extension chain).
  *
  * F-TextTree M6: ported off `scala.meta` quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. Type references travel as
  * `ScalaRefHandle.{TypeName, TypeFull, TermFull}` value nodes.
  * Constructor parameters (`struct.decls: List[Term.Param]`),
  * constructor-call name lists (`struct.names: List[Term.Name]`), and the
  * extension-augmented impl `Defn` set are spliced as pre-rendered
  * `.syntax` text — the scaffolding (`DomainCompositeStructure`) and
  * extensions (`Circe`, `AnyVal`, `Cast*`) still emit `scala.meta`
  * fragments at M6 (extension migration is M8 scope).
  *
  * **Carrier strategy** (same as M5): `CogenProduct[Defn.Trait]` carrier
  * preserved; `mkTrait` continues to return `Defn.Trait` so external
  * call sites (`DomainCompositeRenderer.defns` mirror-interface
  * synthesis, `AnyvalExtension.handleTrait` via
  * `TraitProduct(qqInterface).defn.prependBase`) consume it unchanged.
  * The renderer composes textual output via `TextTree`, lowers to
  * `String`, then re-parses via `DomainScalaParseBack`.
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

  import ctx.conv._

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  def renderInterface(i: NewTypeDef.Interface): RenderableCogenProduct = {
    val flat   = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      izumi.idealingua.typer.ir.FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val fields = DomainScalaStruct.scalaStruct(i.id, flat, i.struct.superclasses, ctx.conv, ctx.domain)
    val t      = ctx.conv.toScala(i.id)

    val qqInterface: Defn.Trait = mkTrait(i.struct.superclasses.interfaces, t, fields)

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
    val structCirceInit = ctx.conv.toScala(implId).sibling(structCirce.name).init()
    // Legacy `defaultExtensions` order: CastSimilar before CastUp.
    val structSims    = DomainCastSimilarExtension.mkConvertersForImplStruct(ctx, implId)
    val structUpcasts = DomainCastUpExtension.generateUpcastsForImplStruct(ctx, i.id, implId, implFlat)

    // AnyVal-eligibility for the impl DTO.
    val implAnyvalBases: List[Init] = {
      val all = implFields.all.map(_.field.field)
      val canBeAnyVal = all.size == 1 && all.forall(f => structFieldQualifiesForAnyVal(f.typeId))
      if (canBeAnyVal) List(ctx.conv.toScala(izumi.idealingua.model.JavaType(Seq.empty, "AnyVal")).init())
      else List.empty
    }

    // The impl render returns `CogenProduct[Defn.Class]`. Augment the
    // companion (`Defn.Object`) with the StructCirce init prepend + the
    // upcasts appended, and inject the StructCirce trait alongside the
    // emitted impl Defns inside the interface companion. Order INSIDE the
    // interface companion is `[case class Struct, StructCirce trait,
    // object Struct]` per legacy emit order.
    val implAugmentedDefns: List[Defn] = implRaw match {
      case cp: CogenProduct[_] =>
        val typedCp = cp.asInstanceOf[CogenProduct[Defn.Class]]
        val classWithAnyVal = typedCp.defn.prependBase(implAnyvalBases)
        val newCompanionBase = typedCp.companionBase.prependBase(structCirceInit).appendDefinitions(structSims ++ structUpcasts)
        val newProduct = CogenProduct[Defn.Class](
          defn          = classWithAnyVal,
          companionBase = newCompanionBase,
          tools         = typedCp.tools,
          more          = typedCp.more,
          preamble      = typedCp.preamble,
        )
        val rendered = newProduct.render
        // Splice the StructCirce trait between the case class and the
        // companion: legacy emit is `[case class Struct, trait StructCirce,
        // object Struct extends StructCirce]`.
        rendered match {
          case caseClass :: rest => caseClass :: structCirce.defn :: rest
          case Nil               => List(structCirce.defn)
        }
      case other =>
        structCirce.defn :: other.render
    }

    // Companion: `def apply(..decls) = TermName(..names)` factory + impl
    // Defns. Splice decls / names / impl Defns as pre-rendered text.
    val applyDeclsText = implStructure.decls.map(DomainScalaParseBack.renderS30(_)).mkString(", ")
    val applyNamesText = implStructure.names.map(DomainScalaParseBack.renderS30(_)).mkString(", ")
    val implTermNameBare = ctx.conv.toScala(implId).termName.value
    val implAugmentedText = implAugmentedDefns.map(DomainScalaParseBack.renderS30(_)).mkString("\n")

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
    val toolsBaseText = DomainScalaParseBack.renderS30(ctx.rt.Conversions.parameterize(List(t.typeFull)).init())
    val toolsTree: TextTree[ScalaRefHandle] =
      q"""implicit class $toolsName(override protected val _value: $tFullTree) extends $toolsBaseText"""

    val companionDefn = DomainScalaParseBack.parseObject(companionTree.mapRender(resolver.resolve))
    val toolsDefn     = DomainScalaParseBack.parseClass(toolsTree.mapRender(resolver.resolve))

    CogenProduct(qqInterface, companionDefn, toolsDefn, List.empty)
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

  /** Build the trait `Defn.Trait` for an interface — exposed so the
    * composite renderer's mirror-interface synthesis can call it.
    *
    * F-TextTree M6: interior composed as `TextTree[ScalaRefHandle]`,
    * lowered to text and parsed back via `DomainScalaParseBack.parseTrait`.
    * The return type stays `Defn.Trait` because callers
    * (`AnyvalExtension.handleTrait` via
    * `TraitProduct(...).defn.prependBase(...)` and downstream extension
    * call sites) consume the trait as a `Defn`.
    */
  def mkTrait(supers: Interfaces, t: ScalaType, fields: ScalaStruct): Defn.Trait = {
    // `Decl.Def(...)` is a `scala.meta` tree; splice as `.syntax` text.
    val declsText = fields.all.map { f =>
      DomainScalaParseBack.renderS30(Decl.Def(List.empty, f.name, List.empty, f.fieldType))
    }.mkString("; ")

    // Trait bases = `IDLGeneratedType` first, then each declared parent
    // interface's qualified shape. Splice via `.syntax`.
    val ifDecls: List[Init] = (ctx.rt.generated +: supers.map(ctx.conv.toScala)).map(_.init())
    val ifDeclsText = ifDecls.map(DomainScalaParseBack.renderS30(_)).mkString(" with ")

    // The trait's bare declaration name. We use `t.typeName.value` directly
    // (a plain identifier; same as `ScalaRefHandle.TypeName(...)` would
    // resolve to). The resolver is not consulted here because `t` is
    // already a fully constructed `ScalaType` whose bare name is known.
    val typeNameBare = t.typeName.value

    val traitTree: TextTree[ScalaRefHandle] =
      if (declsText.isEmpty)
        q"""trait $typeNameBare extends $ifDeclsText"""
      else
        q"""trait $typeNameBare extends $ifDeclsText { $declsText }"""

    val parsed = DomainScalaParseBack.parseTrait(traitTree.mapRender(resolver.resolve))

    // Legacy parity: AnyvalExtension.handleTrait runs on EVERY trait built
    // through mkTrait — top-level interfaces *and* the mirror `Defn` trait
    // synthesised inside DTO companions.
    TraitProduct(parsed).defn.prependBase(DomainAnyvalExtension.withAnyForStruct(ctx, fields))
  }
}
