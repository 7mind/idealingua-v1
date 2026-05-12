package idltest.aliases



final case class D1(value: String, f2: String) extends M1 with idltest.aliases2.M2 with D1.Defn

trait D1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeD1: Encoder.AsObject[D1] = deriveEncoder[D1]
  implicit val decodeD1: Decoder[D1] = deriveDecoder[D1]
}

object D1 extends D1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def value: String
    def f2: String
  }
  def apply(m1: M1, m2: idltest.aliases2.M2): D1 = {
    assert((m2.asInstanceOf[_root_.scala.AnyRef] ne null) && (m1.asInstanceOf[_root_.scala.AnyRef] ne null))
    new D1(value = m1.value, f2 = m2.f2)
  }
  def apply(defn: D1.Defn): D1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new D1(value = defn.value, f2 = defn.f2)
  }
  implicit object D1_upcast_D1 extends izumi.idealingua.runtime.IRTCast[D1, D1] {
    override def convert(_value: D1): D1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      D1(value = _value.value, f2 = _value.f2)
    }
  }
  implicit object D1_upcast_M1 extends izumi.idealingua.runtime.IRTCast[D1, M1] {
    override def convert(_value: D1): M1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      M1.Struct(value = _value.value)
    }
  }
  implicit class D1Extensions(override protected val _value: D1) extends izumi.idealingua.runtime.IRTConversions[D1]
}
       