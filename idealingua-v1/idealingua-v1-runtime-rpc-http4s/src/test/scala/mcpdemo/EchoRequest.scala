package mcpdemo



final case class EchoRequest(msg: String, n: Int) extends EchoRequest.Defn

trait EchoRequestCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeEchoRequest: Encoder.AsObject[EchoRequest] = deriveEncoder[EchoRequest]
  implicit val decodeEchoRequest: Decoder[EchoRequest] = deriveDecoder[EchoRequest]
}

object EchoRequest extends EchoRequestCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def msg: String
    def n: Int
  }
  def apply(msg: String, n: Int): EchoRequest = {
    new EchoRequest(msg = msg, n = n)
  }
  def apply(defn: EchoRequest.Defn): EchoRequest = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new EchoRequest(msg = defn.msg, n = defn.n)
  }
  implicit object EchoRequest_upcast_EchoRequest extends izumi.idealingua.runtime.IRTCast[EchoRequest, EchoRequest] {
    override def convert(_value: EchoRequest): EchoRequest = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      EchoRequest(msg = _value.msg, n = _value.n)
    }
  }
  implicit class EchoRequestExtensions(override protected val _value: EchoRequest) extends izumi.idealingua.runtime.IRTConversions[EchoRequest]
}
       