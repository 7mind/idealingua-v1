package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.types.CSharpField
import izumi.idealingua.typer.ir.Domain

/** Domain-consuming twin of `CSharpField` (PR-02 IMPL-10-prep-Cs1).
  *
  * Legacy `CSharpField` takes a `tp: CSharpType` directly and an implicit
  * `Typespace` only through the `apply(field, ...)` factory. Here we hold
  * a `DomainCSharpType` (Domain-backed) instead.
  *
  * Name-safety logic (`safeName`, `safeVarName`) is shared with legacy
  * `CSharpField` — the reserved-keyword list and the `_` suffix policy for
  * struct-name clashes is byte-equal, so we delegate to
  * `CSharpField.safeName` to avoid duplication. This is the only legacy
  * coupling that remains; it carries no `Typespace` dependency.
  */
final case class DomainCSField(
  name: String,
  tp: DomainCSharpType,
  structName: String,
  by: Seq[String],
) {
  def renderMemberName(capitalize: Boolean = true, uncapitalize: Boolean = false): String = {
    CSharpField.safeName(name, capitalize, uncapitalize, structName)
  }

  private def renderMemberImpl(forInterface: Boolean, by: String): String = {
    if (tp.isNative) {
      s"${if (forInterface) "" else "public "}${tp.renderType(true)} ${if (by.isEmpty) "" else s"$by."}${renderMemberName()} { get; set; }"
    } else {
      "Not Implemented renderMember()"
    }
  }

  def renderMember(forInterface: Boolean): String = {
    if (forInterface) {
      if (by.isEmpty) {
        renderMemberImpl(forInterface, "")
      } else {
        s"new ${renderMemberImpl(forInterface, "")}"
      }
    } else {
      if (by.isEmpty) {
        renderMemberImpl(forInterface, "")
      } else {
        by.map(b => renderMemberImpl(forInterface, b)).mkString("\n")
      }
    }
  }
}

object DomainCSField {
  def apply(
    field: Field,
    structName: String,
    by: Seq[String] = Seq.empty,
  )(implicit
    im: CSharpImports,
    domain: Domain,
  ): DomainCSField = new DomainCSField(field.name, DomainCSharpType(field.typeId), structName, by)

  /** Same as `CSharpField.safeVarName`. */
  def safeVarName(name: String): String = CSharpField.safeName(name, capitalize = false, uncapitalize = false, "")
}
