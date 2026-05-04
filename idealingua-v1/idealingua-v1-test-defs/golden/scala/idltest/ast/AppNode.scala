package idltest.ast



trait AppNode extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def fun: Option[AST]
  def args: List[AST]
}

trait AppNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAppNode: Encoder.AsObject[AppNode] = Encoder.AsObject.instance {
    case v: AppNode.Struct =>
      Map("idltest.ast.AppNode.Struct" -> v).asJsonObject
    case v: TAppNode.Struct =>
      Map("idltest.ast.TAppNode.Struct" -> v).asJsonObject
  }
  implicit val decodeAppNode: Decoder[AppNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.AppNode.Struct" =>
        value.as[AppNode.Struct]
      case "idltest.ast.TAppNode.Struct" =>
        value.as[TAppNode.Struct]
      case _ =>
        val cname = "idltest.ast.AppNode"
        val alts = List("idltest.ast.AppNode.Struct", "idltest.ast.TAppNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object AppNode extends AppNodeCirce {
  def apply(fun: Option[AST], args: List[AST]) = Struct(fun, args)
  final case class Struct(fun: Option[AST], args: List[AST]) extends AppNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends AppNode.StructCirce {
    def apply(appnode: AppNode): AppNode.Struct = {
      assert(appnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new AppNode.Struct(fun = appnode.fun, args = appnode.args)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[AppNode.Struct, AppNode.Struct] {
      override def convert(_value: AppNode.Struct): AppNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AppNode.Struct(fun = _value.fun, args = _value.args)
      }
    }
    implicit object Struct_upcast_AppNode extends izumi.idealingua.runtime.IRTCast[AppNode.Struct, AppNode] {
      override def convert(_value: AppNode.Struct): AppNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AppNode.Struct(fun = _value.fun, args = _value.args)
      }
    }
    implicit class StructExtensions(override protected val _value: AppNode.Struct) extends izumi.idealingua.runtime.IRTConversions[AppNode.Struct]
  }
  implicit object AppNode_downcast_extend_AppNodeStruct extends izumi.idealingua.runtime.IRTExtend[AppNode, AppNode.Struct] {
    class Call(private val _value: AppNode) extends AnyVal {
      def using(): AppNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AppNode.Struct(fun = _value.fun, args = _value.args)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AppNode): Call = new Call(_value)
  }
  implicit object AppNode_downcast_extend_TAppNodeStruct extends izumi.idealingua.runtime.IRTExtend[AppNode, TAppNode.Struct] {
    class Call(private val _value: AppNode) extends AnyVal {
      def using(typeinfo: TypeInfo): TAppNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null)
        TAppNode.Struct(fun = _value.fun, args = _value.args, tpe = typeinfo.tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AppNode): Call = new Call(_value)
  }
  implicit object AppNode_upcast_AppNode extends izumi.idealingua.runtime.IRTCast[AppNode, AppNode] {
    override def convert(_value: AppNode): AppNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AppNode.Struct(fun = _value.fun, args = _value.args)
    }
  }
  implicit class AppNodeExtensions(override protected val _value: AppNode) extends izumi.idealingua.runtime.IRTConversions[AppNode]
}
       