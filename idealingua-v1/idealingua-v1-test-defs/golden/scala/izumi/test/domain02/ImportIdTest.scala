package izumi.test.domain02



trait ImportIdTest extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def id: izumi.test.domain01.ImportAppId
  def fail: izumi.test.domain01.GenericFailure
  def mix: izumi.test.domain01.GenericFailureData
}

trait ImportIdTestCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeImportIdTest: Encoder.AsObject[ImportIdTest] = Encoder.AsObject.instance {
    case v: ImportIdTest.Struct =>
      Map("izumi.test.domain02.ImportIdTest.Struct" -> v).asJsonObject
  }
  implicit val decodeImportIdTest: Decoder[ImportIdTest] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain02.ImportIdTest.Struct" =>
        value.as[ImportIdTest.Struct]
      case _ =>
        val cname = "izumi.test.domain02.ImportIdTest"
        val alts = List("izumi.test.domain02.ImportIdTest.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object ImportIdTest extends ImportIdTestCirce {
  def apply(id: izumi.test.domain01.ImportAppId, fail: izumi.test.domain01.GenericFailure, mix: izumi.test.domain01.GenericFailureData) = Struct(id, fail, mix)
  final case class Struct(id: izumi.test.domain01.ImportAppId, fail: izumi.test.domain01.GenericFailure, mix: izumi.test.domain01.GenericFailureData) extends ImportIdTest
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends ImportIdTest.StructCirce {
    def apply(importidtest: ImportIdTest): ImportIdTest.Struct = {
      assert(importidtest.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdTest.Struct(id = importidtest.id, fail = importidtest.fail, mix = importidtest.mix)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[ImportIdTest.Struct, ImportIdTest.Struct] {
      override def convert(_value: ImportIdTest.Struct): ImportIdTest.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdTest.Struct(id = _value.id, fail = _value.fail, mix = _value.mix)
      }
    }
    implicit object Struct_upcast_ImportIdTest extends izumi.idealingua.runtime.IRTCast[ImportIdTest.Struct, ImportIdTest] {
      override def convert(_value: ImportIdTest.Struct): ImportIdTest = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdTest.Struct(id = _value.id, fail = _value.fail, mix = _value.mix)
      }
    }
    implicit class StructExtensions(override protected val _value: ImportIdTest.Struct) extends izumi.idealingua.runtime.IRTConversions[ImportIdTest.Struct]
  }
  implicit object ImportIdTest_downcast_extend_ImportIdTestStruct extends izumi.idealingua.runtime.IRTExtend[ImportIdTest, ImportIdTest.Struct] {
    class Call(private val _value: ImportIdTest) extends AnyVal {
      def using(): ImportIdTest.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdTest.Struct(id = _value.id, fail = _value.fail, mix = _value.mix)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ImportIdTest): Call = new Call(_value)
  }
  implicit object ImportIdTest_upcast_ImportIdTest extends izumi.idealingua.runtime.IRTCast[ImportIdTest, ImportIdTest] {
    override def convert(_value: ImportIdTest): ImportIdTest = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ImportIdTest.Struct(id = _value.id, fail = _value.fail, mix = _value.mix)
    }
  }
  implicit class ImportIdTestExtensions(override protected val _value: ImportIdTest) extends izumi.idealingua.runtime.IRTConversions[ImportIdTest]
}
       