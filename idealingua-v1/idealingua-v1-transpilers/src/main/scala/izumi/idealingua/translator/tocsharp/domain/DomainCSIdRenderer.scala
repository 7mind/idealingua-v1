package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.IdentifierProduct
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Identifier` as the same pre-extension
  * `IdentifierProduct` the legacy `CSharpTranslator.renderIdentifier`
  * produces (modulo the extension chain).
  *
  * F-TextTree M3 — ported to the typed-renderer protocol. The identifier
  * body itself emits no `CSRefHandle.TypeRef` values at the envelope
  * level: the struct render call returns a `String` with fully-resolved
  * field declarations; `renderToString`/`renderFromString` likewise
  * return fully-resolved code via the converter. Protocol adoption is
  * structural for uniformity.
  */
final class DomainCSIdRenderer(ctx: DomainCSContext) {

  def renderIdentifier(i: NewTypeDef.Identifier, im: CSharpImports): IdentifierProduct =
    renderIdentifier(i, im, preSplice = "", postSplice = "", extraImports = List.empty)

  def renderIdentifier(
    i: NewTypeDef.Identifier,
    im: CSharpImports,
    preSplice: String,
    postSplice: String,
    extraImports: List[String],
  ): IdentifierProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im
    val resolver                    = new DomainCSTypeResolver()

    val widened: List[Field] = i.fields.map(idFieldToField)
    val fields               = widened.map(f => DomainCSField(f, i.id.name))
    val fieldsSorted         = fields.sortBy(_.name)
    val csClass              = DomainCSClass(i.id, i.id.name, fields)
    val prefixLength         = i.id.name.length + 1

    val suffix = fieldsSorted.map(f => f.tp.renderToString(f.renderMemberName(), escape = true)).mkString(" + \":\" + ")
    val assigns = fieldsSorted.zipWithIndex.map {
      case (f, index) =>
        s"res.${f.renderMemberName()} = ${f.tp.renderFromString(s"parts[$index]", unescape = true)};"
    }.mkString("\n")

    val fieldCount = fields.length.toString
    val prefixLengthStr = prefixLength.toString

    val tree: TextTree[CSRefHandle] =
      q"""${im.renderUsings()}
         |$preSplice
         |${csClass.renderHeader()} {
         |    private static char[] idSplitter = new char[]{':'};
         |${csClass.render(withWrapper = false, withSlices = false, withRTTI = true).shift(4)}
         |    public override string ToString() {
         |        var suffix = $suffix;
         |        return "${i.id.name}#" + suffix;
         |    }
         |
         |    public static ${i.id.name} From(string value) {
         |        if (value == null) {
         |            throw new ArgumentNullException("value");
         |        }
         |
         |        if (!value.StartsWith("${i.id.name}#", StringComparison.Ordinal)) {
         |            throw new ArgumentException(string.Format("Expected identifier for type ${i.id.name}, got {0}", value));
         |        }
         |
         |        var parts = value.Substring($prefixLengthStr, value.Length - $prefixLengthStr).Split(idSplitter, StringSplitOptions.None);
         |        if (parts.Length != $fieldCount) {
         |            throw new ArgumentException(string.Format("Expected identifier for type ${i.id.name} with $fieldCount parts, got {0} in string {1}", parts.Length, value));
         |        }
         |
         |        var res = new ${i.id.name}();
         |${assigns.shift(8)}
         |        return res;
         |    }
         |}
         |
         |$postSplice
         |         """.stripMargin

    IdentifierProduct(
      tree.mapRender(resolver.resolve),
      im.renderImports(List("System", "System.Collections", "System.Collections.Generic") ++ extraImports),
    )
  }

  /** Widen an `IdField` to a `Field`. */
  private def idFieldToField(idf: IdField): Field =
    Field(idf.typeId, idf.name, idf.meta)
}
