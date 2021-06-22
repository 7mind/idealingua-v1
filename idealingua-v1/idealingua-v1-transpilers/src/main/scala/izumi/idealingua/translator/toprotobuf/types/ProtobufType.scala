package izumi.idealingua.translator.toprotobuf.types

import izumi.idealingua.model.common.Package

final case class ProtobufType(pkg: Package, name: String, optional: Boolean = false)