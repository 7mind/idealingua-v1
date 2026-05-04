package idltest.syntax



final case class TestId1(value: String) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.value).map(part => escape(part.toString)).mkString(":")
    s"TestId1#$suffix"
  }
}

trait TestId1Circe {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestId1: Encoder[TestId1] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestId1: Decoder[TestId1] = Decoder.decodeString.emapTry(v => Try(TestId1.parse(v)))
  implicit val encodeKeyTestId1: KeyEncoder[TestId1] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestId1: KeyDecoder[TestId1] = new KeyDecoder[TestId1] { final def apply(key: String): Option[TestId1] = Try(TestId1.parse(key)).toOption }
}

object TestId1 extends TestId1Circe {
  def parse(s: String): TestId1 = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("TestId1#")) {
      val name = "IdentifierId:{idltest.syntax}/#TestId1"
      throw new IllegalArgumentException(s"Serialized form of $name should start with TestId1#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    TestId1(value = parsePart[String](parts(0), classOf[String]))
  }
}
       