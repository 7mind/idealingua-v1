package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, Primitive, TypeId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Domain-consuming twin of `CSharpType` (PR-02 IMPL-10-prep-Cs1).
  *
  * Legacy `CSharpType` is `(id: TypeId)(implicit im: CSharpImports, ts: Typespace)`.
  * This twin replaces the `Typespace` dependency with a `Domain` reference
  * held on the converter. The five distinct legacy operations:
  *
  *   - `ts.dealias(al)` (6 sites) → `domain.aliases.getOrElse(al, al)`.
  *     `domain.aliases` is fully resolved post-Phase 3 `AliasDealiaser`, so
  *     the legacy recursive chase collapses to a single lookup.
  *   - `ts(e).asInstanceOf[Enumeration]` (3 sites — `getDefaultValue` enum
  *     head, `getRandomValue` enum members + length) →
  *     `domain.userTypes(e).asInstanceOf[TypeDef.Enum]`. The new IR's
  *     `TypeDef.Enum.members: List[EnumMember]` is field-shape-identical to
  *     the legacy `TypeDef.Enumeration.members: List[EnumMember]`.
  *   - `ts(i).asInstanceOf[Identifier]` (1 site, `randomIdentifier`) →
  *     `domain.userTypes(i).asInstanceOf[TypeDef.Identifier]`. New IR
  *     `fields: List[IdField]` matches legacy `IdTuple = List[IdField]`.
  *   - `ts(a).asInstanceOf[Adt]` (1 site, `randomAdt`) →
  *     `domain.userTypes(a).asInstanceOf[TypeDef.Adt]`. New IR
  *     `alternatives: List[AdtMember]` matches legacy verbatim.
  *   - `ts(i).asInstanceOf[Interface]` + `ts.structure.structure(i)`
  *     + `ts.tools.implId(i)` (1 cluster, `randomInterface`) →
  *     `domain.userTypes(i).asInstanceOf[TypeDef.Interface]` for the iface,
  *     `domain.flattenedStructs(i)` + `DomainCSStruct.fromFlat` for the
  *     legacy `Struct` shape, and `DTOId(i, "Struct")` (constant — legacy
  *     `TypespaceToolsImpl.toDtoName` returns `"Struct"` for InterfaceId).
  *   - `ts(i).asInstanceOf[DTO]` + `ts.structure.structure(i)`
  *     + `ts.inheritance.allParents(i.id)` (1 cluster, `randomDto`) →
  *     `domain.userTypes(i).asInstanceOf[TypeDef.Dto]`,
  *     `domain.flattenedStructs(i.id)` + `DomainCSStruct.fromFlat`, and
  *     `domain.parents.getOrElse(i.id, Set.empty).toList` (the legacy
  *     `allParents` for a DTO returns inherited interfaces, not the DTO
  *     itself; this matches `Domain.parents` semantics).
  *
  * `getRandomValue` is only exercised by `NUnitExtension` and is not on the
  * Domain translator's production path; it is ported here for completeness
  * (so the converter family is fully Domain-backed) and to support future
  * NUnit wiring without re-touching the converter.
  *
  * The class is `final case class` (mirrors legacy) so the converter is
  * value-equal at the call site, but the `domain` field participates in
  * equality — that is intentional and harmless because every converter
  * created in a given `translate()` invocation shares the same `Domain`
  * reference.
  */
