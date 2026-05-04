package idltest.diamonds



trait TestInterface1 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def if1Field_overriden: Int
  def if1Field_inherited: Int
  def sameField: Long
  def sameEverywhereField: Long
}

trait TestInterface1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTestInterface1: Encoder.AsObject[TestInterface1] = Encoder.AsObject.instance {
    case v: TestInterface1.Struct =>
      Map("idltest.diamonds.TestInterface1.Struct" -> v).asJsonObject
  }
  implicit val decodeTestInterface1: Decoder[TestInterface1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.diamonds.TestInterface1.Struct" =>
        value.as[TestInterface1.Struct]
      case _ =>
        val cname = "idltest.diamonds.TestInterface1"
        val alts = List("idltest.diamonds.TestInterface1.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TestInterface1 extends TestInterface1Circe {
  def apply(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long) = Struct(if1Field_overriden, if1Field_inherited, sameField, sameEverywhereField)
  final case class Struct(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long) extends TestInterface1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TestInterface1.StructCirce {
    def apply(testinterface1: TestInterface1): TestInterface1.Struct = {
      assert(testinterface1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestInterface1.Struct(if1Field_overriden = testinterface1.if1Field_overriden, if1Field_inherited = testinterface1.if1Field_inherited, sameField = testinterface1.sameField, sameEverywhereField = testinterface1.sameEverywhereField)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TestInterface1.Struct, TestInterface1.Struct] {
      override def convert(_value: TestInterface1.Struct): TestInterface1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
      }
    }
    implicit object Struct_upcast_TestInterface1 extends izumi.idealingua.runtime.IRTCast[TestInterface1.Struct, TestInterface1] {
      override def convert(_value: TestInterface1.Struct): TestInterface1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
      }
    }
    implicit class StructExtensions(override protected val _value: TestInterface1.Struct) extends izumi.idealingua.runtime.IRTConversions[TestInterface1.Struct]
  }
  implicit object TestInterface1_downcast_extend_TestInterface1Struct extends izumi.idealingua.runtime.IRTExtend[TestInterface1, TestInterface1.Struct] {
    class Call(private val _value: TestInterface1) extends AnyVal {
      def using(): TestInterface1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestInterface1.Struct(if1Field_inherited = _value.if1Field_inherited, sameEverywhereField = _value.sameEverywhereField, sameField = _value.sameField, if1Field_overriden = _value.if1Field_overriden)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface1): Call = new Call(_value)
  }
  implicit object TestInterface1_downcast_extend_DTO1 extends izumi.idealingua.runtime.IRTExtend[TestInterface1, DTO1] {
    class Call(private val _value: TestInterface1) extends AnyVal {
      def using(sameEverywhereField: Long, sameField: Long, if1Field_overriden: Int, testinterface3: TestInterface3, testinterface2: TestInterface2): DTO1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert((testinterface2.asInstanceOf[_root_.scala.AnyRef] ne null) && (testinterface3.asInstanceOf[_root_.scala.AnyRef] ne null))
        DTO1(if1Field_inherited = _value.if1Field_inherited, sameEverywhereField = sameEverywhereField, sameField = sameField, if1Field_overriden = if1Field_overriden, if3Field = testinterface3.if3Field, if2Field = testinterface2.if2Field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TestInterface1): Call = new Call(_value)
  }
  implicit object TestInterface1_upcast_TestInterface1 extends izumi.idealingua.runtime.IRTCast[TestInterface1, TestInterface1] {
    override def convert(_value: TestInterface1): TestInterface1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
    }
  }
  implicit class TestInterface1Extensions(override protected val _value: TestInterface1) extends izumi.idealingua.runtime.IRTConversions[TestInterface1]
}
       