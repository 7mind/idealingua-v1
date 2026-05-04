package idltest.dtofields



trait NullableContent extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: Int }

trait NullableContentCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNullableContent: Encoder.AsObject[NullableContent] = Encoder.AsObject.instance {
    case v: NullableObj =>
      Map("idltest.dtofields.NullableObj" -> v).asJsonObject
    case v: NullableContent.Struct =>
      Map("idltest.dtofields.NullableContent.Struct" -> v).asJsonObject
  }
  implicit val decodeNullableContent: Decoder[NullableContent] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.dtofields.NullableObj" =>
        value.as[NullableObj]
      case "idltest.dtofields.NullableContent.Struct" =>
        value.as[NullableContent.Struct]
      case _ =>
        val cname = "idltest.dtofields.NullableContent"
        val alts = List("idltest.dtofields.NullableObj", "idltest.dtofields.NullableContent.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NullableContent extends NullableContentCirce {
  def apply(a: Int) = Struct(a)
  final case class Struct(a: Int) extends AnyVal with NullableContent
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Int]("a")((v: Struct) => v.a)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Int]("a")((d: Int) => new Struct(d))
  }
  object Struct extends NullableContent.StructCirce {
    def apply(nullablecontent: NullableContent): NullableContent.Struct = {
      assert(nullablecontent.asInstanceOf[_root_.scala.AnyRef] ne null)
      new NullableContent.Struct(a = nullablecontent.a)
    }
    implicit object Struct_cast_into_NullableObj extends izumi.idealingua.runtime.IRTCast[NullableContent.Struct, NullableObj] {
      override def convert(_value: NullableContent.Struct): NullableObj = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NullableObj(a = _value.a)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NullableContent.Struct, NullableContent.Struct] {
      override def convert(_value: NullableContent.Struct): NullableContent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NullableContent.Struct(a = _value.a)
      }
    }
    implicit object Struct_upcast_NullableContent extends izumi.idealingua.runtime.IRTCast[NullableContent.Struct, NullableContent] {
      override def convert(_value: NullableContent.Struct): NullableContent = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NullableContent.Struct(a = _value.a)
      }
    }
    implicit class StructExtensions(override protected val _value: NullableContent.Struct) extends izumi.idealingua.runtime.IRTConversions[NullableContent.Struct]
  }
  implicit object NullableContent_downcast_extend_NullableObj extends izumi.idealingua.runtime.IRTExtend[NullableContent, NullableObj] {
    class Call(private val _value: NullableContent) extends AnyVal {
      def using(): NullableObj = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NullableObj(a = _value.a)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NullableContent): Call = new Call(_value)
  }
  implicit object NullableContent_downcast_extend_NullableContentStruct extends izumi.idealingua.runtime.IRTExtend[NullableContent, NullableContent.Struct] {
    class Call(private val _value: NullableContent) extends AnyVal {
      def using(): NullableContent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NullableContent.Struct(a = _value.a)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NullableContent): Call = new Call(_value)
  }
  implicit object NullableContent_upcast_NullableContent extends izumi.idealingua.runtime.IRTCast[NullableContent, NullableContent] {
    override def convert(_value: NullableContent): NullableContent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NullableContent.Struct(a = _value.a)
    }
  }
  implicit class NullableContentExtensions(override protected val _value: NullableContent) extends izumi.idealingua.runtime.IRTConversions[NullableContent]
}
       