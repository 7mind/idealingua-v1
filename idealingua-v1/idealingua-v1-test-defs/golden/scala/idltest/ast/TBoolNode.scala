package idltest.ast



trait TBoolNode extends izumi.idealingua.runtime.model.IDLGeneratedType with BoolNode {
  def tpe: Type
  def lit: Boolean
}

trait TBoolNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTBoolNode: Encoder.AsObject[TBoolNode] = Encoder.AsObject.instance {
    case v: TBoolNode.Struct =>
      Map("idltest.ast.TBoolNode.Struct" -> v).asJsonObject
  }
  implicit val decodeTBoolNode: Decoder[TBoolNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TBoolNode.Struct" =>
        value.as[TBoolNode.Struct]
      case _ =>
        val cname = "idltest.ast.TBoolNode"
        val alts = List("idltest.ast.TBoolNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object TBoolNode extends TBoolNodeCirce {
  def apply(tpe: Type, lit: Boolean) = Struct(tpe, lit)
  final case class Struct(tpe: Type, lit: Boolean) extends TBoolNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends TBoolNode.StructCirce {
    def apply(typeinfo: TypeInfo, boolnode: BoolNode): TBoolNode.Struct = {
      assert((boolnode.asInstanceOf[_root_.scala.AnyRef] ne null) && (typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null))
      new TBoolNode.Struct(tpe = typeinfo.tpe, lit = boolnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[TBoolNode.Struct, TBoolNode.Struct] {
      override def convert(_value: TBoolNode.Struct): TBoolNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TBoolNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_TBoolNode extends izumi.idealingua.runtime.IRTCast[TBoolNode.Struct, TBoolNode] {
      override def convert(_value: TBoolNode.Struct): TBoolNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TBoolNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    implicit object Struct_upcast_BoolNode extends izumi.idealingua.runtime.IRTCast[TBoolNode.Struct, BoolNode] {
      override def convert(_value: TBoolNode.Struct): BoolNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        BoolNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: TBoolNode.Struct) extends izumi.idealingua.runtime.IRTConversions[TBoolNode.Struct]
  }
  implicit object TBoolNode_downcast_extend_TBoolNodeStruct extends izumi.idealingua.runtime.IRTExtend[TBoolNode, TBoolNode.Struct] {
    class Call(private val _value: TBoolNode) extends AnyVal {
      def using(): TBoolNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TBoolNode.Struct(tpe = _value.tpe, lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: TBoolNode): Call = new Call(_value)
  }
  implicit object TBoolNode_upcast_TBoolNode extends izumi.idealingua.runtime.IRTCast[TBoolNode, TBoolNode] {
    override def convert(_value: TBoolNode): TBoolNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TBoolNode.Struct(tpe = _value.tpe, lit = _value.lit)
    }
  }
  implicit object TBoolNode_upcast_BoolNode extends izumi.idealingua.runtime.IRTCast[TBoolNode, BoolNode] {
    override def convert(_value: TBoolNode): BoolNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      BoolNode.Struct(lit = _value.lit)
    }
  }
  implicit object TBoolNode_upcast_TypeInfo extends izumi.idealingua.runtime.IRTCast[TBoolNode, TypeInfo] {
    override def convert(_value: TBoolNode): TypeInfo = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TypeInfo.Struct(tpe = _value.tpe)
    }
  }
  implicit class TBoolNodeExtensions(override protected val _value: TBoolNode) extends izumi.idealingua.runtime.IRTConversions[TBoolNode]
}
       