package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.*
import izumi.idealingua.model.il.ast.typed.AdtMember
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.products.CogenProduct.AdtProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Adt` as the same `AdtProduct` the legacy
  * `TypeScriptTranslator.renderAdt` produces (modulo the extension chain).
  *
  * IMPL-7b Phase B M3: byte-parity port. The emitted top-level shape is
  * the standard ADT triple:
  *   - `export type <Name> = A | B | ...`
  *   - `export type <Name>Serialized = ASerialized | BSerialized | ...`
  *   - `export class <Name>Helpers { isInstanceOf, serialize, deserialize }`
  *
  * Per-branch helpers (interface impl-id resolution via `ts.tools.implId`,
  * alias dealiasing via `ts.dealias`) keep the legacy `Typespace` threaded
  * per-call — the new IR carries enough state for these queries, but
  * delegating to `Typespace` keeps M3 focused on shape parity without
  * re-deriving the dealias / impl-id rules in TS form.
  *
  * Extension chain (`ctx.ext.extend(i, AdtProduct(...), _.handleAdt)`) is
  * omitted: this renderer emits the pre-extension product, matching the
  * convention from M1 / M2. The default TS extension list does include
  * `IntrospectionExtension.handleAdt`, but the byte-parity unit test
  * compares against the legacy renderer with the SAME empty extension list.
  */
final class DomainTSAdtRenderer(ctx: DomainTSContext) {

  import ctx._

  def renderAdt(i: NewTypeDef.Adt, ts: Typespace): AdtProduct = {
    val legacyDef = ts.domain.types.find(_.id == i.id).get
    val imports   = TypeScriptImports(ts, legacyDef, i.id.path.toPackage, manifest = options.manifest)
    val base      = renderAdtImpl(i.id.name, i.alternatives, ts, exported = true)

    AdtProduct(
      base,
      imports.render(ts),
      s"// ${i.id.name} Algebraic Data Type",
    )
  }

  /** Mirror of legacy `TypeScriptTranslator.renderAdtImpl` (lines 363-448).
    * Public so `DomainTSServiceMethodProduct` can reuse it for nested ADT
    * outputs (`Algebraic`) in service methods.
    */
  def renderAdtImpl(name: String, alternatives: List[AdtMember], ts: Typespace, exported: Boolean = true): String = {
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

  private def adtHasAdt(alternatives: List[AdtMember]): Boolean =
    alternatives.exists(al => al.typeId.isInstanceOf[AdtId])
}
