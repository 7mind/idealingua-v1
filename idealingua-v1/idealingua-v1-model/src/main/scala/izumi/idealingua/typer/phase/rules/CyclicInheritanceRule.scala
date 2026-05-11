package izumi.idealingua.typer.phase.rules

import izumi.idealingua.typer.ir.{Diagnostics, Domain}

/** Validator rule: inheritance cycles in the assembled Domain.
  *
  * Phase 5 (CycleDetector) detects inheritance-only cycles (SCCs whose every
  * back-edge is an inheritance edge) and emits `Diagnostic.CyclicInheritance`.
  *
  * The assembled `Domain.loops` (`Set[Cycle[TypeId]]`) does NOT carry per-edge
  * kind metadata — only `members` and `terminating`.  Without the edge-kind
  * annotation (which is available only in `ResolvedDomain` during Phase 5), it
  * is not possible to distinguish an inheritance-only SCC from a field-reference
  * SCC in the Validator.
  *
  * Decision: this rule is a structural no-op.  All non-terminating cycles are
  * re-reported as `CyclicUsage` by `CyclicUsageRule`, which serves as the
  * defense-in-depth catch-all.  A future extension could add an
  * `inheritanceCycles: Set[Cycle[TypeId]]` field to `Domain` to support exact
  * re-validation here.
  *
  * Diagnostics-mode: never throws (C8/L1).
  */
object CyclicInheritanceRule {

  def apply(domain: Domain): Diagnostics = { val _ = domain; Diagnostics.empty }
}
