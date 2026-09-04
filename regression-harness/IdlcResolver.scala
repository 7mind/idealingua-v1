package regression_harness

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

/** Resolves an `--old` / `--new` compiler reference to a launcher + runtime
  *  artifacts.
  *
  *  Supports:
  *    - `self`         — current working tree (in-place sbt stage; publishLocal
  *                       runtime to the user's ivy2Local when Scala is the
  *                       target language).
  *    - `git:<ref>`    — `git worktree add <ref>` into a per-sha cache dir,
  *                       sbt stage, publishM2 runtime (Scala only) to a per-sha
  *                       m2 dir. Subsequent invocations on the same ref skip
  *                       everything except the version lookup.
  *    - `path:<p>`     — pre-built idlc. `<p>` is either a launcher binary
  *                       (e.g. `…/target/universal/stage/bin/idealingua-v1-compiler`)
  *                       or a directory containing one (a stage root, or its
  *                       `bin/` subdir). Runtime version comes from a sibling
  *                       `version.txt` next to the launcher; otherwise from the
  *                       `--runtime-version` flag. No publishM2 for path: refs;
  *                       Scala consumers must point at an external resolver.
  *
  *  Per-sha cache layout (under `target/regression-harness/cache/idlc/<sha>/`):
  *
  *      stage/                    -- copy of the staged sbt-native-packager tree
  *      stage/bin/idealingua-v1-compiler -- launcher
  *      runtimeVersion.txt        -- version that was publishM2-ed
  *      m2/                       -- local maven repo (Scala only)
  *      .ivy2/                    -- sbt-internal ivy cache (per plan R2)
  *      .coursier/                -- sbt-internal coursier cache (per plan R2)
  *      .sbt/                     -- sbt-internal global base
  *      worktree/                 -- ephemeral; removed unless --keep-worktrees
  *
  *  publishM2 short-circuit (Item A): for non-Scala adapters (TypeScript, C#)
  *  the per-sha m2 dir is unused (TS inlines runtime via `withRuntime=true`;
  *  C# uses NuGet for Newtonsoft.Json with no idl-runtime feed). The resolver
  *  accepts a `targetLang` argument and skips publishM2 entirely when the
  *  caller is not Scala, materially speeding up TS/C# resolutions.
  */
