package idltest.json



final case class JLNumber(value: Double) extends AnyVal with JLNumber.Defn

trait JLNumberCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeJLNumber: Encoder.AsObject[JLNumber] = Encoder.forProduct1[JLNumber, Double]("value")((v: JLNumber) => v.value)
  implicit val decodeJLNumber: Decoder[JLNumber] = Decoder.forProduct1[JLNumber, Double]("value")((d: Double) => new JLNumber(d))
}

object JLNumber extends JLNumberCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Double }
  def apply(value: Double): JLNumber = {
    new JLNumber(value = value)
  }
  def apply(defn: JLNumber.Defn): JLNumber = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new JLNumber(value = defn.value)
  }
  implicit object JLNumber_upcast_JLNumber extends izumi.idealingua.runtime.IRTCast[JLNumber, JLNumber] {
    override def convert(_value: JLNumber): JLNumber = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      JLNumber(value = _value.value)
    }
  }
  implicit class JLNumberExtensions(override protected val _value: JLNumber) extends izumi.idealingua.runtime.IRTConversions[JLNumber]
}
       