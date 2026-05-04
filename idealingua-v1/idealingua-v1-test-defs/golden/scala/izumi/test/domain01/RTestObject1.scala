package izumi.test.domain01



final case class RTestObject1(b: RTestMixin) extends RtestMixin2 with RTestObject1.Defn

trait RTestObject1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeRTestObject1: Encoder.AsObject[RTestObject1] = deriveEncoder[RTestObject1]
  implicit val decodeRTestObject1: Decoder[RTestObject1] = deriveDecoder[RTestObject1]
}

object RTestObject1 extends RTestObject1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def b: RTestMixin }
  def apply(rtestmixin2: RtestMixin2): RTestObject1 = {
    assert(rtestmixin2.asInstanceOf[_root_.scala.AnyRef] ne null)
    new RTestObject1(b = rtestmixin2.b)
  }
  def apply(defn: RTestObject1.Defn): RTestObject1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new RTestObject1(b = defn.b)
  }
  implicit object RTestObject1_cast_into_RTestObject2 extends izumi.idealingua.runtime.IRTCast[RTestObject1, RTestObject2] {
    override def convert(_value: RTestObject1): RTestObject2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RTestObject2(b = _value.b)
    }
  }
  implicit object RTestObject1_cast_into_RtestMixin2Struct extends izumi.idealingua.runtime.IRTCast[RTestObject1, RtestMixin2.Struct] {
    override def convert(_value: RTestObject1): RtestMixin2.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RtestMixin2.Struct(b = _value.b)
    }
  }
  implicit object RTestObject1_upcast_RTestObject1 extends izumi.idealingua.runtime.IRTCast[RTestObject1, RTestObject1] {
    override def convert(_value: RTestObject1): RTestObject1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RTestObject1(b = _value.b)
    }
  }
  implicit object RTestObject1_upcast_RtestMixin2 extends izumi.idealingua.runtime.IRTCast[RTestObject1, RtestMixin2] {
    override def convert(_value: RTestObject1): RtestMixin2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      RtestMixin2.Struct(b = _value.b)
    }
  }
  implicit class RTestObject1Extensions(override protected val _value: RTestObject1) extends izumi.idealingua.runtime.IRTConversions[RTestObject1]
}
       