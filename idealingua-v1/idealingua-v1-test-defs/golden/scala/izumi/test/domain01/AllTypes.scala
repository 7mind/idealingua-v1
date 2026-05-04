package izumi.test.domain01



trait AllTypes extends izumi.idealingua.runtime.model.IDLGeneratedType {
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

trait AllTypesCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAllTypes: Encoder.AsObject[AllTypes] = Encoder.AsObject.instance {
    case v: AllTypesObject =>
      Map("izumi.test.domain01.AllTypesObject" -> v).asJsonObject
    case v: AllTypes.Struct =>
      Map("izumi.test.domain01.AllTypes.Struct" -> v).asJsonObject
  }
  implicit val decodeAllTypes: Decoder[AllTypes] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.AllTypesObject" =>
        value.as[AllTypesObject]
      case "izumi.test.domain01.AllTypes.Struct" =>
        value.as[AllTypes.Struct]
      case _ =>
        val cname = "izumi.test.domain01.AllTypes"
        val alts = List("izumi.test.domain01.AllTypesObject", "izumi.test.domain01.AllTypes.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object AllTypes extends AllTypesCirce {
  def apply(b: Boolean, s: String, int8: Byte, int16: Short, int32: Int, int64: Long, f: Float, d: Double, uuid: java.util.UUID, ts: java.time.ZonedDateTime, tslocal: java.time.LocalDateTime, tsuni: java.time.ZonedDateTime, time: java.time.LocalTime, date: java.time.LocalDate, uint8: Byte, uint16: Short, uint32: Int, uint64: Long, list: List[AllTypes], another: List[AllTypes], selfMap: Map[String, AllTypes], enumMap: Map[String, GoAliasEnumTest], option: Option[AllTypes], selfSet: Set[AllTypes], optionDate: Option[java.time.LocalDateTime], optionTime: Option[java.time.LocalTime]) = Struct(b, s, int8, int16, int32, int64, f, d, uuid, ts, tslocal, tsuni, time, date, uint8, uint16, uint32, uint64, list, another, selfMap, enumMap, option, selfSet, optionDate, optionTime)
  final case class Struct(b: Boolean, s: String, int8: Byte, int16: Short, int32: Int, int64: Long, f: Float, d: Double, uuid: java.util.UUID, ts: java.time.ZonedDateTime, tslocal: java.time.LocalDateTime, tsuni: java.time.ZonedDateTime, time: java.time.LocalTime, date: java.time.LocalDate, uint8: Byte, uint16: Short, uint32: Int, uint64: Long, list: List[AllTypes], another: List[AllTypes], selfMap: Map[String, AllTypes], enumMap: Map[String, GoAliasEnumTest], option: Option[AllTypes], selfSet: Set[AllTypes], optionDate: Option[java.time.LocalDateTime], optionTime: Option[java.time.LocalTime]) extends AllTypes
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends AllTypes.StructCirce {
    def apply(alltypes: AllTypes): AllTypes.Struct = {
      assert(alltypes.asInstanceOf[_root_.scala.AnyRef] ne null)
      new AllTypes.Struct(b = alltypes.b, s = alltypes.s, int8 = alltypes.int8, int16 = alltypes.int16, int32 = alltypes.int32, int64 = alltypes.int64, f = alltypes.f, d = alltypes.d, uuid = alltypes.uuid, ts = alltypes.ts, tslocal = alltypes.tslocal, tsuni = alltypes.tsuni, time = alltypes.time, date = alltypes.date, uint8 = alltypes.uint8, uint16 = alltypes.uint16, uint32 = alltypes.uint32, uint64 = alltypes.uint64, list = alltypes.list, another = alltypes.another, selfMap = alltypes.selfMap, enumMap = alltypes.enumMap, option = alltypes.option, selfSet = alltypes.selfSet, optionDate = alltypes.optionDate, optionTime = alltypes.optionTime)
    }
    implicit object Struct_cast_into_AllTypesObject extends izumi.idealingua.runtime.IRTCast[AllTypes.Struct, AllTypesObject] {
      override def convert(_value: AllTypes.Struct): AllTypesObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AllTypesObject(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[AllTypes.Struct, AllTypes.Struct] {
      override def convert(_value: AllTypes.Struct): AllTypes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AllTypes.Struct(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
      }
    }
    implicit object Struct_upcast_AllTypes extends izumi.idealingua.runtime.IRTCast[AllTypes.Struct, AllTypes] {
      override def convert(_value: AllTypes.Struct): AllTypes = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AllTypes.Struct(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
      }
    }
    implicit class StructExtensions(override protected val _value: AllTypes.Struct) extends izumi.idealingua.runtime.IRTConversions[AllTypes.Struct]
  }
  implicit object AllTypes_downcast_extend_AllTypesObject extends izumi.idealingua.runtime.IRTExtend[AllTypes, AllTypesObject] {
    class Call(private val _value: AllTypes) extends AnyVal {
      def using(): AllTypesObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AllTypesObject(int8 = _value.int8, date = _value.date, tslocal = _value.tslocal, f = _value.f, uint64 = _value.uint64, enumMap = _value.enumMap, int16 = _value.int16, selfSet = _value.selfSet, optionDate = _value.optionDate, int64 = _value.int64, optionTime = _value.optionTime, uint32 = _value.uint32, uuid = _value.uuid, int32 = _value.int32, ts = _value.ts, uint8 = _value.uint8, d = _value.d, b = _value.b, selfMap = _value.selfMap, s = _value.s, tsuni = _value.tsuni, uint16 = _value.uint16, another = _value.another, list = _value.list, option = _value.option, time = _value.time)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AllTypes): Call = new Call(_value)
  }
  implicit object AllTypes_downcast_extend_AllTypesStruct extends izumi.idealingua.runtime.IRTExtend[AllTypes, AllTypes.Struct] {
    class Call(private val _value: AllTypes) extends AnyVal {
      def using(): AllTypes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AllTypes.Struct(int8 = _value.int8, date = _value.date, tslocal = _value.tslocal, f = _value.f, uint64 = _value.uint64, enumMap = _value.enumMap, int16 = _value.int16, selfSet = _value.selfSet, optionDate = _value.optionDate, int64 = _value.int64, optionTime = _value.optionTime, uint32 = _value.uint32, uuid = _value.uuid, int32 = _value.int32, ts = _value.ts, uint8 = _value.uint8, d = _value.d, b = _value.b, selfMap = _value.selfMap, s = _value.s, tsuni = _value.tsuni, uint16 = _value.uint16, another = _value.another, list = _value.list, option = _value.option, time = _value.time)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AllTypes): Call = new Call(_value)
  }
  implicit object AllTypes_upcast_AllTypes extends izumi.idealingua.runtime.IRTCast[AllTypes, AllTypes] {
    override def convert(_value: AllTypes): AllTypes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AllTypes.Struct(b = _value.b, s = _value.s, int8 = _value.int8, int16 = _value.int16, int32 = _value.int32, int64 = _value.int64, f = _value.f, d = _value.d, uuid = _value.uuid, ts = _value.ts, tslocal = _value.tslocal, tsuni = _value.tsuni, time = _value.time, date = _value.date, uint8 = _value.uint8, uint16 = _value.uint16, uint32 = _value.uint32, uint64 = _value.uint64, list = _value.list, another = _value.another, selfMap = _value.selfMap, enumMap = _value.enumMap, option = _value.option, selfSet = _value.selfSet, optionDate = _value.optionDate, optionTime = _value.optionTime)
    }
  }
  implicit class AllTypesExtensions(override protected val _value: AllTypes) extends izumi.idealingua.runtime.IRTConversions[AllTypes]
}
       