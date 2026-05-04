package idltest.ast



trait TIntNode extends izumi.idealingua.runtime.model.IDLGeneratedType with IntNode {
  def tpe: Type
  def lit: Int
}

trait TIntNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTIntNode: Encoder.AsObject[TIntNode] = Encoder.AsObject.instance {
    case v: TIntNode.Struct =>
      Map("idltest.ast.TIntNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTIntNode: Decoder[TIntNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TIntNode.Struct" =>
        value.as[TIntNode.Struct]
      case _ =>
        val cname = "idltest.ast.TIntNode"
        val alts = List("idltest.ast.TIntNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TIntNode extends TIntNodeCirce {
  def apply(tpe: Type, lit: Int) = Struct(tpe, lit)
  final case class Struct(tpe: Type, lit: Int) extends TIntNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TIntNode.StructCirce {
    def apply(typeinfo: TypeInfo, intnode: IntNode): TIntNode.Struct = {
      assert((intnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TIntNode.Struct(tpe = typeinfo.tpe, lit = intnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TIntNode.Struct, TIntNode.Struct] {
      override def convert(_value: TIntNode.Struct): TIntNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIntNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_TIntNode extends izumi.idealingua.runtime.IRTCast[TIntNode.Struct, TIntNode] {
      override def convert(_value: TIntNode.Struct): TIntNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIntNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_IntNode extends izumi.idealingua.runtime.IRTCast[TIntNode.Struct, IntNode] {
      override def convert(_value: TIntNode.Struct): IntNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: TIntNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TIntNode.Struct]
  }
  implicit object TIntNode_downcast_extend_TIntNodeStruct extends izumi.idealingua.runtime.IRTExtend[TIntNode, TIntNode.Struct] {
    class Call(private val _value: TIntNode) extends AnyVal {
      def using(): TIntNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIntNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TIntNode): Call = new Call(_value)
  }
  implicit object TIntNode_upcast_TIntNode extends izumi.idealingua.runtime.IRTCast[TIntNode, TIntNode] {
    override def convert(_value: TIntNode): TIntNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TIntNode.Struct(tpe = _value.tpe, lit = _value.lit)
    }
  }
  implicit object TIntNode_upcast_IntNode extends izumi.idealingua.runtime.IRTCast[TIntNode, IntNode] {
    override def convert(_value: TIntNode): IntNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IntNode.Struct(lit = _value.lit)
    }
  }
  implicit object TIntNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TIntNode, TypeInfo] {
    override def convert(_value: TIntNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TIntNodeExtensions(override protected val _value: TIntNode) extends izumi.idealingua.runtime.IRTConversions[TIntNode]
}
       