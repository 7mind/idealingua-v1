package idltest.datainheritance



final case class TestData1(str: String, i32: Int) extends TestData1.Defn

trait TestData1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestData1: Encoder.AsObject[TestData1] = deriveEncoder[TestData1]
  implicit val decodeTestData1: Decoder[TestData1] = deriveDecoder[TestData1]
}

object TestData1 extends TestData1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def str: String
    def i32: Int
  }
  def apply(str: String, i32: Int): TestData1 = {
    new TestData1(str = str, i32 = i32)
  }
  def apply(defn: TestData1.Defn): TestData1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestData1(str = defn.str, i32 = defn.i32)
  }
  implicit object TestData1_cast_into_ParameterDTO extends izumi.idealingua.runtime.IRTCast[TestData1, ParameterDTO] {
    override def convert(_value: TestData1): ParameterDTO = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ParameterDTO(str = _value.str, i32 = _value.i32)
    }
  }
  implicit object TestData1_upcast_TestData1 extends izumi.idealingua.runtime.IRTCast[TestData1, TestData1] {
    override def convert(_value: TestData1): TestData1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestData1(str = _value.str, i32 = _value.i32)
    }
  }
  implicit class TestData1Extensions(override protected val _value: TestData1) extends izumi.idealingua.runtime.IRTConversions[TestData1]
}
       