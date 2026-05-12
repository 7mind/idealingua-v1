package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{DTOId, EnumId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, EnumMember, Field, NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.domain.DomainTSImports
import izumi.idealingua.typer.ir.{Domain, FlatField, FlatStruct, Fingerprint, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b/7c-post: byte-parity unit test for `DomainTSImports`.
  *
  * Compares the `import { ... } from '...';` block produced by the new
  * Domain-based shim against the legacy `TypeScriptImports.apply(ts, def,
  * pkg)(...).render(ts)` for representative `TypeDef` shapes (DTO with
  * primitive fields, interface with parent reference, ADT alternatives).
  */
final class DomainTSImportsSpec extends AnyFunSuite {

  private val domainId  = DomainId(Seq("idltest"), "ts_imports_spec")
  private val typePath  = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val manifest  = TypeScriptBuildManifest.example

  private def metaFor(d: DomainId): DomainMetadata =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def buildLegacy(td: LegacyTypeDef): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq(td),
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def buildDomain(td: NewTypeDef, flat: Option[FlatStruct] = None): Domain = {
    val flatMap = flat match {
      case Some(fs) => Map(td.id.asInstanceOf[izumi.idealingua.model.common.StructureId] -> fs)
      case None     => Map.empty[izumi.idealingua.model.common.StructureId, FlatStruct]
    }
    Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = flatMap,
      parents           = Map.empty,
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map(td.id -> td),
    )
  }

  // ----- DTO with primitive fields only (no imports) ---------------------

  test("DTO with primitive-only fields: byte-equal to legacy") {
    val dtoId    = DTOId(typePath, "Bag")
    val fields   = List(
      Field(Primitive.TString, "name", emptyMeta),
      Field(Primitive.TInt32, "count", emptyMeta),
    )
    val newDto = NewTypeDef.Dto(dtoId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(fields, List.empty, Super.empty), emptyMeta)

    val flat = FlatStruct(dtoId, fields.map(FlatField(_, dtoId, 0)), List.empty, List.empty)
    val domain = buildDomain(newDto, Some(flat))
    val ts     = buildLegacy(legacyDto)

    val newRender = DomainTSImports.forTypeDef(newDto, dtoId.path.toPackage, domain, manifest).render
    val legacy    = TypeScriptImports(ts, legacyDto, dtoId.path.toPackage, manifest = manifest).render(ts)

    val _ = assert(legacy == newRender, s"DTO/primitive imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }

  // ----- DTO with timestamp primitive (Formatter import) -----------------

  test("DTO with timestamp field: emits Formatter import (byte-equal to legacy)") {
    val dtoId  = DTOId(typePath, "Event")
    val fields = List(
      Field(Primitive.TTs, "when", emptyMeta),
      Field(Primitive.TString, "label", emptyMeta),
    )
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(fields, List.empty, Super.empty), emptyMeta)

    val flat   = FlatStruct(dtoId, fields.map(FlatField(_, dtoId, 0)), List.empty, List.empty)
    val domain = buildDomain(newDto, Some(flat))
    val ts     = buildLegacy(legacyDto)

    val newRender = DomainTSImports.forTypeDef(newDto, dtoId.path.toPackage, domain, manifest).render
    val legacy    = TypeScriptImports(ts, legacyDto, dtoId.path.toPackage, manifest = manifest).render(ts)

    val _ = assert(legacy == newRender, s"DTO/Formatter imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }

  // ----- ADT with enum + DTO alternatives --------------------------------

  test("ADT with mixed-category alternatives: byte-equal to legacy") {
    import izumi.idealingua.model.il.ast.typed.AdtMember
    val enumId = EnumId(typePath, "Color")
    val dtoSubId = DTOId(typePath, "Sub")
    val adtId  = izumi.idealingua.model.common.TypeId.AdtId(typePath, "U")

    val enumTd  = LegacyTypeDef.Enumeration(enumId, List(EnumMember("RED", emptyMeta)), emptyMeta)
    val dtoTd   = LegacyTypeDef.DTO(dtoSubId, Structure(List.empty, List.empty, Super.empty), emptyMeta)
    val adtMembers: List[AdtMember] = List(
      AdtMember(enumId, None, emptyMeta),
      AdtMember(dtoSubId, None, emptyMeta),
    )
    val newAdt    = NewTypeDef.Adt(adtId, adtMembers, emptyMeta)
    val legacyAdt = LegacyTypeDef.Adt(adtId, adtMembers, emptyMeta)

    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq(enumTd, dtoTd, legacyAdt),
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    val ts = new TypespaceImpl(legacyDomain)

    val newDto = NewTypeDef.Dto(dtoSubId, NewStruct(List.empty, List.empty, Super.empty), emptyMeta)
    val newEnum = NewTypeDef.Enum(enumId, List(EnumMember("RED", emptyMeta)), emptyMeta)
    val domain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map(dtoSubId -> FlatStruct(dtoSubId, List.empty, List.empty, List.empty)),
      parents           = Map.empty,
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map[izumi.idealingua.model.common.TypeId, NewTypeDef](
        adtId    -> newAdt,
        dtoSubId -> newDto,
        enumId   -> newEnum,
      ),
    )

    val newRender = DomainTSImports.forTypeDef(newAdt, adtId.path.toPackage, domain, manifest).render
    val legacy    = TypeScriptImports(ts, legacyAdt, adtId.path.toPackage, manifest = manifest).render(ts)

    val _ = assert(legacy == newRender, s"ADT imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }

  // ----- Interface with interface parent ---------------------------------

  test("Interface with parent interface: byte-equal to legacy (impl-id projection)") {
    val parentId = InterfaceId(typePath, "Parent")
    val childId  = InterfaceId(typePath, "Child")
    val parentTd = LegacyTypeDef.Interface(parentId, Structure(List.empty, List.empty, Super.empty), emptyMeta)
    val childTd  = LegacyTypeDef.Interface(childId, Structure(List.empty, List.empty, Super(List(parentId), List.empty, List.empty)), emptyMeta)

    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq(parentTd, childTd),
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    val ts = new TypespaceImpl(legacyDomain)

    val newParent = NewTypeDef.Interface(parentId, NewStruct(List.empty, List.empty, Super.empty), emptyMeta)
    val newChild  = NewTypeDef.Interface(childId, NewStruct(List.empty, List.empty, Super(List(parentId), List.empty, List.empty)), emptyMeta)
    val domain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map(
        parentId -> FlatStruct(parentId, List.empty, List.empty, List.empty),
        childId  -> FlatStruct(childId, List.empty, List.empty, List.empty),
      ),
      parents           = Map(childId -> Set(parentId)),
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map[izumi.idealingua.model.common.TypeId, NewTypeDef](
        parentId -> newParent,
        childId  -> newChild,
      ),
    )

    val newRender = DomainTSImports.forTypeDef(newChild, childId.path.toPackage, domain, manifest).render
    val legacy    = TypeScriptImports(ts, childTd, childId.path.toPackage, manifest = manifest).render(ts)

    val _ = assert(legacy == newRender, s"Interface-with-parent imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }
}
