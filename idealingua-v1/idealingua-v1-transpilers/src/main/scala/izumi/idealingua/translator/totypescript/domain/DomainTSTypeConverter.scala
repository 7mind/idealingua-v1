package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.Generic.{TList, TMap, TSet}
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common._
import izumi.idealingua.model.il.ast.typed.Field
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Domain-consuming twin of `TypeScriptTypeConverter`.
  *
  * PR-02 IMPL-10-prep-Ts1: ports the legacy converter off `Typespace`. The
  * five legacy `ts.X` call sites collapse to Domain-direct lookups:
  *
  *   - `ts.dealias(al)` (1 site, in `deserializeCustomType`) →
  *     `domain.aliases.getOrElse(al, al)`. The new-typer `AliasDealiaser`
  *     (Phase 3) resolves every `AliasId` to a non-alias `TypeId` exactly
  *     once, so the recursive legacy chase collapses to a single lookup.
  *   - `ts(al).asInstanceOf[Alias].target` (3 sites, in `toCustomType`
  *     for-serialized branch and the non-serialized branch, plus
  *     `serializeCustom`) → `aliasTarget(al)` which reads
  *     `domain.userTypes(al)` and projects the new-IR
  *     `TypeDef.Alias.target`. Functionally identical to the legacy lookup;
  *     the result is the alias's declared target (potentially another
  *     alias before Phase 3, but in the new IR aliases are pre-dealiased).
  *   - `ts.tools.implId(i)` (1 site, in `toCustomType` for-serialized
  *     branch) → `DTOId(i, "Struct")`. Pure naming (legacy
  *     `TypespaceToolsImpl.toDtoName` returns the constant `"Struct"` for
  *     `InterfaceId`).
  *
  * All other methods are byte-equal copies of the legacy converter; only
  * the parameter list changes (no `ts: Typespace` per-call — the converter
  * carries `domain: Domain` as a field).
  */
final class DomainTSTypeConverter(domain: Domain) {

  def parseTypeFromString(value: String, target: TypeId): String = {
    target match {
      case Primitive.TBool   => "(" + value + " === 'true')"
      case Primitive.TString => value

      case Primitive.TInt8  => "parseInt(" + value + ", 10)"
      case Primitive.TInt16 => "parseInt(" + value + ", 10)"
      case Primitive.TInt32 => "parseInt(" + value + ", 10)"
      case Primitive.TInt64 => "parseInt(" + value + ", 10)"

      case Primitive.TUInt8  => "parseInt(" + value + ", 10)"
      case Primitive.TUInt16 => "parseInt(" + value + ", 10)"
      case Primitive.TUInt32 => "parseInt(" + value + ", 10)"
      case Primitive.TUInt64 => "parseInt(" + value + ", 10)"

      case Primitive.TFloat  => "parseFloat(" + value + ")"
      case Primitive.TDouble => "parseFloat(" + value + ")"
      case Primitive.TUUID   => value
      case Primitive.TBLOB   => value
      case Primitive.TTime   => "Date.parse(" + value + ")"
      case Primitive.TDate   => "Date.parse(" + value + ")"
      case Primitive.TTs     => "Date.parse(" + value + ")"
      case Primitive.TTsTz   => "Date.parse(" + value + ")"
      case Primitive.TTsU    => "Date.parse(" + value + ")"
      case id: IdentifierId  => s"new ${id.name}($value)"
      case en: EnumId        => s"${en.name}[$value as keyof typeof ${en.name}]"
      case _ => throw new Exception("Unsupported area in parseTypeFromString")
    }
  }

  def emitTypeAsString(value: String, target: TypeId): String = {
    target match {
      case Primitive.TBool   => "(" + value + " ? 'true' : 'false')"
      case Primitive.TString => value

      case Primitive.TInt8  => s"$value.toString()"
      case Primitive.TInt16 => s"$value.toString()"
      case Primitive.TInt32 => s"$value.toString()"
      case Primitive.TInt64 => s"$value.toString()"

      case Primitive.TUInt8  => s"$value.toString()"
      case Primitive.TUInt16 => s"$value.toString()"
      case Primitive.TUInt32 => s"$value.toString()"
      case Primitive.TUInt64 => s"$value.toString()"

      case Primitive.TFloat  => s"$value.toString()"
      case Primitive.TDouble => s"$value.toString()"
      case Primitive.TUUID   => value
      case Primitive.TBLOB   => value
      case _: IdentifierId   => s"$value.toString()"
      case en: EnumId        => s"${en.name}[$value]"
      case _ => throw new Exception("Unsupported area in emitTypeAsString")
    }
  }

