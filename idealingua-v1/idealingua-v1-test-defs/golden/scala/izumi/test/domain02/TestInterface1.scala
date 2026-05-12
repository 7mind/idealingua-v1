package izumi.test.domain02



trait TestInterface1 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def if1Field_overriden: Int
  def if1Field_inherited: Int
  def sameField: Long
  def sameEverywhereField: Long
  def fromOtherDomain: izumi.test.domain01.TestValIdentifier
  def fromOtherDomainDirect: izumi.test.domain01.TestValIdentifier
}

trait TestInterface1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTestInterface1: Encoder.AsObject[TestInterface1] = Encoder.AsObject.instance {
    case v: DTO1 =>
      Map("izumi.test.domain02.DTO1" -> v).asJsonObject
    case v: TestInterface1.Struct =>
      Map("izumi.test.domain02.TestInterface1.Struct" -> v).asJsonObject
    case v: TestInterface3.Struct =>
      Map("izumi.test.domain02.TestInterface3.Struct" -> v).asJsonObject
  }
  implicit val decodeTestInterface1: Decoder[TestInterface1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain02.DTO1" =>
        value.as[DTO1]
      case "izumi.test.domain02.TestInterface1.Struct" =>
        value.as[TestInterface1.Struct]
      case "izumi.test.domain02.TestInterface3.Struct" =>
        value.as[TestInterface3.Struct]
      case _ =>
        val cname = "izumi.test.domain02.TestInterface1"
        val alts = List("izumi.test.domain02.DTO1", "izumi.test.domain02.TestInterface1.Struct", "izumi.test.domain02.TestInterface3.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TestInterface1 extends TestInterface1Circe {
  def apply(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long, fromOtherDomain: izumi.test.domain01.TestValIdentifier, fromOtherDomainDirect: izumi.test.domain01.TestValIdentifier) = Struct(if1Field_overriden, if1Field_inherited, sameField, sameEverywhereField, fromOtherDomain, fromOtherDomainDirect)
  final case class Struct(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long, fromOtherDomain: izumi.test.domain01.TestValIdentifier, fromOtherDomainDirect: izumi.test.domain01.TestValIdentifier) extends TestInterface1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TestInterface1.StructCirce {
    def apply(testinterface1: TestInterface1): TestInterface1.Struct = {
      assert(testinterface1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestInterface1.Struct(if1Field_overriden = testinterface1.if1Field_overriden, if1Field_inherited = testinterface1.if1Field_inherited, sameField = testinterface1.sameField, sameEverywhereField = testinterface1.sameEverywhereField, fromOtherDomain = testinterface1.fromOtherDomain, fromOtherDomainDirect = testinterface1.fromOtherDomainDirect)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TestInterface1.Struct, TestInterface1.Struct] {
      override def convert(_value: TestInterface1.Struct): TestInterface1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect)
      }
    }
    implicit object Struct_upcast_TestInterface1 extends izumi.idealingua.runtime.IRTCast[TestInterface1.Struct, TestInterface1] {
      override def convert(_value: TestInterface1.Struct): TestInterface1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect)
      }
    }
    implicit class StructExtensions(override protected val _value: TestInterface1.Struct) extends izumi.idealingua.runtime.IRTConversions[TestInterface1.Struct]
  }
  implicit object TestInterface1_downcast_extend_DTO1 extends izumi.idealingua.runtime.IRTExtend[TestInterface1, DTO1] {
    class Call(private val _value: TestInterface1) extends AnyVal {
      def using(if3Field: Long, if2Field: Long): DTO1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DTO1(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect, if3Field = if3Field, if2Field = if2Field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface1): Call = new Call(_value)
  }
  implicit object TestInterface1_downcast_extend_TestInterface1Struct extends izumi.idealingua.runtime.IRTExtend[TestInterface1, TestInterface1.Struct] {
    class Call(private val _value: TestInterface1) extends AnyVal {
      def using(): TestInterface1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface1): Call = new Call(_value)
  }
  implicit object TestInterface1_downcast_extend_TestInterface3Struct extends izumi.idealingua.runtime.IRTExtend[TestInterface1, TestInterface3.Struct] {
    class Call(private val _value: TestInterface1) extends AnyVal {
      def using(if3Field: Long): TestInterface3.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface3.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect, if3Field = if3Field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface1): Call = new Call(_value)
  }
  implicit object TestInterface1_upcast_TestInterface1 extends izumi.idealingua.runtime.IRTCast[TestInterface1, TestInterface1] {
    override def convert(_value: TestInterface1): TestInterface1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect)
    }
  }
  implicit class TestInterface1Extensions(override protected val _value: TestInterface1) extends izumi.idealingua.runtime.IRTConversions[TestInterface1]
}
       