package idltest.json



sealed trait JSONLike extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait JSONLikeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeJSONLike: Encoder.AsObject[JSONLike] = Encoder.AsObject.instance {
    case v: JSONLike.JLObject =>
      Map("JLObject" -> v.value).asJsonObject
    case v: JSONLike.JLArray =>
      Map("JLArray" -> v.value).asJsonObject
    case v: JSONLike.JLString =>
      Map("JLString" -> v.value).asJsonObject
    case v: JSONLike.JLNumber =>
      Map("JLNumber" -> v.value).asJsonObject
    case v: JSONLike.JLBool =>
      Map("JLBool" -> v.value).asJsonObject
    case v: JSONLike.JLNull =>
      Map("JLNull" -> v.value).asJsonObject
  }
  implicit val decodeJSONLike: Decoder[JSONLike] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "JLObject" =>
        value.as[_root_.idltest.json.JLObject].map(JSONLike.JLObject.apply)
      case "JLArray" =>
        value.as[_root_.idltest.json.JLArray].map(JSONLike.JLArray.apply)
      case "JLString" =>
        value.as[_root_.idltest.json.JLString].map(JSONLike.JLString.apply)
      case "JLNumber" =>
        value.as[_root_.idltest.json.JLNumber].map(JSONLike.JLNumber.apply)
      case "JLBool" =>
        value.as[_root_.idltest.json.JLBool].map(JSONLike.JLBool.apply)
      case "JLNull" =>
        value.as[_root_.idltest.json.JLNull].map(JSONLike.JLNull.apply)
      case _ =>
        val cname = "idltest.json.JSONLike"
        val alts = List("JLObject", "JLArray", "JLString", "JLNumber", "JLBool", "JLNull").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object JSONLike extends JSONLikeCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = JSONLike
  final case class JLObject(value: _root_.idltest.json.JLObject) extends JSONLike
  implicit def intoJLObject(value: _root_.idltest.json.JLObject): JSONLike = JSONLike.JLObject(value)
  implicit def fromJLObject(value: JSONLike.JLObject): _root_.idltest.json.JLObject = value.value
  final case class JLArray(value: _root_.idltest.json.JLArray) extends JSONLike
  implicit def intoJLArray(value: _root_.idltest.json.JLArray): JSONLike = JSONLike.JLArray(value)
  implicit def fromJLArray(value: JSONLike.JLArray): _root_.idltest.json.JLArray = value.value
  final case class JLString(value: _root_.idltest.json.JLString) extends JSONLike
  implicit def intoJLString(value: _root_.idltest.json.JLString): JSONLike = JSONLike.JLString(value)
  implicit def fromJLString(value: JSONLike.JLString): _root_.idltest.json.JLString = value.value
  final case class JLNumber(value: _root_.idltest.json.JLNumber) extends JSONLike
  implicit def intoJLNumber(value: _root_.idltest.json.JLNumber): JSONLike = JSONLike.JLNumber(value)
  implicit def fromJLNumber(value: JSONLike.JLNumber): _root_.idltest.json.JLNumber = value.value
  final case class JLBool(value: _root_.idltest.json.JLBool) extends JSONLike
  implicit def intoJLBool(value: _root_.idltest.json.JLBool): JSONLike = JSONLike.JLBool(value)
  implicit def fromJLBool(value: JSONLike.JLBool): _root_.idltest.json.JLBool = value.value
  final case class JLNull(value: _root_.idltest.json.JLNull) extends JSONLike
  implicit def intoJLNull(value: _root_.idltest.json.JLNull): JSONLike = JSONLike.JLNull(value)
  implicit def fromJLNull(value: JSONLike.JLNull): _root_.idltest.json.JLNull = value.value
}
       