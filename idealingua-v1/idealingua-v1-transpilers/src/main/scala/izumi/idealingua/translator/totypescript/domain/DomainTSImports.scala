package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, Package, Primitive, TypeId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Algebraic, Alternative, Singular, Struct, Void}
import izumi.idealingua.model.il.ast.typed.DefMethod.RPCMethod
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.publishing.manifests.{TypeScriptBuildManifest, TypeScriptProjectLayout}
import izumi.idealingua.translator.totypescript.TypeScriptImport
import izumi.idealingua.translator.totypescript.layout.TypescriptNamingConvention
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

import scala.util.Try
import scala.util.control.Breaks._

/** Domain-based replacement for `TypeScriptImports` (legacy).
  *
  * Computes the same `List[TypeScriptImport]` and rendered `import { ... }`
  * block as `TypeScriptImports.apply(ts, definition, ...)` but consumes the
  * new-typer `Domain` IR instead of the legacy `Typespace`. Operation map
  * (legacy → Domain):
  *
  *   - `ts.dealias(id)` → `domain.aliases.getOrElse(a, id)` for `AliasId`.
  *     `domain.aliases` carries fully-resolved (non-alias) targets after
  *     `AliasDealiaser` (Phase 3), so the recursive chase collapses to a
  *     single lookup.
  *   - `ts.tools.implId(i)` → `DTOId(i, "Struct")`. Pure naming (legacy
  *     `TypespaceToolsImpl.toDtoName` returns the constant `"Struct"` for
  *     `InterfaceId`).
  *   - `ts.tools.sourceId(d)` → `domain.ephemeralOwner.get(d).collect {
  *     case i: InterfaceId => i }`. Only `EphemeralOrigin.InterfaceMirror`
  *     ephemerals have an `InterfaceId` owner, so the `.collect` projection
  *     reproduces the legacy `interfaceEphemeralsReversed.get(dto)` predicate.
  *   - `ts.structure.structure(i).all` → flatten `Domain.flattenedStructs(i.id)`
  *     into `List[FlatField]`; for `ExtendedField.field.typeId` we use
  *     `FlatField.field.typeId`. Covariant overrides (`defn.variance.last`)
  *     are not represented in the new IR — `StructuralFlattener` retains only
  *     the most-derived closest declaration; the base-type-import the legacy
  *     code would have collected through `variance.last.typeId` is reached
  *     via the same definition's `allParents` chain anyway.
  *   - `ts.inheritance.allParents(i)` → `domain.parents.getOrElse(i, Set.empty)`.
  *     For interfaces, legacy `allParents` includes self but the legacy filter
  *     `.filterNot(_ == i.id)` immediately strips it; new IR `parents` excludes
  *     self by construction, so the post-filter set is identical.
  *
  * The legacy `TypeScriptImports.render(ts)` is reproduced as `render: String`
  * here — no Typespace needed because `implId(i)` and `sourceId(d)` reduce to
  * the lookups described above.
  */
final case class DomainTSImports(imports: List[TypeScriptImport], manifest: TypeScriptBuildManifest, domain: Domain) {

  private def renderTypeImports(id: TypeId): String = id match {
    case adt: AdtId      => s"${adt.name}, ${adt.name}Serialized, ${adt.name}Helpers"
    case i: IdentifierId => s"${i.name}"
    case i: InterfaceId  => s"${i.name}, ${i.name + DomainTSImports.implIdName(i)}, ${i.name + DomainTSImports.implIdName(i)}Serialized"
    case d: DTOId =>
      val mirrorInterface = DomainTSImports.sourceId(domain, d)
      if (mirrorInterface.isDefined) {
        s"${mirrorInterface.get.name + d.name}, ${mirrorInterface.get.name + d.name}Serialized"
      } else {
        s"${d.name}, ${d.name}Serialized"
      }
    case _ => id.name
  }

  def render: String = {
    if (imports.isEmpty) return ""

    imports
      .filterNot(_.id.isInstanceOf[AliasId]).groupBy(_.pkg)
      .map {
        i =>
          if (i._1.startsWith("import")) i._1
          else
            "import {\n" + i._2.map(i2 => renderTypeImports(i2.id)).mkString(",").split(',').map(i2 => i2.trim).distinct.map(i2 => "    " + i2)
              .mkString(",\n") + s"\n} from '${i._1}';"
      }
      .mkString("\n")
  }

  def findImport(id: TypeId): Option[TypeScriptImport] = imports.find(_.id == id)
}

