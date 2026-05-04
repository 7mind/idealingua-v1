package idltest.phase



trait Name_stored_ extends izumi.idealingua.runtime.model.IDLGeneratedType with Name {
  def name: String
  def bytes: Long
}

trait Name_stored_Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeName_stored_ : Encoder.AsObject[Name_stored_] = Encoder.AsObject.instance {
    case v: Name_stored_.Struct =>
      Map("idltest.phase.Name_stored_.Struct" -> v).asJsonObject
    case v: Name_stored =>
      Map("idltest.phase.Name_stored" -> v).asJsonObject
  }
  implicit val decodeName_stored_ : Decoder[Name_stored_] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.phase.Name_stored_.Struct" =>
        value.as[Name_stored_.Struct]
      case "idltest.phase.Name_stored" =>
        value.as[Name_stored]
      case _ =>
        val cname = "idltest.phase.Name_stored_"
        val alts = List("idltest.phase.Name_stored_.Struct", "idltest.phase.Name_stored").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Name_stored_ extends Name_stored_Circe {
  def apply(name: String, bytes: Long) = Struct(name, bytes)
  final case class Struct(name: String, bytes: Long) extends Name_stored_
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Name_stored_.StructCirce {
    def apply(name: Name, lengthinbytes: LengthInBytes): Name_stored_.Struct = {
      assert((lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null) && (name.asInstanceOf[_root_.scala.AnyRef] ne null))
      new Name_stored_.Struct(name = name.name, bytes = lengthinbytes.bytes)
    }
    implicit object Struct_cast_into_Name_stored extends izumi.idealingua.runtime.IRTCast[Name_stored_.Struct, Name_stored] {
      override def convert(_value: Name_stored_.Struct): Name_stored = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored(name = _value.name, bytes = _value.bytes)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Name_stored_.Struct, Name_stored_.Struct] {
      override def convert(_value: Name_stored_.Struct): Name_stored_.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
      }
    }
    implicit object Struct_upcast_Name_stored_ extends izumi.idealingua.runtime.IRTCast[Name_stored_.Struct, Name_stored_] {
      override def convert(_value: Name_stored_.Struct): Name_stored_ = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
      }
    }
    implicit object Struct_upcast_Name extends izumi.idealingua.runtime.IRTCast[Name_stored_.Struct, Name] {
      override def convert(_value: Name_stored_.Struct): Name = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name.Struct(name = _value.name)
      }
    }
    implicit class StructExtensions(override protected val _value: Name_stored_.Struct) extends izumi.idealingua.runtime.IRTConversions[Name_stored_.Struct]
  }
  implicit object Name_stored__downcast_extend_Name_stored_Struct extends izumi.idealingua.runtime.IRTExtend[Name_stored_, Name_stored_.Struct] {
    class Call(private val _value: Name_stored_) extends AnyVal {
      def using(): Name_stored_.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name_stored_): Call = new Call(_value)
  }
  implicit object Name_stored__downcast_extend_Name_view extends izumi.idealingua.runtime.IRTExtend[Name_stored_, Name_view] {
    class Call(private val _value: Name_stored_) extends AnyVal {
      def using(name: String, relatives: List[Name]): Name_view = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_view(bytes = _value.bytes, name = name, relatives = relatives)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name_stored_): Call = new Call(_value)
  }
  implicit object Name_stored__downcast_extend_Name_stored extends izumi.idealingua.runtime.IRTExtend[Name_stored_, Name_stored] {
    class Call(private val _value: Name_stored_) extends AnyVal {
      def using(): Name_stored = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored(name = _value.name, bytes = _value.bytes)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name_stored_): Call = new Call(_value)
  }
  implicit object Name_stored__upcast_Name_stored_ extends izumi.idealingua.runtime.IRTCast[Name_stored_, Name_stored_] {
    override def convert(_value: Name_stored_): Name_stored_ = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
    }
  }
  implicit object Name_stored__upcast_Name extends izumi.idealingua.runtime.IRTCast[Name_stored_, Name] {
    override def convert(_value: Name_stored_): Name = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name.Struct(name = _value.name)
    }
  }
  implicit object Name_stored__upcast_LengthInBytes extends izumi.idealingua.runtime.IRTCast[Name_stored_, LengthInBytes] {
    override def convert(_value: Name_stored_): LengthInBytes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      LengthInBytes.Struct(bytes = _value.bytes)
    }
  }
  implicit class Name_stored_Extensions(override protected val _value: Name_stored_) extends izumi.idealingua.runtime.IRTConversions[Name_stored_]
}
       