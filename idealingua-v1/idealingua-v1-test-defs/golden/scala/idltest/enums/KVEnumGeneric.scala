package idltest.enums



final case class KVEnumGeneric(test: Map[String, TestEnum]) extends KVEnumGeneric.Defn

trait KVEnumGenericCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeKVEnumGeneric: Encoder.AsObject[KVEnumGeneric] = deriveEncoder[KVEnumGeneric]
  implicit val decodeKVEnumGeneric: Decoder[KVEnumGeneric] = deriveDecoder[KVEnumGeneric]
}

object KVEnumGeneric extends KVEnumGenericCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def test: Map[String, TestEnum] }
  def apply(test: Map[String, TestEnum]): KVEnumGeneric = {
    new KVEnumGeneric(test = test)
  }
  def apply(defn: KVEnumGeneric.Defn): KVEnumGeneric = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new KVEnumGeneric(test = defn.test)
  }
  implicit object KVEnumGeneric_upcast_KVEnumGeneric extends izumi.idealingua.runtime.IRTCast[KVEnumGeneric, KVEnumGeneric] {
    override def convert(_value: KVEnumGeneric): KVEnumGeneric = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      KVEnumGeneric(test = _value.test)
    }
  }
  implicit class KVEnumGenericExtensions(override protected val _value: KVEnumGeneric) extends izumi.idealingua.runtime.IRTConversions[KVEnumGeneric]
}
       