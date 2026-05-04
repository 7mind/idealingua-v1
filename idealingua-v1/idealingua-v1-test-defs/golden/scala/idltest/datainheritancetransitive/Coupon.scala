package idltest.datainheritancetransitive



final case class Coupon(validFrom: Option[java.time.LocalDateTime], validTill: Option[java.time.LocalDateTime], code: String, id: CouponID) extends Coupon.Defn

trait CouponCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeCoupon: Encoder.AsObject[Coupon] = deriveEncoder[Coupon]
  implicit val decodeCoupon: Decoder[Coupon] = deriveDecoder[Coupon]
}

object Coupon extends CouponCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def validFrom: Option[java.time.LocalDateTime]
    def validTill: Option[java.time.LocalDateTime]
    def code: String
    def id: CouponID
  }
  def apply(coupondata: CouponData, id: CouponID): Coupon = {
    assert(coupondata.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Coupon(validFrom = coupondata.validFrom, validTill = coupondata.validTill, code = coupondata.code, id = id)
  }
  def apply(defn: Coupon.Defn): Coupon = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Coupon(validFrom = defn.validFrom, validTill = defn.validTill, code = defn.code, id = defn.id)
  }
  implicit object Coupon_upcast_Coupon extends izumi.idealingua.runtime.IRTCast[Coupon, Coupon] {
    override def convert(_value: Coupon): Coupon = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Coupon(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code, id = _value.id)
    }
  }
  implicit object Coupon_upcast_CouponData extends izumi.idealingua.runtime.IRTCast[Coupon, CouponData] {
    override def convert(_value: Coupon): CouponData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CouponData.Struct(validFrom = _value.validFrom, validTill = _value.validTill, code = _value.code)
    }
  }
  implicit class CouponExtensions(override protected val _value: Coupon) extends izumi.idealingua.runtime.IRTConversions[Coupon]
}
       