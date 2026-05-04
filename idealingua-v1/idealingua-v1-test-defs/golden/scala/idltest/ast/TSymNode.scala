package idltest.ast



trait TSymNode extends izumi.idealingua.runtime.model.IDLGeneratedType with SymNode {
  def tpe: Type
  def lit: String
}

trait TSymNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTSymNode: Encoder.AsObject[TSymNode] = Encoder.AsObject.instance {
    case v: TSymNode.Struct =>
      Map("idltest.ast.TSymNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTSymNode: Decoder[TSymNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TSymNode.Struct" =>
        value.as[TSymNode.Struct]
      case _ =>
        val cname = "idltest.ast.TSymNode"
        val alts = List("idltest.ast.TSymNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TSymNode extends TSymNodeCirce {
  def apply(tpe: Type, lit: String) = Struct(tpe, lit)
  final case class Struct(tpe: Type, lit: String) extends TSymNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TSymNode.StructCirce {
    def apply(typeinfo: TypeInfo, symnode: SymNode): TSymNode.Struct = {
      assert((symnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TSymNode.Struct(tpe = typeinfo.tpe, lit = symnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TSymNode.Struct, TSymNode.Struct] {
      override def convert(_value: TSymNode.Struct): TSymNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TSymNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_TSymNode extends izumi.idealingua.runtime.IRTCast[TSymNode.Struct, TSymNode] {
      override def convert(_value: TSymNode.Struct): TSymNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TSymNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_SymNode extends izumi.idealingua.runtime.IRTCast[TSymNode.Struct, SymNode] {
      override def convert(_value: TSymNode.Struct): SymNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SymNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: TSymNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TSymNode.Struct]
  }
  implicit object TSymNode_downcast_extend_TSymNodeStruct extends izumi.idealingua.runtime.IRTExtend[TSymNode, TSymNode.Struct] {
    class Call(private val _value: TSymNode) extends AnyVal {
      def using(): TSymNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TSymNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TSymNode): Call = new Call(_value)
  }
  implicit object TSymNode_upcast_TSymNode extends izumi.idealingua.runtime.IRTCast[TSymNode, TSymNode] {
    override def convert(_value: TSymNode): TSymNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TSymNode.Struct(tpe = _value.tpe, lit = _value.lit)
    }
  }
  implicit object TSymNode_upcast_SymNode extends izumi.idealingua.runtime.IRTCast[TSymNode, SymNode] {
    override def convert(_value: TSymNode): SymNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SymNode.Struct(lit = _value.lit)
    }
  }
  implicit object TSymNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TSymNode, TypeInfo] {
    override def convert(_value: TSymNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TSymNodeExtensions(override protected val _value: TSymNode) extends izumi.idealingua.runtime.IRTConversions[TSymNode]
}
       