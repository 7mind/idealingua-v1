package idltest.phase



final case class Name_view(bytes: Long, name: String, relatives: List[Name]) extends Name with Name_view.Defn

trait Name_viewCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeName_view: Encoder.AsObject[Name_view] = deriveEncoder[Name_view]
  implicit val decodeName_view: Decoder[Name_view] = deriveDecoder[Name_view]
}

object Name_view extends Name_viewCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def bytes: Long
    def name: String
    def relatives: List[Name]
  }
  def apply(lengthinbytes: LengthInBytes, name: Name, relatives: List[Name]): Name_view = {
    assert((name.asInstanceOf[_root_.scala.AnyRef] ne null) && (lengthinbytes.asInstanceOf[_root_.scala.AnyRef] ne null))
    new Name_view(bytes = lengthinbytes.bytes, name = name.name, relatives = relatives)
  }
  def apply(defn: Name_view.Defn): Name_view = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Name_view(bytes = defn.bytes, name = defn.name, relatives = defn.relatives)
  }
  implicit object Name_view_upcast_Name_view extends izumi.idealingua.runtime.IRTCast[Name_view, Name_view] {
    override def convert(_value: Name_view): Name_view = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_view(bytes = _value.bytes, name = _value.name, relatives = _value.relatives)
    }
  }
  implicit object Name_view_upcast_Name extends izumi.idealingua.runtime.IRTCast[Name_view, Name] {
    override def convert(_value: Name_view): Name = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name.Struct(name = _value.name)
    }
  }
  implicit object Name_view_upcast_Name_stored_ extends izumi.idealingua.runtime.IRTCast[Name_view, Name_stored_] {
    override def convert(_value: Name_view): Name_stored_ = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Name_stored_.Struct(name = _value.name, bytes = _value.bytes)
    }
  }
  implicit object Name_view_upcast_LengthInBytes extends izumi.idealingua.runtime.IRTCast[Name_view, LengthInBytes] {
    override def convert(_value: Name_view): LengthInBytes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      LengthInBytes.Struct(bytes = _value.bytes)
    }
  }
  implicit class Name_viewExtensions(override protected val _value: Name_view) extends izumi.idealingua.runtime.IRTConversions[Name_view]
}
       