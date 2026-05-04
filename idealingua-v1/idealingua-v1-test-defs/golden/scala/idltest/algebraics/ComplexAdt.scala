package idltest.algebraics



final case class ComplexAdt(id: AdtTestID) extends ComplexAdt.Defn

trait ComplexAdtCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeComplexAdt: Encoder.AsObject[ComplexAdt] = deriveEncoder[ComplexAdt]
  implicit val decodeComplexAdt: Decoder[ComplexAdt] = deriveDecoder[ComplexAdt]
}

object ComplexAdt extends ComplexAdtCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def id: AdtTestID }
  def apply(id: AdtTestID): ComplexAdt = {
    assert(id.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ComplexAdt(id = id)
  }
  def apply(defn: ComplexAdt.Defn): ComplexAdt = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ComplexAdt(id = defn.id)
  }
  implicit object ComplexAdt_cast_into_ComplexAdt2 extends izumi.idealingua.runtime.IRTCast[ComplexAdt, ComplexAdt2] {
    override def convert(_value: ComplexAdt): ComplexAdt2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ComplexAdt2(id = _value.id)
    }
  }
  implicit object ComplexAdt_upcast_ComplexAdt extends izumi.idealingua.runtime.IRTCast[ComplexAdt, ComplexAdt] {
    override def convert(_value: ComplexAdt): ComplexAdt = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ComplexAdt(id = _value.id)
    }
  }
  implicit class ComplexAdtExtensions(override protected val _value: ComplexAdt) extends izumi.idealingua.runtime.IRTConversions[ComplexAdt]
}
       