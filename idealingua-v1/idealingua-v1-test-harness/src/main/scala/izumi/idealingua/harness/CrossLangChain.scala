package izumi.idealingua.harness

import java.nio.charset.StandardCharsets
import java.util.Arrays

import io.circe.parser._

/** PR-03.4 T2 — Per-tuple chain orchestrator. */
private[harness] object CrossLangChain {

  sealed trait Lang { def name: String }
  object Lang {
    case object Scala      extends Lang { val name = "Scala"      }
    case object Typescript extends Lang { val name = "Typescript" }
    case object CSharp     extends Lang { val name = "CSharp"     }
  }

  sealed trait CrossLangFailureKind
  object CrossLangFailureKind {
    final case class DecodeFailedAt(lang: Lang, detail: String) extends CrossLangFailureKind
    final case class EncodeFailedAt(lang: Lang, detail: String) extends CrossLangFailureKind
    final case class ChainEndpointMismatch(detail: String)      extends CrossLangFailureKind
    final case class WhitespaceMismatch(detail: String)         extends CrossLangFailureKind
    final case class UnknownWireIdAt(lang: Lang)                extends CrossLangFailureKind
    final case class DriverCrashed(lang: Lang, detail: String)  extends CrossLangFailureKind
    final case class DriverTimeout(lang: Lang)                  extends CrossLangFailureKind
    final case class Excluded(reason: String)                   extends CrossLangFailureKind
  }

  final case class ChainOutcome(ok: Boolean, kind: Option[CrossLangFailureKind] = None)

  /** Function shape: (wireId, json) => RoundtripResponse. Adapts both Scala in-JVM and TS/C# subprocess. */
  type RoundtripFn = (String, String) => CrossLangScalaAdapter.RoundtripResponse

  /**
    * Execute one chain: source-fixture → decode_M → encode_M → decode_S → encode_S → byte-compare.
    *
    * Chain: fixture_bytes (S canonical) → M roundtrip → S roundtrip → byte-compare against fixture_bytes.
    */
  def run(
    s: Lang,
    m: Lang,
    fixture: WireFixtures.FixtureFile,
    daemonM: RoundtripFn,
    daemonS: RoundtripFn,
  ): ChainOutcome = {
    val fixtureJsonStr = new String(fixture.bytes, StandardCharsets.UTF_8)

    // Stage 1: M decodes & re-encodes S's fixture
    val resp_M = daemonM(fixture.wireId, fixtureJsonStr)
    if (!resp_M.ok) return failureFromResponse(resp_M, m)

    val json_M = resp_M.reEncodedJson match {
      case Some(j) => j
      case None    => return ChainOutcome(ok = false, kind = Some(CrossLangFailureKind.DecodeFailedAt(m, "no reEncodedJson in ok response")))
    }

    // Stage 2: S decodes M's output, re-encodes
    val resp_S = daemonS(fixture.wireId, json_M)
    if (!resp_S.ok) return failureFromResponse(resp_S, s)

    val finalJsonStr = resp_S.reEncodedJson match {
      case Some(j) => j
      case None    => return ChainOutcome(ok = false, kind = Some(CrossLangFailureKind.DecodeFailedAt(s, "no reEncodedJson in ok response")))
    }

    val finalBytes = finalJsonStr.getBytes(StandardCharsets.UTF_8)

    // Stage 3: byte-strict compare against source fixture
    if (Arrays.equals(fixture.bytes, finalBytes)) {
      ChainOutcome(ok = true)
    } else {
      // Distinguish whitespace-only divergence from semantic divergence
      val parsedFixture = parse(fixtureJsonStr).toOption.map(_.noSpaces)
      val parsedFinal   = parse(finalJsonStr).toOption.map(_.noSpaces)
      if (parsedFixture.isDefined && parsedFixture == parsedFinal) {
        ChainOutcome(ok = false, kind = Some(CrossLangFailureKind.WhitespaceMismatch(
          s"fixture=${parsedFixture.get} final=${parsedFinal.get}"
        )))
      } else {
        ChainOutcome(ok = false, kind = Some(CrossLangFailureKind.ChainEndpointMismatch(
          s"expected=${fixtureJsonStr} actual=${finalJsonStr}"
        )))
      }
    }
  }

  private def failureFromResponse(resp: CrossLangScalaAdapter.RoundtripResponse, lang: Lang): ChainOutcome = {
    val detail = resp.detail.getOrElse("")
    val kind = resp.kind.getOrElse("DecodeFailed") match {
      case "UnknownWireId" => CrossLangFailureKind.UnknownWireIdAt(lang)
      case "DecodeFailed"  => CrossLangFailureKind.DecodeFailedAt(lang, detail)
      case "DriverCrashed" => CrossLangFailureKind.DriverCrashed(lang, detail)
      case "DriverTimeout" => CrossLangFailureKind.DriverTimeout(lang)
      case other           => CrossLangFailureKind.DecodeFailedAt(lang, s"$other: $detail")
    }
    ChainOutcome(ok = false, kind = Some(kind))
  }
}
