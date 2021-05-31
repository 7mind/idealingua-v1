package izumi.idealingua.model.il.ast.raw.models

import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn

final case class ParsedModel(definitions: Seq[RawTopLevelDefn], includes: Seq[Inclusion])
