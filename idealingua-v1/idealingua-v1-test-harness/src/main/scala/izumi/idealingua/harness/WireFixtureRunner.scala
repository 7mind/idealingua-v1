package izumi.idealingua.harness

import io.circe.{Json, parser}

import java.nio.charset.StandardCharsets
import java.nio.file.Path

/** Thrown when any wire fixture fails round-trip verification. */
final class WireFixtureVerificationFailure(message: String) extends RuntimeException(message)

/**
  * Layer B round-trip executor.
  *
  * For each fixture file F.json:
  *   1. Parse bytes as io.circe.Json. On failure → DecodeFailed.
  *   2. Look up WireDispatch.entries(wireId). On miss → UnknownWireId.
  *   3. Decode via entry.decode(json). On failure → DecodeFailed.
  *   4. Re-encode via entry.encode(value) → reJson.
  *   5. Compare reJson.noSpaces.getBytes(UTF-8) byte-for-byte against fixture bytes. On mismatch:
  *      - If parse(bytes).noSpaces != reJson.noSpaces → RoundtripDivergence (semantic difference).
  *      - Otherwise → WhitespaceMismatch (fixture has extra whitespace; authoring error).
  *
  * Producing kinds: DecodeFailed, RoundtripDivergence, WhitespaceMismatch, UnknownWireId.
  */
private[harness] object WireFixtureRunner {

  sealed trait FailureKind
  object FailureKind {
    case object DecodeFailed        extends FailureKind
    case object WhitespaceMismatch  extends FailureKind
    case object RoundtripDivergence extends FailureKind
    case object UnknownWireId       extends FailureKind
  }

  final case class Failure(
    file: Path,
    wireId: String,
    scenario: String,
    kind: FailureKind,
    detail: String,
  )

  def runAll(root: Path): Int = {
    val fixtures = WireFixtures.load(root)
    val failures = fixtures.flatMap(verifyOne)
    if (failures.nonEmpty) {
      throw new WireFixtureVerificationFailure(formatReport(fixtures.size, failures))
    }
    fixtures.size
  }

  private def verifyOne(f: WireFixtures.FixtureFile): Option[Failure] = {
    // Step 1: parse
    val json: Json = parser.parse(new String(f.bytes, StandardCharsets.UTF_8)) match {
      case Left(parseError) =>
        return Some(Failure(f.file, f.wireId, f.scenario, FailureKind.DecodeFailed,
          s"JSON parse error: ${parseError.message}"))
      case Right(j) => j
    }

    // Step 2: look up dispatch entry
    val entry: RoundTripEntry = WireDispatch.entries.get(f.wireId) match {
      case None =>
        return Some(Failure(f.file, f.wireId, f.scenario, FailureKind.UnknownWireId,
          s"No WireDispatch entry for wireId '${f.wireId}'"))
      case Some(e) => e
    }

    // Step 3: decode
    val decoded: Any = entry.decode(json) match {
      case Left(decodingFailure) =>
        return Some(Failure(f.file, f.wireId, f.scenario, FailureKind.DecodeFailed,
          s"Decode error: ${decodingFailure.getMessage}"))
      case Right(v) => v
    }

    // Step 4: re-encode
    val reJson    = entry.encode(decoded)
    val reBytes   = reJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    val canonical = json.noSpaces

    // Step 5: byte-strict equality.
    // byte-mismatch is split into RoundtripDivergence (semantic difference) + WhitespaceMismatch (whitespace authoring error).
    if (!java.util.Arrays.equals(f.bytes, reBytes)) {
      // Distinguish authoring error (fixture has extra whitespace) from semantic divergence
      if (canonical != reJson.noSpaces) {
        return Some(Failure(f.file, f.wireId, f.scenario, FailureKind.RoundtripDivergence,
          s"Canonical form of fixture diverges from re-encoded value.\n" +
            s"  fixture canonical : $canonical\n" +
            s"  re-encoded noSpaces: ${reJson.noSpaces}"))
      } else {
        // canonical == reJson.noSpaces but raw bytes differ → fixture has whitespace
        return Some(Failure(f.file, f.wireId, f.scenario, FailureKind.WhitespaceMismatch,
          s"Fixture bytes are not noSpaces form. Fixture canonical matches encoder output but raw bytes differ.\n" +
            s"  expected (noSpaces): ${reJson.noSpaces.take(200)}\n" +
            s"  fixture raw prefix : ${new String(f.bytes.take(200), StandardCharsets.UTF_8)}"))
      }
    }

    None
  }

  private def formatReport(total: Int, failures: Seq[Failure]): String = {
    val sb = new StringBuilder
    sb.append(s"Wire fixture verification failed ($total fixtures checked, ${failures.size} failed).\n")

    val byKind = failures.groupBy(_.kind)
    val orderedKinds = Seq(
      FailureKind.UnknownWireId,
      FailureKind.DecodeFailed,
      FailureKind.RoundtripDivergence,
      FailureKind.WhitespaceMismatch,
    )

    for (kind <- orderedKinds if byKind.contains(kind)) {
      val fs = byKind(kind)
      val hint = kind match {
        case FailureKind.UnknownWireId       => " — add WireDispatch entry"
        case FailureKind.DecodeFailed        => ""
        case FailureKind.RoundtripDivergence => " — encoder output changed"
        case FailureKind.WhitespaceMismatch  => " — fixture has whitespace; re-save as noSpaces"
      }
      sb.append(s"\n${kind}$hint (${fs.size} fixtures):\n")
      fs.foreach { f =>
        sb.append(s"  ${f.file} (${f.wireId})\n")
        f.detail.linesIterator.foreach(line => sb.append(s"    $line\n"))
      }
    }

    sb.toString()
  }
}
