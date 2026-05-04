package overlaytest.withoverlay



final case class OverlayUserAttributes(name: String, surname: String) extends OverlayUserAttributes.Defn

trait OverlayUserAttributesCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeOverlayUserAttributes: Encoder.AsObject[OverlayUserAttributes] = deriveEncoder[OverlayUserAttributes]
  implicit val decodeOverlayUserAttributes: Decoder[OverlayUserAttributes] = deriveDecoder[OverlayUserAttributes]
}

object OverlayUserAttributes extends OverlayUserAttributesCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def name: String
    def surname: String
  }
  def apply(name: String, surname: String): OverlayUserAttributes = {
    new OverlayUserAttributes(name = name, surname = surname)
  }
  def apply(defn: OverlayUserAttributes.Defn): OverlayUserAttributes = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new OverlayUserAttributes(name = defn.name, surname = defn.surname)
  }
  implicit object OverlayUserAttributes_upcast_OverlayUserAttributes extends izumi.idealingua.runtime.IRTCast[OverlayUserAttributes, OverlayUserAttributes] {
    override def convert(_value: OverlayUserAttributes): OverlayUserAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      OverlayUserAttributes(name = _value.name, surname = _value.surname)
    }
  }
  implicit class OverlayUserAttributesExtensions(override protected val _value: OverlayUserAttributes) extends izumi.idealingua.runtime.IRTConversions[OverlayUserAttributes]
}
       