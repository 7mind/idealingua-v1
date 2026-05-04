package idltest.algebraics



final case class AdtTestID(id: String) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.id).map(part => escape(part.toString)).mkString(":")
    s"AdtTestID#$suffix"
  }
}

trait AdtTestIDCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeAdtTestID: Encoder[AdtTestID] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeAdtTestID: Decoder[AdtTestID] = Decoder.decodeString.emapTry(v => Try(AdtTestID.parse(v)))
  implicit val encodeKeyAdtTestID: KeyEncoder[AdtTestID] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyAdtTestID: KeyDecoder[AdtTestID] = new KeyDecoder[AdtTestID] { final def apply(key: String): Option[AdtTestID] = Try(AdtTestID.parse(key)).toOption }
}

object AdtTestID extends AdtTestIDCirce {
  def parse(s: String): AdtTestID = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("AdtTestID#")) {
      val name = "IdentifierId:{idltest.algebraics}/#AdtTestID"
      throw new IllegalArgumentException(s"Serialized form of $name should start with AdtTestID#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    AdtTestID(id = parsePart[String](parts(0), classOf[String]))
  }
}
       