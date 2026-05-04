package idltest.identifiers



final case class ComplexID(bucket: BucketID, user: UserWithEnumId, i32: Int, uid: java.util.UUID, str: String) extends izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.bucket, this.i32, this.str, this.uid, this.user).map(part => escape(part.toString)).mkString(":")
    s"ComplexID#$suffix"
  }
}

trait ComplexIDCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeComplexID: Encoder[ComplexID] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeComplexID: Decoder[ComplexID] = Decoder.decodeString.emapTry(v => Try(ComplexID.parse(v)))
  implicit val encodeKeyComplexID: KeyEncoder[ComplexID] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyComplexID: KeyDecoder[ComplexID] = new KeyDecoder[ComplexID] { final def apply(key: String): Option[ComplexID] = Try(ComplexID.parse(key)).toOption }
}

object ComplexID extends ComplexIDCirce {
  def parse(s: String): ComplexID = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("ComplexID#")) {
      val name = "IdentifierId:{idltest.identifiers}/#ComplexID"
      throw new IllegalArgumentException(s"Serialized form of $name should start with ComplexID#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    ComplexID(bucket = BucketID.parse(parts(0)), i32 = parsePart[Int](parts(1), classOf[Int]), str = parsePart[String](parts(2), classOf[String]), uid = parsePart[java.util.UUID](parts(3), classOf[java.util.UUID]), user = UserWithEnumId.parse(parts(4)))
  }
}
       