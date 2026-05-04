package idltest.enums



sealed trait TestEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait TestEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestEnum: Encoder[TestEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestEnum: Decoder[TestEnum] = Decoder.decodeString.emapTry(v => Try(TestEnum.parse(v)))
  implicit val encodeKeyTestEnum: KeyEncoder[TestEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestEnum: KeyDecoder[TestEnum] = new KeyDecoder[TestEnum] { final def apply(key: String): Option[TestEnum] = Try(TestEnum.parse(key)).toOption }
}

object TestEnum extends TestEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = TestEnum
  override def all: Seq[TestEnum] = Seq(Element1, Element2, Element3, Element4)
  override def parse(value: String): TestEnum = value match {
    case "Element1" => Element1
    case "Element2" => Element2
    case "Element3" => Element3
    case "Element4" => Element4
  }
  case object Element1 extends TestEnum { override def toString: String = "Element1" }
  case object Element2 extends TestEnum { override def toString: String = "Element2" }
  case object Element3 extends TestEnum { override def toString: String = "Element3" }
  case object Element4 extends TestEnum { override def toString: String = "Element4" }
}
       