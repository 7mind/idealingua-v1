package regression_harness

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*
import scala.util.Using

/** Manages the LLM-generated sample app cache for a (project, language) pair.
 *
 *  Cache layout (committed in the *target* project's repo):
 *
 *    <project>/.idl-regression/
 *      sample_app.scala         — hand-rolled or LLM-rendered driver
 *      sample_app.scala.meta    — KEY=VALUE metadata; today: `idl_sha256=…`
 *
 *  Resolution:
 *
 *    1. If cache exists and metadata matches the current IDL sha — reuse.
 *    2. If cache exists but stale — refuse, ask for `--regen-sample-app`.
 *    3. If cache missing OR `--regen-sample-app` — render a prompt to scratch
 *       and exit 3 with operator instructions. (No automatic LLM invocation.)
 */
final class SampleAppGen(
  project:     Path,
  lang:        String,
  idlSha:      String,
  scratchRoot: Path,
  repoRoot:    Path,
) {

  // Per-language source extension. The prompt language id (`scala`, `typescript`)
  // and the on-disk file extension diverge for TypeScript (`.ts`) — keep both
  // explicit so future adapters can map cleanly (e.g. `csharp` → `.cs`).
  private val srcExt = lang match {
    case "scala"      => "scala"
    case "typescript" => "ts"
    case other        => other
  }

  private val cacheDir   = project.resolve(".idl-regression")
  private val cacheFile  = cacheDir.resolve(s"sample_app.$srcExt")
  private val metaFile   = cacheDir.resolve(s"sample_app.$srcExt.meta")

  /** Either(needSample) — caller exits 3. Right(path) — usable sample. */
  def resolve(genTree: Path, regen: Boolean): Either[Unit, Path] = {
    val hasCache = Files.isRegularFile(cacheFile)
    val matches  = hasCache && metaMatches()

    if (hasCache && matches && !regen) {
      return Right(cacheFile)
    }

    val reason =
      if (!hasCache) "no sample app cached for this project + language"
      else if (regen) "regeneration requested via --regen-sample-app"
      else "cached sample app is stale (IDL sha mismatch)"

    val promptPath = scratchRoot.resolve(s"sample-app-prompt.$lang.md")
    val sentinel   = scratchRoot.resolve("PROMPT_READY.txt")
    renderPrompt(promptPath, genTree)
    writeSentinel(sentinel, promptPath, reason)

    System.err.println(
      s"""
         |[idl-regress] === manual sample-app step required ===
         |[idl-regress] $reason
         |[idl-regress] prompt written to: $promptPath
         |[idl-regress] sentinel:          $sentinel
         |[idl-regress]
         |[idl-regress] Run an LLM with the prompt (self-contained — includes IDL + generated tree).
         |[idl-regress] Drop the resulting sample_app.$lang at: $cacheFile
         |[idl-regress] Then re-run this command.
         |""".stripMargin
    )
    Left(())
  }

  private def metaMatches(): Boolean = {
    if (!Files.isRegularFile(metaFile)) return false
    val text = new String(Files.readAllBytes(metaFile), StandardCharsets.UTF_8)
    val kv   = text.linesIterator
      .map(_.trim).filter(_.nonEmpty).filterNot(_.startsWith("#"))
      .flatMap { l =>
        val i = l.indexOf('=')
        if (i < 0) None else Some(l.substring(0, i).trim -> l.substring(i + 1).trim)
      }.toMap
    kv.get("idl_sha256").contains(idlSha)
  }

  /** Reads the prompt template and substitutes `{{IDL_TREE}}`, `{{GENERATED_TREE}}`,
   *  `{{RUNTIME_VERSION}}`, `{{IDL_SHA256}}`.
   */
  private def renderPrompt(out: Path, genTree: Path): Unit = {
    val tmpl = repoRoot.resolve(s"regression-harness/prompts/sample-app-$lang.md")
    if (!Files.isRegularFile(tmpl)) {
      throw new RuntimeException(s"missing prompt template: $tmpl")
    }
    val text = new String(Files.readAllBytes(tmpl), StandardCharsets.UTF_8)

    val idlTree  = renderTree(project.resolve("source"))
    val genListed = renderTree(genTree)

    val runtimeVersion = readRuntimeVersion()
    val rendered = text
      .replace("{{IDL_TREE}}", idlTree)
      .replace("{{GENERATED_TREE}}", genListed)
      .replace("{{RUNTIME_VERSION}}", runtimeVersion)
      .replace("{{IDL_SHA256}}", idlSha)

    Files.createDirectories(out.getParent)
    Files.write(out, rendered.getBytes(StandardCharsets.UTF_8))
  }

  private def writeSentinel(sentinel: Path, prompt: Path, reason: String): Unit = {
    val msg =
      s"""# idl-regress — manual sample-app step
         |
         |reason:        $reason
         |prompt:        $prompt
         |drop sample at: $cacheFile
         |metadata at:    $metaFile
         |
         |After dropping the sample, also write meta:
         |
         |  echo idl_sha256=$idlSha > $metaFile
         |
         |Then re-run idl-regress with the same arguments.
         |""".stripMargin
    Files.write(sentinel, msg.getBytes(StandardCharsets.UTF_8))
  }

  private def renderTree(root: Path): String = {
    if (!Files.isDirectory(root)) return s"(missing: $root)"
    val entries = scala.collection.mutable.ArrayBuffer.empty[String]
    Files.walk(root).iterator().asScala.foreach { p =>
      if (Files.isRegularFile(p)) {
        entries += root.relativize(p).toString
      }
    }
    entries.sorted.mkString("\n")
  }

  private def readRuntimeVersion(): String = {
    val versionSbt = repoRoot.resolve("version.sbt")
    if (!Files.isRegularFile(versionSbt)) return "UNKNOWN"
    val text = new String(Files.readAllBytes(versionSbt), StandardCharsets.UTF_8)
    val rx   = """"([^"]+)"""".r
    rx.findFirstMatchIn(text).map(_.group(1)).getOrElse("UNKNOWN")
  }
}
