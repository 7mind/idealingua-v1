package idltest.anyvals



final case class Test01DataAnyVal1(value: String, someInt: Byte) extends Test01MixinAnyVal with Test01DataAnyVal1.Defn

trait Test01DataAnyVal1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTest01DataAnyVal1: Encoder.AsObject[Test01DataAnyVal1] = deriveEncoder[Test01DataAnyVal1]
  implicit val decodeTest01DataAnyVal1: Decoder[Test01DataAnyVal1] = deriveDecoder[Test01DataAnyVal1]
}

object Test01DataAnyVal1 extends Test01DataAnyVal1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def value: String
    def someInt: Byte
  }
  def apply(test01mixinanyval: Test01MixinAnyVal, someInt: Byte): Test01DataAnyVal1 = {
    assert(test01mixinanyval.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test01DataAnyVal1(value = test01mixinanyval.value, someInt = someInt)
  }
  def apply(defn: Test01DataAnyVal1.Defn): Test01DataAnyVal1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test01DataAnyVal1(value = defn.value, someInt = defn.someInt)
  }
  implicit object Test01DataAnyVal1_cast_into_Test00Data2AnyVal extends izumi.idealingua.runtime.IRTCast[Test01DataAnyVal1, Test00Data2AnyVal] {
    override def convert(_value: Test01DataAnyVal1): Test00Data2AnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test00Data2AnyVal(value = _value.value, someInt = _value.someInt)
    }
  }
  implicit object Test01DataAnyVal1_cast_into_Test01DataAnyVal2 extends izumi.idealingua.runtime.IRTCast[Test01DataAnyVal1, Test01DataAnyVal2] {
    override def convert(_value: Test01DataAnyVal1): Test01DataAnyVal2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01DataAnyVal2(value = _value.value, someInt = _value.someInt)
    }
  }
  implicit object Test01DataAnyVal1_upcast_Test01DataAnyVal1 extends izumi.idealingua.runtime.IRTCast[Test01DataAnyVal1, Test01DataAnyVal1] {
    override def convert(_value: Test01DataAnyVal1): Test01DataAnyVal1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01DataAnyVal1(value = _value.value, someInt = _value.someInt)
    }
  }
  implicit object Test01DataAnyVal1_upcast_Test01MixinAnyVal extends izumi.idealingua.runtime.IRTCast[Test01DataAnyVal1, Test01MixinAnyVal] {
    override def convert(_value: Test01DataAnyVal1): Test01MixinAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01MixinAnyVal.Struct(value = _value.value)
    }
  }
  implicit class Test01DataAnyVal1Extensions(override protected val _value: Test01DataAnyVal1) extends izumi.idealingua.runtime.IRTConversions[Test01DataAnyVal1]
}
       