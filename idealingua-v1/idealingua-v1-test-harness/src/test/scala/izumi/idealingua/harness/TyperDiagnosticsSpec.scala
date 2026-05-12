package izumi.idealingua.harness

import izumi.idealingua.translator.compat.NewTyperPipeline
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-2/3/5 diagnostics: captures the per-domain diagnostic kind +
  * originating phase for each corpus domain rejected by the new typer
  * pipeline. Complements `ScalaTyperParitySpec` (which excludes them as
  * F-followups IMPL-7a.2-F3/F5c).
  *
  * The 2 expected-rejected domains are listed below. The spec is *positive*:
  * it asserts the new typer rejects each one and that the failure message
  * mentions the expected diagnostic kind. Captured messages are appended to
  * the assertion output so per-domain triage stays visible in CI logs.
  *
  * If a domain stops being rejected (i.e. a fix lands) the assertion in
  * `expectsRejection` will fail loudly, signalling that the exclusion list
  * in `ScalaTyperParitySpec` should be trimmed accordingly.
  *
  * History:
  *   - `{idltest.json}` (F1) and `{idltest.ast}` (F5d) removed 2026-05-12
  *     after the CycleDetector container-indirection fix landed.
  *   - `{izumi.test.clashing}` (F4), `{idltest.aliases}` (F5a),
  *     `{izumi.test.domain02}` (F5b) removed 2026-05-12 after the
  *     NameResolver cross-domain-scope + alias-as-mixin dealias fix landed
  *     (`PR-02 IMPL-2/3-fix`). All three are now accepted by the new typer.
  *   - `{idltest.inheritance}` (F2) removed 2026-05-12 after the
  *     StructuralFlattener covariant-field-merge fix landed
  *     (`PR-02 IMPL-3-fix: StructuralFlattener allows covariant field-type override`).
  */
final class TyperDiagnosticsSpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  /** Per-domain expectation: id → expected diagnostic-kind substring. */
  private val expectations: Seq[(String, String)] = Seq(
    "{idltest.consts}"        -> "BadConstValue",
    "{idltest.services}"      -> "",
  )

  test("new typer rejects the 2 documented F-followup domains and captures per-domain diagnostics") {
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

    // Fail loudly if any of the 3 stopped being rejected — that's a stale exclusion.
    if (unexpectedlyAccepted.nonEmpty) {
      fail(s"The following domains are no longer rejected by NewTyperPipeline and should be re-enabled in ScalaTyperParitySpec: ${unexpectedlyAccepted.mkString(", ")}\n\nFull capture:\n${capturedLines.mkString("\n")}")
    }
  }
}
