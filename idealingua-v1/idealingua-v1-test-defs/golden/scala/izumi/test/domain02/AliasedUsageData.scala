package izumi.test.domain02



final case class AliasedUsageData(testObj: AliasedTestObject, enumField: AliasedGoAliasEnumTest) extends AliasedUsageData.Defn

trait AliasedUsageDataCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeAliasedUsageData: Encoder.AsObject[AliasedUsageData] = deriveEncoder[AliasedUsageData]
  implicit val decodeAliasedUsageData: Decoder[AliasedUsageData] = deriveDecoder[AliasedUsageData]
}

object AliasedUsageData extends AliasedUsageDataCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def testObj: AliasedTestObject
    def enumField: AliasedGoAliasEnumTest
  }
  def apply(testObj: AliasedTestObject, enumField: AliasedGoAliasEnumTest): AliasedUsageData = {
    assert((enumField.asInstanceOf[_root_.scala.AnyRef] ne null) && (testObj.asInstanceOf[_root_.scala.AnyRef] ne null))
    new AliasedUsageData(testObj = testObj, enumField = enumField)
  }
  def apply(defn: AliasedUsageData.Defn): AliasedUsageData = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new AliasedUsageData(testObj = defn.testObj, enumField = defn.enumField)
  }
  implicit object AliasedUsageData_upcast_AliasedUsageData extends izumi.idealingua.runtime.IRTCast[AliasedUsageData, AliasedUsageData] {
    override def convert(_value: AliasedUsageData): AliasedUsageData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AliasedUsageData(testObj = _value.testObj, enumField = _value.enumField)
    }
  }
  implicit class AliasedUsageDataExtensions(override protected val _value: AliasedUsageData) extends izumi.idealingua.runtime.IRTConversions[AliasedUsageData]
}
       