package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.TypeId

/** TypeScript type-reference witness used as the `T` in `TextTree[T]`.
  *
  * F-TextTree M1.5 — corrects M1's `TextTree[Nothing]` mistake. Renderers
  * emit `TextTree[TSRefHandle]` so that a post-composition pass can:
  *   1. collect all type-references via `.values` (for the import section),
  *   2. resolve each reference to a rendered TypeScript identifier via
  *      `.mapRender(resolver.resolve)`.
  *
  * Pattern source: baboon-compiler's `TsValue` (see
  * `baboon-compiler/src/main/scala/io/septimalmind/baboon/translator/typescript/TsValue.scala`
  * and `TsBaboonTranslator.renderTree`, which `o.tree.values.collect`s used
  * types, builds the import block, then `full.mapRender { ... }` substitutes
  * each `TsType` with its (possibly aliased) name).
  *
  * Shape rationale: a single `TypeRef(typeId)` case is the minimum surface
  * that supports cross-domain disambiguation (the `TypeId` carries
  * `path`/`name`/`domain`). The legacy idealingua TS emitter does not use
  * import aliases for name conflicts (it relies on per-domain output
  * directories and `.. /..` relative paths via `DomainTSImports.withImport`),
  * so we do not yet need baboon's `alias` / `predef` / `typeOnly` fields.
  * The witness ADT is left sealed and extensible so M2+ renderers can add
  * cases (e.g. interface impl-id refs, generic applications) without
  * breaking the resolver dispatch.
  */
sealed trait TSRefHandle

object TSRefHandle {
  /** Reference to a user-defined or built-in type by `TypeId`. The resolver
    * dispatches on the `TypeId` shape (`Primitive` / `Generic` / `AdtId` /
    * `DTOId` / `InterfaceId` / `EnumId` / `IdentifierId` / `AliasId`) and
    * emits the appropriate native TS identifier, optionally recording an
    * import contribution.
    */
  final case class TypeRef(typeId: TypeId) extends TSRefHandle

  /** Reference to the serialized (`forSerialized = true`) shape of the
    * given type. Resolver delegates to
    * `DomainTSTypeConverter.toNativeType(id, forSerialized = true)`.
    * F-TextTree M2: introduced to capture the second-most-common typed
    * reference in TS renderers (DTO/Interface/AltOut shapes).
    */
  final case class SerializedTypeRef(typeId: TypeId) extends TSRefHandle
}
