package idltest.json



final case class JLArray(values: List[JSONLike]) extends JLArray.Defn

trait JLArrayCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeJLArray: Encoder.AsObject[JLArray] = deriveEncoder[JLArray]
  implicit val decodeJLArray: Decoder[JLArray] = deriveDecoder[JLArray]
}

object JLArray extends JLArrayCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def values: List[JSONLike] }
  def apply(values: List[JSONLike]): JLArray = {
    new JLArray(values = values)
  }
  def apply(defn: JLArray.Defn): JLArray = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new JLArray(values = defn.values)
  }
  implicit object JLArray_upcast_JLArray extends izumi.idealingua.runtime.IRTCast[JLArray, JLArray] {
    override def convert(_value: JLArray): JLArray = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      JLArray(values = _value.values)
    }
  }
  implicit class JLArrayExtensions(override protected val _value: JLArray) extends izumi.idealingua.runtime.IRTConversions[JLArray]
}
       