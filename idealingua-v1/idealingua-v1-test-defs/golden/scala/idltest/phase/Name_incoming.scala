package idltest.phase



final case class Name_incoming(name: String) extends AnyVal with Name with Name_incoming.Defn

trait Name_incomingCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeName_incoming: Encoder.AsObject[Name_incoming] = Encoder.forProduct1[Name_incoming, String]("name")((v: Name_incoming) => v.name)
  implicit val decodeName_incoming: Decoder[Name_incoming] = Decoder.forProduct1[Name_incoming, String]("name")((d: String) => new Name_incoming(d))
}

object Name_incoming extends Name_incomingCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def name: String }
  def apply(name: Name): Name_incoming = {
    assert(name.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Name_incoming(name = name.name)
  }
  def apply(defn: Name_incoming.Defn): Name_incoming = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Name_incoming(name = defn.name)
  }
  implicit object Name_incoming_cast_into_NameStruct extends izumi.idealingua.runtime.IRTCast[Name_incoming, Name.Struct] {
    override def convert(_value: Name_incoming): Name.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name.Struct(name = _value.name)
    }
  }
  implicit object Name_incoming_upcast_Name_incoming extends izumi.idealingua.runtime.IRTCast[Name_incoming, Name_incoming] {
    override def convert(_value: Name_incoming): Name_incoming = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_incoming(name = _value.name)
    }
  }
  implicit object Name_incoming_upcast_Name extends izumi.idealingua.runtime.IRTCast[Name_incoming, Name] {
    override def convert(_value: Name_incoming): Name = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name.Struct(name = _value.name)
    }
  }
  implicit class Name_incomingExtensions(override protected val _value: Name_incoming) extends izumi.idealingua.runtime.IRTConversions[Name_incoming]
}
       