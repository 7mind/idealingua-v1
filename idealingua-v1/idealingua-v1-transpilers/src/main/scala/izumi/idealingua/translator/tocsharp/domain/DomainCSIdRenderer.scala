package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.IdentifierProduct
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Identifier` as the same pre-extension
  * `IdentifierProduct` the legacy `CSharpTranslator.renderIdentifier`
  * produces (modulo the extension chain).
  *
  * IMPL-7c Phase B M2: byte-parity port. Identifiers carry no
  * inheritance (`TypeDef.Identifier.fields: List[IdField]`); the legacy
  * renderer's `typespace.structure.structure(i)` call returns the widened
  * id-field set directly, so we widen `IdField → Field` and feed the
  * legacy `CSharpField` (via `CSharpField(field, structName)`).
  *
  * `CSharpImports` + `Typespace` are threaded per-call (mirrors the
  * Phase A pattern; the legacy `CSharpImports.apply(definition, ...)`
  * implementation walks the legacy `Typespace`). M5 production-swap will
  * replace the `Typespace` threading with a `DomainCSImports` shim, but
  * at M2 we reuse the legacy plumbing exactly as Phase A delegation does.
  *
  * Production path remains unchanged — `DomainCSharpTranslator.translate()`
  * still goes through Phase A delegation. This renderer is exercised only
  * by the M2 byte-parity unit test until M5 swaps the production path.
  *
  * Extension chain (`ext.preModelEmit` / `ext.postModelEmit` /
  * `ext.imports`) is omitted: the default C# extension set
  * (`JsonNetExtension`) has no `Identifier` overrides, so the
  * pre-extension product is byte-equal to the post-extension product
  * for the default extension list. The literal raw-string template
  * mirrors the legacy renderer with empty-string substitutions for the
  * extension splice points so the resulting string is byte-equal to the
  * legacy output under an empty extension list.
  */
final class DomainCSIdRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderIdentifier(i: NewTypeDef.Identifier, ts: Typespace, im: CSharpImports): IdentifierProduct = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    // Widen `IdField → Field` so `CSharpField` (which takes a `Field`)
    // constructs the same name + type pair as the legacy
    // `structure.structure(i).all.map(f => CSharpField(f.field, i.id.name))`.
    val widened: List[Field] = i.fields.map(idFieldToField)
    val fields               = widened.map(f => CSharpField(f, i.id.name))
    val fieldsSorted         = fields.sortBy(_.name)
    val csClass              = CSharpClass(i.id, i.id.name, fields)
    val prefixLength         = i.id.name.length + 1

    // Mirrors `CSharpTranslator.renderIdentifier` (lines 309-342) with
    // `${ext.preModelEmit(ctx, i)}` / `${ext.postModelEmit(ctx, i)}`
    // resolved to the empty string.
    val decl =
      s"""${im.renderUsings()}
         |
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
         |
         """.stripMargin

    IdentifierProduct(
      decl,
      im.renderImports(List("System", "System.Collections", "System.Collections.Generic")),
    )
  }

  /** Widen an `IdField` to a `Field`. Matches the legacy
    * `StructuralQueriesImpl` widening (id-field `name`, `typeId`, `meta`).
    */
  private def idFieldToField(idf: IdField): Field =
    Field(idf.typeId, idf.name, idf.meta)
}
