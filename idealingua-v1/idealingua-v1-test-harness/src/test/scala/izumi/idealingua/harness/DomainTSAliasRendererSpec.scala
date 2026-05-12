package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AliasId, EnumId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.AliasProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M1: byte-parity unit test for
  * `DomainTSAliasRenderer`.
  *
  * Asserts that the Domain-consuming `DomainTSAliasRenderer` produces the
  * same `AliasProduct.content` string as the legacy
  * `TypeScriptTranslator.renderAlias` (mirror, since the legacy method is
  * `protected`), given matching new-IR / legacy-IR inputs.
  *
  * M1 production path (`DomainTypeScriptTranslator.translate`) is
  * unchanged — Phase A delegation to the legacy `TypeScriptTranslator`
  * still runs. The new renderer is not yet wired into emission. M2+ will
  * replace per-type rendering one family at a time, finally flipping the
  * production path once all families are ported.
  */
final class DomainTSAliasRendererSpec extends AnyFunSuite {

  private val domainId  = DomainId(Seq("idltest"), "ts_alias_render_spec")
  private val typePath  = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val rawMeta   = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)

  // Mirror of legacy `TypeScriptTranslator.renderAlias` (protected). The
  // mirror is verified by inspection against
  // `idealingua-v1-transpilers/.../totypescript/TypeScriptTranslator.scala:233-258`.
  private def legacyRender(i: LegacyTypeDef.Alias, conv: izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter, ts: izumi.idealingua.model.typespace.Typespace): AliasProduct = {
    AliasProduct(
      s"""// TypeScript does not natively support well type aliases.
         |// Normally the code would be:
         |// export type ${i.id.name} = ${conv.toNativeType(i.target, ts)};
         |//
         |// However, constructors and casting won't work correctly.
         |// Therefore, all aliases usage was just replaced with the target
         |// type and this file is for reference purposes only.
         |// Should the new versions of TypeScript support this better -
         |// it can be enabled back.
         |//
         |// See this and other referenced threads for more information:
         |// https://github.com/Microsoft/TypeScript/issues/2552
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

  private def newCtxFor(domainId: DomainId): DomainTSContext = {
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
    new DomainTSContext(newDomain, parsedStub, options)
  }

  private def legacyTypespaceFor(domainId: DomainId): izumi.idealingua.model.typespace.Typespace = {
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
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges")
  }

  test("alias to primitive: byte-equal to legacy") {
    val ctxNew = newCtxFor(domainId)
    val ts     = legacyTypespaceFor(domainId)

    val aliasId     = AliasId(typePath, "MyStrAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, Primitive.TString, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, Primitive.TString, emptyMeta)

    val actual   = ctxNew.aliasRenderer.renderAlias(newAlias, ts)
    val expected = legacyRender(legacyAlias, ctxNew.conv, ts)

    assertProductEqual("alias-primitive", expected, actual)
  }

  test("alias to enum (same domain): byte-equal to legacy") {
    val ctxNew = newCtxFor(domainId)
    val ts     = legacyTypespaceFor(domainId)

    val enumId      = EnumId(typePath, "Color")
    val aliasId     = AliasId(typePath, "ColorAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, enumId, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, enumId, emptyMeta)

    val actual   = ctxNew.aliasRenderer.renderAlias(newAlias, ts)
    val expected = legacyRender(legacyAlias, ctxNew.conv, ts)

    assertProductEqual("alias-to-enum", expected, actual)
  }
}
