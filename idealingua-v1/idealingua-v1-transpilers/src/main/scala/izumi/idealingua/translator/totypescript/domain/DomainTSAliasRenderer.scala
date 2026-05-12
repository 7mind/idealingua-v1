package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.totypescript.products.CogenProduct.AliasProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Alias` as the same `AliasProduct` the legacy
  * `TypeScriptTranslator.renderAlias` produces.
  *
  * IMPL-7b Phase B M1: TypeScript does not natively support type aliases
  * with constructor / casting semantics, so the legacy renderer emits a
  * fixed comment block referencing the alias's target type. The new
  * renderer reproduces this verbatim — the only variable parts of the
  * comment are `i.id.name` (the alias's local name) and the rendered
  * native type for `i.target` (used inside the comment body, not as an
  * actual TS `type` declaration).
  *
  * `Typespace` is threaded through `renderAlias` so the existing
  * `TypeScriptTypeConverter.toNativeType` signature can be reused without
  * modification. Under the new typer's full dealiasing pass, `i.target`
  * is never itself an `AliasId`, so the converter's `ts(al)` lookup paths
  * are not exercised — the `ts` parameter is only structurally required
  * by the converter's method signature.
  *
  * The legacy renderer also runs the extension chain
  * (`ctx.ext.extend(i, AliasProduct(...), _.handleAlias)`); the default
  * TS extension set (`EnumHelpersExtension`, `IntrospectionExtension`)
  * has no alias handler, so the extension chain is a no-op for aliases
  * and M1 omits it. M2+ will reintegrate the extension chain when it
  * matters for richer renderers.
  */
final class DomainTSAliasRenderer(ctx: DomainTSContext) {

  import ctx._

  def renderAlias(i: TypeDef.Alias, ts: Typespace): AliasProduct = {
    AliasProduct(
      s"""// TypeScript does not natively support well type aliases.
         |// Normally the code would be:
         |// export type ${i.id.name} = ${conv.toNativeType(i.target, ts)};
         |//
         |// However, constructors and casting won't work correctly.
         |// Therefore, all aliases usage was just replaced with the target
         |// type and this file is for reference purposes only.
         |// Should the new versions of TypeScript support this better -
         |// it can be enabled back.
         |//
         |// See this and other referenced threads for more information:
         |// https://github.com/Microsoft/TypeScript/issues/2552
          """.stripMargin
    )
  }
}
