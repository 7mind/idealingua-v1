package izumi.test.domain01



trait TestInterface extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def userId: UserId
  def accountBalance: Int
  def latestLogin: Long
  def keys: Map[String, String]
  def nicknames: List[String]
}

trait TestInterfaceCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTestInterface: Encoder.AsObject[TestInterface] = Encoder.AsObject.instance {
    case v: TestObject =>
      Map("izumi.test.domain01.TestObject" -> v).asJsonObject
    case v: TestInterface.Struct =>
      Map("izumi.test.domain01.TestInterface.Struct" -> v).asJsonObject
  }
  implicit val decodeTestInterface: Decoder[TestInterface] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.TestObject" =>
        value.as[TestObject]
      case "izumi.test.domain01.TestInterface.Struct" =>
        value.as[TestInterface.Struct]
      case _ =>
        val cname = "izumi.test.domain01.TestInterface"
        val alts = List("izumi.test.domain01.TestObject", "izumi.test.domain01.TestInterface.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TestInterface extends TestInterfaceCirce {
  def apply(userId: UserId, accountBalance: Int, latestLogin: Long, keys: Map[String, String], nicknames: List[String]) = Struct(userId, accountBalance, latestLogin, keys, nicknames)
  final case class Struct(userId: UserId, accountBalance: Int, latestLogin: Long, keys: Map[String, String], nicknames: List[String]) extends TestInterface
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TestInterface.StructCirce {
    def apply(testinterface: TestInterface): TestInterface.Struct = {
      assert(testinterface.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestInterface.Struct(userId = testinterface.userId, accountBalance = testinterface.accountBalance, latestLogin = testinterface.latestLogin, keys = testinterface.keys, nicknames = testinterface.nicknames)
    }
    implicit object Struct_cast_into_TestObject extends izumi.idealingua.runtime.IRTCast[TestInterface.Struct, TestObject] {
      override def convert(_value: TestInterface.Struct): TestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestObject(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TestInterface.Struct, TestInterface.Struct] {
      override def convert(_value: TestInterface.Struct): TestInterface.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface.Struct(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
      }
    }
    implicit object Struct_upcast_TestInterface extends izumi.idealingua.runtime.IRTCast[TestInterface.Struct, TestInterface] {
      override def convert(_value: TestInterface.Struct): TestInterface = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface.Struct(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
      }
    }
    implicit class StructExtensions(override protected val _value: TestInterface.Struct) extends izumi.idealingua.runtime.IRTConversions[TestInterface.Struct]
  }
  implicit object TestInterface_downcast_extend_TestObject extends izumi.idealingua.runtime.IRTExtend[TestInterface, TestObject] {
    class Call(private val _value: TestInterface) extends AnyVal {
      def using(): TestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestObject(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface): Call = new Call(_value)
  }
  implicit object TestInterface_downcast_extend_TestInterfaceStruct extends izumi.idealingua.runtime.IRTExtend[TestInterface, TestInterface.Struct] {
    class Call(private val _value: TestInterface) extends AnyVal {
      def using(): TestInterface.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface.Struct(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface): Call = new Call(_value)
  }
  implicit object TestInterface_upcast_TestInterface extends izumi.idealingua.runtime.IRTCast[TestInterface, TestInterface] {
    override def convert(_value: TestInterface): TestInterface = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface.Struct(userId = _value.userId, accountBalance = _value.accountBalance, latestLogin = _value.latestLogin, keys = _value.keys, nicknames = _value.nicknames)
    }
  }
  implicit class TestInterfaceExtensions(override protected val _value: TestInterface) extends izumi.idealingua.runtime.IRTConversions[TestInterface]
}
       