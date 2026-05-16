package mcpdemo



final case class ColorResult(color: Color) extends AnyVal with ColorResult.Defn

trait ColorResultCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.derivation.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeColorResult: Encoder.AsObject[ColorResult] = deriveEncoder[ColorResult]
  implicit val decodeColorResult: Decoder[ColorResult] = deriveDecoder[ColorResult]
}

object ColorResult extends ColorResultCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def color: Color }
  def apply(color: Color): ColorResult = {
    assert(color.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ColorResult(color = color)
  }
  def apply(defn: ColorResult.Defn): ColorResult = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ColorResult(color = defn.color)
  }
  implicit object ColorResult_upcast_ColorResult extends izumi.idealingua.runtime.IRTCast[ColorResult, ColorResult] {
    override def convert(_value: ColorResult): ColorResult = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ColorResult(color = _value.color)
    }
  }
  implicit class ColorResultExtensions(override protected val _value: ColorResult) extends izumi.idealingua.runtime.IRTConversions[ColorResult]
}
       