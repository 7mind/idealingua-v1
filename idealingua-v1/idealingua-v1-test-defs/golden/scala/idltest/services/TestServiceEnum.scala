package idltest.services



sealed trait TestServiceEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait TestServiceEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestServiceEnum: Encoder[TestServiceEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestServiceEnum: Decoder[TestServiceEnum] = Decoder.decodeString.emapTry(v => Try(TestServiceEnum.parse(v)))
  implicit val encodeKeyTestServiceEnum: KeyEncoder[TestServiceEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestServiceEnum: KeyDecoder[TestServiceEnum] = new KeyDecoder[TestServiceEnum] { final def apply(key: String): Option[TestServiceEnum] = Try(TestServiceEnum.parse(key)).toOption }
}

object TestServiceEnum extends TestServiceEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = TestServiceEnum
  override def all: Seq[TestServiceEnum] = Seq(Value1, Value2)
  override def parse(value: String): TestServiceEnum = value match {
    case "Value1" => Value1
    case "Value2" => Value2
  }
  case object Value1 extends TestServiceEnum { override def toString: String = "Value1" }
  case object Value2 extends TestServiceEnum { override def toString: String = "Value2" }
}
       