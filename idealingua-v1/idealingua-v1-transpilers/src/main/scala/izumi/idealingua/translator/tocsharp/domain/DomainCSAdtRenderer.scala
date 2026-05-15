package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.il.ast.typed.AdtMember
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AdtProduct
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Adt` as the same pre-extension `AdtProduct`
  * the legacy `CSharpTranslator.renderAdt` produces.
  *
  * F-TextTree M3 — ported to the typed-renderer protocol. The outer ADT
  * envelope is composed as `TextTree[CSRefHandle]` rendered at the
  * product boundary; per-member rendering (`renderAdtMember`) returns
  * `String` since its content is heavily conditional (interface-typed
  * members emit operator stubs as comments). The per-member function is
  * kept as `String`-producing for surface-symmetry with the legacy
  * helper signatures used by `DomainCSServiceMethodProduct`.
  *
  * `renderAdtImpl(name, alternatives, renderUsings)` is public so
  * `DomainCSServiceMethodProduct` can reuse it for nested ADT outputs.
  */
final class DomainCSAdtRenderer(@annotation.unused ctx: DomainCSContext) {

  def renderAdt(i: NewTypeDef.Adt, im: CSharpImports): AdtProduct =
    renderAdt(i, im, preSplice = "", postSplice = "", extraImports = List.empty)

  def renderAdt(
    i: NewTypeDef.Adt,
    im: CSharpImports,
    preSplice: String,
    postSplice: String,
    extraImports: List[String],
  ): AdtProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im

    AdtProduct(
      renderAdtImpl(i.id.name, i.alternatives, renderUsings = true, preSplice = preSplice, postSplice = postSplice),
      im.renderImports(extraImports),
    )
  }

  /** Mirror of legacy `CSharpTranslator.renderAdtImpl`. Returns a
    * `String` (consumed by the renderer envelopes that splice it inline);
    * the body itself is composed as `TextTree[CSRefHandle]` and rendered
    * at the function return boundary.
    */
  def renderAdtImpl(
    adtName: String,
    members: List[AdtMember],
    renderUsings: Boolean = true,
    preSplice: String = "",
    postSplice: String = "",
  )(implicit im: CSharpImports, domain: Domain): String = {
    val resolver = new DomainCSTypeResolver()

    val visitorMethods = members.map(m => s"        void Visit(${m.typename} visitor);").mkString("\n")
    val membersBody    = members.map(m => renderAdtMember(adtName, m)).mkString("\n")
    val usings         = if (renderUsings) members.map(m => renderAdtUsings(m)).mkString("\n") else ""

    val tree: TextTree[CSRefHandle] =
      q"""${im.renderUsings()}
         |$usings
         |
         |$preSplice
         |public abstract class $adtName {
         |    public interface I${adtName}Visitor {
         |$visitorMethods
         |    }
         |
         |    public abstract void Visit(I${adtName}Visitor visitor);
         |    private $adtName() {}
         |
         |${membersBody.shift(4)}
         |}
         |$postSplice
         |     """.stripMargin

    tree.mapRender(resolver.resolve)
  }

  def renderAdtUsings(m: AdtMember)(implicit im: CSharpImports, domain: Domain): String = {
    s"using _${m.typename} = ${DomainCSharpType(m.typeId).renderType(true)};"
  }

  private def renderAdtMember(adtName: String, member: AdtMember)(implicit im: CSharpImports, domain: Domain): String = {
    val needsFQN = im.imports.find(i => i.id == member.typeId)
    val nonambName =
      if (needsFQN.isDefined && needsFQN.get.usingName == "")
        DomainCSharpType(member.typeId).renderType(true)
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
