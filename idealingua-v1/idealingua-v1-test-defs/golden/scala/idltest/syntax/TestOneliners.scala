package idltest.syntax



final case class TestOneliners(testDto: TestDto, str: String, i08: Byte) extends TestOneliners.Defn

trait TestOnelinersCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestOneliners: Encoder.AsObject[TestOneliners] = deriveEncoder[TestOneliners]
  implicit val decodeTestOneliners: Decoder[TestOneliners] = deriveDecoder[TestOneliners]
}

object TestOneliners extends TestOnelinersCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def testDto: TestDto
    def str: String
    def i08: Byte
  }
  def apply(testDto: TestDto.Defn, str: String, i08: Byte): TestOneliners = {
    assert(testDto.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestOneliners(testDto = TestDto(testDto), str = str, i08 = i08)
  }
  def apply(defn: TestOneliners.Defn): TestOneliners = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestOneliners(testDto = defn.testDto, str = defn.str, i08 = defn.i08)
  }
  implicit object TestOneliners_upcast_TestOneliners extends izumi.idealingua.runtime.IRTCast[TestOneliners, TestOneliners] {
    override def convert(_value: TestOneliners): TestOneliners = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestOneliners(testDto = _value.testDto, str = _value.str, i08 = _value.i08)
    }
  }
  implicit class TestOnelinersExtensions(override protected val _value: TestOneliners) extends izumi.idealingua.runtime.IRTConversions[TestOneliners]
}
       