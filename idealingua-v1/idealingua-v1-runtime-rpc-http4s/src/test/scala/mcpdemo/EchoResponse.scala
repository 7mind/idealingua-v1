package mcpdemo



final case class EchoResponse(echo: String, count: Int) extends EchoResponse.Defn

trait EchoResponseCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.derivation.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeEchoResponse: Encoder.AsObject[EchoResponse] = deriveEncoder[EchoResponse]
  implicit val decodeEchoResponse: Decoder[EchoResponse] = deriveDecoder[EchoResponse]
}

object EchoResponse extends EchoResponseCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def echo: String
    def count: Int
  }
  def apply(echo: String, count: Int): EchoResponse = {
    new EchoResponse(echo = echo, count = count)
  }
  def apply(defn: EchoResponse.Defn): EchoResponse = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new EchoResponse(echo = defn.echo, count = defn.count)
  }
  implicit object EchoResponse_upcast_EchoResponse extends izumi.idealingua.runtime.IRTCast[EchoResponse, EchoResponse] {
    override def convert(_value: EchoResponse): EchoResponse = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      EchoResponse(echo = _value.echo, count = _value.count)
    }
  }
  implicit class EchoResponseExtensions(override protected val _value: EchoResponse) extends izumi.idealingua.runtime.IRTConversions[EchoResponse]
}
       