package izumi.test.clashing.another



sealed trait SomeEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait SomeEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeSomeEnum: Encoder[SomeEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeSomeEnum: Decoder[SomeEnum] = Decoder.decodeString.emapTry(v => Try(SomeEnum.parse(v)))
  implicit val encodeKeySomeEnum: KeyEncoder[SomeEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeySomeEnum: KeyDecoder[SomeEnum] = new KeyDecoder[SomeEnum] { final def apply(key: String): Option[SomeEnum] = Try(SomeEnum.parse(key)).toOption }
}

object SomeEnum extends SomeEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = SomeEnum
  override def all: Seq[SomeEnum] = Seq(VALUE)
  override def parse(value: String): SomeEnum = value match {
    case "VALUE" => VALUE
  }
  case object VALUE extends SomeEnum { override def toString: String = "VALUE" }
}
       