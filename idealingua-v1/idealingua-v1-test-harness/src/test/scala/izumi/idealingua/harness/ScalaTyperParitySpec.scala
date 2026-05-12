package izumi.idealingua.harness

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
    * These are IR-phase divergences (Phase 6 StructuralFlattener / Phase 2
    * NameResolver / Phase 8 ConstValueTyper / Phase 12 Validator) — not
    * Scala translator-port divergences.
    *
    *   - `{idltest.inheritance}`: covariant field overrides flagged
    *     FieldNameConflict by new StructuralFlattener. Legacy accepts.
    *     F-followup IMPL-7a.2-F2.
    *   - `{idltest.consts}`: top-level const value categories rejected
    *     by new ConstValueTyper (BadConstValue). F-followup IMPL-7a.2-F3.
    *   - `{izumi.test.clashing}`: cross-domain type references rejected
    *     by new NameResolver (UnknownTypeRef). F-followup IMPL-7a.2-F4.
    *   - `{idltest.aliases}`, `{izumi.test.domain02}`, `{idltest.services}`:
    *     additional IR-phase rejections. See test output diagnostics if
    *     re-enabled. F-followups IMPL-7a.2-F5a/F5b/F5c.
    *
    * Removed 2026-05-12 after CycleDetector container-indirection fix:
    *   - `{idltest.json}` (F1) — recursive `JSONLike` ADT now passes.
    *   - `{idltest.ast}`  (F5d) — recursive AST through `opt[AST]` now passes.
    */
  private val excludedDomainIds: Set[String] = Set(
    "{idltest.inheritance}",
    "{idltest.consts}",
    "{izumi.test.clashing}",
    "{idltest.aliases}",
    "{izumi.test.domain02}",
    "{idltest.services}",
  )

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

    if (typerRejections.nonEmpty || byteDivergences.nonEmpty) {
      val msg = new StringBuilder()
      val _   = msg.append(s"checked $checked domain(s)\n")
      if (typerRejections.nonEmpty) {
        val _ = msg.append(s"New typer rejected ${typerRejections.size} domain(s):\n").append(typerRejections.mkString("\n")).append("\n")
      }
      if (byteDivergences.nonEmpty) {
        val _ = msg.append(s"Byte parity diverged in ${byteDivergences.size} domain(s):\n").append(byteDivergences.mkString("\n"))
      }
      fail(msg.toString)
    }
  }
}
