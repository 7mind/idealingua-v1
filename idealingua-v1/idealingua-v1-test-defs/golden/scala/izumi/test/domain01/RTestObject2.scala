package izumi.test.domain01



final case class RTestObject2(b: RTestMixin) extends RtestMixin2 with RTestObject2.Defn

trait RTestObject2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeRTestObject2: Encoder.AsObject[RTestObject2] = deriveEncoder[RTestObject2]
  implicit val decodeRTestObject2: Decoder[RTestObject2] = deriveDecoder[RTestObject2]
}

object RTestObject2 extends RTestObject2Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def b: RTestMixin }
  def apply(rtestmixin2: RtestMixin2): RTestObject2 = {
    assert(rtestmixin2.asInstanceOf[_root_.scala.AnyRef] ne null)
    new RTestObject2(b = rtestmixin2.b)
  }
  def apply(defn: RTestObject2.Defn): RTestObject2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new RTestObject2(b = defn.b)
  }
  implicit object RTestObject2_cast_into_RTestObject1 extends izumi.idealingua.runtime.IRTCast[RTestObject2, RTestObject1] {
    override def convert(_value: RTestObject2): RTestObject1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RTestObject1(b = _value.b)
    }
  }
  implicit object RTestObject2_cast_into_RtestMixin2Struct extends izumi.idealingua.runtime.IRTCast[RTestObject2, RtestMixin2.Struct] {
    override def convert(_value: RTestObject2): RtestMixin2.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RtestMixin2.Struct(b = _value.b)
    }
  }
  implicit object RTestObject2_upcast_RTestObject2 extends izumi.idealingua.runtime.IRTCast[RTestObject2, RTestObject2] {
    override def convert(_value: RTestObject2): RTestObject2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RTestObject2(b = _value.b)
    }
  }
  implicit object RTestObject2_upcast_RtestMixin2 extends izumi.idealingua.runtime.IRTCast[RTestObject2, RtestMixin2] {
    override def convert(_value: RTestObject2): RtestMixin2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RtestMixin2.Struct(b = _value.b)
    }
  }
  implicit class RTestObject2Extensions(override protected val _value: RTestObject2) extends izumi.idealingua.runtime.IRTConversions[RTestObject2]
}
       