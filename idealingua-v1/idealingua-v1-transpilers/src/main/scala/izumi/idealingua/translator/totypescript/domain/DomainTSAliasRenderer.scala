package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.totypescript.products.CogenProduct.AliasProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Alias` as the same `AliasProduct` the legacy
  * `TypeScriptTranslator.renderAlias` produces.
  *
  * F-TextTree M1.5 (architecture correction) — supersedes M1's
  * `TextTree[Nothing]` + `.render` choice. The renderer now composes
  * `TextTree[TSRefHandle]`: type references travel through the tree as
  * `ValueNode(TSRefHandle.TypeRef(typeId))`, and the boundary call is
  * `tree.mapRender(resolver.resolve)`. This matches baboon's pattern (see
  * `baboon-compiler/src/main/scala/io/septimalmind/baboon/translator/typescript/TsBaboonTranslator.scala`,
  * `renderTree`: `usedTypes = o.tree.values.collect { case t: TsValue.TsType => t }`
  * then `full.mapRender { ... }`) and unlocks the import-section
  * post-pass: a future module-level emitter can call
  * `tree.values.collect { case TSRefHandle.TypeRef(id) => id }` to compute
  * the precise import set for a file, independent of any side-effecting
  * accumulator.
  *
  * Renderer protocol locked at **option B''-lite** for M1.5: the
  * `AliasProduct.content: String` shape is unchanged; the renderer itself
  * still calls `.mapRender` at the product boundary. The harvest pathway is
  * exercised end-to-end (resolver in scope, `.values` available on the
  * tree) so M2+ can incrementally adopt the typed protocol without further
  * boundary changes, and a later layouter-driven pass can move the
  * `.mapRender` call upstream (option B') without touching renderers.
  *
  * IMPL-7b Phase B M1 (semantic notes preserved from the original port):
  * TypeScript does not natively support type aliases with constructor /
  * casting semantics, so the legacy renderer emits a fixed comment block
  * referencing the alias's target type. The new renderer reproduces this
  * verbatim — the only variable parts of the comment are `i.id.name` (the
  * alias's local name, plain text — no type-reference) and the rendered
  * native type for `i.target` (passed through the tree as a
  * `TSRefHandle.TypeRef` value node).
  *
  * Under the new typer's full dealiasing pass, `i.target` is never itself
  * an `AliasId`, so the resolver's converter path collapses to a single
  * lookup; the alias renderer contributes a single, well-formed type
  * reference per emitted file — easy to verify against `verifyGoldens`.
  *
  * The legacy renderer also runs the extension chain
  * (`ctx.ext.extend(i, AliasProduct(...), _.handleAlias)`); the default TS
  * extension set (`EnumHelpersExtension`, `IntrospectionExtension`) has no
  * alias handler, so the extension chain is a no-op for aliases and M1.5
  * omits it. M2+ will reintegrate the extension chain when it matters for
  * richer renderers.
  *
  * Byte-equality with the legacy `stripMargin`-based template is preserved
  * by appending the trailing 10-space pad that the legacy template carried
  * as content after its final `|`-prefixed line (the closing `"""` was
  * column-11-aligned in the source). See `harness/verifyGoldens` for the
  * byte-level contract.
  */
final class DomainTSAliasRenderer(ctx: DomainTSContext) {

  import ctx._

  private val resolver = new DomainTSTypeResolver(conv)

  def renderAlias(i: TypeDef.Alias): AliasProduct = {
    val aliasName: TextTree[TSRefHandle] = TextTree.text(i.id.name)
    val targetRef: TextTree[TSRefHandle] = TextTree.value(TSRefHandle.TypeRef(i.target))

    val tree: TextTree[TSRefHandle] =
      q"""// TypeScript does not natively support well type aliases.
         |// Normally the code would be:
         |// export type $aliasName = $targetRef;
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
    //
    // The harvested type-reference set is `tree.values` — currently just
    // `Seq(TSRefHandle.TypeRef(i.target))`. M2+ will surface this on the
    // product so the module emitter can fold it into the import section
    // without re-walking the IR.
    AliasProduct(tree.mapRender(resolver.resolve))
  }
}
