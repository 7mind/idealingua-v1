package mcpdemo



final case class PaymentOk(txId: String, amount: Long) extends PaymentOk.Defn

trait PaymentOkCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePaymentOk: Encoder.AsObject[PaymentOk] = deriveEncoder[PaymentOk]
  implicit val decodePaymentOk: Decoder[PaymentOk] = deriveDecoder[PaymentOk]
}

object PaymentOk extends PaymentOkCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def txId: String
    def amount: Long
  }
  def apply(txId: String, amount: Long): PaymentOk = {
    new PaymentOk(txId = txId, amount = amount)
  }
  def apply(defn: PaymentOk.Defn): PaymentOk = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new PaymentOk(txId = defn.txId, amount = defn.amount)
  }
  implicit object PaymentOk_upcast_PaymentOk extends izumi.idealingua.runtime.IRTCast[PaymentOk, PaymentOk] {
    override def convert(_value: PaymentOk): PaymentOk = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PaymentOk(txId = _value.txId, amount = _value.amount)
    }
  }
  implicit class PaymentOkExtensions(override protected val _value: PaymentOk) extends izumi.idealingua.runtime.IRTConversions[PaymentOk]
}
       