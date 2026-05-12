package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.InterfaceId
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainMetadata, Field, NodeMeta, Super}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M3: structural-correctness unit test for
  * `DomainInterfaceRenderer`.
  */
final class DomainInterfaceRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "iface_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val scalaBuild = ScalaBuildManifest.example
  private val emptyExts: Seq[ScalaTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[ScalaTranslatorExtension, ScalaBuildManifest](IDLLanguage.Scala, emptyExts, scalaBuild)

  private def renderSyntax(tree: scala.meta.Tree, isScala3: Boolean): String = {
    import scala.meta.*
    val dialect = if (isScala3) scala.meta.dialects.Scala30 else scala.meta.dialects.Scala213
    dialect(tree).syntax
  }

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(domainId: DomainId, ifaceId: InterfaceId, fields: List[Field]): DomainSTContext = {
    val flat = FlatStruct(
      ownerId       = ifaceId,
      fields        = fields.map(f => FlatField(f, ifaceId, 0)),
      conflictsHard = List.empty,
      conflictsSoft = List.empty,
    )
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set(ifaceId: izumi.idealingua.model.common.TypeId),
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map(ifaceId -> flat),
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

  test("Interface with single field renders compilable trait + impl") {
    val ifaceId = InterfaceId(typePath, "Named")
    val fields  = List(Field(Primitive.TString, "name", emptyMeta))
    val ctx     = newCtxFor(domainId, ifaceId, fields)
    val iface = NewTypeDef.Interface(
      id     = ifaceId,
      struct = Struct(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )

    val product = ctx.interfaceRenderer.renderInterface(iface)
    val rendered = product.render
    assert(rendered.nonEmpty, "renderer must produce at least one Defn")

    val s213 = renderSyntax(rendered.head, isScala3 = false)
    val s30  = renderSyntax(rendered.head, isScala3 = true)
    assert(s213.contains("trait Named"), s"expected trait header: $s213")
    assert(s30.contains("trait Named"), s"expected Scala30 trait header: $s30")
  }

  test("Interface with multiple fields surfaces all abstract methods") {
    val ifaceId = InterfaceId(typePath, "Multi")
    val fields = List(
      Field(Primitive.TString, "a", emptyMeta),
      Field(Primitive.TInt32, "b", emptyMeta),
    )
    val ctx = newCtxFor(domainId, ifaceId, fields)
    val iface = NewTypeDef.Interface(
      id     = ifaceId,
      struct = Struct(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )

    val product = ctx.interfaceRenderer.renderInterface(iface)
    val s213 = renderSyntax(product.render.head, isScala3 = false)
    assert(s213.contains("def a") && s213.contains("def b"),
      s"missing abstract methods: $s213")
  }

  test("empty Interface renders without throwing") {
    val ifaceId = InterfaceId(typePath, "Marker")
    val ctx     = newCtxFor(domainId, ifaceId, List.empty)
    val iface = NewTypeDef.Interface(
      id     = ifaceId,
      struct = Struct(fields = List.empty, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )

    val product = ctx.interfaceRenderer.renderInterface(iface)
    assert(product.render.nonEmpty)
    val s213 = renderSyntax(product.render.head, isScala3 = false)
    assert(s213.contains("trait Marker"))
  }
}
