package regression_harness
package adapters

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardCopyOption}
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.sys.process.*

/** TypeScript adapter for idl-regress.
 *
 *  Layout materialized under `workDir`:
 *
 *    <workDir>/
 *      package.json          — rendered from `templates/typescript/package.json` (deps for the IRT runtime + tsx)
 *      tsconfig.json         — rendered from `templates/typescript/tsconfig.json`
 *      irt/                  — generated IRT runtime tree (from `withRuntime=true`)
 *      idltest/, …           — user-domain generated TS modules
 *      sample_app.ts         — copy of the LLM-rendered driver
 *      node_modules/         — populated by `bun install` or `npm install`
 *
 *  Build+run: probe `bun` on PATH; otherwise require `npm` + `node`.
 *
 *    bun:  `bun install` + `bun run sample_app.ts`
 *    npm:  `npm install` + `./node_modules/.bin/tsx sample_app.ts`
 *
 *  `tsx` is declared as a `devDependency` in the template so it is installed
 *  locally by either path — we never rely on a globally-installed `tsx`.
 *
 *  Per-sha runtime cache (cf. M2 `IdlcResolution.runtimeRepoUri`):
 *  TypeScript does NOT need one. `idlc :typescript` with the default
 *  `withRuntime=true` inlines the entire `irt/` runtime tree into the
 *  generated output, so each side's generated tree is self-contained. The
 *  `IdlcResolution.runtimeVersion` field is interpolated into `package.json`
 *  for human-readability but no per-sha m2 directory is materialized.
 */
final class TypescriptAdapter(repoRoot: Path) extends LangAdapter {

  private val Timeout = 5.minutes

