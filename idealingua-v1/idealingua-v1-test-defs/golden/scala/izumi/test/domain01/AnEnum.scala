package izumi.test.domain01



sealed trait AnEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait AnEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeAnEnum: Encoder[AnEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeAnEnum: Decoder[AnEnum] = Decoder.decodeString.emapTry(v => Try(AnEnum.parse(v)))
  implicit val encodeKeyAnEnum: KeyEncoder[AnEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyAnEnum: KeyDecoder[AnEnum] = new KeyDecoder[AnEnum] { final def apply(key: String): Option[AnEnum] = Try(AnEnum.parse(key)).toOption }
}

object AnEnum extends AnEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = AnEnum
  override def all: Seq[AnEnum] = Seq(VALUE1, VALUE2)
  override def parse(value: String): AnEnum = value match {
    case "VALUE1" => VALUE1
    case "VALUE2" => VALUE2
  }
  case object VALUE1 extends AnEnum { override def toString: String = "VALUE1" }
  case object VALUE2 extends AnEnum { override def toString: String = "VALUE2" }
}
       