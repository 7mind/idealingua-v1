package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{DTOId, ServiceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.DefMethod.{Output, RPCMethod, Signature}
import izumi.idealingua.model.il.ast.typed.{DomainMetadata, Field, NodeMeta, SimpleStructure}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M4: structural-correctness unit test for
  * `DomainServiceRenderer`.
  *
  * Relaxed parity bar: the renderer must emit a CogenServiceProduct whose
  * `.render` yields six top-level Defns, all parseable by scala.meta under
  * the Scala 2.13 dialect, with the expected canonical names (Server /
  * Client / WrappedServer / WrappedClient / Methods / Codecs).
  *
  * Fixture: a minimal Service with one Singular-output method `ping(x: i32)
  * → str`. The input ephemeral DTO is pre-materialized in
  * `Domain.flattenedStructs` (Phase 7 work the typer would otherwise do).
  */
final class DomainServiceRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "service_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val scalaBuild = ScalaBuildManifest.example
  private val emptyExts: Seq[ScalaTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[ScalaTranslatorExtension, ScalaBuildManifest](IDLLanguage.Scala, emptyExts, scalaBuild)

  private def renderSyntax(tree: scala.meta.Tree): String = {
    import scala.meta.*
    scala.meta.dialects.Scala213(tree).syntax
  }

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtx(flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct]): DomainSTContext = {
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = flats,
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

  test("service with single Singular method emits 6 top-level defns") {
    val svcId      = ServiceId(domainId, "Ping")
    val svcPath    = TypePath(domainId, Seq("Ping"))
    val xField     = Field(Primitive.TInt32, "x", emptyMeta)
    val method = RPCMethod(
      name      = "ping",
      signature = Signature(
        input  = SimpleStructure(concepts = List.empty, fields = List(xField)),
        output = Output.Singular(Primitive.TString),
      ),
      meta = emptyMeta,
    )
    val svc        = NewTypeDef.Service(svcId, List(method), emptyMeta)

    // Pre-materialize the input + output ephemeral DTOs (what
    // EphemeralSynthesizer would emit).
    val inputDtoId  = DTOId(svcPath, "PingInput")
    val outputDtoId = DTOId(svcPath, "PingOutput")
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      inputDtoId  -> FlatStruct(inputDtoId, List(FlatField(xField, inputDtoId, 0)), List.empty, List.empty),
      outputDtoId -> FlatStruct(outputDtoId, List(FlatField(Field(Primitive.TString, "value", emptyMeta), outputDtoId, 0)), List.empty, List.empty),
    )
    val ctx     = newCtx(flats)
    val product = ctx.serviceRenderer.renderService(svc)
    val defs    = product.render
    assert(defs.size == 8, s"expected 8 defns (server/client traits + 2 wrapped pairs (class+companion each) + methods + codecs), got ${defs.size}")

    // Parse every Defn under Scala 2.13 to verify it is structurally valid.
    val rendered = defs.map(renderSyntax)
    rendered.foreach { s =>
      assert(s.nonEmpty, "every defn renders to non-empty syntax")
    }
    val joined = rendered.mkString("\n\n")
    assert(joined.contains("trait PingServer"), s"expected PingServer trait: $joined")
    assert(joined.contains("trait PingClient"), s"expected PingClient trait: $joined")
    assert(joined.contains("class PingWrappedServer"), s"expected PingWrappedServer: $joined")
    assert(joined.contains("class PingWrappedClient"), s"expected PingWrappedClient: $joined")
    assert(joined.contains("object Ping"), s"expected Ping methods object: $joined")
    assert(joined.contains("object PingCodecs"), s"expected PingCodecs: $joined")
  }

  test("buzzer reuses service renderer body") {
    val bzId = izumi.idealingua.model.common.TypeId.BuzzerId(domainId, "Notif")
    val bzPath = TypePath(domainId, Seq("Notif"))
    val payloadField = Field(Primitive.TString, "payload", emptyMeta)
    val event = RPCMethod(
      name      = "fire",
      signature = Signature(
        input  = SimpleStructure(concepts = List.empty, fields = List(payloadField)),
        output = Output.Void(),
      ),
      meta = emptyMeta,
    )
    val bz = NewTypeDef.Buzzer(bzId, List(event), emptyMeta)

    val inputDtoId  = DTOId(bzPath, "FireInput")
    val outputDtoId = DTOId(bzPath, "FireOutput")
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      inputDtoId  -> FlatStruct(inputDtoId, List(FlatField(payloadField, inputDtoId, 0)), List.empty, List.empty),
      outputDtoId -> FlatStruct(outputDtoId, List.empty, List.empty, List.empty),
    )
    val ctx     = newCtx(flats)
    val product = ctx.serviceRenderer.renderBuzzer(bz)
    val defs    = product.render
    assert(defs.size == 8, s"buzzer should yield 8 defns, got ${defs.size}")
    val joined = defs.map(renderSyntax).mkString("\n\n")
    assert(joined.contains("trait NotifServer"), s"expected NotifServer: $joined")
    assert(joined.contains("trait NotifClient"))
  }
}
