package izumi.test.domain02



sealed trait AdtA extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AdtACirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAdtA: Encoder.AsObject[AdtA] = Encoder.AsObject.instance {
    case v: AdtA.AdtA1 =>
      Map("AdtA1" -> v.value).asJsonObject
    case v: AdtA.AdtA2 =>
      Map("AdtA2" -> v.value).asJsonObject
  }
  implicit val decodeAdtA: Decoder[AdtA] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "AdtA1" =>
        value.as[_root_.izumi.test.domain02.AdtA1].map(AdtA.AdtA1.apply)
      case "AdtA2" =>
        value.as[_root_.izumi.test.domain02.AdtA2].map(AdtA.AdtA2.apply)
      case _ =>
        val cname = "izumi.test.domain02.AdtA"
        val alts = List("AdtA1", "AdtA2").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object AdtA extends AdtACirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = AdtA
  final case class AdtA1(value: _root_.izumi.test.domain02.AdtA1) extends AdtA
  implicit def intoAdtA1(value: _root_.izumi.test.domain02.AdtA1): AdtA = AdtA.AdtA1(value)
  implicit def fromAdtA1(value: AdtA.AdtA1): _root_.izumi.test.domain02.AdtA1 = value.value
  final case class AdtA2(value: _root_.izumi.test.domain02.AdtA2) extends AdtA
  implicit def intoAdtA2(value: _root_.izumi.test.domain02.AdtA2): AdtA = AdtA.AdtA2(value)
  implicit def fromAdtA2(value: AdtA.AdtA2): _root_.izumi.test.domain02.AdtA2 = value.value
}
       