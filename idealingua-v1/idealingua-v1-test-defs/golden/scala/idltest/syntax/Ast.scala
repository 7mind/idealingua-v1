package idltest.syntax



sealed trait Ast extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product

trait AstCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAst: Encoder.AsObject[Ast] = Encoder.AsObject.instance {
    case v: Ast.TestMixin =>
      Map("TestMixin" -> v.value).asJsonObject
    case v: Ast.TestDto =>
      Map("TestDto" -> v.value).asJsonObject
    case v: Ast.TestOneliners =>
      Map("TestOneliners" -> v.value).asJsonObject
  }
  implicit val decodeAst: Decoder[Ast] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "TestMixin" =>
        value.as[_root_.idltest.syntax.TestMixin].map(Ast.TestMixin.apply)
      case "TestDto" =>
        value.as[_root_.idltest.syntax.TestDto].map(Ast.TestDto.apply)
      case "TestOneliners" =>
        value.as[_root_.idltest.syntax.TestOneliners].map(Ast.TestOneliners.apply)
      case _ =>
        val cname = "idltest.syntax.Ast"
        val alts = List("TestMixin", "TestDto", "TestOneliners").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield {
      result
    }
  })
}

object Ast extends AstCirce with izumi.idealingua.runtime.model.IDLAdt {
  import _root_.scala.language.implicitConversions
  type Element = Ast
  final case class TestMixin(value: _root_.idltest.syntax.TestMixin) extends Ast
  implicit def intoTestMixin(value: _root_.idltest.syntax.TestMixin): Ast = Ast.TestMixin(value)
  implicit def fromTestMixin(value: Ast.TestMixin): _root_.idltest.syntax.TestMixin = value.value
  final case class TestDto(value: _root_.idltest.syntax.TestDto) extends Ast
  implicit def intoTestDto(value: _root_.idltest.syntax.TestDto): Ast = Ast.TestDto(value)
  implicit def fromTestDto(value: Ast.TestDto): _root_.idltest.syntax.TestDto = value.value
  final case class TestOneliners(value: _root_.idltest.syntax.TestOneliners) extends Ast
  implicit def intoTestOneliners(value: _root_.idltest.syntax.TestOneliners): Ast = Ast.TestOneliners(value)
  implicit def fromTestOneliners(value: Ast.TestOneliners): _root_.idltest.syntax.TestOneliners = value.value
}
       