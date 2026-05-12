package idltest.ast



trait FloatNode extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def lit: Float }

trait FloatNodeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeFloatNode: Encoder.AsObject[FloatNode] = Encoder.AsObject.instance {
    case v: FloatNode.Struct =>
      Map("idltest.ast.FloatNode.Struct" -> v).asJsonObject
    case v: TFloatNode.Struct =>
      Map("idltest.ast.TFloatNode.Struct" -> v).asJsonObject
  }
  implicit val decodeFloatNode: Decoder[FloatNode] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.ast.FloatNode.Struct" =>
        value.as[FloatNode.Struct]
      case "idltest.ast.TFloatNode.Struct" =>
        value.as[TFloatNode.Struct]
      case _ =>
        val cname = "idltest.ast.FloatNode"
        val alts = List("idltest.ast.FloatNode.Struct", "idltest.ast.TFloatNode.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object FloatNode extends FloatNodeCirce {
  def apply(lit: Float) = Struct(lit)
  final case class Struct(lit: Float) extends AnyVal with FloatNode
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Float]("lit")((v: Struct) => v.lit)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Float]("lit")((d: Float) => new Struct(d))
  }
  object Struct extends FloatNode.StructCirce {
    def apply(floatnode: FloatNode): FloatNode.Struct = {
      assert(floatnode.asInstanceOf[_root_.scala.AnyRef] ne null)
      new FloatNode.Struct(lit = floatnode.lit)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[FloatNode.Struct, FloatNode.Struct] {
      override def convert(_value: FloatNode.Struct): FloatNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        FloatNode.Struct(lit = _value.lit)
      }
    }
    implicit object Struct_upcast_FloatNode extends izumi.idealingua.runtime.IRTCast[FloatNode.Struct, FloatNode] {
      override def convert(_value: FloatNode.Struct): FloatNode = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        FloatNode.Struct(lit = _value.lit)
      }
    }
    implicit class StructExtensions(override protected val _value: FloatNode.Struct) extends izumi.idealingua.runtime.IRTConversions[FloatNode.Struct]
  }
  implicit object FloatNode_downcast_extend_FloatNodeStruct extends izumi.idealingua.runtime.IRTExtend[FloatNode, FloatNode.Struct] {
    class Call(private val _value: FloatNode) extends AnyVal {
      def using(): FloatNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        FloatNode.Struct(lit = _value.lit)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: FloatNode): Call = new Call(_value)
  }
  implicit object FloatNode_downcast_extend_TFloatNodeStruct extends izumi.idealingua.runtime.IRTExtend[FloatNode, TFloatNode.Struct] {
    class Call(private val _value: FloatNode) extends AnyVal {
      def using(tpe: Type): TFloatNode.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(tpe.asInstanceOf[_root_.scala.AnyRef] ne null)
        TFloatNode.Struct(lit = _value.lit, tpe = tpe)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: FloatNode): Call = new Call(_value)
  }
  implicit object FloatNode_upcast_FloatNode extends izumi.idealingua.runtime.IRTCast[FloatNode, FloatNode] {
    override def convert(_value: FloatNode): FloatNode = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      FloatNode.Struct(lit = _value.lit)
    }
  }
  implicit class FloatNodeExtensions(override protected val _value: FloatNode) extends izumi.idealingua.runtime.IRTConversions[FloatNode]
}
       