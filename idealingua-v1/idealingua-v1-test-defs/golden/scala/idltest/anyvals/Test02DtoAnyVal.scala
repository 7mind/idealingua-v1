package idltest.anyvals



final case class Test02DtoAnyVal(value: String) extends AnyVal with Test02DtoAnyVal.Defn

trait Test02DtoAnyValCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTest02DtoAnyVal: Encoder.AsObject[Test02DtoAnyVal] = Encoder.forProduct1[Test02DtoAnyVal, String]("value")((v: Test02DtoAnyVal) => v.value)
  implicit val decodeTest02DtoAnyVal: Decoder[Test02DtoAnyVal] = Decoder.forProduct1[Test02DtoAnyVal, String]("value")((d: String) => new Test02DtoAnyVal(d))
}

object Test02DtoAnyVal extends Test02DtoAnyValCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
  def apply(value: String): Test02DtoAnyVal = {
    new Test02DtoAnyVal(value = value)
  }
  def apply(defn: Test02DtoAnyVal.Defn): Test02DtoAnyVal = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Test02DtoAnyVal(value = defn.value)
  }
  implicit object Test02DtoAnyVal_cast_into_SimpleAnyValRecord extends izumi.idealingua.runtime.IRTCast[Test02DtoAnyVal, SimpleAnyValRecord] {
    override def convert(_value: Test02DtoAnyVal): SimpleAnyValRecord = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SimpleAnyValRecord(value = _value.value)
    }
  }
  implicit object Test02DtoAnyVal_cast_into_Test00Data1AnyVal extends izumi.idealingua.runtime.IRTCast[Test02DtoAnyVal, Test00Data1AnyVal] {
    override def convert(_value: Test02DtoAnyVal): Test00Data1AnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test00Data1AnyVal(value = _value.value)
    }
  }
  implicit object Test02DtoAnyVal_cast_into_Test01MixinAnyValStruct extends izumi.idealingua.runtime.IRTCast[Test02DtoAnyVal, Test01MixinAnyVal.Struct] {
    override def convert(_value: Test02DtoAnyVal): Test01MixinAnyVal.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01MixinAnyVal.Struct(value = _value.value)
    }
  }
  implicit object Test02DtoAnyVal_upcast_Test02DtoAnyVal extends izumi.idealingua.runtime.IRTCast[Test02DtoAnyVal, Test02DtoAnyVal] {
    override def convert(_value: Test02DtoAnyVal): Test02DtoAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test02DtoAnyVal(value = _value.value)
    }
  }
  implicit class Test02DtoAnyValExtensions(override protected val _value: Test02DtoAnyVal) extends izumi.idealingua.runtime.IRTConversions[Test02DtoAnyVal]
}
       