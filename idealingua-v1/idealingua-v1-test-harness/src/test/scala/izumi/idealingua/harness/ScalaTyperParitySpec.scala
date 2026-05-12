package izumi.idealingua.harness

import izumi.idealingua.translator.toscala.domain.DomainScalaTranslator
import izumi.idealingua.translator.{IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade}
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-7a parity gate: assert byte-equal Scala output between
  * `--typer=legacy` and `--typer=new` across the corpus.
  *
  * Plan §6 V2. Under IMPL-7a.2 Phase A `DomainScalaTranslator` re-derives
  * a legacy `Typespace` from the parsed AST and delegates to the legacy
  * `ScalaTranslator`. Parity is therefore structurally guaranteed for
  * every domain the new-typer pipeline accepts. As Phase B replaces
  * internals with direct `Domain` consumption, this spec becomes the gate
  * that catches every divergence.
  *
  * Exclusions are IR-phase divergences (new typer rejects domains legacy
  * accepts) and are tracked as F-followups in tasks.md. They are out of
  * scope for IMPL-7a per plan §1 non-goals.
  */
final class ScalaTyperParitySpec extends AnyFunSuite {
  private val repoRoot   = HarnessCorpus.repoRootForTests()
  private val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)

  /** Fixtures excluded from the parity comparison.
    *
    * Empty as of 2026-05-12: the new typer accepts the full 28-domain corpus
    * and produces byte-equal Scala output to the legacy typer on every
    * domain.
    *
    * Removed 2026-05-12 after CycleDetector container-indirection fix:
    *   - `{idltest.json}` (F1) — recursive `JSONLike` ADT now passes.
    *   - `{idltest.ast}`  (F5d) — recursive AST through `opt[AST]` now passes.
    *
    * Removed 2026-05-12 after NameResolver cross-domain-scope fix
    * (`PR-02 IMPL-2/3-fix`):
    *   - `{izumi.test.clashing}` (F4)  — sub-domain `#`-qualified refs.
    *   - `{idltest.aliases}`     (F5a) — cross-domain alias targets.
    *   - `{izumi.test.domain02}` (F5b) — selective import + identity alias.
    *
    * Removed 2026-05-12 after StructuralFlattener covariant-field-merge fix
    * (`PR-02 IMPL-3-fix: StructuralFlattener allows covariant field-type override`):
    *   - `{idltest.inheritance}` (F2) — covariant field overrides now soft-merged.
    *
    * Removed 2026-05-12 after the final F5e + F5c cleanup
    * (`PR-02 IMPL-3/5-fix: F5e typo + F5c synthesized-ADT validator relaxation`):
    *   - `{idltest.consts}`   (F5e) — fixture-typo `anotherString: XXX` corrected to `str`.
    *   - `{idltest.services}` (F5c T2/T3) — `AdtConflictsRule` skips synthesized
    *     ADTs (`Domain.ephemeralOwner` keys), so the duplicate `SuccessData`
    *     branches synthesized for `SuccessData !! SuccessData` no longer trigger
    *     `DuplicateAdtBranch`.
    */
  private val excludedDomainIds: Set[String] = Set.empty

  /** Per-domain parity assertion. Each non-excluded domain is compiled
    * twice in isolation (legacy + new) and bytes are compared. Per-domain
    * isolation gives clean diagnostics: one failing domain does not
    * mask others.
    */
  test("Scala translator: --typer=new produces byte-equal output to --typer=legacy on the corpus") {
    val fullCorpus = HarnessCorpus.loadCorpus(corpusRoot)

    val legacyOpts = HarnessOptions.optionsFor(IDLLanguage.Scala).copy(typerImpl = TyperImpl.Legacy)
    val newOpts    = legacyOpts.copy(typerImpl = TyperImpl.NewTyper)

    val typerRejections = scala.collection.mutable.Buffer.empty[String]
    val byteDivergences = scala.collection.mutable.Buffer.empty[String]
    var checked         = 0

    for (domain <- fullCorpus) {
      val id = domain.typespace.domain.id.toString
      if (!excludedDomainIds.contains(id)) {
        checked += 1
        val legacyOut = new TypespaceCompilerBaseFacade(legacyOpts).compile(Seq(domain))
        try {
          val newOut    = new TypespaceCompilerBaseFacade(newOpts).compile(Seq(domain))
          val legacyMap = legacyOut.modules.map(m => m.id.toString -> m.content).toMap
          val newMap    = newOut.modules.map(m => m.id.toString -> m.content).toMap
          if (legacyMap.keySet != newMap.keySet) {
            byteDivergences += s"$id: keyset diverges (only legacy=${(legacyMap.keySet -- newMap.keySet).toSeq.sorted.take(3).mkString(",")}; only new=${(newMap.keySet -- legacyMap.keySet).toSeq.sorted.take(3).mkString(",")})"
          } else {
            val diffs = legacyMap.toSeq.sortBy(_._1).flatMap {
              case (path, lc) => if (newMap(path) == lc) None else Some(path)
            }
            if (diffs.nonEmpty) byteDivergences += s"$id: ${diffs.size} byte diff(s) e.g. ${diffs.take(2).mkString(",")}"
          }
        } catch {
          case t: Throwable =>
            typerRejections += s"$id => ${t.getMessage.linesIterator.take(1).mkString}"
        }
      }
    }

    val _ = assert(checked > 0, "parity spec compared 0 domains; corpus exclusion list is over-broad")

    // IMPL-7a.2 Phase B M2 corpus-wide exerciser must produce ZERO
    // alias/enum renderer divergences across all 28 domains after the
    // PR-02 IMPL-2-fix TypeId normalization (same-domain `path.domain`
    // matches legacy `IDLPostTyper.fixPkg` output). Failure here means a
    // future refactor reintroduced the IR-shape divergence.
    val rendererDivergences = DomainScalaTranslator.rendererDivergences

    // IMPL-7a.2 Phase B M3 structural-renderer divergences (Id/DTO/Interface)
    // are informational only: under the relaxed parity bar the structural
    // renderers need not match legacy bytes. Surface a one-line summary so
    // the divergence count is visible in CI output.
    val structuralDivergences = DomainScalaTranslator.rendererStructuralDivergences
    info(s"M3 structural renderer divergences (informational, not gated): ${structuralDivergences.size}")
    if (structuralDivergences.nonEmpty) {
      info(s"  first 5: ${structuralDivergences.take(5).mkString(" | ")}")
    }

    if (typerRejections.nonEmpty || byteDivergences.nonEmpty || rendererDivergences.nonEmpty) {
      val msg = new StringBuilder()
      val _   = msg.append(s"checked $checked domain(s)\n")
      if (typerRejections.nonEmpty) {
        val _ = msg.append(s"New typer rejected ${typerRejections.size} domain(s):\n").append(typerRejections.mkString("\n")).append("\n")
      }
      if (byteDivergences.nonEmpty) {
        val _ = msg.append(s"Byte parity diverged in ${byteDivergences.size} domain(s):\n").append(byteDivergences.mkString("\n")).append("\n")
      }
      if (rendererDivergences.nonEmpty) {
        val _ = msg.append(s"M2 alias/enum renderer exerciser observed ${rendererDivergences.size} divergence(s):\n")
          .append(rendererDivergences.take(10).mkString("\n"))
      }
      fail(msg.toString)
    }
  }
}
