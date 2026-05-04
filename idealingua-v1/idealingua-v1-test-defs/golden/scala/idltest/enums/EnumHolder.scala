package idltest.enums



final case class EnumHolder(en: TestEnum) extends AnyVal with EnumHolder.Defn

trait EnumHolderCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeEnumHolder: Encoder.AsObject[EnumHolder] = Encoder.forProduct1[EnumHolder, TestEnum]("en")((v: EnumHolder) => v.en)
  implicit val decodeEnumHolder: Decoder[EnumHolder] = Decoder.forProduct1[EnumHolder, TestEnum]("en")((d: TestEnum) => new EnumHolder(d))
}

object EnumHolder extends EnumHolderCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def en: TestEnum }
  def apply(en: TestEnum): EnumHolder = {
    assert(en.asInstanceOf[_root_.scala.AnyRef] ne null)
    new EnumHolder(en = en)
  }
  def apply(defn: EnumHolder.Defn): EnumHolder = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new EnumHolder(en = defn.en)
  }
  implicit object EnumHolder_upcast_EnumHolder extends izumi.idealingua.runtime.IRTCast[EnumHolder, EnumHolder] {
    override def convert(_value: EnumHolder): EnumHolder = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      EnumHolder(en = _value.en)
    }
  }
  implicit class EnumHolderExtensions(override protected val _value: EnumHolder) extends izumi.idealingua.runtime.IRTConversions[EnumHolder]
}
       