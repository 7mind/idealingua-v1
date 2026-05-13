package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AliasProduct
import izumi.idealingua.typer.ir.{Domain, TypeDef}

/** Renders a new-IR `TypeDef.Alias` as the same `AliasProduct` the legacy
  * `CSharpTranslator.renderAlias` produces.
  *
  * F-TextTree M3 — ported to the typed-renderer protocol established in
  * the TS port (M1.5/M2). The body is composed as
  * `TextTree[CSRefHandle]` and rendered via `.mapRender(resolver.resolve)`
  * at the product boundary; the type-reference for `i.target` flows
  * through the tree as `TextTree.value(CSRefHandle.TypeRef(i.target))`.
  *
  * C# does not natively support full type aliases (the `using A = B;` form
  * is file-scoped only and cannot cross namespace boundaries), so the
  * legacy renderer emits a fixed comment block referencing the alias's
  * target type. Byte-parity preserved by appending the trailing 9-space
  * pad the legacy `stripMargin` template carried (the closing `"""` was
  * column-10-aligned in the source).
  */
final class DomainCSAliasRenderer(ctx: DomainCSContext) {

  def renderAlias(i: TypeDef.Alias, im: CSharpImports): AliasProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im
    val resolver                    = new DomainCSTypeResolver()

    val aliasName: TextTree[CSRefHandle] = TextTree.text(i.id.name)
    val targetRef: TextTree[CSRefHandle] = TextTree.value(CSRefHandle.TypeRef(i.target))

    val tree: TextTree[CSRefHandle] =
      q"""// C# does not natively support full type aliases. They usually
         |// live only within the current file scope, making it impossible
         |// to make them type aliases within another namespace.
         |//
         |// Had it been fully supported, the code would be something like:
         |// using $aliasName = $targetRef
         |//
         |// For the time being, please use the target type everywhere you need.
         |         """.stripMargin

    AliasProduct(tree.mapRender(resolver.resolve))
  }
}
