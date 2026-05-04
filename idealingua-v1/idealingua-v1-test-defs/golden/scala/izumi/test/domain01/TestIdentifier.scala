package izumi.test.domain01



final case class TestIdentifier(userId: String, context: String, userType: Byte) extends izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.context, this.userId, this.userType).map(part => escape(part.toString)).mkString(":")
    s"TestIdentifier#$suffix"
  }
}

trait TestIdentifierCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestIdentifier: Encoder[TestIdentifier] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestIdentifier: Decoder[TestIdentifier] = Decoder.decodeString.emapTry(v => Try(TestIdentifier.parse(v)))
  implicit val encodeKeyTestIdentifier: KeyEncoder[TestIdentifier] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestIdentifier: KeyDecoder[TestIdentifier] = new KeyDecoder[TestIdentifier] { final def apply(key: String): Option[TestIdentifier] = Try(TestIdentifier.parse(key)).toOption }
}

object TestIdentifier extends TestIdentifierCirce {
  def parse(s: String): TestIdentifier = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("TestIdentifier#")) {
      val name = "IdentifierId:{izumi.test.domain01}/#TestIdentifier"
      throw new IllegalArgumentException(s"Serialized form of $name should start with TestIdentifier#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    TestIdentifier(context = parsePart[String](parts(0), classOf[String]), userId = parsePart[String](parts(1), classOf[String]), userType = parsePart[Byte](parts(2), classOf[Byte]))
  }
}
       