package idltest.json



final case class JLString(value: String) extends AnyVal with JLString.Defn

trait JLStringCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeJLString: Encoder.AsObject[JLString] = Encoder.forProduct1[JLString, String]("value")((v: JLString) => v.value)
  implicit val decodeJLString: Decoder[JLString] = Decoder.forProduct1[JLString, String]("value")((d: String) => new JLString(d))
}

object JLString extends JLStringCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
  def apply(value: String): JLString = {
    new JLString(value = value)
  }
  def apply(defn: JLString.Defn): JLString = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new JLString(value = defn.value)
  }
  implicit object JLString_upcast_JLString extends izumi.idealingua.runtime.IRTCast[JLString, JLString] {
    override def convert(_value: JLString): JLString = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      JLString(value = _value.value)
    }
  }
  implicit class JLStringExtensions(override protected val _value: JLString) extends izumi.idealingua.runtime.IRTConversions[JLString]
}
       