final class IdlcResolver(
  repoRoot: Path,
  scratchRoot: Path,
  keepWorktree: Boolean,
) {

  private val StageRel =
    "idealingua-v1/idealingua-v1-compiler/target/universal/stage"
  private val LauncherRel =
    s"$StageRel/bin/idealingua-v1-compiler"

  /** Source roots whose `.scala` mtimes feed the self-launcher staleness check.
    *  Anything newer than the staged launcher under any of these triggers a
    *  restage. The compiler module owns the launcher's `Main`; transpilers
    *  contribute the codegen logic; model is shared IR. Other modules
    *  (runtimes, tests, harness itself) do not affect the staged idlc binary.
    */
  private val SelfWatchedSources: Seq[String] = Seq(
    "idealingua-v1/idealingua-v1-compiler/src/main/scala",
    "idealingua-v1/idealingua-v1-transpilers/src/main/scala",
    "idealingua-v1/idealingua-v1-model/src/main/scala",
    "idealingua-v1/idealingua-v1-core/src/main/scala",
  )

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

  /** Resolve a reference. `targetLang` is the language adapter the caller will
    *  ultimately drive; only `"scala"` triggers Scala-runtime publish steps.
    *  `runtimeVersionOverride` is honored only for `path:` refs.
    */
  def resolve(
    ref: String,
    targetLang: String                     = "scala",
    runtimeVersionOverride: Option[String] = None,
  ): IdlcResolution = {
    val needsScalaRuntime = targetLang == "scala"
    ref match {
      case "self"                     => resolveSelf(needsScalaRuntime)
      case s if s.startsWith("git:")  => resolveGit(s.stripPrefix("git:"), needsScalaRuntime)
      case s if s.startsWith("path:") => resolvePath(s.stripPrefix("path:"), runtimeVersionOverride, needsScalaRuntime)
      case other => throw new IllegalArgumentException(s"unknown idlc ref: '$other'. Supported: 'self', 'git:<sha|tag|branch>', 'path:<launcher-or-stage-dir>'.")
    }
  }

  // -------------------- self --------------------

  private def resolveSelf(needsScalaRuntime: Boolean): IdlcResolution = {
    val launcher = repoRoot.resolve(LauncherRel)
    val needsStage: Option[String] =
      if (!Files.isExecutable(launcher)) Some("launcher missing or non-executable")
      else staleStagedLauncher(launcher)

    needsStage match {
      case Some(reason) =>
        Harness.say(s"staging idealingua-v1-compiler (self)… [$reason]")
        val rc = Process(Seq("sbt", "-batch", "idealingua-v1-compiler/stage"), repoRoot.toFile).!
        if (rc != 0)
          throw new RuntimeException(
            s"sbt stage failed (exit=$rc). Expected: a staged launcher at $launcher. Observed: sbt exited non-zero. Next: re-run `sbt idealingua-v1-compiler/stage` interactively to see the failure."
          )
        if (!Files.isExecutable(launcher)) {
          throw new RuntimeException(
            s"sbt stage succeeded but launcher missing. Expected: $launcher to exist and be executable. Observed: file missing. Next: inspect `target/universal/stage/` under idealingua-v1-compiler."
          )
        }
      case None =>
        Harness.say(s"reusing self launcher at $launcher")
    }

    val version   = readVersion(repoRoot)
    val ivy2Local = Path.of(sys.props.getOrElse("user.home", "")).resolve(".ivy2/local")

    if (!needsScalaRuntime) {
      Harness.say(s"skipping Scala-runtime publishLocal (self, target lang is not scala; version=$version)")
      return IdlcResolution(
        launcher        = launcher,
        runtimeRepoPath = ivy2Local,
        runtimeRepoUri  = "ivy2Local",
        runtimeVersion  = version,
      )
    }

    if (!hasRuntimeArtifacts(ivy2Local, version, ivy2 = true)) {
      Harness.say(s"publishLocal runtime modules (self, ++ 2.13.18, version=$version)…")
      val rc = Process(
        Seq("sbt", "-batch", "++ 2.13.18") ++ publishTargets("publishLocal"),
        repoRoot.toFile,
      ).!
      if (rc != 0)
        throw new RuntimeException(
          s"self publishLocal failed (exit=$rc). Expected: $RuntimeModules under $ivy2Local for version=$version. Observed: sbt exited non-zero. Next: re-run `sbt ++ 2.13.18 ${publishTargets("publishLocal")
              .mkString(" ")}` interactively."
        )
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

  /** Detect a stale `self` launcher: if any watched `.scala` source has a
    *  newer mtime than the launcher binary, the staged tree predates the
    *  current working copy and must be restaged. Returns `Some(reason)` if
    *  stale, `None` if fresh.
    *
    *  Walks `SelfWatchedSources` (compiler + transpilers + model + core
    *  Scala source roots) and compares each `.scala` file's mtime to the
    *  launcher's. Returns at the first newer file rather than collecting all
    *  of them — one is enough to trigger restage. Quiet on `NoSuchFile` for
    *  missing source roots (cross-project layouts where one of the modules
    *  isn't checked out — unlikely in the in-tree harness but cheap to be
    *  defensive about).
    */
  private def staleStagedLauncher(launcher: Path): Option[String] = {
    val launcherMtime = Files.getLastModifiedTime(launcher).toMillis
    def firstNewerUnder(root: Path): Option[Path] = {
      if (!Files.isDirectory(root)) return None
      val stream = Files.walk(root)
      try {
        stream
          .iterator()
          .asScala
          .filter(p => Files.isRegularFile(p) && p.getFileName.toString.endsWith(".scala"))
          .find(p => Files.getLastModifiedTime(p).toMillis > launcherMtime)
      } finally {
        stream.close()
      }
    }
    SelfWatchedSources.iterator
      .map(repoRoot.resolve)
      .flatMap(r => firstNewerUnder(r).iterator)
      .nextOption()
      .map(p => s"stale launcher: ${repoRoot.relativize(p)} newer than ${repoRoot.relativize(launcher)}")
  }

  // -------------------- git --------------------

  private def resolveGit(ref: String, needsScalaRuntime: Boolean): IdlcResolution = {
    val sha      = resolveSha(ref)
    val shortSha = sha.take(12)
    val perSha   = cacheRoot.resolve(shortSha)
    Files.createDirectories(perSha)

    val cachedStage    = perSha.resolve("stage")
    val cachedLauncher = cachedStage.resolve("bin/idealingua-v1-compiler")
    val m2Dir          = perSha.resolve("m2")
    val versionFile    = perSha.resolve("runtimeVersion.txt")

    val cachedVersion = readFileOpt(versionFile).getOrElse("")
    val runtimeOk =
      if (needsScalaRuntime) hasRuntimeArtifacts(m2Dir, cachedVersion, ivy2 = false)
      else cachedVersion.nonEmpty
    if (Files.isExecutable(cachedLauncher) && runtimeOk) {
      val repoUri = if (needsScalaRuntime) m2Dir.toUri.toString else "ivy2Local"
      Harness.say(s"cache hit: $shortSha → $cachedLauncher (runtime $cachedVersion, scala-runtime=$needsScalaRuntime)")
      IdlcResolution(
        launcher        = cachedLauncher,
        runtimeRepoPath = if (needsScalaRuntime) m2Dir else Path.of(sys.props.getOrElse("user.home", "")).resolve(".ivy2/local"),
        runtimeRepoUri  = repoUri,
        runtimeVersion  = cachedVersion,
      )
    } else buildGit(ref, sha, shortSha, perSha, cachedStage, cachedLauncher, m2Dir, versionFile, needsScalaRuntime)
  }

  private def buildGit(
    ref: String,
    sha: String,
    shortSha: String,
    perSha: Path,
    cachedStage: Path,
    cachedLauncher: Path,
    m2Dir: Path,
    versionFile: Path,
    needsScalaRuntime: Boolean,
  ): IdlcResolution = {
    Harness.say(s"cache miss: building idlc + runtime for git ref '$ref' (sha=$shortSha)")
    val worktree = perSha.resolve("worktree")
    addWorktree(worktree, sha)

    val ivyHome      = perSha.resolve(".ivy2")
    val coursierHome = perSha.resolve(".coursier")
    val sbtGlobal    = perSha.resolve(".sbt")
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
    // - `++ 3.9.0` then switches to Scala 2.13.18 so the runtime artifacts
    //   published below match the `_2.13` coordinates referenced by the
    //   scala-cli template. The generated Scala sources rely on circe-derivation
    //   (Scala-2-only); see M1 finding #4 in tasks.md.
    // - publishM2 is conditional (Item A short-circuit): TS uses
    //   `withRuntime=true` to inline the runtime; C# uses Newtonsoft.Json from
    //   NuGet. Only Scala consumes the per-sha m2 dir.
    val sbtTargets =
      if (needsScalaRuntime) Seq("idealingua-v1-compiler/stage", "++ 3.9.0") ++ publishTargets("publishM2")
      else Seq("idealingua-v1-compiler/stage")
    val phase = if (needsScalaRuntime) "stage + 2.13 publishM2" else "stage (no publishM2 — non-Scala target)"
    Harness.say(s"worktree: sbt $phase (this may take a few minutes)…")
    val rc = Process(
      Seq("sbt", "-batch") ++ sbtSysProps ++ sbtTargets,
      worktree.toFile,
    ).!
    if (rc != 0) {
      throw new RuntimeException(
        s"sbt build failed at git[$shortSha] (exit=$rc). " +
        s"Expected: $phase to succeed and produce a launcher at $LauncherRel. " +
        s"Observed: sbt exited non-zero; worktree retained at $worktree for diagnosis. " +
        s"Next: cd to that worktree and re-run `sbt ${sbtTargets.mkString(" ")}` interactively."
      )
    }

    val producedStage    = worktree.resolve(StageRel)
    val producedLauncher = worktree.resolve(LauncherRel)
    if (!Files.isExecutable(producedLauncher)) {
      throw new RuntimeException(
        s"sbt stage succeeded at git[$shortSha] but launcher is missing. " +
        s"Expected: $producedLauncher executable. " +
        s"Observed: file absent or non-executable. " +
        s"Next: inspect $producedStage; this typically indicates a JavaAppPackaging configuration drift."
      )
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

    if (needsScalaRuntime && !hasRuntimeArtifacts(m2Dir, version, ivy2 = false)) {
      throw new RuntimeException(
        s"publishM2 succeeded but expected artifacts are missing for git[$shortSha]. " +
        s"Expected: ${RuntimeModules.map(m => s"${m}_2.13/$version").mkString(", ")} under $m2Dir. " +
        s"Observed: artifacts absent. " +
        s"Next: run with `--keep-worktrees` and inspect $m2Dir + the sbt log."
      )
    }

    if (!keepWorktree) {
      val m2note = if (needsScalaRuntime) s"runtime in $m2Dir" else "no scala runtime materialized (non-Scala target)"
      Harness.say(s"removing worktree $worktree (launcher cached; $m2note)")
      removeWorktree(worktree)
    } else {
      Harness.say(s"keeping worktree $worktree (--keep-worktrees)")
    }

    val repoUri  = if (needsScalaRuntime) m2Dir.toUri.toString else "ivy2Local"
    val repoPath = if (needsScalaRuntime) m2Dir else Path.of(sys.props.getOrElse("user.home", "")).resolve(".ivy2/local")
    IdlcResolution(
      launcher        = cachedLauncher,
      runtimeRepoPath = repoPath,
      runtimeRepoUri  = repoUri,
      runtimeVersion  = version,
    )
  }

  // -------------------- path -------------------

  /** Resolve a pre-built launcher path. The path may point to:
    *    - the launcher binary directly (`…/stage/bin/idealingua-v1-compiler`);
    *    - the stage root (`…/stage/`) — we look for `bin/idealingua-v1-compiler`;
    *    - any directory containing `bin/idealingua-v1-compiler`.
    *
    *  Runtime version resolution order:
    *    1. `--runtime-version` flag (`runtimeVersionOverride`);
    *    2. sibling `version.txt` (per plan §D2) next to the launcher OR at the
    *       stage root;
    *    3. error (exit 2 from caller).
    *
    *  `path:` refs do NOT publish runtime artifacts. For Scala consumers the
    *  template `{{RUNTIME_REPOSITORY}}` resolves to `ivy2Local` so a previously
    *  populated user ivy cache is used; if absent, the scala-cli build will
    *  fail to resolve and the adapter reports a clear error. TS/C# adapters
    *  are unaffected (they inline / use NuGet).
    */
  private def resolvePath(
    rawPath: String,
    runtimeVersionOverride: Option[String],
    needsScalaRuntime: Boolean,
  ): IdlcResolution = {
    val p = Path.of(rawPath).toAbsolutePath.normalize
    if (!Files.exists(p)) {
      throw new IllegalArgumentException(
        s"path: ref points to a non-existent file/directory. " +
        s"Expected: $p to exist (launcher binary or stage directory). " +
        s"Observed: absent. " +
        s"Next: pass an absolute path to a staged idlc launcher, e.g. " +
        s"--old path:target/regression-harness/cache/idlc/<sha>/stage/bin/idealingua-v1-compiler."
      )
    }

    val launcher: Path =
      if (Files.isRegularFile(p) && Files.isExecutable(p)) p
      else if (Files.isDirectory(p)) {
        val candidates = Seq(
          p.resolve("bin/idealingua-v1-compiler"),
          p.resolve("idealingua-v1-compiler"),
        )
        candidates.find(c => Files.isRegularFile(c) && Files.isExecutable(c)).getOrElse {
          throw new IllegalArgumentException(
            s"path: ref directory does not contain a recognizable launcher. " +
            s"Expected: $p/bin/idealingua-v1-compiler OR $p/idealingua-v1-compiler. " +
            s"Observed: neither path is an executable file. " +
            s"Next: confirm `sbt idealingua-v1-compiler/stage` produced a stage tree under this path."
          )
        }
      } else {
        throw new IllegalArgumentException(
          s"path: ref is neither an executable file nor a directory: $p. " +
          s"Next: ensure the launcher has the executable bit set (`chmod +x`)."
        )
      }

    // Discover sibling version.txt: try next to the launcher, then up two levels
    // (stage root: stage/bin/.. == stage/), then up three (under target/universal/).
    val versionTxt: Option[Path] = Seq(
      launcher.getParent.resolve("version.txt"),
      launcher.getParent.getParent.resolve("version.txt"),
      launcher.getParent.getParent.getParent.resolve("version.txt"),
    ).find(Files.isRegularFile(_))

    val version: String = runtimeVersionOverride
      .orElse(versionTxt.map(t => new String(Files.readAllBytes(t), StandardCharsets.UTF_8).trim))
      .getOrElse {
        throw new IllegalArgumentException(
          s"path: ref lacks a runtime version. " +
          s"Expected: --runtime-version <ver> OR a `version.txt` file next to the launcher (or at the stage root). " +
          s"Observed: neither was provided. " +
          s"Probed: ${Seq(launcher.getParent.resolve("version.txt"), launcher.getParent.getParent.resolve("version.txt")).mkString(", ")}. " +
          s"Next: pass --runtime-version 1.4.20-SNAPSHOT (or whichever version the runtime artifacts were published under)."
        )
      }

    // path: refs do not publish runtime artifacts (no sbt available to run).
    // Scala consumers must have the matching runtime already in `~/.ivy2/local`
    // (e.g. populated by an earlier `self`/`git:` run, or by a prior
    // `sbt publishLocal` against the same source tree). The scala-cli template
    // exposes `ivy2Local` as one of its resolvers, so the build will pick the
    // artifacts up from there.
    val ivy2Local = Path.of(sys.props.getOrElse("user.home", "")).resolve(".ivy2/local")
    Harness.say(s"path: launcher=$launcher version=$version (scala-runtime=$needsScalaRuntime, repo=ivy2Local)")
    IdlcResolution(
      launcher        = launcher,
      runtimeRepoPath = ivy2Local,
      runtimeRepoUri  = "ivy2Local",
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
    // Use `git clone --local` rather than `git worktree add`. A `worktree add`
    // creates a `.git` FILE in the target dir pointing at the main repo's
    // `.git/worktrees/<name>/` — sbt-git's JGit version mis-detects this as
    // a "bare repository" during build.sbt settings resolution and throws
    // `NoWorkTreeException`, killing `sbt stage` before any task runs. A
    // local clone produces a fully self-contained `.git/` directory which
    // JGit handles correctly. `--local` reuses hardlinks for object packs
    // so the disk cost is small.
    if (Files.exists(worktree)) deleteRecursively(worktree)

    val cloneRc = Process(
      Seq("git", "clone", "--local", "--no-tags", "--shared", repoRoot.toAbsolutePath.toString, worktree.toAbsolutePath.toString),
      repoRoot.toFile,
    ).!
    if (cloneRc != 0) {
      throw new RuntimeException(
        s"git clone --local failed (exit=$cloneRc). Tried to populate worktree at $worktree from $repoRoot."
      )
    }

    val checkoutRc = Process(
      Seq("git", "-c", "advice.detachedHead=false", "checkout", "--detach", sha),
      worktree.toFile,
    ).!
    if (checkoutRc != 0) {
      throw new RuntimeException(
        s"git checkout --detach $sha failed (exit=$checkoutRc) in clone at $worktree."
      )
    }
  }

  private def removeWorktree(worktree: Path): Unit = {
    // Self-contained clones — plain directory delete is sufficient.
    if (Files.exists(worktree)) {
      deleteRecursively(worktree)
    }
  }

  private def copyTree(src: Path, dst: Path): Unit = {
    Files.walk(src).iterator().asScala.foreach {
      srcEntry =>
        val rel      = src.relativize(srcEntry)
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
      Files.walk(p).iterator().asScala.toList.reverse.foreach {
        entry =>
          try Files.delete(entry)
          catch { case _: Throwable => () }
      }
    }
  }

  /** Probe: does the publishLocal target hold the expected `_2.13` artifacts?
    *  The scala-cli template targets Scala 3.9.0 (see M1 finding #4 in
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
        else repoPath.resolve("io").resolve("7mind").resolve("izumi")
      if (!Files.isDirectory(groupDir)) false
      else {
        RuntimeModules.forall {
          module =>
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
