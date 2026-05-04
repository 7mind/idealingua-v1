package izumi.test.domain02



final case class TestDataWithAlias(a: izumi.test.domain01.RTestEnum) extends AnyVal with TestDataWithAlias.Defn

trait TestDataWithAliasCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestDataWithAlias: Encoder.AsObject[TestDataWithAlias] = Encoder.forProduct1[TestDataWithAlias, izumi.test.domain01.RTestEnum]("a")((v: TestDataWithAlias) => v.a)
  implicit val decodeTestDataWithAlias: Decoder[TestDataWithAlias] = Decoder.forProduct1[TestDataWithAlias, izumi.test.domain01.RTestEnum]("a")((d: izumi.test.domain01.RTestEnum) => new TestDataWithAlias(d))
}

object TestDataWithAlias extends TestDataWithAliasCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: izumi.test.domain01.RTestEnum }
  def apply(a: izumi.test.domain01.RTestEnum): TestDataWithAlias = {
    assert(a.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestDataWithAlias(a = a)
  }
  def apply(defn: TestDataWithAlias.Defn): TestDataWithAlias = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestDataWithAlias(a = defn.a)
  }
  implicit object TestDataWithAlias_upcast_TestDataWithAlias extends izumi.idealingua.runtime.IRTCast[TestDataWithAlias, TestDataWithAlias] {
    override def convert(_value: TestDataWithAlias): TestDataWithAlias = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestDataWithAlias(a = _value.a)
    }
  }
  implicit class TestDataWithAliasExtensions(override protected val _value: TestDataWithAlias) extends izumi.idealingua.runtime.IRTConversions[TestDataWithAlias]
}
       