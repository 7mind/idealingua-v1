package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.CompositeProduct
import izumi.idealingua.translator.tocsharp.types.CSharpClass
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Dto` as the same pre-extension
  * `CompositeProduct` the legacy `CSharpTranslator.renderDto` produces
  * (modulo the extension chain).
  *
  * IMPL-7c Phase B M2: byte-parity port. DTOs may carry inherited fields
  * via `i.struct.superclasses.interfaces`; we project the flattened
  * struct from `Domain.flattenedStructs(i.id)` and re-derive the legacy
  * `Struct` shape via `DomainCSStruct.fromFlat` (same sort key + dedup
  * as legacy `StructuralQueriesImpl`, language-agnostic).
  *
  * `CSharpClass(id, name, st, implements)` is invoked exactly as legacy
  * does — the legacy passes `List.empty` for `implements` because the
  * `Struct.superclasses.interfaces` are folded in inside `CSharpClass.apply`
  * (`st.superclasses.interfaces ++ implements`). The slice section
  * (`withSlices = true`) iterates `csClass.implements` to render
  * `To<Iface>()` / `Load<Iface>()` helpers and depends on
  * `ts.tools.implId(iface)` + `ts.structure.structure(eid)` for the
  * per-slice field projection. At M2 this is threaded off the legacy
  * `Typespace` (same as Phase A); M5 swap will substitute a
  * `Domain`-backed accessor.
  *
  * Extension chain (`ext.preModelEmit` / `ext.postModelEmit` /
  * `ext.imports`) is omitted: the default C# extension set
  * (`JsonNetExtension`) has no `DTO` overrides, so the pre-extension
  * product is byte-equal to the post-extension product for the default
  * extension list.
  */
final class DomainCSCompositeRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderDto(i: NewTypeDef.Dto, ts: Typespace, im: CSharpImports): CompositeProduct =
    renderDto(i, ts, im, preSplice = "", postSplice = "", extraImports = List.empty)

  /** M5 production-swap variant: splices `preSplice` into the legacy
    * `${ext.preModelEmit(ctx, i)}` slot (legacy `:81`), `postSplice` into
    * `${ext.postModelEmit(ctx, i)}` (legacy `:85`), and merges
    * `extraImports` into the header import list (legacy `:88`).
    *
    * The default no-splice call (used by M2 unit tests) preserves the
    * exact pre-M5 string shape and imports.
    */
  def renderDto(
    i: NewTypeDef.Dto,
    ts: Typespace,
    im: CSharpImports,
    preSplice: String,
    postSplice: String,
    extraImports: List[String],
  ): CompositeProduct = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val struct    = CSharpClass(i.id, i.id.name, structure, List.empty)

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
