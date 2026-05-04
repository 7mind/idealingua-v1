package idltest.events



sealed trait ADTType extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait ADTTypeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeADTType: Encoder.AsObject[ADTType] = Encoder.AsObject.instance {
    case v: ADTType.BranchA =>
      Map("BranchA" -> v.value).asJsonObject
    case v: ADTType.BranchB =>
      Map("BranchB" -> v.value).asJsonObject
  }
  implicit val decodeADTType: Decoder[ADTType] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "BranchA" =>
        value.as[_root_.idltest.events.BranchA].map(ADTType.BranchA.apply)
      case "BranchB" =>
        value.as[_root_.idltest.events.BranchB].map(ADTType.BranchB.apply)
      case _ =>
        val cname = "idltest.events.ADTType"
        val alts = List("BranchA", "BranchB").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object ADTType extends ADTTypeCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = ADTType
  final case class BranchA(value: _root_.idltest.events.BranchA) extends ADTType
  implicit def intoBranchA(value: _root_.idltest.events.BranchA): ADTType = ADTType.BranchA(value)
  implicit def fromBranchA(value: ADTType.BranchA): _root_.idltest.events.BranchA = value.value
  final case class BranchB(value: _root_.idltest.events.BranchB) extends ADTType
  implicit def intoBranchB(value: _root_.idltest.events.BranchB): ADTType = ADTType.BranchB(value)
  implicit def fromBranchB(value: ADTType.BranchB): _root_.idltest.events.BranchB = value.value
}
       