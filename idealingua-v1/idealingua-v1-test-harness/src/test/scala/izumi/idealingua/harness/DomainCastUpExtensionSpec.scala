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
import izumi.idealingua.translator.toscala.domain.extensions.DomainCastUpExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M5: unit test for `DomainCastUpExtension`.
  *
  * A DTO whose flat struct includes the parent interface's fields gets an
  * upcast helper; a DTO with no parent yields nothing.
  */
final class DomainCastUpExtensionSpec extends AnyFunSuite {
  private val domainId  = DomainId(Seq("idltest"), "cast_up_spec")
  private val tp        = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val rawMeta   = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val options   = CompilerOptions[ScalaBuildManifest](IDLLanguage.Scala, ScalaBuildManifest.example)

  private def metaFor(d: DomainId) =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def ctxFor(extras: Map[izumi.idealingua.model.common.TypeId, NewTypeDef], flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct], parents: Map[izumi.idealingua.model.common.TypeId, Set[InterfaceId]]): DomainSTContext = {
    val dom = Domain(
      id = domainId, meta = metaFor(domainId), members = Map.empty, roots = Set.empty,
      ephemeralsOf = Map.empty, ephemeralOwner = Map.empty, flattenedStructs = flats,
      parents = parents, implementingDtos = Map.empty, loops = Set.empty,
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

  test("DTO with interface parent emits upcast helper") {
    val iface = InterfaceId(tp, "P")
    val dto   = DTOId(tp, "C")
    val f = Field(Primitive.TString, "v", emptyMeta)
    val ifaceTD = NewTypeDef.Interface(iface, Struct(List(f), List.empty, Super.empty), emptyMeta)
    val dtoTD = NewTypeDef.Dto(dto, Struct(List(f), List.empty, Super.empty.copy(interfaces = List(iface))), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      iface -> FlatStruct(iface, List(FlatField(f, iface, 0)), List.empty, List.empty),
      dto   -> FlatStruct(dto, List(FlatField(f, iface, 1)), List.empty, List.empty),
    )
    val parents = Map[izumi.idealingua.model.common.TypeId, Set[InterfaceId]](dto -> Set(iface))
    val ctx = ctxFor(Map(iface -> ifaceTD, dto -> dtoTD), flats, parents)

    val ups = DomainCastUpExtension.generateUpcastsForDto(ctx, dtoTD)
    assert(ups.nonEmpty, "expected at least one upcast helper")
    val syntax = {
      import scala.meta.*
      scala.meta.dialects.Scala213(ups.head).syntax
    }
    assert(syntax.contains("_upcast_"), s"expected upcast helper name pattern: $syntax")
  }

  test("DTO without parents still emits a reflexive self upcast helper (legacy parity, defect #3)") {
    val dto = DTOId(tp, "Solo")
    val f = Field(Primitive.TString, "v", emptyMeta)
    val dtoTD = NewTypeDef.Dto(dto, Struct(List(f), List.empty, Super.empty), emptyMeta)
    val flats = Map[izumi.idealingua.model.common.StructureId, FlatStruct](
      dto -> FlatStruct(dto, List(FlatField(f, dto, 0)), List.empty, List.empty)
    )
    val ctx = ctxFor(Map(dto -> dtoTD), flats, Map.empty)
    val ups = DomainCastUpExtension.generateUpcastsForDto(ctx, dtoTD)
    assert(ups.size == 1, "expected exactly the self-cast helper")
    val syntax = {
      import scala.meta.*
      scala.meta.dialects.Scala213(ups.head).syntax
    }
    assert(syntax.contains("Solo_upcast_Solo"), s"expected self-cast `Solo_upcast_Solo`: $syntax")
  }
}
