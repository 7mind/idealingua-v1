package idltest.ast



trait SymNode extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def lit: String }

trait SymNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeSymNode: Encoder.AsObject[SymNode] = Encoder.AsObject.instance {
    case v: SymNode.Struct =>
      Map("idltest.ast.SymNode.Struct" -> v).asJsonObject
    case v: TSymNode.Struct =>
      Map("idltest.ast.TSymNode.Struct" -> v).asJsonObject
  }
  implicit val decodeSymNode: Decoder[SymNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.SymNode.Struct" =>
        value.as[SymNode.Struct]
      case "idltest.ast.TSymNode.Struct" =>
        value.as[TSymNode.Struct]
      case _ =>
        val cname = "idltest.ast.SymNode"
        val alts = List("idltest.ast.SymNode.Struct", "idltest.ast.TSymNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object SymNode extends SymNodeCirce {
  def apply(lit: String) = Struct(lit)
  final case class Struct(lit: String) extends AnyVal with SymNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("lit")((v: Struct) => v.lit)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("lit")((d: String) => new Struct(d))
  }
  object Struct extends SymNode.StructCirce {
    def apply(symnode: SymNode): SymNode.Struct = {
      assert(symnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new SymNode.Struct(lit = symnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[SymNode.Struct, SymNode.Struct] {
      override def convert(_value: SymNode.Struct): SymNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SymNode.Struct(lit = _value.lit)
      }
    }
    implicit object Struct_upcast_SymNode extends izumi.idealingua.runtime.IRTCast[SymNode.Struct, SymNode] {
      override def convert(_value: SymNode.Struct): SymNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SymNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: SymNode.Struct) extends izumi.idealingua.runtime.IRTConversions[SymNode.Struct]
  }
  implicit object SymNode_downcast_extend_SymNodeStruct extends izumi.idealingua.runtime.IRTExtend[SymNode, SymNode.Struct] {
    class Call(private val _value: SymNode) extends AnyVal {
      def using(): SymNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SymNode.Struct(lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SymNode): Call = new Call(_value)
  }
  implicit object SymNode_downcast_extend_TSymNodeStruct extends izumi.idealingua.runtime.IRTExtend[SymNode, TSymNode.Struct] {
    class Call(private val _value: SymNode) extends AnyVal {
      def using(typeinfo: TypeInfo): TSymNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null)
        TSymNode.Struct(lit = _value.lit, tpe = typeinfo.tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SymNode): Call = new Call(_value)
  }
  implicit object SymNode_upcast_SymNode extends izumi.idealingua.runtime.IRTCast[SymNode, SymNode] {
    override def convert(_value: SymNode): SymNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SymNode.Struct(lit = _value.lit)
    }
  }
  implicit class SymNodeExtensions(override protected val _value: SymNode) extends izumi.idealingua.runtime.IRTConversions[SymNode]
}
       