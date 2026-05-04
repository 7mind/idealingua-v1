package idltest.substraction



final case class PublicUser2(name: String) extends AnyVal with PublicUser2.Defn

trait PublicUser2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePublicUser2: Encoder.AsObject[PublicUser2] = Encoder.forProduct1[PublicUser2, String]("name")((v: PublicUser2) => v.name)
  implicit val decodePublicUser2: Decoder[PublicUser2] = Decoder.forProduct1[PublicUser2, String]("name")((d: String) => new PublicUser2(d))
}

object PublicUser2 extends PublicUser2Circe {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def name: String }
  def apply(user2: User2.Defn): PublicUser2 = {
    assert(user2.asInstanceOf[_root_.scala.AnyRef] ne null)
    new PublicUser2(name = user2.name)
  }
  def apply(defn: PublicUser2.Defn): PublicUser2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new PublicUser2(name = defn.name)
  }
  implicit object PublicUser2_cast_into_PublicUser1Struct extends izumi.idealingua.runtime.IRTCast[PublicUser2, PublicUser1.Struct] {
    override def convert(_value: PublicUser2): PublicUser1.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PublicUser1.Struct(name = _value.name)
    }
  }
  implicit object PublicUser2_upcast_PublicUser2 extends izumi.idealingua.runtime.IRTCast[PublicUser2, PublicUser2] {
    override def convert(_value: PublicUser2): PublicUser2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PublicUser2(name = _value.name)
    }
  }
  implicit class PublicUser2Extensions(override protected val _value: PublicUser2) extends izumi.idealingua.runtime.IRTConversions[PublicUser2]
}
       