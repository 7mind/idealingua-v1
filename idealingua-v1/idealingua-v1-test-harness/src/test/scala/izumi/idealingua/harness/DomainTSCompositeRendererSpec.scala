package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.{DTOId, InterfaceId}
import izumi.idealingua.model.common.{DomainId, Generic, Primitive, TypeId, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, DomainMetadata, Field, NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.CompositeProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M2: byte-parity unit test for
  * `DomainTSCompositeRenderer`.
  *
  * Asserts that the Domain-consuming `DomainTSCompositeRenderer` produces
  * the same pre-extension `CompositeProduct` (`more`, `header`, `preamble`)
  * as the legacy `TypeScriptTranslator.renderDto` (mirror, since the legacy
  * method is `protected`), given matching new-IR / legacy-IR inputs and an
  * empty extension list.
  */
final class DomainTSCompositeRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "ts_dto_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)
  private val conv       = new izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter()

  // Mirror of legacy `TypeScriptTranslator.renderDto` (protected). Verified by
  // inspection against `TypeScriptTranslator.scala:175-231`.
  private def legacyRender(i: LegacyTypeDef.DTO, ts: Typespace): CompositeProduct = {
    val imports        = TypeScriptImports(ts, i, i.id.path.toPackage, manifest = tsManifest)
    val fields         = ts.structure.structure(i).all
    val distinctFields = fields.distinctBy(_.field.name).map(_.field)

    val implementsInterfaces =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "implements " + i.struct.superclasses.interfaces.map(iface => iface.name).mkString(", ") + " "
      } else ""

    val extendsInterfacesSerialized =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "extends " + i.struct.superclasses.interfaces.map(iface => s"${iface.name}${ts.tools.implId(iface).name}Serialized").mkString(", ") + " "
      } else ""

    val uniqueInterfaces = ts.inheritance.parentsInherited(i.id).distinctBy(_.name)

    val dto =
      s"""export class ${i.id.name} $implementsInterfaces {
         |${renderRuntimeNames(i.id, i.id.name).shift(4)}
         |${distinctFields.map(f => conv.toFieldMember(f, ts)).mkString("\n").shift(4)}
         |
         |${distinctFields.map(f => conv.toFieldMethods(f, ts)).mkString("\n").shift(4)}
         |    constructor(data: ${i.id.name}Serialized = undefined) {
         |        if (typeof data === 'undefined' || data === null) {
         |${distinctFields
          .map(f => renderDefaultAssign(conv.deserializeName("this." + conv.safeName(f.name), f.typeId), f.typeId)).filterNot(_.isEmpty).mkString("\n").shift(12)}
         |            return;
         |        }
         |
         |${distinctFields
          .map(f => s"${conv.deserializeName("this." + conv.safeName(f.name), f.typeId)} = ${conv.deserializeType("data." + f.name, f.typeId, ts)};").mkString(
            "\n"
          ).shift(8)}
         |    }
         |
         |${uniqueInterfaces.map(si => renderDtoInterfaceSerializer(si, ts)).mkString("\n").shift(4)}
         |${uniqueInterfaces.map(si => renderDtoInterfaceLoader(si, ts)).mkString("\n").shift(4)}
         |    public serialize(): ${i.id.name}Serialized {
         |        return {
         |${renderSerializedObject(distinctFields.toList, ts).shift(12)}
         |        };
         |    }
         |}
         |
         |export interface ${i.id.name}Serialized $extendsInterfacesSerialized {
         |${distinctFields.map(f => s"${conv.toNativeTypeName(f.name, f.typeId)}: ${conv.toNativeType(f.typeId, ts, forSerialized = true)};").mkString("\n").shift(4)}
         |}
         |
         |${uniqueInterfaces.map(sc => sc.name + ts.tools.implId(sc).name + s".register(${i.id.name}.FullClassName, ${i.id.name});").mkString("\n")}
         """.stripMargin

    CompositeProduct(dto, imports.render(ts), s"// ${i.id.name} DTO")
  }

  private def renderDtoInterfaceSerializer(iid: InterfaceId, ts: Typespace): String = {
    val fields = ts.structure.structure(iid)
    s"""public to${iid.name}Serialized(): ${iid.name}${ts.tools.implId(iid).name}Serialized {
       |    return {
       |${renderSerializedObject(fields.all.map(_.field), ts).shift(8)}
       |    };
       |}
       |
       |public to${iid.name}(): ${iid.name}${ts.tools.implId(iid).name} {
       |    return new ${iid.name}${ts.tools.implId(iid).name}(this.to${iid.name}Serialized());
       |}
     """.stripMargin
  }

  private def renderDtoInterfaceLoader(iid: InterfaceId, ts: Typespace): String = {
    val fields = ts.structure.structure(iid)
    s"""public load${iid.name}Serialized(slice: ${iid.name}${ts.tools.implId(iid).name}Serialized) {
       |${renderDeserializeObject(fields.all.map(_.field), ts).shift(4)}
       |}
       |
       |public load${iid.name}(slice: ${iid.name}${ts.tools.implId(iid).name}) {
       |    this.load${iid.name}Serialized(slice.serialize());
       |}
     """.stripMargin
  }

  private def renderSerializedObject(fields: List[Field], ts: Typespace): String = {
    val serialized = fields.map(f => conv.serializeField(f, ts))
    val it         = serialized.iterator
    it.map(m => s"$m${if (it.hasNext) "," else ""}").mkString("\n")
  }

  private def renderDeserializeObject(fields: List[Field], ts: Typespace): String = {
    fields.map(f => conv.deserializeField(f, ts)).mkString("\n")
  }

  private def renderDefaultValue(id: TypeId): Option[String] = id match {
    case g: Generic =>
      g match {
        case _: Generic.TOption => None
        case _: Generic.TMap    => Some("{}")
        case _: Generic.TList   => Some("[]")
        case _: Generic.TSet    => Some("[]")
      }
    case _ => None
  }

  private def renderDefaultAssign(to: String, id: TypeId): String = {
    val defVal = renderDefaultValue(id)
    if (defVal.isDefined) s"$to = ${defVal.get};" else ""
  }

  private def renderRuntimeNames(i: TypeId, holderName: String): String = {
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

  private def newCtxFor(domainId: DomainId, dtoId: DTOId, fields: List[Field]): DomainTSContext = {
    val flat = FlatStruct(
      ownerId       = dtoId,
      fields        = fields.map(f => FlatField(f, dtoId, 0)),
      conflictsHard = List.empty,
      conflictsSoft = List.empty,
    )
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set(dtoId: TypeId),
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

  private def assertProductEqual(label: String, expected: CompositeProduct, actual: CompositeProduct): Unit = {
    val _ = assert(expected.more == actual.more,
      s"$label: body diverges\nlegacy=${expected.more}\nnew   =${actual.more}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges")
  }

  test("DTO with single primitive field: byte-equal to legacy") {
    val dtoId  = DTOId(typePath, "UserDto")
    val fields = List(Field(Primitive.TString, "name", emptyMeta))
    val ctxNew = newCtxFor(domainId, dtoId, fields)

    val newDto = NewTypeDef.Dto(
      id     = dtoId,
      struct = NewStruct(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val legacyDto = LegacyTypeDef.DTO(
      id     = dtoId,
      struct = Structure(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val ts = legacyTypespaceFor(domainId, legacyDto)

    val actual   = ctxNew.compositeRenderer.renderDto(newDto, ts)
    val expected = legacyRender(legacyDto, ts)

    assertProductEqual("dto-single-primitive", expected, actual)
  }

  test("DTO with multiple primitive fields: byte-equal and declaration-order-preserving") {
    val dtoId = DTOId(typePath, "Pair")
    val fields = List(
      Field(Primitive.TString, "key", emptyMeta),
      Field(Primitive.TInt32, "value", emptyMeta),
    )
    val ctxNew = newCtxFor(domainId, dtoId, fields)

    val newDto = NewTypeDef.Dto(
      id     = dtoId,
      struct = NewStruct(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val legacyDto = LegacyTypeDef.DTO(
      id     = dtoId,
      struct = Structure(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val ts = legacyTypespaceFor(domainId, legacyDto)

    val actual   = ctxNew.compositeRenderer.renderDto(newDto, ts)
    val expected = legacyRender(legacyDto, ts)

    assertProductEqual("dto-pair", expected, actual)
  }
}
