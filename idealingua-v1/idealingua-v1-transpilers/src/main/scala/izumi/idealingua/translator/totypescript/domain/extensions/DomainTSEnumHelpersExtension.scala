package izumi.idealingua.translator.totypescript.domain.extensions

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.translator.totypescript.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** PR-02 IMPL-7b Phase B M4: new-IR port of `EnumHelpersExtension`.
  *
  * Emits the `<EnumName>Helpers` companion class (with the static `all`
  * array and `isValid` predicate) appended to the `EnumProduct.content`
  * produced by `DomainTSEnumRenderer`. Wire-format-critical: the
  * `<EnumName>Helpers.all` literal feeds the introspection registration
  * emitted by `DomainTSIntrospectionExtension.handleEnum`.
  *
  * Reads only the new IR's `TypeDef.Enum` (no `Domain`, no `Typespace`).
  * Output is byte-equal to the legacy `EnumHelpersExtension.handleEnum`
  * for the same enum (verified by `DomainTSEnumHelpersExtensionSpec`).
  */
object DomainTSEnumHelpersExtension {

  def handleEnum(enumeration: NewTypeDef.Enum, product: EnumProduct): EnumProduct = {
    val it = enumeration.members.map(_.value).iterator
    val values = it.map {
      m =>
        s"${enumeration.id.name}.$m" + (if (it.hasNext) "," else "")
    }.mkString("\n")

    val extension =
      s"""
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

    EnumProduct(product.content + extension, product.preamble)
  }
}
