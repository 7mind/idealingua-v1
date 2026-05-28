package izumi.idealingua.il.loader

import izumi.idealingua.il.loader.verification.DuplicateDomainsRule
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.loader._
import izumi.idealingua.model.problems.IDLDiagnostics
import izumi.idealingua.typer.NewTyperPipeline
import izumi.idealingua.util.Parallel

/** Loader-side wrapper that resolves cross-domain references, runs the new
  * phase-based typer, and collects post-resolution diagnostics.
  *
  * PR-02 IMPL-10d collapsed the legacy two-phase typing/verification pipeline
  * (`IDLTyper` + `TypespaceVerifier`): both are gone.
  *
  * PR-02 IMPL-13: typing moves from translate-time to load-time.  Each
  * successfully-resolved mesh is fed to `NewTyperPipeline.run`; on `Left(diags)`
  * the loader emits `LoadedDomain.VerificationFailed`, on `Right(domain)`
  * `LoadedDomain.Success(path, parsed, domain, warnings = Vector.empty)`.
  * Translators read `loaded.domain` directly — the pipeline is no longer
  * re-invoked per translator.
  */
class ModelResolver(parallel: Parallel = Parallel.Default) {

  def resolve(domains: UnresolvedDomains): LoadedModels = {
    val globalChecks = Seq(
      DuplicateDomainsRule
    )
    val importResolver = new ExternalRefResolver(domains)

    // Both passes are per-domain pure: `ExternalRefResolver.resolveReferences`
    // constructs a fresh `ExternalRefResolverPass` per call (so the `processed`
    // cache is request-scoped, not shared), and `NewTyperPipeline.run` is a
    // pure function of a single `DomainMeshResolved`.
    val typedRaw = parallel.parMap(domains.domains.results) { parsed =>
      makeLoaded(importResolver.resolveReferences(parsed))
    }

    // Family-level post-pass: enrich each Domain's `aliases` map with
    // cross-domain entries so translator dealias sites can resolve a foreign
    // `AliasId` whose target was declared in an imported domain. The per-domain
    // `AliasDealiaser` runs in isolation and never sees the foreign target;
    // without this pass `domain.aliases.get(foreignAliasId)` returns None and
    // dealias sites either throw or treat the AliasId as its own terminal
    // (both diverge from the legacy `TypespaceImpl.dealias` semantics, which
    // walked `transitivelyReferenced` for cross-domain lookups).
    val successes      = typedRaw.collect { case s: LoadedDomain.Success => s }
    val enrichedByFam  = NewTyperPipeline.finalizeCrossDomainAliases(successes.map(_.domain))
    val successByDom   = successes.zip(enrichedByFam).map { case (ls, d) => ls.copy(domain = d) }
    val replacements: Map[String, LoadedDomain.Success] = successByDom.map(s => s.parsed.id.toString -> s).toMap
    val typed: Seq[LoadedDomain] = typedRaw.map {
      case s: LoadedDomain.Success => replacements.getOrElse(s.parsed.id.toString, s)
      case other                    => other
    }

    val result = LoadedModels(typed, IDLDiagnostics.empty)

    val postDiag = globalChecks.map(_.check(result.successful)).fold(IDLDiagnostics.empty)(_ ++ _)

    result.withDiagnostics(postDiag)
  }

  private def makeLoaded(f: Either[LoadedDomain.Failure, DomainMeshResolved]): LoadedDomain = {
    f.fold(identity, runNewTyper)
  }

  private def runNewTyper(parsed: DomainMeshResolved): LoadedDomain = {
    NewTyperPipeline.run(parsed) match {
      case Right(domain) =>
        LoadedDomain.Success(parsed.origin, parsed, domain, warnings = Vector.empty)
      case Left(diagnostics) =>
        LoadedDomain.VerificationFailed(parsed.origin, parsed.id, diagnostics, warnings = Vector.empty)
    }
  }
}
