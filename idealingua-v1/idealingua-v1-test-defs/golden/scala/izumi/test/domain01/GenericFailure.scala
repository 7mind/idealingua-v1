package izumi.test.domain01



final case class GenericFailure(message: String, diagnostics: Option[String], reserved: Map[String, String], code: GenericFailureCode) extends GenericFailure.Defn

trait GenericFailureCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeGenericFailure: Encoder.AsObject[GenericFailure] = deriveEncoder[GenericFailure]
  implicit val decodeGenericFailure: Decoder[GenericFailure] = deriveDecoder[GenericFailure]
}

object GenericFailure extends GenericFailureCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def message: String
    def diagnostics: Option[String]
    def reserved: Map[String, String]
    def code: GenericFailureCode
  }
  def apply(genericfailuredata: GenericFailureData, code: GenericFailureCode): GenericFailure = {
    assert((code.asInstanceOf[_root_.scala.AnyRef] ne null) && (genericfailuredata.asInstanceOf[_root_.scala.AnyRef] ne null))
    new GenericFailure(message = genericfailuredata.message, diagnostics = genericfailuredata.diagnostics, reserved = genericfailuredata.reserved, code = code)
  }
  def apply(defn: GenericFailure.Defn): GenericFailure = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new GenericFailure(message = defn.message, diagnostics = defn.diagnostics, reserved = defn.reserved, code = defn.code)
  }
  implicit object GenericFailure_upcast_GenericFailure extends izumi.idealingua.runtime.IRTCast[GenericFailure, GenericFailure] {
    override def convert(_value: GenericFailure): GenericFailure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      GenericFailure(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved, code = _value.code)
    }
  }
  implicit object GenericFailure_upcast_GenericFailureData extends izumi.idealingua.runtime.IRTCast[GenericFailure, GenericFailureData] {
    override def convert(_value: GenericFailure): GenericFailureData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      GenericFailureData.Struct(message = _value.message, diagnostics = _value.diagnostics, reserved = _value.reserved)
    }
  }
  implicit class GenericFailureExtensions(override protected val _value: GenericFailure) extends izumi.idealingua.runtime.IRTConversions[GenericFailure]
}
       