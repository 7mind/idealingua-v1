package idltest.algebraics



final case class Failure(code: Byte) extends AnyVal with Failure.Defn

trait FailureCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeFailure: Encoder.AsObject[Failure] = Encoder.forProduct1[Failure, Byte]("code")((v: Failure) => v.code)
  implicit val decodeFailure: Decoder[Failure] = Decoder.forProduct1[Failure, Byte]("code")((d: Byte) => new Failure(d))
}

object Failure extends FailureCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def code: Byte }
  def apply(code: Byte): Failure = {
    new Failure(code = code)
  }
  def apply(defn: Failure.Defn): Failure = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Failure(code = defn.code)
  }
  implicit object Failure_upcast_Failure extends izumi.idealingua.runtime.IRTCast[Failure, Failure] {
    override def convert(_value: Failure): Failure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Failure(code = _value.code)
    }
  }
  implicit class FailureExtensions(override protected val _value: Failure) extends izumi.idealingua.runtime.IRTConversions[Failure]
}
       