object DomainTSImports {

  /** Pure naming — mirrors `TypespaceToolsImpl.implId(i)` for `InterfaceId`. */
  def implIdName(i: InterfaceId): String = "Struct"

  /** Mirror of `TypespaceToolsImpl.sourceId(d)` — only interface-mirror
    * ephemerals have an `InterfaceId` owner.
    */
  def sourceId(domain: Domain, d: DTOId): Option[InterfaceId] =
    domain.ephemeralOwner.get(d).collect { case i: InterfaceId => i }

  /** Mirror of `TypespaceImpl.dealias(t)` — `domain.aliases` carries
    * fully-resolved non-alias targets after `AliasDealiaser`, so the
    * recursive chase collapses to a single lookup.
    */
  def dealias(domain: Domain, t: TypeId): TypeId = t match {
    case a: AliasId => domain.aliases.getOrElse(a, t)
    case _          => t
  }

  // ----- factories -----

  def forTypeDef(td: NewTypeDef, fromPkg: Package, domain: Domain, manifest: TypeScriptBuildManifest, extra: List[TypeScriptImport] = List.empty): DomainTSImports = {
    val types = collectTypes(td, domain)
    DomainTSImports(fromTypes(types, fromPkg, extra, manifest), manifest, domain)
  }

  def forService(svc: NewTypeDef.Service, fromPkg: Package, domain: Domain, manifest: TypeScriptBuildManifest, extra: List[TypeScriptImport] = List.empty): DomainTSImports = {
    val types = svc.methods.flatMap {
      case m: RPCMethod => m.signature.input.fields.flatMap(f => collectTypes(f.typeId, domain)) ++ fromRPCMethodOutput(m.signature.output, domain)
    }
    DomainTSImports(fromTypes(types, fromPkg, extra, manifest), manifest, domain)
  }

  def forBuzzer(bz: NewTypeDef.Buzzer, fromPkg: Package, domain: Domain, manifest: TypeScriptBuildManifest, extra: List[TypeScriptImport] = List.empty): DomainTSImports = {
    val types = bz.events.flatMap {
      case m: RPCMethod => m.signature.input.fields.flatMap(f => collectTypes(f.typeId, domain)) ++ fromRPCMethodOutput(m.signature.output, domain)
    }
    DomainTSImports(fromTypes(types, fromPkg, extra, manifest), manifest, domain)
  }

  // ----- type collection (mirrors legacy `TypeScriptImports.collectTypes`) -----

  private def collectTypes(id: TypeId, domain: Domain): List[TypeId] = id match {
    case p: Primitive => List(p)
    case g: Generic =>
      g match {
        case gm: Generic.TMap    => List(gm) ++ collectTypes(gm.valueType, domain)
        case gl: Generic.TList   => List(gl) ++ collectTypes(gl.valueType, domain)
        case gs: Generic.TSet    => List(gs) ++ collectTypes(gs.valueType, domain)
        case go: Generic.TOption => List(go) ++ collectTypes(go.valueType, domain)
      }
    case a: AdtId         => List(a)
    case i: InterfaceId   => List(i)
    case _: AliasId       => collectTypes(dealias(domain, id), domain)
    case id: IdentifierId => List(id)
    case e: EnumId        => List(e)
    case dto: DTOId       => List(dto)
    case _                => throw new IDLException(s"Impossible type in collectTypes ${id.name} ${id.path.toPackage.mkString(".")}")
  }

