package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AliasProduct
import izumi.idealingua.translator.tocsharp.types.CSharpType
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Alias` as the same `AliasProduct` the legacy
  * `CSharpTranslator.renderAlias` produces.
  *
  * IMPL-7c Phase B M1: C# does not natively support full type aliases
  * (the `using A = B;` form is file-scoped only and cannot cross
  * namespace boundaries), so the legacy renderer emits a fixed comment
  * block referencing the alias's target type. The new renderer
  * reproduces that block verbatim — the only variable parts of the
  * comment are `i.id.name` (the alias's local name) and the rendered
  * native type for `i.target` via `CSharpType(i.target).renderType(true)`
  * (the `withPackage = true` form, fully-qualified by namespace).
  *
  * `CSharpType` requires implicit `CSharpImports` + `Typespace` and is
  * constructed at the call-site rather than held on the context — the
  * imports object is per-definition in the legacy translator
  * (`CSharpImports(definition, definition.id.path.toPackage)`). Both are
  * threaded per-call here for byte parity with the legacy renderer's
  * implicit-resolution chain. Under the new typer's full dealiasing
  * pass, `i.target` is never itself an `AliasId`, so the converter's
  * alias-dereference paths are not exercised; the parameters are only
  * structurally required by the `CSharpType` constructor.
  *
  * The legacy renderer does NOT invoke any extension hook on aliases
  * (`CSharpTranslatorExtension` has no `handleAlias`), so this renderer
  * produces the post-extension byte-equal output at M1 with no
  * extension chain to thread.
  */
final class DomainCSAliasRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderAlias(i: TypeDef.Alias, ts: Typespace, im: CSharpImports): AliasProduct = {
    implicit val _ts: Typespace      = ts
    implicit val _im: CSharpImports  = im
    val cstype = CSharpType(i.target)

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
