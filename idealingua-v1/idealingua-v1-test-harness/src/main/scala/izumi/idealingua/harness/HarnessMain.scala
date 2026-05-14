package izumi.idealingua.harness

import java.nio.file.Paths

/** Entry points for sbt task delegation via `(Compile / runMain)`.
  *
  * Each object accepts a single argument: the repo root path.
  *
  * R1 (2026-05-14): the `RegenerateMain` and `VerifyMain` entrypoints were
  * retired alongside the committed `golden/` tree. Per-language test sources
  * are now generated at build time under
  * `<harnessModuleTarget>/generated-sources/test-harness/` by the
  * `idealingua-v1-compiler` module's `TestCodegenMain` entrypoint, wired into
  * the harness module's `Compile / sourceGenerators`.
  */

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
