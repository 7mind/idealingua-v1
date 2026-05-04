package idltest.ast



sealed trait AST extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait ASTCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAST: Encoder.AsObject[AST] = Encoder.AsObject.instance {
    case v: AST.IntNode =>
      Map("IntNode" -> v.value).asJsonObject
    case v: AST.FloatRenamed =>
      Map("FloatRenamed" -> v.value).asJsonObject
    case v: AST.BoolNode =>
      Map("BoolNode" -> v.value).asJsonObject
    case v: AST.SymNode =>
      Map("SymNode" -> v.value).asJsonObject
    case v: AST.AppNode =>
      Map("AppNode" -> v.value).asJsonObject
    case v: AST.LamNode =>
      Map("LamNode" -> v.value).asJsonObject
    case v: AST.IfNode =>
      Map("IfNode" -> v.value).asJsonObject
  }
  implicit val decodeAST: Decoder[AST] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "IntNode" =>
        value.as[_root_.idltest.ast.IntNode].map(AST.IntNode.apply)
      case "FloatRenamed" =>
        value.as[_root_.idltest.ast.FloatNode].map(AST.FloatRenamed.apply)
      case "BoolNode" =>
        value.as[_root_.idltest.ast.BoolNode].map(AST.BoolNode.apply)
      case "SymNode" =>
        value.as[_root_.idltest.ast.SymNode].map(AST.SymNode.apply)
      case "AppNode" =>
        value.as[_root_.idltest.ast.AppNode].map(AST.AppNode.apply)
      case "LamNode" =>
        value.as[_root_.idltest.ast.LamNode].map(AST.LamNode.apply)
      case "IfNode" =>
        value.as[_root_.idltest.ast.IfNode].map(AST.IfNode.apply)
      case _ =>
        val cname = "idltest.ast.AST"
        val alts = List("IntNode", "FloatRenamed", "BoolNode", "SymNode", "AppNode", "LamNode", "IfNode").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object AST extends ASTCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = AST
  final case class IntNode(value: _root_.idltest.ast.IntNode) extends AST
  implicit def intoIntNode(value: _root_.idltest.ast.IntNode): AST = AST.IntNode(value)
  implicit def fromIntNode(value: AST.IntNode): _root_.idltest.ast.IntNode = value.value
  final case class FloatRenamed(value: _root_.idltest.ast.FloatNode) extends AST
  implicit def intoFloatRenamed(value: _root_.idltest.ast.FloatNode): AST = AST.FloatRenamed(value)
  implicit def fromFloatRenamed(value: AST.FloatRenamed): _root_.idltest.ast.FloatNode = value.value
  final case class BoolNode(value: _root_.idltest.ast.BoolNode) extends AST
  implicit def intoBoolNode(value: _root_.idltest.ast.BoolNode): AST = AST.BoolNode(value)
  implicit def fromBoolNode(value: AST.BoolNode): _root_.idltest.ast.BoolNode = value.value
  final case class SymNode(value: _root_.idltest.ast.SymNode) extends AST
  implicit def intoSymNode(value: _root_.idltest.ast.SymNode): AST = AST.SymNode(value)
  implicit def fromSymNode(value: AST.SymNode): _root_.idltest.ast.SymNode = value.value
  final case class AppNode(value: _root_.idltest.ast.AppNode) extends AST
  implicit def intoAppNode(value: _root_.idltest.ast.AppNode): AST = AST.AppNode(value)
  implicit def fromAppNode(value: AST.AppNode): _root_.idltest.ast.AppNode = value.value
  final case class LamNode(value: _root_.idltest.ast.LamNode) extends AST
  implicit def intoLamNode(value: _root_.idltest.ast.LamNode): AST = AST.LamNode(value)
  implicit def fromLamNode(value: AST.LamNode): _root_.idltest.ast.LamNode = value.value
  final case class IfNode(value: _root_.idltest.ast.IfNode) extends AST
  implicit def intoIfNode(value: _root_.idltest.ast.IfNode): AST = AST.IfNode(value)
  implicit def fromIfNode(value: AST.IfNode): _root_.idltest.ast.IfNode = value.value
}
       