package izumi.test.domain02



final case class DTO1(if1Field_overriden: Int, if1Field_inherited: Int, sameField: Long, sameEverywhereField: Long, fromOtherDomain: izumi.test.domain01.TestValIdentifier, fromOtherDomainDirect: izumi.test.domain01.TestValIdentifier, if3Field: Long, if2Field: Long) extends TestInterface2 with TestInterface3 with DTO1.Defn

trait DTO1Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeDTO1: Encoder.AsObject[DTO1] = deriveEncoder[DTO1]
  implicit val decodeDTO1: Decoder[DTO1] = deriveDecoder[DTO1]
}

object DTO1 extends DTO1Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def if1Field_overriden: Int
    def if1Field_inherited: Int
    def sameField: Long
    def sameEverywhereField: Long
    def fromOtherDomain: izumi.test.domain01.TestValIdentifier
    def fromOtherDomainDirect: izumi.test.domain01.TestValIdentifier
    def if3Field: Long
    def if2Field: Long
  }
  def apply(testinterface1: TestInterface1, testinterface3: TestInterface3, testinterface2: TestInterface2, if1Field_overriden: Int, sameField: Long, sameEverywhereField: Long): DTO1 = {
    assert((testinterface2.asInstanceOf[_root_.scala.AnyRef] ne null) && ((testinterface3.asInstanceOf[_root_.scala.AnyRef] ne null) && (testinterface1.asInstanceOf[_root_.scala.AnyRef] ne null)))
    new DTO1(if1Field_inherited = testinterface1.if1Field_inherited, fromOtherDomain = testinterface1.fromOtherDomain, fromOtherDomainDirect = testinterface1.fromOtherDomainDirect, if3Field = testinterface3.if3Field, if2Field = testinterface2.if2Field, if1Field_overriden = if1Field_overriden, sameField = sameField, sameEverywhereField = sameEverywhereField)
  }
  def apply(defn: DTO1.Defn): DTO1 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new DTO1(if1Field_overriden = defn.if1Field_overriden, if1Field_inherited = defn.if1Field_inherited, sameField = defn.sameField, sameEverywhereField = defn.sameEverywhereField, fromOtherDomain = defn.fromOtherDomain, fromOtherDomainDirect = defn.fromOtherDomainDirect, if3Field = defn.if3Field, if2Field = defn.if2Field)
  }
  implicit object DTO1_upcast_DTO1 extends izumi.idealingua.runtime.IRTCast[DTO1, DTO1] {
    override def convert(_value: DTO1): DTO1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DTO1(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect, if3Field = _value.if3Field, if2Field = _value.if2Field)
    }
  }
  implicit object DTO1_upcast_TestInterface2 extends izumi.idealingua.runtime.IRTCast[DTO1, TestInterface2] {
    override def convert(_value: DTO1): TestInterface2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface2.Struct(if2Field = _value.if2Field, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField)
    }
  }
  implicit object DTO1_upcast_TestInterface3 extends izumi.idealingua.runtime.IRTCast[DTO1, TestInterface3] {
    override def convert(_value: DTO1): TestInterface3 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface3.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect, if3Field = _value.if3Field)
    }
  }
  implicit object DTO1_upcast_TestInterface1 extends izumi.idealingua.runtime.IRTCast[DTO1, TestInterface1] {
    override def convert(_value: DTO1): TestInterface1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestInterface1.Struct(if1Field_overriden = _value.if1Field_overriden, if1Field_inherited = _value.if1Field_inherited, sameField = _value.sameField, sameEverywhereField = _value.sameEverywhereField, fromOtherDomain = _value.fromOtherDomain, fromOtherDomainDirect = _value.fromOtherDomainDirect)
    }
  }
  implicit class DTO1Extensions(override protected val _value: DTO1) extends izumi.idealingua.runtime.IRTConversions[DTO1]
}
       