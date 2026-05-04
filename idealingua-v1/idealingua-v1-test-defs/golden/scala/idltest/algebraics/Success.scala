package idltest.algebraics



final case class Success(message: String) extends AnyVal with Success.Defn

trait SuccessCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeSuccess: Encoder.AsObject[Success] = Encoder.forProduct1[Success, String]("message")((v: Success) => v.message)
  implicit val decodeSuccess: Decoder[Success] = Decoder.forProduct1[Success, String]("message")((d: String) => new Success(d))
}

object Success extends SuccessCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def message: String }
  def apply(message: String): Success = {
    new Success(message = message)
  }
  def apply(defn: Success.Defn): Success = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Success(message = defn.message)
  }
  implicit object Success_upcast_Success extends izumi.idealingua.runtime.IRTCast[Success, Success] {
    override def convert(_value: Success): Success = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Success(message = _value.message)
    }
  }
  implicit class SuccessExtensions(override protected val _value: Success) extends izumi.idealingua.runtime.IRTConversions[Success]
}
       