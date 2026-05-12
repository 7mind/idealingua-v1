package idltest.ast



trait IntNode extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def lit: Int }

trait IntNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIntNode: Encoder.AsObject[IntNode] = Encoder.AsObject.instance {
    case v: IntNode.Struct =>
      Map("idltest.ast.IntNode.Struct" -> v).asJsonObject
    case v: TIntNode.Struct =>
      Map("idltest.ast.TIntNode.Struct" -> v).asJsonObject
  }
  implicit val decodeIntNode: Decoder[IntNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.IntNode.Struct" =>
        value.as[IntNode.Struct]
      case "idltest.ast.TIntNode.Struct" =>
        value.as[TIntNode.Struct]
      case _ =>
        val cname = "idltest.ast.IntNode"
        val alts = List("idltest.ast.IntNode.Struct", "idltest.ast.TIntNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object IntNode extends IntNodeCirce {
  def apply(lit: Int) = Struct(lit)
  final case class Struct(lit: Int) extends AnyVal with IntNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Int]("lit")((v: Struct) => v.lit)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Int]("lit")((d: Int) => new Struct(d))
  }
  object Struct extends IntNode.StructCirce {
    def apply(intnode: IntNode): IntNode.Struct = {
      assert(intnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new IntNode.Struct(lit = intnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[IntNode.Struct, IntNode.Struct] {
      override def convert(_value: IntNode.Struct): IntNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntNode.Struct(lit = _value.lit)
      }
    }
    implicit object Struct_upcast_IntNode extends izumi.idealingua.runtime.IRTCast[IntNode.Struct, IntNode] {
      override def convert(_value: IntNode.Struct): IntNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: IntNode.Struct) extends izumi.idealingua.runtime.IRTConversions[IntNode.Struct]
  }
  implicit object IntNode_downcast_extend_IntNodeStruct extends izumi.idealingua.runtime.IRTExtend[IntNode, IntNode.Struct] {
    class Call(private val _value: IntNode) extends AnyVal {
      def using(): IntNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntNode.Struct(lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IntNode): Call = new Call(_value)
  }
  implicit object IntNode_downcast_extend_TIntNodeStruct extends izumi.idealingua.runtime.IRTExtend[IntNode, TIntNode.Struct] {
    class Call(private val _value: IntNode) extends AnyVal {
      def using(tpe: Type): TIntNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(tpe.asInstanceOf[_root_.scala.AnyRef] ne null)
        TIntNode.Struct(lit = _value.lit, tpe = tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IntNode): Call = new Call(_value)
  }
  implicit object IntNode_upcast_IntNode extends izumi.idealingua.runtime.IRTCast[IntNode, IntNode] {
    override def convert(_value: IntNode): IntNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IntNode.Struct(lit = _value.lit)
    }
  }
  implicit class IntNodeExtensions(override protected val _value: IntNode) extends izumi.idealingua.runtime.IRTConversions[IntNode]
}
       