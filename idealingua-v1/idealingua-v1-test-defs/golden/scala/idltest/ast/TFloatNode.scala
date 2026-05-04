package idltest.ast



trait TFloatNode extends izumi.idealingua.runtime.model.IDLGeneratedType with FloatNode {
  def tpe: Type
  def lit: Float
}

trait TFloatNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTFloatNode: Encoder.AsObject[TFloatNode] = Encoder.AsObject.instance {
    case v: TFloatNode.Struct =>
      Map("idltest.ast.TFloatNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTFloatNode: Decoder[TFloatNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TFloatNode.Struct" =>
        value.as[TFloatNode.Struct]
      case _ =>
        val cname = "idltest.ast.TFloatNode"
        val alts = List("idltest.ast.TFloatNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TFloatNode extends TFloatNodeCirce {
  def apply(tpe: Type, lit: Float) = Struct(tpe, lit)
  final case class Struct(tpe: Type, lit: Float) extends TFloatNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TFloatNode.StructCirce {
    def apply(typeinfo: TypeInfo, floatnode: FloatNode): TFloatNode.Struct = {
      assert((floatnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TFloatNode.Struct(tpe = typeinfo.tpe, lit = floatnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TFloatNode.Struct, TFloatNode.Struct] {
      override def convert(_value: TFloatNode.Struct): TFloatNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TFloatNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_TFloatNode extends izumi.idealingua.runtime.IRTCast[TFloatNode.Struct, TFloatNode] {
      override def convert(_value: TFloatNode.Struct): TFloatNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TFloatNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_FloatNode extends izumi.idealingua.runtime.IRTCast[TFloatNode.Struct, FloatNode] {
      override def convert(_value: TFloatNode.Struct): FloatNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        FloatNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: TFloatNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TFloatNode.Struct]
  }
  implicit object TFloatNode_downcast_extend_TFloatNodeStruct extends izumi.idealingua.runtime.IRTExtend[TFloatNode, TFloatNode.Struct] {
    class Call(private val _value: TFloatNode) extends AnyVal {
      def using(): TFloatNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TFloatNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TFloatNode): Call = new Call(_value)
  }
  implicit object TFloatNode_upcast_TFloatNode extends izumi.idealingua.runtime.IRTCast[TFloatNode, TFloatNode] {
    override def convert(_value: TFloatNode): TFloatNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TFloatNode.Struct(tpe = _value.tpe, lit = _value.lit)
    }
  }
  implicit object TFloatNode_upcast_FloatNode extends izumi.idealingua.runtime.IRTCast[TFloatNode, FloatNode] {
    override def convert(_value: TFloatNode): FloatNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      FloatNode.Struct(lit = _value.lit)
    }
  }
  implicit object TFloatNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TFloatNode, TypeInfo] {
    override def convert(_value: TFloatNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TFloatNodeExtensions(override protected val _value: TFloatNode) extends izumi.idealingua.runtime.IRTConversions[TFloatNode]
}
       