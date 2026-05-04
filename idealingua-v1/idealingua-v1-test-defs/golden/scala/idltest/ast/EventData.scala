package idltest.ast



final case class EventData() extends EventData.Defn

trait EventDataCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeEventData: Encoder.AsObject[EventData] = deriveEncoder[EventData]
  implicit val decodeEventData: Decoder[EventData] = deriveDecoder[EventData]
}

object EventData extends EventDataCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
  def apply(defn: EventData.Defn): EventData = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new EventData()
  }
  implicit object EventData_cast_into_PublicData extends izumi.idealingua.runtime.IRTCast[EventData, PublicData] {
    override def convert(_value: EventData): PublicData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PublicData()
    }
  }
  implicit object EventData_upcast_EventData extends izumi.idealingua.runtime.IRTCast[EventData, EventData] {
    override def convert(_value: EventData): EventData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      EventData()
    }
  }
  implicit class EventDataExtensions(override protected val _value: EventData) extends izumi.idealingua.runtime.IRTConversions[EventData]
}
       