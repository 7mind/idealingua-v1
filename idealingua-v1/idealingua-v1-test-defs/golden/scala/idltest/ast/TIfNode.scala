package idltest.ast



trait TIfNode extends izumi.idealingua.runtime.model.IDLGeneratedType with IfNode {
  def tpe: Type
  def cond: Option[AST]
  def thenNode: Option[AST]
  def elseNode: Option[AST]
}

trait TIfNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTIfNode: Encoder.AsObject[TIfNode] = Encoder.AsObject.instance {
    case v: TIfNode.Struct =>
      Map("idltest.ast.TIfNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTIfNode: Decoder[TIfNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TIfNode.Struct" =>
        value.as[TIfNode.Struct]
      case _ =>
        val cname = "idltest.ast.TIfNode"
        val alts = List("idltest.ast.TIfNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TIfNode extends TIfNodeCirce {
  def apply(tpe: Type, cond: Option[AST], thenNode: Option[AST], elseNode: Option[AST]) = Struct(tpe, cond, thenNode, elseNode)
  final case class Struct(tpe: Type, cond: Option[AST], thenNode: Option[AST], elseNode: Option[AST]) extends TIfNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TIfNode.StructCirce {
    def apply(typeinfo: TypeInfo, ifnode: IfNode): TIfNode.Struct = {
      assert((ifnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TIfNode.Struct(tpe = typeinfo.tpe, cond = ifnode.cond, thenNode = ifnode.thenNode, elseNode = ifnode.elseNode)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TIfNode.Struct, TIfNode.Struct] {
      override def convert(_value: TIfNode.Struct): TIfNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIfNode.Struct(tpe = _value.tpe, cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
      }
    }
    implicit object Struct_upcast_TIfNode extends izumi.idealingua.runtime.IRTCast[TIfNode.Struct, TIfNode] {
      override def convert(_value: TIfNode.Struct): TIfNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIfNode.Struct(tpe = _value.tpe, cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
      }
    }
    implicit object Struct_upcast_IfNode extends izumi.idealingua.runtime.IRTCast[TIfNode.Struct, IfNode] {
      override def convert(_value: TIfNode.Struct): IfNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IfNode.Struct(cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
      }
    }
    implicit class StructExtensions(override protected val _value: TIfNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TIfNode.Struct]
  }
  implicit object TIfNode_downcast_extend_TIfNodeStruct extends izumi.idealingua.runtime.IRTExtend[TIfNode, TIfNode.Struct] {
    class Call(private val _value: TIfNode) extends AnyVal {
      def using(): TIfNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIfNode.Struct(tpe = _value.tpe, thenNode = _value.thenNode, elseNode = _value.elseNode, cond = _value.cond)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TIfNode): Call = new Call(_value)
  }
  implicit object TIfNode_upcast_TIfNode extends izumi.idealingua.runtime.IRTCast[TIfNode, TIfNode] {
    override def convert(_value: TIfNode): TIfNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TIfNode.Struct(tpe = _value.tpe, cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
    }
  }
  implicit object TIfNode_upcast_IfNode extends izumi.idealingua.runtime.IRTCast[TIfNode, IfNode] {
    override def convert(_value: TIfNode): IfNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IfNode.Struct(cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
    }
  }
  implicit object TIfNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TIfNode, TypeInfo] {
    override def convert(_value: TIfNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TIfNodeExtensions(override protected val _value: TIfNode) extends izumi.idealingua.runtime.IRTConversions[TIfNode]
}
       