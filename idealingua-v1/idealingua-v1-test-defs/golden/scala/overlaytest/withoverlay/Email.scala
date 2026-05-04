package overlaytest.withoverlay



final case class Email(attributes: OverlayEmailAttributes) extends Email.Defn

trait EmailCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeEmail: Encoder.AsObject[Email] = deriveEncoder[Email]
  implicit val decodeEmail: Decoder[Email] = deriveDecoder[Email]
}

object Email extends EmailCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def attributes: OverlayEmailAttributes }
  def apply(attributes: OverlayEmailAttributes.Defn): Email = {
    assert(attributes.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Email(attributes = OverlayEmailAttributes(attributes))
  }
  def apply(defn: Email.Defn): Email = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Email(attributes = defn.attributes)
  }
  implicit object Email_upcast_Email extends izumi.idealingua.runtime.IRTCast[Email, Email] {
    override def convert(_value: Email): Email = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Email(attributes = _value.attributes)
    }
  }
  implicit class EmailExtensions(override protected val _value: Email) extends izumi.idealingua.runtime.IRTConversions[Email]
}
       