package idltest.inheritance



final case class CovariantDTO2(field: CovariantA) extends InheritedCovariant with CovariantDTO2.Defn

trait CovariantDTO2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeCovariantDTO2: Encoder.AsObject[CovariantDTO2] = deriveEncoder[CovariantDTO2]
  implicit val decodeCovariantDTO2: Decoder[CovariantDTO2] = deriveDecoder[CovariantDTO2]
}

object CovariantDTO2 extends CovariantDTO2Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def field: CovariantA }
  def apply(field: CovariantA): CovariantDTO2 = {
    assert(field.asInstanceOf[_root_.scala.AnyRef] ne null)
    new CovariantDTO2(field = field)
  }
  def apply(defn: CovariantDTO2.Defn): CovariantDTO2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new CovariantDTO2(field = defn.field)
  }
  implicit object CovariantDTO2_cast_into_InheritedCovariantStruct extends izumi.idealingua.runtime.IRTCast[CovariantDTO2, InheritedCovariant.Struct] {
    override def convert(_value: CovariantDTO2): InheritedCovariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      InheritedCovariant.Struct(field = _value.field)
    }
  }
  implicit object CovariantDTO2_upcast_CovariantDTO2 extends izumi.idealingua.runtime.IRTCast[CovariantDTO2, CovariantDTO2] {
    override def convert(_value: CovariantDTO2): CovariantDTO2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantDTO2(field = _value.field)
    }
  }
  implicit object CovariantDTO2_upcast_InheritedCovariant extends izumi.idealingua.runtime.IRTCast[CovariantDTO2, InheritedCovariant] {
    override def convert(_value: CovariantDTO2): InheritedCovariant = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      InheritedCovariant.Struct(field = _value.field)
    }
  }
  implicit object CovariantDTO2_upcast_WithCovariance extends izumi.idealingua.runtime.IRTCast[CovariantDTO2, WithCovariance] {
    override def convert(_value: CovariantDTO2): WithCovariance = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WithCovariance.Struct(field = _value.field)
    }
  }
  implicit class CovariantDTO2Extensions(override protected val _value: CovariantDTO2) extends izumi.idealingua.runtime.IRTConversions[CovariantDTO2]
}
       