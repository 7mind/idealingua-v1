package izumi.test.domain01



sealed trait GoAliasEnumTest extends izumi.idealingua.runtime.model.IDLEnumElement

trait GoAliasEnumTestCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeGoAliasEnumTest: Encoder[GoAliasEnumTest] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeGoAliasEnumTest: Decoder[GoAliasEnumTest] = Decoder.decodeString.emapTry(v => Try(GoAliasEnumTest.parse(v)))
  implicit val encodeKeyGoAliasEnumTest: KeyEncoder[GoAliasEnumTest] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyGoAliasEnumTest: KeyDecoder[GoAliasEnumTest] = new KeyDecoder[GoAliasEnumTest] { final def apply(key: String): Option[GoAliasEnumTest] = Try(GoAliasEnumTest.parse(key)).toOption }
}

object GoAliasEnumTest extends GoAliasEnumTestCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = GoAliasEnumTest
  override def all: Seq[GoAliasEnumTest] = Seq(Val1, Val2)
  override def parse(value: String): GoAliasEnumTest = value match {
    case "Val1" => Val1
    case "Val2" => Val2
  }
  case object Val1 extends GoAliasEnumTest { override def toString: String = "Val1" }
  case object Val2 extends GoAliasEnumTest { override def toString: String = "Val2" }
}
       