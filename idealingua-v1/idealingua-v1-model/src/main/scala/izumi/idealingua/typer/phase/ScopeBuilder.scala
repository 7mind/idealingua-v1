package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.{DomainId, TypeId}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn, RawTypeDef}
import izumi.idealingua.model.il.ast.raw.defns.RawTypeDef.{ForeignType, NewType}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshLoaded, DomainMeshResolved}
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics}

/** Phase 1 — `ScopeBuilder`.
  *
  * Walks the raw `DomainMeshLoaded` (Phase 0 output of legacy `IDLPretyper`),
  * resolves local and imported names, and emits diagnostics for clashes /
  * unsupported features. Output is the internal `ScopedDomain` value, consumed
  * by Phase 2 (`NameResolver`).
  *
  * Per PR-02 IMPL-2 plan §3: the input is `DomainMeshLoaded` (F18 bypass);
  * IMPL-4's `FamilyIndex` will replace this when Phase 0 is rebuilt.
  */
object ScopeBuilder {

  /** Internal IMPL-2-only intermediate.
    *
    * Captures the post-Phase-1 indices: `localNames`/`importedNames` for
    * Phase 2's name resolution, plus a raw-by-id `index` for source-lookup
    * during structural type construction.
    *
    * @param domainId      Owning domain.
    * @param localNames    Simple-name → local `TypeId`.
    * @param importedNames Simple-name → fully-qualified imported `TypeId`.
    * @param index         `TypeId` → raw type definition (for Phase-2 lookups).
    * @param raw           The original `DomainMeshLoaded` passed through.
    * @param diagnostics   Diagnostics accumulated during scope-building.
    */
  final case class ScopedDomain(
    domainId: DomainId,
    localNames: Map[String, TypeId],
    importedNames: Map[String, TypeId],
    index: Map[TypeId, RawTypeDef],
    raw: DomainMeshLoaded,
    diagnostics: Diagnostics,
  )

  /** Build a `ScopedDomain` from a `DomainMeshLoaded`.
    *
    * Per C8/L1 (diagnostics-mode): no exceptions are thrown for user-visible
    * errors — every problem becomes a `Diagnostic` in `ScopedDomain.diagnostics`.
    */
  def apply(input: DomainMeshLoaded): ScopedDomain = {
    val domainPos: InputPosition = input.meta.position

    val localBuilder = scala.collection.mutable.LinkedHashMap.empty[String, TypeId]
    val indexBuilder = scala.collection.mutable.LinkedHashMap.empty[TypeId, RawTypeDef]
    val diagBuf      = scala.collection.mutable.ArrayBuffer.empty[Diagnostic]

    input.types.foreach {
      case d: RawTypeDef.WithId =>
        val tid  = d.id
        val name = tid.name
        val pos  = withIdMeta(d).position
        localBuilder.get(name) match {
          case Some(existing) =>
            diagBuf += Diagnostic.ScopeCollision(name, existing, tid, pos)
          case None =>
            localBuilder.update(name, tid)
            indexBuilder.update(tid, d)
        }

      case d: NewType =>
        val tid  = d.id.toAliasId
        val name = tid.name
        localBuilder.get(name) match {
          case Some(existing) =>
            diagBuf += Diagnostic.ScopeCollision(name, existing, tid, d.meta.position)
          case None =>
            localBuilder.update(name, tid)
            indexBuilder.update(tid, d)
        }

      case d: ForeignType =>
        diagBuf += Diagnostic.ForeignTypeUnsupported(d.id.name, d.meta.position)

      case _: RawTypeDef.DeclaredType =>
        ()
    }

    val localNames: Map[String, TypeId] = localBuilder.toMap

    val importedBuilder = scala.collection.mutable.LinkedHashMap.empty[String, TypeId]
    input.imports.foreach {
      si =>
        val importedAs   = si.imported.importedAs
        val originalName = si.imported.name
        input.defn.referenced.get(si.domain) match {
          case Some(refMesh) =>
            collectLocalNames(refMesh).get(originalName) match {
              case Some(tid) => importedBuilder.update(importedAs, tid)
              case None      => () // surface later as UnknownTypeRef during Phase 2
            }
          case None =>
            ()
        }
    }

    // Clash check: every imported alias name must not collide with a local name.
    importedBuilder.foreach {
      case (name, tid) =>
        localNames.get(name).foreach { localTid =>
          diagBuf += Diagnostic.ImportNameClashesWithLocal(name, localTid, tid, domainPos)
        }
    }

    ScopedDomain(
      domainId      = input.id,
      localNames    = localNames,
      importedNames = importedBuilder.toMap,
      index         = indexBuilder.toMap,
      raw           = input,
      diagnostics   = Diagnostics(diagBuf.toVector),
    )
  }

  /** `RawTypeDef.WithId` exposes only `id`; each concrete case carries its
    * own `meta`. Project them uniformly here so the main walk stays flat.
    */
  private def withIdMeta(d: RawTypeDef.WithId): RawNodeMeta = d match {
    case t: RawTypeDef.Interface   => t.meta
    case t: RawTypeDef.DTO         => t.meta
    case t: RawTypeDef.Enumeration => t.meta
    case t: RawTypeDef.Alias       => t.meta
    case t: RawTypeDef.Identifier  => t.meta
    case t: RawTypeDef.Adt         => t.meta
  }

  /** Mirror of `IDLPretyper.perform` projection: extract a domain's locally
    * declared simple names → `TypeId` map directly from its raw `members`.
    *
    * Used during import resolution where a `DomainMeshLoaded` may not be
    * pre-built for the referenced mesh.
    */
  private def collectLocalNames(refMesh: DomainMeshResolved): Map[String, TypeId] = {
    refMesh.members.iterator.collect {
      case d: RawTopLevelDefn.TLDBaseType => d.v.id.name -> d.v.id
      case d: RawTopLevelDefn.TLDNewtype  => d.v.id.name -> d.v.id.toAliasId
    }.toMap
  }
}
