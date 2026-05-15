package izumi.idealingua.translator.totypescript.domain.extensions

import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.totypescript.domain.TSRefHandle
import izumi.idealingua.translator.totypescript.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** PR-02 IMPL-7b Phase B M4: new-IR port of `EnumHelpersExtension`.
  *
  * F-TextTree M2 — ported to the typed-renderer protocol. The helper
  * body has no type references (operates on the enum's own value list as
  * plain strings), so the harvest contribution is empty and the
  * `TextTree[TSRefHandle]` is rendered via plain `.render` after the
  * compile-time `T =:= Nothing` evidence is loaded. The protocol
  * adoption is structural — keeps the extension family on the same
  * surface as renderers for the future option-B' layouter pass.
  */
object DomainTSEnumHelpersExtension {

  def handleEnum(enumeration: NewTypeDef.Enum, product: EnumProduct): EnumProduct = {
    val it = enumeration.members.map(_.value).iterator
    val values = it.map {
      m =>
        s"${enumeration.id.name}.$m" + (if (it.hasNext) "," else "")
    }.mkString("\n")

    val extension: TextTree[TSRefHandle] =
      q"""
         |export class ${enumeration.id.name}Helpers {
         |    public static readonly all = [
         |${values.shift(8)}
         |    ];
         |
         |    public static isValid(value: string): boolean {
         |        return ${enumeration.id.name}Helpers.all.indexOf(value as ${enumeration.id.name}) >= 0;
         |    }
         |}
       """.stripMargin

    EnumProduct(product.content + extension.mapRender(_ => ""), product.preamble)
  }
}
