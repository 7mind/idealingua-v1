package idltest.ast



trait TLamNode extends izumi.idealingua.runtime.model.IDLGeneratedType with LamNode {
  def tpe: Type
  def paramNames: List[String]
  def body: Option[AST]
}

trait TLamNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTLamNode: Encoder.AsObject[TLamNode] = Encoder.AsObject.instance {
    case v: TLamNode.Struct =>
      Map("idltest.ast.TLamNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTLamNode: Decoder[TLamNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TLamNode.Struct" =>
        value.as[TLamNode.Struct]
      case _ =>
        val cname = "idltest.ast.TLamNode"
        val alts = List("idltest.ast.TLamNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TLamNode extends TLamNodeCirce {
  def apply(tpe: Type, paramNames: List[String], body: Option[AST]) = Struct(tpe, paramNames, body)
  final case class Struct(tpe: Type, paramNames: List[String], body: Option[AST]) extends TLamNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TLamNode.StructCirce {
    def apply(typeinfo: TypeInfo, lamnode: LamNode): TLamNode.Struct = {
      assert((lamnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TLamNode.Struct(tpe = typeinfo.tpe, paramNames = lamnode.paramNames, body = lamnode.body)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TLamNode.Struct, TLamNode.Struct] {
      override def convert(_value: TLamNode.Struct): TLamNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TLamNode.Struct(tpe = _value.tpe, paramNames = _value.paramNames, body = _value.body)
      }
    }
    implicit object Struct_upcast_TLamNode extends izumi.idealingua.runtime.IRTCast[TLamNode.Struct, TLamNode] {
      override def convert(_value: TLamNode.Struct): TLamNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TLamNode.Struct(tpe = _value.tpe, paramNames = _value.paramNames, body = _value.body)
      }
    }
    implicit object Struct_upcast_LamNode extends izumi.idealingua.runtime.IRTCast[TLamNode.Struct, LamNode] {
      override def convert(_value: TLamNode.Struct): LamNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LamNode.Struct(paramNames = _value.paramNames, body = _value.body)
      }
    }
    implicit class StructExtensions(override protected val _value: TLamNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TLamNode.Struct]
  }
  implicit object TLamNode_downcast_extend_TLamNodeStruct extends izumi.idealingua.runtime.IRTExtend[TLamNode, TLamNode.Struct] {
    class Call(private val _value: TLamNode) extends AnyVal {
      def using(): TLamNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TLamNode.Struct(body = _value.body, tpe = _value.tpe, paramNames = _value.paramNames)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TLamNode): Call = new Call(_value)
  }
  implicit object TLamNode_upcast_TLamNode extends izumi.idealingua.runtime.IRTCast[TLamNode, TLamNode] {
    override def convert(_value: TLamNode): TLamNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TLamNode.Struct(tpe = _value.tpe, paramNames = _value.paramNames, body = _value.body)
    }
  }
  implicit object TLamNode_upcast_LamNode extends izumi.idealingua.runtime.IRTCast[TLamNode, LamNode] {
    override def convert(_value: TLamNode): LamNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      LamNode.Struct(paramNames = _value.paramNames, body = _value.body)
    }
  }
  implicit object TLamNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TLamNode, TypeInfo] {
    override def convert(_value: TLamNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TLamNodeExtensions(override protected val _value: TLamNode) extends izumi.idealingua.runtime.IRTConversions[TLamNode]
}
       