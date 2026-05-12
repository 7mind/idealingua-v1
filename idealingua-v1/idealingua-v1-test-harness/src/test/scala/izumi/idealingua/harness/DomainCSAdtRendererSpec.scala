package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.{AdtId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{AdtMember, DomainDefinition, DomainMetadata, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AdtProduct
import izumi.idealingua.translator.tocsharp.types.CSharpType
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M3: byte-parity unit test for
  * `DomainCSAdtRenderer`.
  *
  * Asserts the Domain-consuming renderer produces the same pre-extension
  * `AdtProduct` as the legacy `CSharpTranslator.renderAdt` mirror with
  * an empty extension list. Scope at M3: ADT with primitive-typed members
  * only (no interface members — that case enables the `operatorsDummy`
  * branch and uses the same shared body, covered structurally by the
  * branch coverage in `renderAdtMember`).
  */
final class DomainCSAdtRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_adt_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // Mirror of legacy `CSharpTranslator.renderAdt` / `renderAdtImpl` /
  // `renderAdtMember` / `renderAdtUsings`, with `ext.preModelEmit` /
  // `postModelEmit` / `imports` resolved empty. See
  // `tocsharp/CSharpTranslator.scala:107-251`.
  private def legacyRenderAdt(i: LegacyTypeDef.Adt)(implicit im: CSharpImports, ts: Typespace): AdtProduct =
    AdtProduct(legacyRenderAdtImpl(i.id.name, i.alternatives), im.renderImports(List.empty))

  private def legacyRenderAdtUsings(m: AdtMember)(implicit im: CSharpImports, ts: Typespace): String =
    s"using _${m.typename} = ${CSharpType(m.typeId).renderType(true)};"

  private def legacyRenderAdtMember(adtName: String, member: AdtMember)(implicit im: CSharpImports, ts: Typespace): String = {
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

  private def legacyRenderAdtImpl(adtName: String, members: List[AdtMember], renderUsings: Boolean = true)(implicit im: CSharpImports, ts: Typespace): String = {
    s"""${im.renderUsings()}
       |${if (renderUsings) members.map(m => legacyRenderAdtUsings(m)).mkString("\n") else ""}
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
       |${members.map(m => legacyRenderAdtMember(adtName, m)).mkString("\n").shift(4)}
       |}
       |
     """.stripMargin
  }

  private def metaFor(d: DomainId): DomainMetadata =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def newCtxFor(d: DomainId, adtId: AdtId, adt: NewTypeDef.Adt): DomainCSContext = {
    val newDomain = Domain(
      id                = d,
      meta              = metaFor(d),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map.empty,
      parents           = Map.empty,
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map(adtId -> adt),
    )
    val parsedStub: DomainMeshResolved = new DomainMeshResolved {
      override def id: DomainId                                  = d
      override def imports: Seq[RawImport]                       = Seq.empty
      override def members: Seq[RawTopLevelDefn]                 = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath                                = FSPath(d.toPackage :+ s"${d.id}.domain")
      override def directInclusions: Seq[RawInclusion]           = Seq.empty
      override def meta: RawNodeMeta                             = rawMeta
    }
    new DomainCSContext(newDomain, parsedStub, options)
  }

  private def legacyTypespaceFor(d: DomainId, td: LegacyTypeDef): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = d,
      meta       = metaFor(d),
      types      = Seq(td),
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def assertProductEqual(label: String, expected: AdtProduct, actual: AdtProduct): Unit = {
    val _ = assert(expected.content == actual.content, s"$label: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
  }

  test("adt with single primitive member: byte-equal to legacy") {
    val adtId  = AdtId(typePath, "Maybe")
    val member = AdtMember(Primitive.TString, None, emptyMeta)
    val newAdt = NewTypeDef.Adt(adtId, List(member), emptyMeta)
    val legacyAdt = LegacyTypeDef.Adt(adtId, List(member), emptyMeta)

    val ctxNew = newCtxFor(domainId, adtId, newAdt)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyAdt)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.adtRenderer.renderAdt(newAdt, im)
    val expected = legacyRenderAdt(legacyAdt)

    assertProductEqual("adt-single-primitive", expected, actual)
  }

  test("adt with multiple primitive members: byte-equal to legacy") {
    val adtId = AdtId(typePath, "Either2")
    val members = List(
      AdtMember(Primitive.TString, None, emptyMeta),
      AdtMember(Primitive.TInt32, None, emptyMeta),
    )
    val newAdt    = NewTypeDef.Adt(adtId, members, emptyMeta)
    val legacyAdt = LegacyTypeDef.Adt(adtId, members, emptyMeta)

    val ctxNew = newCtxFor(domainId, adtId, newAdt)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyAdt)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.adtRenderer.renderAdt(newAdt, im)
    val expected = legacyRenderAdt(legacyAdt)

    assertProductEqual("adt-multi-primitive", expected, actual)
  }
}
