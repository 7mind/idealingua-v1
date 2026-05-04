package idltest.identifiers



final case class UserId(value: java.util.UUID, company: java.util.UUID) extends izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.company, this.value).map(part => escape(part.toString)).mkString(":")
    s"UserId#$suffix"
  }
}

trait UserIdCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeUserId: Encoder[UserId] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeUserId: Decoder[UserId] = Decoder.decodeString.emapTry(v => Try(UserId.parse(v)))
  implicit val encodeKeyUserId: KeyEncoder[UserId] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyUserId: KeyDecoder[UserId] = new KeyDecoder[UserId] { final def apply(key: String): Option[UserId] = Try(UserId.parse(key)).toOption }
}

object UserId extends UserIdCirce {
  def parse(s: String): UserId = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("UserId#")) {
      val name = "IdentifierId:{idltest.identifiers}/#UserId"
      throw new IllegalArgumentException(s"Serialized form of $name should start with UserId#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    UserId(company = parsePart[java.util.UUID](parts(0), classOf[java.util.UUID]), value = parsePart[java.util.UUID](parts(1), classOf[java.util.UUID]))
  }
}
       