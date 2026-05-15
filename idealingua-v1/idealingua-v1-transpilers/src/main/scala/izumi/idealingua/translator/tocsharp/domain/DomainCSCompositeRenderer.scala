package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.CompositeProduct
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Dto` as the same pre-extension
  * `CompositeProduct` the legacy `CSharpTranslator.renderDto` produces.
  *
  * F-TextTree M3 — ported to the typed-renderer protocol. The DTO body
  * itself contributes no value-typed references at the envelope level
  * (the struct's `renderHeader` / `render` methods themselves return
  * `String`, having internal converter usage that produces fully-resolved
  * names via `DomainCSField.renderMember`); the envelope adopts the
  * protocol structurally so the renderer family is uniform.
  */
final class DomainCSCompositeRenderer(ctx: DomainCSContext) {

  def renderDto(i: NewTypeDef.Dto, im: CSharpImports): CompositeProduct =
    renderDto(i, im, preSplice = "", postSplice = "", extraImports = List.empty)

  def renderDto(
    i: NewTypeDef.Dto,
    im: CSharpImports,
    preSplice: String,
    postSplice: String,
    extraImports: List[String],
  ): CompositeProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im
    val resolver                    = new DomainCSTypeResolver()

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val struct    = DomainCSClass(i.id, i.id.name, structure, List.empty)

    val tree: TextTree[CSRefHandle] =
      q"""${im.renderUsings()}
         |$preSplice
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true).shift(4)}
         |}
         |$postSplice
         |       """.stripMargin

    CompositeProduct(
      tree.mapRender(resolver.resolve),
      im.renderImports(List("System", "System.Collections", "System.Collections.Generic") ++ extraImports),
    )
  }
}
