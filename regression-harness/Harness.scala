//> using scala 3.8.3
//> using jvm 21
//> using file IdlcResolution.scala
//> using file IdlcResolver.scala
//> using file SampleAppGen.scala
//> using file LangAdapter.scala
//> using file Canonicalize.scala
//> using file Diff.scala
//> using file adapters/ScalaAdapter.scala
//> using file adapters/TypescriptAdapter.scala
//> using file adapters/CsharpAdapter.scala

package regression_harness

import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.security.MessageDigest
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.sys.process.*
import scala.util.Using
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/** Exit codes:
 *    0  no divergences
 *    1  divergences found (with --fail-on-divergence)
 *    2  build or setup failure
 *    3  sample-app not cached (prompt emitted, user must run LLM)
 *  126  CLI usage error
 */
object Harness {

  private val ExitOk           = 0
  private val ExitDivergence   = 1
  private val ExitBuildFailure = 2
  private val ExitNeedSample   = 3
  private val ExitUsage        = 126

  private val SupportedLangs = Set("scala", "typescript", "csharp")

  case class Args(
    project:           Path,
    oldRef:            String,
    newRef:            String,
    lang:              String,
    out:               Option[Path],
    regenSampleApp:    Boolean,
    keepWorktrees:     Boolean,
    format:            String,
    failOnDivergence:  Boolean,
  )

  def main(rawArgs: Array[String]): Unit = {
    parseArgs(rawArgs) match {
      case Left(msg) =>
        System.err.println(msg)
        System.err.println(usage)
        sys.exit(ExitUsage)
      case Right(args) =>
        sys.exit(run(args))
    }
  }

  private def usage: String =
    """Usage:
      |  idl-regress --project <path>
      |              --old <ref>   ('self' or 'git:<sha|tag|branch>')
      |              --new <ref>   ('self' or 'git:<sha|tag|branch>')
      |              --lang scala|typescript|csharp
      |              [--out <dir>]
      |              [--regen-sample-app]
      |              [--keep-worktrees]            (retain per-sha worktrees after build)
      |              [--format human|json|both]    (default: human)
      |              [--fail-on-divergence]        (default: on)
      |""".stripMargin

  private def parseArgs(raw: Array[String]): Either[String, Args] = {
    val it = raw.iterator.buffered
    var project: Option[Path]  = None
    var oldRef: Option[String] = None
    var newRef: Option[String] = None
    var lang: Option[String]   = None
    var out: Option[Path]      = None
    var regen                  = false
    var keep                   = false
    var format                 = "human"
    var failOnDiv              = true

    while (it.hasNext) {
      it.next() match {
        case "--project"           => project = Some(Paths.get(requireNext(it, "--project")).toAbsolutePath.normalize)
        case "--old"               => oldRef  = Some(requireNext(it, "--old"))
        case "--new"               => newRef  = Some(requireNext(it, "--new"))
        case "--lang"              => lang    = Some(requireNext(it, "--lang"))
        case "--out"               => out     = Some(Paths.get(requireNext(it, "--out")).toAbsolutePath.normalize)
        case "--regen-sample-app"  => regen   = true
        case "--keep-worktrees"    => keep    = true
        case "--format"            => format  = requireNext(it, "--format")
        case "--fail-on-divergence"=> failOnDiv = true
        case "--no-fail-on-divergence" => failOnDiv = false
        case "-h" | "--help"       => return Left("")
        case other                 => return Left(s"unknown argument: $other")
      }
    }

    for {
      p  <- project.toRight("--project is required")
      o  <- oldRef.toRight("--old is required")
      n  <- newRef.toRight("--new is required")
      l  <- lang.toRight("--lang is required")
      _  <- Either.cond(SupportedLangs.contains(l), (), s"unsupported language: $l (supported: ${SupportedLangs.toSeq.sorted.mkString(",")})")
      _  <- Either.cond(Files.isDirectory(p), (), s"--project not a directory: $p")
      _  <- Either.cond(Set("human","json","both").contains(format), (), s"--format must be human|json|both")
    } yield Args(p, o, n, l, out, regen, keep, format, failOnDiv)
  }

  private def requireNext(it: BufferedIterator[String], flag: String): String = {
    if (!it.hasNext) throw new IllegalArgumentException(s"$flag requires a value")
    it.next()
  }

