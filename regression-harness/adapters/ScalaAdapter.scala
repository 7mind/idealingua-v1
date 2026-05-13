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
    workDir:   Path,
    genDir:    Path,
    sampleApp: Path,
    rawOut:    Path,
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
      .replace("{{RUNTIME_VERSION}}", readRuntimeVersion())
      .replace("{{CIRCE_VERSION}}",   CirceVersion)
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

    val cmd = Seq("scala-cli", "run", workDir.toString)
    Harness.say(s"+ ${cmd.mkString(" ")}")
    val proc = Process(cmd).run(logger)

    val deadline = System.nanoTime() + Timeout.toNanos
    while (proc.isAlive() && System.nanoTime() < deadline) {
      Thread.sleep(200L)
    }
    val rc =
      if (proc.isAlive()) {
        proc.destroy()
        Files.writeString(rawOut, stdoutBuf.toString)
        Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)
        return Left(s"scala-cli timed out after $Timeout")
      } else proc.exitValue()

    Files.writeString(rawOut, stdoutBuf.toString)
    Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)

    if (rc != 0) Left(s"scala-cli exit=$rc (stderr captured at ${rawOut}.stderr)")
    else Right(())
  }

  private def whichScalaCli(): Option[Path] = {
    val pathEnv = Option(System.getenv("PATH")).getOrElse("")
    pathEnv.split(java.io.File.pathSeparatorChar).iterator
      .map(p => Path.of(p, "scala-cli"))
      .find(Files.isExecutable)
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

  private def readRuntimeVersion(): String = {
    val versionSbt = repoRoot.resolve("version.sbt")
    if (!Files.isRegularFile(versionSbt)) return "1.4.20-SNAPSHOT"
    val text = new String(Files.readAllBytes(versionSbt), StandardCharsets.UTF_8)
    val rx   = """"([^"]+)"""".r
    rx.findFirstMatchIn(text).map(_.group(1)).getOrElse("1.4.20-SNAPSHOT")
  }

  /** Circe version: derived from the central izumi `fundamentals-json-circe`
   *  dep that the runtime-rpc-scala module pulls in (see `build.sbt` line ~346).
   *  For M1 we hard-code the version known to be in the coursier cache; M2
   *  should read it from a generated version manifest emitted by sbt.
   */
  private val CirceVersion = "0.14.14"
}
