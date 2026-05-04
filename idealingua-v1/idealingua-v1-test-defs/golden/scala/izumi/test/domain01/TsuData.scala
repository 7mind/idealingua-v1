package izumi.test.domain01



final case class TsuData(since: java.time.ZonedDateTime) extends AnyVal with TsuData.Defn

trait TsuDataCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeTsuData: Encoder.AsObject[TsuData] = Encoder.forProduct1[TsuData, java.time.ZonedDateTime]("since")((v: TsuData) => v.since)
  implicit val decodeTsuData: Decoder[TsuData] = Decoder.forProduct1[TsuData, java.time.ZonedDateTime]("since")((d: java.time.ZonedDateTime) => new TsuData(d))
}

object TsuData extends TsuDataCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def since: java.time.ZonedDateTime }
  def apply(since: java.time.ZonedDateTime): TsuData = {
    new TsuData(since = since)
  }
  def apply(defn: TsuData.Defn): TsuData = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new TsuData(since = defn.since)
  }
  implicit object TsuData_upcast_TsuData extends izumi.idealingua.runtime.IRTCast[TsuData, TsuData] {
    override def convert(_value: TsuData): TsuData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TsuData(since = _value.since)
    }
  }
  implicit class TsuDataExtensions(override protected val _value: TsuData) extends izumi.idealingua.runtime.IRTConversions[TsuData]
}
       