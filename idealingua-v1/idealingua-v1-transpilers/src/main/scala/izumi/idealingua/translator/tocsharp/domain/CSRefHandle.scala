package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId

/** C# type-reference witness used as the `T` in `TextTree[T]`.
  *
  * F-TextTree M3 — mirror of the TS port's `TSRefHandle`
  * (see `…/totypescript/domain/TSRefHandle.scala`) for the C# renderer
  * family. Renderers compose `TextTree[CSRefHandle]` so that:
  *   1. type references travel through the tree as
  *      `ValueNode(CSRefHandle.TypeRef(typeId))`,
  *   2. the boundary call `tree.mapRender(resolver.resolve)` substitutes
  *      each reference with the rendered C# identifier.
  *
  * Pattern source: baboon-compiler's typescript translator
  * (`TsValue` + `TsBaboonTranslator.renderTree`) and idealingua-v1 TS M1.5.
  *
  * Shape rationale: the C# renderer family is uniform in how it consumes
  * `DomainCSharpType` — every renderer-level call site is
  * `DomainCSharpType(id).renderType(true)` (fully-qualified, no import
  * shortening). The few `renderType(false)` call sites live inside the
  * converter's own helpers (`renderFromString`, the user-type case of
  * `renderUserType` when `withPackage = false`) and are exercised from
  * inside the converter, not from renderer envelopes. The single
  * `TypeRef(typeId)` case is therefore sufficient for byte-parity across
  * all renderer envelopes that adopt the typed protocol in M3.
  *
  * The ADT is left sealed and extensible so future cycles can add
  * cases (e.g. `LocalTypeRef` for import-shortened rendering when the
  * import accumulator gets lifted upstream, or `ImplStructRef` for the
  * `<Iface>Struct` impl-id shape) without breaking resolver dispatch.
  */
sealed trait CSRefHandle

object CSRefHandle {
  /** Reference to a user-defined or built-in type by `TypeId`. The
    * resolver dispatches on the `TypeId` shape and emits the
    * fully-qualified C# identifier (`namespace.Name`) via
    * `DomainCSharpType.renderType(withPackage = true)`.
    */
  final case class TypeRef(typeId: TypeId) extends CSRefHandle
}
