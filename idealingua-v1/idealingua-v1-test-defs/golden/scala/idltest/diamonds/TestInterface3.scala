package idltest.diamonds



trait TestInterface3 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def if1Field_overriden: Int
  def if1Field_inherited: Int
  def sameField: Long
  def sameEverywhereField: Long
  def if3Field: Long
}

trait TestInterface3Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTestInterface3: Encoder.AsObject[TestInterface3] = Encoder.AsObject.instance {
    case v: TestInterface3.Struct =>
      Map("idltest.diamonds.TestInterface3.Struct" -> v).asJsonObject
  }
  implicit val decodeTestInterface3: Decoder[TestInterface3] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.diamonds.TestInterface3.Struct" =>
        value.as[TestInterface3.Struct]
      case _ =>
        val cname = "idltest.diamonds.TestInterface3"
        val alts = List("idltest.diamonds.TestInterface3.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TestInterface3 extends TestInterface3Circe {
  def apply(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long, if3Field: Long) = Struct(if1Field_overriden, if1Field_inherited, sameField, sameEverywhereField, if3Field)
  final case class Struct(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long, if3Field: Long) extends TestInterface3
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TestInterface3.StructCirce {
    def apply(testinterface1: TestInterface1, testinterface3: TestInterface3, if1Field_overriden: Int, sameEverywhereField: Long): TestInterface3.Struct = {
      assert((testinterface3.asInstanceOf[_root_.scala.AnyRef] ne null) && (testinterface1.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TestInterface3.Struct(if1Field_inherited = testinterface1.if1Field_inherited, sameField = testinterface1.sameField, if3Field = testinterface3.if3Field, if1Field_overriden = if1Field_overriden, sameEverywhereField = sameEverywhereField)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TestInterface3.Struct, TestInterface3.Struct] {
      override def convert(_value: TestInterface3.Struct): TestInterface3.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface3.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, if3Field = _value.if3Field)
      }
    }
    implicit object Struct_upcast_TestInterface3 extends izumi.idealingua.runtime.IRTCast[TestInterface3.Struct, TestInterface3] {
      override def convert(_value: TestInterface3.Struct): TestInterface3 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface3.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, if3Field = _value.if3Field)
      }
    }
    implicit class StructExtensions(override protected val _value: TestInterface3.Struct) extends izumi.idealingua.runtime.IRTConversions[TestInterface3.Struct]
  }
  implicit object TestInterface3_downcast_extend_DTO1 extends izumi.idealingua.runtime.IRTExtend[TestInterface3, DTO1] {
    class Call(private val _value: TestInterface3) extends AnyVal {
      def using(if2Field: Long): DTO1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DTO1(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, if3Field = _value.if3Field, if2Field = if2Field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface3): Call = new Call(_value)
  }
  implicit object TestInterface3_downcast_extend_TestInterface3Struct extends izumi.idealingua.runtime.IRTExtend[TestInterface3, TestInterface3.Struct] {
    class Call(private val _value: TestInterface3) extends AnyVal {
      def using(): TestInterface3.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface3.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, if3Field = _value.if3Field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface3): Call = new Call(_value)
  }
  implicit object TestInterface3_upcast_TestInterface3 extends izumi.idealingua.runtime.IRTCast[TestInterface3, TestInterface3] {
    override def convert(_value: TestInterface3): TestInterface3 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface3.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, if3Field = _value.if3Field)
    }
  }
  implicit object TestInterface3_upcast_TestInterface1 extends izumi.idealingua.runtime.IRTCast[TestInterface3, TestInterface1] {
    override def convert(_value: TestInterface3): TestInterface1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
    }
  }
  implicit class TestInterface3Extensions(override protected val _value: TestInterface3) extends izumi.idealingua.runtime.IRTConversions[TestInterface3]
}
       