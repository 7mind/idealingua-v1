package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{DTOId, InterfaceId}
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
import izumi.idealingua.translator.toscala.domain.extensions.DomainCastDownExpandExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M5: unit test for `DomainCastDownExpandExtension`. */
final class DomainCastDownExpandExtensionSpec extends AnyFunSuite {
  private val domainId  = DomainId(Seq("idltest"), "cast_down_spec")
  private val tp        = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val rawMeta   = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val options   = CompilerOptions[ScalaBuildManifest](IDLLanguage.Scala, ScalaBuildManifest.example)

  private def metaFor(d: DomainId) =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def ctxFor(extras: Map[izumi.idealingua.model.common.TypeId, NewTypeDef], flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct], implementing: Map[InterfaceId, Set[DTOId]]): DomainSTContext = {
    val dom = Domain(
      id = domainId, meta = metaFor(domainId), members = Map.empty, roots = Set.empty,
      ephemeralsOf = Map.empty, ephemeralOwner = Map.empty, flattenedStructs = flats,
      parents = Map.empty, implementingDtos = implementing, loops = Set.empty,
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

  test("interface with one implementing DTO emits a downcast_extend helper") {
    val iface = InterfaceId(tp, "Animal")
    val dto   = DTOId(tp, "Dog")
    val nameField = Field(Primitive.TString, "name", emptyMeta)
    val tagField  = Field(Primitive.TString, "tag", emptyMeta)
    val ifaceTD = NewTypeDef.Interface(iface, Struct(List(nameField), List.empty, Super.empty), emptyMeta)
    val dtoTD   = NewTypeDef.Dto(dto, Struct(List(tagField), List.empty, Super.empty.copy(interfaces = List(iface))), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      iface -> FlatStruct(iface, List(FlatField(nameField, iface, 0)), List.empty, List.empty),
      dto   -> FlatStruct(dto, List(FlatField(nameField, iface, 1), FlatField(tagField, dto, 0)), List.empty, List.empty),
    )
    val ctx = ctxFor(Map(iface -> ifaceTD, dto -> dtoTD), flats, Map(iface -> Set(dto)))

    val helpers = DomainCastDownExpandExtension.constructorsForInterface(ctx, ifaceTD)
    assert(helpers.size == 1, s"expected 1 downcast helper, got ${helpers.size}")
    // F-TextTree M8a: extension returns rendered Scala source directly.
    val syntax = helpers.head
    assert(syntax.contains("_downcast_extend_"), s"expected downcast helper name: $syntax")
    assert(syntax.contains("def using"), s"expected using(...) factory: $syntax")
  }

  test("interface with no implementors yields no helpers") {
    val iface = InterfaceId(tp, "Empty")
    val ifaceTD = NewTypeDef.Interface(iface, Struct(List.empty, List.empty, Super.empty), emptyMeta)
    val ctx = ctxFor(Map(iface -> ifaceTD), Map(iface -> FlatStruct(iface, List.empty, List.empty, List.empty)), Map.empty)
    assert(DomainCastDownExpandExtension.constructorsForInterface(ctx, ifaceTD).isEmpty)
  }
}
