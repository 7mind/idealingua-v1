package idltest.enums



sealed trait ShortSyntaxEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait ShortSyntaxEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeShortSyntaxEnum: Encoder[ShortSyntaxEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeShortSyntaxEnum: Decoder[ShortSyntaxEnum] = Decoder.decodeString.emapTry(v => Try(ShortSyntaxEnum.parse(v)))
  implicit val encodeKeyShortSyntaxEnum: KeyEncoder[ShortSyntaxEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyShortSyntaxEnum: KeyDecoder[ShortSyntaxEnum] = new KeyDecoder[ShortSyntaxEnum] { final def apply(key: String): Option[ShortSyntaxEnum] = Try(ShortSyntaxEnum.parse(key)).toOption }
}

object ShortSyntaxEnum extends ShortSyntaxEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = ShortSyntaxEnum
  override def all: Seq[ShortSyntaxEnum] = Seq(Element11, Element22)
  override def parse(value: String): ShortSyntaxEnum = value match {
    case "Element11" => Element11
    case "Element22" => Element22
  }
  case object Element11 extends ShortSyntaxEnum { override def toString: String = "Element11" }
  case object Element22 extends ShortSyntaxEnum { override def toString: String = "Element22" }
}
       