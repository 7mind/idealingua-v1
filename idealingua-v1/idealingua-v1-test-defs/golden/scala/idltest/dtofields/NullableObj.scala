package idltest.dtofields



final case class NullableObj(a: Int) extends AnyVal with NullableContent with NullableObj.Defn

trait NullableObjCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeNullableObj: Encoder.AsObject[NullableObj] = Encoder.forProduct1[NullableObj, Int]("a")((v: NullableObj) => v.a)
  implicit val decodeNullableObj: Decoder[NullableObj] = Decoder.forProduct1[NullableObj, Int]("a")((d: Int) => new NullableObj(d))
}

object NullableObj extends NullableObjCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: Int }
  def apply(nullablecontent: NullableContent): NullableObj = {
    assert(nullablecontent.asInstanceOf[_root_.scala.AnyRef] ne null)
    new NullableObj(a = nullablecontent.a)
  }
  def apply(defn: NullableObj.Defn): NullableObj = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new NullableObj(a = defn.a)
  }
  implicit object NullableObj_cast_into_NullableContentStruct extends izumi.idealingua.runtime.IRTCast[NullableObj, NullableContent.Struct] {
    override def convert(_value: NullableObj): NullableContent.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NullableContent.Struct(a = _value.a)
    }
  }
  implicit object NullableObj_upcast_NullableObj extends izumi.idealingua.runtime.IRTCast[NullableObj, NullableObj] {
    override def convert(_value: NullableObj): NullableObj = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NullableObj(a = _value.a)
    }
  }
  implicit object NullableObj_upcast_NullableContent extends izumi.idealingua.runtime.IRTCast[NullableObj, NullableContent] {
    override def convert(_value: NullableObj): NullableContent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NullableContent.Struct(a = _value.a)
    }
  }
  implicit class NullableObjExtensions(override protected val _value: NullableObj) extends izumi.idealingua.runtime.IRTConversions[NullableObj]
}
       