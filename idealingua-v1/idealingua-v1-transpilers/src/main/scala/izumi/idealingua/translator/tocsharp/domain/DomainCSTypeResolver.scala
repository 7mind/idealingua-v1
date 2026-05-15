package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.typer.ir.Domain

/** Per-domain C# type-reference resolver.
  *
  * F-TextTree M3 — mirror of `DomainTSTypeResolver` for the C# renderer
  * family. Turns a `CSRefHandle` into the rendered C# identifier; this
  * is what `TextTree[CSRefHandle].mapRender(resolver.resolve)` consumes
  * at the product boundary.
  *
  * The resolver delegates to `DomainCSharpType.renderType(withPackage = true)`,
  * preserving the byte-equality contract validated by `verifyGoldens`. The
  * `CSharpImports` / `Domain` implicits are captured at resolver
  * construction time — every renderer that uses the resolver constructs
  * one per call (the converter shape is `(id, im, domain)` so the
  * resolver can be parameter-passed where the converter is in scope).
  *
  * `harvestTypeIds` is the projection used by a future import-collection
  * pass (option B'): once the renderer family has settled, an upstream
  * pass can walk `tree.values.collect { case CSRefHandle.TypeRef(id) => id }`
  * to compute the precise import set for a file. For M3, the C# import
  * computation continues to be owned by `DomainCSImports` (the renderer
  * doesn't drive the import section directly — the translator constructs
  * the `CSharpImports` instance per type and threads it in).
  */
final class DomainCSTypeResolver()(implicit im: CSharpImports, domain: Domain) {

  /** Render an in-tree type reference to its native C# identifier. Used
    * as the argument to `TextTree[CSRefHandle].mapRender(_)`.
    *
    * Delegates to `DomainCSharpType.renderType(true)` (fully qualified)
    * to preserve the legacy name-shape contract.
    */
  def resolve(ref: CSRefHandle): String = ref match {
    case CSRefHandle.TypeRef(id) => DomainCSharpType(id).renderType(true)
  }

  /** Project a collected reference set into the type-id set. The
    * harvest is a *projection* of the tree — it never adds references
    * the tree does not contain.
    */
  def harvestTypeIds(refs: Iterable[CSRefHandle]): List[TypeId] =
    refs.collect {
      case CSRefHandle.TypeRef(id) => id
    }.toList.distinct
}
