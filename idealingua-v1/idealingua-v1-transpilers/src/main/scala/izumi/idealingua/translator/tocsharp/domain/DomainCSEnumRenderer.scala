package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.translator.tocsharp.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Enum` as the same pre-extension `EnumProduct`
  * the legacy `CSharpTranslator.renderEnumeration` produces before the
  * extension chain runs.
  *
  * IMPL-7c Phase B M1 scope: enum body + `<Name>Helpers` companion only.
  * The legacy `renderEnumeration` interpolates
  * `${ext.postModelEmit(ctx, i)}` at the tail of the body and threads
  * `ext.imports(ctx, i)` into the header import list. The default C#
  * extension set (`JsonNetExtension`) has no `postModelEmit` for
  * `Enumeration` and no `imports` for `Enumeration` — both return empty
  * with the default extension list, so the pre-extension product is
  * byte-equal to the post-extension product for the default
  * configuration. M2+ will reintegrate the extension chain when it
  * starts to matter for richer renderers.
  *
  * `TypeDef.Enum` carries `id: EnumId`, `members: List[EnumMember]`,
  * `meta: NodeMeta` — `EnumMember` is the legacy `EnumMember` type
  * (see `idealingua-v1-model/.../typed/TypeDef.scala`), so the body
  * construction is field-for-field identical to legacy.
  *
  * The header is `im.renderImports(List("System"))` and is threaded from
  * the call-site (the per-definition `CSharpImports` instance is owned
  * by the production translator, not this renderer).
  */
final class DomainCSEnumRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderEnumeration(i: TypeDef.Enum): EnumProduct =
    renderEnumeration(i, postSplice = "", header = "")

  /** M5 production-swap variant: takes a `postSplice` (JsonNet converter
    * block) spliced into the legacy `${ext.postModelEmit(ctx, i)}` slot
    * (legacy `:292`) and a `header` (import lines) for the product header.
    *
    * The default no-splice call (used by M1 unit tests) preserves the
    * exact pre-M5 string shape (`postSplice = ""`, `header = ""`).
    */
  def renderEnumeration(i: TypeDef.Enum, postSplice: String, header: String): EnumProduct = {
    val name = i.id.name

    val members =
      i.members.map(_.value).map(m => s"$m${if (m == i.members.last.value) "" else ","}").mkString("\n").shift(4)
    val allMembers =
      i.members.map(_.value).map(m => s"$name.$m${if (m == i.members.last.value) "" else ","}").mkString("\n").shift(8)
    val fromCases =
      i.members.map(_.value).map(m => s"""case \"$m\": return $name.$m;""").mkString("\n").shift(12)

    val decl: String =
      s"""// $name Enumeration
         |public enum $name {
         |$members
         |}
         |
         |public static class ${name}Helpers {
         |    public static $name From(string value) {
         |        switch (value) {
         |$fromCases
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
         |$allMembers
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

    EnumProduct(decl, header, "")
  }
}
