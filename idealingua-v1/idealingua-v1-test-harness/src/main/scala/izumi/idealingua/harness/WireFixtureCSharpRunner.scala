package izumi.idealingua.harness

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import scala.annotation.nowarn

/** PR-03.3b — C#-leg subprocess orchestration. T2 implementation. */
object WireFixtureCSharpRunner {

  sealed trait FailureKind
  object FailureKind {
    case object DecodeFailed        extends FailureKind
    case object RoundtripDivergence extends FailureKind
    case object WhitespaceMismatch  extends FailureKind
    case object UnknownWireId       extends FailureKind
    case object DriverCrashed       extends FailureKind
    case object DriverTimeout       extends FailureKind
  }

  final case class Failure(
    file: Path,
    wireId: String,
    scenario: String,
    kind: FailureKind,
    detail: String,
  )

  @nowarn("cat=unused")
  def runAll(repoRoot: Path, fixturesRoot: Path, harnessCSharpDir: Path): Int = {
    val fixtures = WireFixtures.load(fixturesRoot)
    if (fixtures.isEmpty) return 0

    CSharpDriverBridge.runDriver(harnessCSharpDir, fixtures) match {
      case Left(crashMsg) =>
        throw new WireFixtureVerificationFailure(s"C# driver crashed: $crashMsg")

      case Right(driverResults) =>
        val byKey = driverResults.map(r => (r.wireId, r.scenario) -> r).toMap

        val failures: Seq[Failure] = fixtures.flatMap { f =>
          byKey.get((f.wireId, f.scenario)) match {
            case None =>
              Some(Failure(f.file, f.wireId, f.scenario, FailureKind.DriverCrashed,
                "driver did not return result for this fixture"))

            case Some(r) if !r.ok =>
              val kind = parseKind(r.kind)
              Some(Failure(f.file, f.wireId, f.scenario, kind, r.detail.getOrElse("")))

            case Some(r) =>
              val expected = f.bytes
              val actual   = r.reEncodedJson.getOrElse("").getBytes(StandardCharsets.UTF_8)
              if (java.util.Arrays.equals(expected, actual)) {
                None
              } else {
                val fixtureCanonical = normalizeJson(new String(expected, StandardCharsets.UTF_8))
                val actualStr        = new String(actual, StandardCharsets.UTF_8)
                val kind = if (fixtureCanonical == actualStr) FailureKind.WhitespaceMismatch
                           else FailureKind.RoundtripDivergence
                Some(Failure(f.file, f.wireId, f.scenario, kind,
                  s"expected: ${new String(expected, StandardCharsets.UTF_8)} | actual: $actualStr"))
              }
          }
        }

        if (failures.nonEmpty) {
          throw new WireFixtureVerificationFailure(formatReport(fixtures.size, failures))
        }
        fixtures.size
    }
  }

  private def normalizeJson(raw: String): String = {
    import io.circe.parser._
    parse(raw).toOption.map(_.noSpaces).getOrElse(raw)
  }

  private def parseKind(kindOpt: Option[String]): FailureKind = kindOpt match {
    case Some("DecodeFailed")        => FailureKind.DecodeFailed
    case Some("RoundtripDivergence") => FailureKind.RoundtripDivergence
    case Some("WhitespaceMismatch")  => FailureKind.WhitespaceMismatch
    case Some("UnknownWireId")       => FailureKind.UnknownWireId
    case Some("DriverTimeout")       => FailureKind.DriverTimeout
    case _                           => FailureKind.DriverCrashed
  }

  private def formatReport(total: Int, failures: Seq[Failure]): String = {
    val sb = new StringBuilder
    sb.append(s"C# wire fixture verification failed ($total fixtures checked, ${failures.size} failed).\n")

    val byKind = failures.groupBy(_.kind)
    val orderedKinds = Seq(
      FailureKind.UnknownWireId,
      FailureKind.DecodeFailed,
      FailureKind.RoundtripDivergence,
      FailureKind.WhitespaceMismatch,
      FailureKind.DriverCrashed,
      FailureKind.DriverTimeout,
    )

    for (kind <- orderedKinds if byKind.contains(kind)) {
      val fs = byKind(kind)
      val hint = kind match {
        case FailureKind.UnknownWireId       => " — add Dispatch.cs entry"
        case FailureKind.DecodeFailed        => ""
        case FailureKind.RoundtripDivergence => " — encoder output changed"
        case FailureKind.WhitespaceMismatch  => " — fixture has whitespace; re-save as noSpaces"
        case FailureKind.DriverCrashed       => " — driver did not return result"
        case FailureKind.DriverTimeout       => " — driver subprocess timed out"
      }
      sb.append(s"\n${kind}$hint (${fs.size} fixtures):\n")
      fs.foreach { f =>
        sb.append(s"  ${f.file} (${f.wireId}/${f.scenario})\n")
        f.detail.linesIterator.foreach(line => sb.append(s"    $line\n"))
      }
    }

    sb.toString()
  }
}
