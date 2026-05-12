package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{AbstractIndefiniteId, Builtin, DomainId, IndefiniteId, TypeId}
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
        val tid  = normalize(d.id, rootId)
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
        // F-clone-newtype (PR-02 IMPL-7a.2-Fh2): a `clone X into Y { ... }`
        // declaration with non-empty modifiers materializes as the same kind
        // as `X` (DTO/Interface/Identifier), not as an `AliasId`. Mirrors
        // legacy `IDLTyper.fixType` arm for `RawTypeDef.NewType(_, _, Some(_))`
        // (`IDLTyper.scala:174-189`). For empty-modifier clones we keep the
        // legacy alias materialization (`:171-172`).
        val tid  = newtypeRegisteredId(d, rootId, parsed, family)
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
            collectLocalNames(importedDomain, family).get(originalName) match {
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
    * via `family.domains(importedDomainId)`.  Mirrors the registration path
    * above: returned `TypeId`s are normalised so a same-domain alias declared
    * in `domain.id` carries `path.domain == domain.id` (legacy parity with
    * `IDLPostTyper.fixPkg`).
    */
  private def collectLocalNames(domain: DomainMeshLoaded, family: FamilyIndex): Map[String, TypeId] = {
    domain.types.iterator.collect {
      case d: RawTypeDef.WithId => d.id.name -> normalize(d.id, domain.id)
      case d: NewType           => d.id.name -> newtypeRegisteredId(d, domain.id, domain, family)
    }.toMap
  }

  /** Determine the registered TypeId for a NewType (with or without modifiers).
    *
    * With empty modifiers the clone is a pure alias (legacy
    * `IDLTyper.scala:171-172`). With non-empty modifiers it adopts the kind of
    * its source (legacy `:174-189`): DTO source → DTOId; Interface source →
    * InterfaceId; Identifier source → IdentifierId (legacy throws for any
    * other source kind, which we surface here as a fallback to AliasId so the
    * IR stays well-formed).
    */
  private[phase] def newtypeRegisteredId(d: NewType, rootId: DomainId, parsed: DomainMeshLoaded, family: FamilyIndex): TypeId = {
    if (d.modifiers.isEmpty) {
      normalize(d.id.toAliasId, rootId)
    } else {
      newtypeKindedId(d, rootId, parsed, family).getOrElse(normalize(d.id.toAliasId, rootId))
    }
  }

  /** Resolve the source RawTypeDef and return the corresponding kinded TypeId
    * for the clone (rooted at `rootId`). Consults the local `parsed.types`
    * first, then the family index for cross-domain sources.
    */
  private def newtypeKindedId(d: NewType, rootId: DomainId, parsed: DomainMeshLoaded, family: FamilyIndex): Option[TypeId] = {
    findRawSource(d.source, parsed, family) match {
      case Some(_: RawTypeDef.DTO)        => Some(normalize(d.id.toDataId, rootId))
      case Some(_: RawTypeDef.Interface)  => Some(normalize(d.id.toInterfaceId, rootId))
      case Some(_: RawTypeDef.Identifier) => Some(normalize(d.id.toIdId, rootId))
      case _                              => None
    }
  }

  /** Look up the raw type referenced by a `NewType.source` in the local domain
    * (`parsed`) or, if qualified, the cross-domain family scope. Returns the
    * raw type definition so `newtypeKindedId` can branch on its kind.
    */
  private[phase] def findRawSource(ref: AbstractIndefiniteId, parsed: DomainMeshLoaded, family: FamilyIndex): Option[RawTypeDef] = ref match {
    case id: IndefiniteId =>
      val isLocal = id.pkg.isEmpty || id.pkg == parsed.id.toPackage
      if (isLocal) findByNameInDomain(id.name, parsed)
      else {
        val otherDomain = DomainId(id.pkg.init, id.pkg.last)
        family.domains.get(otherDomain).flatMap(d => findByNameInDomain(id.name, d))
      }
    case _ => None
  }

  private def findByNameInDomain(name: String, domain: DomainMeshLoaded): Option[RawTypeDef] = {
    domain.types.collectFirst {
      case d: RawTypeDef.WithId if d.id.name == name => d
      case d: NewType           if d.id.name == name => d
    }
  }

  /** Rewrite a parser-produced `TypeId` so its `TypePath.domain` reflects the
    * owning `rootId` rather than `DomainId.Undefined`. Mirrors the legacy
    * `IDLPostTyper.fixPkg` / `fixServiceId` / `fixBuzzerId` / `fixStreamsId`
    * normalisation step that the new typer was missing — the parser
    * (`ParsedId.typePath`) produces `DomainId.Undefined` for any locally
    * declared identifier (no qualifying package), and the legacy typer rewrites
    * those to the owning domain immediately. Without this step, downstream
    * consumers see `path.domain == DomainId.Undefined` for every locally
    * declared `TypeId`, causing rendering divergences in same-domain
    * references (alias targets, ADT members, field types, etc.).
    *
    * Built-ins (whose `path.domain` is `DomainId.Builtin`) and types that
    * already carry a definite owning domain are returned unchanged.
    */
  private def normalize(tid: TypeId, rootId: DomainId): TypeId = tid match {
    case _: Builtin                                            => tid
    case t: DTOId        if t.path.domain == DomainId.Undefined => t.copy(path = t.path.copy(domain = rootId))
    case t: InterfaceId  if t.path.domain == DomainId.Undefined => t.copy(path = t.path.copy(domain = rootId))
    case t: EnumId       if t.path.domain == DomainId.Undefined => t.copy(path = t.path.copy(domain = rootId))
    case t: AliasId      if t.path.domain == DomainId.Undefined => t.copy(path = t.path.copy(domain = rootId))
    case t: IdentifierId if t.path.domain == DomainId.Undefined => t.copy(path = t.path.copy(domain = rootId))
    case t: AdtId        if t.path.domain == DomainId.Undefined => t.copy(path = t.path.copy(domain = rootId))
    case t: ServiceId    if t.domain == DomainId.Undefined      => t.copy(domain = rootId)
    case t: BuzzerId     if t.domain == DomainId.Undefined      => t.copy(domain = rootId)
    case t: StreamsId    if t.domain == DomainId.Undefined      => t.copy(domain = rootId)
    case _ => tid
  }

  /** Public-to-package normalisation entry point so `NameResolver` can apply
    * the same rewrite to the `d.id` it carries from the raw AST into the IR.
    * Same semantics as the private `normalize`.
    */
  private[phase] def normalizeId(tid: TypeId, rootId: DomainId): TypeId = normalize(tid, rootId)
}