  def deserializeName(name: String, target: TypeId): String = target match {
    case Primitive.TTime    => name + "AsString"
    case Primitive.TDate    => name + "AsString"
    case Primitive.TTs      => name + "AsString"
    case Primitive.TTsTz    => name + "AsString"
    case Primitive.TTsU     => name + "AsString"
    case o: Generic.TOption => deserializeName(name, o.valueType)
    case _                  => name
  }

  def deserializeType(variable: String, target: TypeId, asAny: Boolean = false): String = {
    target match {
      case Primitive.TBool   => variable
      case Primitive.TString => variable
      case Primitive.TInt8   => variable
      case Primitive.TInt16  => variable
      case Primitive.TInt32  => variable
      case Primitive.TInt64  => variable
      case Primitive.TUInt8  => variable
      case Primitive.TUInt16 => variable
      case Primitive.TUInt32 => variable
      case Primitive.TUInt64 => variable
      case Primitive.TFloat  => variable
      case Primitive.TDouble => variable
      case Primitive.TUUID   => variable
      case Primitive.TBLOB   => variable
      case Primitive.TTime   => variable
      case Primitive.TDate   => variable
      case Primitive.TTs     => variable
      case Primitive.TTsTz   => variable
      case Primitive.TTsU    => variable
      case g: Generic        => deserializeGenericType(variable, g, asAny)
      case _                 => deserializeCustomType(variable, target, asAny)
    }
  }

  def deserializeGenericType(variable: String, target: Generic, asAny: Boolean = false): String = target match {
    case gm: Generic.TMap =>
      s"Object.keys($variable).reduce<any>((previous, current) => {previous[current] = ${deserializeType(s"$variable[current as any]", gm.valueType, asAny)}; return previous; }, {})"
    case gl: Generic.TList =>
      gl.valueType match {
        case _: Primitive => s"$variable.slice()"
        case _            => s"$variable.map(${if (asAny) "(e: any)" else "e"} => { return ${deserializeType("e", gl.valueType, asAny)}; })"
      }
    case go: Generic.TOption => s"typeof $variable !== 'undefined' ? ${deserializeType(variable, go.valueType, asAny)} : undefined"
    case gs: Generic.TSet =>
      gs.valueType match {
        case _: Primitive => s"$variable.slice()"
        case _            => s"$variable.map(${if (asAny) "(e: any)" else "e"} => { return ${deserializeType("e", gs.valueType, asAny)}; })"
      }
  }

  def deserializeCustomType(variable: String, target: TypeId, asAny: Boolean = false): String = target match {
    case a: AdtId         => s"${a.name}Helpers.deserialize(${variable + (if (asAny) " as any" else "")})"
    case i: InterfaceId   => s"${i.name}Struct.create(${variable + (if (asAny) " as any" else "")})"
    case d: DTOId         => s"new ${d.name}(${variable + (if (asAny) " as any" else "")})"
    case al: AliasId      => deserializeType(variable, dealias(al), asAny)
    case id: IdentifierId => s"new ${id.name}(${variable + (if (asAny) " as any" else "")})"
    case en: EnumId       => s"${en.name}[$variable as keyof typeof ${en.name}]"

    case _ => s"'$variable: Error here! Not Implemented! ${target.name}'"
  }

  def toNativeType(id: TypeId, forSerialized: Boolean = false, forMap: Boolean = false): String = {
    id match {
      case t: Generic   => toGenericType(t, forSerialized)
      case t: Primitive => toPrimitiveType(t, forSerialized)
      case _            => toCustomType(id, forSerialized, forMap)
    }
  }

  def toNativeTypeName(name: String, id: TypeId): String = id match {
    case t: Generic =>
      t match {
        case _: Generic.TOption => name
        case _                  => name
      }
    case _ => name
  }

