package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.IdentifierId
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, IdField, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.IdentifierProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M2: byte-parity unit test for `DomainTSIdRenderer`.
  *
  * Asserts that the Domain-consuming `DomainTSIdRenderer` produces the same
  * pre-extension `IdentifierProduct` (`identifier` body, `identifierInterface`
  * body, `header`, `preamble`) as the legacy
  * `TypeScriptTranslator.renderIdentifier` (mirror, since the legacy method
  * is `protected`), given matching new-IR / legacy-IR inputs and an empty
  * extension list.
  */
final class DomainTSIdRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "ts_id_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)
  private val conv       = new izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter()

  // Mirror of legacy `TypeScriptTranslator.renderIdentifier` (protected). The
  // mirror is verified by inspection against
  // `idealingua-v1-transpilers/.../totypescript/TypeScriptTranslator.scala:466-524`.
  private def legacyRender(i: LegacyTypeDef.Identifier, ts: Typespace): IdentifierProduct = {
    val imports      = TypeScriptImports(ts, i, i.id.path.toPackage, manifest = tsManifest)
    val fields       = ts.structure.structure(i)
    val sortedFields = fields.all.sortBy(_.field.name)
    val typeName     = i.id.name

    val identifierInterface =
      s"""export interface I$typeName {
         |    getPackageName(): string;
         |    getClassName(): string;
         |    getFullClassName(): string;
         |    serialize(): string;
         |
         |${fields.all
          .map(f => s"${conv.toNativeTypeName(conv.safeName(f.field.name), f.field.typeId)}: ${conv.toNativeType(f.field.typeId, ts)};").mkString("\n").shift(4)}
         |}
         """.stripMargin

    val identifier =
      s"""export class $typeName implements I$typeName {
         |${renderRuntimeNames(i.id, typeName).shift(4)}
         |${fields.all.map(f => conv.toFieldMember(f.field, ts)).mkString("\n").shift(4)}
         |
         |${fields.all.map(f => conv.toFieldMethods(f.field, ts)).mkString("\n").shift(4)}
         |    constructor(data: string | I$typeName = undefined) {
         |        if (typeof data === 'undefined' || data === null) {
         |            return;
         |        }
         |
         |        if (typeof data === 'string') {
         |            if (!data.startsWith('$typeName#')) {
         |                throw new Error('Identifier must start with $typeName, got ' + data);
         |            }
         |            const parts = data.substr(data.indexOf('#') + 1).split(':');
         |${sortedFields.zipWithIndex.map {
          case (sf, index) => s"this.${conv.safeName(sf.field.name)} = ${conv.parseTypeFromString(s"decodeURIComponent(parts[$index])", sf.field.typeId)};"
        }.mkString("\n").shift(12)}
         |        } else {
         |${fields.all
          .map(f => s"this.${conv.safeName(f.field.name)} = ${conv.deserializeType("data." + conv.safeName(f.field.name), f.field.typeId, ts)};").mkString(
            "\n"
          ).shift(12)}
         |        }
         |    }
         |
         |    public toString(): string {
         |        const suffix = ${sortedFields
          .map(sf => "encodeURIComponent(" + conv.emitTypeAsString(s"this.${conv.safeName(sf.field.name)}", sf.field.typeId) + ")").mkString(" + ':' + ")};
         |        return '$typeName#' + suffix;
         |    }
         |
         |    public serialize(): string {
         |        return this.toString();
         |    }
         |}
         """.stripMargin

    IdentifierProduct(identifier, identifierInterface, imports.render(ts), s"// ${i.id.name} Identifier")
  }

  private def renderRuntimeNames(i: izumi.idealingua.model.common.TypeId, holderName: String): String = {
    val pkg = i.path.toPackage.mkString(".")
    s"""// Runtime identification methods
       |public static readonly PackageName = '$pkg';
       |public static readonly ClassName = '${i.name}';
       |public static readonly FullClassName = '${i.wireId}';
       |
       |public getPackageName(): string { return $holderName.PackageName; }
       |public getClassName(): string { return $holderName.ClassName; }
       |public getFullClassName(): string { return $holderName.FullClassName; }
       """.stripMargin
  }

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(domainId: DomainId): DomainTSContext = {
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
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

  private def legacyTypespaceFor(domainId: DomainId, typeDef: LegacyTypeDef): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq(typeDef),
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def assertProductEqual(label: String, expected: IdentifierProduct, actual: IdentifierProduct): Unit = {
    val _ = assert(expected.identitier == actual.identitier,
      s"$label: identifier body diverges\nlegacy=${expected.identitier}\nnew   =${actual.identitier}")
    val _ = assert(expected.identifierInterface == actual.identifierInterface,
      s"$label: identifierInterface diverges\nlegacy=${expected.identifierInterface}\nnew   =${actual.identifierInterface}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges")
  }

  test("identifier with single primitive field: byte-equal to legacy") {
    val ctxNew = newCtxFor(domainId)
    val id     = IdentifierId(typePath, "UserId")
    val newId = NewTypeDef.Identifier(
      id     = id,
      fields = List(IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)),
      meta   = emptyMeta,
    )
    val legacyId = LegacyTypeDef.Identifier(
      id     = id,
      fields = List(IdField.PrimitiveField(Primitive.TString, "value", emptyMeta)),
      meta   = emptyMeta,
    )
    val ts = legacyTypespaceFor(domainId, legacyId)

    val actual   = ctxNew.idRenderer.renderIdentifier(newId, ts)
    val expected = legacyRender(legacyId, ts)

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
    val ts       = legacyTypespaceFor(domainId, legacyId)

    val actual   = ctxNew.idRenderer.renderIdentifier(newId, ts)
    val expected = legacyRender(legacyId, ts)

    assertProductEqual("id-multi-primitive", expected, actual)
  }
}
