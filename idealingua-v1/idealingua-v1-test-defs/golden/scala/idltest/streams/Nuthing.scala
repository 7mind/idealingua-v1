package idltest.streams



final case class Nuthing() extends Nuthing.Defn

trait NuthingCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeNuthing: Encoder.AsObject[Nuthing] = deriveEncoder[Nuthing]
  implicit val decodeNuthing: Decoder[Nuthing] = deriveDecoder[Nuthing]
}

object Nuthing extends NuthingCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
  def apply(defn: Nuthing.Defn): Nuthing = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Nuthing()
  }
  implicit object Nuthing_upcast_Nuthing extends izumi.idealingua.runtime.IRTCast[Nuthing, Nuthing] {
    override def convert(_value: Nuthing): Nuthing = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Nuthing()
    }
  }
  implicit class NuthingExtensions(override protected val _value: Nuthing) extends izumi.idealingua.runtime.IRTConversions[Nuthing]
}
       