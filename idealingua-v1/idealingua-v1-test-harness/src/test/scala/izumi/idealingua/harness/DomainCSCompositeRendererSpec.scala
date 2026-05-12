package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, Field, NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.{DomainCSContext, DomainCSStruct}
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.CompositeProduct
import izumi.idealingua.translator.tocsharp.types.CSharpClass
import izumi.idealingua.typer.ir.{Domain, FlatField, FlatStruct, Fingerprint, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M2: byte-parity unit test for
  * `DomainCSCompositeRenderer`.
  *
  * Asserts that the Domain-consuming `DomainCSCompositeRenderer` produces
  * the same pre-extension `CompositeProduct.more` as the legacy
  * `CSharpTranslator.renderDto` (mirror, since the legacy method is
  * `protected`), given matching new-IR / legacy-IR inputs and an empty
  * extension list.
  *
  * Scope at M2: DTO with no interface superclasses (the slice-helpers
  * branch in `CSharpClass.render` is inert with `implements = List.empty`
  * folded from `Super.empty`). Inherited-field flattening is covered by
  * the interface spec.
  */
final class DomainCSCompositeRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_composite_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // Mirror of legacy `CSharpTranslator.renderDto` (protected), with
  // `ext.preModelEmit`/`postModelEmit`/`imports` resolved empty.
  private def legacyRender(i: LegacyTypeDef.DTO)(implicit im: CSharpImports, ts: Typespace): CompositeProduct = {
    val structure = ts.structure.structure(i)
    val struct    = CSharpClass(i.id, i.id.name, structure, List.empty)

    val dto =
      s"""${im.renderUsings()}
         |
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true).shift(4)}
         |}
         |
         |       """.stripMargin

    CompositeProduct(dto, im.renderImports(List("System", "System.Collections", "System.Collections.Generic")))
  }

  private def metaFor(d: DomainId): DomainMetadata =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def newCtxFor(d: DomainId, dtoId: DTOId, newFields: List[Field]): DomainCSContext = {
    val flat = FlatStruct(
      ownerId       = dtoId,
      fields        = newFields.map(f => FlatField(f, dtoId, 0)),
      conflictsHard = List.empty,
      conflictsSoft = List.empty,
    )
    val newDto = NewTypeDef.Dto(dtoId, NewStruct(newFields, List.empty, Super.empty), emptyMeta)
    val newDomain = Domain(
      id                = d,
      meta              = metaFor(d),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map(dtoId -> flat),
      parents           = Map.empty,
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map(dtoId -> newDto),
    )
    val parsedStub: DomainMeshResolved = new DomainMeshResolved {
      override def id: DomainId                                  = d
      override def imports: Seq[RawImport]                       = Seq.empty
      override def members: Seq[RawTopLevelDefn]                 = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath                                = FSPath(d.toPackage :+ s"${d.id}.domain")
      override def directInclusions: Seq[RawInclusion]           = Seq.empty
      override def meta: RawNodeMeta                             = rawMeta
    }
    new DomainCSContext(newDomain, parsedStub, options)
  }

  private def legacyTypespaceFor(d: DomainId, td: LegacyTypeDef): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = d,
      meta       = metaFor(d),
      types      = Seq(td),
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def assertProductEqual(label: String, expected: CompositeProduct, actual: CompositeProduct): Unit = {
    val _ = assert(expected.more == actual.more, s"$label: more diverges\nlegacy=${expected.more}\nnew   =${actual.more}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
  }

  test("DTO with single primitive field: byte-equal to legacy") {
    val dtoId  = DTOId(typePath, "Point")
    val field  = Field(Primitive.TInt32, "x", emptyMeta)
    val newDto = NewTypeDef.Dto(dtoId, NewStruct(List(field), List.empty, Super.empty), emptyMeta)
    val ctxNew = newCtxFor(domainId, dtoId, List(field))

    // Note: ctxNew already carries the DTO in `userTypes` / `flattenedStructs`.
    // Sanity: assert the adapter recovers a legacy-shaped Struct with the same
    // field name + origin set up.
    val _ = DomainCSStruct.fromFlat(dtoId, ctxNew.domain.flattenedStructs(dtoId), newDto.struct.superclasses, ctxNew.domain)

    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(List(field), List.empty, Super.empty), emptyMeta)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyDto)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.compositeRenderer.renderDto(newDto, im)
    val expected = legacyRender(legacyDto)

    assertProductEqual("dto-single-primitive", expected, actual)
  }

  test("DTO with multiple primitive fields: byte-equal and order-preserving") {
    val dtoId   = DTOId(typePath, "Triple")
    val fields  = List(
      Field(Primitive.TInt32, "a", emptyMeta),
      Field(Primitive.TString, "b", emptyMeta),
      Field(Primitive.TBool, "c", emptyMeta),
    )
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(fields, List.empty, Super.empty), emptyMeta)
    val ctxNew    = newCtxFor(domainId, dtoId, fields)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyDto)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.compositeRenderer.renderDto(newDto, im)
    val expected = legacyRender(legacyDto)

    assertProductEqual("dto-multi-primitive", expected, actual)
  }
}