  def toCustomType(id: TypeId, forSerialized: Boolean = false, forMap: Boolean): String = {
    if (forSerialized) {
      id match {
        case i: InterfaceId  => s"{[key: string]: ${i.name + implIdName(i) + "Serialized"}}"
        case _: AdtId        => "{[key: string]: any}"
        case al: AliasId     => toNativeType(aliasTarget(al), forSerialized)
        case _: DTOId        => s"${id.name}Serialized"
        case _: EnumId       => "string"
        case _: IdentifierId => "string"
        case _               => s"${id.name}"
      }
    } else {
      id match {
        case al: AliasId                 => toNativeType(aliasTarget(al), forSerialized, forMap)
        case _: EnumId | _: IdentifierId => if (forMap) "string" else id.name
        case _                           => id.name
      }
    }
  }

  private def toPrimitiveType(id: Primitive, forSerialized: Boolean): String = id match {
    case Primitive.TBool   => "boolean"
    case Primitive.TString => "string"
    case Primitive.TInt8   => "number"
    case Primitive.TInt16  => "number"
    case Primitive.TInt32  => "number"
    case Primitive.TInt64  => "number"
    case Primitive.TUInt8  => "number"
    case Primitive.TUInt16 => "number"
    case Primitive.TUInt32 => "number"
    case Primitive.TUInt64 => "number"
    case Primitive.TFloat  => "number"
    case Primitive.TDouble => "number"
    case Primitive.TUUID   => "string"
    case Primitive.TBLOB   => "string"
    case Primitive.TTime   => if (forSerialized) "string" else "Date"
    case Primitive.TDate   => if (forSerialized) "string" else "Date"
    case Primitive.TTs     => if (forSerialized) "string" else "Date"
    case Primitive.TTsTz   => if (forSerialized) "string" else "Date"
    case Primitive.TTsU    => if (forSerialized) "string" else "Date"
  }

  private def toGenericType(typeId: Generic, forSerialized: Boolean): String = {
    // Wrap union types in parentheses before suffixing `[]`. TS precedence
    // parses `A | B[]` as `A | (B[])` whereas we want `(A | B)[]` for
    // `list[opt[T]]` / `set[opt[T]]`. Without the wrap, `set[opt[i32]]`
    // emits `number | undefined[]` which `tsc` rejects with TS2339.
    def parenIfUnion(s: String): String = if (s.contains(" | ")) s"($s)" else s
    typeId match {
      case _: Generic.TSet => parenIfUnion(toNativeType(typeId.asInstanceOf[TSet].valueType, forSerialized)) + "[]"
      case _: Generic.TMap =>
        "{[key: " + toNativeType(typeId.asInstanceOf[TMap].keyType, forSerialized, forMap = true) + "]: " + toNativeType(
          typeId.asInstanceOf[TMap].valueType,
          forSerialized,
        ) + "}"
      case _: Generic.TList   => parenIfUnion(toNativeType(typeId.asInstanceOf[TList].valueType, forSerialized)) + "[]"
      case o: Generic.TOption => toNativeType(o.valueType, forSerialized) + " | undefined"
    }
  }

  def serializeField(field: Field): String = {
    s"${field.name}: ${serializeValue("this." + safeName(field.name), field.typeId)}"
  }

  def deserializeField(field: Field): String = {
    s"${deserializeName("this." + safeName(field.name), field.typeId)} = ${deserializeType(s"slice.${field.name}", field.typeId)};"
  }

  def serializeValue(name: String, id: TypeId, nonMember: Boolean = false, asAny: Boolean = false): String = id match {
    case p: Primitive => serializePrimitive(name, p, nonMember)
    case _: Generic   => serializeGeneric(name, id, asAny)
    case _            => serializeCustom(name, id)
  }

