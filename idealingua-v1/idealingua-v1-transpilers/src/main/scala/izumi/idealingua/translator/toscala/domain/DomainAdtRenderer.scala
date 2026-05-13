package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.toscala.products.CogenProduct.{AdtElementClassProduct, AdtElementProduct, AdtProduct}
import izumi.idealingua.translator.toscala.products.RenderableCogenProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Adt` as an `AdtProduct` (pre-extension).
  *
  * F-TextTree M5..M8f: ported off legacy quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary; M8f retires the explicit parse-back call
  * site (the carrier owns the String → Defn boundary now).
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

  def renderAdt(i: NewTypeDef.Adt): RenderableCogenProduct = {
    val adtId                              = i.id
    val typeName: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeName(adtId))
    val typeFull: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(adtId))

    val members: List[AdtElementClassProduct] = i.alternatives.map { m =>
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

      AdtElementProduct.fromTexts(
        name           = memberName,
        defnText       = branchElement.mapRender(resolver.resolve),
        companionText  = branchCompanion.mapRender(resolver.resolve),
        convertersText = List(
          intoConverter.mapRender(resolver.resolve),
          fromConverter.mapRender(resolver.resolve),
        ),
      )
    }

    // Empty body — legacy `q"…{}"` then `dialect.syntax` printer drops the
    // empty braces; the parse-back path preserves source-level `{}`, so we
    // omit them here for byte-equal output against the goldens.
    val traitTree: TextTree[ScalaRefHandle] =
      q"""sealed trait $typeName extends $adtElInit with $productRef"""

    val companionTree: TextTree[ScalaRefHandle] =
      q"""object $typeName extends $adtInit {
         |  import _root_.scala.language.implicitConversions
         |
         |  type Element = $typeFull
         |
         |}""".stripMargin

    AdtProduct.fromTexts(
      defnTraitText     = traitTree.mapRender(resolver.resolve),
      companionBaseText = companionTree.mapRender(resolver.resolve),
      elements          = members,
    )
  }
}
