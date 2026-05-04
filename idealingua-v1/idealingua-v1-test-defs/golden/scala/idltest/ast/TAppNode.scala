package idltest.ast



trait TAppNode extends izumi.idealingua.runtime.model.IDLGeneratedType with AppNode {
  def tpe: Type
  def fun: Option[AST]
  def args: List[AST]
}

trait TAppNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTAppNode: Encoder.AsObject[TAppNode] = Encoder.AsObject.instance {
    case v: TAppNode.Struct =>
      Map("idltest.ast.TAppNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTAppNode: Decoder[TAppNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TAppNode.Struct" =>
        value.as[TAppNode.Struct]
      case _ =>
        val cname = "idltest.ast.TAppNode"
        val alts = List("idltest.ast.TAppNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TAppNode extends TAppNodeCirce {
  def apply(tpe: Type, fun: Option[AST], args: List[AST]) = Struct(tpe, fun, args)
  final case class Struct(tpe: Type, fun: Option[AST], args: List[AST]) extends TAppNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TAppNode.StructCirce {
    def apply(typeinfo: TypeInfo, appnode: AppNode): TAppNode.Struct = {
      assert((appnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TAppNode.Struct(tpe = typeinfo.tpe, fun = appnode.fun, args = appnode.args)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TAppNode.Struct, TAppNode.Struct] {
      override def convert(_value: TAppNode.Struct): TAppNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TAppNode.Struct(tpe = _value.tpe, fun = _value.fun, args = _value.args)
      }
    }
    implicit object Struct_upcast_TAppNode extends izumi.idealingua.runtime.IRTCast[TAppNode.Struct, TAppNode] {
      override def convert(_value: TAppNode.Struct): TAppNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TAppNode.Struct(tpe = _value.tpe, fun = _value.fun, args = _value.args)
      }
    }
    implicit object Struct_upcast_AppNode extends izumi.idealingua.runtime.IRTCast[TAppNode.Struct, AppNode] {
      override def convert(_value: TAppNode.Struct): AppNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AppNode.Struct(fun = _value.fun, args = _value.args)
      }
    }
    implicit class StructExtensions(override protected val _value: TAppNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TAppNode.Struct]
  }
  implicit object TAppNode_downcast_extend_TAppNodeStruct extends izumi.idealingua.runtime.IRTExtend[TAppNode, TAppNode.Struct] {
    class Call(private val _value: TAppNode) extends AnyVal {
      def using(): TAppNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TAppNode.Struct(tpe = _value.tpe, fun = _value.fun, args = _value.args)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TAppNode): Call = new Call(_value)
  }
  implicit object TAppNode_upcast_TAppNode extends izumi.idealingua.runtime.IRTCast[TAppNode, TAppNode] {
    override def convert(_value: TAppNode): TAppNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TAppNode.Struct(tpe = _value.tpe, fun = _value.fun, args = _value.args)
    }
  }
  implicit object TAppNode_upcast_AppNode extends izumi.idealingua.runtime.IRTCast[TAppNode, AppNode] {
    override def convert(_value: TAppNode): AppNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AppNode.Struct(fun = _value.fun, args = _value.args)
    }
  }
  implicit object TAppNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TAppNode, TypeInfo] {
    override def convert(_value: TAppNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TAppNodeExtensions(override protected val _value: TAppNode) extends izumi.idealingua.runtime.IRTConversions[TAppNode]
}
       