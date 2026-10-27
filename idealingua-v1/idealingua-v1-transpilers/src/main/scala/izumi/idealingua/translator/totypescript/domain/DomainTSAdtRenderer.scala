package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.Primitive
import izumi.idealingua.model.common.TypeId.*
import izumi.idealingua.model.il.ast.typed.AdtMember
import izumi.idealingua.translator.totypescript.products.CogenProduct.AdtProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Adt` as the same `AdtProduct` the legacy
  * `TypeScriptTranslator.renderAdt` produces (modulo the extension chain).
  *
  * F-TextTree M2 — ported to the M1.5 typed-renderer protocol. The two
  * top-level type-union lines (`export type <Name> = A | B | ...` and
  * `export type <Name>Serialized = ASerialized | BSerialized | ...`)
  * carry alternative `TypeId`s as `TextTree.value(TSRefHandle.TypeRef(...))`
  * and `TextTree.value(TSRefHandle.SerializedTypeRef(...))` (where
  * applicable), so the harvest pathway can recover the precise reference
  * set for the import section. The `<Name>Helpers` companion class body
  * stays as String-level composition: per-branch dispatch (interface
  * impl-id resolution, alias dealiasing) requires conditional shape
  * decisions that are clearer expressed as plain Scala.
  */
final class DomainTSAdtRenderer(ctx: DomainTSContext) {

  import ctx._

  private val resolver = new DomainTSTypeResolver(conv)

  def renderAdt(i: NewTypeDef.Adt): AdtProduct = {
    val imports = DomainTSImports.forTypeDef(i, i.id.path.toPackage, ctx.domain, options.manifest)
    val base    = renderAdtImplTree(i.id.name, i.alternatives, exported = true).mapRender(resolver.resolve)

    AdtProduct(
      base,
      imports.render,
      s"// ${i.id.name} Algebraic Data Type",
    )
  }

  /** Public string-typed gate; used by `DomainTSServiceMethodProduct` for
    * nested ADT outputs (`Algebraic`). Internally resolves the tree.
    */
  def renderAdtImpl(name: String, alternatives: List[AdtMember], exported: Boolean = true): String =
    renderAdtImplTree(name, alternatives, exported).mapRender(resolver.resolve)

  /** Tree-typed core: composes the ADT body as `TextTree[TSRefHandle]` so
    * the alternative `TypeId`s flow through the harvest pathway.
    */
  def renderAdtImplTree(name: String, alternatives: List[AdtMember], exported: Boolean = true): TextTree[TSRefHandle] = {
    val hasInterfaces = alternatives.count(al => al.typeId.isInstanceOf[InterfaceId]) > 0
    val exp           = if (exported) "export " else ""

    val primitives: Map[AdtMember, Primitive] = alternatives.flatMap(alt => primitiveOf(alt).map(alt -> _)).toMap
    def wrapperName(alt: AdtMember): String = primitiveWrapperName(name, alt)

    val nativeUnion: TextTree[TSRefHandle] =
      alternatives.map(alt =>
        if (primitives.contains(alt)) TextTree.text[TSRefHandle](wrapperName(alt))
        else TextTree.value[TSRefHandle](TSRefHandle.TypeRef(alt.typeId))
      ).join(" | ")

    val nativeSerializedUnion: TextTree[TSRefHandle] =
      alternatives.map(alt =>
        primitives.get(alt) match {
          case Some(p) => TextTree.text[TSRefHandle](conv.toNativeType(p, forSerialized = true))
          case None    => TextTree.value[TSRefHandle](TSRefHandle.SerializedTypeRef(alt.typeId))
        }
      ).join(" | ")

    val wrappers: String =
      primitives.toList.sortBy { case (alt, _) => alternatives.indexOf(alt) }.map {
        case (alt, p) =>
          // Exported even for a non-exported ADT: the alternatives of an
          // inline method output appear in the exported service signatures.
          s"""export class ${wrapperName(alt)} {
             |    public constructor(public readonly value: ${conv.toNativeType(p)}) {
             |    }
             |
             |    public getClassName(): string {
             |        return '${alt.typeId.name}';
             |    }
             |
             |    public serialize(): ${conv.toNativeType(p, forSerialized = true)} {
             |        return ${conv.serializeValue("this.value", p, nonMember = true)};
             |    }
             |}
             |""".stripMargin + "\n"
      }.mkString

    val isInstanceOfChecks: String =
      alternatives.map(alt =>
        if (primitives.contains(alt)) s"o instanceof ${wrapperName(alt)}"
        else if (alt.typeId.isInstanceOf[InterfaceId])
          s"${alt.typeId.name}${DomainTSStruct.implId(alt.typeId.asInstanceOf[InterfaceId]).name}.isRegisteredType(fullClassName)"
        else if (alt.typeId.isInstanceOf[AdtId]) s"${alt.typeId.name}Helpers.isInstanceOf(o)"
        else "o instanceof " + conv.toNativeType(alt.typeId)
      ).mkString(" || ")

    val serializeReturn: String =
      alternatives.map(alt =>
        alt.typeId match {
          case _ if primitives.contains(alt) => conv.toNativeType(primitives(alt), forSerialized = true)
          case interfaceId: InterfaceId => alt.typeId.name + DomainTSStruct.implId(interfaceId).name + "Serialized"
          case al: AliasId => {
            val dealiased = DomainTSImports.dealias(ctx.domain, al)
            dealiased match {
              case _: IdentifierId => "string"
              case _               => dealiased.name + "Serialized"
            }
          }
          case _: IdentifierId => "string"
          case _               => alt.typeId.name + "Serialized"
        }
      ).mkString(" | ")

    val fullClassDecl     = if (hasInterfaces) "const fullClassName = o.getFullClassName();" else ""
    val fullClassDeclAdt  = if (hasInterfaces) "const fullClassName = adt.getFullClassName();" else ""
    val needsSerializedTmp = adtHasAdt(alternatives) || hasInterfaces
    val serializedDecl    = if (needsSerializedTmp) "let serialized: any = undefined;" else ""

    val adtBranches: String =
      alternatives.filter(al => al.typeId.isInstanceOf[AdtId]).map(al => al.typeId.asInstanceOf[AdtId]).map(adtId =>
        s"if (${adtId.name}Helpers.isInstanceOf(adt)) {\n    className = '${adtId.name}';\n    serialized = ${adtId.name}Helpers.serialize(adt as ${adtId.name});\n}"
      ).mkString(" else \n").shift(8)

    val ifaceBranches: String =
      alternatives.filter(al => al.typeId.isInstanceOf[InterfaceId]).map(al => al.typeId.asInstanceOf[InterfaceId]).map(interfaceId =>
        s"if (${interfaceId.name}${DomainTSStruct.implId(interfaceId).name}.isRegisteredType(fullClassName)) {\n    className = '${interfaceId.name}'; serialized = {[fullClassName]: adt.serialize()};\n}"
      ).mkString(" else \n").shift(8)

    val memberNameBranches: String =
      alternatives.filter(al => al.memberName.isDefined).map(a => s"if (className == '${a.typeId.name}') {\n    className = '${a.memberName.get}'\n}").mkString("\n").shift(8)

    val returnSerialized = if (needsSerializedTmp) "serialized || " else ""

    val cases: String =
      alternatives.map { a =>
        val value = primitives.get(a) match {
          case Some(p) => s"new ${wrapperName(a)}(${deserializePrimitive("content", p)})"
          case None    => conv.deserializeType("content", a.typeId, asAny = true)
        }
        "case '" + a.wireId + "': return " + value + ";"
      }.mkString("\n").shift(12)

    q"""$wrappers${exp}type $name = $nativeUnion;
       |${exp}type ${name}Serialized = $nativeSerializedUnion
       |
       |${exp}class ${name}Helpers {
       |    public static isInstanceOf(o: any): boolean {
       |        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
       |            return false;
       |        }
       |        $fullClassDecl
       |        return $isInstanceOfChecks;
       |    }
       |
       |    public static serialize(adt: $name): {[key: string]: $serializeReturn} {
       |        let className = adt.getClassName();
       |        $fullClassDeclAdt
       |        $serializedDecl
       |$adtBranches
       |$ifaceBranches
       |$memberNameBranches
       |        return {
       |            [className]: ${returnSerialized}adt.serialize()
       |        };
       |    }
       |
       |    public static deserialize(data: {[key: string]: $serializeReturn}): $name {
       |        const id = Object.keys(data)[0];
       |        const content = (data as any)[id];
       |        switch (id) {
       |$cases
       |            default:
       |                throw new Error('Unknown type id ' + id + ' for $name');
       |        }
       |    }
       |}
     """.stripMargin
  }

  /** The TypeScript type of the values of an alternative. A primitive value
    * carries no class name, so a primitive alternative is carried by a
    * generated wrapper class (as the Scala and C# ADTs wrap every
    * alternative); its wire form is the primitive's own.
    */
  def alternativeNativeType(adtName: String, alt: AdtMember): String =
    if (primitiveOf(alt).isDefined) primitiveWrapperName(adtName, alt) else conv.toNativeType(alt.typeId)

  private def primitiveWrapperName(adtName: String, alt: AdtMember): String = s"${adtName}_${alt.typename}"

  private def primitiveOf(alt: AdtMember): Option[Primitive] =
    DomainTSImports.dealias(ctx.domain, alt.typeId) match {
      case p: Primitive => Some(p)
      case _            => None
    }

  /** The inverse of `serializeValue(_, p, nonMember = true)`. */
  private def deserializePrimitive(variable: String, p: Primitive): String = p match {
    case Primitive.TTime => s"Formatter.readTime($variable)"
    case Primitive.TDate => s"Formatter.readDate($variable)"
    case Primitive.TTs   => s"Formatter.readLocalDateTime($variable)"
    case Primitive.TTsTz => s"Formatter.readZoneDateTime($variable)"
    case Primitive.TTsU  => s"Formatter.readUTCDateTime($variable)"
    case _               => conv.deserializeType(variable, p, asAny = true)
  }

  private def adtHasAdt(alternatives: List[AdtMember]): Boolean =
    alternatives.exists(al => al.typeId.isInstanceOf[AdtId])
}
