package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.IdentifierProduct
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Identifier` as the same pre-extension
  * `IdentifierProduct` the legacy `CSharpTranslator.renderIdentifier`
  * produces (modulo the extension chain).
  *
  * IMPL-10-prep-Cs1: byte-parity port now consumes `DomainCSField` and
  * `DomainCSClass` (Domain-backed) instead of the legacy `CSharpField`
  * and `CSharpClass`. `Typespace` no longer threaded — the renderer
  * carries `ctx.domain` implicitly.
  *
  * Identifiers carry no inheritance (`TypeDef.Identifier.fields: List[IdField]`);
  * we widen `IdField → Field` and feed the Domain `DomainCSField` factory.
  *
  * Extension chain (`ext.preModelEmit` / `ext.postModelEmit` /
  * `ext.imports`) is omitted: the default C# extension set
  * (`JsonNetExtension`) has no `Identifier` overrides, so the
  * pre-extension product is byte-equal to the post-extension product
  * for the default extension list.
  */
final class DomainCSIdRenderer(ctx: DomainCSContext) {

  def renderIdentifier(i: NewTypeDef.Identifier, im: CSharpImports): IdentifierProduct =
    renderIdentifier(i, im, preSplice = "", postSplice = "", extraImports = List.empty)

  /** Production-swap variant: splices `preSplice` / `postSplice` into the
    * legacy `${ext.preModelEmit(ctx, i)}` / `${ext.postModelEmit(ctx, i)}`
    * slots, and merges `extraImports` into the header import list.
    */
  def renderIdentifier(
    i: NewTypeDef.Identifier,
    im: CSharpImports,
    preSplice: String,
    postSplice: String,
    extraImports: List[String],
  ): IdentifierProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im

    val widened: List[Field] = i.fields.map(idFieldToField)
    val fields               = widened.map(f => DomainCSField(f, i.id.name))
    val fieldsSorted         = fields.sortBy(_.name)
    val csClass              = DomainCSClass(i.id, i.id.name, fields)
    val prefixLength         = i.id.name.length + 1

    val decl =
      s"""${im.renderUsings()}
         |$preSplice
         |${csClass.renderHeader()} {
         |    private static char[] idSplitter = new char[]{':'};
         |${csClass.render(withWrapper = false, withSlices = false, withRTTI = true).shift(4)}
         |    public override string ToString() {
         |        var suffix = ${fieldsSorted.map(f => f.tp.renderToString(f.renderMemberName(), escape = true)).mkString(" + \":\" + ")};
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
         |        var parts = value.Substring($prefixLength, value.Length - $prefixLength).Split(idSplitter, StringSplitOptions.None);
         |        if (parts.Length != ${fields.length}) {
         |            throw new ArgumentException(string.Format("Expected identifier for type ${i.id.name} with ${fields.length} parts, got {0} in string {1}", parts.Length, value));
         |        }
         |
         |        var res = new ${i.id.name}();
         |${fieldsSorted.zipWithIndex.map { case (f, index) => s"res.${f.renderMemberName()} = ${f.tp.renderFromString(s"parts[$index]", unescape = true)};" }
          .mkString("\n").shift(8)}
         |        return res;
         |    }
         |}
         |
         |$postSplice
         """.stripMargin

    IdentifierProduct(
      decl,
      im.renderImports(List("System", "System.Collections", "System.Collections.Generic") ++ extraImports),
    )
  }

  /** Widen an `IdField` to a `Field`. */
  private def idFieldToField(idf: IdField): Field =
    Field(idf.typeId, idf.name, idf.meta)
}
