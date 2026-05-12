package izumi.idealingua.model.il.ast.typed

import izumi.idealingua.model.il.ast.raw.domains.Import
import izumi.idealingua.model.loader.FSPath

case class Inclusion(include: String)

final case class DomainMetadata(origin: FSPath, directInclusions: Seq[Inclusion], directImports: Seq[Import], meta: NodeMeta)
