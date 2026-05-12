package idltest.ast



trait LamNode extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def paramNames: List[String]
  def body: Option[AST]
}

trait LamNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeLamNode: Encoder.AsObject[LamNode] = Encoder.AsObject.instance {
    case v: LamNode.Struct =>
      Map("idltest.ast.LamNode.Struct" -> v).asJsonObject
    case v: TLamNode.Struct =>
      Map("idltest.ast.TLamNode.Struct" -> v).asJsonObject
  }
  implicit val decodeLamNode: Decoder[LamNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.LamNode.Struct" =>
        value.as[LamNode.Struct]
      case "idltest.ast.TLamNode.Struct" =>
        value.as[TLamNode.Struct]
      case _ =>
        val cname = "idltest.ast.LamNode"
        val alts = List("idltest.ast.LamNode.Struct", "idltest.ast.TLamNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object LamNode extends LamNodeCirce {
  def apply(paramNames: List[String], body: Option[AST]) = Struct(paramNames, body)
  final case class Struct(paramNames: List[String], body: Option[AST]) extends LamNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends LamNode.StructCirce {
    def apply(lamnode: LamNode): LamNode.Struct = {
      assert(lamnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new LamNode.Struct(paramNames = lamnode.paramNames, body = lamnode.body)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[LamNode.Struct, LamNode.Struct] {
      override def convert(_value: LamNode.Struct): LamNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LamNode.Struct(paramNames = _value.paramNames, body = _value.body)
      }
    }
    implicit object Struct_upcast_LamNode extends izumi.idealingua.runtime.IRTCast[LamNode.Struct, LamNode] {
      override def convert(_value: LamNode.Struct): LamNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LamNode.Struct(paramNames = _value.paramNames, body = _value.body)
      }
    }
    implicit class StructExtensions(override protected val _value: LamNode.Struct) extends izumi.idealingua.runtime.IRTConversions[LamNode.Struct]
  }
  implicit object LamNode_downcast_extend_LamNodeStruct extends izumi.idealingua.runtime.IRTExtend[LamNode, LamNode.Struct] {
    class Call(private val _value: LamNode) extends AnyVal {
      def using(): LamNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        LamNode.Struct(paramNames = _value.paramNames, body = _value.body)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: LamNode): Call = new Call(_value)
  }
  implicit object LamNode_downcast_extend_TLamNodeStruct extends izumi.idealingua.runtime.IRTExtend[LamNode, TLamNode.Struct] {
    class Call(private val _value: LamNode) extends AnyVal {
      def using(tpe: Type): TLamNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(tpe.asInstanceOf[_root_.scala.AnyRef] ne null)
        TLamNode.Struct(paramNames = _value.paramNames, body = _value.body, tpe = tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: LamNode): Call = new Call(_value)
  }
  implicit object LamNode_upcast_LamNode extends izumi.idealingua.runtime.IRTCast[LamNode, LamNode] {
    override def convert(_value: LamNode): LamNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      LamNode.Struct(paramNames = _value.paramNames, body = _value.body)
    }
  }
  implicit class LamNodeExtensions(override protected val _value: LamNode) extends izumi.idealingua.runtime.IRTConversions[LamNode]
}
       