package idltest.events



sealed trait EnumType extends izumi.idealingua.runtime.model.IDLEnumElement

trait EnumTypeCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeEnumType: Encoder[EnumType] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeEnumType: Decoder[EnumType] = Decoder.decodeString.emapTry(v => Try(EnumType.parse(v)))
  implicit val encodeKeyEnumType: KeyEncoder[EnumType] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyEnumType: KeyDecoder[EnumType] = new KeyDecoder[EnumType] { final def apply(key: String): Option[EnumType] = Try(EnumType.parse(key)).toOption }
}

object EnumType extends EnumTypeCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = EnumType
  override def all: Seq[EnumType] = Seq(EnumA, EnumB)
  override def parse(value: String): EnumType = value match {
    case "EnumA" => EnumA
    case "EnumB" => EnumB
  }
  case object EnumA extends EnumType { override def toString: String = "EnumA" }
  case object EnumB extends EnumType { override def toString: String = "EnumB" }
}
       