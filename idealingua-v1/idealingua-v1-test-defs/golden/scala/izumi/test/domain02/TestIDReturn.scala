package izumi.test.domain02



final case class TestIDReturn(a: Int) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.a).map(part => escape(part.toString)).mkString(":")
    s"TestIDReturn#$suffix"
  }
}

trait TestIDReturnCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeTestIDReturn: Encoder[TestIDReturn] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestIDReturn: Decoder[TestIDReturn] = Decoder.decodeString.emapTry(v => Try(TestIDReturn.parse(v)))
  implicit val encodeKeyTestIDReturn: KeyEncoder[TestIDReturn] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestIDReturn: KeyDecoder[TestIDReturn] = new KeyDecoder[TestIDReturn] { final def apply(key: String): Option[TestIDReturn] = Try(TestIDReturn.parse(key)).toOption }
}

object TestIDReturn extends TestIDReturnCirce {
  def parse(s: String): TestIDReturn = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("TestIDReturn#")) {
      val name = "IdentifierId:{izumi.test.domain02}/#TestIDReturn"
      throw new IllegalArgumentException(s"Serialized form of $name should start with TestIDReturn#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    TestIDReturn(a = parsePart[Int](parts(0), classOf[Int]))
  }
}
       