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

  def loadCorpus(corpusRoot: Path): Seq[LoadedDomain.Success] = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val rules    = TypespaceCompilerBaseFacade.descriptors.flatMap(_.rules)
    val resolver = new ModelResolver(rules)
    val loaded   = context.loader.load()
    resolver.resolve(loaded).throwIfFailed().successful
  }
}
