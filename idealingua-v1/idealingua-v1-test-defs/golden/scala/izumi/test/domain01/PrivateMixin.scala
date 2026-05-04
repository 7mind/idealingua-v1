package izumi.test.domain01



trait PrivateMixin extends izumi.idealingua.runtime.model.IDLGeneratedType with PrivateMixinParent {
  def parent_embedded: String
  def parent: String
  def embedded: Boolean
}

trait PrivateMixinCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePrivateMixin: Encoder.AsObject[PrivateMixin] = Encoder.AsObject.instance {
    case v: PrivateMixin.Struct =>
      Map("izumi.test.domain01.PrivateMixin.Struct" -> v).asJsonObject
  }
  implicit val decodePrivateMixin: Decoder[PrivateMixin] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.PrivateMixin.Struct" =>
        value.as[PrivateMixin.Struct]
      case _ =>
        val cname = "izumi.test.domain01.PrivateMixin"
        val alts = List("izumi.test.domain01.PrivateMixin.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object PrivateMixin extends PrivateMixinCirce {
  def apply(parent_embedded: String, parent: String, embedded: Boolean) = Struct(parent_embedded, parent, embedded)
  final case class Struct(parent_embedded: String, parent: String, embedded: Boolean) extends PrivateMixin
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends PrivateMixin.StructCirce {
    def apply(privatemixinprivateparent: PrivateMixinPrivateParent, privatemixinparent: PrivateMixinParent, privatemixin: PrivateMixin): PrivateMixin.Struct = {
      assert((privatemixin.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixinparent.asInstanceOf[_root_.scala.AnyRef] ne null) && (privatemixinprivateparent.asInstanceOf[_root_.scala.AnyRef] ne null)))
      new PrivateMixin.Struct(parent_embedded = privatemixinprivateparent.parent_embedded, parent = privatemixinparent.parent, embedded = privatemixin.embedded)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[PrivateMixin.Struct, PrivateMixin.Struct] {
      override def convert(_value: PrivateMixin.Struct): PrivateMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded)
      }
    }
    implicit object Struct_upcast_PrivateMixin extends izumi.idealingua.runtime.IRTCast[PrivateMixin.Struct, PrivateMixin] {
      override def convert(_value: PrivateMixin.Struct): PrivateMixin = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded)
      }
    }
    implicit object Struct_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[PrivateMixin.Struct, PrivateMixinParent] {
      override def convert(_value: PrivateMixin.Struct): PrivateMixinParent = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinParent.Struct(parent = _value.parent)
      }
    }
    implicit class StructExtensions(override protected val _value: PrivateMixin.Struct) extends izumi.idealingua.runtime.IRTConversions[PrivateMixin.Struct]
  }
  implicit object PrivateMixin_downcast_extend_AnotherTestObject extends izumi.idealingua.runtime.IRTExtend[PrivateMixin, AnotherTestObject] {
    class Call(private val _value: PrivateMixin) extends AnyVal {
      def using(extendedmixin: ExtendedMixin): AnotherTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(extendedmixin.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnotherTestObject(embedded = _value.embedded, parent = _value.parent, parent_embedded = _value.parent_embedded, own = extendedmixin.own)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixin): Call = new Call(_value)
  }
  implicit object PrivateMixin_downcast_extend_PrivateMixinStruct extends izumi.idealingua.runtime.IRTExtend[PrivateMixin, PrivateMixin.Struct] {
    class Call(private val _value: PrivateMixin) extends AnyVal {
      def using(): PrivateMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixin.Struct(embedded = _value.embedded, parent = _value.parent, parent_embedded = _value.parent_embedded)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixin): Call = new Call(_value)
  }
  implicit object PrivateMixin_upcast_PrivateMixin extends izumi.idealingua.runtime.IRTCast[PrivateMixin, PrivateMixin] {
    override def convert(_value: PrivateMixin): PrivateMixin = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixin.Struct(parent_embedded = _value.parent_embedded, parent = _value.parent, embedded = _value.embedded)
    }
  }
  implicit object PrivateMixin_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[PrivateMixin, PrivateMixinParent] {
    override def convert(_value: PrivateMixin): PrivateMixinParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinParent.Struct(parent = _value.parent)
    }
  }
  implicit object PrivateMixin_upcast_PrivateMixinPrivateParent extends izumi.idealingua.runtime.IRTCast[PrivateMixin, PrivateMixinPrivateParent] {
    override def convert(_value: PrivateMixin): PrivateMixinPrivateParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
    }
  }
  implicit class PrivateMixinExtensions(override protected val _value: PrivateMixin) extends izumi.idealingua.runtime.IRTConversions[PrivateMixin]
}
       