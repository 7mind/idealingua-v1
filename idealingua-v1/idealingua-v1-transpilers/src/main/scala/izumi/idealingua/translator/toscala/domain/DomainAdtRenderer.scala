package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.toscala.products.CogenProduct.{AdtElementProduct, AdtProduct}
import izumi.idealingua.translator.toscala.products.RenderableCogenProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Adt` as an `AdtProduct` (pre-extension).
  *
  * F-TextTree M5: ported off `scala.meta` quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. The renderer interior carries zero
  * `scala.meta.Tree` material; type references travel as
  * `ScalaRefHandle.{TypeFull, TypeAbsolute, TypeFullWithin, TermFullWithin}`
  * value nodes.
  *
  * **Carrier strategy** (same as `DomainEnumRenderer`): the legacy
  * `AdtProduct` / `AdtElementProduct` carriers hold `Defn` material; M5
  * keeps the carrier intact so extensions (Circe sibling + base, AnyVal,
  * Cast*) continue to push `Defn` into the product unchanged. The
  * renderer composes textual output via `TextTree`, lowers to `String`,
  * then re-parses to `Defn.Trait` / `Defn.Object` / `Defn.Class` /
  * `Defn.Def` via `DomainScalaParseBack` at the boundary.
  *
  * **Byte parity**: the legacy printer drops empty `{}` from the trait
  * declaration; the parser preserves source-level `{}`. The renderer
  * therefore emits `sealed trait $name extends $bases` (no trailing
  * braces) for byte-equal output. The companion body has stats so the
  * braces are preserved by both paths. The `scala.Product` mixin uses
  * the minimized `typeFull` form (e.g. `scala.Product`, not
  * `_root_.scala.Product`) to match the legacy `toScala[Product].init()`
  * shape. `verifyGoldens` is byte-equal across the corpus.
  *
  * Per F5c T1 the new-typer `EphemeralSynthesizer` auto-wraps non-structural
  * alternative-branch targets in a synthetic DTO, so every
  * `AdtMember.typeId` reaching this renderer is already a `StructureId`
  * (DTO / Interface / Identifier) or another `AdtId`. No additional
  * lifting is required here.
  */
final class DomainAdtRenderer(ctx: DomainSTContext) {

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  private val adtElInit  = ctx.rt.adtEl.typeFull.toString
  private val adtInit    = ctx.rt.adt.typeFull.toString
  // Legacy emits `extends ... with scala.Product` via
  // `ctx.conv.toScala[Product].init()` which renders the minimized
  // `typeFull` form of `scala.Product`. Pre-compute the same here.
  private val productRef = ctx.conv.toScala(classOf[Product]).typeFull.toString

  def renderAdt(i: NewTypeDef.Adt, bases: List[scala.meta.Init] = List.empty): RenderableCogenProduct = {
    val adtId                              = i.id
    val typeName: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeName(adtId))
    val typeFull: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(adtId))

    val members: List[AdtElementProduct[scala.meta.Defn.Class]] = i.alternatives.map { m =>
      val memberName    = m.typename
      val targetAbsolute: TextTree[ScalaRefHandle] =
        TextTree.value(ScalaRefHandle.TypeAbsolute(m.typeId))
      val branchFull: TextTree[ScalaRefHandle] =
        TextTree.value(ScalaRefHandle.TypeFullWithin(adtId, memberName))
      val branchTermFull: TextTree[ScalaRefHandle] =
        TextTree.value(ScalaRefHandle.TermFullWithin(adtId, memberName))

      val branchElement: TextTree[ScalaRefHandle] =
        q"""final case class $memberName(value: $targetAbsolute) extends $typeFull"""

      val branchCompanion: TextTree[ScalaRefHandle] =
        q"""object $memberName {}"""

      val intoConverter: TextTree[ScalaRefHandle] =
        q"""implicit def ${"into" + memberName}(value: $targetAbsolute): $typeFull = $branchTermFull(value)"""

      val fromConverter: TextTree[ScalaRefHandle] =
        q"""implicit def ${"from" + memberName}(value: $branchFull): $targetAbsolute = value.value"""

      val elementDefn   = DomainScalaParseBack.parseClass(branchElement.mapRender(resolver.resolve))
      val companionDefn = DomainScalaParseBack.parseObject(branchCompanion.mapRender(resolver.resolve))
      val convs         = List(
        DomainScalaParseBack.parseDef(intoConverter.mapRender(resolver.resolve)),
        DomainScalaParseBack.parseDef(fromConverter.mapRender(resolver.resolve)),
      )

      AdtElementProduct(memberName, elementDefn, companionDefn, convs)
    }

    val basesText: Seq[String] = bases.map(_.toString)
    val basesInline =
      if (basesText.isEmpty) "" else basesText.mkString(" with ", " with ", "")

    // Empty body — legacy `q"…{}"` then `dialect.syntax` printer drops the
    // empty braces; the parse-back path preserves source-level `{}`, so we
    // omit them here for byte-equal output against the goldens.
    val traitTree: TextTree[ScalaRefHandle] =
      q"""sealed trait $typeName extends $adtElInit$basesInline with $productRef"""

    val companionTree: TextTree[ScalaRefHandle] =
      q"""object $typeName extends $adtInit {
         |  import _root_.scala.language.implicitConversions
         |
         |  type Element = $typeFull
         |
         |}""".stripMargin

    val traitDefn     = DomainScalaParseBack.parseTrait(traitTree.mapRender(resolver.resolve))
    val companionDefn = DomainScalaParseBack.parseObject(companionTree.mapRender(resolver.resolve))

    AdtProduct(traitDefn, companionDefn, members)
  }
}
