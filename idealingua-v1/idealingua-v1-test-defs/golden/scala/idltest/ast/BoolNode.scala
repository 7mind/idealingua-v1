package idltest.ast



trait BoolNode extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def lit: Boolean }

trait BoolNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeBoolNode: Encoder.AsObject[BoolNode] = Encoder.AsObject.instance {
    case v: TBoolNode.Struct =>
      Map("idltest.ast.TBoolNode.Struct" -> v).asJsonObject
    case v: BoolNode.Struct =>
      Map("idltest.ast.BoolNode.Struct" -> v).asJsonObject
  }
  implicit val decodeBoolNode: Decoder[BoolNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.TBoolNode.Struct" =>
        value.as[TBoolNode.Struct]
      case "idltest.ast.BoolNode.Struct" =>
        value.as[BoolNode.Struct]
      case _ =>
        val cname = "idltest.ast.BoolNode"
        val alts = List("idltest.ast.TBoolNode.Struct", "idltest.ast.BoolNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object BoolNode extends BoolNodeCirce {
  def apply(lit: Boolean) = Struct(lit)
  final case class Struct(lit: Boolean) extends AnyVal with BoolNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Boolean]("lit")((v: Struct) => v.lit)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Boolean]("lit")((d: Boolean) => new Struct(d))
  }
  object Struct extends BoolNode.StructCirce {
    def apply(boolnode: BoolNode): BoolNode.Struct = {
      assert(boolnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new BoolNode.Struct(lit = boolnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[BoolNode.Struct, BoolNode.Struct] {
      override def convert(_value: BoolNode.Struct): BoolNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        BoolNode.Struct(lit = _value.lit)
      }
    }
    implicit object Struct_upcast_BoolNode extends izumi.idealingua.runtime.IRTCast[BoolNode.Struct, BoolNode] {
      override def convert(_value: BoolNode.Struct): BoolNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        BoolNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: BoolNode.Struct) extends izumi.idealingua.runtime.IRTConversions[BoolNode.Struct]
  }
  implicit object BoolNode_downcast_extend_TBoolNodeStruct extends izumi.idealingua.runtime.IRTExtend[BoolNode, TBoolNode.Struct] {
    class Call(private val _value: BoolNode) extends AnyVal {
      def using(typeinfo: TypeInfo): TBoolNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(typeinfo.asInstanceOf[_root_.scala.AnyRef] ne null)
        TBoolNode.Struct(lit = _value.lit, tpe = typeinfo.tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: BoolNode): Call = new Call(_value)
  }
  implicit object BoolNode_downcast_extend_BoolNodeStruct extends izumi.idealingua.runtime.IRTExtend[BoolNode, BoolNode.Struct] {
    class Call(private val _value: BoolNode) extends AnyVal {
      def using(): BoolNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        BoolNode.Struct(lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: BoolNode): Call = new Call(_value)
  }
  implicit object BoolNode_upcast_BoolNode extends izumi.idealingua.runtime.IRTCast[BoolNode, BoolNode] {
    override def convert(_value: BoolNode): BoolNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      BoolNode.Struct(lit = _value.lit)
    }
  }
  implicit class BoolNodeExtensions(override protected val _value: BoolNode) extends izumi.idealingua.runtime.IRTConversions[BoolNode]
}
       