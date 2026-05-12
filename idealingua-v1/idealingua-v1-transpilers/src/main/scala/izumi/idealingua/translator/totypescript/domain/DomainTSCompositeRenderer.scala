package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.{Generic, TypeId}
import izumi.idealingua.model.common.TypeId.{InterfaceId, AliasId}
import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.translator.totypescript.products.CogenProduct.CompositeProduct
import izumi.idealingua.typer.ir.{FlatStruct, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Dto` as the same `CompositeProduct` the legacy
  * `TypeScriptTranslator.renderDto` produces (modulo the extension chain).
  *
  * IMPL-7b Phase B M2: byte-parity port. DTOs may carry inherited fields
  * via `i.struct.superclasses.interfaces`; we project the flattened struct
  * from `Domain.flattenedStructs(i.id)` and re-derive the legacy `Struct`
  * shape via `DomainTSStruct.fromFlat` (same sort key + dedup as legacy
  * `StructuralQueriesImpl`, language-agnostic).
  *
  * Cross-references the interface impl id (`<Iface>Struct`) for interface
  * serializer/loader and the static `.register(...)` calls; we use
  * `DomainTSStruct.implId(iid)` which is structurally equivalent to
  * `typespace.tools.implId(iid)` (`DTOId(iid, "Struct")`).
  *
  * "Unique interfaces" (the set of interface ancestors used for
  * `to<Iface>Serialized` / `load<Iface>` helpers and `.register(...)` calls)
  * is mirrored via the legacy `ts.inheritance.parentsInherited(i.id)`
  * call. The new IR equivalent is `Domain.parents`, but the order matters
  * for byte parity. At M2 we re-use the legacy `Typespace` for the parents
  * query — same Typespace already used by the converter for imports.
  * M3+ can swap to `Domain.parents` once the ordering invariant is verified.
  *
  * Extension chain (`ctx.ext.extend(i, CompositeProduct(...), _.handleDTO)`)
  * is omitted: the default TS extension set
  * (`EnumHelpersExtension`, `IntrospectionExtension`) has no `handleDTO`
  * override, so the pre-extension product is byte-equal to the
  * post-extension product for the default extension list.
  */
final class DomainTSCompositeRenderer(ctx: DomainTSContext) {

  import ctx._

  def renderDto(i: NewTypeDef.Dto): CompositeProduct = {
    val imports = DomainTSImports.forTypeDef(i, i.id.path.toPackage, ctx.domain, options.manifest)

    val flat = ctx.domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val legacyStruct   = DomainTSStruct.fromFlat(i.id, flat, i.struct.superclasses, ctx.domain)
    val fields         = legacyStruct.all
    val distinctFields = fields.distinctBy(_.field.name).map(_.field)

    val implementsInterfaces =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "implements " + i.struct.superclasses.interfaces.map(iface => iface.name).mkString(", ") + " "
      } else {
        ""
      }

    val extendsInterfacesSerialized =
      if (i.struct.superclasses.interfaces.nonEmpty) {
        "extends " + i.struct.superclasses.interfaces.map(iface => s"${iface.name}${DomainTSStruct.implId(iface).name}Serialized").mkString(", ") + " "
      } else {
        ""
      }

    val uniqueInterfaces = DomainTSStruct.parentsInherited(ctx.domain, i.id).distinctBy(_.name)

    val dto =
      s"""export class ${i.id.name} $implementsInterfaces {
         |${renderRuntimeNames(i.id, i.id.name).shift(4)}
         |${distinctFields.map(f => conv.toFieldMember(f)).mkString("\n").shift(4)}
         |
         |${distinctFields.map(f => conv.toFieldMethods(f)).mkString("\n").shift(4)}
         |    constructor(data: ${i.id.name}Serialized = undefined) {
         |        if (typeof data === 'undefined' || data === null) {
         |${distinctFields
          .map(f => renderDefaultAssign(conv.deserializeName("this." + conv.safeName(f.name), f.typeId), f.typeId)).filterNot(_.isEmpty).mkString("\n").shift(12)}
         |            return;
         |        }
         |
         |${distinctFields
          .map(f => s"${conv.deserializeName("this." + conv.safeName(f.name), f.typeId)} = ${conv.deserializeType("data." + f.name, f.typeId)};").mkString(
            "\n"
          ).shift(8)}
         |    }
         |
         |${uniqueInterfaces.map(si => renderDtoInterfaceSerializer(si)).mkString("\n").shift(4)}
         |${uniqueInterfaces.map(si => renderDtoInterfaceLoader(si)).mkString("\n").shift(4)}
         |    public serialize(): ${i.id.name}Serialized {
         |        return {
         |${renderSerializedObject(distinctFields.toList).shift(12)}
         |        };
         |    }
         |}
         |
         |export interface ${i.id.name}Serialized $extendsInterfacesSerialized {
         |${distinctFields.map(f => s"${conv.toNativeTypeName(f.name, f.typeId)}: ${conv.toNativeType(f.typeId, forSerialized = true)};").mkString("\n").shift(4)}
         |}
         |
         |${uniqueInterfaces.map(sc => sc.name + DomainTSStruct.implId(sc).name + s".register(${i.id.name}.FullClassName, ${i.id.name});").mkString("\n")}
         """.stripMargin

    CompositeProduct(dto, imports.render, s"// ${i.id.name} DTO")
  }

  private def renderDtoInterfaceSerializer(iid: InterfaceId): String = {
    val fields = DomainTSStruct.structureOf(ctx.domain, iid)
    val implN  = DomainTSStruct.implId(iid).name
    s"""public to${iid.name}Serialized(): ${iid.name}${implN}Serialized {
       |    return {
       |${renderSerializedObject(fields.all.map(_.field)).shift(8)}
       |    };
       |}
       |
       |public to${iid.name}(): ${iid.name}$implN {
       |    return new ${iid.name}$implN(this.to${iid.name}Serialized());
       |}
     """.stripMargin
  }

  private def renderDtoInterfaceLoader(iid: InterfaceId): String = {
    val fields = DomainTSStruct.structureOf(ctx.domain, iid)
    val implN  = DomainTSStruct.implId(iid).name
    s"""public load${iid.name}Serialized(slice: ${iid.name}${implN}Serialized) {
       |${renderDeserializeObject(fields.all.map(_.field)).shift(4)}
       |}
       |
       |public load${iid.name}(slice: ${iid.name}$implN) {
       |    this.load${iid.name}Serialized(slice.serialize());
       |}
     """.stripMargin
  }

  private def renderSerializedObject(fields: List[Field]): String = {
    val serialized = fields.map(f => conv.serializeField(f))
    val it         = serialized.iterator
    it.map(m => s"$m${if (it.hasNext) "," else ""}").mkString("\n")
  }

  private def renderDeserializeObject(fields: List[Field]): String = {
    fields.map(f => conv.deserializeField(f)).mkString("\n")
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

  /** Mirror of legacy `TypeScriptTranslator.renderRuntimeNames(TypeId, String)`
    * (`TypeScriptTranslator.scala:91-102`). */
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
