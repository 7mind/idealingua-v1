package izumi.test.domain01



final case class IDForDomain2(a: Int) extends AnyVal with izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.a).map(part => escape(part.toString)).mkString(":")
    s"IDForDomain2#$suffix"
  }
}

trait IDForDomain2Circe {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeIDForDomain2: Encoder[IDForDomain2] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeIDForDomain2: Decoder[IDForDomain2] = Decoder.decodeString.emapTry(v => Try(IDForDomain2.parse(v)))
  implicit val encodeKeyIDForDomain2: KeyEncoder[IDForDomain2] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyIDForDomain2: KeyDecoder[IDForDomain2] = new KeyDecoder[IDForDomain2] { final def apply(key: String): Option[IDForDomain2] = Try(IDForDomain2.parse(key)).toOption }
}

object IDForDomain2 extends IDForDomain2Circe {
  def parse(s: String): IDForDomain2 = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("IDForDomain2#")) {
      val name = "IdentifierId:{izumi.test.domain01}/#IDForDomain2"
      throw new IllegalArgumentException(s"Serialized form of $name should start with IDForDomain2#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    IDForDomain2(a = parsePart[Int](parts(0), classOf[Int]))
  }
}
       