package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AliasId, EnumId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.AliasProduct
import izumi.idealingua.translator.tocsharp.types.CSharpType
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M1: byte-parity unit test for
  * `DomainCSAliasRenderer`.
  *
  * Asserts that the Domain-consuming `DomainCSAliasRenderer` produces the
  * same `AliasProduct.content` string as the legacy
  * `CSharpTranslator.renderAlias` (mirror, since the legacy method is
  * `protected`), given matching new-IR / legacy-IR inputs.
  *
  * M1 production path (`DomainCSharpTranslator.translate`) is
  * unchanged — Phase A delegation to the legacy `CSharpTranslator` still
  * runs. The new renderer is not yet wired into emission. M2+ will
  * replace per-type rendering one family at a time, finally flipping the
  * production path once all families are ported.
  */
final class DomainCSAliasRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_alias_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // Mirror of legacy `CSharpTranslator.renderAlias` (protected). The mirror
  // is verified by inspection against
  // `idealingua-v1-transpilers/.../tocsharp/CSharpTranslator.scala:91-105`.
  private def legacyRender(i: LegacyTypeDef.Alias)(implicit im: CSharpImports, ts: Typespace): AliasProduct = {
    val cstype = CSharpType(i.target)
    AliasProduct(
      s"""// C# does not natively support full type aliases. They usually
         |// live only within the current file scope, making it impossible
         |// to make them type aliases within another namespace.
         |//
         |// Had it been fully supported, the code would be something like:
         |// using ${i.id.name} = ${cstype.renderType(true)}
         |//
         |// For the time being, please use the target type everywhere you need.
         """.stripMargin
    )
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

  private def legacyTypespaceFor(domainId: DomainId): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq.empty,
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def assertProductEqual(label: String, expected: AliasProduct, actual: AliasProduct): Unit = {
    val _ = assert(expected.content == actual.content, s"$label: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
  }

  test("alias to primitive: byte-equal to legacy") {
    val ctxNew = newCtxFor(domainId)
    implicit val ts: Typespace      = legacyTypespaceFor(domainId)
    implicit val im: CSharpImports  = CSharpImports(List.empty)

    val aliasId     = AliasId(typePath, "MyStrAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, Primitive.TString, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, Primitive.TString, emptyMeta)

    val actual   = ctxNew.aliasRenderer.renderAlias(newAlias, ts, im)
    val expected = legacyRender(legacyAlias)

    assertProductEqual("alias-primitive", expected, actual)
  }

  test("alias to enum (same domain): byte-equal to legacy") {
    val ctxNew = newCtxFor(domainId)
    implicit val ts: Typespace      = legacyTypespaceFor(domainId)
    implicit val im: CSharpImports  = CSharpImports(List.empty)

    val enumId      = EnumId(typePath, "Color")
    val aliasId     = AliasId(typePath, "ColorAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, enumId, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, enumId, emptyMeta)

    val actual   = ctxNew.aliasRenderer.renderAlias(newAlias, ts, im)
    val expected = legacyRender(legacyAlias)

    assertProductEqual("alias-to-enum", expected, actual)
  }
}
