package izumi.test.domain01



trait ExtendedMixin extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def parent_embedded: String
  def parent: String
  def embedded: Boolean
  def own: Byte
}

trait ExtendedMixinCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeExtendedMixin: Encoder.AsObject[ExtendedMixin] = Encoder.AsObject.instance {
    case v: PrivateTestObject =>
      Map("izumi.test.domain01.PrivateTestObject" -> v).asJsonObject
    case v: ExtendedMixin.Struct =>
      Map("izumi.test.domain01.ExtendedMixin.Struct" -> v).asJsonObject
  }
  implicit val decodeExtendedMixin: Decoder[ExtendedMixin] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.PrivateTestObject" =>
        value.as[PrivateTestObject]
      case "izumi.test.domain01.ExtendedMixin.Struct" =>
        value.as[ExtendedMixin.Struct]
      case _ =>
        val cname = "izumi.test.domain01.ExtendedMixin"
        val alts = List("izumi.test.domain01.PrivateTestObject", "izumi.test.domain01.ExtendedMixin.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object ExtendedMixin extends ExtendedMixinCirce {
  def apply(parent_embedded: String, parent: String, embedded: Boolean, own: Byte) = Struct(parent_embedded, parent, embedded, own)
  final case class Struct(parent_embedded: String, parent: String, embedded: Boolean, own: Byte) extends ExtendedMixin
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends ExtendedMixin.StructCirce {
    def apply(privatemixinprivateparent: PrivateMixinPrivateParent, privatemixinparent: PrivateMixinParent, privatemixin: PrivateMixin, extendedmixin: ExtendedMixin): ExtendedMixin.Struct = {
      assert((extendedmixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixinparent.asInstanceOf[_root_.scala.AnyRef] ne null) && (privatemixinprivateparent.asInstanceOf[_root_.scala.AnyRef] ne null))))
      new ExtendedMixin.Struct(parent_embedded = privatemixinprivateparent.parent_embedded, parent = privatemixinparent.parent, embedded = privatemixin.embedded, own = extendedmixin.own)
    }
    implicit object Struct_cast_into_PrivateTestObject extends izumi.idealingua.runtime.IRTCast[ExtendedMixin.Struct, PrivateTestObject] {
      override def convert(_value: ExtendedMixin.Struct): PrivateTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
      }
    }
    implicit object Struct_cast_into_AnotherTestObject extends izumi.idealingua.runtime.IRTCast[ExtendedMixin.Struct, AnotherTestObject] {
      override def convert(_value: ExtendedMixin.Struct): AnotherTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnotherTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[ExtendedMixin.Struct, ExtendedMixin.Struct] {
      override def convert(_value: ExtendedMixin.Struct): ExtendedMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
      }
    }
    implicit object Struct_upcast_ExtendedMixin extends izumi.idealingua.runtime.IRTCast[ExtendedMixin.Struct, ExtendedMixin] {
      override def convert(_value: ExtendedMixin.Struct): ExtendedMixin = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
      }
    }
    implicit class StructExtensions(override protected val _value: ExtendedMixin.Struct) extends izumi.idealingua.runtime.IRTConversions[ExtendedMixin.Struct]
  }
  implicit object ExtendedMixin_cast_into_AnotherTestObject extends izumi.idealingua.runtime.IRTCast[ExtendedMixin, AnotherTestObject] {
    override def convert(_value: ExtendedMixin): AnotherTestObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AnotherTestObject(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object ExtendedMixin_downcast_extend_AnotherTestObject extends izumi.idealingua.runtime.IRTExtend[ExtendedMixin, AnotherTestObject] {
    class Call(private val _value: ExtendedMixin) extends AnyVal {
      def using(): AnotherTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnotherTestObject(embedded = _value.embedded, parent = _value.parent, parent_embedded = _value.parent_embedded, own = _value.own)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ExtendedMixin): Call = new Call(_value)
  }
  implicit object ExtendedMixin_downcast_extend_PrivateTestObject extends izumi.idealingua.runtime.IRTExtend[ExtendedMixin, PrivateTestObject] {
    class Call(private val _value: ExtendedMixin) extends AnyVal {
      def using(): PrivateTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateTestObject(embedded = _value.embedded, parent = _value.parent, parent_embedded = _value.parent_embedded, own = _value.own)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ExtendedMixin): Call = new Call(_value)
  }
  implicit object ExtendedMixin_downcast_extend_ExtendedMixinStruct extends izumi.idealingua.runtime.IRTExtend[ExtendedMixin, ExtendedMixin.Struct] {
    class Call(private val _value: ExtendedMixin) extends AnyVal {
      def using(): ExtendedMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ExtendedMixin.Struct(embedded = _value.embedded, parent = _value.parent, parent_embedded = _value.parent_embedded, own = _value.own)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ExtendedMixin): Call = new Call(_value)
  }
  implicit object ExtendedMixin_upcast_ExtendedMixin extends izumi.idealingua.runtime.IRTCast[ExtendedMixin, ExtendedMixin] {
    override def convert(_value: ExtendedMixin): ExtendedMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ExtendedMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded, own = _value.own)
    }
  }
  implicit object ExtendedMixin_upcast_PrivateMixin extends izumi.idealingua.runtime.IRTCast[ExtendedMixin, PrivateMixin] {
    override def convert(_value: ExtendedMixin): PrivateMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded)
    }
  }
  implicit object ExtendedMixin_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[ExtendedMixin, PrivateMixinParent] {
    override def convert(_value: ExtendedMixin): PrivateMixinParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinParent.Struct(parent = _value.parent)
    }
  }
  implicit object ExtendedMixin_upcast_PrivateMixinPrivateParent extends izumi.idealingua.runtime.IRTCast[ExtendedMixin, PrivateMixinPrivateParent] {
    override def convert(_value: ExtendedMixin): PrivateMixinPrivateParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
    }
  }
  implicit class ExtendedMixinExtensions(override protected val _value: ExtendedMixin) extends izumi.idealingua.runtime.IRTConversions[ExtendedMixin]
}
       