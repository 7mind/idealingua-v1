package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{DTOId, IdentifierId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainMetadata, Field, IdField, NodeMeta, Super}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.domain.extensions.DomainAnyvalExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M5: unit test for `DomainAnyvalExtension`.
  *
  * Verifies the AnyVal / Any decision matches legacy heuristics:
  *   - Identifier with a single field → AnyVal.
  *   - Identifier with two fields    → no extra base.
  *   - DTO with single scalar field  → AnyVal.
  *   - DTO with two fields           → no extra base.
  *   - Empty interface               → Any.
  */
final class DomainAnyvalExtensionSpec extends AnyFunSuite {
  private val domainId  = DomainId(Seq("idltest"), "anyval_spec")
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

  test("identifier with single field gets AnyVal init") {
    val id = IdentifierId(tp, "UserId")
    val td = NewTypeDef.Identifier(id, List(IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)), emptyMeta)
    val ctx = ctxFor(Map(id -> td), Map.empty)
    assert(DomainAnyvalExtension.withAnyvalForIdentifier(ctx, td).size == 1)
  }

  test("identifier with two fields gets no AnyVal init") {
    val id = IdentifierId(tp, "PairId")
    val td = NewTypeDef.Identifier(id,
      List(
        IdField.PrimitiveField(Primitive.TString, "a", emptyMeta),
        IdField.PrimitiveField(Primitive.TInt32, "b", emptyMeta),
      ), emptyMeta)
    val ctx = ctxFor(Map(id -> td), Map.empty)
    assert(DomainAnyvalExtension.withAnyvalForIdentifier(ctx, td).isEmpty)
  }

  test("single-scalar DTO gets AnyVal init") {
    val id = DTOId(tp, "ScalarDto")
    val field = Field(Primitive.TString, "v", emptyMeta)
    val td = NewTypeDef.Dto(id, Struct(List(field), List.empty, Super.empty), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      id -> FlatStruct(id, List(FlatField(field, id, 0)), List.empty, List.empty)
    )
    val ctx = ctxFor(Map(id -> td), flats)
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, td).size == 1)
    assert(DomainAnyvalExtension.structCanBeAnyVal(ctx, td))
  }

  test("two-field DTO gets no AnyVal init") {
    val id = DTOId(tp, "PairDto")
    val fa = Field(Primitive.TString, "a", emptyMeta)
    val fb = Field(Primitive.TInt32, "b", emptyMeta)
    val td = NewTypeDef.Dto(id, Struct(List(fa, fb), List.empty, Super.empty), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      id -> FlatStruct(id, List(FlatField(fa, id, 0), FlatField(fb, id, 0)), List.empty, List.empty)
    )
    val ctx = ctxFor(Map(id -> td), flats)
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, td).isEmpty)
    assert(!DomainAnyvalExtension.structCanBeAnyVal(ctx, td))
  }

  test("empty interface gets Any init") {
    val id = InterfaceId(tp, "Marker")
    val td = NewTypeDef.Interface(id, Struct(List.empty, List.empty, Super.empty), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      id -> FlatStruct(id, List.empty, List.empty, List.empty)
    )
    val ctx = ctxFor(Map(id -> td), flats)
    assert(DomainAnyvalExtension.withAnyForInterface(ctx, td).size == 1)
  }
}
