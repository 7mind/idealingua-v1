package izumi.test.domain02



trait TestInterface2 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def if2Field: Long
  def sameField: Long
  def sameEverywhereField: Long
}

trait TestInterface2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTestInterface2: Encoder.AsObject[TestInterface2] = Encoder.AsObject.instance {
    case v: TestInterface2.Struct =>
      Map("izumi.test.domain02.TestInterface2.Struct" -> v).asJsonObject
    case v: DTO1 =>
      Map("izumi.test.domain02.DTO1" -> v).asJsonObject
  }
  implicit val decodeTestInterface2: Decoder[TestInterface2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain02.TestInterface2.Struct" =>
        value.as[TestInterface2.Struct]
      case "izumi.test.domain02.DTO1" =>
        value.as[DTO1]
      case _ =>
        val cname = "izumi.test.domain02.TestInterface2"
        val alts = List("izumi.test.domain02.TestInterface2.Struct", "izumi.test.domain02.DTO1").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TestInterface2 extends TestInterface2Circe {
  def apply(if2Field: Long, sameField: Long, sameEverywhereField: Long) = Struct(if2Field, sameField, sameEverywhereField)
  final case class Struct(if2Field: Long, sameField: Long, sameEverywhereField: Long) extends TestInterface2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TestInterface2.StructCirce {
    def apply(testinterface2: TestInterface2): TestInterface2.Struct = {
      assert(testinterface2.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestInterface2.Struct(if2Field = testinterface2.if2Field, sameField = testinterface2.sameField, sameEverywhereField = testinterface2.sameEverywhereField)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TestInterface2.Struct, TestInterface2.Struct] {
      override def convert(_value: TestInterface2.Struct): TestInterface2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface2.Struct(if2Field = _value.if2Field, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
      }
    }
    implicit object Struct_upcast_TestInterface2 extends izumi.idealingua.runtime.IRTCast[TestInterface2.Struct, TestInterface2] {
      override def convert(_value: TestInterface2.Struct): TestInterface2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface2.Struct(if2Field = _value.if2Field, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
      }
    }
    implicit class StructExtensions(override protected val _value: TestInterface2.Struct) extends izumi.idealingua.runtime.IRTConversions[TestInterface2.Struct]
  }
  implicit object TestInterface2_downcast_extend_TestInterface2Struct extends izumi.idealingua.runtime.IRTExtend[TestInterface2, TestInterface2.Struct] {
    class Call(private val _value: TestInterface2) extends AnyVal {
      def using(): TestInterface2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface2.Struct(if2Field = _value.if2Field, sameEverywhereField = _value.sameEverywhereField, sameField = _value.sameField)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface2): Call = new Call(_value)
  }
  implicit object TestInterface2_downcast_extend_DTO1 extends izumi.idealingua.runtime.IRTExtend[TestInterface2, DTO1] {
    class Call(private val _value: TestInterface2) extends AnyVal {
      def using(sameEverywhereField: Long, sameField: Long, if1Field_overriden: Int, testinterface3: TestInterface3, testinterface1: TestInterface1): DTO1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert((testinterface1.asInstanceOf[_root_.scala.AnyRef] ne null) && (testinterface3.asInstanceOf[_root_.scala.AnyRef] ne null))
        DTO1(if2Field = _value.if2Field, sameEverywhereField = sameEverywhereField, sameField = sameField, if1Field_overriden = if1Field_overriden, if3Field = testinterface3.if3Field, fromOtherDomainDirect = testinterface1.fromOtherDomainDirect, if1Field_inherited = testinterface1.if1Field_inherited, fromOtherDomain = testinterface1.fromOtherDomain)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface2): Call = new Call(_value)
  }
  implicit object TestInterface2_upcast_TestInterface2 extends izumi.idealingua.runtime.IRTCast[TestInterface2, TestInterface2] {
    override def convert(_value: TestInterface2): TestInterface2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface2.Struct(if2Field = _value.if2Field, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
    }
  }
  implicit class TestInterface2Extensions(override protected val _value: TestInterface2) extends izumi.idealingua.runtime.IRTConversions[TestInterface2]
}
       