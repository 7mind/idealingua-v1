package idltest.anyvals



final case class Test02UserAnyVal(test02DtoAnyVal: Test02DtoAnyVal, i08: Byte) extends Test02UserAnyVal.Defn

trait Test02UserAnyValCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTest02UserAnyVal: Encoder.AsObject[Test02UserAnyVal] = deriveEncoder[Test02UserAnyVal]
  implicit val decodeTest02UserAnyVal: Decoder[Test02UserAnyVal] = deriveDecoder[Test02UserAnyVal]
}

object Test02UserAnyVal extends Test02UserAnyValCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def test02DtoAnyVal: Test02DtoAnyVal
    def i08: Byte
  }
  def apply(test02DtoAnyVal: Test02DtoAnyVal.Defn, i08: Byte): Test02UserAnyVal = {
    assert(test02DtoAnyVal.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test02UserAnyVal(test02DtoAnyVal = Test02DtoAnyVal(test02DtoAnyVal), i08 = i08)
  }
  def apply(defn: Test02UserAnyVal.Defn): Test02UserAnyVal = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test02UserAnyVal(test02DtoAnyVal = defn.test02DtoAnyVal, i08 = defn.i08)
  }
  implicit object Test02UserAnyVal_upcast_Test02UserAnyVal extends izumi.idealingua.runtime.IRTCast[Test02UserAnyVal, Test02UserAnyVal] {
    override def convert(_value: Test02UserAnyVal): Test02UserAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test02UserAnyVal(test02DtoAnyVal = _value.test02DtoAnyVal, i08 = _value.i08)
    }
  }
  implicit class Test02UserAnyValExtensions(override protected val _value: Test02UserAnyVal) extends izumi.idealingua.runtime.IRTConversions[Test02UserAnyVal]
}
       