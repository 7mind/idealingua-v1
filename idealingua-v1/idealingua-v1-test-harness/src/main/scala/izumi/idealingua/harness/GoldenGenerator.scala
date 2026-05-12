package izumi.idealingua.harness

import java.nio.file.{Files, Path}

object GoldenGenerator {

  /** Regenerates all golden files under `goldenRoot/{scala,typescript,csharp}/...`.
    *
    * Idempotent: clears each per-language subdirectory before writing so stale
    * files from a prior run with a different corpus do not persist. Only the
    * per-language subdirs are deleted — `goldenRoot` itself and any sibling
    * files (e.g. `.gitkeep`) are preserved.
    *
    * PR-02 IMPL-10b: the public `scalaTyper` parameter is gone — the
    * regenerator no longer accepts a per-call typer pin. Per-language typer
    * dispatch is internalized in `GoldenCompile.compileAll` (Scala + C# on
    * `TyperImpl.NewTyper`; TypeScript pinned to `TyperImpl.Legacy` pending
    * the IMPL-7b NewTyper-TS golden divergence followup).
    *
    * No logging inside; caller is responsible for progress reporting.
    */
  def regenerate(corpusRoot: Path, goldenRoot: Path): Unit = {
    val loaded   = HarnessCorpus.loadCorpus(corpusRoot)
    val produced = GoldenCompile.compileAll(loaded, goldenRoot)

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

  /** Recursively delete regular files and directories under `path`, but skip
    * symbolic links entirely (do not follow, do not delete the link itself).
    *
    * Rationale: `golden/typescript/irt` is a checked-in symlink pointing into
    * the runtime-rpc-typescript source tree so wire-fixture TS compilation can
    * resolve `IRT.*` imports. Treating it as a deletable entry erases the
    * symlink and breaks downstream `runWireFixtures` / `runCrossLangInterop`.
    * `Files.walk` enumerates symlinks as leaf entries when not configured with
    * `FileVisitOption.FOLLOW_LINKS`, so filtering them out at delete time is
    * sufficient. The harness regenerator's contract is "rewrite golden source
    * trees" — symlinks are out of scope.
    */
  private def deleteRecursively(path: Path): Unit = {
    if (!Files.exists(path)) return
    val stream = Files.walk(path)
    try {
      stream
        .sorted(java.util.Comparator.reverseOrder[Path])
        .forEach { p =>
          // Skip the root itself: callers want to clear its *contents*, not
          // remove the directory entry (subsequent writes recreate the
          // subtree via `Files.createDirectories`).
          // Skip symbolic links: see scaladoc above.
          val isRoot = p == path
          if (!isRoot && !Files.isSymbolicLink(p)) {
            // `deleteIfExists` so a non-empty directory (e.g. one still
            // containing a preserved symlink we just skipped) surfaces
            // `DirectoryNotEmptyException` rather than silently passing —
            // make the failure observable.
            val _ = Files.deleteIfExists(p)
          }
        }
    } finally {
      stream.close()
    }
  }

}
