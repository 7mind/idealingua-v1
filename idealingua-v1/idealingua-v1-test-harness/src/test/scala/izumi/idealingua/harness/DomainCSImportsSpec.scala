package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, Field, NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.DomainCSImports
import izumi.idealingua.typer.ir.{Domain, FlatField, FlatStruct, Fingerprint, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b/7c-post: byte-parity unit test for `DomainCSImports`.
  *
  * Compares the `using ...;` block produced by the new Domain-based shim
  * against the legacy `CSharpImports.apply(def, pkg)(ts).renderImports(...)`
  * for representative `TypeDef` shapes (DTO with primitive fields, DTO with
  * timestamp + option, DTO with reference type triggering namespace import).
  */
final class DomainCSImportsSpec extends AnyFunSuite {

  private val domainId  = DomainId(Seq("idltest"), "cs_imports_spec")
  private val typePath  = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty

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

  // ----- DTO with primitive fields only (no `using`s) --------------------

  test("DTO with primitive-only fields: byte-equal to legacy") {
    val dtoId  = DTOId(typePath, "Bag")
    val fields = List(
      Field(Primitive.TString, "name", emptyMeta),
      Field(Primitive.TInt32, "count", emptyMeta),
    )
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(fields, List.empty, Super.empty), emptyMeta)

    val flat   = FlatStruct(dtoId, fields.map(FlatField(_, dtoId, 0)), List.empty, List.empty)
    val domain = buildDomain(newDto, Some(flat))
    val ts     = buildLegacy(legacyDto)

    val newRender = DomainCSImports.forTypeDef(newDto, dtoId.path.toPackage, domain).renderImports(List("System", "System.Collections", "System.Collections.Generic"))
    val legacy    = CSharpImports(legacyDto, dtoId.path.toPackage)(ts).renderImports(List("System", "System.Collections", "System.Collections.Generic"))

    val _ = assert(legacy == newRender, s"DTO/primitive C# imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }

  // ----- DTO with timestamp primitive ------------------------------------

  test("DTO with timestamp field: emits System + IRT + Globalization (byte-equal to legacy)") {
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

    val newRender = DomainCSImports.forTypeDef(newDto, dtoId.path.toPackage, domain).renderImports(List("System"))
    val legacy    = CSharpImports(legacyDto, dtoId.path.toPackage)(ts).renderImports(List("System"))

    val _ = assert(legacy == newRender, s"DTO/timestamp C# imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }

  // ----- DTO with Option<primitive> (nullable check path) ----------------

  test("DTO with Option<String>: nullable-aware import emission (byte-equal to legacy)") {
    import izumi.idealingua.model.common.Generic
    val dtoId  = DTOId(typePath, "MaybeBag")
    val optType = Generic.TOption(Primitive.TString)
    val fields = List(
      Field(optType, "name", emptyMeta),
    )
    val newDto    = NewTypeDef.Dto(dtoId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyDto = LegacyTypeDef.DTO(dtoId, Structure(fields, List.empty, Super.empty), emptyMeta)

    val flat   = FlatStruct(dtoId, fields.map(FlatField(_, dtoId, 0)), List.empty, List.empty)
    val domain = buildDomain(newDto, Some(flat))
    val ts     = buildLegacy(legacyDto)

    val newRender = DomainCSImports.forTypeDef(newDto, dtoId.path.toPackage, domain).renderImports(List("System"))
    val legacy    = CSharpImports(legacyDto, dtoId.path.toPackage)(ts).renderImports(List("System"))

    val _ = assert(legacy == newRender, s"DTO/Option C# imports diverge:\nlegacy=$legacy\nnew   =$newRender")
  }
}
