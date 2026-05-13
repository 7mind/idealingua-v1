package izumi.idealingua.il.loader

import izumi.idealingua.il.loader.verification.DuplicateDomainsRule
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.loader._
import izumi.idealingua.model.problems.IDLDiagnostics
import izumi.idealingua.typer.NewTyperPipeline

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
class ModelResolver() {

  def resolve(domains: UnresolvedDomains): LoadedModels = {
    val globalChecks = Seq(
      DuplicateDomainsRule
    )
    val importResolver = new ExternalRefResolver(domains)

    val typed = domains.domains.results
      .map(importResolver.resolveReferences)
      .map(makeLoaded)

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
