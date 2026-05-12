package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.EnumId
import izumi.idealingua.model.common.{DomainId, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainMetadata, EnumMember, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M1: byte-parity unit test for
  * `DomainTSEnumRenderer`.
  *
  * Asserts that the Domain-consuming `DomainTSEnumRenderer` produces the
  * same pre-extension `EnumProduct` as the legacy
  * `TypeScriptTranslator.renderEnumeration` (mirror, since the legacy
  * method is `protected`), given matching new-IR / legacy-IR inputs and
  * an empty extension list.
  *
  * Extension integration (`EnumHelpersExtension.handleEnum` is the only
  * handler that touches enums in the default TS extension set) is M2+
  * scope. M1 production path is unchanged.
  */
final class DomainTSEnumRendererSpec extends AnyFunSuite {

  private val domainId  = DomainId(Seq("idltest"), "ts_enum_render_spec")
  private val typePath  = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val rawMeta   = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)

  // Mirror of legacy `TypeScriptTranslator.renderEnumeration` (protected).
  // The mirror is verified by inspection against
  // `idealingua-v1-transpilers/.../totypescript/TypeScriptTranslator.scala:450-464`.
  private def legacyRender(i: LegacyTypeDef.Enumeration): EnumProduct = {
    val it = i.members.map(_.value).iterator
    val members = it.map {
      m =>
        s"$m = '$m'" + (if (it.hasNext) "," else "")
    }.mkString("\n")

    val content =
      s"""export enum ${i.id.name} {
         |${members.shift(4)}
         |}
       """.stripMargin

    EnumProduct(content, s"// ${i.id.name} Enumeration")
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

  private def assertProductEqual(label: String, expected: EnumProduct, actual: EnumProduct): Unit = {
    val _ = assert(expected.content == actual.content, s"$label: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges\nlegacy=${expected.preamble}\nnew   =${actual.preamble}")
  }

  test("single-member enum: byte-equal to legacy") {
    val ctxNew  = newCtxFor(domainId)
    val enumId  = EnumId(typePath, "Mono")
    val members = List(EnumMember("ONLY", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val actual   = ctxNew.enumRenderer.renderEnumeration(newEnum)
    val expected = legacyRender(legacyEnum)

    assertProductEqual("mono-enum", expected, actual)
  }

  test("multi-member enum: byte-equal to legacy and order-preserving") {
    val ctxNew  = newCtxFor(domainId)
    val enumId  = EnumId(typePath, "Color")
    val members = List(EnumMember("RED", emptyMeta), EnumMember("GREEN", emptyMeta), EnumMember("BLUE", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val actual   = ctxNew.enumRenderer.renderEnumeration(newEnum)
    val expected = legacyRender(legacyEnum)

    assertProductEqual("rgb-enum", expected, actual)
  }
}
