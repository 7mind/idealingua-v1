package idltest.services



sealed trait Environment extends izumi.idealingua.runtime.model.IDLEnumElement

trait EnvironmentCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeEnvironment: Encoder[Environment] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeEnvironment: Decoder[Environment] = Decoder.decodeString.emapTry(v => Try(Environment.parse(v)))
  implicit val encodeKeyEnvironment: KeyEncoder[Environment] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyEnvironment: KeyDecoder[Environment] = new KeyDecoder[Environment] { final def apply(key: String): Option[Environment] = Try(Environment.parse(key)).toOption }
}

object Environment extends EnvironmentCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = Environment
  override def all: Seq[Environment] = Seq(Dev, Prod)
  override def parse(value: String): Environment = value match {
    case "Dev" => Dev
    case "Prod" => Prod
  }
  case object Dev extends Environment { override def toString: String = "Dev" }
  case object Prod extends Environment { override def toString: String = "Prod" }
}
       