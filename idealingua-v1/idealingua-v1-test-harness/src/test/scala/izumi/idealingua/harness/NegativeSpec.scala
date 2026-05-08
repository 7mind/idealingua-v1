package izumi.idealingua.harness

import org.scalatest.funsuite.AnyFunSuite

final class NegativeSpec extends AnyFunSuite {
  private val repoRoot     = HarnessCorpus.repoRootForTests()
  private val negativeRoot = HarnessCorpus.negativeRoot(repoRoot)

  for (caseDir <- HarnessCorpus.listNegativeCases(negativeRoot)) {
    val displayName = negativeRoot.relativize(caseDir).toString
    test(s"legacy compiler rejects $displayName") {
      intercept[Throwable] {
        HarnessCorpus.loadCorpus(caseDir)
      }
    }
  }
}
