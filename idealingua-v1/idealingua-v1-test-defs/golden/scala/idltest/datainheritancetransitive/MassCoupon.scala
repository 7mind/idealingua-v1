package idltest.datainheritancetransitive



final case class MassCoupon(validFrom: Option[java.time.LocalDateTime], validTill: Option[java.time.LocalDateTime], code: String, id: CouponID, limit: Option[Long]) extends MassCoupon.Defn

trait MassCouponCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeMassCoupon: Encoder.AsObject[MassCoupon] = deriveEncoder[MassCoupon]
  implicit val decodeMassCoupon: Decoder[MassCoupon] = deriveDecoder[MassCoupon]
}

object MassCoupon extends MassCouponCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def validFrom: Option[java.time.LocalDateTime]
    def validTill: Option[java.time.LocalDateTime]
    def code: String
    def id: CouponID
    def limit: Option[Long]
  }
  def apply(coupondata: CouponData, coupon: Coupon.Defn, code: String, limit: Option[Long]): MassCoupon = {
    assert((coupon.asInstanceOf[_root_.scala.AnyRef] ne null) && (coupondata.asInstanceOf[_root_.scala.AnyRef] ne null))
    new MassCoupon(validFrom = coupondata.validFrom, validTill = coupondata.validTill, id = coupon.id, code = code, limit = limit)
  }
  def apply(defn: MassCoupon.Defn): MassCoupon = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new MassCoupon(validFrom = defn.validFrom, validTill = defn.validTill, code = defn.code, id = defn.id, limit = defn.limit)
  }
  implicit object MassCoupon_upcast_MassCoupon extends izumi.idealingua.runtime.IRTCast[MassCoupon, MassCoupon] {
    override def convert(_value: MassCoupon): MassCoupon = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      MassCoupon(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code, id = _value.id, limit = _value.limit)
    }
  }
  implicit object MassCoupon_upcast_CouponData extends izumi.idealingua.runtime.IRTCast[MassCoupon, CouponData] {
    override def convert(_value: MassCoupon): CouponData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CouponData.Struct(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code)
    }
  }
  implicit class MassCouponExtensions(override protected val _value: MassCoupon) extends izumi.idealingua.runtime.IRTConversions[MassCoupon]
}
       