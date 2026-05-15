package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.Primitive

/** A node in `Domain.members`: either a user-declared structural type, a
  * synthesized ephemeral DTO, or a built-in primitive.
  *
  * The `Member` wrapper allows `Domain.members: Map[TypeId, Member]` to hold
  * all three categories under one key space, so translators can look up any
  * referenced type in O(1) without branching on which sub-map to consult.
  *
  * Note: services, buzzers, and streams are NOT in `Domain.members` because
  * their IDs (`ServiceId`, `BuzzerId`, `StreamsId`) do not extend the sealed
  * `TypeId` trait (see `TypeDef` scaladoc for full rationale).  They appear in
  * `Domain.services`, `Domain.buzzers`, and `Domain.streams` respectively.
  */
sealed trait Member

object Member {

  /** A user-declared structural type (`TypeDef`): DTO, Interface, Identifier,
    * Adt, Enum, or Alias.
    */
  final case class User(defn: TypeDef) extends Member

  /** A synthesized ephemeral DTO produced by Phase 7 (EphemeralSynthesizer).
    * See `EphemeralDto` and `EphemeralOrigin` for details.
    */
  final case class Ephemeral(defn: EphemeralDto) extends Member

  /** A built-in primitive (`Primitive.*`), made explicit in `members` so that
    * translators can resolve primitive references through the same lookup path
    * as user types.
    */
  final case class Builtin(prim: Primitive) extends Member
}
