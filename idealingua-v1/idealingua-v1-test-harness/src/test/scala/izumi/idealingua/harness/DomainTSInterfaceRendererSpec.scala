package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.InterfaceId
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
import izumi.idealingua.translator.totypescript.products.CogenProduct.InterfaceProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, FlatField, FlatStruct, Struct => NewStruct, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M2: byte-parity unit test for
  * `DomainTSInterfaceRenderer`.
  *
  * Asserts that the Domain-consuming `DomainTSInterfaceRenderer` produces
  * the same pre-extension `InterfaceProduct` (`iface`, `companion`,
  * `header`, `preamble`) as the legacy
  * `TypeScriptTranslator.renderInterface` (mirror, since the legacy method
  * is `protected`), given matching new-IR / legacy-IR inputs and an empty
  * extension list.
  */
final class DomainTSInterfaceRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "ts_iface_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)
  private val conv       = new izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter()

  // Mirror of legacy `TypeScriptTranslator.renderInterface` (protected).
  // Verified by inspection against `TypeScriptTranslator.scala:536-635`.
  private def legacyRender(i: LegacyTypeDef.Interface, ts: Typespace): InterfaceProduct = {
    val imports = TypeScriptImports(ts, i, i.id.path.toPackage, manifest = tsManifest)
    val extendsInterfaces =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "extends " + i.struct.superclasses.interfaces.map(iface => iface.name).mkString(", ") + " "
      } else ""

    val extendsInterfacesSerialized =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "extends " + i.struct.superclasses.interfaces.map(iface => iface.name + ts.tools.implId(iface).name + "Serialized").mkString(", ") + " "
      } else ""

    val fields         = ts.structure.structure(i)
    val distinctFields = fields.all.distinctBy(_.field.name).map(_.field)
    val implId         = ts.tools.implId(i.id)
    val eid            = i.id.name + implId.name

    val iface =
      s"""export interface ${i.id.name} $extendsInterfaces{
         |    getPackageName(): string;
         |    getClassName(): string;
         |    getFullClassName(): string;
         |    serialize(): ${eid}Serialized;
         |
         |${fields.all
          .map(f => s"${conv.toNativeTypeName(conv.safeName(f.field.name), f.field.typeId)}: ${conv.toNativeType(f.field.typeId, ts)};").mkString("\n").shift(4)}
         |}
         |
         |export interface ${eid}Serialized $extendsInterfacesSerialized{
         |${fields.all
          .map(f => s"${conv.toNativeTypeName(f.field.name, f.field.typeId)}: ${conv.toNativeType(f.field.typeId, ts, forSerialized = true)};").mkString("\n").shift(4)}
         |}
       """.stripMargin

    val uniqueInterfaces = ts.inheritance.parentsInherited(i.id).distinctBy(_.name)
    val companion =
      s"""export class $eid implements ${i.id.name} {
         |${renderRuntimeNames(implId, eid).shift(4)}
         |${fields.all.map(f => conv.toFieldMember(f.field, ts)).mkString("\n").shift(4)}
         |
         |${fields.all.map(f => conv.toFieldMethods(f.field, ts)).mkString("\n").shift(4)}
         |    constructor(data: ${eid}Serialized = undefined) {
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
         |    public serialize(): ${eid}Serialized {
         |        return {
         |${renderSerializedObject(distinctFields.toList, ts).shift(12)}
         |        };
         |    }
         |
         |    // Polymorphic section below. If a new type to be registered, use $eid.register method
         |    // which will add it to the known list. You can also overwrite the existing registrations
         |    // in order to provide extended functionality on existing models, preserving the original class name.
         |
         |    private static _knownPolymorphic: {[key: string]: {new (data?: $eid| ${eid}Serialized): ${i.id.name}}} = {
         |        // This basic registration will happen below [$eid.FullClassName]: $eid
         |    };
         |
         |    public static register(className: string, ctor: {new (data?: $eid| ${eid}Serialized): ${i.id.name}}): void {
         |        this._knownPolymorphic[className] = ctor;
         |    }
         |
         |    public static create(data: {[key: string]: ${eid}Serialized}): ${i.id.name} {
         |        const polymorphicId = Object.keys(data)[0];
         |        const ctor = $eid._knownPolymorphic[polymorphicId];
         |        if (!ctor) {
         |          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for $eid.Create');
         |        }
         |
         |        return new ctor(data[polymorphicId]);
         |    }
         |
         |    public static getRegisteredTypes(): string[] {
         |        return Object.keys($eid._knownPolymorphic);
         |    }
         |
         |    public static isRegisteredType(key: string): boolean {
         |        return key in $eid._knownPolymorphic;
         |    }
         |}
         |
         |${uniqueInterfaces.map(sc => sc.name + ts.tools.implId(sc).name + s".register($eid.FullClassName, $eid);").mkString("\n")}
       """.stripMargin

    InterfaceProduct(iface, companion, imports.render(ts), s"// ${i.id.name} Interface")
  }

  private def renderSerializedObject(fields: List[Field], ts: Typespace): String = {
    val serialized = fields.map(f => conv.serializeField(f, ts))
    val it         = serialized.iterator
    it.map(m => s"$m${if (it.hasNext) "," else ""}").mkString("\n")
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

  private def newCtxFor(domainId: DomainId, ifaceId: InterfaceId, fields: List[Field]): DomainTSContext = {
    val flat = FlatStruct(
      ownerId       = ifaceId,
      fields        = fields.map(f => FlatField(f, ifaceId, 0)),
      conflictsHard = List.empty,
      conflictsSoft = List.empty,
    )
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set(ifaceId: TypeId),
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map(ifaceId -> flat),
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

  private def assertProductEqual(label: String, expected: InterfaceProduct, actual: InterfaceProduct): Unit = {
    val _ = assert(expected.iface == actual.iface,
      s"$label: iface body diverges\nlegacy=${expected.iface}\nnew   =${actual.iface}")
    val _ = assert(expected.companion == actual.companion,
      s"$label: companion diverges\nlegacy=${expected.companion}\nnew   =${actual.companion}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges")
  }

  test("interface with single field: byte-equal to legacy") {
    val ifaceId = InterfaceId(typePath, "Named")
    val fields  = List(Field(Primitive.TString, "name", emptyMeta))
    val ctxNew  = newCtxFor(domainId, ifaceId, fields)

    val newIface = NewTypeDef.Interface(
      id     = ifaceId,
      struct = NewStruct(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val legacyIface = LegacyTypeDef.Interface(
      id     = ifaceId,
      struct = Structure(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val ts = legacyTypespaceFor(domainId, legacyIface)

    val actual   = ctxNew.interfaceRenderer.renderInterface(newIface, ts)
    val expected = legacyRender(legacyIface, ts)

    assertProductEqual("iface-single-field", expected, actual)
  }

  test("interface with multiple fields: byte-equal and declaration-order-preserving") {
    val ifaceId = InterfaceId(typePath, "Multi")
    val fields = List(
      Field(Primitive.TString, "a", emptyMeta),
      Field(Primitive.TInt32, "b", emptyMeta),
    )
    val ctxNew = newCtxFor(domainId, ifaceId, fields)

    val newIface = NewTypeDef.Interface(
      id     = ifaceId,
      struct = NewStruct(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val legacyIface = LegacyTypeDef.Interface(
      id     = ifaceId,
      struct = Structure(fields = fields, removedFields = List.empty, superclasses = Super.empty),
      meta   = emptyMeta,
    )
    val ts = legacyTypespaceFor(domainId, legacyIface)

    val actual   = ctxNew.interfaceRenderer.renderInterface(newIface, ts)
    val expected = legacyRender(legacyIface, ts)

    assertProductEqual("iface-multi-field", expected, actual)
  }
}
