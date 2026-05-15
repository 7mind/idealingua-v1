package izumi.idealingua.translator.toscala.types

import izumi.idealingua.model.il.ast.typed.TypeDef._

sealed trait ClassSource

/** Origin tag carried by a `DomainCompositeStructure` so renderers can branch
  * on whether a struct is a DTO body, an interface impl body, or a service /
  * buzzer method input/output.
  *
  * IMPL-10c (2026-05-12): the legacy `ServiceContext` / `ServiceMethodProduct`
  * payloads on `CsMethodInput` / `CsMethodOutput` were dropped — they were
  * never read by the Domain pipeline (callers only used a `case _:` match
  * to skip body emission). The new-typer path carries the equivalent data
  * via `DomainServiceContext` + `DomainServiceMethodProduct`.
  */
object ClassSource {
  final case class CsDTO(dto: DTO) extends ClassSource
  final case class CsInterface(i: Interface) extends ClassSource
  case object CsMethodInput extends ClassSource
  case object CsMethodOutput extends ClassSource
}
