package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.EnumId
import izumi.idealingua.model.common.{DomainId, TypePath}
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, EnumMember, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.EnumRenderer
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.translator.toscala.products.CogenProduct.EnumProduct
import izumi.idealingua.translator.toscala.{STContext => LegacySTContext}
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M1: byte-parity unit test for
  * `DomainEnumRenderer`.
  *
  * Asserts that the Domain-consuming `DomainEnumRenderer` produces the
  * same pre-extension `EnumProduct` as the legacy
  * `EnumRenderer.renderEnumeration`, given matching new-IR / legacy-IR
  * inputs and an empty extension list.
  *
  * Extension integration (`CirceDerivationTranslatorExtension.handleEnum`
  * is the only handler that touches enums in the default extension set)
  * is M2's scope. M1 production path is unchanged.
  */
final class DomainEnumRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "enum_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val scalaBuild = ScalaBuildManifest.example
  private val emptyExts: Seq[ScalaTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[ScalaTranslatorExtension, ScalaBuildManifest](IDLLanguage.Scala, emptyExts, scalaBuild)

  // Pattern-matched against legacy `ModuleTools.toSource` (line 20-21):
  // an `if` over a known/scala-version-string yields a value with an inferred
  // type that has `apply(Tree)` available (scala.meta API). The implicit
  // conversion enabling `Dialect#apply(Tree)` is exposed by `scala.meta.*`.
  private def renderSyntax(tree: scala.meta.Tree, isScala3: Boolean): String = {
    import scala.meta.*
    val dialect = if (isScala3) scala.meta.dialects.Scala30 else scala.meta.dialects.Scala213
    dialect(tree).syntax
  }

  private def assertSyntaxEqual(label: String, left: scala.meta.Tree, right: scala.meta.Tree): Unit = {
    val l213 = renderSyntax(left, isScala3 = false)
    val r213 = renderSyntax(right, isScala3 = false)
    val l3   = renderSyntax(left, isScala3 = true)
    val r3   = renderSyntax(right, isScala3 = true)
    val _ = assert(l213 == r213, s"$label: Scala213 syntax diverges\nlegacy=$l213\nnew   =$r213")
    val _ = assert(l3 == r3, s"$label: Scala30 syntax diverges\nlegacy=$l3\nnew   =$r3")
  }

  private def assertEnumProductEqual(label: String, expected: EnumProduct, actual: EnumProduct): Unit = {
    assertSyntaxEqual(s"$label.defn", expected.defn, actual.defn)
    assertSyntaxEqual(s"$label.companionBase", expected.companionBase, actual.companionBase)
    val _ = assert(expected.elements.size == actual.elements.size, s"$label: element count diverges (${expected.elements.size} vs ${actual.elements.size})")
    expected.elements.zip(actual.elements).zipWithIndex.foreach {
      case (((eName, eDefn), (aName, aDefn)), idx) =>
        val _ = assert(eName.value == aName.value, s"$label.elements[$idx].name diverges (${eName.value} vs ${aName.value})")
        assertSyntaxEqual(s"$label.elements[$idx].defn", eDefn, aDefn)
    }
  }

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(domainId: DomainId): DomainSTContext = {
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
    new DomainSTContext(newDomain, parsedStub, options)
  }

  private def legacyCtxFor(domainId: DomainId): LegacySTContext = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq.empty,
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    val ts = new TypespaceImpl(legacyDomain)
    new LegacySTContext(ts, emptyExts, scalaBuild.sbt)
  }

  test("single-member enum: byte-equal to legacy") {
    val ctxNew    = newCtxFor(domainId)
    val ctxLegacy = legacyCtxFor(domainId)
    val enumId    = EnumId(typePath, "Mono")
    val members   = List(EnumMember("ONLY", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val actual   = ctxNew.enumRenderer.renderEnumeration(newEnum)
    val expected = new EnumRenderer(ctxLegacy).renderEnumeration(legacyEnum).asInstanceOf[EnumProduct]

    assertEnumProductEqual("mono-enum", expected, actual)
  }

  test("multi-member enum: byte-equal to legacy and order-preserving") {
    val ctxNew    = newCtxFor(domainId)
    val ctxLegacy = legacyCtxFor(domainId)
    val enumId    = EnumId(typePath, "Color")
    val members   = List(EnumMember("RED", emptyMeta), EnumMember("GREEN", emptyMeta), EnumMember("BLUE", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val actual   = ctxNew.enumRenderer.renderEnumeration(newEnum)
    val expected = new EnumRenderer(ctxLegacy).renderEnumeration(legacyEnum).asInstanceOf[EnumProduct]

    assertEnumProductEqual("rgb-enum", expected, actual)
    val _ = assert(actual.elements.map(_._1.value) == List("RED", "GREEN", "BLUE"), "element order")
  }
}
