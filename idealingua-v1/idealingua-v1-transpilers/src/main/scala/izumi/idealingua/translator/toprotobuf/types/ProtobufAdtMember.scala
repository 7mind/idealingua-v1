package izumi.idealingua.translator.toprotobuf.types

import izumi.fundamentals.platform.strings.IzString._

final case class ProtobufAdtMember(memberName: Option[String], tpe: ProtobufType) {
  def name: String = memberName.getOrElse(tpe.name).uncapitalize
}
