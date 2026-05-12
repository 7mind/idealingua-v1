package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.DTOId
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
import izumi.idealingua.translator.toscala.domain.extensions.DomainCastSimilarExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M5: unit test for `DomainCastSimilarExtension`.
  *
  * Two DTOs with identical signatures produce mutual cast helpers; mismatched
  * signatures produce none. Result list ordering is deterministic.
  */
final class DomainCastSimilarExtensionSpec extends AnyFunSuite {
  private val domainId  = DomainId(Seq("idltest"), "cast_similar_spec")
  private val tp        = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val rawMeta   = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val options   = CompilerOptions[ScalaBuildManifest](IDLLanguage.Scala, ScalaBuildManifest.example)

  private def metaFor(d: DomainId) =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def ctxFor(extras: Map[izumi.idealingua.model.common.TypeId, NewTypeDef], flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct]): DomainSTContext = {
    val dom = Domain(
      id = domainId, meta = metaFor(domainId), members = Map.empty, roots = Set.empty,
      ephemeralsOf = Map.empty, ephemeralOwner = Map.empty, flattenedStructs = flats,
      parents = Map.empty, implementingDtos = Map.empty, loops = Set.empty,
      fingerprints = Map.empty, domainFingerprint = Fingerprint(ByteVector.empty),
      imports = Map.empty, consts = List.empty, aliases = Map.empty, userTypes = extras,
    )
    val parsed: DomainMeshResolved = new DomainMeshResolved {
      override def id: DomainId = domainId
      override def imports: Seq[RawImport] = Seq.empty
      override def members: Seq[RawTopLevelDefn] = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath = FSPath(domainId.toPackage :+ s"${domainId.id}.domain")
      override def directInclusions: Seq[RawInclusion] = Seq.empty
      override def meta: RawNodeMeta = rawMeta
    }
    new DomainSTContext(dom, parsed, options)
  }

  test("two identical DTOs yield cross-cast helpers (parseable scala.meta)") {
    val a = DTOId(tp, "A")
    val b = DTOId(tp, "B")
    val f = Field(Primitive.TString, "x", emptyMeta)
    val aTD = NewTypeDef.Dto(a, Struct(List(f), List.empty, Super.empty), emptyMeta)
    val bTD = NewTypeDef.Dto(b, Struct(List(f), List.empty, Super.empty), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      a -> FlatStruct(a, List(FlatField(f, a, 0)), List.empty, List.empty),
      b -> FlatStruct(b, List(FlatField(f, b, 0)), List.empty, List.empty),
    )
    val ctx = ctxFor(Map(a -> aTD, b -> bTD), flats)

    val convertersFromA = DomainCastSimilarExtension.mkConvertersForDto(ctx, aTD)
    assert(convertersFromA.size == 1, s"expected 1 cast helper from A→B, got ${convertersFromA.size}")

    val syntax = {
      import scala.meta.*
      scala.meta.dialects.Scala213(convertersFromA.head).syntax
    }
    assert(syntax.contains("A_cast_into_"), s"expected cast helper name pattern: $syntax")
  }

  test("DTOs with mismatched signatures yield no cast helpers") {
    val a = DTOId(tp, "A")
    val b = DTOId(tp, "B")
    val fa = Field(Primitive.TString, "x", emptyMeta)
    val fb = Field(Primitive.TInt32, "y", emptyMeta)
    val aTD = NewTypeDef.Dto(a, Struct(List(fa), List.empty, Super.empty), emptyMeta)
    val bTD = NewTypeDef.Dto(b, Struct(List(fb), List.empty, Super.empty), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      a -> FlatStruct(a, List(FlatField(fa, a, 0)), List.empty, List.empty),
      b -> FlatStruct(b, List(FlatField(fb, b, 0)), List.empty, List.empty),
    )
    val ctx = ctxFor(Map(a -> aTD, b -> bTD), flats)

    assert(DomainCastSimilarExtension.mkConvertersForDto(ctx, aTD).isEmpty)
  }
}
