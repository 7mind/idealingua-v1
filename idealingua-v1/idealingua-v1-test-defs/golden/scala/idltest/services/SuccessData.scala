package idltest.services



trait SuccessData extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def greeting: String }

trait SuccessDataCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeSuccessData: Encoder.AsObject[SuccessData] = Encoder.AsObject.instance {
    case v: SuccessDataData =>
      Map("idltest.services.SuccessDataData" -> v).asJsonObject
    case v: SuccessData.Struct =>
      Map("idltest.services.SuccessData.Struct" -> v).asJsonObject
  }
  implicit val decodeSuccessData: Decoder[SuccessData] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.services.SuccessDataData" =>
        value.as[SuccessDataData]
      case "idltest.services.SuccessData.Struct" =>
        value.as[SuccessData.Struct]
      case _ =>
        val cname = "idltest.services.SuccessData"
        val alts = List("idltest.services.SuccessDataData", "idltest.services.SuccessData.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object SuccessData extends SuccessDataCirce {
  def apply(greeting: String) = Struct(greeting)
  final case class Struct(greeting: String) extends AnyVal with SuccessData
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("greeting")((v: Struct) => v.greeting)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("greeting")((d: String) => new Struct(d))
  }
  object Struct extends SuccessData.StructCirce {
    def apply(successdata: SuccessData): SuccessData.Struct = {
      assert(successdata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new SuccessData.Struct(greeting = successdata.greeting)
    }
    implicit object Struct_cast_into_SuccessDataData extends izumi.idealingua.runtime.IRTCast[SuccessData.Struct, SuccessDataData] {
      override def convert(_value: SuccessData.Struct): SuccessDataData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessDataData(greeting = _value.greeting)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[SuccessData.Struct, SuccessData.Struct] {
      override def convert(_value: SuccessData.Struct): SuccessData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessData.Struct(greeting = _value.greeting)
      }
    }
    implicit object Struct_upcast_SuccessData extends izumi.idealingua.runtime.IRTCast[SuccessData.Struct, SuccessData] {
      override def convert(_value: SuccessData.Struct): SuccessData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessData.Struct(greeting = _value.greeting)
      }
    }
    implicit class StructExtensions(override protected val _value: SuccessData.Struct) extends izumi.idealingua.runtime.IRTConversions[SuccessData.Struct]
  }
  implicit object SuccessData_downcast_extend_SuccessDataData extends izumi.idealingua.runtime.IRTExtend[SuccessData, SuccessDataData] {
    class Call(private val _value: SuccessData) extends AnyVal {
      def using(): SuccessDataData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessDataData(greeting = _value.greeting)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SuccessData): Call = new Call(_value)
  }
  implicit object SuccessData_downcast_extend_SuccessDataStruct extends izumi.idealingua.runtime.IRTExtend[SuccessData, SuccessData.Struct] {
    class Call(private val _value: SuccessData) extends AnyVal {
      def using(): SuccessData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessData.Struct(greeting = _value.greeting)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SuccessData): Call = new Call(_value)
  }
  implicit object SuccessData_upcast_SuccessData extends izumi.idealingua.runtime.IRTCast[SuccessData, SuccessData] {
    override def convert(_value: SuccessData): SuccessData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SuccessData.Struct(greeting = _value.greeting)
    }
  }
  implicit class SuccessDataExtensions(override protected val _value: SuccessData) extends izumi.idealingua.runtime.IRTConversions[SuccessData]
}
       