package idltest.algebraics



sealed trait AdtWithInterface extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AdtWithInterfaceCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAdtWithInterface: Encoder.AsObject[AdtWithInterface] = Encoder.AsObject.instance {
    case v: AdtWithInterface.AFace =>
      Map("AFace" -> v.value).asJsonObject
    case v: AdtWithInterface.Success =>
      Map("Success" -> v.value).asJsonObject
  }
  implicit val decodeAdtWithInterface: Decoder[AdtWithInterface] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "AFace" =>
        value.as[_root_.idltest.algebraics.AFace].map(AdtWithInterface.AFace.apply)
      case "Success" =>
        value.as[_root_.idltest.algebraics.Success].map(AdtWithInterface.Success.apply)
      case _ =>
        val cname = "idltest.algebraics.AdtWithInterface"
        val alts = List("AFace", "Success").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object AdtWithInterface extends AdtWithInterfaceCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = AdtWithInterface
  final case class AFace(value: _root_.idltest.algebraics.AFace) extends AdtWithInterface
  implicit def intoAFace(value: _root_.idltest.algebraics.AFace): AdtWithInterface = AdtWithInterface.AFace(value)
  implicit def fromAFace(value: AdtWithInterface.AFace): _root_.idltest.algebraics.AFace = value.value
  final case class Success(value: _root_.idltest.algebraics.Success) extends AdtWithInterface
  implicit def intoSuccess(value: _root_.idltest.algebraics.Success): AdtWithInterface = AdtWithInterface.Success(value)
  implicit def fromSuccess(value: AdtWithInterface.Success): _root_.idltest.algebraics.Success = value.value
}
       