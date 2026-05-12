package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.EnumId
import izumi.idealingua.model.common.{DomainId, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainMetadata, EnumMember, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M1: byte-parity unit test for
  * `DomainCSEnumRenderer`.
  *
  * Asserts that the Domain-consuming `DomainCSEnumRenderer` produces the
  * same pre-extension `EnumProduct.content` as the legacy
  * `CSharpTranslator.renderEnumeration` (mirror, since the legacy
  * method is `protected`), given matching new-IR / legacy-IR inputs.
  *
  * The legacy renderer interpolates `${ext.postModelEmit(ctx, i)}` at
  * the tail of the body and threads `ext.imports(ctx, i)` into the
  * header import list. The default C# extension set
  * (`JsonNetExtension`) has no `postModelEmit` for `Enumeration` and
  * no `imports` for `Enumeration` — both return empty with the default
  * extension list, so the pre-extension product is byte-equal to the
  * post-extension product for the default configuration. M2+ will
  * reintegrate the extension chain.
  *
  * The header is `im.renderImports(List("System"))` which is owned by
  * the production translator (per-definition `CSharpImports` instance)
  * and is asserted at the test boundary — not produced by the
  * renderer itself.
  */
final class DomainCSEnumRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_enum_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // Mirror of legacy `CSharpTranslator.renderEnumeration` (protected). The
  // mirror is verified by inspection against
  // `idealingua-v1-transpilers/.../tocsharp/CSharpTranslator.scala:253-300`.
  // Computes only the `content` field — `header` is produced from
  // `im.renderImports(List("System") ++ ext.imports(ctx, i))` at the
  // translator level (owned by the production translator's CSharpImports
  // instance, not the renderer), and is exercised by the existing
  // legacy harness paths; M1 asserts content byte-parity.
  private def legacyRenderContent(i: LegacyTypeDef.Enumeration): String = {
    val name = i.id.name
    s"""// $name Enumeration
       |public enum $name {
       |${i.members.map(_.value).map(m => s"$m${if (m == i.members.last.value) "" else ","}").mkString("\n").shift(4)}
       |}
       |
       |public static class ${name}Helpers {
       |    public static $name From(string value) {
       |        switch (value) {
       |${i.members.map(_.value).map(m => s"""case \"$m\": return $name.$m;""").mkString("\n").shift(12)}
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
       |${i.members.map(_.value).map(m => s"$name.$m${if (m == i.members.last.value) "" else ","}").mkString("\n").shift(8)}
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
       |
       |""".stripMargin
  }

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(domainId: DomainId): DomainCSContext = {
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
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
      userTypes         = Map.empty,
    )
    val parsedStub: DomainMeshResolved = new DomainMeshResolved {
      override def id: DomainId                                  = domainId
      override def imports: Seq[RawImport]                       = Seq.empty
      override def members: Seq[RawTopLevelDefn]                 = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath                                = FSPath(domainId.toPackage :+ s"${domainId.id}.domain")
      override def directInclusions: Seq[RawInclusion]           = Seq.empty
      override def meta: RawNodeMeta                             = rawMeta
    }
    new DomainCSContext(newDomain, parsedStub, options)
  }

  private def assertContentEqual(label: String, expected: String, actual: EnumProduct): Unit = {
    val _ = assert(expected == actual.content, s"$label: content diverges\nlegacy=$expected\nnew   =${actual.content}")
  }

  test("single-member enum: byte-equal to legacy") {
    val ctxNew  = newCtxFor(domainId)
    val enumId  = EnumId(typePath, "Mono")
    val members = List(EnumMember("ONLY", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val actual   = ctxNew.enumRenderer.renderEnumeration(newEnum)
    val expected = legacyRenderContent(legacyEnum)

    assertContentEqual("mono-enum", expected, actual)
  }

  test("multi-member enum: byte-equal to legacy and order-preserving") {
    val ctxNew  = newCtxFor(domainId)
    val enumId  = EnumId(typePath, "Color")
    val members = List(EnumMember("RED", emptyMeta), EnumMember("GREEN", emptyMeta), EnumMember("BLUE", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val actual   = ctxNew.enumRenderer.renderEnumeration(newEnum)
    val expected = legacyRenderContent(legacyEnum)

    assertContentEqual("rgb-enum", expected, actual)
  }
}
