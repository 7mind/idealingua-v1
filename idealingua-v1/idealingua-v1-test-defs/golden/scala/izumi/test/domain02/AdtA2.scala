package izumi.test.domain02



final case class AdtA2(b: Int) extends AnyVal with AdtA2.Defn

trait AdtA2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeAdtA2: Encoder.AsObject[AdtA2] = Encoder.forProduct1[AdtA2, Int]("b")((v: AdtA2) => v.b)
  implicit val decodeAdtA2: Decoder[AdtA2] = Decoder.forProduct1[AdtA2, Int]("b")((d: Int) => new AdtA2(d))
}

object AdtA2 extends AdtA2Circe {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def b: Int }
  def apply(b: Int): AdtA2 = {
    new AdtA2(b = b)
  }
  def apply(defn: AdtA2.Defn): AdtA2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new AdtA2(b = defn.b)
  }
  implicit object AdtA2_cast_into_Adt2 extends izumi.idealingua.runtime.IRTCast[AdtA2, Adt2] {
    override def convert(_value: AdtA2): Adt2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Adt2(b = _value.b)
    }
  }
  implicit object AdtA2_upcast_AdtA2 extends izumi.idealingua.runtime.IRTCast[AdtA2, AdtA2] {
    override def convert(_value: AdtA2): AdtA2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AdtA2(b = _value.b)
    }
  }
  implicit class AdtA2Extensions(override protected val _value: AdtA2) extends izumi.idealingua.runtime.IRTConversions[AdtA2]
}
       