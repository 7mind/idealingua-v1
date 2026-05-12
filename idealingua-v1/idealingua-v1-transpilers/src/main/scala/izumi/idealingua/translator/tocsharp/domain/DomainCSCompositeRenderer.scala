package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.CompositeProduct
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Dto` as the same pre-extension
  * `CompositeProduct` the legacy `CSharpTranslator.renderDto` produces
  * (modulo the extension chain).
  *
  * IMPL-10-prep-Cs1: byte-parity port now consumes `DomainCSClass`
  * (Domain-backed). `Typespace` no longer threaded.
  *
  * DTOs may carry inherited fields via `i.struct.superclasses.interfaces`;
  * we project the flattened struct from `Domain.flattenedStructs(i.id)`
  * and re-derive the legacy `Struct` shape via `DomainCSStruct.fromFlat`.
  *
  * `DomainCSClass(id, name, st, implements)` folds the
  * `Struct.superclasses.interfaces` into the `implements` list inside the
  * factory — `withSlices = true` then iterates them to render
  * `To<Iface>()` / `Load<Iface>()` helpers. The per-slice impl-struct
  * lookup goes through `domain.flattenedStructs` (via the renderSlice
  * helper inside `DomainCSClass`).
  *
  * Extension chain is omitted as in the legacy default
  * (`JsonNetExtension` has no DTO overrides in `imports` / `postModelEmit`
  * apart from the synthetic-impl handling).
  */
final class DomainCSCompositeRenderer(ctx: DomainCSContext) {

  def renderDto(i: NewTypeDef.Dto, im: CSharpImports): CompositeProduct =
    renderDto(i, im, preSplice = "", postSplice = "", extraImports = List.empty)

  /** Production-swap variant: splices `preSplice` / `postSplice` and
    * merges `extraImports`.
    */
  def renderDto(
    i: NewTypeDef.Dto,
    im: CSharpImports,
    preSplice: String,
    postSplice: String,
    extraImports: List[String],
  ): CompositeProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val struct    = DomainCSClass(i.id, i.id.name, structure, List.empty)

    val dto =
      s"""${im.renderUsings()}
         |$preSplice
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true).shift(4)}
         |}
         |$postSplice
         |       """.stripMargin

    CompositeProduct(dto, im.renderImports(List("System", "System.Collections", "System.Collections.Generic") ++ extraImports))
  }
}
