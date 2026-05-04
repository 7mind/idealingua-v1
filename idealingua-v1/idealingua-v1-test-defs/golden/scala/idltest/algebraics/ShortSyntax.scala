package idltest.algebraics



sealed trait ShortSyntax extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait ShortSyntaxCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeShortSyntax: Encoder.AsObject[ShortSyntax] = Encoder.AsObject.instance {
    case v: ShortSyntax.TestSuccess =>
      Map("TestSuccess" -> v.value).asJsonObject
    case v: ShortSyntax.Failure =>
      Map("Failure" -> v.value).asJsonObject
  }
  implicit val decodeShortSyntax: Decoder[ShortSyntax] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "TestSuccess" =>
        value.as[_root_.idltest.algebraics.Success].map(ShortSyntax.TestSuccess.apply)
      case "Failure" =>
        value.as[_root_.idltest.algebraics.Failure].map(ShortSyntax.Failure.apply)
      case _ =>
        val cname = "idltest.algebraics.ShortSyntax"
        val alts = List("TestSuccess", "Failure").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object ShortSyntax extends ShortSyntaxCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = ShortSyntax
  final case class TestSuccess(value: _root_.idltest.algebraics.Success) extends ShortSyntax
  implicit def intoTestSuccess(value: _root_.idltest.algebraics.Success): ShortSyntax = ShortSyntax.TestSuccess(value)
  implicit def fromTestSuccess(value: ShortSyntax.TestSuccess): _root_.idltest.algebraics.Success = value.value
  final case class Failure(value: _root_.idltest.algebraics.Failure) extends ShortSyntax
  implicit def intoFailure(value: _root_.idltest.algebraics.Failure): ShortSyntax = ShortSyntax.Failure(value)
  implicit def fromFailure(value: ShortSyntax.Failure): _root_.idltest.algebraics.Failure = value.value
}
       