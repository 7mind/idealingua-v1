package izumi.idealingua.model.il.ast

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, DomainMeshResolved, SingleImport}
import izumi.idealingua.model.problems.IDLException

/** Structural projection from the resolved raw mesh to the loaded mesh.
  *
  * Collects top-level definitions by category (types, services, buzzers,
  * streams, consts), flattens imports to single-symbol form, and rejects
  * domains where an imported symbol name clashes with a locally-defined
  * type name.
  *
  * PR-02 IMPL-10d: this class was extracted from the deleted
  * `IDLTyper.scala`. It is the sole pre-typing helper that survives the
  * legacy typer purge — `NewTyperPipeline.run` invokes it to obtain the
  * `DomainMeshLoaded` Phase 0 expects.
  */
class IDLPretyper(defn: DomainMeshResolved) {
  def perform(): DomainMeshLoaded = {
    val types = defn.members.collect {
      case d: RawTopLevelDefn.TLDBaseType => d.v
      case d: RawTopLevelDefn.TLDNewtype  => d.v
    }
    val imports = defn.imports.flatMap {
      i =>
        i.identifiers.map(SingleImport(i.id, _))
    }
    val services = defn.members.collect { case d: RawTopLevelDefn.TLDService => d.v }
    val buzzers  = defn.members.collect { case d: RawTopLevelDefn.TLDBuzzer => d.v }
    val streams  = defn.members.collect { case d: RawTopLevelDefn.TLDStreams => d.v }
    val consts   = defn.members.collect { case d: RawTopLevelDefn.TLDConsts => d.v }

    val allImportNames = imports.map(_.imported.importedAs)
    val allTypeNames = defn.members.collect {
      case d: RawTopLevelDefn.TLDBaseType => d.v.id.name
      case d: RawTopLevelDefn.TLDNewtype  => d.v.id.name
    }

    val clashes = allImportNames.intersect(allTypeNames)
    if (clashes.nonEmpty) {
      throw new IDLException(s"[${defn.id}] Import names clashing with domain names: ${clashes.niceList()}")
    }
    DomainMeshLoaded(
      defn.id,
      defn.origin,
      defn.directInclusions,
      defn.imports,
      defn.meta,
      types,
      services,
      buzzers,
      streams,
      consts,
      imports,
      defn,
    )
  }
}
