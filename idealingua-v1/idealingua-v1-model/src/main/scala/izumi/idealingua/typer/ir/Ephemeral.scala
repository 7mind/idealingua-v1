package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId.{DTOId, InterfaceId}

/** Synthetic DTO produced by Phase 7 (EphemeralSynthesizer).
  *
  * Each service/buzzer method yields two ephemeral DTOs (input + output);
  * each interface that is implemented by at least one DTO yields one
  * `InterfaceMirror` ephemeral.  Ephemerals are stored in `Domain.members`
  * under `Member.Ephemeral` alongside user-declared types.
  *
  * @param id     The synthesized DTOId.  Namespace follows the owning
  *               service/buzzer (per `DTOId.apply(parent: ServiceId, ...)`).
  * @param origin Which synthesis rule produced this DTO.
  * @param struct The materialized struct for code generation.
  */
final case class EphemeralDto(
  id: DTOId,
  origin: EphemeralOrigin,
  struct: Struct,
)

/** Discriminator for the three synthesis rules that produce ephemeral DTOs. */
sealed trait EphemeralOrigin

object EphemeralOrigin {

  /** Input parameter bag for an RPC method on a `Service` or `Buzzer`. */
  final case class MethodInput(owner: TypeId, methodName: String) extends EphemeralOrigin

  /** Output (result) struct for an RPC method on a `Service` or `Buzzer`. */
  final case class MethodOutput(owner: TypeId, methodName: String) extends EphemeralOrigin

  /** Mirror DTO synthesized from an interface so that translators that cannot
    * express interfaces directly (e.g. some language targets) have a concrete
    * struct to emit.
    */
  final case class InterfaceMirror(source: InterfaceId) extends EphemeralOrigin
}
