package izumi.idealingua.harness

import org.scalatest.funsuite.AnyFunSuite

/** Negative-case regression suite.
  *
  * Each `.must-reject` case directory is loaded via the resolver. The
  * expectation is that loading itself throws — either the parser
  * (syntactic errors), the resolver (unresolved imports), or the
  * load-time new-typer (semantic errors: ADT conflicts, missing references,
  * cyclic structure, naming convention violations, etc.).
  *
  * PR-02 IMPL-13: the new-typer pipeline now runs at load time inside
  * `ModelResolver`; its rejections are routed through
  * `LoadedDomain.VerificationFailed` and surface via
  * `LoadedModels.throwIfFailed()` (which `HarnessCorpus.loadCorpus`
  * invokes).  This spec therefore only needs the load call — no separate
  * pipeline invocation.
  *
  * History:
  *   - Pre-IMPL-10d: the legacy typer ran inside the resolver; bad models
  *     were rejected at load.
  *   - IMPL-10d → IMPL-12 interim: typer was deferred to translate-time, so
  *     this spec invoked `NewTyperPipeline.run(d.parsed)` explicitly per
  *     domain to preserve the rejection guarantee.
  *   - IMPL-13: typer moved back to load-time (via Either-routing), so the
  *     explicit pipeline invocation became redundant.
  */
final class NegativeSpec extends AnyFunSuite {
  private val repoRoot     = HarnessCorpus.repoRootForTests()
  private val negativeRoot = HarnessCorpus.negativeRoot(repoRoot)

  for (caseDir <- HarnessCorpus.listNegativeCases(negativeRoot)) {
    val displayName = negativeRoot.relativize(caseDir).toString
    test(s"loader+new-typer rejects $displayName") {
      intercept[Throwable] {
        val _ = HarnessCorpus.loadCorpus(caseDir)
      }
    }
  }
}
