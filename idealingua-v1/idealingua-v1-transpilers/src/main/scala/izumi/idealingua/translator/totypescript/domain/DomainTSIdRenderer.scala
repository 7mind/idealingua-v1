package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.ExtendedField
import izumi.idealingua.model.common.TypeId.IdentifierId
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.translator.totypescript.products.CogenProduct.IdentifierProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Identifier` as the same `IdentifierProduct` the
  * legacy `TypeScriptTranslator.renderIdentifier` produces (modulo the
  * extension chain).
  *
  * F-TextTree M2: ported to the M1.5 typed-renderer protocol. Type
  * references inside the emitted identifier-interface field list flow
  * through the tree as `TextTree.value(TSRefHandle.TypeRef(...))` nodes;
  * the surrounding scaffolding (constructor body, runtime-name block,
  * `toString` / `serialize` methods) is composed via `q"..."` with the
  * existing converter helpers (`toFieldMember` / `toFieldMethods` /
  * `parseTypeFromString` / `deserializeType` / `emitTypeAsString`) kept as
  * plain-text interpolations — those helpers compose multiple converter
  * calls into a single emitted string and modelling each one as a
  * distinct `TSRefHandle` case would explode the witness surface without
  * harvest benefit. The end-of-pipe `.mapRender(resolver.resolve)` call
  * resolves all `TypeRef` nodes via `DomainTSTypeConverter.toNativeType`.
  */
final class DomainTSIdRenderer(ctx: DomainTSContext) {

  import ctx._

  private val resolver = new DomainTSTypeResolver(conv)

  def renderIdentifier(i: NewTypeDef.Identifier): IdentifierProduct = {
    val typeName = i.id.name

    val imports = DomainTSImports.forTypeDef(i, i.id.path.toPackage, ctx.domain, options.manifest)

    val fields: List[ExtendedField] = i.fields.map(idFieldToExtendedField(i.id, _))
    val sortedFields = fields.sortBy(_.field.name)

    val ifaceFields: TextTree[TSRefHandle] =
      fields.map { f =>
        val nm = conv.toNativeTypeName(conv.safeName(f.field.name), f.field.typeId)
        q"$nm: ${TextTree.value[TSRefHandle](TSRefHandle.TypeRef(f.field.typeId))};"
      }.joinN()

    val identifierInterface: TextTree[TSRefHandle] =
      q"""export interface I$typeName {
         |    getPackageName(): string;
         |    getClassName(): string;
         |    getFullClassName(): string;
         |    serialize(): string;
         |
         |${ifaceFields.shift(4)}
         |}
         """.stripMargin

    val runtimeNames = renderRuntimeNames(i.id, typeName)

    val members      = fields.map(f => conv.toFieldMember(f.field)).mkString("\n").shift(4)
    val methods      = fields.map(f => conv.toFieldMethods(f.field)).mkString("\n").shift(4)
    val parseFromStr = sortedFields.zipWithIndex.map {
      case (sf, index) => s"this.${conv.safeName(sf.field.name)} = ${conv.parseTypeFromString(s"decodeURIComponent(parts[$index])", sf.field.typeId)};"
    }.mkString("\n").shift(12)
    val fromObject = fields
      .map(f => s"this.${conv.safeName(f.field.name)} = ${conv.deserializeType("data." + conv.safeName(f.field.name), f.field.typeId)};").mkString("\n").shift(12)
    val toStringSuffix = sortedFields
      .map(sf => "encodeURIComponent(" + conv.emitTypeAsString(s"this.${conv.safeName(sf.field.name)}", sf.field.typeId) + ")").mkString(" + ':' + ")

    val identifier: TextTree[TSRefHandle] =
      q"""export class $typeName implements I$typeName {
         |${runtimeNames.shift(4)}
         |$members
         |
         |$methods
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
         |$parseFromStr
         |        } else {
         |$fromObject
         |        }
         |    }
         |
         |    public toString(): string {
         |        const suffix = $toStringSuffix;
         |        return '$typeName#' + suffix;
         |    }
         |
         |    public serialize(): string {
         |        return this.toString();
         |    }
         |}
         """.stripMargin

    IdentifierProduct(
      identifier.mapRender(resolver.resolve),
      identifierInterface.mapRender(resolver.resolve),
      imports.render,
      s"// ${i.id.name} Identifier",
    )
  }

  /** Mirror of legacy `TypeScriptTranslator.renderRuntimeNames(TypeId, String)`
    * (`TypeScriptTranslator.scala:91-102`). The legacy method is `protected`;
    * the rendered output is fully determined by the inputs.
    */
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

  /** Widen an `IdField` to a `Field` and wrap in an `ExtendedField` carrying
    * the identifier's own id as `definedBy`/`usedBy` (distance 0).
    */
  private def idFieldToExtendedField(owner: IdentifierId, idf: IdField): ExtendedField = {
    val widened = Field(idf.typeId, idf.name, idf.meta)
    ExtendedField(
      field = widened,
      defn  = izumi.idealingua.model.common.FieldDef(
        definedBy        = owner,
        definedWithIndex = 0,
        usedBy           = owner,
        distance         = 0,
      ),
    )
  }
}