  def serializePrimitive(name: String, id: Primitive, nonMember: Boolean = false): String = id match {
    case Primitive.TBool   => s"$name"
    case Primitive.TString => s"$name"
    case Primitive.TInt8   => s"$name"
    case Primitive.TInt16  => s"$name"
    case Primitive.TInt32  => s"$name"
    case Primitive.TInt64  => s"$name"
    case Primitive.TUInt8  => s"$name"
    case Primitive.TUInt16 => s"$name"
    case Primitive.TUInt32 => s"$name"
    case Primitive.TUInt64 => s"$name"
    case Primitive.TFloat  => s"$name"
    case Primitive.TDouble => s"$name"
    case Primitive.TUUID   => s"$name"
    case Primitive.TBLOB   => s"$name"
    case Primitive.TTime   => if (nonMember) s"Formatter.writeTime($name)" else s"${name}AsString";
    case Primitive.TDate   => if (nonMember) s"Formatter.writeDate($name)" else s"${name}AsString";
    case Primitive.TTs     => if (nonMember) s"Formatter.writeLocalDateTime($name)" else s"${name}AsString";
    case Primitive.TTsTz   => if (nonMember) s"Formatter.writeZoneDateTime($name)" else s"${name}AsString";
    case Primitive.TTsU    => if (nonMember) s"Formatter.writeUTCDateTime($name)" else s"${name}AsString";
  }

  def serializeGeneric(name: String, id: TypeId, asAny: Boolean = false): String = id match {
    case m: Generic.TMap =>
      s"Object.keys($name).reduce<any>((previous, current) => {previous[current] = ${serializeValue(s"$name[current as any]", m.valueType, nonMember = true)}; return previous; }, {})"
    case s: Generic.TSet =>
      s.valueType match {
        case _: Primitive => s"$name.slice()"
        case _            => s"$name.map(${if (asAny) "(e: any)" else "e"} => { return ${serializeValue("e", s.valueType, nonMember = true)}; })"
      }
    case l: Generic.TList =>
      l.valueType match {
        case _: Primitive => s"$name.slice()"
        case _            => s"$name.map(${if (asAny) "(e: any)" else "e"} => { return ${serializeValue("e", l.valueType, nonMember = true)}; })"
      }
    case o: Generic.TOption => s"typeof $name !== 'undefined' ? ${serializeValue(name, o.valueType)} : undefined"
    case _                  => s"$name: 'Error here! Not Implemented!'"
  }

  def serializeCustom(name: String, id: TypeId): String = id match {
    case a: AdtId        => s"${a.name}Helpers.serialize($name)"
    case _: InterfaceId  => s"{[$name.getFullClassName()]: $name.serialize()}"
    case _: DTOId        => s"$name.serialize()"
    case al: AliasId     => serializeValue(name, aliasTarget(al))
    case _: IdentifierId => s"$name.serialize()"
    case en: EnumId      => s"${en.name}[$name]"

    case _ => s"'$name: Error here! Not Implemented! ${id.name}'"
  }

  def toFieldMethods(id: TypeId, name: String, optional: Boolean = false): String = id match {
    case Primitive.TBool   => toBooleanField(name, optional)
    case Primitive.TString => toStringField(name, Int.MinValue, optional)
    case Primitive.TInt8   => toIntField(name, -128, 127, optional)
    case Primitive.TInt16  => toIntField(name, -32768, 32767, optional)
    case Primitive.TInt32  => toIntField(name, -2147483648, 2147483647, optional)
    case Primitive.TInt64  => toIntField(name, Int.MinValue, Int.MaxValue, optional)
    case Primitive.TUInt8  => toIntField(name, 0, 255, optional)
    case Primitive.TUInt16 => toIntField(name, 0, 65535, optional)
    case Primitive.TUInt32 => toIntField(name, 0, BigInt("4294967295"), optional)
    case Primitive.TUInt64 => toIntField(name, 0, BigInt("18446744073709551615"), optional)
    case Primitive.TFloat  => toDoubleField(name, 32, optional)
    case Primitive.TDouble => toDoubleField(name, 64, optional)
    case Primitive.TUUID   => toGuidField(name, optional)
    case Primitive.TBLOB   => toStringField(name, Int.MinValue, optional)
    case Primitive.TTime   => toTimeField(name, optional)
    case Primitive.TDate   => toDateField(name, optional)
    case Primitive.TTs     => toDateTimeField(name, local = true, optional)
    case Primitive.TTsTz   => toDateTimeField(name, local = false, optional)
    case Primitive.TTsU    => toDateTimeField(name, local = false, optional, utc = true)
    case _ =>
      s"""public get ${safeName(name)}(): ${toNativeType(id)}${if (optional) " | undefined" else ""} {
         |    return this._$name;
         |}
         |
         |public set ${safeName(name)}(value: ${toNativeType(id)}${if (optional) " | undefined" else ""}) {
         |    if (typeof value === 'undefined' || value === null) {
         |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
         |    }
         |    this._$name = value;
         |}
       """.stripMargin
  }

