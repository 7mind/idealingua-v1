package izumi.test.domain01



trait PrivateMixinParent extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def parent: String }

trait PrivateMixinParentCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePrivateMixinParent: Encoder.AsObject[PrivateMixinParent] = Encoder.AsObject.instance {
    case v: PrivateMixin.Struct =>
      Map("izumi.test.domain01.PrivateMixin.Struct" -> v).asJsonObject
    case v: PrivateMixinParent.Struct =>
      Map("izumi.test.domain01.PrivateMixinParent.Struct" -> v).asJsonObject
  }
  implicit val decodePrivateMixinParent: Decoder[PrivateMixinParent] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.PrivateMixin.Struct" =>
        value.as[PrivateMixin.Struct]
      case "izumi.test.domain01.PrivateMixinParent.Struct" =>
        value.as[PrivateMixinParent.Struct]
      case _ =>
        val cname = "izumi.test.domain01.PrivateMixinParent"
        val alts = List("izumi.test.domain01.PrivateMixin.Struct", "izumi.test.domain01.PrivateMixinParent.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object PrivateMixinParent extends PrivateMixinParentCirce {
  def apply(parent: String) = Struct(parent)
  final case class Struct(parent: String) extends AnyVal with PrivateMixinParent
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("parent")((v: Struct) => v.parent)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("parent")((d: String) => new Struct(d))
  }
  object Struct extends PrivateMixinParent.StructCirce {
    def apply(privatemixinparent: PrivateMixinParent): PrivateMixinParent.Struct = {
      assert(privatemixinparent.asInstanceOf[_root_.scala.AnyRef] ne null)
      new PrivateMixinParent.Struct(parent = privatemixinparent.parent)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[PrivateMixinParent.Struct, PrivateMixinParent.Struct] {
      override def convert(_value: PrivateMixinParent.Struct): PrivateMixinParent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinParent.Struct(parent = _value.parent)
      }
    }
    implicit object Struct_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[PrivateMixinParent.Struct, PrivateMixinParent] {
      override def convert(_value: PrivateMixinParent.Struct): PrivateMixinParent = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinParent.Struct(parent = _value.parent)
      }
    }
    implicit class StructExtensions(override protected val _value: PrivateMixinParent.Struct) extends izumi.idealingua.runtime.IRTConversions[PrivateMixinParent.Struct]
  }
  implicit object PrivateMixinParent_downcast_extend_AnotherTestObject extends izumi.idealingua.runtime.IRTExtend[PrivateMixinParent, AnotherTestObject] {
    class Call(private val _value: PrivateMixinParent) extends AnyVal {
      def using(parent_embedded: String, embedded: Boolean, own: Byte): AnotherTestObject = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnotherTestObject(parent = _value.parent, parent_embedded = parent_embedded, embedded = embedded, own = own)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixinParent): Call = new Call(_value)
  }
  implicit object PrivateMixinParent_downcast_extend_PrivateMixinStruct extends izumi.idealingua.runtime.IRTExtend[PrivateMixinParent, PrivateMixin.Struct] {
    class Call(private val _value: PrivateMixinParent) extends AnyVal {
      def using(parent_embedded: String, embedded: Boolean): PrivateMixin.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixin.Struct(parent = _value.parent, parent_embedded = parent_embedded, embedded = embedded)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixinParent): Call = new Call(_value)
  }
  implicit object PrivateMixinParent_downcast_extend_PrivateMixinParentStruct extends izumi.idealingua.runtime.IRTExtend[PrivateMixinParent, PrivateMixinParent.Struct] {
    class Call(private val _value: PrivateMixinParent) extends AnyVal {
      def using(): PrivateMixinParent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PrivateMixinParent.Struct(parent = _value.parent)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PrivateMixinParent): Call = new Call(_value)
  }
  implicit object PrivateMixinParent_upcast_PrivateMixinParent extends izumi.idealingua.runtime.IRTCast[PrivateMixinParent, PrivateMixinParent] {
    override def convert(_value: PrivateMixinParent): PrivateMixinParent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PrivateMixinParent.Struct(parent = _value.parent)
    }
  }
  implicit class PrivateMixinParentExtensions(override protected val _value: PrivateMixinParent) extends izumi.idealingua.runtime.IRTConversions[PrivateMixinParent]
}
       