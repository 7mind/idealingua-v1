package mcpdemo



sealed trait Color extends izumi.idealingua.runtime.model.IDLEnumElement

trait ColorCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeColor: Encoder[Color] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeColor: Decoder[Color] = Decoder.decodeString.emapTry(v => Try(Color.parse(v)))
  implicit val encodeKeyColor: KeyEncoder[Color] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyColor: KeyDecoder[Color] = new KeyDecoder[Color] { final def apply(key: String): Option[Color] = Try(Color.parse(key)).toOption }
}

object Color extends ColorCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = Color
  override def all: Seq[Color] = Seq(Red, Green, Blue)
  override def parse(value: String): Color = value match {
    case "Red" => Red
    case "Green" => Green
    case "Blue" => Blue
  }
  case object Red extends Color { override def toString: String = "Red" }
  case object Green extends Color { override def toString: String = "Green" }
  case object Blue extends Color { override def toString: String = "Blue" }
}
       