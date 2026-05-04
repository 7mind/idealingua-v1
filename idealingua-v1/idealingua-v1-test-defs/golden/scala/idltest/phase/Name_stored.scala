package idltest.phase



final case class Name_stored(name: String, bytes: Long) extends Name_stored_ with Name_stored.Defn

trait Name_storedCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeName_stored: Encoder.AsObject[Name_stored] = deriveEncoder[Name_stored]
  implicit val decodeName_stored: Decoder[Name_stored] = deriveDecoder[Name_stored]
}

object Name_stored extends Name_storedCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def name: String
    def bytes: Long
  }
  def apply(name: Name, lengthinbytes: LengthInBytes): Name_stored = {
    assert((lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null) && (name.asInstanceOf[_root_.scala.AnyRef] ne null))
    new Name_stored(name = name.name, bytes = lengthinbytes.bytes)
  }
  def apply(defn: Name_stored.Defn): Name_stored = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Name_stored(name = defn.name, bytes = defn.bytes)
  }
  implicit object Name_stored_cast_into_Name_stored_Struct extends izumi.idealingua.runtime.IRTCast[Name_stored, Name_stored_.Struct] {
    override def convert(_value: Name_stored): Name_stored_.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
    }
  }
  implicit object Name_stored_upcast_Name_stored extends izumi.idealingua.runtime.IRTCast[Name_stored, Name_stored] {
    override def convert(_value: Name_stored): Name_stored = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_stored(name = _value.name, bytes = _value.bytes)
    }
  }
  implicit object Name_stored_upcast_Name_stored_ extends izumi.idealingua.runtime.IRTCast[Name_stored, Name_stored_] {
    override def convert(_value: Name_stored): Name_stored_ = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
    }
  }
  implicit object Name_stored_upcast_Name extends izumi.idealingua.runtime.IRTCast[Name_stored, Name] {
    override def convert(_value: Name_stored): Name = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name.Struct(name = _value.name)
    }
  }
  implicit class Name_storedExtensions(override protected val _value: Name_stored) extends izumi.idealingua.runtime.IRTConversions[Name_stored]
}
       