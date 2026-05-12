package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AdtId, DTOId, EnumId, IdentifierId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{AdtMember, DomainDefinition, DomainMetadata, EnumMember, Field, IdField, IdTuple, NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.TSTContext
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.{IntrospectionExtension, TypeScriptTranslatorExtension}
import izumi.idealingua.translator.totypescript.products.CogenProduct.{AdtProduct, CompositeProduct, EnumProduct, IdentifierProduct, InterfaceProduct}
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M4: byte-parity unit test for
  * `DomainTSIntrospectionExtension`.
  *
  * Asserts that the Domain-consuming extension produces the same
  * `Introspector.register(...)` registration block as the legacy
  * `IntrospectionExtension` for each of the 5 handler methods (enum,
  * identifier, DTO, interface, ADT), given matching new-IR / legacy-IR
  * inputs and a fixed pre-extension product.
  *
  * The legacy extension consumes `TSTContext` for `ctx.manifest`,
  * `ctx.typespace` (used by `unwindField`'s `ts.dealias` walk), and
  * `ctx.conv` (used for `conv.safeName`). We construct a real `TSTContext`
  * over the matching legacy `DomainDefinition` so both renderers traverse
  * the exact same alias / impl-id paths.
  */
final class DomainTSIntrospectionExtensionSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "ts_introspection_ext_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(domainId: DomainId, flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct] = Map.empty): DomainTSContext = {
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
    new DomainTSContext(newDomain, parsedStub, options)
  }

  private def legacyTSTContext(types: Seq[LegacyTypeDef]): TSTContext = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = types,
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    val ts: Typespace = new TypespaceImpl(legacyDomain)
    new TSTContext(ts, tsManifest, emptyExts)
  }

  test("handleEnum: byte-equal to legacy") {
    val enumId  = EnumId(typePath, "Color")
    val members = List(EnumMember("RED", emptyMeta), EnumMember("GREEN", emptyMeta), EnumMember("BLUE", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)

    val base = EnumProduct("PREFIX-CONTENT-", "// Color Enumeration")
    val ctxNew  = newCtxFor(domainId)
    val ctxLeg  = legacyTSTContext(Seq(legacyEnum))

    val actual   = ctxNew.introspectionExtension.handleEnum(options, newEnum, base)
    val expected = IntrospectionExtension.handleEnum(ctxLeg, legacyEnum, base)

    val _ = assert(actual.content == expected.content,
      s"enum: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(actual.preamble == expected.preamble, "enum: preamble diverges")
  }

  test("handleIdentifier: byte-equal to legacy") {
    val idId    = IdentifierId(typePath, "UserId")
    val idField = IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)
    val fields: IdTuple = List(idField)
    val newId    = NewTypeDef.Identifier(idId, fields, emptyMeta)
    val legacyId = LegacyTypeDef.Identifier(idId, fields, emptyMeta)

    val base = IdentifierProduct("classBody", "ifaceBody-", "// header", "// preamble")
    val ctxNew  = newCtxFor(domainId)
    val ctxLeg  = legacyTSTContext(Seq(legacyId))

    val actual   = ctxNew.introspectionExtension.handleIdentifier(ctxNew.domain, options, ctxNew.conv, newId, base)
    val expected = IntrospectionExtension.handleIdentifier(ctxLeg, legacyId, base)

    val _ = assert(actual.identitier == expected.identitier, "id: class body diverges")
    val _ = assert(actual.identifierInterface == expected.identifierInterface,
      s"id: identifierInterface diverges\nlegacy=${expected.identifierInterface}\nnew   =${actual.identifierInterface}")
    val _ = assert(actual.header == expected.header, "id: header diverges")
    val _ = assert(actual.preamble == expected.preamble, "id: preamble diverges")
  }

  test("handleDTO: byte-equal to legacy (single-field DTO)") {
    val dtoId  = DTOId(typePath, "User")
    val nameF  = Field(Primitive.TString, "name", emptyMeta)
    val ageF   = Field(Primitive.TInt32, "age", emptyMeta)
    val struct = Structure(List(nameF, ageF), List.empty, Super.empty)
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(List(nameF, ageF), List.empty, Super.empty), emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, struct, emptyMeta)
    val flat = FlatStruct(dtoId, List(FlatField(nameF, dtoId, 0), FlatField(ageF, dtoId, 0)), List.empty, List.empty)

    val base = CompositeProduct("class-body-", "// header", "// User DTO")
    val ctxNew  = newCtxFor(domainId, flats = Map(dtoId -> flat))
    val ctxLeg  = legacyTSTContext(Seq(legacyDto))

    val actual   = ctxNew.introspectionExtension.handleDTO(ctxNew.domain, options, ctxNew.conv, newDto, base)
    val expected = IntrospectionExtension.handleDTO(ctxLeg, legacyDto, base)

    val _ = assert(actual.more == expected.more,
      s"dto: content diverges\nlegacy=${expected.more}\nnew   =${actual.more}")
    val _ = assert(actual.header == expected.header, "dto: header diverges")
    val _ = assert(actual.preamble == expected.preamble, "dto: preamble diverges")
  }

  test("handleInterface: byte-equal to legacy") {
    val ifaceId = InterfaceId(typePath, "Animal")
    val nameF   = Field(Primitive.TString, "name", emptyMeta)
    val legSt   = Structure(List(nameF), List.empty, Super.empty)
    val newIface    = NewTypeDef.Interface(ifaceId, NewStruct(List(nameF), List.empty, Super.empty), emptyMeta)
    val legacyIface = LegacyTypeDef.Interface(ifaceId, legSt, emptyMeta)

    val base = InterfaceProduct("iface-body", "companion-body-", "// header", "// Animal Interface")
    val ctxNew  = newCtxFor(domainId)
    val ctxLeg  = legacyTSTContext(Seq(legacyIface))

    val actual   = ctxNew.introspectionExtension.handleInterface(ctxNew.domain, options, ctxNew.conv, newIface, base)
    val expected = IntrospectionExtension.handleInterface(ctxLeg, legacyIface, base)

    val _ = assert(actual.iface == expected.iface, "iface: iface diverges")
    val _ = assert(actual.companion == expected.companion,
      s"iface: companion diverges\nlegacy=${expected.companion}\nnew   =${actual.companion}")
    val _ = assert(actual.header == expected.header, "iface: header diverges")
    val _ = assert(actual.preamble == expected.preamble, "iface: preamble diverges")
  }

  test("handleAdt: byte-equal to legacy (two-DTO branches)") {
    val branchADtoId = DTOId(typePath, "BranchA")
    val branchBDtoId = DTOId(typePath, "BranchB")
    val branchAField = Field(Primitive.TString, "a", emptyMeta)
    val branchBField = Field(Primitive.TInt32, "b", emptyMeta)
    val adtId        = AdtId(typePath, "Choice")
    val members = List(
      AdtMember(branchADtoId, None, emptyMeta),
      AdtMember(branchBDtoId, None, emptyMeta),
    )
    val newAdt    = NewTypeDef.Adt(adtId, members, emptyMeta)
    val legacyAdt = LegacyTypeDef.Adt(adtId, members, emptyMeta)

    val branchA: LegacyTypeDef = LegacyTypeDef.DTO(branchADtoId, Structure(List(branchAField), List.empty, Super.empty), emptyMeta)
    val branchB: LegacyTypeDef = LegacyTypeDef.DTO(branchBDtoId, Structure(List(branchBField), List.empty, Super.empty), emptyMeta)

    val base = AdtProduct("adt-body-", "// header", "// Choice Algebraic Data Type")
    val ctxNew = newCtxFor(domainId)
    val ctxLeg = legacyTSTContext(Seq(legacyAdt, branchA, branchB))

    val actual   = ctxNew.introspectionExtension.handleAdt(options, ctxNew.domain, newAdt, base)
    val expected = IntrospectionExtension.handleAdt(ctxLeg, legacyAdt, base)

    val _ = assert(actual.content == expected.content,
      s"adt: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(actual.header == expected.header, "adt: header diverges")
    val _ = assert(actual.preamble == expected.preamble, "adt: preamble diverges")
  }
}
