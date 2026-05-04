package izumi.idealingua.harness

import java.nio.file.{Files, Path}
import java.util.stream.Collectors
import scala.jdk.CollectionConverters._

/** Thrown when the on-disk goldens diverge from the compiler's current output. */
final class GoldenVerificationFailure(msg: String) extends RuntimeException(msg)

object GoldenVerifier {

  /**
    * Verifies that the on-disk goldens under goldenRoot match the compiler's current output for
    * the corpus at corpusRoot.
    *
    * Throws GoldenVerificationFailure with a structured diff report if any files are missing,
    * stale, or have mismatched content.
    */
  def verify(corpusRoot: Path, goldenRoot: Path): Unit = {
    val loaded   = HarnessCorpus.loadCorpus(corpusRoot)
    val produced = GoldenCompile.compileAll(loaded, goldenRoot)
    val onDisk   = walkGoldens(goldenRoot)

    val missing    = produced.keySet.diff(onDisk.keySet)
    val stale      = onDisk.keySet.diff(produced.keySet)
    val mismatched = produced.keySet.intersect(onDisk.keySet)
      .filterNot(p => java.util.Arrays.equals(produced(p), onDisk(p)))

    if (missing.nonEmpty || stale.nonEmpty || mismatched.nonEmpty) {
      val msg = formatDiffReport(missing, stale, mismatched, goldenRoot)
      throw new GoldenVerificationFailure(msg)
    }
  }

  private def walkGoldens(goldenRoot: Path): Map[Path, Array[Byte]] = {
    if (!Files.exists(goldenRoot)) return Map.empty

    val stream = Files.walk(goldenRoot)
    try {
      stream
        .collect(Collectors.toList[Path]())
        .asScala
        .filter(Files.isRegularFile(_))
        .map(p => p -> Files.readAllBytes(p))
        .toMap
    } finally {
      stream.close()
    }
  }

  private def formatDiffReport(
    missing: Set[Path],
    stale: Set[Path],
    mismatched: Set[Path],
    goldenRoot: Path,
  ): String = {
    val sb = new StringBuilder
    sb.append("Golden verification failed.\n")

    if (missing.nonEmpty) {
      sb.append(s"\nMissing goldens (${missing.size} files — run regenerateGoldens to create them):\n")
      missing.toSeq.sorted.foreach(p => sb.append(s"  Missing golden: ${goldenRoot.relativize(p)}\n"))
    }

    if (stale.nonEmpty) {
      sb.append(s"\nStale goldens (${stale.size} files — run regenerateGoldens to remove them):\n")
      stale.toSeq.sorted.foreach(p => sb.append(s"  Stale golden: ${goldenRoot.relativize(p)}\n"))
    }

    if (mismatched.nonEmpty) {
      sb.append(s"\nMismatched goldens (${mismatched.size} files — run regenerateGoldens to update them):\n")
      mismatched.toSeq.sorted.foreach(p => sb.append(s"  Golden mismatch: ${goldenRoot.relativize(p)}\n"))
    }

    sb.toString()
  }
}
