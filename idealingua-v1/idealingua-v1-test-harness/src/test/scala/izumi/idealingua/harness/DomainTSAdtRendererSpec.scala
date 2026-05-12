package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.{AdtId, DTOId}
import izumi.idealingua.model.common.{DomainId, Primitive, TypePath}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.TypeDef.*
import izumi.idealingua.model.il.ast.typed.{AdtMember, DomainDefinition, DomainMetadata, Field, NodeMeta, Structure, Super, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId.{AliasId, IdentifierId, InterfaceId}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.TypeScriptBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.AdtProduct
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M3: byte-parity unit test for `DomainTSAdtRenderer`.
  *
  * Asserts that the Domain-consuming `DomainTSAdtRenderer` produces the same
  * pre-extension `AdtProduct` (`content`, `header`, `preamble`) as the
  * legacy `TypeScriptTranslator.renderAdt` (mirror, since the legacy method
  * is `protected`), given matching new-IR / legacy-IR inputs and an empty
  * extension list.
  */
final class DomainTSAdtRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "ts_adt_render_spec")
  private val typePath   = TypePath(domainId, Seq.empty)
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)
  private val conv       = new izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter()

  // Mirror of legacy `TypeScriptTranslator.renderAdt` + `renderAdtImpl`
  // (protected). Verified by inspection against
  // `TypeScriptTranslator.scala:260-448`.
  private def legacyRender(i: LegacyTypeDef.Adt, ts: Typespace): AdtProduct = {
    val imports = TypeScriptImports(ts, i, i.id.path.toPackage, manifest = tsManifest)
    val base    = renderAdtImpl(i.id.name, i.alternatives, ts)
    AdtProduct(base, imports.render(ts), s"// ${i.id.name} Algebraic Data Type")
  }

  private def adtHasAdt(alternatives: List[AdtMember]): Boolean =
    alternatives.exists(al => al.typeId.isInstanceOf[AdtId])

  private def renderAdtImpl(name: String, alternatives: List[AdtMember], ts: Typespace, exported: Boolean = true): String = {
    val hasInterfaces = alternatives.count(al => al.typeId.isInstanceOf[InterfaceId]) > 0

    s"""${if (exported) "export " else ""}type $name = ${alternatives.map(alt => conv.toNativeType(alt.typeId, ts)).mkString(" | ")};
       |${if (exported) "export " else ""}type ${name}Serialized = ${alternatives
        .map(alt => conv.toNativeType(alt.typeId, ts, forSerialized = true)).mkString(" | ")}
       |
       |${if (exported) "export " else ""}class ${name}Helpers {
       |    public static isInstanceOf(o: any): boolean {
       |        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
       |            return false;
       |        }
       |        ${if (hasInterfaces) "const fullClassName = o.getFullClassName();" else ""}
       |        return ${alternatives
        .map(
          alt =>
            if (alt.typeId.isInstanceOf[InterfaceId])
              s"${alt.typeId.name}${ts.tools.implId(alt.typeId.asInstanceOf[InterfaceId]).name}.isRegisteredType(fullClassName)"
            else if (alt.typeId.isInstanceOf[AdtId]) s"${alt.typeId.name}Helpers.isInstanceOf(o)"
            else "o instanceof " + conv.toNativeType(alt.typeId, ts)
        ).mkString(" || ")};
       |    }
       |
       |    public static serialize(adt: $name): {[key: string]: ${alternatives
        .map(
          alt =>
            alt.typeId match {
              case interfaceId: InterfaceId => alt.typeId.name + ts.tools.implId(interfaceId).name + "Serialized"
              case al: AliasId => {
                val dealiased = ts.dealias(al)
                dealiased match {
                  case _: IdentifierId => "string"
                  case _               => dealiased.name + "Serialized"
                }
              }
              case _: IdentifierId => "string"
              case _               => alt.typeId.name + "Serialized"
            }
        ).mkString(" | ")}} {
       |        let className = adt.getClassName();
       |        ${if (hasInterfaces) "const fullClassName = adt.getFullClassName();" else ""}
       |        ${if (adtHasAdt(alternatives) || hasInterfaces) "let serialized: any = undefined;" else ""}
       |${alternatives
        .filter(al => al.typeId.isInstanceOf[AdtId]).map(al => al.typeId.asInstanceOf[AdtId]).map(
          adtId =>
            s"if (${adtId.name}Helpers.isInstanceOf(adt)) {\n    className = '${adtId.name}';\n    serialized = ${adtId.name}Helpers.serialize(adt as ${adtId.name});\n}"
        ).mkString(" else \n").shift(8)}
       |${alternatives
        .filter(al => al.typeId.isInstanceOf[InterfaceId]).map(al => al.typeId.asInstanceOf[InterfaceId]).map(
          interfaceId =>
            s"if (${interfaceId.name}${ts.tools.implId(interfaceId).name}.isRegisteredType(fullClassName)) {\n    className = '${interfaceId.name}'; serialized = {[fullClassName]: adt.serialize()};\n}"
        ).mkString(" else \n").shift(8)}
       |${alternatives
        .filter(al => al.memberName.isDefined).map(a => s"if (className == '${a.typeId.name}') {\n    className = '${a.memberName.get}'\n}").mkString("\n").shift(8)}
       |        return {
       |            [className]: ${if (adtHasAdt(alternatives) || hasInterfaces) "serialized || " else ""}adt.serialize()
       |        };
       |    }
       |
       |    public static deserialize(data: {[key: string]: ${alternatives
        .map(
          alt =>
            alt.typeId match {
              case interfaceId: InterfaceId => alt.typeId.name + ts.tools.implId(interfaceId).name + "Serialized"
              case al: AliasId => {
                val dealiased = ts.dealias(al)
                dealiased match {
                  case _: IdentifierId => "string"
                  case _               => dealiased.name + "Serialized"
                }
              }
              case _: IdentifierId => "string"
              case _               => alt.typeId.name + "Serialized"
            }
        ).mkString(" | ")}}): $name {
       |        const id = Object.keys(data)[0];
       |        const content = (data as any)[id];
       |        switch (id) {
       |${alternatives.map(a => "case '" + a.wireId + "': return " + conv.deserializeType("content", a.typeId, ts, asAny = true) + ";").mkString("\n").shift(12)}
       |            default:
       |                throw new Error('Unknown type id ' + id + ' for $name');
       |        }
       |    }
       |}
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

  private def legacyTypespaceFor(domainId: DomainId, types: Seq[LegacyTypeDef]): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = types,
      services   = Seq.empty,
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def assertProductEqual(label: String, expected: AdtProduct, actual: AdtProduct): Unit = {
    val _ = assert(expected.content == actual.content,
      s"$label: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(expected.header == actual.header, s"$label: header diverges")
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges")
  }

  test("ADT with two DTO branches: byte-equal to legacy") {
    val branchADtoId = DTOId(typePath, "BranchA")
    val branchBDtoId = DTOId(typePath, "BranchB")
    val branchAField = Field(Primitive.TString, "a", emptyMeta)
    val branchBField = Field(Primitive.TInt32, "b", emptyMeta)
    val adtId        = AdtId(typePath, "Choice")
    val members = List(
      AdtMember(branchADtoId, None, emptyMeta),
      AdtMember(branchBDtoId, None, emptyMeta),
    )
    val newAdt = NewTypeDef.Adt(adtId, members, emptyMeta)
    val legacyAdt = LegacyTypeDef.Adt(adtId, members, emptyMeta)

    val branchA: LegacyTypeDef = LegacyTypeDef.DTO(branchADtoId, Structure(List(branchAField), List.empty, Super.empty), emptyMeta)
    val branchB: LegacyTypeDef = LegacyTypeDef.DTO(branchBDtoId, Structure(List(branchBField), List.empty, Super.empty), emptyMeta)
    val ts = legacyTypespaceFor(domainId, Seq(legacyAdt, branchA, branchB))

    val ctxNew   = newCtxFor(domainId)
    val actual   = ctxNew.adtRenderer.renderAdt(newAdt, ts)
    val expected = legacyRender(legacyAdt, ts)

    assertProductEqual("adt-two-dto", expected, actual)
  }

  test("ADT with renamed branch (memberName.isDefined): byte-equal to legacy") {
    val branchADtoId = DTOId(typePath, "BranchA")
    val branchBDtoId = DTOId(typePath, "BranchB")
    val branchAField = Field(Primitive.TString, "a", emptyMeta)
    val branchBField = Field(Primitive.TInt32, "b", emptyMeta)
    val adtId        = AdtId(typePath, "Sum")
    val members = List(
      AdtMember(branchADtoId, Some("First"), emptyMeta),
      AdtMember(branchBDtoId, Some("Second"), emptyMeta),
    )
    val newAdt = NewTypeDef.Adt(adtId, members, emptyMeta)
    val legacyAdt = LegacyTypeDef.Adt(adtId, members, emptyMeta)

    val branchA: LegacyTypeDef = LegacyTypeDef.DTO(branchADtoId, Structure(List(branchAField), List.empty, Super.empty), emptyMeta)
    val branchB: LegacyTypeDef = LegacyTypeDef.DTO(branchBDtoId, Structure(List(branchBField), List.empty, Super.empty), emptyMeta)
    val ts = legacyTypespaceFor(domainId, Seq(legacyAdt, branchA, branchB))

    val ctxNew   = newCtxFor(domainId)
    val actual   = ctxNew.adtRenderer.renderAdt(newAdt, ts)
    val expected = legacyRender(legacyAdt, ts)

    assertProductEqual("adt-renamed-branches", expected, actual)
  }
}
