package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.il.ast.typed.{AdtMember, DefMethod, EnumMember, IdField, NodeMeta, TypedStream}

/** A user-declared structural type in the new IR.
  *
  * Every `TypeDef` carries its `TypeId` (a subtype of the legacy `TypeId`
  * sealed trait) and the `NodeMeta` (source position + doc comment +
  * annotations) from the raw AST.
  *
  * Note: services, buzzers, and streams are NOT `TypeDef` cases.  The legacy
  * model represents them with `ServiceId`, `BuzzerId`, `StreamsId` — plain
  * case classes that do NOT extend `TypeId` (the sealed trait).  Promoting them
  * to `TypeDef` would require new `TypeId` subtypes, which in turn would break
  * exhaustive pattern matches in existing legacy code that cannot be edited in
  * IMPL-1.  Instead, `Domain` carries them in dedicated `List[ServiceDef]`,
  * `List[BuzzerDef]`, `List[StreamsDef]` fields.  IMPL-2+ will establish the
  * full first-class member mapping once the sealed-TypeId tension is resolved.
  *
  * Collection-typed members (`alternatives`, `members`, `fields`) are `List`
  * to preserve declaration order — see master plan §4 "Field-ordering
  * invariant".
  */
sealed trait TypeDef {
  def id: TypeId
  def meta: NodeMeta
}

object TypeDef {

  /** A data-transfer object (DTO / mixin). */
  final case class Dto(id: DTOId, struct: Struct, meta: NodeMeta) extends TypeDef

  /** An interface (structural supertype). */
  final case class Interface(id: InterfaceId, struct: Struct, meta: NodeMeta) extends TypeDef

  /** An identifier type (a composite key whose fields are primitives or
    * sub-identifiers).
    */
  final case class Identifier(id: IdentifierId, fields: List[IdField], meta: NodeMeta) extends TypeDef

  /** An algebraic data type (tagged union). `alternatives` preserves source
    * declaration order (see Field-ordering invariant).
    */
  final case class Adt(id: AdtId, alternatives: List[AdtMember], meta: NodeMeta) extends TypeDef

  /** An enumeration. `members` preserves source declaration order. */
  final case class Enum(id: EnumId, members: List[EnumMember], meta: NodeMeta) extends TypeDef

  /** A type alias.  `target` is the resolved (fully-qualified) target `TypeId`;
    * aliases are fully dealiased by Phase 3 before any structural phase runs.
    */
  final case class Alias(id: AliasId, target: TypeId, meta: NodeMeta) extends TypeDef
}

/** An RPC service definition.
  *
  * Kept separate from `TypeDef` because `ServiceId` does not extend `TypeId`
  * (the sealed legacy trait) — see `TypeDef` scaladoc for rationale.
  * `methods` preserves source declaration order (Field-ordering invariant).
  */
final case class ServiceDef(id: TypeId.ServiceId, methods: List[DefMethod], meta: NodeMeta)

/** A buzzer (event bus / fire-and-forget service).
  *
  * See `ServiceDef` for why this is separate from `TypeDef`.
  * `events` preserves source declaration order.
  */
final case class BuzzerDef(id: TypeId.BuzzerId, events: List[DefMethod], meta: NodeMeta)

/** A streams declaration (deprecated-but-kept-working per C5/Q1).
  *
  * See `ServiceDef` for why this is separate from `TypeDef`.
  * `streams` preserves source declaration order.
  */
final case class StreamsDef(id: TypeId.StreamsId, streams: List[TypedStream], meta: NodeMeta)
