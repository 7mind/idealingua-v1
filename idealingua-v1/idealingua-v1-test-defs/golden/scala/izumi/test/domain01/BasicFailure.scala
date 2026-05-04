package izumi.test.domain01



final case class BasicFailure(code: Int) extends AnyVal with CommonFailure with BasicFailure.Defn

trait BasicFailureCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeBasicFailure: Encoder.AsObject[BasicFailure] = Encoder.forProduct1[BasicFailure, Int]("code")((v: BasicFailure) => v.code)
  implicit val decodeBasicFailure: Decoder[BasicFailure] = Decoder.forProduct1[BasicFailure, Int]("code")((d: Int) => new BasicFailure(d))
}

object BasicFailure extends BasicFailureCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def code: Int }
  def apply(commonfailure: CommonFailure): BasicFailure = {
    assert(commonfailure.asInstanceOf[_root_.scala.AnyRef] ne null)
    new BasicFailure(code = commonfailure.code)
  }
  def apply(defn: BasicFailure.Defn): BasicFailure = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new BasicFailure(code = defn.code)
  }
  implicit object BasicFailure_cast_into_CommonFailureStruct extends izumi.idealingua.runtime.IRTCast[BasicFailure, CommonFailure.Struct] {
    override def convert(_value: BasicFailure): CommonFailure.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CommonFailure.Struct(code = _value.code)
    }
  }
  implicit object BasicFailure_upcast_BasicFailure extends izumi.idealingua.runtime.IRTCast[BasicFailure, BasicFailure] {
    override def convert(_value: BasicFailure): BasicFailure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      BasicFailure(code = _value.code)
    }
  }
  implicit object BasicFailure_upcast_CommonFailure extends izumi.idealingua.runtime.IRTCast[BasicFailure, CommonFailure] {
    override def convert(_value: BasicFailure): CommonFailure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CommonFailure.Struct(code = _value.code)
    }
  }
  implicit class BasicFailureExtensions(override protected val _value: BasicFailure) extends izumi.idealingua.runtime.IRTConversions[BasicFailure]
}
       