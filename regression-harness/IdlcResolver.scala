package regression_harness

import java.nio.file.{Files, Path}
import scala.sys.process.*

/** Resolves an `--old` / `--new` compiler reference to a launcher path.
 *
 *  M1 supports only `self`: builds the compiler from the current repo via
 *  `sbt idealingua-v1-compiler/stage` and returns the produced launcher.
 *
 *  M2 will add:
 *    - `git:<sha-or-tag>` — git worktree + stage, cached by sha
 *    - `path:/abs/path`   — pre-built launcher (skips build entirely)
 */
final class IdlcResolver(repoRoot: Path, scratchRoot: Path) {

  private val LauncherRel =
    "idealingua-v1/idealingua-v1-compiler/target/universal/stage/bin/idealingua-v1-compiler"

  def resolve(ref: String): Path = ref match {
    case "self"             => resolveSelf()
    case s if s.startsWith("git:")  => throw new NotImplementedError("--old/--new git:* is M2 scope")
    case s if s.startsWith("path:") => throw new NotImplementedError("--old/--new path:* is M2 scope")
    case other              => throw new IllegalArgumentException(s"unknown idlc ref: '$other'. M1 supports only 'self'.")
  }

  private def resolveSelf(): Path = {
    val launcher = repoRoot.resolve(LauncherRel)
    if (Files.isExecutable(launcher)) {
      Harness.say(s"reusing cached launcher at $launcher")
      return launcher
    }

    Harness.say("staging idealingua-v1-compiler (this may take a few minutes)…")
    val rc = Process(
      Seq("sbt", "-batch", "idealingua-v1-compiler/stage"),
      repoRoot.toFile,
    ).!
    if (rc != 0) {
      throw new RuntimeException(s"sbt stage exit=$rc")
    }
    if (!Files.isExecutable(launcher)) {
      throw new RuntimeException(s"stage succeeded but launcher missing: $launcher")
    }
    launcher
  }
}
