package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AliasProduct
import izumi.idealingua.typer.ir.{Domain, TypeDef}

/** Renders a new-IR `TypeDef.Alias` as the same `AliasProduct` the legacy
  * `CSharpTranslator.renderAlias` produces.
  *
  * IMPL-10-prep-Cs1: C# converter family ported off `Typespace`; uses
  * `DomainCSharpType` (Domain-backed) for the rendered native target.
  * `Typespace` no longer threaded; the renderer carries `ctx.domain`
  * implicitly so the converter can dealias against `domain.aliases`.
  *
  * C# does not natively support full type aliases (the `using A = B;` form
  * is file-scoped only and cannot cross namespace boundaries), so the
  * legacy renderer emits a fixed comment block referencing the alias's
  * target type. The new renderer reproduces that block verbatim — the
  * only variable parts of the comment are `i.id.name` and the rendered
  * native type for `i.target` via `DomainCSharpType(i.target).renderType(true)`
  * (the `withPackage = true` form, fully-qualified by namespace).
  *
  * The legacy renderer does NOT invoke any extension hook on aliases
  * (`CSharpTranslatorExtension` has no `handleAlias`), so this renderer
  * produces the post-extension byte-equal output with no extension chain
  * to thread.
  */
final class DomainCSAliasRenderer(ctx: DomainCSContext) {

  def renderAlias(i: TypeDef.Alias, im: CSharpImports): AliasProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im
    val cstype = DomainCSharpType(i.target)

    AliasProduct(
      s"""// C# does not natively support full type aliases. They usually
         |// live only within the current file scope, making it impossible
         |// to make them type aliases within another namespace.
         |//
         |// Had it been fully supported, the code would be something like:
         |// using ${i.id.name} = ${cstype.renderType(true)}
         |//
         |// For the time being, please use the target type everywhere you need.
         """.stripMargin
    )
  }
}
