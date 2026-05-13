package regression_harness

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

/** Resolves an `--old` / `--new` compiler reference to a launcher + runtime
 *  artifacts.
 *
 *  M2 supports:
 *    - `self`         — current working tree (in-place sbt stage; publishLocal
 *                       runtime to the user's ivy2Local).
 *    - `git:<ref>`    — `git worktree add <ref>` into a per-sha cache dir,
 *                       sbt stage, publishLocal runtime to a per-sha m2 dir.
 *                       Subsequent invocations on the same ref skip everything
 *                       except the version lookup.
 *
 *  M3+ scope: `path:<launcher>` (pre-built launcher, skip build).
 *
 *  Per-sha cache layout (under `target/regression-harness/cache/idlc/<sha>/`):
 *
 *      launcher                  -- copy of the staged idlc binary
 *      runtimeVersion.txt        -- version that was publishLocal-ed
 *      m2/                       -- local maven repo (`-Dmaven.repo.local`)
 *      .ivy2/                    -- sbt-internal ivy cache (per plan R2)
 *      .coursier/                -- sbt-internal coursier cache (per plan R2)
 *      .sbt/                     -- sbt-internal global base
 *      worktree/                 -- ephemeral; removed unless --keep-worktrees
 */
final class IdlcResolver(
  repoRoot:      Path,
  scratchRoot:   Path,
  keepWorktree:  Boolean,
) {

  private val StageRel =
    "idealingua-v1/idealingua-v1-compiler/target/universal/stage"
  private val LauncherRel =
    s"$StageRel/bin/idealingua-v1-compiler"

  private val cacheRoot: Path = repoRoot.resolve("target/regression-harness/cache/idlc")

  /** Maven artifact stem (no scala suffix). Both modules are crossProjects;
   *  the publish target is the JVM concretization (`<stem>JVM`). The published
   *  artifact name is `<stem>_<scalaBinary>` — what the scala-cli template
   *  references via `using dep "io.7mind.izumi::<stem>:<version>"`.
   */
  private val RuntimeModules: Seq[String] = Seq(
    "idealingua-v1-model",
    "idealingua-v1-runtime-rpc-scala",
  )

  /** sbt project keys to publish (JVM cross-projection of each module). */
  private def publishTargets(action: String): Seq[String] =
    RuntimeModules.map(m => s"${m}JVM/$action")

  def resolve(ref: String): IdlcResolution = ref match {
    case "self"                     => resolveSelf()
    case s if s.startsWith("git:")  => resolveGit(s.stripPrefix("git:"))
    case s if s.startsWith("path:") => throw new NotImplementedError("--old/--new path:* is M3+ scope")
    case other                      => throw new IllegalArgumentException(
      s"unknown idlc ref: '$other'. Supported: 'self', 'git:<sha|tag|branch>'.")
  }

  // -------------------- self --------------------

  private def resolveSelf(): IdlcResolution = {
    val launcher = repoRoot.resolve(LauncherRel)
    if (!Files.isExecutable(launcher)) {
      Harness.say("staging idealingua-v1-compiler (self)…")
      val rc = Process(Seq("sbt", "-batch", "idealingua-v1-compiler/stage"), repoRoot.toFile).!
      if (rc != 0) throw new RuntimeException(s"sbt stage exit=$rc")
      if (!Files.isExecutable(launcher)) {
        throw new RuntimeException(s"stage succeeded but launcher missing: $launcher")
      }
    } else {
      Harness.say(s"reusing self launcher at $launcher")
    }

    val version = readVersion(repoRoot)
    val ivy2Local = Path.of(sys.props.getOrElse("user.home", "")).resolve(".ivy2/local")

    if (!hasRuntimeArtifacts(ivy2Local, version, ivy2 = true)) {
      Harness.say(s"publishLocal runtime modules (self, ++ 2.13.18, version=$version)…")
      val rc = Process(
        Seq("sbt", "-batch", "++ 2.13.18") ++ publishTargets("publishLocal"),
        repoRoot.toFile,
      ).!
      if (rc != 0) throw new RuntimeException(s"self publishLocal exit=$rc")
    } else {
      Harness.say(s"reusing ivy2Local runtime artifacts (version=$version)")
    }

    IdlcResolution(
      launcher        = launcher,
      runtimeRepoPath = ivy2Local,
      runtimeRepoUri  = "ivy2Local",
      runtimeVersion  = version,
    )
  }

  // -------------------- git --------------------

  private def resolveGit(ref: String): IdlcResolution = {
    val sha     = resolveSha(ref)
    val shortSha = sha.take(12)
    val perSha   = cacheRoot.resolve(shortSha)
    Files.createDirectories(perSha)

    val cachedStage    = perSha.resolve("stage")
    val cachedLauncher = cachedStage.resolve("bin/idealingua-v1-compiler")
    val m2Dir          = perSha.resolve("m2")
    val versionFile    = perSha.resolve("runtimeVersion.txt")

    val cachedVersion = readFileOpt(versionFile).getOrElse("")
    if (Files.isExecutable(cachedLauncher) && hasRuntimeArtifacts(m2Dir, cachedVersion, ivy2 = false)) {
      Harness.say(s"cache hit: $shortSha → $cachedLauncher (runtime $cachedVersion)")
      IdlcResolution(
        launcher        = cachedLauncher,
        runtimeRepoPath = m2Dir,
        runtimeRepoUri  = m2Dir.toUri.toString,
        runtimeVersion  = cachedVersion,
      )
    } else buildGit(ref, sha, shortSha, perSha, cachedStage, cachedLauncher, m2Dir, versionFile)
  }

  private def buildGit(
    ref: String, sha: String, shortSha: String, perSha: Path,
    cachedStage: Path, cachedLauncher: Path, m2Dir: Path, versionFile: Path,
  ): IdlcResolution = {
    Harness.say(s"cache miss: building idlc + runtime for git ref '$ref' (sha=$shortSha)")
    val worktree = perSha.resolve("worktree")
    addWorktree(worktree, sha)

    val ivyHome     = perSha.resolve(".ivy2")
    val coursierHome= perSha.resolve(".coursier")
    val sbtGlobal   = perSha.resolve(".sbt")
    Files.createDirectories(ivyHome)
    Files.createDirectories(coursierHome)
    Files.createDirectories(sbtGlobal)
    Files.createDirectories(m2Dir)

    val sbtSysProps = Seq(
      s"-Dsbt.global.base=${sbtGlobal.toAbsolutePath}",
      s"-Dsbt.ivy.home=${ivyHome.toAbsolutePath}",
      s"-Divy.home=${ivyHome.toAbsolutePath}",
      s"-Dcoursier.cache=${coursierHome.toAbsolutePath}",
      s"-Dmaven.repo.local=${m2Dir.toAbsolutePath}",
    )

    // - `idealingua-v1-compiler/stage` runs at the project's default Scala
    //   (3.8.3); the launcher script is dialect-agnostic.
    // - `++ 2.13.18` then switches to Scala 2.13.18 so the runtime artifacts
    //   published below match the `_2.13` coordinates referenced by the
    //   scala-cli template. The generated Scala sources rely on circe-derivation
    //   (Scala-2-only); see M1 finding #4 in tasks.md.
    Harness.say("worktree: sbt stage + 2.13 publishM2 (this may take a few minutes)…")
    val rc = Process(
      Seq("sbt", "-batch") ++ sbtSysProps ++
        Seq("idealingua-v1-compiler/stage", "++ 2.13.18") ++
        publishTargets("publishM2"),
      worktree.toFile,
    ).!
    if (rc != 0) {
      throw new RuntimeException(s"git[$shortSha] sbt stage+publishM2 exit=$rc — worktree retained at $worktree for diagnosis")
    }

    val producedStage    = worktree.resolve(StageRel)
    val producedLauncher = worktree.resolve(LauncherRel)
    if (!Files.isExecutable(producedLauncher)) {
      throw new RuntimeException(s"git[$shortSha] stage finished but launcher missing: $producedLauncher")
    }

    // Copy the full sbt-native-packager stage tree (bin/ + lib/). The launcher
    // bash script references its libs via absolute path resolution relative to
    // its own bin/, so we MUST preserve the whole tree to survive worktree
    // removal.
    if (Files.exists(cachedStage)) deleteRecursively(cachedStage)
    copyTree(producedStage, cachedStage)
    cachedLauncher.toFile.setExecutable(true, false)

    val version = readVersion(worktree)
    Files.write(versionFile, version.getBytes(StandardCharsets.UTF_8))

    if (!hasRuntimeArtifacts(m2Dir, version, ivy2 = false)) {
      throw new RuntimeException(
        s"git[$shortSha] publishM2 did not produce expected artifacts under $m2Dir " +
        s"(version=$version, modules=${RuntimeModules.mkString(",")})"
      )
    }

    if (!keepWorktree) {
      Harness.say(s"removing worktree $worktree (launcher cached; runtime in $m2Dir)")
      removeWorktree(worktree)
    } else {
      Harness.say(s"keeping worktree $worktree (--keep-worktrees)")
    }

    IdlcResolution(
      launcher        = cachedLauncher,
      runtimeRepoPath = m2Dir,
      runtimeRepoUri  = m2Dir.toUri.toString,
      runtimeVersion  = version,
    )
  }

  /** Resolve any rev-ish (sha, short sha, tag, branch) to a full sha. */
  private def resolveSha(ref: String): String = {
    val out = new StringBuilder
    val err = new StringBuilder
    val rc = Process(Seq("git", "rev-parse", "--verify", s"$ref^{commit}"), repoRoot.toFile)
      .!(ProcessLogger(o => out.append(o).append('\n'), e => err.append(e).append('\n')))
    if (rc != 0) {
      throw new RuntimeException(s"git rev-parse '$ref' failed (exit=$rc): ${err.toString.trim}")
    }
    val sha = out.toString.trim
    if (sha.length < 7 || !sha.forall(c => "0123456789abcdef".contains(c))) {
      throw new RuntimeException(s"unexpected git rev-parse output for '$ref': '$sha'")
    }
    sha
  }

  private def addWorktree(worktree: Path, sha: String): Unit = {
    // Best-effort cleanup of any prior registration / leftover dir. Both
    // `worktree remove` and `worktree prune` are tolerant of "not found" /
    // "no stale entries" and exit 0.
    Process(
      Seq("git", "worktree", "remove", "--force", worktree.toAbsolutePath.toString),
      repoRoot.toFile,
    ).!(ProcessLogger(_ => (), _ => ()))
    Process(Seq("git", "worktree", "prune"), repoRoot.toFile).!(ProcessLogger(_ => (), _ => ()))
    if (Files.exists(worktree)) deleteRecursively(worktree)

    val rc = Process(
      Seq("git", "worktree", "add", "--detach", worktree.toAbsolutePath.toString, sha),
      repoRoot.toFile,
    ).!
    if (rc != 0) {
      throw new RuntimeException(
        s"git worktree add failed (exit=$rc). If a previous worktree is registered, run: " +
        s"`git worktree remove --force $worktree` then retry."
      )
    }
  }

  private def removeWorktree(worktree: Path): Unit = {
    // `git worktree remove` first (clean record), then physical fallback.
    Process(Seq("git", "worktree", "remove", "--force", worktree.toAbsolutePath.toString), repoRoot.toFile).!
    if (Files.exists(worktree)) {
      deleteRecursively(worktree)
    }
    Process(Seq("git", "worktree", "prune"), repoRoot.toFile).!
  }

  private def copyTree(src: Path, dst: Path): Unit = {
    Files.walk(src).iterator().asScala.foreach { srcEntry =>
      val rel    = src.relativize(srcEntry)
      val dstEntry = dst.resolve(rel.toString)
      if (Files.isDirectory(srcEntry)) {
        Files.createDirectories(dstEntry)
      } else {
        Files.createDirectories(dstEntry.getParent)
        Files.copy(srcEntry, dstEntry, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
      }
    }
  }

  private def deleteRecursively(p: Path): Unit = {
    if (Files.exists(p)) {
      Files.walk(p).iterator().asScala.toList.reverse.foreach { entry =>
        try Files.delete(entry) catch { case _: Throwable => () }
      }
    }
  }

  /** Probe: does the publishLocal target hold the expected `_2.13` artifacts?
   *  The scala-cli template targets Scala 2.13.18 (see M1 finding #4 in
   *  tasks.md), so we require `<artifact>_2.13` specifically.
   *
   *  ivy2 local:  <repo>/io.7mind.izumi/<artifact>_2.13/<version>/…
   *  maven local: <repo>/io/7mind/izumi/<artifact>_2.13/<version>/…
   */
  private def hasRuntimeArtifacts(repoPath: Path, version: String, ivy2: Boolean): Boolean = {
    if (version.isEmpty || !Files.isDirectory(repoPath)) false
    else {
      val groupDir =
        if (ivy2) repoPath.resolve("io.7mind.izumi")
        else      repoPath.resolve("io").resolve("7mind").resolve("izumi")
      if (!Files.isDirectory(groupDir)) false
      else {
        RuntimeModules.forall { module =>
          Files.isDirectory(groupDir.resolve(s"${module}_2.13").resolve(version))
        }
      }
    }
  }

  private def readVersion(root: Path): String = {
    val versionSbt = root.resolve("version.sbt")
    if (!Files.isRegularFile(versionSbt)) throw new RuntimeException(s"missing version.sbt at $root")
    val text = new String(Files.readAllBytes(versionSbt), StandardCharsets.UTF_8)
    val rx   = """"([^"]+)"""".r
    rx.findFirstMatchIn(text).map(_.group(1))
      .getOrElse(throw new RuntimeException(s"could not parse version from $versionSbt"))
  }

  private def readFileOpt(p: Path): Option[String] = {
    if (Files.isRegularFile(p)) Some(new String(Files.readAllBytes(p), StandardCharsets.UTF_8).trim)
    else None
  }
}
