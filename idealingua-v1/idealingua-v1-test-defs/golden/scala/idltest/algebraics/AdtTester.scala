package idltest.algebraics



sealed trait AdtTester extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AdtTesterCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAdtTester: Encoder.AsObject[AdtTester] = Encoder.AsObject.instance {
    case v: AdtTester.ComplexAdt =>
      Map("ComplexAdt" -> v.value).asJsonObject
    case v: AdtTester.ComplexAdt2 =>
      Map("ComplexAdt2" -> v.value).asJsonObject
  }
  implicit val decodeAdtTester: Decoder[AdtTester] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "ComplexAdt" =>
        value.as[_root_.idltest.algebraics.ComplexAdt].map(AdtTester.ComplexAdt.apply)
      case "ComplexAdt2" =>
        value.as[_root_.idltest.algebraics.ComplexAdt2].map(AdtTester.ComplexAdt2.apply)
      case _ =>
        val cname = "idltest.algebraics.AdtTester"
        val alts = List("ComplexAdt", "ComplexAdt2").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object AdtTester extends AdtTesterCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = AdtTester
  final case class ComplexAdt(value: _root_.idltest.algebraics.ComplexAdt) extends AdtTester
  implicit def intoComplexAdt(value: _root_.idltest.algebraics.ComplexAdt): AdtTester = AdtTester.ComplexAdt(value)
  implicit def fromComplexAdt(value: AdtTester.ComplexAdt): _root_.idltest.algebraics.ComplexAdt = value.value
  final case class ComplexAdt2(value: _root_.idltest.algebraics.ComplexAdt2) extends AdtTester
  implicit def intoComplexAdt2(value: _root_.idltest.algebraics.ComplexAdt2): AdtTester = AdtTester.ComplexAdt2(value)
  implicit def fromComplexAdt2(value: AdtTester.ComplexAdt2): _root_.idltest.algebraics.ComplexAdt2 = value.value
}
       