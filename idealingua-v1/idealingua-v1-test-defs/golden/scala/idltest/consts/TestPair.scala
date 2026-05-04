package idltest.consts



final case class TestPair(value: Int, name: String) extends TestPair.Defn

trait TestPairCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestPair: Encoder.AsObject[TestPair] = deriveEncoder[TestPair]
  implicit val decodeTestPair: Decoder[TestPair] = deriveDecoder[TestPair]
}

object TestPair extends TestPairCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def value: Int
    def name: String
  }
  def apply(value: Int, name: String): TestPair = {
    new TestPair(value = value, name = name)
  }
  def apply(defn: TestPair.Defn): TestPair = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestPair(value = defn.value, name = defn.name)
  }
  implicit object TestPair_upcast_TestPair extends izumi.idealingua.runtime.IRTCast[TestPair, TestPair] {
    override def convert(_value: TestPair): TestPair = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestPair(value = _value.value, name = _value.name)
    }
  }
  implicit class TestPairExtensions(override protected val _value: TestPair) extends izumi.idealingua.runtime.IRTConversions[TestPair]
}
       