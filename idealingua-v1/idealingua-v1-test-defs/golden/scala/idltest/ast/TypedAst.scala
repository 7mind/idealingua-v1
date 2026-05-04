package idltest.ast



sealed trait TypedAst extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait TypedAstCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTypedAst: Encoder.AsObject[TypedAst] = Encoder.AsObject.instance {
    case v: TypedAst.TIntNode =>
      Map("TIntNode" -> v.value).asJsonObject
    case v: TypedAst.TFloatNode =>
      Map("TFloatNode" -> v.value).asJsonObject
    case v: TypedAst.TBoolNode =>
      Map("TBoolNode" -> v.value).asJsonObject
    case v: TypedAst.TSymNode =>
      Map("TSymNode" -> v.value).asJsonObject
    case v: TypedAst.TAppNode =>
      Map("TAppNode" -> v.value).asJsonObject
    case v: TypedAst.TLamNode =>
      Map("TLamNode" -> v.value).asJsonObject
    case v: TypedAst.TIfNode =>
      Map("TIfNode" -> v.value).asJsonObject
  }
  implicit val decodeTypedAst: Decoder[TypedAst] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "TIntNode" =>
        value.as[_root_.idltest.ast.TIntNode].map(TypedAst.TIntNode.apply)
      case "TFloatNode" =>
        value.as[_root_.idltest.ast.TFloatNode].map(TypedAst.TFloatNode.apply)
      case "TBoolNode" =>
        value.as[_root_.idltest.ast.TBoolNode].map(TypedAst.TBoolNode.apply)
      case "TSymNode" =>
        value.as[_root_.idltest.ast.TSymNode].map(TypedAst.TSymNode.apply)
      case "TAppNode" =>
        value.as[_root_.idltest.ast.TAppNode].map(TypedAst.TAppNode.apply)
      case "TLamNode" =>
        value.as[_root_.idltest.ast.TLamNode].map(TypedAst.TLamNode.apply)
      case "TIfNode" =>
        value.as[_root_.idltest.ast.TIfNode].map(TypedAst.TIfNode.apply)
      case _ =>
        val cname = "idltest.ast.TypedAst"
        val alts = List("TIntNode", "TFloatNode", "TBoolNode", "TSymNode", "TAppNode", "TLamNode", "TIfNode").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object TypedAst extends TypedAstCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = TypedAst
  final case class TIntNode(value: _root_.idltest.ast.TIntNode) extends TypedAst
  implicit def intoTIntNode(value: _root_.idltest.ast.TIntNode): TypedAst = TypedAst.TIntNode(value)
  implicit def fromTIntNode(value: TypedAst.TIntNode): _root_.idltest.ast.TIntNode = value.value
  final case class TFloatNode(value: _root_.idltest.ast.TFloatNode) extends TypedAst
  implicit def intoTFloatNode(value: _root_.idltest.ast.TFloatNode): TypedAst = TypedAst.TFloatNode(value)
  implicit def fromTFloatNode(value: TypedAst.TFloatNode): _root_.idltest.ast.TFloatNode = value.value
  final case class TBoolNode(value: _root_.idltest.ast.TBoolNode) extends TypedAst
  implicit def intoTBoolNode(value: _root_.idltest.ast.TBoolNode): TypedAst = TypedAst.TBoolNode(value)
  implicit def fromTBoolNode(value: TypedAst.TBoolNode): _root_.idltest.ast.TBoolNode = value.value
  final case class TSymNode(value: _root_.idltest.ast.TSymNode) extends TypedAst
  implicit def intoTSymNode(value: _root_.idltest.ast.TSymNode): TypedAst = TypedAst.TSymNode(value)
  implicit def fromTSymNode(value: TypedAst.TSymNode): _root_.idltest.ast.TSymNode = value.value
  final case class TAppNode(value: _root_.idltest.ast.TAppNode) extends TypedAst
  implicit def intoTAppNode(value: _root_.idltest.ast.TAppNode): TypedAst = TypedAst.TAppNode(value)
  implicit def fromTAppNode(value: TypedAst.TAppNode): _root_.idltest.ast.TAppNode = value.value
  final case class TLamNode(value: _root_.idltest.ast.TLamNode) extends TypedAst
  implicit def intoTLamNode(value: _root_.idltest.ast.TLamNode): TypedAst = TypedAst.TLamNode(value)
  implicit def fromTLamNode(value: TypedAst.TLamNode): _root_.idltest.ast.TLamNode = value.value
  final case class TIfNode(value: _root_.idltest.ast.TIfNode) extends TypedAst
  implicit def intoTIfNode(value: _root_.idltest.ast.TIfNode): TypedAst = TypedAst.TIfNode(value)
  implicit def fromTIfNode(value: TypedAst.TIfNode): _root_.idltest.ast.TIfNode = value.value
}
       