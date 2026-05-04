package izumi.test.domain01



trait CommonFailure extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def code: Int }

trait CommonFailureCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeCommonFailure: Encoder.AsObject[CommonFailure] = Encoder.AsObject.instance {
    case v: CommonFailure.Struct =>
      Map("izumi.test.domain01.CommonFailure.Struct" -> v).asJsonObject
    case v: BasicFailure =>
      Map("izumi.test.domain01.BasicFailure" -> v).asJsonObject
  }
  implicit val decodeCommonFailure: Decoder[CommonFailure] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.CommonFailure.Struct" =>
        value.as[CommonFailure.Struct]
      case "izumi.test.domain01.BasicFailure" =>
        value.as[BasicFailure]
      case _ =>
        val cname = "izumi.test.domain01.CommonFailure"
        val alts = List("izumi.test.domain01.CommonFailure.Struct", "izumi.test.domain01.BasicFailure").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object CommonFailure extends CommonFailureCirce {
  def apply(code: Int) = Struct(code)
  final case class Struct(code: Int) extends AnyVal with CommonFailure
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Int]("code")((v: Struct) => v.code)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Int]("code")((d: Int) => new Struct(d))
  }
  object Struct extends CommonFailure.StructCirce {
    def apply(commonfailure: CommonFailure): CommonFailure.Struct = {
      assert(commonfailure.asInstanceOf[_root_.scala.AnyRef] ne null)
      new CommonFailure.Struct(code = commonfailure.code)
    }
    implicit object Struct_cast_into_BasicFailure extends izumi.idealingua.runtime.IRTCast[CommonFailure.Struct, BasicFailure] {
      override def convert(_value: CommonFailure.Struct): BasicFailure = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        BasicFailure(code = _value.code)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[CommonFailure.Struct, CommonFailure.Struct] {
      override def convert(_value: CommonFailure.Struct): CommonFailure.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CommonFailure.Struct(code = _value.code)
      }
    }
    implicit object Struct_upcast_CommonFailure extends izumi.idealingua.runtime.IRTCast[CommonFailure.Struct, CommonFailure] {
      override def convert(_value: CommonFailure.Struct): CommonFailure = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CommonFailure.Struct(code = _value.code)
      }
    }
    implicit class StructExtensions(override protected val _value: CommonFailure.Struct) extends izumi.idealingua.runtime.IRTConversions[CommonFailure.Struct]
  }
  implicit object CommonFailure_downcast_extend_CommonFailureStruct extends izumi.idealingua.runtime.IRTExtend[CommonFailure, CommonFailure.Struct] {
    class Call(private val _value: CommonFailure) extends AnyVal {
      def using(): CommonFailure.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CommonFailure.Struct(code = _value.code)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CommonFailure): Call = new Call(_value)
  }
  implicit object CommonFailure_downcast_extend_BasicFailure extends izumi.idealingua.runtime.IRTExtend[CommonFailure, BasicFailure] {
    class Call(private val _value: CommonFailure) extends AnyVal {
      def using(): BasicFailure = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        BasicFailure(code = _value.code)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CommonFailure): Call = new Call(_value)
  }
  implicit object CommonFailure_upcast_CommonFailure extends izumi.idealingua.runtime.IRTCast[CommonFailure, CommonFailure] {
    override def convert(_value: CommonFailure): CommonFailure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CommonFailure.Struct(code = _value.code)
    }
  }
  implicit class CommonFailureExtensions(override protected val _value: CommonFailure) extends izumi.idealingua.runtime.IRTConversions[CommonFailure]
}
       