package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.ExtendedField
import izumi.idealingua.model.common.TypeId.IdentifierId
import izumi.idealingua.model.il.ast.typed.{Field, IdField}
import izumi.idealingua.translator.totypescript.products.CogenProduct.IdentifierProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Identifier` as the same `IdentifierProduct` the
  * legacy `TypeScriptTranslator.renderIdentifier` produces (modulo the
  * extension chain).
  *
  * IMPL-7b Phase B M2: byte-parity port. Identifiers carry no inheritance
  * (`TypeDef.Identifier.fields: List[IdField]`); the legacy renderer's
  * `typespace.structure.structure(i)` call returns the widened id-field set
  * directly, so we widen `IdField → Field` and synthesize the
  * `ExtendedField` shape the legacy converter helpers expect.
  *
  * `Typespace` is threaded per-call for two reasons:
  *   1. `TypeScriptTypeConverter` requires it on every type-rendering call.
  *   2. `TypeScriptImports.apply(ts, definition, ...)` walks the legacy
  *      typespace; rather than land a `DomainTSImports` shim at M2, we
  *      reuse the legacy plumbing exactly as Phase A delegation does.
  *
  * Production path remains unchanged — `DomainTypeScriptTranslator.translate()`
  * still goes through Phase A delegation. This renderer is exercised only
  * by the M2 byte-parity unit test until M6 swaps the production path.
  *
  * Extension chain (`ctx.ext.extend(i, IdentifierProduct(...), _.handleIdentifier)`)
  * is omitted: the default TS extension set (`EnumHelpersExtension`,
  * `IntrospectionExtension`) has no `handleIdentifier` override, so the
  * pre-extension product is byte-equal to the post-extension product for
  * the default extension list.
  */
final class DomainTSIdRenderer(ctx: DomainTSContext) {

  import ctx._

  def renderIdentifier(i: NewTypeDef.Identifier): IdentifierProduct = {
    val typeName = i.id.name

    // IMPL-7b/7c-post: imports are computed from `Domain` via `DomainTSImports`,
    // removing the last legacy `TypeScriptImports.apply(ts, ...)` call on the
    // identifier path. IMPL-10-prep-Ts1: the `ts: Typespace` parameter has
    // been removed from the converter (`DomainTSTypeConverter`) and from this
    // renderer; all type-rendering reads `Domain` directly.
    val imports = DomainTSImports.forTypeDef(i, i.id.path.toPackage, ctx.domain, options.manifest)

    val fields: List[ExtendedField] = i.fields.map(idFieldToExtendedField(i.id, _))
    val sortedFields = fields.sortBy(_.field.name)

    val identifierInterface =
      s"""export interface I$typeName {
         |    getPackageName(): string;
         |    getClassName(): string;
         |    getFullClassName(): string;
         |    serialize(): string;
         |
         |${fields
          .map(f => s"${conv.toNativeTypeName(conv.safeName(f.field.name), f.field.typeId)}: ${conv.toNativeType(f.field.typeId)};").mkString("\n").shift(4)}
         |}
         """.stripMargin

    val identifier =
      s"""export class $typeName implements I$typeName {
         |${renderRuntimeNames(i.id, typeName).shift(4)}
         |${fields.map(f => conv.toFieldMember(f.field)).mkString("\n").shift(4)}
         |
         |${fields.map(f => conv.toFieldMethods(f.field)).mkString("\n").shift(4)}
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
         |${fields
          .map(f => s"this.${conv.safeName(f.field.name)} = ${conv.deserializeType("data." + conv.safeName(f.field.name), f.field.typeId)};").mkString(
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

    IdentifierProduct(identifier, identifierInterface, imports.render, s"// ${i.id.name} Identifier")
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