final case class DomainCSharpType(
  id: TypeId
)(implicit
  im: CSharpImports,
  domain: Domain,
) {

  def isNative: Boolean            = isNativeImpl(id)
  def isNullable: Boolean          = isNullableImpl(id)
  def defaultValue: String         = getDefaultValue(id)
  def getInitValue: Option[String] = getInitValue(id)
  def getRandomValue(depth: Int = 0): String = getRandomValue(id, depth)

  def isNullableImpl(id: TypeId): Boolean = id match {
    case g: Generic =>
      g match {
        case _: Generic.TMap    => true
        case _: Generic.TList   => true
        case _: Generic.TSet    => true
        case _: Generic.TOption => true
      }
    case p: Primitive =>
      p match {
        case Primitive.TBool   => false
        case Primitive.TString => true
        case Primitive.TInt8   => false
        case Primitive.TInt16  => false
        case Primitive.TInt32  => false
        case Primitive.TInt64  => false
        case Primitive.TUInt8  => false
        case Primitive.TUInt16 => false
        case Primitive.TUInt32 => false
        case Primitive.TUInt64 => false
        case Primitive.TFloat  => false
        case Primitive.TDouble => false
        case Primitive.TUUID   => false
        case Primitive.TTime   => false
        case Primitive.TDate   => false
        case Primitive.TTs     => false
        case Primitive.TTsTz   => false
        case Primitive.TTsU    => false
        case Primitive.TBLOB   => ???
      }
    case _ =>
      id match {
        case _: EnumId           => false
        case _: InterfaceId      => true
        case _: IdentifierId     => true
        case _: AdtId | _: DTOId => true
        case al: AliasId         => isNullableImpl(dealias(al))
        case _                   => throw new IDLException(s"Impossible isNullableImpl type: ${id.name}")
      }
  }

  private def isNativeImpl(id: TypeId): Boolean = id match {
    case g: Generic =>
      g match {
        case _: Generic.TMap    => true
        case _: Generic.TList   => true
        case _: Generic.TSet    => true
        case _: Generic.TOption => true
      }
    case p: Primitive =>
      p match {
        case Primitive.TBool   => true
        case Primitive.TString => true
        case Primitive.TInt8   => true
        case Primitive.TInt16  => true
        case Primitive.TInt32  => true
        case Primitive.TInt64  => true
        case Primitive.TUInt8  => true
        case Primitive.TUInt16 => true
        case Primitive.TUInt32 => true
        case Primitive.TUInt64 => true
        case Primitive.TFloat  => true
        case Primitive.TDouble => true
        case Primitive.TUUID   => true
        case Primitive.TTime   => true
        case Primitive.TDate   => true
        case Primitive.TTs     => true
        case Primitive.TTsTz   => true
        case Primitive.TTsU    => true
        case Primitive.TBLOB   => true
      }
    case _ =>
      id match {
        case _: EnumId           => true
        case _: InterfaceId      => true
        case _: IdentifierId     => true
        case _: AdtId | _: DTOId => true
        case al: AliasId         => isNativeImpl(dealias(al))
        case _                   => throw new IDLException(s"Impossible isNativeImpl type: ${id.name}")
      }
  }

  private def getDefaultValue(id: TypeId): String = id match {
    case g: Generic =>
      g match {
        case _: Generic.TMap    => "null"
        case _: Generic.TList   => "null"
        case _: Generic.TSet    => "null"
        case _: Generic.TOption => "null"
      }
    case p: Primitive =>
      p match {
        case Primitive.TBool   => "false"
        case Primitive.TString => "null"
        case Primitive.TInt8   => "0"
        case Primitive.TInt16  => "0"
        case Primitive.TInt32  => "0"
        case Primitive.TInt64  => "0"
        case Primitive.TUInt8  => "0"
        case Primitive.TUInt16 => "0"
        case Primitive.TUInt32 => "0"
        case Primitive.TUInt64 => "0"
        case Primitive.TFloat  => "0.0f"
        case Primitive.TDouble => "0.0"
        case Primitive.TUUID   => "null"
        case Primitive.TTime   => "null"
        case Primitive.TDate   => "0"
        case Primitive.TTs     => "0"
        case Primitive.TTsTz   => "0"
        case Primitive.TTsU    => "0"
        case Primitive.TBLOB   => "null"
      }
    case _ =>
      id match {
        case e: EnumId           => s"${e.name}.${lookupEnum(e).members.head.value}"
        case _: InterfaceId      => "null"
        case _: IdentifierId     => "null"
        case _: AdtId | _: DTOId => "null"
        case al: AliasId         => getDefaultValue(dealias(al))
        case _                   => throw new IDLException(s"Impossible getDefaultValue type: ${id.name}")
      }
  }

  private def getInitValue(id: TypeId): Option[String] = id match {
    case g: Generic =>
      g match {
        case _: Generic.TMap    => Some(s"new ${renderType(withPackage = true)}()")
        case _: Generic.TList   => Some(s"new ${renderType(withPackage = true)}()")
        case _: Generic.TSet    => Some(s"new ${renderType(withPackage = true)}()")
        case _: Generic.TOption => None
      }
    case p: Primitive =>
      p match {
        case Primitive.TBool   => None
        case Primitive.TString => None
        case Primitive.TInt8   => None
        case Primitive.TInt16  => None
        case Primitive.TInt32  => None
        case Primitive.TInt64  => None
        case Primitive.TUInt8  => None
        case Primitive.TUInt16 => None
        case Primitive.TUInt32 => None
        case Primitive.TUInt64 => None
        case Primitive.TFloat  => None
        case Primitive.TDouble => None
        case Primitive.TUUID   => None
        case Primitive.TTime   => None
        case Primitive.TDate   => None
        case Primitive.TTs     => None
        case Primitive.TTsTz   => None
        case Primitive.TTsU    => None
        case Primitive.TBLOB   => None
      }
    case _ =>
      id match {
        case _: EnumId           => None
        case _: InterfaceId      => None
        case _: IdentifierId     => None
        case _: AdtId | _: DTOId => None
        case al: AliasId         => getInitValue(dealias(al))
        case _                   => throw new IDLException(s"Impossible getInitValue type: ${id.name}")
      }
  }

  private def getRandomValue(id: TypeId, depth: Int): String = {
    val rnd = new scala.util.Random()
    id match {
      case g: Generic =>
        g match {
          case gm: Generic.TMap   => s"new ${DomainCSharpType(gm).renderType(true)}()"
          case gl: Generic.TList  => s"new ${DomainCSharpType(gl).renderType(true)}()"
          case gs: Generic.TSet   => s"new ${DomainCSharpType(gs).renderType(true)}()"
          case _: Generic.TOption => "null"
        }
      case p: Primitive =>
        p match {
          case Primitive.TBool   => rnd.nextBoolean().toString
          case Primitive.TString => "\"str_" + rnd.nextInt(20000) + "\""
          case Primitive.TInt8   => rnd.nextInt(127).toString
          case Primitive.TInt16  => (256 + rnd.nextInt(32767 - 255)).toString
          case Primitive.TInt32  => (32768 + rnd.nextInt(2147483647 - 32767)).toString
          case Primitive.TInt64  => (2147483648L + rnd.nextInt(2147483647)).toString
          case Primitive.TUInt8  => rnd.nextInt(127).toString
          case Primitive.TUInt16 => (256 + rnd.nextInt(32767 - 255)).toString
          case Primitive.TUInt32 => (32768 + rnd.nextInt(2147483647 - 32767)).toString
          case Primitive.TUInt64 => (2147483648L + rnd.nextInt(2147483647)).toString
          case Primitive.TFloat  => rnd.nextFloat().toString + "f"
          case Primitive.TDouble => (2147483648L + rnd.nextFloat()).toString
          case Primitive.TBLOB   => ???
          case Primitive.TUUID   => s"""new System.Guid("${java.util.UUID.randomUUID.toString}")"""
          case Primitive.TTime =>
            s"""System.TimeSpan.Parse(string.Format("{0:D2}:{1:D2}:{2:D2}.{3:D3}", ${rnd.nextInt(24)}, ${rnd.nextInt(60)}, ${rnd.nextInt(60)}, ${100 + rnd.nextInt(
                100
              )}))"""
          case Primitive.TDate =>
            s"""System.DateTime.Parse(string.Format("{0:D4}-{1:D2}-{2:D2}", ${1984 + rnd.nextInt(20)}, ${1 + rnd.nextInt(12)}, ${1 + rnd.nextInt(28)}))"""
          case Primitive.TTs =>
            s"""System.DateTime.ParseExact(string.Format("{0:D4}-{1:D2}-{2:D2}T{3:D2}:{4:D2}:{5:D2}.{6:D3}", ${1984 + rnd.nextInt(20)}, ${1 + rnd.nextInt(12)}, ${1 + rnd
                .nextInt(28)}, ${rnd.nextInt(24)}, ${rnd.nextInt(60)}, ${rnd.nextInt(60)}, ${100 + rnd.nextInt(
                100
              )}), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None)"""
          case Primitive.TTsTz =>
            s"""System.DateTime.ParseExact(string.Format("{0:D4}-{1:D2}-{2:D2}T{3:D2}:{4:D2}:{5:D2}.{6:D3}+10:00", ${1984 + rnd.nextInt(20)}, ${1 + rnd.nextInt(
                12
              )}, ${1 + rnd.nextInt(28)}, ${rnd.nextInt(24)}, ${rnd.nextInt(60)}, ${rnd.nextInt(60)}, ${100 + rnd.nextInt(
                100
              )}), JsonNetTimeFormats.Tsz, CultureInfo.InvariantCulture, DateTimeStyles.None)"""
          case Primitive.TTsU =>
            s"""System.DateTime.ParseExact(string.Format("{0:D4}-{1:D2}-{2:D2}T{3:D2}:{4:D2}:{5:D2}.{6:D3}Z", ${1984 + rnd.nextInt(20)}, ${1 + rnd.nextInt(12)}, ${1 + rnd
                .nextInt(28)}, ${rnd.nextInt(24)}, ${rnd.nextInt(60)}, ${rnd.nextInt(60)}, ${100 + rnd.nextInt(
                100
              )}), JsonNetTimeFormats.Tsu, CultureInfo.InvariantCulture, DateTimeStyles.None)"""
        }
      case _ =>
        id match {
          case e: EnumId => {
            val enu = lookupEnum(e)
            s"${e.path.toPackage.map(p => p.capitalize).mkString(".") + "." + e.name}.${enu.members.map(_.value).apply(rnd.nextInt(enu.members.length))}"
          }
          case i: InterfaceId  => if (depth <= 0) "null" else randomInterface(i, depth)
          case i: IdentifierId => randomIdentifier(i, depth)
          case a: AdtId        => if (depth <= 0) "null" else randomAdt(a, depth)
          case i: DTOId        => if (depth <= 0) "null" else randomDto(i, depth)
          case al: AliasId     => getRandomValue(dealias(al), depth)
          case _               => throw new IDLException(s"Impossible getRandomValue type: ${id.name}")
        }
    }
  }

  private def randomIdentifier(i: IdentifierId, depth: Int): String = {
    val inst = lookupIdentifier(i)
    s"""new ${i.path.toPackage.map(p => p.capitalize).mkString(".") + "." + i.name}(
       |${inst.fields.map(f => DomainCSharpType(f.typeId).getRandomValue(depth - 1)).mkString(",\n").shift(4)}
       |)
     """.stripMargin
  }

  private def randomAdt(i: AdtId, depth: Int): String = {
    val adt = lookupAdt(i)
    s"""new ${i.path.toPackage.map(p => p.capitalize).mkString(".") + "." + i.name}.${adt.alternatives.head.wireId}(
       |${DomainCSharpType(adt.alternatives.head.typeId).getRandomValue(depth - 1).shift(4)}
       |)
     """.stripMargin
  }

  private def randomInterface(i: InterfaceId, depth: Int): String = {
    val inst        = lookupInterface(i)
    val flat        = domain.flattenedStructs.getOrElse(
      i,
      izumi.idealingua.typer.ir.FlatStruct(i, List.empty, List.empty, List.empty),
    )
    val structure   = DomainCSStruct.fromFlat(i, flat, inst.struct.superclasses, domain)
    val eid         = DomainCSStruct.implId(i)
    val parentIfaces: Set[TypeId] = inst.struct.superclasses.interfaces.toSet[TypeId]
    val validFields = structure.all.filterNot(f => parentIfaces.contains(f.defn.definedBy))
    val struct      = DomainCSClass.fromFields(eid, i.name + eid.name, validFields.map(_.field), List.empty)
    randomDtoFromClass(struct, i.name + eid.name, i, depth)
  }

  private def randomDto(i: DTOId, depth: Int): String = {
    val flat = domain.flattenedStructs.getOrElse(
      i,
      izumi.idealingua.typer.ir.FlatStruct(i, List.empty, List.empty, List.empty),
    )
    // For synthetic ids not in userTypes (e.g. interface impl-DTOs constructed on the fly),
    // fall back to an empty Super — the inherited interfaces won't be needed for
    // random-value generation since only the field set is consumed downstream.
    val supers = domain.userTypes.get(i) match {
      case Some(d: NewTypeDef.Dto) => d.struct.superclasses
      case _                       => izumi.idealingua.model.il.ast.typed.Super(List.empty, List.empty, List.empty)
    }
    val structure = DomainCSStruct.fromFlat(i, flat, supers, domain)
    val struct    = DomainCSClass(i, i.name, structure, List.empty)
    val implIface = domain.parents.getOrElse(i, Set.empty).find(ii => DomainCSStruct.implId(ii) == i)
    val dtoName =
      if (implIface.isDefined) implIface.get.path.toPackage.map(p => p.capitalize).mkString(".") + "." + implIface.get.name + i.name
      else
        i.path.toPackage.map(p => p.capitalize).mkString(".") + "." + i.name
    s"""new $dtoName(
       |${struct.fields.map(f => f.tp.getRandomValue(depth - 1)).mkString(",\n").shift(4)}
       |)
       """.stripMargin
  }

  private def randomDtoFromClass(struct: DomainCSClass, dtoName: String, parentIface: InterfaceId, depth: Int): String = {
    val fqDtoName = parentIface.path.toPackage.map(p => p.capitalize).mkString(".") + "." + dtoName
    s"""new $fqDtoName(
       |${struct.fields.map(f => f.tp.getRandomValue(depth - 1)).mkString(",\n").shift(4)}
       |)
       """.stripMargin
  }

  def renderToString(name: String, escape: Boolean): String = {
    val res = id match {
      case Primitive.TString => name
      case Primitive.TInt8   => return s"$name.ToString()" // No Escaping needed for integers
      case Primitive.TInt16  => return s"$name.ToString()"
      case Primitive.TInt32  => return s"$name.ToString()"
      case Primitive.TInt64  => return s"$name.ToString()"
      case Primitive.TUInt8  => return s"$name.ToString()"
      case Primitive.TUInt16 => return s"$name.ToString()"
      case Primitive.TUInt32 => return s"$name.ToString()"
      case Primitive.TUInt64 => return s"$name.ToString()"
      case Primitive.TBool   => return s"$name.ToString()"
      case Primitive.TBLOB   => ???
      case Primitive.TUUID   => s"$name.ToString()"
      case _: EnumId         => s"$name.ToString()"
      case _: IdentifierId   => s"$name.ToString()"
      case _                 => throw new IDLException(s"Should never render non int, string, or Guid types to strings. Used for type ${id.name}")
    }
    if (escape) {
      s"IRT.Transport.UrlEscaper.Escape($res)"
    } else {
      res
    }
  }

  def renderFromString(src: String, unescape: Boolean, currentDomain: String = ""): String = {
    val source = if (unescape) s"IRT.Transport.UrlEscaper.UnEscape($src)" else src
    id match {
      case Primitive.TString => source
      case Primitive.TInt8   => s"sbyte.Parse($src)" // No Escaping needed for integers
      case Primitive.TInt16  => s"short.Parse($src)"
      case Primitive.TInt32  => s"int.Parse($src)"
      case Primitive.TInt64  => s"long.Parse($src)"
      case Primitive.TUInt8  => s"byte.Parse($src)"
      case Primitive.TUInt16 => s"ushort.Parse($src)"
      case Primitive.TUInt32 => s"uint.Parse($src)"
      case Primitive.TUInt64 => s"ulong.Parse($src)"
      case Primitive.TBool   => s"bool.Parse($src)"
      case Primitive.TUUID   => s"new Guid($source)"
      case Primitive.TBLOB   => ???
      case _: EnumId         => s"${renderType(currentDomain != "" && currentDomain != id.uniqueDomainName)}Helpers.From($source)"
      case _: IdentifierId   => s"${renderType(currentDomain != "" && currentDomain != id.uniqueDomainName)}.From($source)"
      case _                 => throw new IDLException(s"Should never render non int, string, or Guid types to strings. Used for type ${id.name}")
    }
  }

  def renderType(withPackage: Boolean = false): String = {
    renderNativeType(id, withPackage)
  }

  private def renderNativeType(id: TypeId, withPackage: Boolean): String = id match {
    case g: Generic   => renderGenericType(g, withPackage)
    case p: Primitive => renderPrimitiveType(p)
    case _            => renderUserType(id, withPackage = withPackage)
  }

  private def renderGenericType(generic: Generic, withPackage: Boolean): String = {
    generic match {
      case gm: Generic.TMap  => s"Dictionary<${renderNativeType(gm.keyType, withPackage)}, ${renderNativeType(gm.valueType, withPackage)}>"
      case gl: Generic.TList => s"List<${renderNativeType(gl.valueType, withPackage)}>"
      case gs: Generic.TSet  => s"List<${renderNativeType(gs.valueType, withPackage)}>"
      case go: Generic.TOption =>
        if (!isNullableImpl(go.valueType)) s"Nullable<${renderNativeType(go.valueType, withPackage)}>" else renderNativeType(go.valueType, withPackage)
    }
  }

  protected def renderPrimitiveType(primitive: Primitive): String = primitive match {
    case Primitive.TBool   => "bool"
    case Primitive.TString => "string"

    case Primitive.TInt8  => "sbyte"
    case Primitive.TUInt8 => "byte"

    case Primitive.TInt16  => "short"
    case Primitive.TUInt16 => "ushort"

    case Primitive.TInt32  => "int"
    case Primitive.TUInt32 => "uint"

    case Primitive.TInt64  => "long"
    case Primitive.TUInt64 => "ulong"

    case Primitive.TFloat  => "float"
    case Primitive.TDouble => "double"
    case Primitive.TUUID   => "Guid"
    case Primitive.TBLOB   => ???
    case Primitive.TTime   => "TimeSpan"
    case Primitive.TDate   => "DateTime" // Could be Date
    case Primitive.TTs     => "DateTime"
    case Primitive.TTsTz   => "DateTime"
    case Primitive.TTsU    => "DateTime"
  }

  protected def renderUserType(id: TypeId, withPackage: Boolean = false): String = {
    val fullName = id.path.toPackage.map(p => p.capitalize).mkString(".") + "." + id.name
    id match {
      case _: EnumId           => if (withPackage) fullName else s"${im.withImport(id)}"
      case _: InterfaceId      => if (withPackage) fullName else s"${im.withImport(id)}"
      case _: IdentifierId     => if (withPackage) fullName else s"${im.withImport(id)}"
      case _: AdtId | _: DTOId => if (withPackage) fullName else s"${im.withImport(id)}"
      case al: AliasId         => renderNativeType(dealias(al), withPackage)
      case _                   => throw new IDLException(s"Impossible renderUserType ${id.name}")
    }
  }

  // -- Domain-direct helpers (replace the legacy Typespace call sites) ----

  /** Mirror of `Typespace.dealias(AliasId)` — single lookup, since
    * `domain.aliases` is fully resolved post-`AliasDealiaser`.
    */
  private def dealias(a: AliasId): TypeId = domain.aliases.getOrElse(a, a)

  /** Mirror of `ts(e).asInstanceOf[Enumeration]` — projects the new IR's
    * `TypeDef.Enum` (field-shape-identical to legacy `Enumeration`).
    */
  private def lookupEnum(e: EnumId): NewTypeDef.Enum =
    domain.userTypes(e).asInstanceOf[NewTypeDef.Enum]

  /** Mirror of `ts(i).asInstanceOf[Identifier]`. */
  private def lookupIdentifier(i: IdentifierId): NewTypeDef.Identifier =
    domain.userTypes(i).asInstanceOf[NewTypeDef.Identifier]

  /** Mirror of `ts(a).asInstanceOf[Adt]`. */
  private def lookupAdt(a: AdtId): NewTypeDef.Adt =
    domain.userTypes(a).asInstanceOf[NewTypeDef.Adt]

  /** Mirror of `ts(i).asInstanceOf[Interface]`. */
  private def lookupInterface(i: InterfaceId): NewTypeDef.Interface =
    domain.userTypes(i).asInstanceOf[NewTypeDef.Interface]

}

object DomainCSharpType {
  def apply(
    id: TypeId
  )(implicit
    im: CSharpImports,
    domain: Domain,
  ): DomainCSharpType = new DomainCSharpType(id)
}
