package mcpdemo



trait TimedFailure extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def code: Int
  def message: String
}

trait TimedFailureCirce {
  import _root_.io.circe.syntax._
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTimedFailure: Encoder.AsObject[TimedFailure] = Encoder.AsObject.instance {
    case v: ServiceError =>
      Map("mcpdemo.ServiceError" -> v).asJsonObject
    case v: TimedFailure.Struct =>
      Map("mcpdemo.TimedFailure.Struct" -> v).asJsonObject
  }
  implicit val decodeTimedFailure: Decoder[TimedFailure] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "mcpdemo.ServiceError" =>
        value.as[ServiceError]
      case "mcpdemo.TimedFailure.Struct" =>
        value.as[TimedFailure.Struct]
      case _ =>
        val cname = "mcpdemo.TimedFailure"
        val alts = List("mcpdemo.ServiceError", "mcpdemo.TimedFailure.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TimedFailure extends TimedFailureCirce {
  def apply(code: Int, message: String) = Struct(code, message)
  final case class Struct(code: Int, message: String) extends TimedFailure
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.derivation.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TimedFailure.StructCirce {
    def apply(timedfailure: TimedFailure): TimedFailure.Struct = {
      assert(timedfailure.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TimedFailure.Struct(code = timedfailure.code, message = timedfailure.message)
    }
    implicit object Struct_cast_into_ServiceError extends izumi.idealingua.runtime.IRTCast[TimedFailure.Struct, ServiceError] {
      override def convert(_value: TimedFailure.Struct): ServiceError = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ServiceError(code = _value.code, message = _value.message)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TimedFailure.Struct, TimedFailure.Struct] {
      override def convert(_value: TimedFailure.Struct): TimedFailure.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TimedFailure.Struct(code = _value.code, message = _value.message)
      }
    }
    implicit object Struct_upcast_TimedFailure extends izumi.idealingua.runtime.IRTCast[TimedFailure.Struct, TimedFailure] {
      override def convert(_value: TimedFailure.Struct): TimedFailure = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TimedFailure.Struct(code = _value.code, message = _value.message)
      }
    }
    implicit class StructExtensions(override protected val _value: TimedFailure.Struct) extends izumi.idealingua.runtime.IRTConversions[TimedFailure.Struct]
  }
  implicit object TimedFailure_downcast_extend_ServiceError extends izumi.idealingua.runtime.IRTExtend[TimedFailure, ServiceError] {
    class Call(private val _value: TimedFailure) extends AnyVal {
      def using(): ServiceError = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ServiceError(code = _value.code, message = _value.message)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TimedFailure): Call = new Call(_value)
  }
  implicit object TimedFailure_downcast_extend_TimedFailureStruct extends izumi.idealingua.runtime.IRTExtend[TimedFailure, TimedFailure.Struct] {
    class Call(private val _value: TimedFailure) extends AnyVal {
      def using(): TimedFailure.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TimedFailure.Struct(code = _value.code, message = _value.message)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TimedFailure): Call = new Call(_value)
  }
  implicit object TimedFailure_upcast_TimedFailure extends izumi.idealingua.runtime.IRTCast[TimedFailure, TimedFailure] {
    override def convert(_value: TimedFailure): TimedFailure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TimedFailure.Struct(code = _value.code, message = _value.message)
    }
  }
  implicit class TimedFailureExtensions(override protected val _value: TimedFailure) extends izumi.idealingua.runtime.IRTConversions[TimedFailure]
}
       