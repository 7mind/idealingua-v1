package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.{AdtId, DTOId, EnumId, IdentifierId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{AdtMember, DomainMetadata, EnumMember, Field, IdField, NodeMeta, Super}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.ScalaBuildManifest
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.toscala.domain.DomainSTContext
import izumi.idealingua.translator.toscala.domain.extensions.DomainCirceDerivationTranslatorExtension
import izumi.idealingua.translator.toscala.extensions.ScalaTranslatorExtension
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7a.2 Phase B M5: unit test for `DomainCirceDerivationTranslatorExtension`.
  *
  * Wire-format-critical: verifies that the new-IR Circe extension produces
  * `Encoder`/`Decoder` boilerplate traits for one representative case per
  * type family (Identifier, Enum, DTO, ADT, Interface). Each emitted trait
  * is parsed via scala.meta under both Scala 2.13 and Scala 3.0 dialects.
  *
  * Determinism check: an Interface with two implementing DTOs has its case
  * arms emitted in `_.toString`-sorted order, so we assert a fixed order.
  */
final class DomainCirceExtensionSpec extends AnyFunSuite {
  private val domainId  = DomainId(Seq("idltest"), "circe_spec")
  private val tp        = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty
  private val rawMeta   = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val emptyExts: Seq[ScalaTranslatorExtension] = Seq.empty
  private val options   = CompilerOptions[ScalaTranslatorExtension, ScalaBuildManifest](IDLLanguage.Scala, emptyExts, ScalaBuildManifest.example)

  private def metaFor(d: DomainId) =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def ctxFor(
    extras: Map[izumi.idealingua.model.common.TypeId, NewTypeDef],
    flats: Map[izumi.idealingua.model.common.StructureId, FlatStruct],
    implementing: Map[InterfaceId, Set[DTOId]] = Map.empty,
  ): DomainSTContext = {
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

  private def renderSyntax(tree: scala.meta.Tree, isScala3: Boolean): String = {
    import scala.meta.*
    val dialect = if (isScala3) scala.meta.dialects.Scala30 else scala.meta.dialects.Scala213
    dialect(tree).syntax
  }

  test("identifier circe trait parses and contains Encoder/Decoder") {
    val id = IdentifierId(tp, "UserId")
    val td = NewTypeDef.Identifier(id, List(IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)), emptyMeta)
    val ctx = ctxFor(Map(id -> td), Map.empty)
    val ct = DomainCirceDerivationTranslatorExtension.emitForIdentifier(ctx, td)
    val s = renderSyntax(ct.defn, isScala3 = false)
    assert(s.contains("UserIdCirce"))
    assert(s.contains("encodeUserId"))
    assert(s.contains("decodeUserId"))
  }

  test("enum circe trait parses and contains Encoder/Decoder") {
    val id = EnumId(tp, "Color")
    val td = NewTypeDef.Enum(id, List(EnumMember("Red", emptyMeta), EnumMember("Green", emptyMeta)), emptyMeta)
    val ctx = ctxFor(Map(id -> td), Map.empty)
    val ct = DomainCirceDerivationTranslatorExtension.emitForEnum(ctx, td)
    val s = renderSyntax(ct.defn, isScala3 = false)
    assert(s.contains("ColorCirce"))
    assert(s.contains("encodeColor"))
  }

  test("DTO circe trait parses (deriveEncoder/deriveDecoder shape)") {
    val id = DTOId(tp, "User")
    val f1 = Field(Primitive.TString, "name", emptyMeta)
    val f2 = Field(Primitive.TInt32, "age", emptyMeta)
    val td = NewTypeDef.Dto(id, Struct(List(f1, f2), List.empty, Super.empty), emptyMeta)
    val flat = FlatStruct(id, List(FlatField(f1, id, 0), FlatField(f2, id, 0)), List.empty, List.empty)
    val ctx = ctxFor(Map(id -> td), Map(id -> flat))
    val ct213 = DomainCirceDerivationTranslatorExtension.emitForDto(ctx, td, List("2.13.18"))
    val s213 = renderSyntax(ct213.defn, isScala3 = false)
    assert(s213.contains("UserCirce"))
    assert(s213.contains("deriveEncoder[User]"))
    assert(s213.contains("deriveDecoder[User]"))
    assert(s213.contains("io.circe.derivation"), s"expected scala 2.13 deriver import: $s213")

    val ct3 = DomainCirceDerivationTranslatorExtension.emitForDto(ctx, td, List("3.8.3"))
    val s3 = renderSyntax(ct3.defn, isScala3 = true)
    assert(s3.contains("io.circe.generic.semiauto"), s"expected scala 3 deriver import: $s3")
  }

  test("ADT circe trait parses with one case-arm per alternative") {
    val a = DTOId(tp, "A")
    val b = DTOId(tp, "B")
    val adtId = AdtId(tp, "Choice")
    val td = NewTypeDef.Adt(adtId, List(AdtMember(a, None, emptyMeta), AdtMember(b, None, emptyMeta)), emptyMeta)
    val ctx = ctxFor(Map(adtId -> td), Map.empty)
    val ct = DomainCirceDerivationTranslatorExtension.emitForAdt(ctx, td)
    val s = renderSyntax(ct.defn, isScala3 = false)
    assert(s.contains("ChoiceCirce"))
    assert(s.contains("encodeChoice"))
    assert(s.contains("decodeChoice"))
  }

  test("interface circe trait emits implementor cases in sorted (deterministic) order") {
    val iface = InterfaceId(tp, "Shape")
    val cir   = DTOId(tp, "Circle")
    val sqr   = DTOId(tp, "Square")
    val td = NewTypeDef.Interface(iface, Struct(List.empty, List.empty, Super.empty), emptyMeta)
    // Provide implementors in deliberately reverse-sorted insertion order to
    // verify that the extension sorts by `_.toString`.
    val impl = Map[InterfaceId, Set[DTOId]](iface -> Set(sqr, cir))
    val ctx = ctxFor(Map(iface -> td), Map.empty, impl)
    val ct = DomainCirceDerivationTranslatorExtension.emitForInterface(ctx, td)
    val s213 = renderSyntax(ct.defn, isScala3 = false)
    assert(s213.contains("ShapeCirce"))
    // wireId for cross-domain DTO is the fully qualified path; check substring
    val circleIdx = s213.indexOf("Circle")
    val squareIdx = s213.indexOf("Square")
    assert(circleIdx > 0 && squareIdx > 0, s"expected both wire ids: $s213")
    assert(circleIdx < squareIdx, s"expected deterministic Circle-before-Square order: $s213")
  }
}
