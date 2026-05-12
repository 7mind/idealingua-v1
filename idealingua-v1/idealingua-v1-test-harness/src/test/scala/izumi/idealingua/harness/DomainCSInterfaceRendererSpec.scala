package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.InterfaceId
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
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.InterfaceProduct
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField}
import izumi.idealingua.typer.ir.{Domain, FlatField, FlatStruct, Fingerprint, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M2: byte-parity unit test for
  * `DomainCSInterfaceRenderer`.
  *
  * Asserts that the Domain-consuming `DomainCSInterfaceRenderer` produces
  * the same pre-extension `InterfaceProduct` (`iface` + `companion`) as
  * the legacy `CSharpTranslator.renderInterface` (mirror, since the
  * legacy method is `protected`), given matching new-IR / legacy-IR
  * inputs and an empty extension list.
  */
final class DomainCSInterfaceRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_interface_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // Mirror of legacy `CSharpTranslator.renderInterface` (protected) with
  // `ext.preModelEmit`/`postModelEmit`/`imports` resolved empty. See
  // `idealingua-v1-transpilers/.../tocsharp/CSharpTranslator.scala:350-390`.
  private def legacyRender(i: LegacyTypeDef.Interface)(implicit im: CSharpImports, ts: Typespace): InterfaceProduct = {
    val structure = ts.structure.structure(i)
    val eid       = ts.tools.implId(i.id)

    val parentIfaces = ts.inheritance.parentsInherited(i.id).filter(_ != i.id)
    val validFields  = structure.all.filterNot(f => parentIfaces.contains(f.defn.definedBy))
    val ifaceFields =
      validFields.map(f => (f.defn.variance.nonEmpty, CSharpField(f.field, eid.name, Seq.empty)))

    val struct = CSharpClass(eid, i.id.name + eid.name, structure, List(i.id))
    val ifaceImplements =
      if (i.struct.superclasses.interfaces.isEmpty) ": IRTTI"
      else
        ": " +
        i.struct.superclasses.interfaces.map(ifc => ifc.name).mkString(", ") + ", IRTTI"

    val iface =
      s"""${im.renderUsings()}
         |
         |public interface ${i.id.name}$ifaceImplements {
         |${ifaceFields
          .map(f => s"${if (f._1) "// Would have been covariance, but C# doesn't support it:\n// " else ""}${f._2.renderMember(true)}").mkString("\n").shift(4)}
         |}
         |
         |       """.stripMargin

    val companion =
      s"""
         |${struct.renderHeader()} {
         |${struct.render(withWrapper = false, withSlices = true, withRTTI = true, withCTORs = Some(i.id.name)).shift(4)}
         |}
         |
         |       """.stripMargin

    InterfaceProduct(
      iface,
      companion,
      im.renderImports(List("IRT", "System", "System.Collections", "System.Collections.Generic", "System.Reflection")),
    )
  }

  private def metaFor(d: DomainId): DomainMetadata =
    DomainMetadata(FSPath(d.toPackage :+ s"${d.id}.domain"), Seq.empty, Seq.empty, emptyMeta)

  private def newCtxFor(d: DomainId, ifId: InterfaceId, fields: List[Field]): DomainCSContext = {
    val flat = FlatStruct(
      ownerId       = ifId,
      fields        = fields.map(f => FlatField(f, ifId, 0)),
      conflictsHard = List.empty,
      conflictsSoft = List.empty,
    )
    val newIf = NewTypeDef.Interface(ifId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val newDomain = Domain(
      id                = d,
      meta              = metaFor(d),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map(ifId -> flat),
      parents           = Map.empty,
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map(ifId -> newIf),
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

  private def assertProductEqual(label: String, expected: InterfaceProduct, actual: InterfaceProduct): Unit = {
    val _ = assert(expected.iface == actual.iface, s"$label: iface diverges\nlegacy=${expected.iface}\nnew   =${actual.iface}")
    val _ = assert(expected.companion == actual.companion, s"$label: companion diverges\nlegacy=${expected.companion}\nnew   =${actual.companion}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
  }

  test("interface with single primitive field, no parents: byte-equal to legacy") {
    val ifId   = InterfaceId(typePath, "Named")
    val fields = List(Field(Primitive.TString, "name", emptyMeta))
    val ctxNew = newCtxFor(domainId, ifId, fields)
    val newIf  = NewTypeDef.Interface(ifId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyIf = LegacyTypeDef.Interface(ifId, Structure(fields, List.empty, Super.empty), emptyMeta)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyIf)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.interfaceRenderer.renderInterface(newIf, im)
    val expected = legacyRender(legacyIf)

    assertProductEqual("iface-single-primitive", expected, actual)
  }

  test("interface with multiple primitive fields: byte-equal and order-preserving") {
    val ifId   = InterfaceId(typePath, "Sized")
    val fields = List(
      Field(Primitive.TInt32, "width", emptyMeta),
      Field(Primitive.TInt32, "height", emptyMeta),
    )
    val ctxNew = newCtxFor(domainId, ifId, fields)
    val newIf  = NewTypeDef.Interface(ifId, NewStruct(fields, List.empty, Super.empty), emptyMeta)
    val legacyIf = LegacyTypeDef.Interface(ifId, Structure(fields, List.empty, Super.empty), emptyMeta)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyIf)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.interfaceRenderer.renderInterface(newIf, im)
    val expected = legacyRender(legacyIf)

    assertProductEqual("iface-multi-primitive", expected, actual)
  }
}
