package idltest.inheritance



final case class CovariantDTO1(field: Covariant) extends WithCovariance with CovariantDTO1.Defn

trait CovariantDTO1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeCovariantDTO1: Encoder.AsObject[CovariantDTO1] = deriveEncoder[CovariantDTO1]
  implicit val decodeCovariantDTO1: Decoder[CovariantDTO1] = deriveDecoder[CovariantDTO1]
}

object CovariantDTO1 extends CovariantDTO1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def field: Covariant }
  def apply(withcovariance: WithCovariance): CovariantDTO1 = {
    assert(withcovariance.asInstanceOf[_root_.scala.AnyRef] ne null)
    new CovariantDTO1(field = withcovariance.field)
  }
  def apply(defn: CovariantDTO1.Defn): CovariantDTO1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new CovariantDTO1(field = defn.field)
  }
  implicit object CovariantDTO1_cast_into_WithCovarianceStruct extends izumi.idealingua.runtime.IRTCast[CovariantDTO1, WithCovariance.Struct] {
    override def convert(_value: CovariantDTO1): WithCovariance.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WithCovariance.Struct(field = _value.field)
    }
  }
  implicit object CovariantDTO1_upcast_CovariantDTO1 extends izumi.idealingua.runtime.IRTCast[CovariantDTO1, CovariantDTO1] {
    override def convert(_value: CovariantDTO1): CovariantDTO1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantDTO1(field = _value.field)
    }
  }
  implicit object CovariantDTO1_upcast_WithCovariance extends izumi.idealingua.runtime.IRTCast[CovariantDTO1, WithCovariance] {
    override def convert(_value: CovariantDTO1): WithCovariance = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WithCovariance.Struct(field = _value.field)
    }
  }
  implicit class CovariantDTO1Extensions(override protected val _value: CovariantDTO1) extends izumi.idealingua.runtime.IRTConversions[CovariantDTO1]
}
       