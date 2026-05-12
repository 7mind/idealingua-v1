package izumi.idealingua.translator.totypescript.domain.extensions

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.*
import izumi.idealingua.model.common.{Generic, Primitive, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.model.publishing.manifests.TypeScriptProjectLayout
import izumi.idealingua.translator.CompilerOptions.TypescriptTranslatorOptions
import izumi.idealingua.translator.totypescript.domain.DomainTSStruct
import izumi.idealingua.translator.totypescript.products.CogenProduct.*
import izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** PR-02 IMPL-7b Phase B M4: new-IR port of `IntrospectionExtension`.
  *
  * Emits the wire-format-bridging `Introspector.register(...)` registration
  * block appended to each `EnumProduct` / `IdentifierProduct.identifierInterface` /
  * `CompositeProduct.more` / `InterfaceProduct.companion` / `AdtProduct.content`
  * produced by the corresponding new-IR renderers.
  *
  * Reads only the new IR's `Domain` (`flattenedStructs`, `aliases`) plus the
  * `TypeDef` being rendered, the `TypescriptTranslatorOptions` (for the
  * IRT-import-path derivation), and the `TypeScriptTypeConverter` (for the
  * `safeName` mapping used in DTO field accessors). No `Typespace` lookup.
  *
  * Alias resolution: legacy `ts.dealias(al)` is replaced by a single lookup
  * in `Domain.aliases`. By Phase 3 (`AliasDealiaser`) every `AliasId` maps
  * directly to its fully-dealiased target `TypeId`, so no recursion is
  * needed; if an alias is somehow missing from the map the renderer falls
  * back to the alias itself (which the legacy converter would have
  * `wireId`-rendered identically).
  *
  * Interface impl-DTO companion id: legacy `ts.tools.implId(iid)` is replaced
  * by `DomainTSStruct.implId(iid) = DTOId(iid, "Struct")` — structurally
  * equivalent.
  *
  * Output is byte-equal to the legacy `IntrospectionExtension` for the same
  * inputs (verified by `DomainTSIntrospectionExtensionSpec`).
  */
object DomainTSIntrospectionExtension {

  private def unwindType(domain: Domain, id: TypeId): String = id match {
    case Primitive.TBool   => "{intro: IntrospectorTypes.Bool}"
    case Primitive.TString => "{intro: IntrospectorTypes.Str}"
    case Primitive.TInt8   => "{intro: IntrospectorTypes.I08}"
    case Primitive.TInt16  => "{intro: IntrospectorTypes.I16}"
    case Primitive.TInt32  => "{intro: IntrospectorTypes.I32}"
    case Primitive.TInt64  => "{intro: IntrospectorTypes.I64}"
    case Primitive.TUInt8  => "{intro: IntrospectorTypes.U08}"
    case Primitive.TUInt16 => "{intro: IntrospectorTypes.U16}"
    case Primitive.TUInt32 => "{intro: IntrospectorTypes.U32}"
    case Primitive.TUInt64 => "{intro: IntrospectorTypes.U64}"
    case Primitive.TFloat  => "{intro: IntrospectorTypes.F32}"
    case Primitive.TDouble => "{intro: IntrospectorTypes.F64}"
    case Primitive.TUUID   => "{intro: IntrospectorTypes.Uid}"
    case Primitive.TBLOB   => ???
    case Primitive.TTime   => "{intro: IntrospectorTypes.Time}"
    case Primitive.TDate   => "{intro: IntrospectorTypes.Date}"
    case Primitive.TTs     => "{intro: IntrospectorTypes.Tsl}"
    case Primitive.TTsTz   => "{intro: IntrospectorTypes.Tsz}"
    case Primitive.TTsU    => "{intro: IntrospectorTypes.Tsu}"
    case g: Generic =>
      g match {
        case gm: Generic.TMap    => s"{intro: IntrospectorTypes.Map, key: ${unwindType(domain, gm.keyType)}, value: ${unwindType(domain, gm.valueType)}} as IIntrospectorMapType"
        case gl: Generic.TList   => s"{intro: IntrospectorTypes.List, value: ${unwindType(domain, gl.valueType)}} as IIntrospectorGenericType"
        case gs: Generic.TSet    => s"{intro: IntrospectorTypes.Set, value: ${unwindType(domain, gs.valueType)}} as IIntrospectorGenericType"
        case go: Generic.TOption => s"{intro: IntrospectorTypes.Opt, value: ${unwindType(domain, go.valueType)}} as IIntrospectorGenericType"
      }
    case id: DTOId        => s"{intro: IntrospectorTypes.Data, full: '${id.path.toPackage.mkString(".") + "." + id.name}'} as IIntrospectorUserType"
    case id: InterfaceId  => s"{intro: IntrospectorTypes.Mixin, full: '${id.path.toPackage.mkString(".") + "." + id.name}'} as IIntrospectorUserType"
    case id: AdtId        => s"{intro: IntrospectorTypes.Adt, full: '${id.path.toPackage.mkString(".") + "." + id.name}'} as IIntrospectorUserType"
    case id: EnumId       => s"{intro: IntrospectorTypes.Enum, full: '${id.path.toPackage.mkString(".") + "." + id.name}'} as IIntrospectorUserType"
    case id: IdentifierId => s"{intro: IntrospectorTypes.Id, full: '${id.path.toPackage.mkString(".") + "." + id.name}'} as IIntrospectorUserType"
    case al: AliasId      => unwindType(domain, domain.aliases.getOrElse(al, al))
    case other            => throw new Exception(s"Unwind type is not implemented for type $other")
  }

  private def unwindField(domain: Domain, conv: TypeScriptTypeConverter, name: String, id: TypeId): String = {
    s"""{
       |    name: '$name',
       |    accessName: '${conv.safeName(name)}',
       |    type: ${unwindType(domain, id)}
       |}""".stripMargin
  }

  private def unwindAdtMember(domain: Domain, name: String, id: TypeId): String = {
    s"""{
       |    name: '$name',
       |    type: ${unwindType(domain, id)}
       |}""".stripMargin
  }

  private def irtImportPath(options: TypescriptTranslatorOptions, id: TypeId): String = {
    if (options.manifest.layout == TypeScriptProjectLayout.YARN) {
      s"${options.manifest.yarn.scope}/irt"
    } else {
      id.path.toPackage.map(_ => "..").mkString("/") + "/irt"
    }
  }

  def handleEnum(options: TypescriptTranslatorOptions, enumeration: NewTypeDef.Enum, product: EnumProduct): EnumProduct = {
    val pkg   = enumeration.id.path.toPackage.mkString(".")
    val short = enumeration.id.name
    val full  = pkg + "." + short
    val extension =
      s"""
         |// Introspector registration
         |import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '${irtImportPath(options, enumeration.id)}';
         |Introspector.register('$full', {
         |        full: '$full',
         |        short: '$short',
         |        package: '$pkg',
         |        type: IntrospectorTypes.Enum,
         |        options: ${short}Helpers.all
         |    } as IIntrospectorEnumObject
         |);
       """.stripMargin

    EnumProduct(product.content + extension, product.preamble)
  }

  def handleIdentifier(
    domain: Domain,
    options: TypescriptTranslatorOptions,
    conv: TypeScriptTypeConverter,
    identifier: NewTypeDef.Identifier,
    product: IdentifierProduct,
  ): IdentifierProduct = {
    val short = identifier.id.name
    val fields: List[IdField] = identifier.fields
    val extension =
      s"""
         |// Introspector registration
         |import {
         |    Introspector,
         |    IntrospectorTypes,
         |    IIntrospectorUserType,
         |    IIntrospectorGenericType,
         |    IIntrospectorMapType,
         |    IIntrospectorIdObject
         |} from '${irtImportPath(options, identifier.id)}';
         |Introspector.register($short.FullClassName, {
         |        full: $short.FullClassName,
         |        short: $short.ClassName,
         |        package: $short.PackageName,
         |        type: IntrospectorTypes.Id,
         |        ctor: () => new $short(),
         |        fields: [
         |${fields.map(f => unwindField(domain, conv, f.name, f.typeId)).mkString(",\n").shift(12)}
         |        ]
         |    } as IIntrospectorIdObject
         |);
       """.stripMargin

    // Mirror legacy `IntrospectionExtension.handleIdentifier` exactly: it
    // calls the 3-arg `IdentifierProduct(...)` constructor, which means the
    // `preamble` slot drops to its default `""` rather than being preserved
    // from the input product. Match that behaviour for byte parity.
    IdentifierProduct(product.identitier, product.identifierInterface + extension, product.header)
  }

  private def renderDTOIntrospector(domain: Domain, conv: TypeScriptTypeConverter, name: String, fields: Iterable[Field]): String = {
    s"""Introspector.register($name.FullClassName, {
       |        full: $name.FullClassName,
       |        short: $name.ClassName,
       |        package: $name.PackageName,
       |        type: IntrospectorTypes.Data,
       |        ctor: () => new $name(),
       |        fields: [
       |${fields.map(f => unwindField(domain, conv, f.name, f.typeId)).mkString(",\n").shift(12)}
       |        ]
       |    } as IIntrospectorDataObject
       |);
     """.stripMargin
  }

  /** Project the new-IR flat struct down to the same `List[Field]` shape the
    * legacy `ts.structure.structure(dto.id).all.distinctBy(_.field.name).map(_.field)`
    * walk produces. `FlatStruct.fields` is already in resolved inheritance
    * order (legacy `StructuralQueriesImpl.scala:41`), so `distinctBy(_.name)`
    * preserves the first occurrence per name, matching the legacy semantics.
    */
  private def flatDtoFields(domain: Domain, dtoId: DTOId): List[Field] = {
    val flat = domain.flattenedStructs.getOrElse(dtoId, FlatStruct(dtoId, List.empty, List.empty, List.empty))
    flat.fields.map(_.field).distinctBy(_.name)
  }

  def handleDTO(
    domain: Domain,
    options: TypescriptTranslatorOptions,
    conv: TypeScriptTypeConverter,
    dto: NewTypeDef.Dto,
    product: CompositeProduct,
  ): CompositeProduct = {
    val short  = dto.id.name
    val fields = flatDtoFields(domain, dto.id)
    val extension =
      s"""
         |// Introspector registration
         |import {
         |    Introspector,
         |    IntrospectorTypes,
         |    IIntrospectorUserType,
         |    IIntrospectorGenericType,
         |    IIntrospectorMapType,
         |    IIntrospectorDataObject
         |} from '${irtImportPath(options, dto.id)}';
         |${renderDTOIntrospector(domain, conv, short, fields)}
       """.stripMargin

    CompositeProduct(product.more + extension, product.header, product.preamble)
  }

  def handleInterface(
    domain: Domain,
    options: TypescriptTranslatorOptions,
    conv: TypeScriptTypeConverter,
    interface: NewTypeDef.Interface,
    product: InterfaceProduct,
  ): InterfaceProduct = {
    val short  = interface.id.name
    val pkg    = interface.id.path.toPackage.mkString(".")
    val full   = s"$pkg.$short"
    val fields = interface.struct.fields
    val implId = DomainTSStruct.implId(interface.id)
    val eid    = interface.id.name + implId.name

    val extension =
      s"""
         |// Introspector registration
         |import {
         |    Introspector,
         |    IntrospectorTypes,
         |    IIntrospectorUserType,
         |    IIntrospectorGenericType,
         |    IIntrospectorMapType,
         |    IIntrospectorMixinObject,
         |    IIntrospectorDataObject
         |} from '${irtImportPath(options, interface.id)}';
         |Introspector.register('$full', {
         |        full: '$full',
         |        short: '$short',
         |        package: '$pkg',
         |        type: IntrospectorTypes.Mixin,
         |        ctor: () => new $eid(),
         |        fields: [
         |${fields.map(f => unwindField(domain, conv, f.name, f.typeId)).mkString(",\n").shift(12)}
         |        ],
         |        implementations: $eid.getRegisteredTypes
         |    } as IIntrospectorMixinObject
         |);
         |${renderDTOIntrospector(domain, conv, eid, fields)}
       """.stripMargin

    InterfaceProduct(product.iface, product.companion + extension, product.header, product.preamble)
  }

  def handleAdt(
    options: TypescriptTranslatorOptions,
    domain: Domain,
    adt: NewTypeDef.Adt,
    product: AdtProduct,
  ): AdtProduct = {
    val pkg   = adt.id.path.toPackage.mkString(".")
    val short = adt.id.name
    val full  = pkg + "." + short

    val extension =
      s"""
         |// Introspector registration
         |import {
         |    Introspector,
         |    IntrospectorTypes,
         |    IIntrospectorUserType,
         |    IIntrospectorGenericType,
         |    IIntrospectorMapType,
         |    IIntrospectorAdtObject
         |} from '${irtImportPath(options, adt.id)}';
         |Introspector.register('$full', {
         |        full: '$full',
         |        short: '$short',
         |        package: '$pkg',
         |        type: IntrospectorTypes.Adt,
         |        options: [
         |${adt.alternatives.map(f => unwindAdtMember(domain, f.wireId, f.typeId)).mkString(",\n").shift(12)}
         |        ]
         |    } as IIntrospectorAdtObject
         |);
       """.stripMargin

    AdtProduct(product.content + extension, product.header, product.preamble)
  }
}
