package izumi.idealingua.compiler.testcodegen

import java.nio.file.Paths

/** Sbt-task entry point: invoked from
  * `idealingua-v1-test-harness` module's `Compile / sourceGenerators` task
  * via a forked JVM (`(idealingua-v1-compiler / Compile / runner).value.run`).
  *
  * Usage:
  *   `TestCodegenMain <repoRoot> <genRoot>`
  *
  *   - `<repoRoot>` — absolute path to the idealingua-v1 git checkout root.
  *   - `<genRoot>`  — directory to (re-)populate with generated trees.
  *                    Usually `<harnessTarget>/generated-sources/test-harness`.
  *
  * The corpus path is derived from `<repoRoot>`:
  *   `<repoRoot>/idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source`.
  */
object TestCodegenMain {
  def main(args: Array[String]): Unit = {
    require(args.length == 2, s"Usage: TestCodegenMain <repoRoot> <genRoot>, got ${args.mkString(", ")}")
    val repoRoot = Paths.get(args(0)).toAbsolutePath
    val genRoot  = Paths.get(args(1)).toAbsolutePath
    val corpus   = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source")
    TestSourceGenerator.generate(corpus, genRoot, repoRoot)
  }
}
