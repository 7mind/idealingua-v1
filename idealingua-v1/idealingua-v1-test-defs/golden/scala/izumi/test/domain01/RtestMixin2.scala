package izumi.test.domain01



trait RtestMixin2 extends izumi.idealingua.runtime.model.IDLGeneratedType { def b: RTestMixin }

trait RtestMixin2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeRtestMixin2: Encoder.AsObject[RtestMixin2] = Encoder.AsObject.instance {
    case v: RTestObject2 =>
      Map("izumi.test.domain01.RTestObject2" -> v).asJsonObject
    case v: RTestObject1 =>
      Map("izumi.test.domain01.RTestObject1" -> v).asJsonObject
    case v: RtestMixin2.Struct =>
      Map("izumi.test.domain01.RtestMixin2.Struct" -> v).asJsonObject
  }
  implicit val decodeRtestMixin2: Decoder[RtestMixin2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.RTestObject2" =>
        value.as[RTestObject2]
      case "izumi.test.domain01.RTestObject1" =>
        value.as[RTestObject1]
      case "izumi.test.domain01.RtestMixin2.Struct" =>
        value.as[RtestMixin2.Struct]
      case _ =>
        val cname = "izumi.test.domain01.RtestMixin2"
        val alts = List("izumi.test.domain01.RTestObject2", "izumi.test.domain01.RTestObject1", "izumi.test.domain01.RtestMixin2.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object RtestMixin2 extends RtestMixin2Circe {
  def apply(b: RTestMixin) = Struct(b)
  final case class Struct(b: RTestMixin) extends RtestMixin2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends RtestMixin2.StructCirce {
    def apply(rtestmixin2: RtestMixin2): RtestMixin2.Struct = {
      assert(rtestmixin2.asInstanceOf[_root_.scala.AnyRef] ne null)
      new RtestMixin2.Struct(b = rtestmixin2.b)
    }
    implicit object Struct_cast_into_RTestObject1 extends izumi.idealingua.runtime.IRTCast[RtestMixin2.Struct, RTestObject1] {
      override def convert(_value: RtestMixin2.Struct): RTestObject1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestObject1(b = _value.b)
      }
    }
    implicit object Struct_cast_into_RTestObject2 extends izumi.idealingua.runtime.IRTCast[RtestMixin2.Struct, RTestObject2] {
      override def convert(_value: RtestMixin2.Struct): RTestObject2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestObject2(b = _value.b)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[RtestMixin2.Struct, RtestMixin2.Struct] {
      override def convert(_value: RtestMixin2.Struct): RtestMixin2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RtestMixin2.Struct(b = _value.b)
      }
    }
    implicit object Struct_upcast_RtestMixin2 extends izumi.idealingua.runtime.IRTCast[RtestMixin2.Struct, RtestMixin2] {
      override def convert(_value: RtestMixin2.Struct): RtestMixin2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RtestMixin2.Struct(b = _value.b)
      }
    }
    implicit class StructExtensions(override protected val _value: RtestMixin2.Struct) extends izumi.idealingua.runtime.IRTConversions[RtestMixin2.Struct]
  }
  implicit object RtestMixin2_downcast_extend_RTestObject2 extends izumi.idealingua.runtime.IRTExtend[RtestMixin2, RTestObject2] {
    class Call(private val _value: RtestMixin2) extends AnyVal {
      def using(): RTestObject2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestObject2(b = _value.b)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: RtestMixin2): Call = new Call(_value)
  }
  implicit object RtestMixin2_downcast_extend_RTestObject1 extends izumi.idealingua.runtime.IRTExtend[RtestMixin2, RTestObject1] {
    class Call(private val _value: RtestMixin2) extends AnyVal {
      def using(): RTestObject1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestObject1(b = _value.b)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: RtestMixin2): Call = new Call(_value)
  }
  implicit object RtestMixin2_downcast_extend_RtestMixin2Struct extends izumi.idealingua.runtime.IRTExtend[RtestMixin2, RtestMixin2.Struct] {
    class Call(private val _value: RtestMixin2) extends AnyVal {
      def using(): RtestMixin2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RtestMixin2.Struct(b = _value.b)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: RtestMixin2): Call = new Call(_value)
  }
  implicit object RtestMixin2_upcast_RtestMixin2 extends izumi.idealingua.runtime.IRTCast[RtestMixin2, RtestMixin2] {
    override def convert(_value: RtestMixin2): RtestMixin2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RtestMixin2.Struct(b = _value.b)
    }
  }
  implicit class RtestMixin2Extensions(override protected val _value: RtestMixin2) extends izumi.idealingua.runtime.IRTConversions[RtestMixin2]
}
       