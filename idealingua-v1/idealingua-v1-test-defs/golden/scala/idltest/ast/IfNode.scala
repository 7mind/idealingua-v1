package idltest.ast



trait IfNode extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def cond: Option[AST]
  def thenNode: Option[AST]
  def elseNode: Option[AST]
}

trait IfNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIfNode: Encoder.AsObject[IfNode] = Encoder.AsObject.instance {
    case v: TIfNode.Struct =>
      Map("idltest.ast.TIfNode.Struct" -> v).asJsonObject
    case v: IfNode.Struct =>
      Map("idltest.ast.IfNode.Struct" -> v).asJsonObject
  }
  implicit val decodeIfNode: Decoder[IfNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TIfNode.Struct" =>
        value.as[TIfNode.Struct]
      case "idltest.ast.IfNode.Struct" =>
        value.as[IfNode.Struct]
      case _ =>
        val cname = "idltest.ast.IfNode"
        val alts = List("idltest.ast.TIfNode.Struct", "idltest.ast.IfNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object IfNode extends IfNodeCirce {
  def apply(cond: Option[AST], thenNode: Option[AST], elseNode: Option[AST]) = Struct(cond, thenNode, elseNode)
  final case class Struct(cond: Option[AST], thenNode: Option[AST], elseNode: Option[AST]) extends IfNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends IfNode.StructCirce {
    def apply(ifnode: IfNode): IfNode.Struct = {
      assert(ifnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new IfNode.Struct(cond = ifnode.cond, thenNode = ifnode.thenNode, elseNode = ifnode.elseNode)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[IfNode.Struct, IfNode.Struct] {
      override def convert(_value: IfNode.Struct): IfNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IfNode.Struct(cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
      }
    }
    implicit object Struct_upcast_IfNode extends izumi.idealingua.runtime.IRTCast[IfNode.Struct, IfNode] {
      override def convert(_value: IfNode.Struct): IfNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IfNode.Struct(cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
      }
    }
    implicit class StructExtensions(override protected val _value: IfNode.Struct) extends izumi.idealingua.runtime.IRTConversions[IfNode.Struct]
  }
  implicit object IfNode_downcast_extend_TIfNodeStruct extends izumi.idealingua.runtime.IRTExtend[IfNode, TIfNode.Struct] {
    class Call(private val _value: IfNode) extends AnyVal {
      def using(typeinfo: TypeInfo): TIfNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIfNode.Struct(thenNode = _value.thenNode, elseNode = _value.elseNode, cond = _value.cond, tpe = typeinfo.tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IfNode): Call = new Call(_value)
  }
  implicit object IfNode_downcast_extend_IfNodeStruct extends izumi.idealingua.runtime.IRTExtend[IfNode, IfNode.Struct] {
    class Call(private val _value: IfNode) extends AnyVal {
      def using(): IfNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IfNode.Struct(thenNode = _value.thenNode, elseNode = _value.elseNode, cond = _value.cond)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IfNode): Call = new Call(_value)
  }
  implicit object IfNode_upcast_IfNode extends izumi.idealingua.runtime.IRTCast[IfNode, IfNode] {
    override def convert(_value: IfNode): IfNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IfNode.Struct(cond = _value.cond, thenNode = _value.thenNode, elseNode = _value.elseNode)
    }
  }
  implicit class IfNodeExtensions(override protected val _value: IfNode) extends izumi.idealingua.runtime.IRTConversions[IfNode]
}
       