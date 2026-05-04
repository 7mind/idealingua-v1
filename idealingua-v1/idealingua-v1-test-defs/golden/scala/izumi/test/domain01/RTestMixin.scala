package izumi.test.domain01



trait RTestMixin extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: RTestEnum }

trait RTestMixinCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeRTestMixin: Encoder.AsObject[RTestMixin] = Encoder.AsObject.instance {
    case v: RTestMixin.Struct =>
      Map("izumi.test.domain01.RTestMixin.Struct" -> v).asJsonObject
  }
  implicit val decodeRTestMixin: Decoder[RTestMixin] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.RTestMixin.Struct" =>
        value.as[RTestMixin.Struct]
      case _ =>
        val cname = "izumi.test.domain01.RTestMixin"
        val alts = List("izumi.test.domain01.RTestMixin.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object RTestMixin extends RTestMixinCirce {
  def apply(a: RTestEnum) = Struct(a)
  final case class Struct(a: RTestEnum) extends AnyVal with RTestMixin
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, RTestEnum]("a")((v: Struct) => v.a)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, RTestEnum]("a")((d: RTestEnum) => new Struct(d))
  }
  object Struct extends RTestMixin.StructCirce {
    def apply(rtestmixin: RTestMixin): RTestMixin.Struct = {
      assert(rtestmixin.asInstanceOf[_root_.scala.AnyRef] ne null)
      new RTestMixin.Struct(a = rtestmixin.a)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[RTestMixin.Struct, RTestMixin.Struct] {
      override def convert(_value: RTestMixin.Struct): RTestMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestMixin.Struct(a = _value.a)
      }
    }
    implicit object Struct_upcast_RTestMixin extends izumi.idealingua.runtime.IRTCast[RTestMixin.Struct, RTestMixin] {
      override def convert(_value: RTestMixin.Struct): RTestMixin = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestMixin.Struct(a = _value.a)
      }
    }
    implicit class StructExtensions(override protected val _value: RTestMixin.Struct) extends izumi.idealingua.runtime.IRTConversions[RTestMixin.Struct]
  }
  implicit object RTestMixin_downcast_extend_RTestMixinStruct extends izumi.idealingua.runtime.IRTExtend[RTestMixin, RTestMixin.Struct] {
    class Call(private val _value: RTestMixin) extends AnyVal {
      def using(): RTestMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        RTestMixin.Struct(a = _value.a)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: RTestMixin): Call = new Call(_value)
  }
  implicit object RTestMixin_upcast_RTestMixin extends izumi.idealingua.runtime.IRTCast[RTestMixin, RTestMixin] {
    override def convert(_value: RTestMixin): RTestMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RTestMixin.Struct(a = _value.a)
    }
  }
  implicit class RTestMixinExtensions(override protected val _value: RTestMixin) extends izumi.idealingua.runtime.IRTConversions[RTestMixin]
}
       