package idltest.ast



final case class PublicData() extends PublicData.Defn

trait PublicDataCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePublicData: Encoder.AsObject[PublicData] = deriveEncoder[PublicData]
  implicit val decodePublicData: Decoder[PublicData] = deriveDecoder[PublicData]
}

object PublicData extends PublicDataCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
  def apply(defn: PublicData.Defn): PublicData = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new PublicData()
  }
  implicit object PublicData_cast_into_EventData extends izumi.idealingua.runtime.IRTCast[PublicData, EventData] {
    override def convert(_value: PublicData): EventData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      EventData()
    }
  }
  implicit object PublicData_upcast_PublicData extends izumi.idealingua.runtime.IRTCast[PublicData, PublicData] {
    override def convert(_value: PublicData): PublicData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PublicData()
    }
  }
  implicit class PublicDataExtensions(override protected val _value: PublicData) extends izumi.idealingua.runtime.IRTConversions[PublicData]
}
       