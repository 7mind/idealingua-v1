package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.{DomainId, TypeId}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTypeDef}
import izumi.idealingua.model.il.ast.raw.defns.RawTypeDef.{ForeignType, NewType}
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, FamilyIndex}

/** Phase 1 — `ScopeBuilder`.
  *
  * Walks the raw `DomainMeshLoaded` for a single domain, resolves local and
  * imported names against the cross-domain `FamilyIndex` (Phase 0 output), and
  * emits diagnostics for clashes / unsupported features. Output is the internal
  * `ScopedDomain` value, consumed by Phase 2 (`NameResolver`).
  *
  * Per master plan §3 Phase 1 (line 231): input is
  * `(DomainId, DomainMeshLoaded, FamilyIndex)` — this closes F18 (the IMPL-2
  * bypass that consumed `DomainMeshLoaded` directly without the family index).
  *
  * Cross-domain import resolution uses `family.domains(importedDomainId)` rather
  * than the embedded `parsed.defn.referenced` map; this eliminates the duplicate
  * recursive resolution that the IMPL-2 bypass performed.
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

  /** Build a `ScopedDomain` from a `DomainMeshLoaded` and its `FamilyIndex`.
    *
    * @param rootId  The domain identifier for `parsed` (must be a key in `family.domains`).
    * @param parsed  The loaded domain AST for `rootId` (`family.domains(rootId)`).
    * @param family  The full cross-domain family index (Phase 0 output).
    *
    * Per C8/L1 (diagnostics-mode): no exceptions are thrown for user-visible
    * errors — every problem becomes a `Diagnostic` in `ScopedDomain.diagnostics`.
    */
  def apply(rootId: DomainId, parsed: DomainMeshLoaded, family: FamilyIndex): ScopedDomain = {
    val domainPos: InputPosition = parsed.meta.position

    val localBuilder = scala.collection.mutable.LinkedHashMap.empty[String, TypeId]
    val indexBuilder = scala.collection.mutable.LinkedHashMap.empty[TypeId, RawTypeDef]
    val diagBuf      = scala.collection.mutable.ArrayBuffer.empty[Diagnostic]

    parsed.types.foreach {
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
    parsed.imports.foreach {
      si =>
        val importedAs   = si.imported.importedAs
        val originalName = si.imported.name
        // Resolve via the family index: no recursive re-typing of imported domains.
        family.domains.get(si.domain) match {
          case Some(importedDomain) =>
            collectLocalNames(importedDomain).get(originalName) match {
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
      domainId      = rootId,
      localNames    = localNames,
      importedNames = importedBuilder.toMap,
      index         = indexBuilder.toMap,
      raw           = parsed,
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

  /** Extract the locally-declared simple names → `TypeId` map from a
    * `DomainMeshLoaded` (using the pre-extracted `types` field).
    *
    * Used during import resolution to look up the imported domain's local names
    * via `family.domains(importedDomainId)`.
    */
  private def collectLocalNames(domain: DomainMeshLoaded): Map[String, TypeId] = {
    domain.types.iterator.collect {
      case d: RawTypeDef.WithId => d.id.name -> (d.id: TypeId)
      case d: NewType           => d.id.name -> (d.id.toAliasId: TypeId)
    }.toMap
  }
}
