package izumi.idealingua.harness

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.loader.LoadedDomain

import java.io.File
import java.nio.file.Path

object HarnessCorpus {

  def corpusRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source")

  /** Build-time generated tree root (R1).
    *
    * Layout under `<repoRoot>/idealingua-v1/idealingua-v1-test-harness/target/generated-sources/test-harness/`:
    *
    *   - `scala/`              — Scala translator output (Compile sources).
    *   - `scala-mcp/`          — Scala MCP bridge output (Compile sources).
    *   - `scala-mcp-resources/mcp/` — bridge `*.mcp.json` classpath resources.
    *   - `typescript/`         — TS translator output + `irt` symlink.
    *   - `csharp/`             — C# translator output.
    *
    * Replaces the legacy committed `idealingua-v1-test-defs/golden/` tree.
    * Re-populated by the `Compile / sourceGenerators` hook on every build.
    */
  def harnessGenRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/target/generated-sources/test-harness")

  /**
    * Returns the Scala-language sub-tree of the wire-fixtures directory.
    *
    * Path: `<repoRoot>/idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala/`
    *
    * Choice: this function returns the language-specific subtree directly so that
    * WireFixtureRunner.load(root) performs a flat 2-level walk: `<root>/<wireId>/<scenario>.json`.
    * The language prefix (`scala/`) is embedded here, not in the runner, keeping the runner generic.
    */
  def wireFixturesScalaRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/scala")

  def wireFixturesTypescriptRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript")

  def harnessTypescriptDir(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/src/main/typescript")

  def wireFixturesCSharpRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/wire-fixtures/csharp")

  def harnessCSharpDir(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-harness/src/main/csharp")

  def negativeRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/negative")

  /** Returns sub-sub-directories that contain a `.must-reject` marker file. */
  def listNegativeCases(negativeRoot: Path): Seq[Path] = {
    if (!java.nio.file.Files.exists(negativeRoot)) return Seq.empty
    val s = java.nio.file.Files.walk(negativeRoot)
    try {
      val all = scala.collection.mutable.Buffer[Path]()
      val it  = s.iterator()
      while (it.hasNext) {
        val p = it.next()
        if (java.nio.file.Files.isRegularFile(p) && p.getFileName.toString.endsWith(".must-reject")) {
          all += p.getParent
        }
      }
      all.toSeq.sortBy(_.toString)
    } finally s.close()
  }

  /** Repo root for tests. Tests run from sbt's cwd which is the project root. */
  def repoRootForTests(): Path = {
    val override_ = System.getProperty("harness.repoRoot")
    if (override_ != null && override_.nonEmpty) java.nio.file.Paths.get(override_)
    else java.nio.file.Paths.get(System.getProperty("user.dir"))
  }

  def loadCorpus(corpusRoot: Path): Seq[LoadedDomain.Success] = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val resolver = new ModelResolver()
    val loaded   = context.loader.load()
    resolver.resolve(loaded).throwIfFailed().successful
  }
}
