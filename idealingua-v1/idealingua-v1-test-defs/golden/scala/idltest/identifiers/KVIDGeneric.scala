package idltest.identifiers



final case class KVIDGeneric(test: Map[String, BucketID]) extends KVIDGeneric.Defn

trait KVIDGenericCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeKVIDGeneric: Encoder.AsObject[KVIDGeneric] = deriveEncoder[KVIDGeneric]
  implicit val decodeKVIDGeneric: Decoder[KVIDGeneric] = deriveDecoder[KVIDGeneric]
}

object KVIDGeneric extends KVIDGenericCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def test: Map[String, BucketID] }
  def apply(test: Map[String, BucketID]): KVIDGeneric = {
    new KVIDGeneric(test = test)
  }
  def apply(defn: KVIDGeneric.Defn): KVIDGeneric = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new KVIDGeneric(test = defn.test)
  }
  implicit object KVIDGeneric_upcast_KVIDGeneric extends izumi.idealingua.runtime.IRTCast[KVIDGeneric, KVIDGeneric] {
    override def convert(_value: KVIDGeneric): KVIDGeneric = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      KVIDGeneric(test = _value.test)
    }
  }
  implicit class KVIDGenericExtensions(override protected val _value: KVIDGeneric) extends izumi.idealingua.runtime.IRTConversions[KVIDGeneric]
}
       