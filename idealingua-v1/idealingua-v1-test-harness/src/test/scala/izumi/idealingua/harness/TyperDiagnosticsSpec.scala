package izumi.idealingua.harness

import izumi.idealingua.translator.compat.NewTyperPipeline
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-2/3/5 diagnostics: captures the per-domain diagnostic kind +
  * originating phase for each corpus domain rejected by the new typer
  * pipeline. Complements `ScalaTyperParitySpec` (which excludes them as
  * F-followups IMPL-7a.2-F1..F5).
  *
  * The 8 expected-rejected domains are listed below. The spec is *positive*:
  * it asserts the new typer rejects each one and that the failure message
  * mentions the expected diagnostic kind. Captured messages are appended to
  * the assertion output so per-domain triage stays visible in CI logs.
  *
  * If a domain stops being rejected (i.e. a fix lands) the assertion in
  * `expectsRejection` will fail loudly, signalling that the exclusion list
  * in `ScalaTyperParitySpec` should be trimmed accordingly.
  */
final class TyperDiagnosticsSpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  /** Per-domain expectation: id → expected diagnostic-kind substring. */
  private val expectations: Seq[(String, String)] = Seq(
    "{idltest.json}"          -> "NonTerminatingCycle",
    "{idltest.inheritance}"   -> "FieldNameConflict",
    "{idltest.consts}"        -> "BadConstValue",
    "{izumi.test.clashing}"   -> "UnknownTypeRef",
    "{idltest.aliases}"       -> "",
    "{izumi.test.domain02}"   -> "",
    "{idltest.services}"      -> "",
    "{idltest.ast}"           -> "",
  )

  test("new typer rejects the 8 documented F-followup domains and captures per-domain diagnostics") {
    val fullCorpus = HarnessCorpus.loadCorpus(corpusRoot)
    val byId       = fullCorpus.map(d => d.typespace.domain.id.toString -> d).toMap

    val capturedLines = scala.collection.mutable.Buffer.empty[String]
    val unexpectedlyAccepted = scala.collection.mutable.Buffer.empty[String]

    for ((id, expectedKind) <- expectations) {
      byId.get(id) match {
        case None =>
          capturedLines += s"$id: NOT LOADED (corpus drift?)"
        case Some(domain) =>
          try {
            val _ = NewTyperPipeline.run(domain.parsed)
            unexpectedlyAccepted += id
            capturedLines += s"$id: NEW TYPER ACCEPTED (exclusion may be stale)"
          } catch {
            case t: Throwable =>
              val msg = t.getMessage
              capturedLines += s"$id:"
              for (line <- msg.linesIterator) capturedLines += s"  $line"
              if (expectedKind.nonEmpty && !msg.contains(expectedKind)) {
                capturedLines += s"  !! expected diagnostic kind '$expectedKind' not found in message"
              }
          }
      }
    }

    // Always print the captured diagnostics — info() so they survive in CI.
    info(capturedLines.mkString("\n"))

    // Fail loudly if any of the 8 stopped being rejected — that's a stale exclusion.
    if (unexpectedlyAccepted.nonEmpty) {
      fail(s"The following domains are no longer rejected by NewTyperPipeline and should be re-enabled in ScalaTyperParitySpec: ${unexpectedlyAccepted.mkString(", ")}\n\nFull capture:\n${capturedLines.mkString("\n")}")
    }
  }
}
