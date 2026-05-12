package izumi.idealingua.harness

import izumi.idealingua.translator.compat.NewTyperPipeline
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-2/3/5 diagnostics: regression guard that the new typer
  * pipeline accepts every domain in the corpus.
  *
  * Originally a *positive* spec listing the F-followup domains expected to
  * be rejected (with the expected diagnostic kind for each). Inverted on
  * 2026-05-12 after the final F5e + F5c cleanup landed and the rejection
  * list emptied: the spec now asserts that `NewTyperPipeline.run` succeeds
  * on all 28 corpus domains. Any future regression where a previously
  * accepted domain is rejected will fail this spec and surface the captured
  * diagnostic in CI logs.
  *
  * History (all resolved):
  *   - `{idltest.json}` (F1) and `{idltest.ast}` (F5d) — CycleDetector
  *     container-indirection fix, 2026-05-12.
  *   - `{izumi.test.clashing}` (F4), `{idltest.aliases}` (F5a),
  *     `{izumi.test.domain02}` (F5b) — NameResolver cross-domain-scope +
  *     alias-as-mixin dealias fix (`PR-02 IMPL-2/3-fix`), 2026-05-12.
  *   - `{idltest.inheritance}` (F2) — StructuralFlattener
  *     covariant-field-merge fix, 2026-05-12.
  *   - `{idltest.consts}` (F3) — ConstValueTyper top-level untyped +
  *     list-literal routing fix, 2026-05-12.
  *   - `{idltest.consts}` (F5e) — fixture typo `anotherString: XXX`
  *     corrected to `str`, 2026-05-12.
  *   - `{idltest.services}` (F5c T1) — EphemeralSynthesizer auto-wraps
  *     non-DTO alt-output branches, 2026-05-12.
  *   - `{idltest.services}` (F5c T2/T3) — AdtConflictsRule skips
  *     synthesized ADTs (`Domain.ephemeralOwner` keys), 2026-05-12.
  */
final class TyperDiagnosticsSpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  /** Per-domain expectation: id → expected diagnostic-kind substring.
    *
    * Empty as of 2026-05-12: parity gate is fully unblocked.
    */
  private val expectations: Seq[(String, String)] = Seq.empty

  test("new typer accepts every domain in the corpus (regression guard)") {
    val fullCorpus = HarnessCorpus.loadCorpus(corpusRoot)

    val capturedLines        = scala.collection.mutable.Buffer.empty[String]
    val unexpectedlyRejected = scala.collection.mutable.Buffer.empty[String]
    val unexpectedlyAccepted = scala.collection.mutable.Buffer.empty[String]
    val byId                 = fullCorpus.map(d => d.parsed.id.toString -> d).toMap

    // Pass 1 — every corpus domain must be accepted by the new typer.
    for (domain <- fullCorpus) {
      val id = domain.parsed.id.toString
      try {
        val _ = NewTyperPipeline.run(domain.parsed)
      } catch {
        case t: Throwable =>
          unexpectedlyRejected += id
          capturedLines += s"$id (UNEXPECTED REJECTION):"
          for (line <- t.getMessage.linesIterator) capturedLines += s"  $line"
      }
    }

    // Pass 2 — any residual entries in `expectations` are checked positively
    // (rejection with the documented diagnostic substring). Empty by design.
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

    if (capturedLines.nonEmpty) info(capturedLines.mkString("\n"))

    if (unexpectedlyRejected.nonEmpty) {
      fail(s"New typer regressed on ${unexpectedlyRejected.size} domain(s) that previously passed: ${unexpectedlyRejected.mkString(", ")}\n\nFull capture:\n${capturedLines.mkString("\n")}")
    }
    if (unexpectedlyAccepted.nonEmpty) {
      fail(s"The following domains are no longer rejected by NewTyperPipeline and should be removed from `expectations`: ${unexpectedlyAccepted.mkString(", ")}\n\nFull capture:\n${capturedLines.mkString("\n")}")
    }
  }
}
