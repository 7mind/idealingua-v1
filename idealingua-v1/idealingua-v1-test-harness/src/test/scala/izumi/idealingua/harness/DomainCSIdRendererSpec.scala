package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.IdentifierId
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, IdField, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.IdentifierProduct
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField}
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M2: byte-parity unit test for
  * `DomainCSIdRenderer`.
  *
  * Asserts that the Domain-consuming `DomainCSIdRenderer` produces the
  * same pre-extension `IdentifierProduct` as the legacy
  * `CSharpTranslator.renderIdentifier` (mirror, since the legacy method
  * is `protected`), given matching new-IR / legacy-IR inputs and an empty
  * extension list.
  */
final class DomainCSIdRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_id_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // Mirror of legacy `CSharpTranslator.renderIdentifier` (protected).
  // Verified by inspection against
  // `idealingua-v1-transpilers/.../tocsharp/CSharpTranslator.scala:302-348`,
  // with `ext.preModelEmit`/`ext.postModelEmit` resolved to "" for the
  // empty extension list.
  private def legacyRender(i: LegacyTypeDef.Identifier)(implicit im: CSharpImports, ts: Typespace): IdentifierProduct = {
    val fields       = ts.structure.structure(i).all.map(f => CSharpField(f.field, i.id.name))
    val fieldsSorted = fields.sortBy(_.name)
    val csClass      = CSharpClass(i.id, i.id.name, fields)
    val prefixLength = i.id.name.length + 1

    val decl =
      s"""${im.renderUsings()}
         |
         |${csClass.renderHeader()} {
         |    private static char[] idSplitter = new char[]{':'};
         |${csClass.render(withWrapper = false, withSlices = false, withRTTI = true).shift(4)}
         |    public override string ToString() {
         |        var suffix = ${fieldsSorted.map(f => f.tp.renderToString(f.renderMemberName(), escape = true)).mkString(" + \":\" + ")};
         |        return "${i.id.name}#" + suffix;
         |    }
         |
         |    public static ${i.id.name} From(string value) {
         |        if (value == null) {
         |            throw new ArgumentNullException("value");
         |        }
         |
         |        if (!value.StartsWith("${i.id.name}#", StringComparison.Ordinal)) {
         |            throw new ArgumentException(string.Format("Expected identifier for type ${i.id.name}, got {0}", value));
         |        }
         |
         |        var parts = value.Substring($prefixLength, value.Length - $prefixLength).Split(idSplitter, StringSplitOptions.None);
         |        if (parts.Length != ${fields.length}) {
         |            throw new ArgumentException(string.Format("Expected identifier for type ${i.id.name} with ${fields.length} parts, got {0} in string {1}", parts.Length, value));
         |        }
         |
         |        var res = new ${i.id.name}();
         |${fieldsSorted.zipWithIndex.map { case (f, index) => s"res.${f.renderMemberName()} = ${f.tp.renderFromString(s"parts[$index]", unescape = true)};" }
          .mkString("\n").shift(8)}
         |        return res;
         |    }
         |}
         |
         |
         """.stripMargin

    IdentifierProduct(
      decl,
      im.renderImports(List("System", "System.Collections", "System.Collections.Generic")),
    )
  }

  private def metaFor(d: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(d.toPackage :+ s"${d.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(d: DomainId): DomainCSContext = {
    val newDomain = Domain(
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

  private def assertProductEqual(label: String, expected: IdentifierProduct, actual: IdentifierProduct): Unit = {
    val _ = assert(expected.identitier == actual.identitier, s"$label: identifier diverges\nlegacy=${expected.identitier}\nnew   =${actual.identitier}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
  }

  test("identifier with single primitive field: byte-equal to legacy") {
    val ctxNew = newCtxFor(domainId)
    val id     = IdentifierId(typePath, "UserId")
    val newId    = NewTypeDef.Identifier(id, List(IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)), emptyMeta)
    val legacyId = LegacyTypeDef.Identifier(id, List(IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)), emptyMeta)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyId)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.idRenderer.renderIdentifier(newId, ts, im)
    val expected = legacyRender(legacyId)

    assertProductEqual("id-single-primitive", expected, actual)
  }

  test("identifier with multiple primitive fields: byte-equal and sort-stable") {
    val ctxNew = newCtxFor(domainId)
    val id     = IdentifierId(typePath, "CompositeId")
    val fields = List(
      IdField.PrimitiveField(Primitive.TString, "name", emptyMeta),
      IdField.PrimitiveField(Primitive.TInt32, "version", emptyMeta),
    )
    val newId    = NewTypeDef.Identifier(id, fields, emptyMeta)
    val legacyId = LegacyTypeDef.Identifier(id, fields, emptyMeta)
    implicit val ts: Typespace     = legacyTypespaceFor(domainId, legacyId)
    implicit val im: CSharpImports = CSharpImports(List.empty)

    val actual   = ctxNew.idRenderer.renderIdentifier(newId, ts, im)
    val expected = legacyRender(legacyId)

    assertProductEqual("id-multi-primitive", expected, actual)
  }
}
