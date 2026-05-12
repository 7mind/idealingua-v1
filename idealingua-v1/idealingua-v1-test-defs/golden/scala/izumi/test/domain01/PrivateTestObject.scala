package izumi.test.domain01



final case class PrivateTestObject(parent_embedded: String, parent: String, embedded: Boolean, own: Byte) extends ExtendedMixin with PrivateTestObject.Defn

trait PrivateTestObjectCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePrivateTestObject: Encoder.AsObject[PrivateTestObject] = deriveEncoder[PrivateTestObject]
  implicit val decodePrivateTestObject: Decoder[PrivateTestObject] = deriveDecoder[PrivateTestObject]
}

object PrivateTestObject extends PrivateTestObjectCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def parent_embedded: String
    def parent: String
    def embedded: Boolean
    def own: Byte
  }
  def apply(privatemixinprivateparent: PrivateMixinPrivateParent, privatemixinparent: PrivateMixinParent, privatemixin: PrivateMixin, extendedmixin: ExtendedMixin): PrivateTestObject = {
    assert((extendedmixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixinparent.asInstanceOf[_root_.scala.AnyRef] ne null) && (privatemixinprivateparent.asInstanceOf[_root_.scala.AnyRef] ne null))))
    new PrivateTestObject(parent_embedded = privatemixinprivateparent.parent_embedded, parent = privatemixinparent.parent, embedded = privatemixin.embedded, own = extendedmixin.own)
  }
  def apply(defn: PrivateTestObject.Defn): PrivateTestObject = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new PrivateTestObject(parent_embedded = defn.parent_embedded, parent = defn.parent, embedded = defn.embedded, own = defn.own)
  }
  implicit object PrivateTestObject_cast_into_AnotherTestObject extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, AnotherTestObject] {
    override def convert(_value: PrivateTestObject): AnotherTestObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AnotherTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object PrivateTestObject_cast_into_ExtendedMixinStruct extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, ExtendedMixin.Struct] {
    override def convert(_value: PrivateTestObject): ExtendedMixin.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object PrivateTestObject_upcast_PrivateTestObject extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, PrivateTestObject] {
    override def convert(_value: PrivateTestObject): PrivateTestObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object PrivateTestObject_upcast_ExtendedMixin extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, ExtendedMixin] {
    override def convert(_value: PrivateTestObject): ExtendedMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object PrivateTestObject_upcast_PrivateMixin extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, PrivateMixin] {
    override def convert(_value: PrivateTestObject): PrivateMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded)
    }
  }
  implicit object PrivateTestObject_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, PrivateMixinParent] {
    override def convert(_value: PrivateTestObject): PrivateMixinParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinParent.Struct(parent = _value.parent)
    }
  }
  implicit object PrivateTestObject_upcast_PrivateMixinPrivateParent extends izumi.idealingua.runtime.IRTCast[PrivateTestObject, PrivateMixinPrivateParent] {
    override def convert(_value: PrivateTestObject): PrivateMixinPrivateParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
    }
  }
  implicit class PrivateTestObjectExtensions(override protected val _value: PrivateTestObject) extends izumi.idealingua.runtime.IRTConversions[PrivateTestObject]
}
       