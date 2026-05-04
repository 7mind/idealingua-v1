package idltest.identifiers



final case class CompanyId(value: java.util.UUID, iid: Long) extends izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.iid, this.value).map(part => escape(part.toString)).mkString(":")
    s"CompanyId#$suffix"
  }
}

trait CompanyIdCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeCompanyId: Encoder[CompanyId] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeCompanyId: Decoder[CompanyId] = Decoder.decodeString.emapTry(v => Try(CompanyId.parse(v)))
  implicit val encodeKeyCompanyId: KeyEncoder[CompanyId] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyCompanyId: KeyDecoder[CompanyId] = new KeyDecoder[CompanyId] { final def apply(key: String): Option[CompanyId] = Try(CompanyId.parse(key)).toOption }
}

object CompanyId extends CompanyIdCirce {
  def parse(s: String): CompanyId = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("CompanyId#")) {
      val name = "IdentifierId:{idltest.identifiers}/#CompanyId"
      throw new IllegalArgumentException(s"Serialized form of $name should start with CompanyId#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    CompanyId(iid = parsePart[Long](parts(0), classOf[Long]), value = parsePart[java.util.UUID](parts(1), classOf[java.util.UUID]))
  }
}
       