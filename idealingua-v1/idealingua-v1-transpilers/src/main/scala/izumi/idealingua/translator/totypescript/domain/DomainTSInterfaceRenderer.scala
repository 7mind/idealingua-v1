package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.{Generic, TypeId}
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.products.CogenProduct.InterfaceProduct
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Interface` as the same `InterfaceProduct` the
  * legacy `TypeScriptTranslator.renderInterface` produces (modulo the
  * extension chain).
  *
  * IMPL-7b Phase B M2: byte-parity port. Two emitted top-level shapes:
  *   - `export interface <Name> extends ... { ... }` plus
  *     `export interface <Name>StructSerialized extends ... { ... }`
  *   - `export class <Name>Struct implements <Name> { ... }` with the
  *     polymorphic registration helpers, plus the `.register(...)` calls
  *     against each transitively-inherited parent interface.
  *
  * Flattened struct comes from `Domain.flattenedStructs(i.id)`; `Super`
  * comes from `i.struct.superclasses`. The legacy `<Iface>Struct` impl id
  * is synthesized via `DomainTSStruct.implId(i.id)` matching legacy
  * `typespace.tools.implId(i.id)`.
  *
  * `parentsInherited(i.id)` comes from the legacy `ts.inheritance` for
  * order-preserving M2 parity — the new IR `Domain.parents` set has the
  * same membership but no defined order; using legacy threading keeps M2
  * focused on the renderer-shape port without surfacing ordering invariants.
  *
  * Extension chain (`handleInterface`) is omitted: default TS extensions
  * (`EnumHelpersExtension`, `IntrospectionExtension`) have no
  * `handleInterface` override.
  */
final class DomainTSInterfaceRenderer(ctx: DomainTSContext) {

  import ctx._

  def renderInterface(i: NewTypeDef.Interface, ts: Typespace): InterfaceProduct = {
    val legacyDef = ts.domain.types.find(_.id == i.id).get
    val imports   = TypeScriptImports(ts, legacyDef, i.id.path.toPackage, manifest = options.manifest)

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
         |${uniqueInterfaces.map(sc => sc.name + DomainTSStruct.implId(sc).name + s".register($eid.FullClassName, $eid);").mkString("\n")}
       """.stripMargin

    InterfaceProduct(iface, companion, imports.render(ts), s"// ${i.id.name} Interface")
  }

  private def renderSerializedObject(fields: List[izumi.idealingua.model.il.ast.typed.Field], ts: Typespace): String = {
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
