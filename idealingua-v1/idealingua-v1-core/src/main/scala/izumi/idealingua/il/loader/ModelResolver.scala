package izumi.idealingua.il.loader

import izumi.idealingua.il.loader.verification.DuplicateDomainsRule
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.loader._
import izumi.idealingua.model.problems.IDLDiagnostics

/** Loader-side wrapper that resolves cross-domain references and collects
  * post-resolution diagnostics.
  *
  * PR-02 IMPL-10d collapsed the legacy two-phase typing/verification pipeline
  * (`IDLTyper` + `TypespaceVerifier`): both are gone. Typing now runs at
  * translate-time via `NewTyperPipeline.run`, which accumulates its own
  * structured diagnostics. The resolver's residual job is to produce a
  * `LoadedDomain.Success(path, parsed, warnings = Vector.empty)` from each
  * successfully resolved mesh and to run global checks (duplicate domain ids).
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
    f.fold(identity, parsed => LoadedDomain.Success(parsed.origin, parsed, warnings = Vector.empty))
  }
}
