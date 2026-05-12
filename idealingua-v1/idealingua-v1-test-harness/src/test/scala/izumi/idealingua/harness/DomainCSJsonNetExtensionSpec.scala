package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AdtId, DTOId, EnumId, IdentifierId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{
  AdtMember,
  DomainDefinition,
  DomainMetadata,
  EnumMember,
  Field,
  IdField,
  NodeMeta,
  Structure,
  Super,
  TypeDef => LegacyTypeDef,
}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.{CSTContext, CSharpImports}
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension
import izumi.idealingua.translator.tocsharp.extensions.{CSharpTranslatorExtension, JsonNetExtension}
import izumi.idealingua.typer.ir.{Domain, FlatField, FlatStruct, Fingerprint, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M4: byte-parity unit test for
  * `DomainCSJsonNetExtension`.
  *
  * The legacy `JsonNetExtension` is the C# wire-format authority — its
  * generated `JsonConverter` classes determine the cross-language
  * interop contract for the C# leg. M4 ports it to the new-IR
  * `DomainCSJsonNetExtension` and asserts each `pre*` / `post*` /
  * `imports*` entry produces output byte-identical to the legacy emitter
  * for matching inputs.
  *
  * Scope at M4: per-handler smoke tests (DTO / Identifier / Interface /
  * ADT / Enum). Each handler is exercised in isolation; the
  * cross-handler integration (production-path swap that splices these
  * outputs into the renderers' raw-string templates) is M5 scope.
  */
final class DomainCSJsonNetExtensionSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_jsonnet_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  private def metaFor(d: DomainId): DomainMetadata =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def parsedStub(d: DomainId): DomainMeshResolved = new DomainMeshResolved {
    override def id: DomainId                                  = d
    override def imports: Seq[RawImport]                       = Seq.empty
    override def members: Seq[RawTopLevelDefn]                 = Seq.empty
    override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
    override def origin: FSPath                                = FSPath(d.toPackage :+ s"${d.id}.domain")
    override def directInclusions: Seq[RawInclusion]           = Seq.empty
    override def meta: RawNodeMeta                             = rawMeta
  }

  private def emptyDomain(d: DomainId): Domain = Domain(
    id                = d,
    meta              = metaFor(d),
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

  private def legacyTypespaceFor(d: DomainId, tds: LegacyTypeDef*): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = d,
      meta       = metaFor(d),
      types      = tds.toSeq,
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def legacyCtx(ts: Typespace): CSTContext =
    new CSTContext(ts, Seq(JsonNetExtension))

  // ----------------------------------------------------------------------
  // Identifier handlers
  // ----------------------------------------------------------------------
  test("Identifier: pre-attribute + post-converter + imports byte-equal to legacy") {
    val idId    = IdentifierId(typePath, "UserId")
    val fields  = List[IdField](IdField.PrimitiveField(Primitive.TString, "v", emptyMeta))
    val legacy  = LegacyTypeDef.Identifier(idId, fields, emptyMeta)
    val newId   = NewTypeDef.Identifier(idId, fields, emptyMeta)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacy)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPre     = JsonNetExtension.preModelEmit(lctx, legacy)
    val expectedPost    = JsonNetExtension.postModelEmit(lctx, legacy)
    val expectedImports = JsonNetExtension.imports(lctx, legacy).toList

    val actualPre     = DomainCSJsonNetExtension.preIdentifier(newId)
    val actualPost    = DomainCSJsonNetExtension.postIdentifier(newId)
    val actualImports = DomainCSJsonNetExtension.importsIdentifier

    assert(expectedPre == actualPre, s"identifier pre attr diverges\nlegacy=$expectedPre\nnew   =$actualPre")
    assert(expectedPost == actualPost, s"identifier post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
    assert(expectedImports == actualImports, s"identifier imports diverge\nlegacy=$expectedImports\nnew   =$actualImports")
  }

  // ----------------------------------------------------------------------
  // Enum handlers
  // ----------------------------------------------------------------------
  test("Enum: pre-attribute + post-converter + imports byte-equal to legacy") {
    val enumId  = EnumId(typePath, "Color")
    val members = List(EnumMember("RED", emptyMeta), EnumMember("GREEN", emptyMeta), EnumMember("BLUE", emptyMeta))
    val legacy  = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)
    val newE    = NewTypeDef.Enum(enumId, members, emptyMeta)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacy)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPre     = JsonNetExtension.preModelEmit(lctx, legacy)
    val expectedPost    = JsonNetExtension.postModelEmit(lctx, legacy)
    val expectedImports = JsonNetExtension.imports(lctx, legacy).toList

    val actualPre     = DomainCSJsonNetExtension.preEnum(newE)
    val actualPost    = DomainCSJsonNetExtension.postEnum(newE)
    val actualImports = DomainCSJsonNetExtension.importsEnum

    assert(expectedPre == actualPre, s"enum pre attr diverges\nlegacy=$expectedPre\nnew   =$actualPre")
    assert(expectedPost == actualPost, s"enum post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
    assert(expectedImports == actualImports, s"enum imports diverge\nlegacy=$expectedImports\nnew   =$actualImports")
  }

  // ----------------------------------------------------------------------
  // DTO handlers (no inherited fields, no impl-iface ancestry)
  // ----------------------------------------------------------------------
  test("DTO with single primitive field: pre + post + imports byte-equal to legacy") {
    val dtoId = DTOId(typePath, "Point")
    val field = Field(Primitive.TInt32, "x", emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(List(field), List.empty, Super.empty), emptyMeta)
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(List(field), List.empty, Super.empty), emptyMeta)

    val domain = emptyDomain(domainId).copy(
      flattenedStructs = Map(dtoId -> FlatStruct(dtoId, List(FlatField(field, dtoId, 0)), List.empty, List.empty)),
      userTypes        = Map(dtoId -> newDto),
    )
    val newCtx = new DomainCSContext(domain, parsedStub(domainId), options)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyDto)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPre     = JsonNetExtension.preModelEmit(lctx, legacyDto)
    val expectedPost    = JsonNetExtension.postModelEmit(lctx, legacyDto)
    val expectedImports = JsonNetExtension.imports(lctx, legacyDto).toList

    val actualPre     = newCtx.jsonNetExtension.preDto(domain, newDto)
    val actualPost    = newCtx.jsonNetExtension.postDto(domain, newDto, ts, im)
    val actualImports = newCtx.jsonNetExtension.importsDto

    assert(expectedPre == actualPre, s"dto pre attr diverges\nlegacy=$expectedPre\nnew   =$actualPre")
    assert(expectedPost == actualPost, s"dto post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
    assert(expectedImports == actualImports, s"dto imports diverge\nlegacy=$expectedImports\nnew   =$actualImports")
  }

  test("DTO with multiple primitive fields: post converter preserves field order") {
    val dtoId  = DTOId(typePath, "Triple")
    val fields = List(
      Field(Primitive.TInt32, "a", emptyMeta),
      Field(Primitive.TString, "b", emptyMeta),
      Field(Primitive.TBool, "c", emptyMeta),
    )
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(fields, List.empty, Super.empty), emptyMeta)
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(fields, List.empty, Super.empty), emptyMeta)

    val domain = emptyDomain(domainId).copy(
      flattenedStructs = Map(dtoId -> FlatStruct(dtoId, fields.map(f => FlatField(f, dtoId, 0)), List.empty, List.empty)),
      userTypes        = Map(dtoId -> newDto),
    )
    val newCtx = new DomainCSContext(domain, parsedStub(domainId), options)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyDto)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPost = JsonNetExtension.postModelEmit(lctx, legacyDto)
    val actualPost   = newCtx.jsonNetExtension.postDto(domain, newDto, ts, im)

    assert(expectedPost == actualPost, s"multi-field dto post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
  }

  test("DTO with no fields: post converter emits reader.Skip() branch") {
    val dtoId    = DTOId(typePath, "Empty")
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(List.empty, List.empty, Super.empty), emptyMeta)
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(List.empty, List.empty, Super.empty), emptyMeta)

    val domain = emptyDomain(domainId).copy(
      flattenedStructs = Map(dtoId -> FlatStruct(dtoId, List.empty, List.empty, List.empty)),
      userTypes        = Map(dtoId -> newDto),
    )
    val newCtx = new DomainCSContext(domain, parsedStub(domainId), options)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyDto)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPost = JsonNetExtension.postModelEmit(lctx, legacyDto)
    val actualPost   = newCtx.jsonNetExtension.postDto(domain, newDto, ts, im)

    assert(expectedPost == actualPost, s"empty-dto post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
    assert(actualPost.contains("reader.Skip();"), "empty-dto path must emit reader.Skip()")
  }

  // ----------------------------------------------------------------------
  // Interface handlers
  // ----------------------------------------------------------------------
  test("Interface: pre + post + imports byte-equal to legacy") {
    val ifId    = InterfaceId(typePath, "Animal")
    val field   = Field(Primitive.TString, "name", emptyMeta)
    val supers  = Super(List.empty, List.empty, List.empty)
    val legacyI = LegacyTypeDef.Interface(ifId, Structure(List(field), List.empty, supers), emptyMeta)
    val newI    = NewTypeDef.Interface(ifId, NewStruct(List(field), List.empty, supers), emptyMeta)

    val domain = emptyDomain(domainId).copy(
      flattenedStructs = Map(ifId -> FlatStruct(ifId, List(FlatField(field, ifId, 0)), List.empty, List.empty)),
      userTypes        = Map(ifId -> newI),
    )
    val newCtx = new DomainCSContext(domain, parsedStub(domainId), options)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyI)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPre     = JsonNetExtension.preModelEmit(lctx, legacyI)
    val expectedPost    = JsonNetExtension.postModelEmit(lctx, legacyI)
    val expectedImports = JsonNetExtension.imports(lctx, legacyI).toList

    val actualPre     = newCtx.jsonNetExtension.preInterface(newI)
    val actualPost    = newCtx.jsonNetExtension.postInterface(domain, newI)
    val actualImports = newCtx.jsonNetExtension.importsInterface

    assert(expectedPre == actualPre, s"iface pre attr diverges\nlegacy=$expectedPre\nnew   =$actualPre")
    assert(expectedPost == actualPost, s"iface post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
    assert(expectedImports == actualImports, s"iface imports diverge\nlegacy=$expectedImports\nnew   =$actualImports")
  }

  test("Interface impl-struct converter: post block matches legacy `JsonNetExtension.postModelEmit(ctx, dto)`") {
    // The legacy renderer constructs the synthetic impl-DTO and emits its
    // converter via `postModelEmit(ctx, dto: DTO)`. Verify the new-IR
    // helper `postInterfaceImplStruct` matches that legacy branch.
    val ifId   = InterfaceId(typePath, "Animal")
    val field  = Field(Primitive.TString, "name", emptyMeta)
    val supers = Super(List.empty, List.empty, List.empty)
    val legacyI = LegacyTypeDef.Interface(ifId, Structure(List(field), List.empty, supers), emptyMeta)
    val newI    = NewTypeDef.Interface(ifId, NewStruct(List(field), List.empty, supers), emptyMeta)

    val domain = emptyDomain(domainId).copy(
      flattenedStructs = Map(ifId -> FlatStruct(ifId, List(FlatField(field, ifId, 0)), List.empty, List.empty)),
      userTypes        = Map(ifId -> newI),
    )
    val newCtx = new DomainCSContext(domain, parsedStub(domainId), options)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyI)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    // Legacy: construct the synthetic impl-DTO the same way `renderInterface` does.
    val eid       = ts.tools.implId(ifId)
    val validFields = ts.structure.structure(legacyI).all.map(_.field)
    val legacyDto = LegacyTypeDef.DTO(eid, Structure(validFields, List.empty, Super(List(ifId), List.empty, List.empty)), NodeMeta.empty)

    val expectedPre  = JsonNetExtension.preModelEmit(lctx, legacyDto)
    val expectedPost = JsonNetExtension.postModelEmit(lctx, legacyDto)

    val actualPre  = newCtx.jsonNetExtension.preInterfaceImplStruct(newI)
    val actualPost = newCtx.jsonNetExtension.postInterfaceImplStruct(domain, newI, ts, im)

    assert(expectedPre == actualPre, s"iface impl-struct pre attr diverges\nlegacy=$expectedPre\nnew   =$actualPre")
    assert(expectedPost == actualPost, s"iface impl-struct post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
  }

  // ----------------------------------------------------------------------
  // ADT handlers
  // ----------------------------------------------------------------------
  test("ADT with primitive-typed members: pre + post + imports byte-equal to legacy") {
    val adtId = AdtId(typePath, "AOrB")
    val alts  = List(
      AdtMember(Primitive.TInt32, None, emptyMeta),
      AdtMember(Primitive.TString, None, emptyMeta),
    )
    val legacyA = LegacyTypeDef.Adt(adtId, alts, emptyMeta)
    val newA    = NewTypeDef.Adt(adtId, alts, emptyMeta)

    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyA)
    implicit val im: CSharpImports = CSharpImports(List.empty)
    val lctx = legacyCtx(ts)

    val expectedPre     = JsonNetExtension.preModelEmit(lctx, legacyA)
    val expectedPost    = JsonNetExtension.postModelEmit(lctx, legacyA)
    val expectedImports = JsonNetExtension.imports(lctx, legacyA).toList

    val actualPre     = DomainCSJsonNetExtension.preAdt(newA)
    val actualPost    = DomainCSJsonNetExtension.postAdt(newA, ts, im)
    val actualImports = DomainCSJsonNetExtension.importsAdt

    assert(expectedPre == actualPre, s"adt pre attr diverges\nlegacy=$expectedPre\nnew   =$actualPre")
    assert(expectedPost == actualPost, s"adt post converter diverges\nlegacy=$expectedPost\nnew   =$actualPost")
    assert(expectedImports == actualImports, s"adt imports diverge\nlegacy=$expectedImports\nnew   =$actualImports")
  }
}
