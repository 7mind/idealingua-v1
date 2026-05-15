package izumi.idealingua.translator.totypescript.domain.extensions

import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.TypeId.*
import izumi.idealingua.model.common.{Generic, Primitive, TypeId}
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.model.publishing.manifests.TypeScriptProjectLayout
import izumi.idealingua.translator.CompilerOptions.TypescriptTranslatorOptions
import izumi.idealingua.translator.totypescript.domain.{DomainTSStruct, DomainTSTypeConverter, TSRefHandle}
import izumi.idealingua.translator.totypescript.products.CogenProduct.*
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** PR-02 IMPL-7b Phase B M4: new-IR port of `IntrospectionExtension`.
  *
  * F-TextTree M2 — ported to the typed-renderer protocol. The
  * registration blocks themselves emit fully-qualified TS literals
  * (`'full.class.name'`, primitive descriptors), not native-TS type
  * references, so the harvest contribution is empty for this extension.
  * The protocol adoption is structural; the body composes through
  * `q"..."` and is rendered with a resolver-less mapRender (the
  * trees carry no `ValueNode`s) preserving byte parity. Per-call
  * resolver creation is kept inline so the extension can be invoked from
  * the legacy production path without DI changes.
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
    case Primitive.TBLOB   => "{intro: IntrospectorTypes.Blob}"
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

  private def unwindField(domain: Domain, conv: DomainTSTypeConverter, name: String, id: TypeId): String = {
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

  /** Render a tree that carries no `TSRefHandle` value nodes (this
    * extension's emitted blocks are all string-literal-based). A
    * fall-through resolver suffices.
    */
  private def render(tree: TextTree[TSRefHandle]): String =
    tree.mapRender(_ => "")

  def handleEnum(options: TypescriptTranslatorOptions, enumeration: NewTypeDef.Enum, product: EnumProduct): EnumProduct = {
    val pkg   = enumeration.id.path.toPackage.mkString(".")
    val short = enumeration.id.name
    val full  = pkg + "." + short
    val extension: TextTree[TSRefHandle] =
      q"""
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

    EnumProduct(product.content + render(extension), product.preamble)
  }

  def handleIdentifier(
    domain: Domain,
    options: TypescriptTranslatorOptions,
    conv: DomainTSTypeConverter,
    identifier: NewTypeDef.Identifier,
    product: IdentifierProduct,
  ): IdentifierProduct = {
    val short = identifier.id.name
    val fields: List[IdField] = identifier.fields
    val unwound = fields.map(f => unwindField(domain, conv, f.name, f.typeId)).mkString(",\n").shift(12)
    val extension: TextTree[TSRefHandle] =
      q"""
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
         |$unwound
         |        ]
         |    } as IIntrospectorIdObject
         |);
       """.stripMargin

    IdentifierProduct(product.identitier, product.identifierInterface + render(extension), product.header)
  }

  private def renderDTOIntrospector(domain: Domain, conv: DomainTSTypeConverter, name: String, fields: Iterable[Field]): String = {
    val unwound = fields.map(f => unwindField(domain, conv, f.name, f.typeId)).mkString(",\n").shift(12)
    val tree: TextTree[TSRefHandle] =
      q"""Introspector.register($name.FullClassName, {
         |        full: $name.FullClassName,
         |        short: $name.ClassName,
         |        package: $name.PackageName,
         |        type: IntrospectorTypes.Data,
         |        ctor: () => new $name(),
         |        fields: [
         |$unwound
         |        ]
         |    } as IIntrospectorDataObject
         |);
       """.stripMargin
    render(tree)
  }

  private def flatDtoFields(domain: Domain, dtoId: DTOId): List[Field] = {
    val flat = domain.flattenedStructs.getOrElse(dtoId, FlatStruct(dtoId, List.empty, List.empty, List.empty))
    flat.fields.map(_.field).distinctBy(_.name)
  }

  def handleDTO(
    domain: Domain,
    options: TypescriptTranslatorOptions,
    conv: DomainTSTypeConverter,
    dto: NewTypeDef.Dto,
    product: CompositeProduct,
  ): CompositeProduct = {
    val short  = dto.id.name
    val fields = flatDtoFields(domain, dto.id)
    val introspector = renderDTOIntrospector(domain, conv, short, fields)
    val extension: TextTree[TSRefHandle] =
      q"""
         |// Introspector registration
         |import {
         |    Introspector,
         |    IntrospectorTypes,
         |    IIntrospectorUserType,
         |    IIntrospectorGenericType,
         |    IIntrospectorMapType,
         |    IIntrospectorDataObject
         |} from '${irtImportPath(options, dto.id)}';
         |$introspector
       """.stripMargin

    CompositeProduct(product.more + render(extension), product.header, product.preamble)
  }

  def handleInterface(
    domain: Domain,
    options: TypescriptTranslatorOptions,
    conv: DomainTSTypeConverter,
    interface: NewTypeDef.Interface,
    product: InterfaceProduct,
  ): InterfaceProduct = {
    val short  = interface.id.name
    val pkg    = interface.id.path.toPackage.mkString(".")
    val full   = s"$pkg.$short"
    val fields = interface.struct.fields
    val implId = DomainTSStruct.implId(interface.id)
    val eid    = interface.id.name + implId.name

    val unwound      = fields.map(f => unwindField(domain, conv, f.name, f.typeId)).mkString(",\n").shift(12)
    val introspector = renderDTOIntrospector(domain, conv, eid, fields)

    val extension: TextTree[TSRefHandle] =
      q"""
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
         |$unwound
         |        ],
         |        implementations: $eid.getRegisteredTypes
         |    } as IIntrospectorMixinObject
         |);
         |$introspector
       """.stripMargin

    InterfaceProduct(product.iface, product.companion + render(extension), product.header, product.preamble)
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

    val members = adt.alternatives.map(f => unwindAdtMember(domain, f.wireId, f.typeId)).mkString(",\n").shift(12)

    val extension: TextTree[TSRefHandle] =
      q"""
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
         |$members
         |        ]
         |    } as IIntrospectorAdtObject
         |);
       """.stripMargin

    AdtProduct(product.content + render(extension), product.header, product.preamble)
  }
}
