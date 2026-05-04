package izumi.test.domain01



final case class OffsetLimit(offset: Int, limit: Short) extends OffsetLimit.Defn

trait OffsetLimitCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeOffsetLimit: Encoder.AsObject[OffsetLimit] = deriveEncoder[OffsetLimit]
  implicit val decodeOffsetLimit: Decoder[OffsetLimit] = deriveDecoder[OffsetLimit]
}

object OffsetLimit extends OffsetLimitCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def offset: Int
    def limit: Short
  }
  def apply(offset: Int, limit: Short): OffsetLimit = {
    new OffsetLimit(offset = offset, limit = limit)
  }
  def apply(defn: OffsetLimit.Defn): OffsetLimit = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new OffsetLimit(offset = defn.offset, limit = defn.limit)
  }
  implicit object OffsetLimit_upcast_OffsetLimit extends izumi.idealingua.runtime.IRTCast[OffsetLimit, OffsetLimit] {
    override def convert(_value: OffsetLimit): OffsetLimit = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      OffsetLimit(offset = _value.offset, limit = _value.limit)
    }
  }
  implicit class OffsetLimitExtensions(override protected val _value: OffsetLimit) extends izumi.idealingua.runtime.IRTConversions[OffsetLimit]
}
       