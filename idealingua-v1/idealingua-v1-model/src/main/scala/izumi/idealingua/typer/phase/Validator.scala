package izumi.idealingua.typer.phase

import izumi.idealingua.typer.ir.{Diagnostics, Domain}
import izumi.idealingua.typer.phase.rules._

/** Phase 12 — `Validator`.
  *
  * Runs all 7 structural validation rules against the frozen `Domain` IR
  * produced by Phase 11 (`Assembler`) and accumulates the resulting
  * `Diagnostics`.
  *
  * Rules are applied in declaration order; each rule independently consumes
  * `Domain` and returns `Diagnostics`.  Results are merged via `Diagnostics.++`.
  *
  * Per C8/L1: the Validator emits diagnostics, never throws.
  *
  * Overlap with earlier phases:
  *   - `CyclicInheritanceRule` and `CyclicUsageRule` re-inspect `Domain.loops`
  *     (populated by Phase 5) as a defense-in-depth safety net.
  *   - `CyclicImportsRule` is a no-op in this phase: import-cycle detection is
  *     Phase 0's responsibility (emits `CyclicDomainImport`); the frozen
  *     `Domain` IR does not carry the inter-domain adjacency graph required for
  *     re-validation.
  *
  * Caller contract: `Validator.apply(domain)` returns a `Diagnostics`
  * accumulator that is separate from `Domain` (the frozen IR is not mutated).
  * Downstream consumers decide how to handle or display the returned diagnostics.
  */
object Validator {

  private val rules: List[Domain => Diagnostics] = List(
    BasicNamingConventionsRule.apply,
    DuplicateMemberRule.apply,
    CyclicInheritanceRule.apply,
    AdtMembersRule.apply,
    AdtConflictsRule.apply,
    CyclicUsageRule.apply,
    CyclicImportsRule.apply,
  )

  def apply(domain: Domain): Diagnostics =
    rules.foldLeft(Diagnostics.empty) { (acc, rule) => acc ++ rule(domain) }
}
