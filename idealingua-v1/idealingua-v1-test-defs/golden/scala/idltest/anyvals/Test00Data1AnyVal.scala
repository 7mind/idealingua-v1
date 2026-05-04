package idltest.anyvals



final case class Test00Data1AnyVal(value: String) extends AnyVal with Test00Data1AnyVal.Defn

trait Test00Data1AnyValCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTest00Data1AnyVal: Encoder.AsObject[Test00Data1AnyVal] = Encoder.forProduct1[Test00Data1AnyVal, String]("value")((v: Test00Data1AnyVal) => v.value)
  implicit val decodeTest00Data1AnyVal: Decoder[Test00Data1AnyVal] = Decoder.forProduct1[Test00Data1AnyVal, String]("value")((d: String) => new Test00Data1AnyVal(d))
}

object Test00Data1AnyVal extends Test00Data1AnyValCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
  def apply(value: String): Test00Data1AnyVal = {
    new Test00Data1AnyVal(value = value)
  }
  def apply(defn: Test00Data1AnyVal.Defn): Test00Data1AnyVal = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test00Data1AnyVal(value = defn.value)
  }
  implicit object Test00Data1AnyVal_cast_into_SimpleAnyValRecord extends izumi.idealingua.runtime.IRTCast[Test00Data1AnyVal, SimpleAnyValRecord] {
    override def convert(_value: Test00Data1AnyVal): SimpleAnyValRecord = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SimpleAnyValRecord(value = _value.value)
    }
  }
  implicit object Test00Data1AnyVal_cast_into_Test02DtoAnyVal extends izumi.idealingua.runtime.IRTCast[Test00Data1AnyVal, Test02DtoAnyVal] {
    override def convert(_value: Test00Data1AnyVal): Test02DtoAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test02DtoAnyVal(value = _value.value)
    }
  }
  implicit object Test00Data1AnyVal_cast_into_Test01MixinAnyValStruct extends izumi.idealingua.runtime.IRTCast[Test00Data1AnyVal, Test01MixinAnyVal.Struct] {
    override def convert(_value: Test00Data1AnyVal): Test01MixinAnyVal.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01MixinAnyVal.Struct(value = _value.value)
    }
  }
  implicit object Test00Data1AnyVal_upcast_Test00Data1AnyVal extends izumi.idealingua.runtime.IRTCast[Test00Data1AnyVal, Test00Data1AnyVal] {
    override def convert(_value: Test00Data1AnyVal): Test00Data1AnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test00Data1AnyVal(value = _value.value)
    }
  }
  implicit class Test00Data1AnyValExtensions(override protected val _value: Test00Data1AnyVal) extends izumi.idealingua.runtime.IRTConversions[Test00Data1AnyVal]
}
       