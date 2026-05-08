package izumi.idealingua.harness

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.TypespaceCompilerBaseFacade

import java.io.File
import java.nio.file.Path

object HarnessCorpus {

  def corpusRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source")

  def goldenRoot(repoRoot: Path): Path =
    repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/golden")

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

  def loadCorpus(corpusRoot: Path): Seq[LoadedDomain.Success] = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val rules    = TypespaceCompilerBaseFacade.descriptors.flatMap(_.rules)
    val resolver = new ModelResolver(rules)
    val loaded   = context.loader.load()
    resolver.resolve(loaded).throwIfFailed().successful
  }
}
