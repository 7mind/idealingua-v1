package izumi.test.domain01



final case class ImportAppId(id: java.util.UUID) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.id).map(part => escape(part.toString)).mkString(":")
    s"ImportAppId#$suffix"
  }
}

trait ImportAppIdCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeImportAppId: Encoder[ImportAppId] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeImportAppId: Decoder[ImportAppId] = Decoder.decodeString.emapTry(v => Try(ImportAppId.parse(v)))
  implicit val encodeKeyImportAppId: KeyEncoder[ImportAppId] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyImportAppId: KeyDecoder[ImportAppId] = new KeyDecoder[ImportAppId] { final def apply(key: String): Option[ImportAppId] = Try(ImportAppId.parse(key)).toOption }
}

object ImportAppId extends ImportAppIdCirce {
  def parse(s: String): ImportAppId = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("ImportAppId#")) {
      val name = "IdentifierId:{izumi.test.domain01}/#ImportAppId"
      throw new IllegalArgumentException(s"Serialized form of $name should start with ImportAppId#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    ImportAppId(id = parsePart[java.util.UUID](parts(0), classOf[java.util.UUID]))
  }
}
       