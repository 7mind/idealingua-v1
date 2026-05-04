package idltest.syntax



final case class TestDto1(value: TestMixin) extends TestDto1.Defn

trait TestDto1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestDto1: Encoder.AsObject[TestDto1] = deriveEncoder[TestDto1]
  implicit val decodeTestDto1: Decoder[TestDto1] = deriveDecoder[TestDto1]
}

object TestDto1 extends TestDto1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: TestMixin }
  def apply(value: TestMixin): TestDto1 = {
    assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestDto1(value = value)
  }
  def apply(defn: TestDto1.Defn): TestDto1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestDto1(value = defn.value)
  }
  implicit object TestDto1_cast_into_TestDto extends izumi.idealingua.runtime.IRTCast[TestDto1, TestDto] {
    override def convert(_value: TestDto1): TestDto = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestDto(value = _value.value)
    }
  }
  implicit object TestDto1_upcast_TestDto1 extends izumi.idealingua.runtime.IRTCast[TestDto1, TestDto1] {
    override def convert(_value: TestDto1): TestDto1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestDto1(value = _value.value)
    }
  }
  implicit class TestDto1Extensions(override protected val _value: TestDto1) extends izumi.idealingua.runtime.IRTConversions[TestDto1]
}
       