  def toFieldMethods(field: Field): String = field.typeId match {
    case go: Generic.TOption => toFieldMethods(go.valueType, field.name, optional = true)
    case _                   => toFieldMethods(field.typeId, field.name)
  }

  def toFieldMember(field: Field): String = {
    toFieldMember(field.name, field.typeId)
  }

  def toFieldMember(name: String, id: TypeId): String = id match {
    case Primitive.TBool   => toPrivateMember(name, "boolean")
    case Primitive.TString => toPrivateMember(name, "string")
    case Primitive.TInt8   => toPrivateMember(name, "number")
    case Primitive.TInt16  => toPrivateMember(name, "number")
    case Primitive.TInt32  => toPrivateMember(name, "number")
    case Primitive.TInt64  => toPrivateMember(name, "number")
    case Primitive.TUInt8  => toPrivateMember(name, "number")
    case Primitive.TUInt16 => toPrivateMember(name, "number")
    case Primitive.TUInt32 => toPrivateMember(name, "number")
    case Primitive.TUInt64 => toPrivateMember(name, "number")
    case Primitive.TFloat  => toPrivateMember(name, "number")
    case Primitive.TDouble => toPrivateMember(name, "number")
    case Primitive.TUUID   => toPrivateMember(name, "string")
    case Primitive.TBLOB   => toPrivateMember(name, "string")
    case Primitive.TTime   => toPrivateMember(name, "Date")
    case Primitive.TDate   => toPrivateMember(name, "Date")
    case Primitive.TTs     => toPrivateMember(name, "Date")
    case Primitive.TTsTz   => toPrivateMember(name, "Date")
    case Primitive.TTsU    => toPrivateMember(name, "Date")
    case _                 => toPrivateMember(name, toNativeType(id))
  }

  def safeName(name: String): String = {
    val ecma1 = Seq(
      "do",
      "if",
      "in",
      "for",
      "let",
      "new",
      "try",
      "var",
      "case",
      "else",
      "enum",
      "eval",
      "null",
      "as",
      "this",
      "true",
      "void",
      "with",
      "await",
      "break",
      "catch",
      "class",
      "const",
      "false",
      "super",
      "throw",
      "while",
      "yield",
      "delete",
      "export",
      "import",
      "public",
      "return",
      "static",
      "switch",
      "typeof",
      "default",
      "extends",
      "finally",
      "package",
      "private",
      "continue",
      "debugger",
      "function",
      "arguments",
      "interface",
      "protected",
      "any",
      "implements",
      "instanceof",
      "namespace",
      "boolean",
      "constructor",
      "declare",
      "get",
      "module",
      "require",
      "number",
      "set",
      "string",
      "symbol",
      "type",
      "from",
      "of",
    )

    if (ecma1.contains(name)) s"${name}_" else name
  }

  def toPrivateMember(name: String, memberType: String): String = {
    s"private _$name: $memberType;"
  }

  def toIntField(name: String, min: BigInt = Int.MinValue, max: BigInt = Int.MaxValue, optional: Boolean = false): String = {
    var base =
      s"""public get ${safeName(name)}(): number${if (optional) " | undefined" else ""} {
         |    return this._$name;
         |}
         |
         |public set ${safeName(name)}(value: number${if (optional) " | undefined" else ""}) {
         |    if (typeof value === 'undefined' || value === null) {
         |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
         |    }
         |
         |    if (typeof value !== 'number') {
         |        throw new Error('Field ${safeName(name)} expects type number, got ' + value);
         |    }
         |
         |    if (value % 1 !== 0) {
         |        throw new Error('Field ${safeName(name)} is expected to be an integer, got ' + value);
         |    }
       """

    if (min != Int.MinValue) {
      base +=
        s"""
           |    if (value < $min) {
           |        throw new Error('Field ${safeName(name)} is expected to be not less than $min, got ' + value);
           |    }
         """
    }

    if (max != Int.MaxValue) {
      base +=
        s"""
           |    if (value > $max) {
           |        throw new Error('Field ${safeName(name)} is expected to be not greater than $max, got ' + value);
           |    }
         """
    }

    base +=
      s"""
         |    this._$name = value;
         |}
       """
    base.stripMargin
  }

