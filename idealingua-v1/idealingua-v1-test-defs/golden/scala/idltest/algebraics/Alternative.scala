package idltest.algebraics



sealed trait Alternative extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AlternativeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAlternative: Encoder.AsObject[Alternative] = Encoder.AsObject.instance {
    case v: Alternative.TestSuccess =>
      Map("TestSuccess" -> v.value).asJsonObject
    case v: Alternative.Failure =>
      Map("Failure" -> v.value).asJsonObject
  }
  implicit val decodeAlternative: Decoder[Alternative] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "TestSuccess" =>
        value.as[_root_.idltest.algebraics.Success].map(Alternative.TestSuccess.apply)
      case "Failure" =>
        value.as[_root_.idltest.algebraics.Failure].map(Alternative.Failure.apply)
      case _ =>
        val cname = "idltest.algebraics.Alternative"
        val alts = List("TestSuccess", "Failure").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object Alternative extends AlternativeCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = Alternative
  final case class TestSuccess(value: _root_.idltest.algebraics.Success) extends Alternative
  implicit def intoTestSuccess(value: _root_.idltest.algebraics.Success): Alternative = Alternative.TestSuccess(value)
  implicit def fromTestSuccess(value: Alternative.TestSuccess): _root_.idltest.algebraics.Success = value.value
  final case class Failure(value: _root_.idltest.algebraics.Failure) extends Alternative
  implicit def intoFailure(value: _root_.idltest.algebraics.Failure): Alternative = Alternative.Failure(value)
  implicit def fromFailure(value: Alternative.Failure): _root_.idltest.algebraics.Failure = value.value
}
       