package idltest.services



final case class SuccessDataData(greeting: String) extends AnyVal with SuccessData with SuccessDataData.Defn

trait SuccessDataDataCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeSuccessDataData: Encoder.AsObject[SuccessDataData] = Encoder.forProduct1[SuccessDataData, String]("greeting")((v: SuccessDataData) => v.greeting)
  implicit val decodeSuccessDataData: Decoder[SuccessDataData] = Decoder.forProduct1[SuccessDataData, String]("greeting")((d: String) => new SuccessDataData(d))
}

object SuccessDataData extends SuccessDataDataCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def greeting: String }
  def apply(successdata: SuccessData): SuccessDataData = {
    assert(successdata.asInstanceOf[_root_.scala.AnyRef] ne null)
    new SuccessDataData(greeting = successdata.greeting)
  }
  def apply(defn: SuccessDataData.Defn): SuccessDataData = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new SuccessDataData(greeting = defn.greeting)
  }
  implicit object SuccessDataData_cast_into_SuccessDataStruct extends izumi.idealingua.runtime.IRTCast[SuccessDataData, SuccessData.Struct] {
    override def convert(_value: SuccessDataData): SuccessData.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SuccessData.Struct(greeting = _value.greeting)
    }
  }
  implicit object SuccessDataData_upcast_SuccessDataData extends izumi.idealingua.runtime.IRTCast[SuccessDataData, SuccessDataData] {
    override def convert(_value: SuccessDataData): SuccessDataData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SuccessDataData(greeting = _value.greeting)
    }
  }
  implicit object SuccessDataData_upcast_SuccessData extends izumi.idealingua.runtime.IRTCast[SuccessDataData, SuccessData] {
    override def convert(_value: SuccessDataData): SuccessData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SuccessData.Struct(greeting = _value.greeting)
    }
  }
  implicit class SuccessDataDataExtensions(override protected val _value: SuccessDataData) extends izumi.idealingua.runtime.IRTConversions[SuccessDataData]
}
       