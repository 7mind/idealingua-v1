package izumi.test.domain01



final case class TestValIdentifier(userId: String) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.userId).map(part => escape(part.toString)).mkString(":")
    s"TestValIdentifier#$suffix"
  }
}

trait TestValIdentifierCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestValIdentifier: Encoder[TestValIdentifier] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestValIdentifier: Decoder[TestValIdentifier] = Decoder.decodeString.emapTry(v => Try(TestValIdentifier.parse(v)))
  implicit val encodeKeyTestValIdentifier: KeyEncoder[TestValIdentifier] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestValIdentifier: KeyDecoder[TestValIdentifier] = new KeyDecoder[TestValIdentifier] { final def apply(key: String): Option[TestValIdentifier] = Try(TestValIdentifier.parse(key)).toOption }
}

object TestValIdentifier extends TestValIdentifierCirce {
  def parse(s: String): TestValIdentifier = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("TestValIdentifier#")) {
      val name = "IdentifierId:{izumi.test.domain01}/#TestValIdentifier"
      throw new IllegalArgumentException(s"Serialized form of $name should start with TestValIdentifier#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    TestValIdentifier(userId = parsePart[String](parts(0), classOf[String]))
  }
}
       