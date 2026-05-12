package idltest.datainheritancetransitive



trait CouponData extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def validFrom: Option[java.time.LocalDateTime]
  def validTill: Option[java.time.LocalDateTime]
  def code: String
}

trait CouponDataCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeCouponData: Encoder.AsObject[CouponData] = Encoder.AsObject.instance {
    case v: CouponData.Struct =>
      Map("idltest.datainheritancetransitive.CouponData.Struct" -> v).asJsonObject
  }
  implicit val decodeCouponData: Decoder[CouponData] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.datainheritancetransitive.CouponData.Struct" =>
        value.as[CouponData.Struct]
      case _ =>
        val cname = "idltest.datainheritancetransitive.CouponData"
        val alts = List("idltest.datainheritancetransitive.CouponData.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object CouponData extends CouponDataCirce {
  def apply(validFrom: Option[java.time.LocalDateTime], validTill: Option[java.time.LocalDateTime], code: String) = Struct(validFrom, validTill, code)
  final case class Struct(validFrom: Option[java.time.LocalDateTime], validTill: Option[java.time.LocalDateTime], code: String) extends CouponData
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends CouponData.StructCirce {
    def apply(coupondata: CouponData): CouponData.Struct = {
      assert(coupondata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new CouponData.Struct(validFrom = coupondata.validFrom, validTill = coupondata.validTill, code = coupondata.code)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[CouponData.Struct, CouponData.Struct] {
      override def convert(_value: CouponData.Struct): CouponData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CouponData.Struct(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code)
      }
    }
    implicit object Struct_upcast_CouponData extends izumi.idealingua.runtime.IRTCast[CouponData.Struct, CouponData] {
      override def convert(_value: CouponData.Struct): CouponData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CouponData.Struct(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code)
      }
    }
    implicit class StructExtensions(override protected val _value: CouponData.Struct) extends izumi.idealingua.runtime.IRTConversions[CouponData.Struct]
  }
  implicit object CouponData_downcast_extend_Coupon extends izumi.idealingua.runtime.IRTExtend[CouponData, Coupon] {
    class Call(private val _value: CouponData) extends AnyVal {
      def using(id: CouponID): Coupon = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(id.asInstanceOf[_root_.scala.AnyRef] ne null)
        Coupon(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code, id = id)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CouponData): Call = new Call(_value)
  }
  implicit object CouponData_downcast_extend_MassCoupon extends izumi.idealingua.runtime.IRTExtend[CouponData, MassCoupon] {
    class Call(private val _value: CouponData) extends AnyVal {
      def using(id: CouponID, limit: Option[Long]): MassCoupon = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(id.asInstanceOf[_root_.scala.AnyRef] ne null)
        MassCoupon(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code, id = id, limit = limit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CouponData): Call = new Call(_value)
  }
  implicit object CouponData_downcast_extend_CouponDataStruct extends izumi.idealingua.runtime.IRTExtend[CouponData, CouponData.Struct] {
    class Call(private val _value: CouponData) extends AnyVal {
      def using(): CouponData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CouponData.Struct(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CouponData): Call = new Call(_value)
  }
  implicit object CouponData_upcast_CouponData extends izumi.idealingua.runtime.IRTCast[CouponData, CouponData] {
    override def convert(_value: CouponData): CouponData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CouponData.Struct(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code)
    }
  }
  implicit class CouponDataExtensions(override protected val _value: CouponData) extends izumi.idealingua.runtime.IRTConversions[CouponData]
}
       