package overlaytest.withoverlay



final case class OverlayEmailAttributes(disposable: Boolean) extends AnyVal with OverlayEmailAttributes.Defn

trait OverlayEmailAttributesCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeOverlayEmailAttributes: Encoder.AsObject[OverlayEmailAttributes] = Encoder.forProduct1[OverlayEmailAttributes, Boolean]("disposable")((v: OverlayEmailAttributes) => v.disposable)
  implicit val decodeOverlayEmailAttributes: Decoder[OverlayEmailAttributes] = Decoder.forProduct1[OverlayEmailAttributes, Boolean]("disposable")((d: Boolean) => new OverlayEmailAttributes(d))
}

object OverlayEmailAttributes extends OverlayEmailAttributesCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def disposable: Boolean }
  def apply(disposable: Boolean): OverlayEmailAttributes = {
    new OverlayEmailAttributes(disposable = disposable)
  }
  def apply(defn: OverlayEmailAttributes.Defn): OverlayEmailAttributes = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new OverlayEmailAttributes(disposable = defn.disposable)
  }
  implicit object OverlayEmailAttributes_upcast_OverlayEmailAttributes extends izumi.idealingua.runtime.IRTCast[OverlayEmailAttributes, OverlayEmailAttributes] {
    override def convert(_value: OverlayEmailAttributes): OverlayEmailAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      OverlayEmailAttributes(disposable = _value.disposable)
    }
  }
  implicit class OverlayEmailAttributesExtensions(override protected val _value: OverlayEmailAttributes) extends izumi.idealingua.runtime.IRTConversions[OverlayEmailAttributes]
}
       