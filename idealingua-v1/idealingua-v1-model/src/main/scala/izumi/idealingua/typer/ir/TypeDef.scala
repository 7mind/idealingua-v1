package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.il.ast.typed.{AdtMember, DefMethod, EnumMember, IdField, NodeMeta, TypedStream}

/** A user-declared structural or service-family type in the new IR.
  *
  * Every `TypeDef` carries its `TypeId` (a subtype of the legacy `TypeId`
  * sealed trait) and the `NodeMeta` (source position + doc comment +
  * annotations) from the raw AST.
  *
  * After F16/Option A1 widening, services, buzzers, and streams have first-class
  * `TypeId` subtypes (`ServiceId`, `BuzzerId`, `StreamsId`) and flow through the
  * uniform `Map[TypeId, Member]` namespace as `Member.User(TypeDef.Service(...))`
  * etc., matching master plan §4 pseudocode.
  *
  * Collection-typed members (`alternatives`, `members`, `fields`, `methods`,
  * `events`, `streams`) are `List` to preserve declaration order — see master
  * plan §4 "Field-ordering invariant".
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

  /** An RPC service definition. `methods` preserves source declaration order. */
  final case class Service(id: ServiceId, methods: List[DefMethod], meta: NodeMeta) extends TypeDef

  /** A buzzer (event bus / fire-and-forget service). `events` preserves source
    * declaration order.
    */
  final case class Buzzer(id: BuzzerId, events: List[DefMethod], meta: NodeMeta) extends TypeDef

  /** A streams declaration (deprecated-but-kept-working per C5/Q1).
    * `streams` preserves source declaration order.
    */
  final case class Streams(id: StreamsId, streams: List[TypedStream], meta: NodeMeta) extends TypeDef
}
