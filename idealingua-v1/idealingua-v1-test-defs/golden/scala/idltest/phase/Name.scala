package idltest.phase



trait Name extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def name: String }

trait NameCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeName: Encoder.AsObject[Name] = Encoder.AsObject.instance {
    case v: Name_stored_.Struct =>
      Map("idltest.phase.Name_stored_.Struct" -> v).asJsonObject
    case v: Name_view =>
      Map("idltest.phase.Name_view" -> v).asJsonObject
    case v: Name_stored =>
      Map("idltest.phase.Name_stored" -> v).asJsonObject
    case v: Name.Struct =>
      Map("idltest.phase.Name.Struct" -> v).asJsonObject
    case v: Name_incoming =>
      Map("idltest.phase.Name_incoming" -> v).asJsonObject
  }
  implicit val decodeName: Decoder[Name] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.phase.Name_stored_.Struct" =>
        value.as[Name_stored_.Struct]
      case "idltest.phase.Name_view" =>
        value.as[Name_view]
      case "idltest.phase.Name_stored" =>
        value.as[Name_stored]
      case "idltest.phase.Name.Struct" =>
        value.as[Name.Struct]
      case "idltest.phase.Name_incoming" =>
        value.as[Name_incoming]
      case _ =>
        val cname = "idltest.phase.Name"
        val alts = List("idltest.phase.Name_stored_.Struct", "idltest.phase.Name_view", "idltest.phase.Name_stored", "idltest.phase.Name.Struct", "idltest.phase.Name_incoming").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Name extends NameCirce {
  def apply(name: String) = Struct(name)
  final case class Struct(name: String) extends AnyVal with Name
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("name")((v: Struct) => v.name)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("name")((d: String) => new Struct(d))
  }
  object Struct extends Name.StructCirce {
    def apply(name: Name): Name.Struct = {
      assert(name.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Name.Struct(name = name.name)
    }
    implicit object Struct_cast_into_Name_incoming extends izumi.idealingua.runtime.IRTCast[Name.Struct, Name_incoming] {
      override def convert(_value: Name.Struct): Name_incoming = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_incoming(name = _value.name)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Name.Struct, Name.Struct] {
      override def convert(_value: Name.Struct): Name.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name.Struct(name = _value.name)
      }
    }
    implicit object Struct_upcast_Name extends izumi.idealingua.runtime.IRTCast[Name.Struct, Name] {
      override def convert(_value: Name.Struct): Name = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name.Struct(name = _value.name)
      }
    }
    implicit class StructExtensions(override protected val _value: Name.Struct) extends izumi.idealingua.runtime.IRTConversions[Name.Struct]
  }
  implicit object Name_downcast_extend_Name_stored_Struct extends izumi.idealingua.runtime.IRTExtend[Name, Name_stored_.Struct] {
    class Call(private val _value: Name) extends AnyVal {
      def using(lengthinbytes: LengthInBytes): Name_stored_.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored_.Struct(name = _value.name, bytes = lengthinbytes.bytes)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name): Call = new Call(_value)
  }
  implicit object Name_downcast_extend_Name_view extends izumi.idealingua.runtime.IRTExtend[Name, Name_view] {
    class Call(private val _value: Name) extends AnyVal {
      def using(name: String, relatives: List[Name], lengthinbytes: LengthInBytes): Name_view = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_view(name = name, relatives = relatives, bytes = lengthinbytes.bytes)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name): Call = new Call(_value)
  }
  implicit object Name_downcast_extend_Name_stored extends izumi.idealingua.runtime.IRTExtend[Name, Name_stored] {
    class Call(private val _value: Name) extends AnyVal {
      def using(lengthinbytes: LengthInBytes): Name_stored = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_stored(name = _value.name, bytes = lengthinbytes.bytes)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name): Call = new Call(_value)
  }
  implicit object Name_downcast_extend_NameStruct extends izumi.idealingua.runtime.IRTExtend[Name, Name.Struct] {
    class Call(private val _value: Name) extends AnyVal {
      def using(): Name.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name.Struct(name = _value.name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name): Call = new Call(_value)
  }
  implicit object Name_downcast_extend_Name_incoming extends izumi.idealingua.runtime.IRTExtend[Name, Name_incoming] {
    class Call(private val _value: Name) extends AnyVal {
      def using(): Name_incoming = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Name_incoming(name = _value.name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Name): Call = new Call(_value)
  }
  implicit object Name_upcast_Name extends izumi.idealingua.runtime.IRTCast[Name, Name] {
    override def convert(_value: Name): Name = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name.Struct(name = _value.name)
    }
  }
  implicit class NameExtensions(override protected val _value: Name) extends izumi.idealingua.runtime.IRTConversions[Name]
}
       