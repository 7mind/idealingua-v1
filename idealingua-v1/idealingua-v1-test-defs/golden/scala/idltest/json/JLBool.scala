package idltest.json



final case class JLBool(value: Boolean) extends AnyVal with JLBool.Defn

trait JLBoolCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeJLBool: Encoder.AsObject[JLBool] = Encoder.forProduct1[JLBool, Boolean]("value")((v: JLBool) => v.value)
  implicit val decodeJLBool: Decoder[JLBool] = Decoder.forProduct1[JLBool, Boolean]("value")((d: Boolean) => new JLBool(d))
}

object JLBool extends JLBoolCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Boolean }
  def apply(value: Boolean): JLBool = {
    new JLBool(value = value)
  }
  def apply(defn: JLBool.Defn): JLBool = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new JLBool(value = defn.value)
  }
  implicit object JLBool_upcast_JLBool extends izumi.idealingua.runtime.IRTCast[JLBool, JLBool] {
    override def convert(_value: JLBool): JLBool = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      JLBool(value = _value.value)
    }
  }
  implicit class JLBoolExtensions(override protected val _value: JLBool) extends izumi.idealingua.runtime.IRTConversions[JLBool]
}
       