package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.totypescript.products.CogenProduct.AliasProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Alias` as the same `AliasProduct` the legacy
  * `TypeScriptTranslator.renderAlias` produces.
  *
  * F-TextTree M1: this renderer is the proof-of-pattern for adopting
  * `izumi.fundamentals.platform.strings.TextTree` across the translator
  * tree. The internal `TextTree[Nothing]` is rendered to a `String` at
  * the `AliasProduct.content` boundary — that is, renderer protocol
  * **option (a)**: `Module(path, content: String)` shape is unchanged;
  * renderers internally compose `TextTree` and call `.render` at the
  * product boundary. The alternative — option (b), threading `TextTree`
  * all the way to the layouter — was rejected for M1 because it would
  * cascade through every product type in
  * `toscala/products/`, `totypescript/products/`, `tocsharp/products/`
  * and is out of scope for a single-renderer foundation cycle. M2+ may
  * re-evaluate option (b) once enough renderers are ported that the
  * String-at-boundary conversion becomes the bottleneck.
  *
  * The rest of the `toscala/`, `totypescript/`, `tocsharp/` renderers
  * stay on the legacy raw-string-template approach until subsequent
  * F-TextTree milestones (M2..MN). Roughly ~3000 LOC of follow-on work
  * across the three translator trees + extensions.
  *
  * IMPL-7b Phase B M1 (semantic notes preserved from the original port):
  * TypeScript does not natively support type aliases with constructor /
  * casting semantics, so the legacy renderer emits a fixed comment block
  * referencing the alias's target type. The new renderer reproduces this
  * verbatim — the only variable parts of the comment are `i.id.name`
  * (the alias's local name) and the rendered native type for `i.target`
  * (used inside the comment body, not as an actual TS `type`
  * declaration).
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
  *
  * Byte-equality with the legacy `stripMargin`-based template is
  * preserved by appending the trailing 10-space pad that the legacy
  * template carried as content after its final `|`-prefixed line (the
  * closing `"""` was column-11-aligned in the source). See
  * `harness/verifyGoldens` for the byte-level contract.
  */
final class DomainTSAliasRenderer(ctx: DomainTSContext) {

  import ctx._

  def renderAlias(i: TypeDef.Alias): AliasProduct = {
    val aliasName: TextTree[Nothing]  = TextTree.text(i.id.name)
    val nativeType: TextTree[Nothing] = TextTree.text(conv.toNativeType(i.target))

    val tree: TextTree[Nothing] =
      q"""// TypeScript does not natively support well type aliases.
         |// Normally the code would be:
         |// export type $aliasName = $nativeType;
         |//
         |// However, constructors and casting won't work correctly.
         |// Therefore, all aliases usage was just replaced with the target
         |// type and this file is for reference purposes only.
         |// Should the new versions of TypeScript support this better -
         |// it can be enabled back.
         |//
         |// See this and other referenced threads for more information:
         |// https://github.com/Microsoft/TypeScript/issues/2552
         |          """.stripMargin

    // The legacy template's closing `"""` sat at column 11, with no `|`,
    // so `stripMargin` left 10 trailing spaces in the rendered output
    // (preceded by a newline). The `q"..."` form above places the closing
    // `"""` on its own line with a `|` + 10 spaces, then `stripMargin`
    // strips the `|` and we keep the 10 spaces — byte-identical to the
    // legacy emit. `verifyGoldens` is the operational test.
    AliasProduct(tree.render)
  }
}
