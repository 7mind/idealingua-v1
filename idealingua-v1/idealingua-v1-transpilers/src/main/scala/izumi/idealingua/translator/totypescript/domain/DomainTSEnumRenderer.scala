package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.totypescript.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Enum` as the same pre-extension `EnumProduct`
  * the legacy `TypeScriptTranslator.renderEnumeration` produces before the
  * extension chain runs.
  *
  * F-TextTree M2 — ported to the typed-renderer protocol established by
  * M1.5: the body is composed as `TextTree[TSRefHandle]` and rendered via
  * `.mapRender(resolver.resolve)` at the product boundary. The enum body
  * itself contributes no type references (members are plain string
  * literals), so the harvest set is empty — the protocol adoption is
  * purely structural, keeping the renderer family uniform for the future
  * import-collection upstream pass (option B').
  */
final class DomainTSEnumRenderer(ctx: DomainTSContext) {

  private val resolver = new DomainTSTypeResolver(ctx.conv)

  def renderEnumeration(i: TypeDef.Enum): EnumProduct = {
    val members: TextTree[TSRefHandle] = {
      val it = i.members.map(_.value).iterator
      it.map {
        m =>
          val suffix = if (it.hasNext) "," else ""
          TextTree.text[TSRefHandle](s"$m = '$m'$suffix")
      }.toList.joinN()
    }

    val name = TextTree.text[TSRefHandle](i.id.name)

    val tree: TextTree[TSRefHandle] =
      q"""export enum $name {
         |${members.shift(4)}
         |}
       """.stripMargin

    EnumProduct(tree.mapRender(resolver.resolve), s"// ${i.id.name} Enumeration")
  }
}
