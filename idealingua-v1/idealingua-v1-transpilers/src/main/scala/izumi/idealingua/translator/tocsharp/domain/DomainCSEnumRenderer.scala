package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.translator.tocsharp.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Enum` as the same pre-extension `EnumProduct`
  * the legacy `CSharpTranslator.renderEnumeration` produces.
  *
  * F-TextTree M3 — ported to the typed-renderer protocol. The enum body
  * contributes no type references (members are plain string literals),
  * so the harvest set is empty; protocol adoption is purely structural,
  * keeping the renderer family uniform for the future option-B'
  * import-collection pass.
  */
final class DomainCSEnumRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderEnumeration(i: TypeDef.Enum): EnumProduct =
    renderEnumeration(i, postSplice = "", header = "")

  /** M5 production-swap variant: takes a `postSplice` (JsonNet converter
    * block) spliced into the legacy `${ext.postModelEmit(ctx, i)}` slot
    * and a `header` (import lines) for the product header.
    */
  def renderEnumeration(i: TypeDef.Enum, postSplice: String, header: String): EnumProduct = {
    val name = i.id.name

    val membersTree: TextTree[CSRefHandle] = {
      val it = i.members.map(_.value).iterator
      it.map { m =>
        val suffix = if (it.hasNext) "," else ""
        TextTree.text[CSRefHandle](s"$m$suffix")
      }.toList.joinN()
    }

    val allMembersTree: TextTree[CSRefHandle] = {
      val it = i.members.map(_.value).iterator
      it.map { m =>
        val suffix = if (it.hasNext) "," else ""
        TextTree.text[CSRefHandle](s"$name.$m$suffix")
      }.toList.joinN()
    }

    val fromCasesTree: TextTree[CSRefHandle] = {
      i.members.map(_.value).map { m =>
        TextTree.text[CSRefHandle](s"""case "$m": return $name.$m;""")
      }.joinN()
    }

    val tree: TextTree[CSRefHandle] =
      q"""// $name Enumeration
         |public enum $name {
         |${membersTree.shift(4)}
         |}
         |
         |public static class ${name}Helpers {
         |    public static $name From(string value) {
         |        switch (value) {
         |${fromCasesTree.shift(12)}
         |            default:
         |                throw new ArgumentOutOfRangeException();
         |        }
         |    }
         |
         |    public static bool IsValid(string value) {
         |        return Enum.IsDefined(typeof($name), value);
         |    }
         |
         |    // The elements in the array are still changeable, please use with care.
         |    private static readonly $name[] all = new $name[] {
         |${allMembersTree.shift(8)}
         |    };
         |
         |    public static $name[] GetAll() {
         |        return ${name}Helpers.all;
         |    }
         |
         |    // Extensions
         |
         |    public static string ToString(this $name e) {
         |        return Enum.GetName(typeof($name), e);
         |    }
         |}
         |
         |$postSplice
         |""".stripMargin

    EnumProduct(tree.mapRender(_ => ""), header, "")
  }
}
