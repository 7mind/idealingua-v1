package izumi.test.domain02



final case class AdtA1(a: Int) extends AnyVal with AdtA1.Defn

trait AdtA1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeAdtA1: Encoder.AsObject[AdtA1] = Encoder.forProduct1[AdtA1, Int]("a")((v: AdtA1) => v.a)
  implicit val decodeAdtA1: Decoder[AdtA1] = Decoder.forProduct1[AdtA1, Int]("a")((d: Int) => new AdtA1(d))
}

object AdtA1 extends AdtA1Circe {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: Int }
  def apply(a: Int): AdtA1 = {
    new AdtA1(a = a)
  }
  def apply(defn: AdtA1.Defn): AdtA1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new AdtA1(a = defn.a)
  }
  implicit object AdtA1_upcast_AdtA1 extends izumi.idealingua.runtime.IRTCast[AdtA1, AdtA1] {
    override def convert(_value: AdtA1): AdtA1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AdtA1(a = _value.a)
    }
  }
  implicit class AdtA1Extensions(override protected val _value: AdtA1) extends izumi.idealingua.runtime.IRTConversions[AdtA1]
}
       