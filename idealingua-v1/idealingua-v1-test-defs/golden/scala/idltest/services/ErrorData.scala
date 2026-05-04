package idltest.services



trait ErrorData extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def message: String }

trait ErrorDataCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeErrorData: Encoder.AsObject[ErrorData] = Encoder.AsObject.instance {
    case v: ErrorData.Struct =>
      Map("idltest.services.ErrorData.Struct" -> v).asJsonObject
  }
  implicit val decodeErrorData: Decoder[ErrorData] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.services.ErrorData.Struct" =>
        value.as[ErrorData.Struct]
      case _ =>
        val cname = "idltest.services.ErrorData"
        val alts = List("idltest.services.ErrorData.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object ErrorData extends ErrorDataCirce {
  def apply(message: String) = Struct(message)
  final case class Struct(message: String) extends AnyVal with ErrorData
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("message")((v: Struct) => v.message)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("message")((d: String) => new Struct(d))
  }
  object Struct extends ErrorData.StructCirce {
    def apply(errordata: ErrorData): ErrorData.Struct = {
      assert(errordata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ErrorData.Struct(message = errordata.message)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[ErrorData.Struct, ErrorData.Struct] {
      override def convert(_value: ErrorData.Struct): ErrorData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ErrorData.Struct(message = _value.message)
      }
    }
    implicit object Struct_upcast_ErrorData extends izumi.idealingua.runtime.IRTCast[ErrorData.Struct, ErrorData] {
      override def convert(_value: ErrorData.Struct): ErrorData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ErrorData.Struct(message = _value.message)
      }
    }
    implicit class StructExtensions(override protected val _value: ErrorData.Struct) extends izumi.idealingua.runtime.IRTConversions[ErrorData.Struct]
  }
  implicit object ErrorData_downcast_extend_ErrorDataStruct extends izumi.idealingua.runtime.IRTExtend[ErrorData, ErrorData.Struct] {
    class Call(private val _value: ErrorData) extends AnyVal {
      def using(): ErrorData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ErrorData.Struct(message = _value.message)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ErrorData): Call = new Call(_value)
  }
  implicit object ErrorData_upcast_ErrorData extends izumi.idealingua.runtime.IRTCast[ErrorData, ErrorData] {
    override def convert(_value: ErrorData): ErrorData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ErrorData.Struct(message = _value.message)
    }
  }
  implicit class ErrorDataExtensions(override protected val _value: ErrorData) extends izumi.idealingua.runtime.IRTConversions[ErrorData]
}
       