package idltest.phase



trait LengthInBytes extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def bytes: Long }

trait LengthInBytesCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeLengthInBytes: Encoder.AsObject[LengthInBytes] = Encoder.AsObject.instance {
    case v: LengthInBytes.Struct =>
      Map("idltest.phase.LengthInBytes.Struct" -> v).asJsonObject
  }
  implicit val decodeLengthInBytes: Decoder[LengthInBytes] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.phase.LengthInBytes.Struct" =>
        value.as[LengthInBytes.Struct]
      case _ =>
        val cname = "idltest.phase.LengthInBytes"
        val alts = List("idltest.phase.LengthInBytes.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object LengthInBytes extends LengthInBytesCirce {
  def apply(bytes: Long) = Struct(bytes)
  final case class Struct(bytes: Long) extends AnyVal with LengthInBytes
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Long]("bytes")((v: Struct) => v.bytes)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Long]("bytes")((d: Long) => new Struct(d))
  }
  object Struct extends LengthInBytes.StructCirce {
    def apply(lengthinbytes: LengthInBytes): LengthInBytes.Struct = {
      assert(lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null)
      new LengthInBytes.Struct(bytes = lengthinbytes.bytes)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[LengthInBytes.Struct, LengthInBytes.Struct] {
      override def convert(_value: LengthInBytes.Struct): LengthInBytes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LengthInBytes.Struct(bytes = _value.bytes)
      }
    }
    implicit object Struct_upcast_LengthInBytes extends izumi.idealingua.runtime.IRTCast[LengthInBytes.Struct, LengthInBytes] {
      override def convert(_value: LengthInBytes.Struct): LengthInBytes = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LengthInBytes.Struct(bytes = _value.bytes)
      }
    }
    implicit class StructExtensions(override protected val _value: LengthInBytes.Struct) extends izumi.idealingua.runtime.IRTConversions[LengthInBytes.Struct]
  }
  implicit object LengthInBytes_downcast_extend_Name_view extends izumi.idealingua.runtime.IRTExtend[LengthInBytes, Name_view] {
    class Call(private val _value: LengthInBytes) extends AnyVal {
      def using(name: String, relatives: List[Name]): Name_view = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_view(bytes = _value.bytes, name = name, relatives = relatives)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: LengthInBytes): Call = new Call(_value)
  }
  implicit object LengthInBytes_downcast_extend_LengthInBytesStruct extends izumi.idealingua.runtime.IRTExtend[LengthInBytes, LengthInBytes.Struct] {
    class Call(private val _value: LengthInBytes) extends AnyVal {
      def using(): LengthInBytes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LengthInBytes.Struct(bytes = _value.bytes)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: LengthInBytes): Call = new Call(_value)
  }
  implicit object LengthInBytes_upcast_LengthInBytes extends izumi.idealingua.runtime.IRTCast[LengthInBytes, LengthInBytes] {
    override def convert(_value: LengthInBytes): LengthInBytes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      LengthInBytes.Struct(bytes = _value.bytes)
    }
  }
  implicit class LengthInBytesExtensions(override protected val _value: LengthInBytes) extends izumi.idealingua.runtime.IRTConversions[LengthInBytes]
}
       