package izumi.test.domain01



final case class AnotherTestObject(parent_embedded: String, parent: String, embedded: Boolean, own: Byte) extends AnotherTestObject.Defn

trait AnotherTestObjectCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeAnotherTestObject: Encoder.AsObject[AnotherTestObject] = deriveEncoder[AnotherTestObject]
  implicit val decodeAnotherTestObject: Decoder[AnotherTestObject] = deriveDecoder[AnotherTestObject]
}

object AnotherTestObject extends AnotherTestObjectCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def parent_embedded: String
    def parent: String
    def embedded: Boolean
    def own: Byte
  }
  def apply(privatemixinprivateparent: PrivateMixinPrivateParent, privatemixinparent: PrivateMixinParent, privatemixin: PrivateMixin, extendedmixin: ExtendedMixin): AnotherTestObject = {
    assert((extendedmixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixinparent.asInstanceOf[_root_.scala.AnyRef] ne null) && (privatemixinprivateparent.asInstanceOf[_root_.scala.AnyRef] ne null))))
    new AnotherTestObject(parent_embedded = privatemixinprivateparent.parent_embedded, parent = privatemixinparent.parent, embedded = privatemixin.embedded, own = extendedmixin.own)
  }
  def apply(defn: AnotherTestObject.Defn): AnotherTestObject = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new AnotherTestObject(parent_embedded = defn.parent_embedded, parent = defn.parent, embedded = defn.embedded, own = defn.own)
  }
  implicit object AnotherTestObject_cast_into_PrivateTestObject extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, PrivateTestObject] {
    override def convert(_value: AnotherTestObject): PrivateTestObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object AnotherTestObject_cast_into_ExtendedMixinStruct extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, ExtendedMixin.Struct] {
    override def convert(_value: AnotherTestObject): ExtendedMixin.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object AnotherTestObject_upcast_AnotherTestObject extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, AnotherTestObject] {
    override def convert(_value: AnotherTestObject): AnotherTestObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AnotherTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object AnotherTestObject_upcast_ExtendedMixin extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, ExtendedMixin] {
    override def convert(_value: AnotherTestObject): ExtendedMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object AnotherTestObject_upcast_PrivateMixin extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, PrivateMixin] {
    override def convert(_value: AnotherTestObject): PrivateMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded)
    }
  }
  implicit object AnotherTestObject_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, PrivateMixinParent] {
    override def convert(_value: AnotherTestObject): PrivateMixinParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinParent.Struct(parent = _value.parent)
    }
  }
  implicit object AnotherTestObject_upcast_PrivateMixinPrivateParent extends izumi.idealingua.runtime.IRTCast[AnotherTestObject, PrivateMixinPrivateParent] {
    override def convert(_value: AnotherTestObject): PrivateMixinPrivateParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
    }
  }
  implicit class AnotherTestObjectExtensions(override protected val _value: AnotherTestObject) extends izumi.idealingua.runtime.IRTConversions[AnotherTestObject]
}
       