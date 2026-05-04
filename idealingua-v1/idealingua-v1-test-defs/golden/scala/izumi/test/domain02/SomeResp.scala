package izumi.test.domain02



sealed trait SomeResp extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait SomeRespCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeSomeResp: Encoder.AsObject[SomeResp] = Encoder.AsObject.instance {
    case v: SomeResp.ImportedBasicFailure =>
      Map("ImportedBasicFailure" -> v.value).asJsonObject
    case v: SomeResp.DTO1 =>
      Map("DTO1" -> v.value).asJsonObject
  }
  implicit val decodeSomeResp: Decoder[SomeResp] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "ImportedBasicFailure" =>
        value.as[_root_.izumi.test.domain02.ImportedBasicFailure].map(SomeResp.ImportedBasicFailure.apply)
      case "DTO1" =>
        value.as[_root_.izumi.test.domain02.DTO1].map(SomeResp.DTO1.apply)
      case _ =>
        val cname = "izumi.test.domain02.SomeResp"
        val alts = List("ImportedBasicFailure", "DTO1").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object SomeResp extends SomeRespCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = SomeResp
  final case class ImportedBasicFailure(value: _root_.izumi.test.domain02.ImportedBasicFailure) extends SomeResp
  implicit def intoImportedBasicFailure(value: _root_.izumi.test.domain02.ImportedBasicFailure): SomeResp = SomeResp.ImportedBasicFailure(value)
  implicit def fromImportedBasicFailure(value: SomeResp.ImportedBasicFailure): _root_.izumi.test.domain02.ImportedBasicFailure = value.value
  final case class DTO1(value: _root_.izumi.test.domain02.DTO1) extends SomeResp
  implicit def intoDTO1(value: _root_.izumi.test.domain02.DTO1): SomeResp = SomeResp.DTO1(value)
  implicit def fromDTO1(value: SomeResp.DTO1): _root_.izumi.test.domain02.DTO1 = value.value
}
       