package mcpdemo



final case class PaymentRejected(reason: String, code: Int) extends PaymentRejected.Defn

trait PaymentRejectedCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.derivation.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePaymentRejected: Encoder.AsObject[PaymentRejected] = deriveEncoder[PaymentRejected]
  implicit val decodePaymentRejected: Decoder[PaymentRejected] = deriveDecoder[PaymentRejected]
}

object PaymentRejected extends PaymentRejectedCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def reason: String
    def code: Int
  }
  def apply(reason: String, code: Int): PaymentRejected = {
    new PaymentRejected(reason = reason, code = code)
  }
  def apply(defn: PaymentRejected.Defn): PaymentRejected = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new PaymentRejected(reason = defn.reason, code = defn.code)
  }
  implicit object PaymentRejected_upcast_PaymentRejected extends izumi.idealingua.runtime.IRTCast[PaymentRejected, PaymentRejected] {
    override def convert(_value: PaymentRejected): PaymentRejected = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PaymentRejected(reason = _value.reason, code = _value.code)
    }
  }
  implicit class PaymentRejectedExtensions(override protected val _value: PaymentRejected) extends izumi.idealingua.runtime.IRTConversions[PaymentRejected]
}
       