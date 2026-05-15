package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.typer.ir.Domain

/** Domain-consuming twin of the (now-deleted) legacy `CSharpField` (PR-02
  * IMPL-10-prep-Cs1; reserved-keyword `safeName` inlined post-IMPL-10c).
  *
  * Holds a `DomainCSharpType` (Domain-backed) rather than the legacy
  * `CSharpType` (Typespace-backed). `safeName` / `safeVarName` are inlined
  * verbatim from the deleted `CSharpField`; their behaviour is unchanged.
  */
final case class DomainCSField(
  name: String,
  tp: DomainCSharpType,
  structName: String,
  by: Seq[String],
) {
  def renderMemberName(capitalize: Boolean = true, uncapitalize: Boolean = false): String = {
    DomainCSField.safeName(name, capitalize, uncapitalize, structName)
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
  import izumi.fundamentals.platform.strings.IzString._

  def apply(
    field: Field,
    structName: String,
    by: Seq[String] = Seq.empty,
  )(implicit
    im: CSharpImports,
    domain: Domain,
  ): DomainCSField = new DomainCSField(field.name, DomainCSharpType(field.typeId), structName, by)

  /** Reserved-keyword-safe identifier rendering. Inlined verbatim from the
    * (now-deleted) legacy `CSharpField.safeName`. */
  def safeVarName(name: String): String = safeName(name, capitalize = false, uncapitalize = false, "")

  def safeName(name: String, capitalize: Boolean, uncapitalize: Boolean, structName: String): String = {
    val systemReserved = Seq("Type", "Environment")

    val reserved = Seq(
      "abstract",
      "as",
      "base",
      "bool",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "checked",
      "class",
      "const",
      "continue",
      "decimal",
      "default",
      "delegate",
      "do",
      "double",
      "else",
      "enum",
      "event",
      "explicit",
      "extern",
      "false",
      "finally",
      "fixed",
      "float",
      "for",
      "foreach",
      "goto",
      "if",
      "implicit",
      "in",
      "int",
      "interface",
      "internal",
      "is",
      "lock",
      "long",
      "namespace",
      "new",
      "null",
      "object",
      "operator",
      "out",
      "override",
      "params",
      "private",
      "protected",
      "public",
      "readonly",
      "ref",
      "return",
      "sbyte",
      "sealed",
      "short",
      "sizeof",
      "stackalloc",
      "static",
      "string",
      "struct",
      "switch",
      "this",
      "throw",
      "true",
      "try",
      "typeof",
      "uint",
      "ulong",
      "unchecked",
      "unsafe",
      "ushort",
      "using",
      "using",
      "static",
      "virtual",
      "void",
      "volatile",
      "while",
    )

    val all = systemReserved ++ reserved

    val finalName = if (capitalize) name.capitalize else if (uncapitalize) name.uncapitalize else name
    if (finalName == structName) {
      s"@${finalName}_"
    } else {
      if (all.contains(finalName)) s"@$finalName" else finalName
    }
  }
}
