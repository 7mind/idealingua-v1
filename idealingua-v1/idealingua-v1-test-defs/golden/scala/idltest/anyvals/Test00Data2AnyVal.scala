package idltest.anyvals



final case class Test00Data2AnyVal(value: String, someInt: Byte) extends Test00Data2AnyVal.Defn

trait Test00Data2AnyValCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTest00Data2AnyVal: Encoder.AsObject[Test00Data2AnyVal] = deriveEncoder[Test00Data2AnyVal]
  implicit val decodeTest00Data2AnyVal: Decoder[Test00Data2AnyVal] = deriveDecoder[Test00Data2AnyVal]
}

object Test00Data2AnyVal extends Test00Data2AnyValCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def value: String
    def someInt: Byte
  }
  def apply(test00data1anyval: Test00Data1AnyVal.Defn, someInt: Byte): Test00Data2AnyVal = {
    assert(test00data1anyval.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test00Data2AnyVal(value = test00data1anyval.value, someInt = someInt)
  }
  def apply(defn: Test00Data2AnyVal.Defn): Test00Data2AnyVal = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test00Data2AnyVal(value = defn.value, someInt = defn.someInt)
  }
  implicit object Test00Data2AnyVal_cast_into_Test01DataAnyVal1 extends izumi.idealingua.runtime.IRTCast[Test00Data2AnyVal, Test01DataAnyVal1] {
    override def convert(_value: Test00Data2AnyVal): Test01DataAnyVal1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01DataAnyVal1(value = _value.value, someInt = _value.someInt)
    }
  }
  implicit object Test00Data2AnyVal_cast_into_Test01DataAnyVal2 extends izumi.idealingua.runtime.IRTCast[Test00Data2AnyVal, Test01DataAnyVal2] {
    override def convert(_value: Test00Data2AnyVal): Test01DataAnyVal2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01DataAnyVal2(value = _value.value, someInt = _value.someInt)
    }
  }
  implicit object Test00Data2AnyVal_upcast_Test00Data2AnyVal extends izumi.idealingua.runtime.IRTCast[Test00Data2AnyVal, Test00Data2AnyVal] {
    override def convert(_value: Test00Data2AnyVal): Test00Data2AnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test00Data2AnyVal(value = _value.value, someInt = _value.someInt)
    }
  }
  implicit class Test00Data2AnyValExtensions(override protected val _value: Test00Data2AnyVal) extends izumi.idealingua.runtime.IRTConversions[Test00Data2AnyVal]
}
       