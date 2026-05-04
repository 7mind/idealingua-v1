package idltest.algebraics



final case class ComplexAdt2(id: AdtTestID) extends ComplexAdt2.Defn

trait ComplexAdt2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeComplexAdt2: Encoder.AsObject[ComplexAdt2] = deriveEncoder[ComplexAdt2]
  implicit val decodeComplexAdt2: Decoder[ComplexAdt2] = deriveDecoder[ComplexAdt2]
}

object ComplexAdt2 extends ComplexAdt2Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def id: AdtTestID }
  def apply(id: AdtTestID): ComplexAdt2 = {
    assert(id.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ComplexAdt2(id = id)
  }
  def apply(defn: ComplexAdt2.Defn): ComplexAdt2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ComplexAdt2(id = defn.id)
  }
  implicit object ComplexAdt2_cast_into_ComplexAdt extends izumi.idealingua.runtime.IRTCast[ComplexAdt2, ComplexAdt] {
    override def convert(_value: ComplexAdt2): ComplexAdt = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ComplexAdt(id = _value.id)
    }
  }
  implicit object ComplexAdt2_upcast_ComplexAdt2 extends izumi.idealingua.runtime.IRTCast[ComplexAdt2, ComplexAdt2] {
    override def convert(_value: ComplexAdt2): ComplexAdt2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ComplexAdt2(id = _value.id)
    }
  }
  implicit class ComplexAdt2Extensions(override protected val _value: ComplexAdt2) extends izumi.idealingua.runtime.IRTConversions[ComplexAdt2]
}
       