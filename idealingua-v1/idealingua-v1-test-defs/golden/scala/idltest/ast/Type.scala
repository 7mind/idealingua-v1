package idltest.ast



final case class Type(label: String) extends AnyVal with Type.Defn

trait TypeCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeType: Encoder.AsObject[Type] = Encoder.forProduct1[Type, String]("label")((v: Type) => v.label)
  implicit val decodeType: Decoder[Type] = Decoder.forProduct1[Type, String]("label")((d: String) => new Type(d))
}

object Type extends TypeCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def label: String }
  def apply(label: String): Type = {
    new Type(label = label)
  }
  def apply(defn: Type.Defn): Type = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Type(label = defn.label)
  }
  implicit object Type_upcast_Type extends izumi.idealingua.runtime.IRTCast[Type, Type] {
    override def convert(_value: Type): Type = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Type(label = _value.label)
    }
  }
  implicit class TypeExtensions(override protected val _value: Type) extends izumi.idealingua.runtime.IRTConversions[Type]
}
       