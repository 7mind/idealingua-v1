package idltest.blobtest



final case class BlobHolder(payload: Array[Byte], label: String) extends BlobHolder.Defn

trait BlobHolderCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeBlobHolder: Encoder.AsObject[BlobHolder] = deriveEncoder[BlobHolder]
  implicit val decodeBlobHolder: Decoder[BlobHolder] = deriveDecoder[BlobHolder]
}

object BlobHolder extends BlobHolderCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def payload: Array[Byte]
    def label: String
  }
  def apply(payload: Array[Byte], label: String): BlobHolder = {
    new BlobHolder(payload = payload, label = label)
  }
  def apply(defn: BlobHolder.Defn): BlobHolder = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new BlobHolder(payload = defn.payload, label = defn.label)
  }
  implicit object BlobHolder_upcast_BlobHolder extends izumi.idealingua.runtime.IRTCast[BlobHolder, BlobHolder] {
    override def convert(_value: BlobHolder): BlobHolder = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      BlobHolder(payload = _value.payload, label = _value.label)
    }
  }
  implicit class BlobHolderExtensions(override protected val _value: BlobHolder) extends izumi.idealingua.runtime.IRTConversions[BlobHolder]
}
       