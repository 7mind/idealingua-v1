package izumi.test.domain01



trait AnyValTest extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def boolField: Boolean }

trait AnyValTestCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAnyValTest: Encoder.AsObject[AnyValTest] = Encoder.AsObject.instance {
    case v: AnyValTest.Struct =>
      Map("izumi.test.domain01.AnyValTest.Struct" -> v).asJsonObject
  }
  implicit val decodeAnyValTest: Decoder[AnyValTest] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.AnyValTest.Struct" =>
        value.as[AnyValTest.Struct]
      case _ =>
        val cname = "izumi.test.domain01.AnyValTest"
        val alts = List("izumi.test.domain01.AnyValTest.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object AnyValTest extends AnyValTestCirce {
  def apply(boolField: Boolean) = Struct(boolField)
  final case class Struct(boolField: Boolean) extends AnyVal with AnyValTest
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Boolean]("boolField")((v: Struct) => v.boolField)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Boolean]("boolField")((d: Boolean) => new Struct(d))
  }
  object Struct extends AnyValTest.StructCirce {
    def apply(anyvaltest: AnyValTest): AnyValTest.Struct = {
      assert(anyvaltest.asInstanceOf[_root_.scala.AnyRef] ne null)
      new AnyValTest.Struct(boolField = anyvaltest.boolField)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[AnyValTest.Struct, AnyValTest.Struct] {
      override def convert(_value: AnyValTest.Struct): AnyValTest.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnyValTest.Struct(boolField = _value.boolField)
      }
    }
    implicit object Struct_upcast_AnyValTest extends izumi.idealingua.runtime.IRTCast[AnyValTest.Struct, AnyValTest] {
      override def convert(_value: AnyValTest.Struct): AnyValTest = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnyValTest.Struct(boolField = _value.boolField)
      }
    }
    implicit class StructExtensions(override protected val _value: AnyValTest.Struct) extends izumi.idealingua.runtime.IRTConversions[AnyValTest.Struct]
  }
  implicit object AnyValTest_downcast_extend_AnyValTestStruct extends izumi.idealingua.runtime.IRTExtend[AnyValTest, AnyValTest.Struct] {
    class Call(private val _value: AnyValTest) extends AnyVal {
      def using(): AnyValTest.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnyValTest.Struct(boolField = _value.boolField)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AnyValTest): Call = new Call(_value)
  }
  implicit object AnyValTest_upcast_AnyValTest extends izumi.idealingua.runtime.IRTCast[AnyValTest, AnyValTest] {
    override def convert(_value: AnyValTest): AnyValTest = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AnyValTest.Struct(boolField = _value.boolField)
    }
  }
  implicit class AnyValTestExtensions(override protected val _value: AnyValTest) extends izumi.idealingua.runtime.IRTConversions[AnyValTest]
}
       