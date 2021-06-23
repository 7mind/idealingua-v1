package izumi.idealingua.translator.toprotobuf.types

import izumi.idealingua.model.common.Package

final case class ProtobufType(pkg: Package, name: String, args: List[ProtobufType] = Nil, optional: Option[Boolean] = Some(false)) {
  def fullName: String = {
    if (pkg.nonEmpty) {
      s"${pkg.mkString(".")}.$name"
    } else {
      name
    }
  }

  def imports: Set[String] = {
    if (pkg.nonEmpty) {
      Set(s"""import "${pkg.mkString("/")}.proto";""")
    } else {
      Set.empty
    } ++ args.flatMap(_.imports)
  }
}