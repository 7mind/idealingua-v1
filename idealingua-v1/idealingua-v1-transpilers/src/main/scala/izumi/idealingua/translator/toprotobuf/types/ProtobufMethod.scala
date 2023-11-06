package izumi.idealingua.translator.toprotobuf.types

import izumi.idealingua.translator.toprotobuf.types.ProtobufMethod.ProtobufOutput

final case class ProtobufMethod(name: String, inputs: List[ProtobufField], output: ProtobufOutput)

object ProtobufMethod {
  sealed trait ProtobufOutput

  sealed trait NonAlternativeOutput extends ProtobufOutput

  case object Void extends NonAlternativeOutput
  final case class ADT(members: List[ProtobufAdtMember]) extends NonAlternativeOutput
  final case class Single(tpe: ProtobufType) extends NonAlternativeOutput
  final case class Structure(fields: List[ProtobufField]) extends NonAlternativeOutput

  final case class Alternative(success: NonAlternativeOutput, failure: NonAlternativeOutput) extends ProtobufOutput
}
