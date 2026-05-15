package regression_harness
package adapters

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

/** scala-cli driven adapter.
 *
 *  Materializes a tiny scala-cli project:
 *
 *    <workDir>/
 *      project.scala         — using-directives (rendered from template)
 *      generated/            — copy of `genDir`
 *      sample_app.scala      — copy of the LLM-rendered driver
 *
 *  Run: `scala-cli run <workDir>`. Captures combined stdout+stderr.
 *  Stdout is treated as the canonical NDJSON stream; stderr goes to a sibling
 *  `.stderr` file for diagnostics.
 *
 *  scala-cli MUST be on PATH. We do not bring our own — the nix shell provides
 *  it (`flake.nix` pulls scala-cli ~1.10 from nixpkgs 25.11). If absent, the
 *  adapter fails fast with a clear message.
 */
final class ScalaAdapter(repoRoot: Path) extends LangAdapter {

  private val Timeout = 5.minutes

  override def buildAndRun(
    workDir:    Path,
    genDir:     Path,
    sampleApp:  Path,
    resolution: IdlcResolution,
    rawOut:     Path,
  ): Either[String, Unit] = {
    if (whichScalaCli().isEmpty) {
      return Left(
        "scala-cli not found on PATH. Install scala-cli (e.g. `nix-shell` in this repo brings it in) and retry."
      )
    }

    Files.createDirectories(workDir)

    // 1. project.scala from template
    val template = repoRoot.resolve("regression-harness/templates/scala/project.scala.template")
    if (!Files.isRegularFile(template)) {
      return Left(s"missing template: $template")
    }
    val rendered = new String(Files.readAllBytes(template), StandardCharsets.UTF_8)
      .replace("{{RUNTIME_VERSION}}",    resolution.runtimeVersion)
      .replace("{{RUNTIME_REPOSITORY}}", resolution.runtimeRepoUri)
      .replace("{{CIRCE_VERSION}}",      CirceVersion)
    Files.write(workDir.resolve("project.scala"), rendered.getBytes(StandardCharsets.UTF_8))

    // 2. generated sources — copy tree, filtering to *.scala only (idlc emits a
    //    `<target>/scala/` subdir on PLAIN layout; we flatten that one level so
    //    sources live under workDir/generated/).
    val genDst = workDir.resolve("generated")
    Files.createDirectories(genDst)
    val genSrc =
      if (Files.isDirectory(genDir.resolve("scala"))) genDir.resolve("scala") else genDir
    copyScalaSources(genSrc, genDst)

    // 3. sample app
    Files.copy(sampleApp, workDir.resolve("sample_app.scala"), StandardCopyOption.REPLACE_EXISTING)

    // 4. run
    val stdoutBuf = new StringBuilder
    val stderrBuf = new StringBuilder
    val logger = ProcessLogger(
      o => { stdoutBuf.append(o); stdoutBuf.append('\n') },
      e => { stderrBuf.append(e); stderrBuf.append('\n') },
    )

    // scala-cli's directory scanner silently skips any source under a directory named
    // `test/` (it's part of the hidden `--default-forbidden-directories` list — see
    // `scala-cli run --help-full`). The corpus under main-tests contains `izumi/test/*.domain`
    // which generates output under `generated/izumi/test/...`; passing the workDir alone would
    // drop those without any warning. Enumerate every `.scala` file explicitly so the scanner's
    // directory heuristic never applies. `project.scala` (using-directives) is included by
    // the same walk and is honored by scala-cli regardless of position in the input list.
    val inputs = listScalaInputs(workDir).map(_.toString)

    // Two-step run:
    //   1. `scala-cli --power compile --print-classpath …` resolves deps, compiles,
    //       and prints the resulting `:`-separated classpath to stdout.
    //   2. `java -cp <classpath> sample_app.SampleApp` runs the sample app.
    // Avoids `scala-cli run`'s execve-based JVM launcher, whose native binding
    // (`coursier.jvm.Execve` → `com.oracle.svm.core.posix.headers.LibC`) is
    // unbundled in some scala-cli distributions (e.g. the GraalVM-based nixpkgs
    // build trips `NoClassDefFoundError` on `Execve.java:29` for large
    // generated projects). The compile-and-run-by-hand split is equivalent
    // semantically (same classpath, same main class) and works uniformly.
    val cpBuf = new StringBuilder
    val cpLogger = ProcessLogger(
      o => { cpBuf.append(o); cpBuf.append('\n') },
      e => { stderrBuf.append(e); stderrBuf.append('\n') },
    )
    val compileCmd = Seq("scala-cli", "--power", "compile", "--print-classpath") ++ inputs
    Harness.say(s"+ scala-cli --power compile --print-classpath <${inputs.size} .scala files under $workDir>")
    val compileRc = Process(compileCmd).!(cpLogger)
    if (compileRc != 0) {
      Files.writeString(rawOut, stdoutBuf.toString)
      Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)
      return Left(s"scala-cli compile exit=$compileRc (stderr captured at ${rawOut}.stderr)")
    }
    val classpath = cpBuf.toString.trim.linesIterator.toList.lastOption.getOrElse("")
    if (classpath.isEmpty) {
      Files.writeString(rawOut, stdoutBuf.toString)
      Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)
      return Left(s"scala-cli compile produced empty classpath (stderr captured at ${rawOut}.stderr)")
    }

    val runCmd = Seq("java", "-cp", classpath, "sample_app.SampleApp")
    Harness.say(s"+ java -cp <classpath:${classpath.length}B> sample_app.SampleApp")
    val proc = Process(runCmd).run(logger)

    val deadline = System.nanoTime() + Timeout.toNanos
    while (proc.isAlive() && System.nanoTime() < deadline) {
      Thread.sleep(200L)
    }
    val rc =
      if (proc.isAlive()) {
        proc.destroy()
        Files.writeString(rawOut, stdoutBuf.toString)
        Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)
        return Left(s"sample app timed out after $Timeout")
      } else proc.exitValue()

    Files.writeString(rawOut, stdoutBuf.toString)
    Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)

    if (rc != 0) Left(s"sample app exit=$rc (stderr captured at ${rawOut}.stderr)")
    else Right(())
  }

  private def whichScalaCli(): Option[Path] = {
    val pathEnv = Option(System.getenv("PATH")).getOrElse("")
    pathEnv.split(java.io.File.pathSeparatorChar).iterator
      .map(p => Path.of(p, "scala-cli"))
      .find(Files.isExecutable)
  }

  /** Enumerate every `.scala` file under `root` as an absolute path list. Stable order
   *  (lexicographic) for reproducibility of the invocation; ordering does not affect
   *  scala-cli compilation semantics. Used to bypass scala-cli's directory-based source
   *  scanner (which skips `test/` subdirs); when files are passed individually they are
   *  always accepted.
   */
  private def listScalaInputs(root: Path): Seq[Path] = {
    if (!Files.isDirectory(root)) return Seq.empty
    val buf = scala.collection.mutable.ArrayBuffer.empty[Path]
    Files.walk(root).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p) && p.getFileName.toString.endsWith(".scala")) buf += p
    }
    buf.sortBy(_.toString).toSeq
  }

  private def copyScalaSources(src: Path, dst: Path): Unit = {
    if (!Files.isDirectory(src)) return
    Files.walk(src).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p) && p.getFileName.toString.endsWith(".scala")) {
        val rel    = src.relativize(p)
        val target = dst.resolve(rel.toString)
        Files.createDirectories(target.getParent)
        Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  /** Circe version: derived from the central izumi `fundamentals-json-circe`
   *  dep that the runtime-rpc-scala module pulls in (see `build.sbt` line ~346).
   *  For M2 we hard-code the version known to be in the coursier cache; a future
   *  milestone should read it from a generated version manifest emitted by sbt.
   */
  private val CirceVersion = "0.14.14"
}
