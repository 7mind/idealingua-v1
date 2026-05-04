package izumi.test.domain01



sealed trait GenericFailureCode extends izumi.idealingua.runtime.model.IDLEnumElement

trait GenericFailureCodeCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeGenericFailureCode: Encoder[GenericFailureCode] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeGenericFailureCode: Decoder[GenericFailureCode] = Decoder.decodeString.emapTry(v => Try(GenericFailureCode.parse(v)))
  implicit val encodeKeyGenericFailureCode: KeyEncoder[GenericFailureCode] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyGenericFailureCode: KeyDecoder[GenericFailureCode] = new KeyDecoder[GenericFailureCode] { final def apply(key: String): Option[GenericFailureCode] = Try(GenericFailureCode.parse(key)).toOption }
}

object GenericFailureCode extends GenericFailureCodeCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = GenericFailureCode
  override def all: Seq[GenericFailureCode] = Seq(EntityNotFound, EntityAlreadyExists, ExpirationFailure, ConditionNotMet, AccessDenied, AssertionFailed, UnexpectedException, CodecFailed, Unknown)
  override def parse(value: String): GenericFailureCode = value match {
    case "EntityNotFound" => EntityNotFound
    case "EntityAlreadyExists" => EntityAlreadyExists
    case "ExpirationFailure" => ExpirationFailure
    case "ConditionNotMet" => ConditionNotMet
    case "AccessDenied" => AccessDenied
    case "AssertionFailed" => AssertionFailed
    case "UnexpectedException" => UnexpectedException
    case "CodecFailed" => CodecFailed
    case "Unknown" => Unknown
  }
  case object EntityNotFound extends GenericFailureCode { override def toString: String = "EntityNotFound" }
  case object EntityAlreadyExists extends GenericFailureCode { override def toString: String = "EntityAlreadyExists" }
  case object ExpirationFailure extends GenericFailureCode { override def toString: String = "ExpirationFailure" }
  case object ConditionNotMet extends GenericFailureCode { override def toString: String = "ConditionNotMet" }
  case object AccessDenied extends GenericFailureCode { override def toString: String = "AccessDenied" }
  case object AssertionFailed extends GenericFailureCode { override def toString: String = "AssertionFailed" }
  case object UnexpectedException extends GenericFailureCode { override def toString: String = "UnexpectedException" }
  case object CodecFailed extends GenericFailureCode { override def toString: String = "CodecFailed" }
  case object Unknown extends GenericFailureCode { override def toString: String = "Unknown" }
}
       