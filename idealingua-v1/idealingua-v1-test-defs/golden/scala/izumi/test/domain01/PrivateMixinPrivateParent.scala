package izumi.test.domain01



trait PrivateMixinPrivateParent extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def parent_embedded: String }

trait PrivateMixinPrivateParentCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePrivateMixinPrivateParent: Encoder.AsObject[PrivateMixinPrivateParent] = Encoder.AsObject.instance {
    case v: PrivateMixinPrivateParent.Struct =>
      Map("izumi.test.domain01.PrivateMixinPrivateParent.Struct" -> v).asJsonObject
  }
  implicit val decodePrivateMixinPrivateParent: Decoder[PrivateMixinPrivateParent] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.PrivateMixinPrivateParent.Struct" =>
        value.as[PrivateMixinPrivateParent.Struct]
      case _ =>
        val cname = "izumi.test.domain01.PrivateMixinPrivateParent"
        val alts = List("izumi.test.domain01.PrivateMixinPrivateParent.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object PrivateMixinPrivateParent extends PrivateMixinPrivateParentCirce {
  def apply(parent_embedded: String) = Struct(parent_embedded)
  final case class Struct(parent_embedded: String) extends AnyVal with PrivateMixinPrivateParent
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("parent_embedded")((v: Struct) => v.parent_embedded)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("parent_embedded")((d: String) => new Struct(d))
  }
  object Struct extends PrivateMixinPrivateParent.StructCirce {
    def apply(privatemixinprivateparent: PrivateMixinPrivateParent): PrivateMixinPrivateParent.Struct = {
      assert(privatemixinprivateparent.asInstanceOf[_root_.scala.AnyRef] ne null)
      new PrivateMixinPrivateParent.Struct(parent_embedded = privatemixinprivateparent.parent_embedded)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[PrivateMixinPrivateParent.Struct, PrivateMixinPrivateParent.Struct] {
      override def convert(_value: PrivateMixinPrivateParent.Struct): PrivateMixinPrivateParent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
      }
    }
    implicit object Struct_upcast_PrivateMixinPrivateParent extends izumi.idealingua.runtime.IRTCast[PrivateMixinPrivateParent.Struct, PrivateMixinPrivateParent] {
      override def convert(_value: PrivateMixinPrivateParent.Struct): PrivateMixinPrivateParent = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
      }
    }
    implicit class StructExtensions(override protected val _value: PrivateMixinPrivateParent.Struct) extends izumi.idealingua.runtime.IRTConversions[PrivateMixinPrivateParent.Struct]
  }
  implicit object PrivateMixinPrivateParent_downcast_extend_AnotherTestObject extends izumi.idealingua.runtime.IRTExtend[PrivateMixinPrivateParent, AnotherTestObject] {
    class Call(private val _value: PrivateMixinPrivateParent) extends AnyVal {
      def using(extendedmixin: ExtendedMixin, privatemixin: PrivateMixin, privatemixinparent: PrivateMixinParent): AnotherTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert((privatemixinparent.asInstanceOf[_root_.scala.AnyRef] ne null) && ((privatemixin.asInstanceOf[_root_.scala.AnyRef] ne null) && (extendedmixin.asInstanceOf[_root_.scala.AnyRef] ne null)))
        AnotherTestObject(parent_embedded = _value.parent_embedded, own = extendedmixin.own, embedded = privatemixin.embedded, parent = privatemixinparent.parent)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixinPrivateParent): Call = new Call(_value)
  }
  implicit object PrivateMixinPrivateParent_downcast_extend_PrivateMixinPrivateParentStruct extends izumi.idealingua.runtime.IRTExtend[PrivateMixinPrivateParent, PrivateMixinPrivateParent.Struct] {
    class Call(private val _value: PrivateMixinPrivateParent) extends AnyVal {
      def using(): PrivateMixinPrivateParent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixinPrivateParent): Call = new Call(_value)
  }
  implicit object PrivateMixinPrivateParent_upcast_PrivateMixinPrivateParent extends izumi.idealingua.runtime.IRTCast[PrivateMixinPrivateParent, PrivateMixinPrivateParent] {
    override def convert(_value: PrivateMixinPrivateParent): PrivateMixinPrivateParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinPrivateParent.Struct(parent_embedded = _value.parent_embedded)
    }
  }
  implicit class PrivateMixinPrivateParentExtensions(override protected val _value: PrivateMixinPrivateParent) extends izumi.idealingua.runtime.IRTConversions[PrivateMixinPrivateParent]
}
       