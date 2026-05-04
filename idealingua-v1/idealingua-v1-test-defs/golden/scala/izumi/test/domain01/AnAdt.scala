package izumi.test.domain01



sealed trait AnAdt extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AnAdtCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAnAdt: Encoder.AsObject[AnAdt] = Encoder.AsObject.instance {
    case v: AnAdt.AllTypes =>
      Map("AllTypes" -> v.value).asJsonObject
    case v: AnAdt.TestObject =>
      Map("TestObject" -> v.value).asJsonObject
    case v: AnAdt.AnotherMember =>
      Map("AnotherMember" -> v.value).asJsonObject
  }
  implicit val decodeAnAdt: Decoder[AnAdt] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "AllTypes" =>
        value.as[_root_.izumi.test.domain01.AllTypes].map(AnAdt.AllTypes.apply)
      case "TestObject" =>
        value.as[_root_.izumi.test.domain01.TestObject].map(AnAdt.TestObject.apply)
      case "AnotherMember" =>
        value.as[_root_.izumi.test.domain01.AnyValTest].map(AnAdt.AnotherMember.apply)
      case _ =>
        val cname = "izumi.test.domain01.AnAdt"
        val alts = List("AllTypes", "TestObject", "AnotherMember").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object AnAdt extends AnAdtCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = AnAdt
  final case class AllTypes(value: _root_.izumi.test.domain01.AllTypes) extends AnAdt
  implicit def intoAllTypes(value: _root_.izumi.test.domain01.AllTypes): AnAdt = AnAdt.AllTypes(value)
  implicit def fromAllTypes(value: AnAdt.AllTypes): _root_.izumi.test.domain01.AllTypes = value.value
  final case class TestObject(value: _root_.izumi.test.domain01.TestObject) extends AnAdt
  implicit def intoTestObject(value: _root_.izumi.test.domain01.TestObject): AnAdt = AnAdt.TestObject(value)
  implicit def fromTestObject(value: AnAdt.TestObject): _root_.izumi.test.domain01.TestObject = value.value
  final case class AnotherMember(value: _root_.izumi.test.domain01.AnyValTest) extends AnAdt
  implicit def intoAnotherMember(value: _root_.izumi.test.domain01.AnyValTest): AnAdt = AnAdt.AnotherMember(value)
  implicit def fromAnotherMember(value: AnAdt.AnotherMember): _root_.izumi.test.domain01.AnyValTest = value.value
}
       