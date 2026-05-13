package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.{Generic, TypeId}
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.translator.totypescript.products.CogenProduct.InterfaceProduct
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Interface` as the same `InterfaceProduct` the
  * legacy `TypeScriptTranslator.renderInterface` produces (modulo the
  * extension chain).
  *
  * F-TextTree M2 — ported to the M1.5 typed-renderer protocol. Type
  * references in the emitted interface and serialized-interface field
  * lists flow through the tree as
  * `TextTree.value(TSRefHandle.TypeRef(...))` (and the `Serialized`
  * variant). Everything else is composed with `q"..."` plus String
  * helpers.
  */
final class DomainTSInterfaceRenderer(ctx: DomainTSContext) {

  import ctx._

  private val resolver = new DomainTSTypeResolver(conv)

  def renderInterface(i: NewTypeDef.Interface): InterfaceProduct = {
    val imports = DomainTSImports.forTypeDef(i, i.id.path.toPackage, ctx.domain, options.manifest)

    val extendsInterfaces =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "extends " + i.struct.superclasses.interfaces.map(iface => iface.name).mkString(", ") + " "
      } else {
        ""
      }

    val extendsInterfacesSerialized =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "extends " + i.struct.superclasses.interfaces.map(iface => iface.name + DomainTSStruct.implId(iface).name + "Serialized").mkString(", ") + " "
      } else {
        ""
      }

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val legacyStruct   = DomainTSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val fields         = legacyStruct
    val distinctFields = fields.all.distinctBy(_.field.name).map(_.field)
    val implId         = DomainTSStruct.implId(i.id)
    val eid            = i.id.name + implId.name

    val ifaceFields: TextTree[TSRefHandle] =
      fields.all.map { f =>
        val nm = conv.toNativeTypeName(conv.safeName(f.field.name), f.field.typeId)
        q"$nm: ${TextTree.value[TSRefHandle](TSRefHandle.TypeRef(f.field.typeId))};"
      }.joinN()

    val ifaceSerializedFields: TextTree[TSRefHandle] =
      fields.all.map { f =>
        val nm = conv.toNativeTypeName(f.field.name, f.field.typeId)
        q"$nm: ${TextTree.value[TSRefHandle](TSRefHandle.SerializedTypeRef(f.field.typeId))};"
      }.joinN()

    val iface: TextTree[TSRefHandle] =
      q"""export interface ${i.id.name} $extendsInterfaces{
         |    getPackageName(): string;
         |    getClassName(): string;
         |    getFullClassName(): string;
         |    serialize(): ${eid}Serialized;
         |
         |${ifaceFields.shift(4)}
         |}
         |
         |export interface ${eid}Serialized $extendsInterfacesSerialized{
         |${ifaceSerializedFields.shift(4)}
         |}
       """.stripMargin

    val uniqueInterfaces = DomainTSStruct.parentsInherited(ctx.domain, i.id).distinctBy(_.name)

    val runtimeNames    = renderRuntimeNames(implId, eid).shift(4)
    val membersBlock    = fields.all.map(f => conv.toFieldMember(f.field)).mkString("\n").shift(4)
    val methodsBlock    = fields.all.map(f => conv.toFieldMethods(f.field)).mkString("\n").shift(4)
    val defaultAssigns  = distinctFields
      .map(f => renderDefaultAssign(conv.deserializeName("this." + conv.safeName(f.name), f.typeId), f.typeId)).filterNot(_.isEmpty).mkString("\n").shift(12)
    val fromObject      = distinctFields
      .map(f => s"${conv.deserializeName("this." + conv.safeName(f.name), f.typeId)} = ${conv.deserializeType("data." + f.name, f.typeId)};").mkString("\n").shift(8)
    val serializedBody  = renderSerializedObject(distinctFields.toList).shift(12)
    val registrations   = uniqueInterfaces.map(sc => sc.name + DomainTSStruct.implId(sc).name + s".register($eid.FullClassName, $eid);").mkString("\n")

    val companion: TextTree[TSRefHandle] =
      q"""export class $eid implements ${i.id.name} {
         |$runtimeNames
         |$membersBlock
         |
         |$methodsBlock
         |    constructor(data: ${eid}Serialized = undefined) {
         |        if (typeof data === 'undefined' || data === null) {
         |$defaultAssigns
         |            return;
         |        }
         |
         |$fromObject
         |    }
         |
         |    public serialize(): ${eid}Serialized {
         |        return {
         |$serializedBody
         |        };
         |    }
         |
         |    // Polymorphic section below. If a new type to be registered, use $eid.register method
         |    // which will add it to the known list. You can also overwrite the existing registrations
         |    // in order to provide extended functionality on existing models, preserving the original class name.
         |
         |    private static _knownPolymorphic: {[key: string]: {new (data?: ${eid + "| " + eid + "Serialized"}): ${i.id.name}}} = {
         |        // This basic registration will happen below [$eid.FullClassName]: $eid
         |    };
         |
         |    public static register(className: string, ctor: {new (data?: ${eid + "| " + eid + "Serialized"}): ${i.id.name}}): void {
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
         |$registrations
       """.stripMargin

    InterfaceProduct(iface.mapRender(resolver.resolve), companion.mapRender(resolver.resolve), imports.render, s"// ${i.id.name} Interface")
  }

  private def renderSerializedObject(fields: List[izumi.idealingua.model.il.ast.typed.Field]): String = {
    val serialized = fields.map(f => conv.serializeField(f))
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
    case _: AliasId => None
    case _          => None
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
}
