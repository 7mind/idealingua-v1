package idltest.syntax



trait TestMixin extends Any with izumi.idealingua.runtime.model.IDLGeneratedType

trait TestMixinCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTestMixin: Encoder.AsObject[TestMixin] = Encoder.AsObject.instance {
    case v: TestMixin.Struct =>
      Map("idltest.syntax.TestMixin.Struct" -> v).asJsonObject
  }
  implicit val decodeTestMixin: Decoder[TestMixin] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.syntax.TestMixin.Struct" =>
        value.as[TestMixin.Struct]
      case _ =>
        val cname = "idltest.syntax.TestMixin"
        val alts = List("idltest.syntax.TestMixin.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TestMixin extends TestMixinCirce {
  def apply() = Struct()
  final case class Struct() extends TestMixin
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TestMixin.StructCirce {
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TestMixin.Struct, TestMixin.Struct] {
      override def convert(_value: TestMixin.Struct): TestMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestMixin.Struct()
      }
    }
    implicit object Struct_upcast_TestMixin extends izumi.idealingua.runtime.IRTCast[TestMixin.Struct, TestMixin] {
      override def convert(_value: TestMixin.Struct): TestMixin = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestMixin.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: TestMixin.Struct) extends izumi.idealingua.runtime.IRTConversions[TestMixin.Struct]
  }
  implicit object TestMixin_downcast_extend_TestMixinStruct extends izumi.idealingua.runtime.IRTExtend[TestMixin, TestMixin.Struct] {
    class Call(private val _value: TestMixin) extends AnyVal {
      def using(): TestMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestMixin.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestMixin): Call = new Call(_value)
  }
  implicit object TestMixin_upcast_TestMixin extends izumi.idealingua.runtime.IRTCast[TestMixin, TestMixin] {
    override def convert(_value: TestMixin): TestMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestMixin.Struct()
    }
  }
  implicit class TestMixinExtensions(override protected val _value: TestMixin) extends izumi.idealingua.runtime.IRTConversions[TestMixin]
}
       