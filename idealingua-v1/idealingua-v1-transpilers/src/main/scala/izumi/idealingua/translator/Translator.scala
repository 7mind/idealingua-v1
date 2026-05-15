package izumi.idealingua.translator

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.typed.DomainMetadata
import izumi.idealingua.model.output.Module

case class Translated(domainId: DomainId, meta: DomainMetadata, modules: Seq[Module])

trait Translator {
  def translate(): Translated
}
