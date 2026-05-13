package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.toscala.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Enum` as an `EnumProduct` (pre-extension).
  *
  * F-TextTree M5: ported off `scala.meta` quasiquotes onto
  * `TextTree[ScalaRefHandle]` composition + `.mapRender(resolver.resolve)`
  * at the renderer boundary. The renderer interior carries zero
  * `scala.meta.Tree` material; type references travel as
  * `ScalaRefHandle.{TypeName, TypeFull}` value nodes.
  *
  * **Carrier strategy**: the legacy `EnumProduct` carries `Defn.Trait`,
  * `Defn.Object`, and `List[(Term.Name, Defn)]`. M5 keeps the carrier
  * intact (the carrier-migration cycle is deferred — extensions still
  * push `Defn` material into `companion.prependBase` and
  * `more :+ circe.defn`). The renderer composes textual output via
  * `TextTree`, lowers it to `String`, then re-parses the string back to
  * the expected `Defn` shape via `DomainScalaParseBack`.
  *
  * **Byte parity**: byte-equal goldens are preserved. Two parse-roundtrip
  * pitfalls drove the renderer's text shape:
  *   1. `q"trait F extends X {}".syntax` drops the empty braces; the
  *      parser preserves source-level `{}`. The renderer therefore
  *      emits `sealed trait $name extends $base` (no trailing braces).
  *   2. The companion body has stats — parser keeps the braces, which
  *      matches the legacy printer.
  * `verifyGoldens` is byte-equal across the corpus.
  */
final class DomainEnumRenderer(ctx: DomainSTContext) {

  import ctx._

  private val resolver    = new DomainScalaTextResolver(conv)
  private val enumElInit  = rt.enumEl.typeFull.toString
  private val idlEnumInit = rt.idlEnum.typeFull.toString

  def renderEnumeration(i: TypeDef.Enum): EnumProduct = {
    val typeName: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeName(i.id))
    val typeFull: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(i.id))

    // Each member contributes a case object placed into the companion. The
    // legacy renderer pairs the member's `Term.Name` with its `Defn` for
    // downstream `companion.appendDefinitions(elements.map(_._2))`. The
    // term-name is a plain `Term.Name(value)` literal (no qualified path).
    val members: List[(scala.meta.Term.Name, scala.meta.Defn)] = i.members.map { m =>
      val termText = m.value
      val element: TextTree[ScalaRefHandle] =
        q"""case object $termText extends $typeFull {
           |  override def toString: String = "${m.value}"
           |}""".stripMargin
      val parsed = DomainScalaParseBack.parseDefn(element.mapRender(resolver.resolve))
      scala.meta.Term.Name(termText) -> parsed
    }

    val memberRefs: Seq[TextTree[ScalaRefHandle]] =
      i.members.map(m => TextTree.text[ScalaRefHandle](m.value))

    val parseArms: Seq[TextTree[ScalaRefHandle]] =
      i.members.map(m => q"""case "${m.value}" => ${m.value}""")

    // Empty body — legacy `q"…{}"` then `dialect.syntax` printer drops the
    // empty braces; the parse-back path preserves source-level `{}`, so we
    // omit them here for byte-equal output against the goldens.
    val traitTree: TextTree[ScalaRefHandle] =
      q"""sealed trait $typeName extends $enumElInit"""

    val companionTree: TextTree[ScalaRefHandle] =
      q"""object $typeName extends $idlEnumInit {
         |  type Element = $typeFull
         |
         |  override def all: Seq[$typeFull] = Seq(${memberRefs.join(", ")})
         |
         |  override def parse(value: String): $typeName = value match {
         |    ${parseArms.joinN().shift(4).trim}
         |  }
         |}""".stripMargin

    val traitDefn     = DomainScalaParseBack.parseTrait(traitTree.mapRender(resolver.resolve))
    val companionDefn = DomainScalaParseBack.parseObject(companionTree.mapRender(resolver.resolve))

    EnumProduct(traitDefn, companionDefn, members)
  }
}
