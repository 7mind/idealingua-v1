package idltest.anyvals



final case class RecordId(value: java.util.UUID) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.value).map(part => escape(part.toString)).mkString(":")
    s"RecordId#$suffix"
  }
}

trait RecordIdCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeRecordId: Encoder[RecordId] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeRecordId: Decoder[RecordId] = Decoder.decodeString.emapTry(v => Try(RecordId.parse(v)))
  implicit val encodeKeyRecordId: KeyEncoder[RecordId] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyRecordId: KeyDecoder[RecordId] = new KeyDecoder[RecordId] { final def apply(key: String): Option[RecordId] = Try(RecordId.parse(key)).toOption }
}

object RecordId extends RecordIdCirce {
  def parse(s: String): RecordId = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("RecordId#")) {
      val name = "IdentifierId:{idltest.anyvals}/#RecordId"
      throw new IllegalArgumentException(s"Serialized form of $name should start with RecordId#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    RecordId(value = parsePart[java.util.UUID](parts(0), classOf[java.util.UUID]))
  }
}
       