package idltest.syntax



final case class TestId(value: String) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.value).map(part => escape(part.toString)).mkString(":")
    s"TestId#$suffix"
  }
}

trait TestIdCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestId: Encoder[TestId] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestId: Decoder[TestId] = Decoder.decodeString.emapTry(v => Try(TestId.parse(v)))
  implicit val encodeKeyTestId: KeyEncoder[TestId] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestId: KeyDecoder[TestId] = new KeyDecoder[TestId] { final def apply(key: String): Option[TestId] = Try(TestId.parse(key)).toOption }
}

object TestId extends TestIdCirce {
  def parse(s: String): TestId = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("TestId#")) {
      val name = "IdentifierId:{idltest.syntax}/#TestId"
      throw new IllegalArgumentException(s"Serialized form of $name should start with TestId#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    TestId(value = parsePart[String](parts(0), classOf[String]))
  }
}
       