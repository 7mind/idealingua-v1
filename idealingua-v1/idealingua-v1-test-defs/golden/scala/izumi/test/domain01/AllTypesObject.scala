package izumi.test.domain01



final case class AllTypesObject(b: Boolean, s: String, int8: Byte, int16: Short, int32: Int, int64: Long, f: Float, d: Double, uuid: java.util.UUID, ts: java.time.ZonedDateTime, tslocal: java.time.LocalDateTime, tsuni: java.time.ZonedDateTime, time: java.time.LocalTime, date: java.time.LocalDate, uint8: Byte, uint16: Short, uint32: Int, uint64: Long, list: List[AllTypes], another: List[AllTypes], selfMap: Map[String, AllTypes], enumMap: Map[String, GoAliasEnumTest], option: Option[AllTypes], selfSet: Set[AllTypes], optionDate: Option[java.time.LocalDateTime], optionTime: Option[java.time.LocalTime]) extends AllTypes with AllTypesObject.Defn

trait AllTypesObjectCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeAllTypesObject: Encoder.AsObject[AllTypesObject] = deriveEncoder[AllTypesObject]
  implicit val decodeAllTypesObject: Decoder[AllTypesObject] = deriveDecoder[AllTypesObject]
}

object AllTypesObject extends AllTypesObjectCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def b: Boolean
    def s: String
    def int8: Byte
    def int16: Short
    def int32: Int
    def int64: Long
    def f: Float
    def d: Double
    def uuid: java.util.UUID
    def ts: java.time.ZonedDateTime
    def tslocal: java.time.LocalDateTime
    def tsuni: java.time.ZonedDateTime
    def time: java.time.LocalTime
    def date: java.time.LocalDate
    def uint8: Byte
    def uint16: Short
    def uint32: Int
    def uint64: Long
    def list: List[AllTypes]
    def another: List[AllTypes]
    def selfMap: Map[String, AllTypes]
    def enumMap: Map[String, GoAliasEnumTest]
    def option: Option[AllTypes]
    def selfSet: Set[AllTypes]
    def optionDate: Option[java.time.LocalDateTime]
    def optionTime: Option[java.time.LocalTime]
  }
  def apply(alltypes: AllTypes): AllTypesObject = {
    assert(alltypes.asInstanceOf[_root_.scala.AnyRef] ne null)
    new AllTypesObject(b = alltypes.b, s = alltypes.s, int8 = alltypes.int8, int16 = alltypes.int16, int32 = alltypes.int32, int64 = alltypes.int64, f = alltypes.f, d = alltypes.d, uuid = alltypes.uuid, ts = alltypes.ts, tslocal = alltypes.tslocal, tsuni = alltypes.tsuni, time = alltypes.time, date = alltypes.date, uint8 = alltypes.uint8, uint16 = alltypes.uint16, uint32 = alltypes.uint32, uint64 = alltypes.uint64, list = alltypes.list, another = alltypes.another, selfMap = alltypes.selfMap, enumMap = alltypes.enumMap, option = alltypes.option, selfSet = alltypes.selfSet, optionDate = alltypes.optionDate, optionTime = alltypes.optionTime)
  }
  def apply(defn: AllTypesObject.Defn): AllTypesObject = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new AllTypesObject(b = defn.b, s = defn.s, int8 = defn.int8, int16 = defn.int16, int32 = defn.int32, int64 = defn.int64, f = defn.f, d = defn.d, uuid = defn.uuid, ts = defn.ts, tslocal = defn.tslocal, tsuni = defn.tsuni, time = defn.time, date = defn.date, uint8 = defn.uint8, uint16 = defn.uint16, uint32 = defn.uint32, uint64 = defn.uint64, list = defn.list, another = defn.another, selfMap = defn.selfMap, enumMap = defn.enumMap, option = defn.option, selfSet = defn.selfSet, optionDate = defn.optionDate, optionTime = defn.optionTime)
  }
  implicit object AllTypesObject_cast_into_AllTypesStruct extends izumi.idealingua.runtime.IRTCast[AllTypesObject, AllTypes.Struct] {
    override def convert(_value: AllTypesObject): AllTypes.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AllTypes.Struct(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
    }
  }
  implicit object AllTypesObject_upcast_AllTypesObject extends izumi.idealingua.runtime.IRTCast[AllTypesObject, AllTypesObject] {
    override def convert(_value: AllTypesObject): AllTypesObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AllTypesObject(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
    }
  }
  implicit object AllTypesObject_upcast_AllTypes extends izumi.idealingua.runtime.IRTCast[AllTypesObject, AllTypes] {
    override def convert(_value: AllTypesObject): AllTypes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AllTypes.Struct(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
    }
  }
  implicit class AllTypesObjectExtensions(override protected val _value: AllTypesObject) extends izumi.idealingua.runtime.IRTConversions[AllTypesObject]
}
       