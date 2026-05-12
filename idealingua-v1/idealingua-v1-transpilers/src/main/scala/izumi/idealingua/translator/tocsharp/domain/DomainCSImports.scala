package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, Package, Primitive, TypeId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Algebraic, Alternative, Singular, Struct, Void}
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.translator.tocsharp.{CSharpImport, CSharpImports}
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Domain-based replacement for `CSharpImports.apply(definition, pkg)(ts)`.
  *
  * Produces a legacy `CSharpImports` value (a thin case class around
  * `List[CSharpImport]`) using the new-typer `Domain` IR instead of
  * `Typespace`. The legacy `CSharpImports` instance is what is threaded
  * through the C# renderers and the `DomainCSJsonNetExtension` as
  * `implicit im: CSharpImports`; returning the legacy class verbatim keeps
  * those call sites unchanged.
  *
  * Operation map (legacy → Domain):
  *
  *   - `ts.dealias(id)` → `domain.aliases.getOrElse(a, id)` for `AliasId`.
  *     `domain.aliases` carries fully-resolved (non-alias) targets after
  *     `AliasDealiaser` (Phase 3).
  *   - `ts.structure.structure(i).all` → `domain.flattenedStructs(i.id)
  *     .fields`. Covariant overrides (`defn.variance.last.typeId`) are not
  *     represented in the new IR; the import-collection logic falls back to
  *     `field.typeId` (the most-derived type).
  *   - `CSharpType(go.valueType)(im = null, ts).isNullable` → local
  *     `isNullable(id, domain)` predicate (`ts.dealias` is the only
  *     `Typespace` dependency in the legacy implementation and reduces to a
  *     `domain.aliases` lookup).
  *
  * C# imports do NOT thread interface impl-id implementors (unlike TS) —
  * see legacy `CSharpImports.scala:152-159`: the Interface / DTO branches
  * walk only the flattened-struct fields and do not append
  * `inheritance.allParents(...).map(ts.tools.implId)`. We preserve that
  * asymmetry.
  */
object DomainCSImports {

  /** Mirror of `TypespaceImpl.dealias(t)` — single lookup, since
    * `domain.aliases` is fully resolved post-`AliasDealiaser`.
    */
  def dealias(domain: Domain, t: TypeId): TypeId = t match {
    case a: AliasId => domain.aliases.getOrElse(a, t)
    case _          => t
  }

  /** Mirror of `CSharpType.isNullableImpl` (legacy
    * `CSharpType.scala:25-64`). The only `Typespace` dependency in the
    * legacy predicate is `ts.dealias(AliasId)`; the new IR carries the same
    * information in `domain.aliases`.
    */
  private def isNullable(id: TypeId, domain: Domain): Boolean = id match {
    case g: Generic =>
      g match {
        case _: Generic.TMap    => true
        case _: Generic.TList   => true
        case _: Generic.TSet    => true
        case _: Generic.TOption => true
      }
    case p: Primitive =>
      p match {
        case Primitive.TString => true
        case Primitive.TBLOB   => ???
        case _                 => false
      }
    case _: EnumId                        => false
    case _: InterfaceId | _: IdentifierId => true
    case _: AdtId | _: DTOId              => true
    case a: AliasId                       => isNullable(dealias(domain, a), domain)
    case _                                => throw new IDLException(s"Impossible isNullable type: ${id.name}")
  }

  // ----- factories (mirror `CSharpImports.apply` overloads) -----

  /** Mirror of `CSharpImports.apply(definition, fromPkg, extra)(ts)` for the
    * structural-type branches (Identifier / DTO / Interface / Adt / Alias /
    * Enum).
    */
  def forTypeDef(td: NewTypeDef, fromPkg: Package, domain: Domain, extra: List[CSharpImport] = List.empty): CSharpImports =
    CSharpImports(fromTypes(collectTypes(td, domain), fromPkg, extra))

  /** Mirror of `CSharpImports.apply(svc, fromPkg, extra)(ts)`. */
  def forService(svc: NewTypeDef.Service, fromPkg: Package, domain: Domain, extra: List[CSharpImport] = List.empty): CSharpImports = {
    val types = svc.methods.flatMap {
      case m: RPCMethod => m.signature.input.fields.flatMap(f => collectTypes(f.typeId, domain)) ++ fromRPCMethodOutput(m.signature.output, domain)
    }
    CSharpImports(fromTypes(types, fromPkg, extra))
  }

  /** Mirror of `CSharpImports.apply(bz, fromPkg, extra)(ts)`. */
  def forBuzzer(bz: NewTypeDef.Buzzer, fromPkg: Package, domain: Domain, extra: List[CSharpImport] = List.empty): CSharpImports = {
    val types = bz.events.flatMap {
      case m: RPCMethod => m.signature.input.fields.flatMap(f => collectTypes(f.typeId, domain)) ++ fromRPCMethodOutput(m.signature.output, domain)
    }
    CSharpImports(fromTypes(types, fromPkg, extra))
  }

  // ----- type collection (mirrors legacy `CSharpImports.collectTypes`) -----