  override def buildAndRun(
    workDir:    Path,
    genDir:     Path,
    sampleApp:  Path,
    resolution: IdlcResolution,
    rawOut:     Path,
  ): Either[String, Unit] = {
    val runner = pickRunner() match {
      case Some(r) => r
      case None    =>
        return Left(
          "no TypeScript build environment on PATH. Need either `bun`, or `npm` + `node` (nix-shell " +
          "provides nodejs_24). If using npm, `tsx` is installed locally via the package.json template."
        )
    }
    Harness.say(s"typescript runner: ${runner.name}")

    Files.createDirectories(workDir)

    // 1. resolve generated TS subtree. idlc emits `<target>/typescript/` under PLAIN layout.
    val tsSrcRoot =
      if (Files.isDirectory(genDir.resolve("typescript"))) genDir.resolve("typescript") else genDir
    if (!Files.isDirectory(tsSrcRoot)) {
      return Left(s"no typescript output found under $genDir")
    }
    val irtSrc = tsSrcRoot.resolve("irt")
    if (!Files.isDirectory(irtSrc)) {
      return Left(s"generated tree missing irt/ runtime — was idlc invoked with withRuntime=true? ($tsSrcRoot)")
    }

    // 2. copy the entire generated tree (irt/ + user modules + index.ts), but SKIP
    //    the package.json / tsconfig.json that idlc emits — we materialize our own.
    copyTreeFiltered(tsSrcRoot, workDir, skip = Set("package.json", "tsconfig.json", "tsconfig.es.json"))

    // 3. templates
    val pkgTemplate = repoRoot.resolve("regression-harness/templates/typescript/package.json")
    val tscTemplate = repoRoot.resolve("regression-harness/templates/typescript/tsconfig.json")
    if (!Files.isRegularFile(pkgTemplate)) return Left(s"missing template: $pkgTemplate")
    if (!Files.isRegularFile(tscTemplate)) return Left(s"missing template: $tscTemplate")

    val pkgRendered = new String(Files.readAllBytes(pkgTemplate), StandardCharsets.UTF_8)
      .replace("{{RUNTIME_VERSION}}", resolution.runtimeVersion)
    Files.write(workDir.resolve("package.json"), pkgRendered.getBytes(StandardCharsets.UTF_8))
    val tscRendered = new String(Files.readAllBytes(tscTemplate), StandardCharsets.UTF_8)
    Files.write(workDir.resolve("tsconfig.json"), tscRendered.getBytes(StandardCharsets.UTF_8))

    // 4. sample app
    Files.copy(sampleApp, workDir.resolve("sample_app.ts"), StandardCopyOption.REPLACE_EXISTING)

    // 5. install deps
    val installCmd: Seq[String] = runner.name match {
      case "bun" => Seq("bun", "install")
      case _     => Seq("npm", "install", "--no-audit", "--no-fund", "--silent")
    }
    val installRc = runCapturing(installCmd, workDir, rawOut.resolveSibling(rawOut.getFileName.toString + ".install"))
    if (installRc != 0) return Left(s"${installCmd.head} install exit=$installRc")

    // 6. compile with tsc (esModuleInterop=false + module=CommonJS, per tsconfig
    //    template). Avoids tsx/esbuild's `__toESM` interop helper, which wraps a
    //    CJS `export = X` module in a non-callable namespace — historically this
    //    broke `import * as moment from 'moment'` in v1.4.19's IRT
    //    (`irt/formatter.ts:51 TypeError: moment is not a function`). Under
    //    `tsc + node` the legacy import form binds the namespace directly to the
    //    CJS exports object, so `moment(...)` stays callable for v1.4.19's
    //    output AND HEAD's `import moment = require('moment')` continues to work.
    //    Trade-off: tsc surfaces strict-mode type errors that `tsx` silently
    //    accepted — those are translator defects to fix at source.
    val tscBin = workDir.resolve("node_modules/.bin/tsc")
    if (!Files.isExecutable(tscBin)) return Left(s"tsc not found at $tscBin (npm install should have placed it via the typescript devDependency)")
    val tscRc = runCapturing(Seq(tscBin.toString, "-p", "tsconfig.json"), workDir, rawOut.resolveSibling(rawOut.getFileName.toString + ".tsc"))
    if (tscRc != 0) return Left(s"tsc exit=$tscRc (log captured at ${rawOut}.tsc)")

    // 7. run the compiled sample app under plain node — no CJS interop helper.
    val runCmd: Seq[String] = runner.name match {
      case "bun" => Seq("node", "out/sample_app.js")
      case "npm" => Seq("node", "out/sample_app.js")
      case other => return Left(s"unknown runner: $other")
    }
    Harness.say(s"+ ${runCmd.mkString(" ")} (cwd=$workDir)")

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
        Files.writeString(rawOut, stdoutBuf.toString)
        Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)
        return Left(s"${runner.name} timed out after $Timeout")
      } else proc.exitValue()

    Files.writeString(rawOut, stdoutBuf.toString)
    Files.writeString(Path.of(rawOut.toString + ".stderr"), stderrBuf.toString)

    if (rc != 0) Left(s"${runner.name} exit=$rc (stderr captured at ${rawOut}.stderr)")
    else Right(())
  }

  // ------------------------------------------------------------------
  // Runner probe
  // ------------------------------------------------------------------

  private case class Runner(name: String)

  /** Probe order: `bun` (one-shot install + run) → `npm` + `node` (resolves
   *  `tsx` declared in the package.json template). We don't probe for `tsx`
   *  globally — it is always installed locally via `npm install`.
   */
  private def pickRunner(): Option[Runner] = {
    if (onPath("bun"))                              Some(Runner("bun"))
    else if (onPath("npm") && onPath("node"))       Some(Runner("npm"))
    else None
  }

  private def onPath(bin: String): Boolean = {
    val pathEnv = Option(System.getenv("PATH")).getOrElse("")
    pathEnv.split(java.io.File.pathSeparatorChar).iterator
      .map(p => Path.of(p, bin))
      .exists(Files.isExecutable)
  }

  // ------------------------------------------------------------------
  // Filesystem helpers
  // ------------------------------------------------------------------

  private def copyTreeFiltered(src: Path, dst: Path, skip: Set[String]): Unit = {
    if (!Files.isDirectory(src)) return
    Files.walk(src).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p)) {
        val name = p.getFileName.toString
        val relParent = src.relativize(p).getParent
        val isTopLevelSkip = (relParent == null || relParent.toString.isEmpty) && skip.contains(name)
        if (!isTopLevelSkip) {
          val rel    = src.relativize(p)
          val target = dst.resolve(rel.toString)
          if (target.getParent != null) Files.createDirectories(target.getParent)
          Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING)
        }
      }
    }
  }

  private def runCapturing(cmd: Seq[String], cwd: Path, log: Path): Int = {
    Harness.say(s"+ ${cmd.mkString(" ")} (cwd=$cwd)")
    val sb = new StringBuilder
    val logger = ProcessLogger(
      o => { sb.append(o); sb.append('\n') },
      e => { sb.append(e); sb.append('\n') },
    )
    val rc = Process(cmd, cwd.toFile).!(logger)
    Files.writeString(log, sb.toString)
    rc
  }
}
