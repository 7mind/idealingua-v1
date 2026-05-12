package izumi.idealingua.harness

import izumi.idealingua.translator.compat.NewTyperPipeline
import org.scalatest.funsuite.AnyFunSuite

/** Negative-case regression suite.
  *
  * Each `.must-reject` case directory is loaded via the resolver and then
  * fed to the new typer (`NewTyperPipeline.run`). The expectation is that
  * at least one of those two stages raises an exception — either the parser
  * (syntactic errors), the resolver (unresolved imports), or the new typer
  * (semantic errors: ADT conflicts, missing references, cyclic structure,
  * naming convention violations, etc.).
  *
  * PR-02 IMPL-10d: the legacy typer + `TypespaceVerifier` are gone, so the
  * resolver alone no longer rejects semantically-invalid models. Typing now
  * runs at translate-time via `NewTyperPipeline.run`; this spec invokes it
  * explicitly on each domain to preserve the original "loading rejects bad
  * models" guarantee for the corpus of `.must-reject` fixtures.
  */
final class NegativeSpec extends AnyFunSuite {
  private val repoRoot     = HarnessCorpus.repoRootForTests()
  private val negativeRoot = HarnessCorpus.negativeRoot(repoRoot)

  for (caseDir <- HarnessCorpus.listNegativeCases(negativeRoot)) {
    val displayName = negativeRoot.relativize(caseDir).toString
    test(s"loader+new-typer rejects $displayName") {
      intercept[Throwable] {
        val loaded = HarnessCorpus.loadCorpus(caseDir)
        loaded.foreach(d => NewTyperPipeline.run(d.parsed))
      }
    }
  }
}
