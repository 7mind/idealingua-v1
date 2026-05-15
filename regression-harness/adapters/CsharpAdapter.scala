package regression_harness
package adapters

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

/** C# adapter for idl-regress.
 *
 *  Layout materialized under `workDir`:
 *
 *    <workDir>/
 *      Driver.csproj         — rendered from `templates/csharp/Driver.csproj`
 *      csharp/               — copy of the generated tree (idlc emits
 *                              `<target>/csharp/<Package>/...` under PLAIN
 *                              layout + `<target>/csharp/IRT/...` runtime)
 *      sample_app.cs         — copy of the LLM-rendered driver
 *      bin/, obj/            — populated by `dotnet build/run`
 *
 *  Build+run: `dotnet run --project <workDir>` (5-minute timeout).
 *
 *  The csproj template:
 *    - includes IRT/Marshaller, IRT/Logger (recursive), plus the small set of
 *      IRT root files we need (Dispatcher, RTTI, Either, Void, ILogger,
 *      IServiceDispatcher, DispatcherException);
 *    - EXCLUDES IRT/Transport (pulls WebSocketSharp etc. that we don't
 *      need for stdout-emitting sample apps), and EXCLUDES IRT/UrlEscaper.cs
 *      (depends on System.Web which is not auto-referenced by net9.0);
 *    - sets `EnableDefaultCompileItems=false` (otherwise SDK auto-globs
 *      collide with our explicit Compile items);
 *    - pins `Newtonsoft.Json` 13.0.3, matching the FROZEN test-harness
 *      Driver.csproj.
 *
 *  Per-sha runtime cache: NOT needed. idlc emits the full IRT runtime tree
 *  into `<target>/csharp/IRT/` (withRuntime=true default), so each side's
 *  generated tree is self-contained — there is no NuGet feed dependency for
 *  the IRT runtime, only for `Newtonsoft.Json`. `IdlcResolution.runtimeVersion`
 *  is recorded in the csproj as an XML comment for human-readability but no
 *  per-sha NuGet feed is materialized.
 */
final class CsharpAdapter(repoRoot: Path) extends LangAdapter {

  private val Timeout = 5.minutes

  /** NDJSON line shape — matches `Canonicalize.LineRx`.
   *
   *  `dotnet run` interleaves SDK chatter on stdout (e.g. the `SYSLIB0014`
   *  deprecation warning from `WebClient` in some IRT helpers) even with
   *  `--verbosity quiet --nologo`. We pre-filter stdout to only retain
   *  lines matching the canonical NDJSON form before persisting `rawOut`,
   *  so downstream `Canonicalize` does not fail on warning-shaped lines.
   *  The unfiltered stream is kept beside as `<rawOut>.unfiltered` for
   *  postmortem inspection.
   */
  private val NdjsonLine = """^[A-Za-z0-9_.]+\t[A-Za-z0-9_-]+\t.+$""".r

  override def buildAndRun(
    workDir:    Path,
    genDir:     Path,
    sampleApp:  Path,
    resolution: IdlcResolution,
    rawOut:     Path,
  ): Either[String, Unit] = {
    if (whichDotnet().isEmpty) {
      return Left(
        "dotnet not found on PATH. The nix-shell in this repo provides dotnet-sdk-9; " +
        "install dotnet >= 8 and retry."
      )
    }

    Files.createDirectories(workDir)

    // 1. resolve generated C# subtree. idlc emits `<target>/csharp/` under PLAIN layout.
    val csSrcRoot =
      if (Files.isDirectory(genDir.resolve("csharp"))) genDir.resolve("csharp") else genDir
    if (!Files.isDirectory(csSrcRoot)) {
      return Left(s"no csharp output found under $genDir")
    }
    val irtSrc = csSrcRoot.resolve("IRT")
    if (!Files.isDirectory(irtSrc)) {
      return Left(s"generated tree missing IRT/ runtime — was idlc invoked with withRuntime=true? ($csSrcRoot)")
    }

    // 2. copy the entire generated C# tree under workDir/csharp/. The csproj
    //    template refers to it via the `csharp/...` glob root, so do NOT flatten.
    copyTree(csSrcRoot, workDir.resolve("csharp"))

    // 3. csproj template
    val csprojTemplate = repoRoot.resolve("regression-harness/templates/csharp/Driver.csproj")
    if (!Files.isRegularFile(csprojTemplate)) return Left(s"missing template: $csprojTemplate")
    val csprojRendered = new String(Files.readAllBytes(csprojTemplate), StandardCharsets.UTF_8)
      .replace("{{RUNTIME_VERSION}}", resolution.runtimeVersion)
    Files.write(workDir.resolve("Driver.csproj"), csprojRendered.getBytes(StandardCharsets.UTF_8))

    // 4. sample app
    Files.copy(sampleApp, workDir.resolve("sample_app.cs"), StandardCopyOption.REPLACE_EXISTING)

    // 5. run. `dotnet run --project <dir>` does build+execute in one shot;
    //    stderr captures `dotnet`'s own info chatter so we keep it separate.
    val runCmd = Seq(
      "dotnet", "run",
      "--project", workDir.toString,
      "--verbosity", "quiet",
      "--nologo",
    )
    Harness.say(s"+ ${runCmd.mkString(" ")}")

    val stdoutBuf = new StringBuilder
    val stderrBuf = new StringBuilder
    val logger = ProcessLogger(
      o => { stdoutBuf.append(o); stdoutBuf.append('\n') },
      e => { stderrBuf.append(e); stderrBuf.append('\n') },
    )
    val proc = Process(runCmd, workDir.toFile).run(logger)

    val deadline = System.nanoTime() + Timeout.toNanos
    while (proc.isAlive() && System.nanoTime() < deadline) Thread.sleep(200L)
    val rc =
      if (proc.isAlive()) {
        proc.destroy()
        persistStreams(rawOut, stdoutBuf.toString, stderrBuf.toString)
        return Left(s"dotnet timed out after $Timeout")
      } else proc.exitValue()

    persistStreams(rawOut, stdoutBuf.toString, stderrBuf.toString)

    if (rc != 0) Left(s"dotnet exit=$rc (stderr captured at ${rawOut}.stderr)")
    else Right(())
  }

  /** Write stdout (NDJSON-filtered) to `rawOut`, raw stdout to
   *  `<rawOut>.unfiltered`, and stderr to `<rawOut>.stderr`.
   */
  private def persistStreams(rawOut: Path, stdout: String, stderr: String): Unit = {
    val filtered = stdout.linesIterator
      .filter(l => NdjsonLine.matches(l.stripLineEnd))
      .mkString("\n")
    val toWrite = if (filtered.isEmpty) "" else filtered + "\n"
    Files.writeString(rawOut, toWrite)
    Files.writeString(Path.of(rawOut.toString + ".unfiltered"), stdout)
    Files.writeString(Path.of(rawOut.toString + ".stderr"), stderr)
  }

  // ------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------

  private def whichDotnet(): Option[Path] = {
    val pathEnv = Option(System.getenv("PATH")).getOrElse("")
    pathEnv.split(java.io.File.pathSeparatorChar).iterator
      .map(p => Path.of(p, "dotnet"))
      .find(Files.isExecutable)
  }

  private def copyTree(src: Path, dst: Path): Unit = {
    if (!Files.isDirectory(src)) return
    Files.walk(src).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p)) {
        val rel    = src.relativize(p)
        val target = dst.resolve(rel.toString)
        if (target.getParent != null) Files.createDirectories(target.getParent)
        Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }
}
