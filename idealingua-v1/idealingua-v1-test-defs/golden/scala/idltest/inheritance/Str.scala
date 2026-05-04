package idltest.inheritance



final case class Str(str: String) extends AnyVal with Empty with Str.Defn

trait StrCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeStr: Encoder.AsObject[Str] = Encoder.forProduct1[Str, String]("str")((v: Str) => v.str)
  implicit val decodeStr: Decoder[Str] = Decoder.forProduct1[Str, String]("str")((d: String) => new Str(d))
}

object Str extends StrCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def str: String }
  def apply(str: String): Str = {
    new Str(str = str)
  }
  def apply(defn: Str.Defn): Str = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Str(str = defn.str)
  }
  implicit object Str_upcast_Str extends izumi.idealingua.runtime.IRTCast[Str, Str] {
    override def convert(_value: Str): Str = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Str(str = _value.str)
    }
  }
  implicit object Str_upcast_Empty extends izumi.idealingua.runtime.IRTCast[Str, Empty] {
    override def convert(_value: Str): Empty = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit class StrExtensions(override protected val _value: Str) extends izumi.idealingua.runtime.IRTConversions[Str]
}
       