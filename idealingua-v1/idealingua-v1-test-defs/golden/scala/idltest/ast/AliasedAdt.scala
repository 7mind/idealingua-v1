package idltest.ast



sealed trait AliasedAdt extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AliasedAdtCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAliasedAdt: Encoder.AsObject[AliasedAdt] = Encoder.AsObject.instance {
    case v: AliasedAdt.Event =>
      Map("event" -> v.value).asJsonObject
    case v: AliasedAdt.Public =>
      Map("public" -> v.value).asJsonObject
  }
  implicit val decodeAliasedAdt: Decoder[AliasedAdt] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "event" =>
        value.as[_root_.idltest.ast.EventData].map(AliasedAdt.Event.apply)
      case "public" =>
        value.as[_root_.idltest.ast.PublicData].map(AliasedAdt.Public.apply)
      case _ =>
        val cname = "idltest.ast.AliasedAdt"
        val alts = List("event", "public").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object AliasedAdt extends AliasedAdtCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = AliasedAdt
  final case class Event(value: _root_.idltest.ast.EventData) extends AliasedAdt
  implicit def intoEvent(value: _root_.idltest.ast.EventData): AliasedAdt = AliasedAdt.Event(value)
  implicit def fromEvent(value: AliasedAdt.Event): _root_.idltest.ast.EventData = value.value
  final case class Public(value: _root_.idltest.ast.PublicData) extends AliasedAdt
  implicit def intoPublic(value: _root_.idltest.ast.PublicData): AliasedAdt = AliasedAdt.Public(value)
  implicit def fromPublic(value: AliasedAdt.Public): _root_.idltest.ast.PublicData = value.value
}
       