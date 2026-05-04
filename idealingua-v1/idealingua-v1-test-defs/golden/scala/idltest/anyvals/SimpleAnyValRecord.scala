package idltest.anyvals



final case class SimpleAnyValRecord(value: String) extends AnyVal with SimpleAnyValRecord.Defn

trait SimpleAnyValRecordCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeSimpleAnyValRecord: Encoder.AsObject[SimpleAnyValRecord] = Encoder.forProduct1[SimpleAnyValRecord, String]("value")((v: SimpleAnyValRecord) => v.value)
  implicit val decodeSimpleAnyValRecord: Decoder[SimpleAnyValRecord] = Decoder.forProduct1[SimpleAnyValRecord, String]("value")((d: String) => new SimpleAnyValRecord(d))
}

object SimpleAnyValRecord extends SimpleAnyValRecordCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
  def apply(value: String): SimpleAnyValRecord = {
    new SimpleAnyValRecord(value = value)
  }
  def apply(defn: SimpleAnyValRecord.Defn): SimpleAnyValRecord = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new SimpleAnyValRecord(value = defn.value)
  }
  implicit object SimpleAnyValRecord_cast_into_Test00Data1AnyVal extends izumi.idealingua.runtime.IRTCast[SimpleAnyValRecord, Test00Data1AnyVal] {
    override def convert(_value: SimpleAnyValRecord): Test00Data1AnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test00Data1AnyVal(value = _value.value)
    }
  }
  implicit object SimpleAnyValRecord_cast_into_Test02DtoAnyVal extends izumi.idealingua.runtime.IRTCast[SimpleAnyValRecord, Test02DtoAnyVal] {
    override def convert(_value: SimpleAnyValRecord): Test02DtoAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test02DtoAnyVal(value = _value.value)
    }
  }
  implicit object SimpleAnyValRecord_cast_into_Test01MixinAnyValStruct extends izumi.idealingua.runtime.IRTCast[SimpleAnyValRecord, Test01MixinAnyVal.Struct] {
    override def convert(_value: SimpleAnyValRecord): Test01MixinAnyVal.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01MixinAnyVal.Struct(value = _value.value)
    }
  }
  implicit object SimpleAnyValRecord_upcast_SimpleAnyValRecord extends izumi.idealingua.runtime.IRTCast[SimpleAnyValRecord, SimpleAnyValRecord] {
    override def convert(_value: SimpleAnyValRecord): SimpleAnyValRecord = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SimpleAnyValRecord(value = _value.value)
    }
  }
  implicit class SimpleAnyValRecordExtensions(override protected val _value: SimpleAnyValRecord) extends izumi.idealingua.runtime.IRTConversions[SimpleAnyValRecord]
}
       