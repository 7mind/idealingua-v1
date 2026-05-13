package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.TypeId

/** Per-domain TypeScript type-reference resolver.
  *
  * Mirror of baboon's pattern (see `TsBaboonTranslator.renderTree`):
  *   - `resolve` turns a `TSRefHandle` into the rendered TS identifier; this
  *     is what `TextTree[TSRefHandle].mapRender(resolver.resolve)` consumes
  *     at the product boundary.
  *   - `harvestImports` projects the set of references gathered from a
  *     `TextTree[TSRefHandle].values` into a set of `TypeScriptImport`s
  *     (delegating to `DomainTSImports` for the per-package import-path
  *     computation).
  *
  * Two-phase contract: build a `TextTree[TSRefHandle]`, walk it once for
  * imports, walk it again via `mapRender` for the final string. The
  * `DomainTSTypeConverter` continues to own the type-name shape (interface
  * impl-id Suffixes, `Serialized` suffixes, generic envelopes); the
  * resolver simply wraps the converter for the in-tree-substitution leg.
  *
  * F-TextTree M1.5 — currently exercised by `DomainTSAliasRenderer` only;
  * subsequent renderers (M2+) will adopt the same protocol.
  */
final class DomainTSTypeResolver(conv: DomainTSTypeConverter) {

  /** Render an in-tree type reference to its native TS identifier. Used as
    * the argument to `TextTree[TSRefHandle].mapRender(_)`.
    *
    * Delegates to `DomainTSTypeConverter.toNativeType` to preserve the
    * legacy name-shape contract (verified by `verifyGoldens`).
    */
  def resolve(ref: TSRefHandle): String = ref match {
    case TSRefHandle.TypeRef(id) => conv.toNativeType(id)
  }

  /** Project a collected reference set into the import contributions for a
    * given source package. The reference iteration order is preserved (after
    * `.distinct`); `DomainTSImports.forTypes` is the existing per-package
    * resolver that knows how to relativize import paths under the active
    * `TypeScriptBuildManifest` layout (YARN vs flat).
    *
    * The harvest is a *projection* of the tree — it never adds references
    * the tree does not contain. Callers that need to import auxiliary
    * runtime types (e.g. `Formatter` from `irt` for date primitives) still
    * thread those through `DomainTSImports` via the `extra` parameter or
    * via `TSRefHandle.TypeRef(<primitive>)` nodes in the tree itself.
    */
  def harvestTypeIds(refs: Iterable[TSRefHandle]): List[TypeId] =
    refs.collect { case TSRefHandle.TypeRef(id) => id }.toList.distinct
}
