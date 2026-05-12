package izumi.idealingua.harness

import izumi.idealingua.translator.TyperImpl

import java.nio.file.{Files, Path}

object GoldenGenerator {

  /**
    * Regenerates all golden files under goldenRoot/{scala,typescript,csharp}/...
    *
    * Idempotent: clears each per-language subdirectory before writing so stale files from a prior
    * run with a different corpus do not persist. Only the per-language subdirs are deleted —
    * goldenRoot itself and any sibling files (e.g. .gitkeep) are preserved.
    *
    * `scalaTyper` selects the Scala backend's typer (default `TyperImpl.Legacy`).
    * Pass `TyperImpl.NewTyper` to regenerate as the new-typer output for the
    * IMPL-9 compile gate (see `GoldenCompile.compileAll` for context).
    *
    * No logging inside; caller is responsible for progress reporting.
    */
  def regenerate(corpusRoot: Path, goldenRoot: Path, scalaTyper: TyperImpl = TyperImpl.Legacy): Unit = {
    val loaded = HarnessCorpus.loadCorpus(corpusRoot)
    val produced = GoldenCompile.compileAll(loaded, goldenRoot, scalaTyper)

    // Clear per-language subdirectories before writing
    for (lang <- GoldenCompile.languages) {
      val langDir = goldenRoot.resolve(lang.toString)
      if (Files.exists(langDir)) {
        deleteRecursively(langDir)
      }
    }

    // Write all produced files
    for ((targetPath, bytes) <- produced) {
      Files.createDirectories(targetPath.getParent)
      Files.write(targetPath, bytes)
    }
  }

  private def deleteRecursively(path: Path): Unit = {
    if (!Files.exists(path)) return
    val stream = Files.walk(path)
    try {
      stream.sorted(java.util.Comparator.reverseOrder[Path]).forEach(p => Files.delete(p))
    } finally {
      stream.close()
    }
  }
}