  /** Mirror of legacy `TypeScriptImports.collectTypes(ts, definition)` —
    * works against `NewTypeDef`. For structures (DTO / Interface) the
    * flattened-struct walk reads `Domain.flattenedStructs(i.id).fields`;
    * interface-parent impl-ids are derived from `Domain.parents`.
    */
  private def collectTypes(definition: NewTypeDef, domain: Domain): List[TypeId] = definition match {
    case a: NewTypeDef.Alias =>
      List(a.target)
    case _: NewTypeDef.Enum =>
      List.empty
    case i: NewTypeDef.Identifier =>
      i.fields.flatMap(f => List(f.typeId) ++ collectTypes(f.typeId, domain))
    case i: NewTypeDef.Interface =>
      val flat = domain.flattenedStructs.get(i.id).map(_.fields).getOrElse(List.empty)
      val directIfaces = i.struct.superclasses.interfaces
      val ancestors = domain.parents.getOrElse(i.id, Set.empty).toList
      directIfaces ++
      flat.flatMap(f => List(f.field.typeId) ++ collectTypes(f.field.typeId, domain)).filterNot(_ == definition.id) ++
      ancestors.filterNot(directIfaces.contains).filterNot(_ == i.id).map(ifc => DTOId(ifc, implIdName(ifc)))
    case d: NewTypeDef.Dto =>
      val flat = domain.flattenedStructs.get(d.id).map(_.fields).getOrElse(List.empty)
      val directIfaces = d.struct.superclasses.interfaces
      val ancestors = domain.parents.getOrElse(d.id, Set.empty).toList
      directIfaces ++
      flat.flatMap(f => List(f.field.typeId) ++ collectTypes(f.field.typeId, domain)).filterNot(_ == definition.id) ++
      ancestors.filterNot(directIfaces.contains).map(ifc => DTOId(ifc, implIdName(ifc)))
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

  // ----- type -> TypeScriptImport (mirrors legacy `fromTypes` + `withImport`) -----

  private def fromTypes(types: List[TypeId], fromPkg: Package, extra: List[TypeScriptImport], manifest: TypeScriptBuildManifest): List[TypeScriptImport] = {
    val imports = types.distinct
    if (fromPkg.isEmpty) return List.empty
    imports.flatMap(i => withImport(i, fromPkg, manifest).map(wi => (i, wi))).filterNot(_._2.isEmpty).map(m => TypeScriptImport(m._1, m._2)) ++ extra
  }

  private def withImport(t: TypeId, fromPackage: Package, manifest: TypeScriptBuildManifest): Seq[String] = {
    val depth: Int = if (manifest.layout == TypeScriptProjectLayout.YARN) 1 else fromPackage.size

    val pathToRoot = List.fill(depth)("../").mkString
    val scopeRoot = if (manifest.layout == TypeScriptProjectLayout.YARN) manifest.yarn.scope + "/" else pathToRoot

    t match {
      case g: Generic =>
        g match {
          case _: Generic.TOption => return Seq.empty
          case _: Generic.TMap    => return Seq.empty
          case _: Generic.TList   => return Seq.empty
          case _: Generic.TSet    => return Seq.empty
        }
      case p: Primitive =>
        p match {
          case Primitive.TTs   => return Seq(s"import { Formatter } from '${scopeRoot}irt';")
          case Primitive.TTsU  => return Seq(s"import { Formatter } from '${scopeRoot}irt';")
          case Primitive.TTsTz => return Seq(s"import { Formatter } from '${scopeRoot}irt';")
          case Primitive.TTime => return Seq(s"import { Formatter } from '${scopeRoot}irt';")
          case Primitive.TDate => return Seq(s"import { Formatter } from '${scopeRoot}irt';")
          case _               => return Seq.empty
        }
      case _ =>
    }

    if (t.path.toPackage.isEmpty) return Seq.empty

    var srcPkg   = fromPackage
    var destPkg  = t.path.toPackage
    var matching = 0

    breakable {
      for (i <- srcPkg.indices) {
        if (destPkg.size < i || pathDiffers(srcPkg, destPkg, i)) break()
        matching += 1
      }
    }

    srcPkg  = srcPkg.drop(matching)
    destPkg = destPkg.drop(matching)

    if (srcPkg.isEmpty && destPkg.isEmpty) return Seq(s"./${t.name}")

    var importOffset = ""
    var importFile   = ""

    if (srcPkg.nonEmpty && manifest.layout == TypeScriptProjectLayout.YARN) {
      val conv = new TypescriptNamingConvention(manifest)
      importOffset = conv.toScopedId(t.path.toPackage)
      importFile   = importOffset
    } else {
      if (srcPkg.nonEmpty) {
        (1 to srcPkg.size).foreach(_ => importOffset += "../")
      } else {
        importOffset = "./"
      }
      importFile = importOffset + destPkg.mkString("/")
    }

    Seq(importFile)
  }

  private def pathDiffers(srcPkg: Package, destPkg: Package, depth: Int): Boolean =
    Try(srcPkg(depth) != destPkg(depth)).getOrElse(true)
}
