package izumi.test.domain01



final case class TestObject(userId: UserId, accountBalance: Int, latestLogin: Long, keys: Map[String, String], nicknames: List[String]) extends TestInterface with TestObject.Defn

trait TestObjectCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTestObject: Encoder.AsObject[TestObject] = deriveEncoder[TestObject]
  implicit val decodeTestObject: Decoder[TestObject] = deriveDecoder[TestObject]
}

object TestObject extends TestObjectCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def userId: UserId
    def accountBalance: Int
    def latestLogin: Long
    def keys: Map[String, String]
    def nicknames: List[String]
  }
  def apply(testinterface: TestInterface): TestObject = {
    assert(testinterface.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestObject(userId = testinterface.userId, accountBalance = testinterface.accountBalance, latestLogin = testinterface.latestLogin, keys = testinterface.keys, nicknames = testinterface.nicknames)
  }
  def apply(defn: TestObject.Defn): TestObject = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TestObject(userId = defn.userId, accountBalance = defn.accountBalance, latestLogin = defn.latestLogin, keys = defn.keys, nicknames = defn.nicknames)
  }
  implicit object TestObject_cast_into_TestInterfaceStruct extends izumi.idealingua.runtime.IRTCast[TestObject, TestInterface.Struct] {
    override def convert(_value: TestObject): TestInterface.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface.Struct(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
    }
  }
  implicit object TestObject_upcast_TestObject extends izumi.idealingua.runtime.IRTCast[TestObject, TestObject] {
    override def convert(_value: TestObject): TestObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestObject(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
    }
  }
  implicit object TestObject_upcast_TestInterface extends izumi.idealingua.runtime.IRTCast[TestObject, TestInterface] {
    override def convert(_value: TestObject): TestInterface = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface.Struct(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
    }
  }
  implicit class TestObjectExtensions(override protected val _value: TestObject) extends izumi.idealingua.runtime.IRTConversions[TestObject]
}
       