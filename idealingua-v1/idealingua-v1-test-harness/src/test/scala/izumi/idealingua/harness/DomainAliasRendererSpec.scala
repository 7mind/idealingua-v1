package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AliasId, EnumId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.translator.toscala.{STContext => LegacySTContext}
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M1: byte-parity unit test for
  * `DomainAliasRenderer`.
  *
  * Asserts that the Domain-consuming `DomainAliasRenderer` produces the
  * same scala.meta `Defn` sequence (compared via `.syntax` under both the
  * Scala 2.13 and Scala 3 dialects) as the legacy
  * `ScalaTranslator.renderAlias`, given matching new-IR / legacy-IR
  * inputs.
  *
  * M1 production path (`DomainScalaTranslator.translate`) is unchanged —
  * the new renderer is not yet wired into emission. M2 will flip the
  * production path.
  */
final class DomainAliasRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "alias_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val scalaBuild = ScalaBuildManifest.example
  private val emptyExts: Seq[ScalaTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[ScalaTranslatorExtension, ScalaBuildManifest](IDLLanguage.Scala, emptyExts, scalaBuild)

  // Mirror of legacy `ScalaTranslator.renderAlias` (protected). The
  // mirror is verified by inspection against
  // `idealingua-v1-transpilers/.../toscala/ScalaTranslator.scala:105-107`.
  private def legacyRender(i: LegacyTypeDef.Alias, ctx: LegacySTContext): Seq[scala.meta.Defn] = {
    import scala.meta.*
    Seq(q"type ${ctx.conv.toScala(i.id).typeName} = ${ctx.conv.toScala(i.target).typeFull}")
  }

  // Pattern-matched against legacy `ModuleTools.toSource` (line 20-21):
  // an `if` over a known/scala-version-string yields a value with an inferred
  // type that has `apply(Tree)` available (scala.meta API). The implicit
  // conversion enabling `Dialect#apply(Tree)` is exposed by `scala.meta.*`.
  private def renderSyntax(tree: scala.meta.Tree, isScala3: Boolean): String = {
    import scala.meta.*
    val dialect = if (isScala3) scala.meta.dialects.Scala30 else scala.meta.dialects.Scala213
    dialect(tree).syntax
  }

  private def assertByteEqual(expected: Seq[scala.meta.Defn], actual: Seq[scala.meta.Defn], label: String): Unit = {
    val expSyntax213 = expected.map(t => renderSyntax(t, isScala3 = false)).mkString("\n")
    val actSyntax213 = actual.map(t => renderSyntax(t, isScala3 = false)).mkString("\n")
    val expSyntax3   = expected.map(t => renderSyntax(t, isScala3 = true)).mkString("\n")
    val actSyntax3   = actual.map(t => renderSyntax(t, isScala3 = true)).mkString("\n")
    val _ = assert(expSyntax213 == actSyntax213, s"$label: Scala213 syntax diverges\nlegacy=$expSyntax213\nnew   =$actSyntax213")
    val _ = assert(expSyntax3 == actSyntax3, s"$label: Scala30 syntax diverges\nlegacy=$expSyntax3\nnew   =$actSyntax3")
  }

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  // We don't need a fully-populated `Domain`; the alias renderer reads
  // only `ctx.conv` (a `ScalaTypeConverter(domainId)`). We pass a
  // structurally-minimal `Domain` so that `DomainSTContext` constructs
  // cleanly and the renderer exercises the real production code path.
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
      override def id: DomainId                                         = domainId
      override def imports: Seq[RawImport]                              = Seq.empty
      override def members: Seq[RawTopLevelDefn]                        = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved]        = Map.empty
      override def origin: FSPath                                       = FSPath(domainId.toPackage :+ s"${domainId.id}.domain")
      override def directInclusions: Seq[RawInclusion]                  = Seq.empty
      override def meta: RawNodeMeta                                    = rawMeta
    }
    new DomainSTContext(newDomain, parsedStub, options)
  }

  // The legacy STContext requires a Typespace; we synthesize an empty
  // DomainDefinition so the constructor succeeds. `ctx.conv` only reads
  // `domain.id`, which makes the legacy/new converters interchangeable
  // for the alias rendering path.
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

  test("alias to primitive: byte-equal to legacy") {
    val ctxNew    = newCtxFor(domainId)
    val ctxLegacy = legacyCtxFor(domainId)

    val aliasId     = AliasId(typePath, "MyStrAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, Primitive.TString, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, Primitive.TString, emptyMeta)

    val newDefns    = ctxNew.aliasRenderer.renderAlias(newAlias)
    val legacyDefns = legacyRender(legacyAlias, ctxLegacy)

    assertByteEqual(legacyDefns, newDefns, "alias-primitive")
  }

  test("alias to alias (same domain): byte-equal to legacy") {
    val ctxNew    = newCtxFor(domainId)
    val ctxLegacy = legacyCtxFor(domainId)

    val targetId    = AliasId(typePath, "Base")
    val aliasId     = AliasId(typePath, "DerivedAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, targetId, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, targetId, emptyMeta)

    val newDefns    = ctxNew.aliasRenderer.renderAlias(newAlias)
    val legacyDefns = legacyRender(legacyAlias, ctxLegacy)

    assertByteEqual(legacyDefns, newDefns, "alias-to-alias")
  }

  test("alias to enum (same domain): byte-equal to legacy") {
    val ctxNew    = newCtxFor(domainId)
    val ctxLegacy = legacyCtxFor(domainId)

    val enumId      = EnumId(typePath, "Color")
    val aliasId     = AliasId(typePath, "ColorAlias")
    val newAlias    = NewTypeDef.Alias(aliasId, enumId, emptyMeta)
    val legacyAlias = LegacyTypeDef.Alias(aliasId, enumId, emptyMeta)

    val newDefns    = ctxNew.aliasRenderer.renderAlias(newAlias)
    val legacyDefns = legacyRender(legacyAlias, ctxLegacy)

    assertByteEqual(legacyDefns, newDefns, "alias-to-enum")
  }
}
