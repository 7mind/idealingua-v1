package izumi.idealingua.harness

import izumi.idealingua.translator.TyperImpl

import java.nio.file.Paths

/**
  * Entry points for sbt task delegation via (Compile / runMain).
  * Each object accepts a single argument: the repo root path.
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

/** PR-02 IMPL-7a.2 IMPL-9 compile gate (opt-in).
  *
  * Same shape as `RegenerateMain` but drives the Scala backend through
  * `TyperImpl.NewTyper`. After this task writes, the standard sbt
  * `idealingua-v1-test-harness/Compile/compile` (which already has
  * `golden/scala` on `unmanagedSourceDirectories`) type-checks every
  * emitted module — any new-typer codegen defect surfaces as a compile
  * error. Commit the regenerated goldens deliberately; the byte-parity
  * spec (`ScalaTranslatorByteParitySpec`) continues to compare legacy
  * vs new in memory at test time and is unaffected by which view is on
  * disk.
  */
object RegenerateNewTyperMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 1, s"Usage: RegenerateNewTyperMain <repoRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0))
    GoldenGenerator.regenerate(
      HarnessCorpus.corpusRoot(repoRoot),
      HarnessCorpus.goldenRoot(repoRoot),
      scalaTyper = TyperImpl.NewTyper,
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
    val report = WireFixtureCrossLangRunner.runAll(repoRoot)
    System.out.println(report.formatSummary())
  }
}
