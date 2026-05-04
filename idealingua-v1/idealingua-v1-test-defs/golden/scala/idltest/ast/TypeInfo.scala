package idltest.ast



trait TypeInfo extends izumi.idealingua.runtime.model.IDLGeneratedType { def tpe: Type }

trait TypeInfoCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTypeInfo: Encoder.AsObject[TypeInfo] = Encoder.AsObject.instance {
    case v: TypeInfo.Struct =>
      Map("idltest.ast.TypeInfo.Struct" -> v).asJsonObject
  }
  implicit val decodeTypeInfo: Decoder[TypeInfo] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TypeInfo.Struct" =>
        value.as[TypeInfo.Struct]
      case _ =>
        val cname = "idltest.ast.TypeInfo"
        val alts = List("idltest.ast.TypeInfo.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TypeInfo extends TypeInfoCirce {
  def apply(tpe: Type) = Struct(tpe)
  final case class Struct(tpe: Type) extends TypeInfo
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TypeInfo.StructCirce {
    def apply(typeinfo: TypeInfo): TypeInfo.Struct = {
      assert(typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TypeInfo.Struct(tpe = typeinfo.tpe)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TypeInfo.Struct, TypeInfo.Struct] {
      override def convert(_value: TypeInfo.Struct): TypeInfo.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TypeInfo.Struct(tpe = _value.tpe)
      }
    }
    implicit object Struct_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TypeInfo.Struct, TypeInfo] {
      override def convert(_value: TypeInfo.Struct): TypeInfo = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TypeInfo.Struct(tpe = _value.tpe)
      }
    }
    implicit class StructExtensions(override protected val _value: TypeInfo.Struct) extends izumi.idealingua.runtime.IRTConversions[TypeInfo.Struct]
  }
  implicit object TypeInfo_downcast_extend_TypeInfoStruct extends izumi.idealingua.runtime.IRTExtend[TypeInfo, TypeInfo.Struct] {
    class Call(private val _value: TypeInfo) extends AnyVal {
      def using(): TypeInfo.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TypeInfo.Struct(tpe = _value.tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TypeInfo): Call = new Call(_value)
  }
  implicit object TypeInfo_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TypeInfo, TypeInfo] {
    override def convert(_value: TypeInfo): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TypeInfoExtensions(override protected val _value: TypeInfo) extends izumi.idealingua.runtime.IRTConversions[TypeInfo]
}
       