  def toDoubleField(name: String, precision: Int = 64, optional: Boolean = false): String = {
    s"""public get ${safeName(name)}(): number${if (optional) " | undefined" else ""} {
       |    // Precision: $precision
       |    return this._$name;
       |}
       |
       |public set ${safeName(name)}(value: number${if (optional) " | undefined" else ""}) {
       |    // Precision: $precision
       |    if (typeof value === 'undefined' || value === null) {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
       |    }
       |
       |    if (typeof value !== 'number') {
       |        throw new Error('Field ${safeName(name)} expects type number, got ' + value);
       |    }
       |
       |    this._$name = value;
       |}
     """.stripMargin
  }

  def toBooleanField(name: String, optional: Boolean = false): String = {
    s"""public get ${safeName(name)}(): boolean${if (optional) " | undefined" else ""} {
       |    return this._$name;
       |}
       |
       |public set ${safeName(name)}(value: boolean${if (optional) " | undefined" else ""}) {
       |    if (typeof value === 'undefined' || value === null) {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
       |    }
       |
       |    if (typeof value !== 'boolean') {
       |        throw new Error('Field ${safeName(name)} expects boolean type, got ' + value);
       |    }
       |
       |    this._$name = value;
       |}
     """.stripMargin
  }

  def toGuidField(name: String, optional: Boolean = false): String = {
    s"""public get ${safeName(name)}(): string${if (optional) " | undefined" else ""} {
       |    return this._$name;
       |}
       |
       |public set ${safeName(name)}(value: string${if (optional) " | undefined" else ""}) {
       |    if (typeof value === 'undefined' || value === null) {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
       |    }
       |
       |    if (typeof value !== 'string') {
       |        throw new Error('Field ${safeName(name)} expects type string, got ' + value);
       |    }
       |
       |    if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$$')) {
       |        throw new Error('Field ${safeName(name)} expects guid format, got ' + value);
       |    }
       |
       |    this._$name = value;
       |}
     """.stripMargin
  }

  def toStringField(name: String, max: Int = Int.MinValue, optional: Boolean = false): String = {
    var base =
      s"""public get ${safeName(name)}(): string${if (optional) " | undefined" else ""} {
         |    return this._$name;
         |}
         |
         |public set ${safeName(name)}(value: string${if (optional) " | undefined" else ""}) {
         |    if (typeof value === 'undefined' || value === null) {
         |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
         |    }
         |
         |    if (typeof value !== 'string') {
         |        throw new Error('Field ${safeName(name)} expects type string, got ' + value);
         |    }
     """

    if (max > 0) {
      base +=
        s"""
           |    if (value.length > $max) {
           |        value = value.substr(0, $max);
           |    }
         """
    }

    base +=
      s"""
         |    this._$name = value;
         |}
       """
    base.stripMargin
  }

  def toDateTimeField(name: String, local: Boolean, optional: Boolean = false, utc: Boolean = false): String = {
    s"""public get ${safeName(name)}(): Date${if (optional) " | undefined" else ""} {
       |    return this._$name;
       |}
       |
       |public set ${safeName(name)}(value: Date${if (optional) " | undefined" else ""}) {
       |    if (typeof value === 'undefined' || value === null) {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
       |    }
       |
       |    if (!(value instanceof Date)) {
       |        throw new Error('Field ${safeName(name)} expects type Date, got ' + value);
       |    }
       |    this._$name = value;
       |}
       |
       |public get ${safeName(name)}AsString(): string${if (optional) " | undefined" else ""} {
       |    ${if (optional) s"if (!this._$name) {\n        return undefined;\n    }" else ""}
       |    return Formatter.write${if (local) "Local" else if (utc) "UTC" else "Zone"}DateTime(this._$name);
       |}
       |
       |public set ${safeName(name)}AsString(value: string${if (optional) " | undefined" else ""}) {
       |    if (typeof value !== 'string') {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('${safeName(name)}AsString expects type string, got ' + value);"}
       |    }
       |    this._$name = Formatter.read${if (local) "Local" else if (utc) "UTC" else "Zone"}DateTime(value);
       |}
     """.stripMargin
  }

