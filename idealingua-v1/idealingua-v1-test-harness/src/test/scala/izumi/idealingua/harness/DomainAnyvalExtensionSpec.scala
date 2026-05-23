package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AliasId, DTOId, IdentifierId, InterfaceId}
import izumi.idealingua.model.common.StructureId
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

  private def ctxFor(
    extras: Map[izumi.idealingua.model.common.TypeId, NewTypeDef],
    flats: Map[StructureId, FlatStruct],
    crossDomainUserTypes: Map[izumi.idealingua.model.common.TypeId, NewTypeDef] = Map.empty,
    aliases: Map[AliasId, izumi.idealingua.model.common.TypeId] = Map.empty,
    crossDomainFlattenedStructs: Map[StructureId, FlatStruct] = Map.empty,
  ): DomainSTContext = {
    val dom = Domain(
      id = domainId, meta = metaFor(domainId), members = Map.empty, roots = Set.empty,
      ephemeralsOf = Map.empty, ephemeralOwner = Map.empty, flattenedStructs = flats,
      crossDomainFlattenedStructs = crossDomainFlattenedStructs,
      crossDomainUserTypes = crossDomainUserTypes,
      parents = Map.empty, implementingDtos = Map.empty, loops = Set.empty,
      fingerprints = Map.empty, domainFingerprint = Fingerprint(ByteVector.empty),
      imports = Map.empty, consts = List.empty, aliases = aliases, userTypes = extras,
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

  // Regression: DTO mixing in an empty interface and wrapping a single 2-field
  // identifier field must extend AnyVal. The flat struct has one entry (the
  // identifier field); the identifier itself has 2 fields, which makes it
  // AnyVal-carrier-eligible. Legacy parity: `Test01DataAnyVal1` in
  // idltest.anyvals + an identifier-typed scalar field.
  test("DTO with empty mixin and single identifier field gets AnyVal init") {
    val idId   = IdentifierId(tp, "ItemID")
    val idDef  = NewTypeDef.Identifier(idId,
      List(
        IdField.PrimitiveField(Primitive.TUUID, "id", emptyMeta),
        IdField.PrimitiveField(Primitive.TUUID, "app", emptyMeta),
      ), emptyMeta)

    val mixinId = InterfaceId(tp, "NodeVarBase")
    val mixin   = NewTypeDef.Interface(mixinId, Struct(List.empty, List.empty, Super.empty), emptyMeta)

    val dtoId = DTOId(tp, "NodeVarItem")
    val field = Field(idId, "val", emptyMeta)
    val dto   = NewTypeDef.Dto(dtoId, Struct(List(field), List.empty, Super(List(mixinId), List.empty, List.empty)), emptyMeta)

    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      dtoId   -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty),
      mixinId -> FlatStruct(mixinId, List.empty, List.empty, List.empty),
    )
    val ctx = ctxFor(Map(idId -> idDef, mixinId -> mixin, dtoId -> dto), flats)
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, dto).size == 1)
    assert(DomainAnyvalExtension.structCanBeAnyVal(ctx, dto))
  }

  // Regression: same shape, but the referenced identifier lives in a
  // different domain. Legacy parity required `Typespace`-wide lookup for the
  // field-count check; the new IR resolves the cross-domain identifier
  // through `crossDomainUserTypes` populated by `StructuralFlattener`.
  test("DTO with single foreign identifier field gets AnyVal init") {
    val foreignDomain = DomainId(Seq("idltest"), "foreign_ids")
    val foreignPath   = TypePath(foreignDomain, Seq.empty)
    val foreignIdId   = IdentifierId(foreignPath, "ItemID")

    val mixinId = InterfaceId(tp, "NodeVarBase")
    val mixin   = NewTypeDef.Interface(mixinId, Struct(List.empty, List.empty, Super.empty), emptyMeta)

    val dtoId = DTOId(tp, "NodeVarItem")
    val field = Field(foreignIdId, "val", emptyMeta)
    val dto   = NewTypeDef.Dto(dtoId, Struct(List(field), List.empty, Super(List(mixinId), List.empty, List.empty)), emptyMeta)

    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      dtoId   -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty),
      mixinId -> FlatStruct(mixinId, List.empty, List.empty, List.empty),
    )
    val foreignIdDef = NewTypeDef.Identifier(foreignIdId,
      List(
        IdField.PrimitiveField(Primitive.TUUID, "id", emptyMeta),
        IdField.PrimitiveField(Primitive.TUUID, "app", emptyMeta),
      ), emptyMeta)
    val ctx = ctxFor(
      extras               = Map(mixinId -> mixin, dtoId -> dto),
      flats                = flats,
      crossDomainUserTypes = Map(foreignIdId -> foreignIdDef),
    )
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, dto).size == 1)
    assert(DomainAnyvalExtension.structCanBeAnyVal(ctx, dto))
  }

  // Regression: DTO whose single field resolves through a local alias that
  // points at a foreign 2-field Identifier must still extend AnyVal.
  // The alias-dealiasing arm in `canBeAnyValField` must recurse into the
  // cross-domain user-types map.
  test("DTO with alias-to-foreign-Identifier field gets AnyVal init") {
    val foreignDomain = DomainId(Seq("idltest"), "foreign_ids")
    val foreignPath   = TypePath(foreignDomain, Seq.empty)
    val foreignIdId   = IdentifierId(foreignPath, "ItemID")

    val aliasId = AliasId(tp, "A")
    val dtoId   = DTOId(tp, "D")
    val field   = Field(aliasId, "val", emptyMeta)
    val dto     = NewTypeDef.Dto(dtoId, Struct(List(field), List.empty, Super.empty), emptyMeta)

    val flats = Map[StructureId, FlatStruct](
      dtoId -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty)
    )
    val foreignIdDef = NewTypeDef.Identifier(foreignIdId,
      List(
        IdField.PrimitiveField(Primitive.TUUID, "id", emptyMeta),
        IdField.PrimitiveField(Primitive.TUUID, "app", emptyMeta),
      ), emptyMeta)
    val ctx = ctxFor(
      extras               = Map(dtoId -> dto),
      flats                = flats,
      crossDomainUserTypes = Map(foreignIdId -> foreignIdDef),
      aliases              = Map(aliasId -> foreignIdId),
    )
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, dto) == List("AnyVal"))
  }

  // Regression (PR-01-D02): DTO whose single field is a foreign multi-field
  // DTO must extend AnyVal. `canBeAnyValField` consults
  // `crossDomainFlattenedStructs`; `StructuralFlattener` must populate that
  // map via the field-driven harvest (not only via the super-driven harvest).
  test("DTO with single foreign multi-field DTO field gets AnyVal init") {
    val foreignDomain = DomainId(Seq("idltest"), "foreign_ids")
    val foreignPath   = TypePath(foreignDomain, Seq.empty)
    val foreignDtoId  = DTOId(foreignPath, "MultiFieldDto")

    val fa = Field(Primitive.TString, "a", emptyMeta)
    val fb = Field(Primitive.TInt32, "b", emptyMeta)

    val dtoId = DTOId(tp, "D")
    val field = Field(foreignDtoId, "val", emptyMeta)
    val dto   = NewTypeDef.Dto(dtoId, Struct(List(field), List.empty, Super.empty), emptyMeta)

    val flats = Map[StructureId, FlatStruct](
      dtoId -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty)
    )
    val foreignFlat = FlatStruct(foreignDtoId, List(FlatField(fa, foreignDtoId, 0), FlatField(fb, foreignDtoId, 0)), List.empty, List.empty)
    val ctx = ctxFor(
      extras                     = Map(dtoId -> dto),
      flats                      = flats,
      crossDomainFlattenedStructs = Map(foreignDtoId -> foreignFlat),
    )
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, dto) == List("AnyVal"))
  }

  // D01 follow-on: `canBeAnyValField` (now consolidated from three copies into
  // one) must return `true` for a foreign IdentifierId when `crossDomainUserTypes`
  // is populated — covering the former `structFieldQualifiesForAnyVal` call site
  // in `DomainInterfaceRenderer` that was local-only and lacked cross-domain
  // awareness before D01 was fixed.
  test("canBeAnyValField returns true for foreign IdentifierId via crossDomainUserTypes") {
    val foreignDomain = DomainId(Seq("idltest"), "foreign_ids")
    val foreignPath   = TypePath(foreignDomain, Seq.empty)
    val foreignIdId   = IdentifierId(foreignPath, "ItemID")

    val dtoId = DTOId(tp, "ImplStruct")
    val field = Field(foreignIdId, "val", emptyMeta)
    val dto   = NewTypeDef.Dto(dtoId, Struct(List(field), List.empty, Super.empty), emptyMeta)

    val flats = Map[StructureId, FlatStruct](
      dtoId -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty)
    )
    val foreignIdDef = NewTypeDef.Identifier(foreignIdId,
      List(
        IdField.PrimitiveField(Primitive.TUUID, "id", emptyMeta),
        IdField.PrimitiveField(Primitive.TUUID, "app", emptyMeta),
      ), emptyMeta)
    val ctx = ctxFor(
      extras               = Map(dtoId -> dto),
      flats                = flats,
      crossDomainUserTypes = Map(foreignIdId -> foreignIdDef),
    )
    // structCanBeAnyVal is the public entry point that internally calls
    // canBeAnyValField — asserting it returns true pins the cross-domain
    // foreign-Identifier branch of the now-unified predicate.
    assert(DomainAnyvalExtension.structCanBeAnyVal(ctx, dto))
  }

  // Regression (PR-01-D02): same as above but the foreign type is an
  // Interface rather than a DTO.
  test("DTO with single foreign multi-field Interface field gets AnyVal init") {
    val foreignDomain  = DomainId(Seq("idltest"), "foreign_ids")
    val foreignPath    = TypePath(foreignDomain, Seq.empty)
    val foreignIfaceId = InterfaceId(foreignPath, "MultiFieldInterface")

    val fa = Field(Primitive.TString, "a", emptyMeta)
    val fb = Field(Primitive.TInt32, "b", emptyMeta)

    val dtoId = DTOId(tp, "D")
    val field = Field(foreignIfaceId, "val", emptyMeta)
    val dto   = NewTypeDef.Dto(dtoId, Struct(List(field), List.empty, Super.empty), emptyMeta)

    val flats = Map[StructureId, FlatStruct](
      dtoId -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty)
    )
    val foreignFlat = FlatStruct(foreignIfaceId, List(FlatField(fa, foreignIfaceId, 0), FlatField(fb, foreignIfaceId, 0)), List.empty, List.empty)
    val ctx = ctxFor(
      extras                     = Map(dtoId -> dto),
      flats                      = flats,
      crossDomainFlattenedStructs = Map(foreignIfaceId -> foreignFlat),
    )
    assert(DomainAnyvalExtension.withAnyvalForComposite(ctx, dto) == List("AnyVal"))
  }
}
