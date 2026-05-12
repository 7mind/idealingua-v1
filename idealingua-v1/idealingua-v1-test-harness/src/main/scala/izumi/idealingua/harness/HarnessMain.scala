package izumi.idealingua.harness

import java.nio.file.Paths

/** Entry points for sbt task delegation via `(Compile / runMain)`.
  *
  * Each object accepts a single argument: the repo root path.
  *
  * PR-02 IMPL-10b: `RegenerateNewTyperMain` was retired. The harness now runs
  * `TyperImpl.NewTyper` end-to-end across all three backends, so the dedicated
  * new-typer regeneration entry point and its `regenerateGoldensNewTyper` sbt
  * task are redundant — plain `regenerateGoldens` is the canonical path.
  */
object RegenerateMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: RegenerateMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0))
    GoldenGenerator.regenerate(
      HarnessCorpus.corpusRoot(repoRoot),
      HarnessCorpus.goldenRoot(repoRoot),
    )
  }
}

object VerifyMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: VerifyMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0))
    GoldenVerifier.verify(
      HarnessCorpus.corpusRoot(repoRoot),
      HarnessCorpus.goldenRoot(repoRoot),
    )
  }
}

object WireFixturesMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: WireFixturesMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0))
    val _ = WireFixtureRunner.runAll(HarnessCorpus.wireFixturesScalaRoot(repoRoot))
    val _ = WireFixtureTypescriptRunner.runAll(
      repoRoot,
      HarnessCorpus.wireFixturesTypescriptRoot(repoRoot),
      HarnessCorpus.harnessTypescriptDir(repoRoot),
    )
    val _ = WireFixtureCSharpRunner.runAll(
      repoRoot,
      HarnessCorpus.wireFixturesCSharpRoot(repoRoot),
      HarnessCorpus.harnessCSharpDir(repoRoot),
    )
  }
}

object CrossLangMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: CrossLangMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = java.nio.file.Paths.get(args(0))
    val report   = WireFixtureCrossLangRunner.runAll(repoRoot)
    System.out.println(report.formatSummary())
  }
}
