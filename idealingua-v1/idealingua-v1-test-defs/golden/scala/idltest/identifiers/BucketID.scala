package idltest.identifiers



final case class BucketID(app: java.util.UUID, user: java.util.UUID, bucket: String) extends izumi.idealingua.runtime.model.IDLGeneratedType with izumi.idealingua.runtime.model.IDLIdentifier {
  override def toString: String = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    val suffix = Seq(this.app, this.bucket, this.user).map(part => escape(part.toString)).mkString(":")
    s"BucketID#$suffix"
  }
}

trait BucketIDCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeBucketID: Encoder[BucketID] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeBucketID: Decoder[BucketID] = Decoder.decodeString.emapTry(v => Try(BucketID.parse(v)))
  implicit val encodeKeyBucketID: KeyEncoder[BucketID] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyBucketID: KeyDecoder[BucketID] = new KeyDecoder[BucketID] { final def apply(key: String): Option[BucketID] = Try(BucketID.parse(key)).toOption }
}

object BucketID extends BucketIDCirce {
  def parse(s: String): BucketID = {
    import izumi.idealingua.runtime.model.IDLIdentifier.*
    if (!s.startsWith("BucketID#")) {
      val name = "IdentifierId:{idltest.identifiers}/#BucketID"
      throw new IllegalArgumentException(s"Serialized form of $name should start with BucketID#")
    }
    val withoutPrefix = s.substring(s.indexOf("#") + 1)
    val parts = withoutPrefix.split(':').map(part => unescape(part))
    BucketID(app = parsePart[java.util.UUID](parts(0), classOf[java.util.UUID]), bucket = parsePart[String](parts(1), classOf[String]), user = parsePart[java.util.UUID](parts(2), classOf[java.util.UUID]))
  }
}
       