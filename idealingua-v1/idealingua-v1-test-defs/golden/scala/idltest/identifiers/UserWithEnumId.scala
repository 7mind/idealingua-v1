package idltest.identifiers



final case class UserWithEnumId(value: java.util.UUID, company: java.util.UUID, dept: DepartmentEnum) extends izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.company, this.dept, this.value).map(part => escape(part.toString)).mkString(":")
    s"UserWithEnumId#$suffix"
  }
}

trait UserWithEnumIdCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeUserWithEnumId: Encoder[UserWithEnumId] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeUserWithEnumId: Decoder[UserWithEnumId] = Decoder.decodeString.emapTry(v => Try(UserWithEnumId.parse(v)))
  implicit val encodeKeyUserWithEnumId: KeyEncoder[UserWithEnumId] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyUserWithEnumId: KeyDecoder[UserWithEnumId] = new KeyDecoder[UserWithEnumId] { final def apply(key: String): Option[UserWithEnumId] = Try(UserWithEnumId.parse(key)).toOption }
}

object UserWithEnumId extends UserWithEnumIdCirce {
  def parse(s: String): UserWithEnumId = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("UserWithEnumId#")) {
      val name = "IdentifierId:{idltest.identifiers}/#UserWithEnumId"
      throw new IllegalArgumentException(s"Serialized form of $name should start with UserWithEnumId#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    UserWithEnumId(company = parsePart[java.util.UUID](parts(0), classOf[java.util.UUID]), dept = DepartmentEnum.parse(parts(1)), value = parsePart[java.util.UUID](parts(2), classOf[java.util.UUID]))
  }
}
       