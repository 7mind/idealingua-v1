package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.il.ast.typed.AdtMember
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AdtProduct
import izumi.idealingua.translator.tocsharp.types.CSharpType
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Adt` as the same pre-extension `AdtProduct`
  * the legacy `CSharpTranslator.renderAdt` produces (modulo the extension
  * chain).
  *
  * IMPL-7c Phase B M3: byte-parity port. The emitted top-level shape is
  * the legacy ADT triple:
  *   - per-member `using _<MemberName> = <NativeType>;` aliases,
  *   - `public abstract class <Name>` with nested `I<Name>Visitor`
  *     interface and per-member sealed sub-classes,
  *   - per-sub-class explicit conversion operators (or commented-out
  *     dummies when the underlying type is an `InterfaceId`).
  *
  * Per-branch helpers (`CSharpType(member.typeId).renderType(true)` for
  * the unambig name lookup and `CSharpType(member.typeId)` for native
  * type rendering) keep `Typespace` + `CSharpImports` threaded per-call,
  * same convention as M2.
  *
  * Extension chain (`ext.preModelEmit(ctx, adt)` / `ext.postModelEmit(ctx, adt)`
  * / `ext.imports(ctx, i)`) is omitted: the default C# extension set
  * (`JsonNetExtension`) has no `Adt` overrides, so the pre-extension
  * product is byte-equal to the post-extension product for the default
  * extension list.
  *
  * `renderAdtImpl(name, alternatives, renderUsings)` is public so
  * `DomainCSServiceMethodProduct` can reuse it for nested ADT outputs
  * (`Algebraic`) in service methods, matching the legacy translator's
  * sharing pattern (`renderServiceMethodOutModel` → `renderAdtImpl`).
  */
final class DomainCSAdtRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderAdt(i: NewTypeDef.Adt, ts: Typespace, im: CSharpImports): AdtProduct = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    AdtProduct(renderAdtImpl(i.id.name, i.alternatives), im.renderImports(List.empty))
  }

  /** Mirror of legacy `CSharpTranslator.renderAdtImpl` (lines 156-174).
    * Public so `DomainCSServiceMethodProduct` can reuse it for nested ADT
    * outputs in service methods. Extension chain omitted: with an empty
    * extension list, `ext.preModelEmit` / `ext.postModelEmit` resolve to
    * `""`, so the surrounding whitespace from the legacy template is
    * preserved exactly by substituting blank interpolations.
    */
  def renderAdtImpl(adtName: String, members: List[AdtMember], renderUsings: Boolean = true)(implicit im: CSharpImports, ts: Typespace): String = {
    s"""${im.renderUsings()}
       |${if (renderUsings) members.map(m => renderAdtUsings(m)).mkString("\n") else ""}
       |
       |
       |public abstract class $adtName {
       |    public interface I${adtName}Visitor {
       |${members.map(m => s"        void Visit(${m.typename} visitor);").mkString("\n")}
       |    }
       |
       |    public abstract void Visit(I${adtName}Visitor visitor);
       |    private $adtName() {}
       |
       |${members.map(m => renderAdtMember(adtName, m)).mkString("\n").shift(4)}
       |}
       |
     """.stripMargin
  }

  /** Mirror of legacy `renderAdtUsings` (line 152-154). */
  def renderAdtUsings(m: AdtMember)(implicit im: CSharpImports, ts: Typespace): String = {
    s"using _${m.typename} = ${CSharpType(m.typeId).renderType(true)};"
  }

  /** Mirror of legacy `renderAdtMember` (lines 107-150). */
  private def renderAdtMember(adtName: String, member: AdtMember)(implicit im: CSharpImports, ts: Typespace): String = {
    val needsFQN = im.imports.find(i => i.id == member.typeId)
    val nonambName =
      if (needsFQN.isDefined && needsFQN.get.usingName == "")
        CSharpType(member.typeId).renderType(true)
      else s"_${member.typename}"

    val operators =
      s"""    public static explicit operator $nonambName(${member.typename} m) {
         |        return m.Value;
         |    }
         |
         |    public static explicit operator ${member.typename}($nonambName m) {
         |        return new ${member.typename}(m);
         |    }
       """.stripMargin

    val operatorsDummy =
      s"""    // We would normally want to have an operator, but unfortunately if it is an interface,
         |    // it will fail on "user-defined conversions to or from an interface are not allowed".
         |    // public static explicit operator $nonambName(${member.typename} m) {
         |    //     return m.Value;
         |    // }
         |    //
         |    // public static explicit operator ${member.typename}($nonambName m) {
         |    //     return new ${member.typename}(m);
         |    // }
       """.stripMargin

    s"""public sealed class ${member.typename}: $adtName {
       |    public $nonambName Value { get; private set; }
       |    public ${member.typename}($nonambName value) {
       |        this.Value = value;
       |    }
       |
       |    public override void Visit(I${adtName}Visitor visitor) {
       |        visitor.Visit(this);
       |    }
       |
       |${if (member.typeId.isInstanceOf[InterfaceId]) operatorsDummy else operators}
       |}
     """.stripMargin
  }
}
