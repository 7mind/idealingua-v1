package izumi.test.domain01



sealed trait RTestEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait RTestEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeRTestEnum: Encoder[RTestEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeRTestEnum: Decoder[RTestEnum] = Decoder.decodeString.emapTry(v => Try(RTestEnum.parse(v)))
  implicit val encodeKeyRTestEnum: KeyEncoder[RTestEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyRTestEnum: KeyDecoder[RTestEnum] = new KeyDecoder[RTestEnum] { final def apply(key: String): Option[RTestEnum] = Try(RTestEnum.parse(key)).toOption }
}

object RTestEnum extends RTestEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = RTestEnum
  override def all: Seq[RTestEnum] = Seq(A)
  override def parse(value: String): RTestEnum = value match {
    case "A" => A
  }
  case object A extends RTestEnum { override def toString: String = "A" }
}
       