  private def run(args: Args): Int = scala.util.boundary {
    val runId       = System.currentTimeMillis().toString
    val repoRoot    = detectRepoRoot()
    val scratchRoot = repoRoot.resolve(s"target/regression-harness/$runId")
    Files.createDirectories(scratchRoot)
    val outDir = args.out.getOrElse(scratchRoot.resolve("out"))
    Files.createDirectories(outDir)

    say(s"run: $runId")
    say(s"project: ${args.project}")
    say(s"scratch: $scratchRoot")
    say(s"out:     $outDir")

    val resolver = new IdlcResolver(repoRoot, scratchRoot, keepWorktree = args.keepWorktrees)

    val resOld =
      try resolver.resolve(args.oldRef)
      catch { case e: Throwable => say(s"FATAL: idlc resolve(old=${args.oldRef}): ${e.getMessage}"); scala.util.boundary.break(ExitBuildFailure) }
    val resNew =
      try resolver.resolve(args.newRef)
      catch { case e: Throwable => say(s"FATAL: idlc resolve(new=${args.newRef}): ${e.getMessage}"); scala.util.boundary.break(ExitBuildFailure) }

    say(s"idlc old: ${resOld.launcher} (runtime ${resOld.runtimeVersion} from ${resOld.runtimeRepoUri})")
    say(s"idlc new: ${resNew.launcher} (runtime ${resNew.runtimeVersion} from ${resNew.runtimeRepoUri})")

    val idlSha = hashIdlTree(args.project)
    say(s"idl sha256: ${idlSha.take(16)}...")

    val sides = Seq(
      ("old", resOld, scratchRoot.resolve("gen-old")),
      ("new", resNew, scratchRoot.resolve("gen-new")),
    )

    sides.foreach { case (label, res, dst) =>
      Files.createDirectories(dst)
      val rc = runIdlc(res.launcher, args.project, dst, args.lang)
      if (rc != 0) {
        say(s"FATAL: idlc[$label] exit $rc — see $scratchRoot/idlc-$label.log")
        scala.util.boundary.break(ExitBuildFailure)
      }
      say(s"generated[$label] -> $dst")
    }

    // For M1 the two sides are identical (self vs self); use the 'old' generated tree
    // as the source-of-truth for sample-app prompt + adapter input.
    val genTree = scratchRoot.resolve("gen-old")

    val sampleApp =
      new SampleAppGen(args.project, args.lang, idlSha, scratchRoot, repoRoot)
        .resolve(genTree, args.regenSampleApp) match {
          case Left(_)     => scala.util.boundary.break(ExitNeedSample)
          case Right(path) => path
        }
    say(s"sample-app: $sampleApp")

    val adapter: LangAdapter = args.lang match {
      case "scala"      => new adapters.ScalaAdapter(repoRoot)
      case "typescript" => new adapters.TypescriptAdapter(repoRoot)
      case "csharp"     => new adapters.CsharpAdapter(repoRoot)
      case other        => throw new IllegalStateException(s"no adapter: $other")
    }

    val rawOut = mutable.LinkedHashMap.empty[String, Path]
    for ((label, res, genDir) <- sides) {
      val side = scratchRoot.resolve(s"app-$label")
      Files.createDirectories(side)
      val raw  = scratchRoot.resolve(s"raw-$label.txt")
      adapter.buildAndRun(side, genDir, sampleApp, res, raw) match {
        case Left(err)  =>
          say(s"FATAL: adapter[$label]: $err")
          scala.util.boundary.break(ExitBuildFailure)
        case Right(())  =>
          rawOut(label) = raw
      }
    }

    val canon = mutable.LinkedHashMap.empty[String, Path]
    rawOut.foreach { case (label, raw) =>
      val out = scratchRoot.resolve(s"wire-$label.ndjson")
      Canonicalize.canonicalize(raw, out) match {
        case Left(err) =>
          say(s"FATAL: canonicalize[$label]: $err")
          scala.util.boundary.break(ExitBuildFailure)
        case Right(_)  => canon(label) = out
      }
    }

    val report = Diff.compare(canon("old"), canon("new"))
    Diff.writeReports(report, outDir, args.format)

    if (report.divergences.isEmpty) {
      say(s"OK: zero divergences across ${report.totalLines} lines")
      ExitOk
    } else {
      say(s"DIVERGENCE: ${report.divergences.size} of ${report.totalLines} lines differ")
      if (args.failOnDivergence) ExitDivergence else ExitOk
    }
  }

  private def runIdlc(launcher: Path, project: Path, target: Path, lang: String): Int = {
    val role = lang match {
      case "scala"      => ":scala"
      case "typescript" => ":typescript"
      case "csharp"     => ":csharp"
      case other        => throw new IllegalStateException(s"unsupported lang for idlc dispatch: $other")
    }
    val source  = project.resolve("source")
    val overlay = project.resolve("overlay")
    // NB: the staged sbt-native-packager launcher consumes `-d` and `-v` as its own
    //     debug/verbose toggles; we MUST use long-form options so they propagate to
    //     the JVM application. `--disable-zip` instead of `-nz`, `--define=k=v` for `-d`.
    val cmd = Seq(
      launcher.toString,
      s"--root=${project}",
      s"--source=${source}",
      s"--overlay=${overlay}",
      s"--target=${target}",
      role,
      "--define=layout=PLAIN",
      "--disable-zip",
    )
    say(s"+ ${cmd.mkString(" ")}")
    Process(cmd).!
  }

  /** Walks `<project>/source` + any `*.model` files, hashes contents. */
  private def hashIdlTree(project: Path): String = {
    val src = project.resolve("source")
    if (!Files.isDirectory(src)) {
      throw new IllegalArgumentException(s"project has no 'source/' subdir: $project")
    }
    val files = scala.collection.mutable.ArrayBuffer.empty[Path]
    Files.walk(src).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p)) {
        val n = p.getFileName.toString
        if (n.endsWith(".domain") || n.endsWith(".model")) files += p
      }
    }
    Files.walk(project, 1).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p)) {
        val n = p.getFileName.toString
        if (n.endsWith(".model")) files += p
      }
    }
    val md = MessageDigest.getInstance("SHA-256")
    files.sortBy(_.toString).foreach { p =>
      md.update(project.relativize(p).toString.getBytes(StandardCharsets.UTF_8))
      md.update(0.toByte)
      md.update(Files.readAllBytes(p))
      md.update(0.toByte)
    }
    md.digest().map(b => f"${b & 0xff}%02x").mkString
  }

  private def detectRepoRoot(): Path = {
    val cwd = Paths.get("").toAbsolutePath
    var c   = cwd
    while (c != null && !Files.isDirectory(c.resolve(".git"))) c = c.getParent
    if (c == null) cwd else c
  }

  private[regression_harness] def say(msg: String): Unit = {
    System.err.println(s"[idl-regress] $msg")
  }
}
