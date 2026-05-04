package idltest.syntax



final case class TestDto(value: TestMixin) extends TestDto.Defn

trait TestDtoCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestDto: Encoder.AsObject[TestDto] = deriveEncoder[TestDto]
  implicit val decodeTestDto: Decoder[TestDto] = deriveDecoder[TestDto]
}

object TestDto extends TestDtoCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: TestMixin }
  def apply(value: TestMixin): TestDto = {
    assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestDto(value = value)
  }
  def apply(defn: TestDto.Defn): TestDto = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestDto(value = defn.value)
  }
  implicit object TestDto_cast_into_TestDto1 extends izumi.idealingua.runtime.IRTCast[TestDto, TestDto1] {
    override def convert(_value: TestDto): TestDto1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestDto1(value = _value.value)
    }
  }
  implicit object TestDto_upcast_TestDto extends izumi.idealingua.runtime.IRTCast[TestDto, TestDto] {
    override def convert(_value: TestDto): TestDto = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestDto(value = _value.value)
    }
  }
  implicit class TestDtoExtensions(override protected val _value: TestDto) extends izumi.idealingua.runtime.IRTConversions[TestDto]
}
       