package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.translator.totypescript.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Enum` as the same pre-extension `EnumProduct`
  * the legacy `TypeScriptTranslator.renderEnumeration` produces before the
  * extension chain runs.
  *
  * IMPL-7b Phase B M1 scope: enum body structure only. The legacy
  * `renderEnumeration` invokes `ctx.ext.extend(i, EnumProduct(...), _.handleEnum)`
  * after constructing the pre-extension product; the default TS extension
  * set includes `EnumHelpersExtension`, which is the only handler that
  * touches enums and which depends on legacy translator-internal state
  * (`TSTContext`, legacy `Enumeration`). M2+ will reintegrate that
  * extension chain. M1 byte-parity is asserted with an empty extension
  * list so the legacy and new paths produce the same pre-extension
  * output.
  *
  * `TypeDef.Enum` carries `id: EnumId`, `members: List[EnumMember]`,
  * `meta: NodeMeta` — `EnumMember` is the legacy `EnumMember` type
  * (see `idealingua-v1-model/.../typed/TypeDef.scala`), so the body
  * construction is field-for-field identical to legacy.
  */
final class DomainTSEnumRenderer(@annotation.unused ctx: DomainTSContext) {

  def renderEnumeration(i: TypeDef.Enum): EnumProduct = {
    val it = i.members.map(_.value).iterator
    val members = it.map {
      m =>
        s"$m = '$m'" + (if (it.hasNext) "," else "")
    }.mkString("\n")

    val content =
      s"""export enum ${i.id.name} {
         |${members.shift(4)}
         |}
       """.stripMargin

    EnumProduct(content, s"// ${i.id.name} Enumeration")
  }
}