  def toDateField(name: String, optional: Boolean = false): String = {
    s"""public get ${safeName(name)}(): Date${if (optional) " | undefined" else ""} {
       |    return this._$name;
       |}
       |
       |public set ${safeName(name)}(value: Date${if (optional) " | undefined" else ""}) {
       |    if (typeof value === 'undefined' || value === null) {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
       |    }
       |
       |    if (!(value instanceof Date)) {
       |        throw new Error('Field ${safeName(name)} expects type Date, got ' + value);
       |    }
       |    this._$name = value;
       |}
       |
       |public get ${safeName(name)}AsString(): string${if (optional) " | undefined" else ""} {
       |    ${if (optional) s"if (!this._$name) {\n        return undefined;\n    }" else ""}
       |    return Formatter.writeDate(this._$name);
       |}
       |
       |public set ${safeName(name)}AsString(value: string${if (optional) " | undefined" else ""}) {
       |    if (typeof value !== 'string') {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('${safeName(name)}AsString expects type string, got ' + value);"}
       |    }
       |    this._$name = Formatter.readDate(value);
       |}
     """.stripMargin
  }

  def toTimeField(name: String, optional: Boolean = false): String = {
    s"""public get ${safeName(name)}(): Date${if (optional) " | undefined" else ""} {
       |    return this._$name;
       |}
       |
       |public set ${safeName(name)}(value: Date${if (optional) " | undefined" else ""}) {
       |    if (typeof value === 'undefined' || value === null) {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('Field ${safeName(name)} is not optional');"}
       |    }
       |
       |    if (!(value instanceof Date)) {
       |        throw new Error('Field ${safeName(name)} expects type Date, got ' + value);
       |    }
       |
       |    this._$name = value;
       |}
       |
       |public get ${safeName(name)}AsString(): string${if (optional) " | undefined" else ""} {
       |    ${if (optional) s"if (!this._$name) {\n        return undefined;\n    }" else ""}
       |    return Formatter.writeTime(this._$name);
       |}
       |
       |public set ${safeName(name)}AsString(value: string${if (optional) " | undefined" else ""}) {
       |    if (typeof value !== 'string') {
       |        ${if (optional) s"this._$name = undefined;\n        return;" else s"throw new Error('${safeName(name)}AsString expects type string, got ' + value);"}
       |    }
       |
       |    this._$name = Formatter.readTime(value);
       |}
     """.stripMargin
  }

  // ----- Domain-direct helpers (replace the 5 legacy Typespace call sites) -----

  /** Mirrors legacy `Typespace.dealias(t)` for `AliasId`. Falls back to the
    * alias itself if the map lookup fails — same as legacy behaviour on a
    * malformed `Typespace` (which would throw deeper inside the typer).
    */
  private def dealias(a: AliasId): TypeId = domain.aliases.getOrElse(a, a)

  /** Mirrors legacy `ts(al).asInstanceOf[Alias].target`. Prefer the
    * family-aggregated `domain.aliases` map (populated by
    * `NewTyperPipeline.finalizeCrossDomainAliases`), which carries
    * cross-domain `AliasId`s referenced from this domain. Fall back to the
    * local `userTypes` map when an alias is purely intra-domain. This
    * matches legacy `TypespaceImpl.dealias`, which walked
    * `transitivelyReferenced` across domain boundaries.
    */
  private def aliasTarget(a: AliasId): TypeId =
    domain.aliases.get(a) match {
      case Some(t) => t
      case None    => domain.userTypes(a).asInstanceOf[NewTypeDef.Alias].target
    }

  /** Mirrors legacy `ts.tools.implId(i)` — `DTOId(i, "Struct")`. Same naming
    * as `DomainTSStruct.implId` and `DomainTSImports.implIdName`.
    */
  private def implIdName(i: InterfaceId): String = "Struct"
}
