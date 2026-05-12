package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AdtId, DTOId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{AdtMember, DomainMetadata, Field, NodeMeta, Super}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M4: structural-correctness unit test for
  * `DomainAdtRenderer`.
  *
  * Relaxed parity bar: the renderer must produce compilable Scala (parseable
  * by scala.meta under both 2.13 and 3.x dialects) whose top-level shape is
  * a `sealed trait` + companion + per-branch `final case class`. Branch
  * names and target field type come straight off `AdtMember`.
  */
final class DomainAdtRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "adt_render_spec")
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

  private def newCtxFor(extras: Map[izumi.idealingua.model.common.TypeId, NewTypeDef], flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct]): DomainSTContext = {
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
      userTypes         = extras,
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

  test("ADT with two DTO branches renders sealed trait + branch case classes") {
    val branchADtoId = DTOId(typePath, "A")
    val branchBDtoId = DTOId(typePath, "B")
    val branchAField = Field(Primitive.TString, "a", emptyMeta)
    val branchBField = Field(Primitive.TInt32, "b", emptyMeta)
    val adtId    = AdtId(typePath, "Choice")
    val members  = List(
      AdtMember(branchADtoId, None, emptyMeta),
      AdtMember(branchBDtoId, None, emptyMeta),
    )
    val adt = NewTypeDef.Adt(adtId, members, emptyMeta)

    // Provide flat structs for the branch DTOs so any nested lookup doesn't NPE.
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      branchADtoId -> FlatStruct(branchADtoId, List(FlatField(branchAField, branchADtoId, 0)), List.empty, List.empty),
      branchBDtoId -> FlatStruct(branchBDtoId, List(FlatField(branchBField, branchBDtoId, 0)), List.empty, List.empty),
    )
    // Also stub the branch DTO TypeDefs in userTypes for completeness.
    val extras = Map[izumi.idealingua.model.common.TypeId, NewTypeDef](
      branchADtoId -> NewTypeDef.Dto(branchADtoId, Struct(List(branchAField), List.empty, Super.empty), emptyMeta),
      branchBDtoId -> NewTypeDef.Dto(branchBDtoId, Struct(List(branchBField), List.empty, Super.empty), emptyMeta),
      adtId        -> adt,
    )

    val ctx     = newCtxFor(extras, flats)
    val product = ctx.adtRenderer.renderAdt(adt)
    val defs    = product.render
    assert(defs.nonEmpty, "ADT product render must be non-empty")

    val s213 = defs.map(renderSyntax(_, isScala3 = false)).mkString("\n")
    val s30  = defs.map(renderSyntax(_, isScala3 = true)).mkString("\n")
    assert(s213.contains("sealed trait Choice"), s"expected sealed trait Choice: $s213")
    assert(s213.contains("final case class A(value:"), s"expected branch A: $s213")
    assert(s213.contains("final case class B(value:"), s"expected branch B: $s213")
    assert(s213.contains("object Choice"), s"expected companion: $s213")
    assert(s30.contains("sealed trait Choice"))
  }

  test("single-branch ADT renders without throwing") {
    val branchId = DTOId(typePath, "Solo")
    val adtId    = AdtId(typePath, "Mono")
    val members  = List(AdtMember(branchId, None, emptyMeta))
    val adt      = NewTypeDef.Adt(adtId, members, emptyMeta)

    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      branchId -> FlatStruct(branchId, List.empty, List.empty, List.empty)
    )
    val extras = Map[izumi.idealingua.model.common.TypeId, NewTypeDef](
      branchId -> NewTypeDef.Dto(branchId, Struct(List.empty, List.empty, Super.empty), emptyMeta),
      adtId    -> adt,
    )

    val ctx     = newCtxFor(extras, flats)
    val product = ctx.adtRenderer.renderAdt(adt)
    val defs    = product.render
    assert(defs.nonEmpty)
    val s213 = defs.map(renderSyntax(_, isScala3 = false)).mkString("\n")
    assert(s213.contains("sealed trait Mono"))
    assert(s213.contains("final case class Solo"))
  }
}
