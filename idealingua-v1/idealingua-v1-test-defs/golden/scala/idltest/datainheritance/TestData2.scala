package idltest.datainheritance



final case class TestData2(str: String, i32: Int, value: Byte) extends TestData2.Defn

trait TestData2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestData2: Encoder.AsObject[TestData2] = deriveEncoder[TestData2]
  implicit val decodeTestData2: Decoder[TestData2] = deriveDecoder[TestData2]
}

object TestData2 extends TestData2Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def str: String
    def i32: Int
    def value: Byte
  }
  def apply(testdata1: TestData1.Defn, value: Byte): TestData2 = {
    assert(testdata1.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestData2(str = testdata1.str, i32 = testdata1.i32, value = value)
  }
  def apply(defn: TestData2.Defn): TestData2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestData2(str = defn.str, i32 = defn.i32, value = defn.value)
  }
  implicit object TestData2_upcast_TestData2 extends izumi.idealingua.runtime.IRTCast[TestData2, TestData2] {
    override def convert(_value: TestData2): TestData2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestData2(str = _value.str, i32 = _value.i32, value = _value.value)
    }
  }
  implicit class TestData2Extensions(override protected val _value: TestData2) extends izumi.idealingua.runtime.IRTConversions[TestData2]
}
       