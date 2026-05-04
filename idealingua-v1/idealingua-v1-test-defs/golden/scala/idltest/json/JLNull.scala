package idltest.json



final case class JLNull() extends JLNull.Defn

trait JLNullCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeJLNull: Encoder.AsObject[JLNull] = deriveEncoder[JLNull]
  implicit val decodeJLNull: Decoder[JLNull] = deriveDecoder[JLNull]
}

object JLNull extends JLNullCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
  def apply(defn: JLNull.Defn): JLNull = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new JLNull()
  }
  implicit object JLNull_upcast_JLNull extends izumi.idealingua.runtime.IRTCast[JLNull, JLNull] {
    override def convert(_value: JLNull): JLNull = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      JLNull()
    }
  }
  implicit class JLNullExtensions(override protected val _value: JLNull) extends izumi.idealingua.runtime.IRTConversions[JLNull]
}
       