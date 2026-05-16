package mcpdemo



sealed trait PaymentResult extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait PaymentResultCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePaymentResult: Encoder.AsObject[PaymentResult] = Encoder.AsObject.instance {
    case v: PaymentResult.PaymentOk =>
      Map("PaymentOk" -> v.value).asJsonObject
    case v: PaymentResult.PaymentRejected =>
      Map("PaymentRejected" -> v.value).asJsonObject
  }
  implicit val decodePaymentResult: Decoder[PaymentResult] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "PaymentOk" =>
        value.as[_root_.mcpdemo.PaymentOk].map(PaymentResult.PaymentOk.apply)
      case "PaymentRejected" =>
        value.as[_root_.mcpdemo.PaymentRejected].map(PaymentResult.PaymentRejected.apply)
      case _ =>
        val cname = "mcpdemo.PaymentResult"
        val alts = List("PaymentOk", "PaymentRejected").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object PaymentResult extends PaymentResultCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = PaymentResult
  final case class PaymentOk(value: _root_.mcpdemo.PaymentOk) extends PaymentResult
  implicit def intoPaymentOk(value: _root_.mcpdemo.PaymentOk): PaymentResult = PaymentResult.PaymentOk(value)
  implicit def fromPaymentOk(value: PaymentResult.PaymentOk): _root_.mcpdemo.PaymentOk = value.value
  final case class PaymentRejected(value: _root_.mcpdemo.PaymentRejected) extends PaymentResult
  implicit def intoPaymentRejected(value: _root_.mcpdemo.PaymentRejected): PaymentResult = PaymentResult.PaymentRejected(value)
  implicit def fromPaymentRejected(value: PaymentResult.PaymentRejected): _root_.mcpdemo.PaymentRejected = value.value
}
       