package izumi.test.domain02



final case class Adt2(b: Int) extends AnyVal with Adt2.Defn

trait Adt2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeAdt2: Encoder.AsObject[Adt2] = Encoder.forProduct1[Adt2, Int]("b")((v: Adt2) => v.b)
  implicit val decodeAdt2: Decoder[Adt2] = Decoder.forProduct1[Adt2, Int]("b")((d: Int) => new Adt2(d))
}

object Adt2 extends Adt2Circe {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def b: Int }
  def apply(b: Int): Adt2 = {
    new Adt2(b = b)
  }
  def apply(defn: Adt2.Defn): Adt2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Adt2(b = defn.b)
  }
  implicit object Adt2_cast_into_AdtA2 extends izumi.idealingua.runtime.IRTCast[Adt2, AdtA2] {
    override def convert(_value: Adt2): AdtA2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AdtA2(b = _value.b)
    }
  }
  implicit object Adt2_upcast_Adt2 extends izumi.idealingua.runtime.IRTCast[Adt2, Adt2] {
    override def convert(_value: Adt2): Adt2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Adt2(b = _value.b)
    }
  }
  implicit class Adt2Extensions(override protected val _value: Adt2) extends izumi.idealingua.runtime.IRTConversions[Adt2]
}
       