  private def collectTypes(id: TypeId, domain: Domain): List[TypeId] = id match {
    case p: Primitive => List(p)
    case g: Generic =>
      g match {
        case gm: Generic.TMap    => List(gm) ++ collectTypes(gm.valueType, domain)
        case gl: Generic.TList   => List(gl) ++ collectTypes(gl.valueType, domain)
        case gs: Generic.TSet    => List(gs) ++ collectTypes(gs.valueType, domain)
        case go: Generic.TOption =>
          // Legacy: `if (CSharpType(go.valueType)(im = null, ts).isNullable) List(go) else List.empty`.
          val nullable = isNullable(go.valueType, domain)
          (if (nullable) List(go) else List.empty) ++ collectTypes(go.valueType, domain)
      }
    case a: AdtId         => List(a)
    case i: InterfaceId   => List(i)
    case _: AliasId       => collectTypes(dealias(domain, id), domain)
    case id: IdentifierId => List(id)
    case e: EnumId        => List(e)
    case dto: DTOId       => List(dto)
    case _                => throw new IDLException(s"Impossible type in collectTypes ${id.name} ${id.path.toPackage.mkString(".")}")
  }

  private def collectTypes(definition: NewTypeDef, domain: Domain): List[TypeId] = definition match {
    case a: NewTypeDef.Alias =>
      List(a.target)
    case _: NewTypeDef.Enum =>
      List.empty
    case i: NewTypeDef.Identifier =>
      i.fields.flatMap(f => List(f.typeId) ++ collectTypes(f.typeId, domain))
    case i: NewTypeDef.Interface =>
      val flat = domain.flattenedStructs.get(i.id).map(_.fields).getOrElse(List.empty)
      i.struct.superclasses.interfaces ++
      flat.flatMap(f => List(f.field.typeId) ++ collectTypes(f.field.typeId, domain)).filterNot(_ == definition.id)
    case d: NewTypeDef.Dto =>
      val flat = domain.flattenedStructs.get(d.id).map(_.fields).getOrElse(List.empty)
      d.struct.superclasses.interfaces ++
      flat.flatMap(f => List(f.field.typeId) ++ collectTypes(f.field.typeId, domain)).filterNot(_ == definition.id)
    case a: NewTypeDef.Adt =>
      a.alternatives.flatMap(al => List(al.typeId) ++ collectTypes(al.typeId, domain))
    case _ => List.empty
  }

  private def fromRPCMethodOutput(output: DefMethod.Output, domain: Domain): List[TypeId] = output match {
    case st: Struct      => st.struct.fields.flatMap(ff => collectTypes(ff.typeId, domain))
    case ad: Algebraic   => ad.alternatives.flatMap(al => collectTypes(al.typeId, domain))
    case si: Singular    => collectTypes(si.typeId, domain)
    case _: Void         => List.empty
    case al: Alternative => fromRPCMethodOutput(al.success, domain) ++ fromRPCMethodOutput(al.failure, domain)
  }

  // ----- type -> CSharpImport (mirrors legacy `fromTypes` + `withImport`) -----

  private def fromTypes(types: List[TypeId], fromPkg: Package, extra: List[CSharpImport]): List[CSharpImport] = {
    val imports = types.distinct
    if (fromPkg.isEmpty) return List.empty

    val packages = imports.map(i => (i, withImport(i, fromPkg))).filterNot(_._2.isEmpty).groupBy(_._1.name)

    packages
      .flatMap(
        pt =>
          if (pt._2.length == 1 || pt._2.head._1.isInstanceOf[Generic])
            Seq(CSharpImport(pt._2.head._1, pt._2.head._2, s""))
          else
            pt._2.zipWithIndex.map {
              case (pt2, index) => CSharpImport(pt2._1, pt2._2, s"${pt2._1.name}_$index")
            }
      ).toList ++ extra
  }

  private def withImport(t: TypeId, fromPackage: Package): Seq[String] = {
    t match {
      case Primitive.TTime => return Seq("System")
      case Primitive.TTs   => return Seq("System", "IRT", "System.Globalization")
      case Primitive.TTsTz => return Seq("System", "IRT", "System.Globalization")
      case Primitive.TTsU  => return Seq("System", "IRT", "System.Globalization")
      case Primitive.TDate => return Seq("System", "IRT", "System.Globalization")
      case Primitive.TUUID => return Seq("System")
      case Primitive.TBLOB => ???
      case g: Generic =>
        g match {
          case _: Generic.TOption => return Seq("System")
          case _: Generic.TMap    => return Seq("System.Collections", "System.Collections.Generic")
          case _: Generic.TList   => return Seq("System.Collections", "System.Collections.Generic")
          case _: Generic.TSet    => return Seq("System.Collections", "System.Collections.Generic")
        }
      case _: Primitive => return Seq.empty
      case _            =>
    }

    if (t.path.toPackage.isEmpty) return Seq.empty

    val nestedDepth = t.path.toPackage.zip(fromPackage).count(x => x._1 == x._2)
    if (nestedDepth == t.path.toPackage.size) return Seq.empty

    Seq(t.path.toPackage.map(p => p.capitalize).mkString("."))
  }
}
