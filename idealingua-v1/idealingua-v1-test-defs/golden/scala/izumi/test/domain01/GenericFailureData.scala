package izumi.test.domain01



trait GenericFailureData extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def message: String
  def diagnostics: Option[String]
  def reserved: Map[String, String]
}

trait GenericFailureDataCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeGenericFailureData: Encoder.AsObject[GenericFailureData] = Encoder.AsObject.instance {
    case v: GenericFailureData.Struct =>
      Map("izumi.test.domain01.GenericFailureData.Struct" -> v).asJsonObject
  }
  implicit val decodeGenericFailureData: Decoder[GenericFailureData] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.GenericFailureData.Struct" =>
        value.as[GenericFailureData.Struct]
      case _ =>
        val cname = "izumi.test.domain01.GenericFailureData"
        val alts = List("izumi.test.domain01.GenericFailureData.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object GenericFailureData extends GenericFailureDataCirce {
  def apply(message: String, diagnostics: Option[String], reserved: Map[String, String]) = Struct(message, diagnostics, reserved)
  final case class Struct(message: String, diagnostics: Option[String], reserved: Map[String, String]) extends GenericFailureData
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends GenericFailureData.StructCirce {
    def apply(genericfailuredata: GenericFailureData): GenericFailureData.Struct = {
      assert(genericfailuredata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new GenericFailureData.Struct(message = genericfailuredata.message, diagnostics = genericfailuredata.diagnostics, reserved = genericfailuredata.reserved)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[GenericFailureData.Struct, GenericFailureData.Struct] {
      override def convert(_value: GenericFailureData.Struct): GenericFailureData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        GenericFailureData.Struct(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved)
      }
    }
    implicit object Struct_upcast_GenericFailureData extends izumi.idealingua.runtime.IRTCast[GenericFailureData.Struct, GenericFailureData] {
      override def convert(_value: GenericFailureData.Struct): GenericFailureData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        GenericFailureData.Struct(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved)
      }
    }
    implicit class StructExtensions(override protected val _value: GenericFailureData.Struct) extends izumi.idealingua.runtime.IRTConversions[GenericFailureData.Struct]
  }
  implicit object GenericFailureData_downcast_extend_GenericFailure extends izumi.idealingua.runtime.IRTExtend[GenericFailureData, GenericFailure] {
    class Call(private val _value: GenericFailureData) extends AnyVal {
      def using(code: GenericFailureCode): GenericFailure = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(code.asInstanceOf[_root_.scala.AnyRef] ne null)
        GenericFailure(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved, code = code)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: GenericFailureData): Call = new Call(_value)
  }
  implicit object GenericFailureData_downcast_extend_GenericFailureDataStruct extends izumi.idealingua.runtime.IRTExtend[GenericFailureData, GenericFailureData.Struct] {
    class Call(private val _value: GenericFailureData) extends AnyVal {
      def using(): GenericFailureData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        GenericFailureData.Struct(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: GenericFailureData): Call = new Call(_value)
  }
  implicit object GenericFailureData_upcast_GenericFailureData extends izumi.idealingua.runtime.IRTCast[GenericFailureData, GenericFailureData] {
    override def convert(_value: GenericFailureData): GenericFailureData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      GenericFailureData.Struct(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved)
    }
  }
  implicit class GenericFailureDataExtensions(override protected val _value: GenericFailureData) extends izumi.idealingua.runtime.IRTConversions[GenericFailureData]
}
       