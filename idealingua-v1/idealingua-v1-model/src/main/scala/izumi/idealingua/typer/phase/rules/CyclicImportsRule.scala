package izumi.idealingua.typer.phase.rules

import izumi.idealingua.typer.ir.{Diagnostics, Domain}

/** Validator rule: cyclic domain imports.
  *
  * The legacy `CyclicImportsRule` traversed the live `Typespace.domain.referenced`
  * map to detect cycles in the domain import graph.  In the new IR, the frozen
  * `Domain` does not carry inter-domain adjacency — it only holds transitive
  * imported types in `Domain.imports: Map[DomainId, Set[TypeId]]`.  Cycle
  * detection across that flat map is not feasible without reconstructing the
  * full import graph.
  *
  * Phase 0 (IdealinguaFamilyManager) already detects import cycles and emits
  * `Diagnostic.CyclicDomainImport` diagnostics into the `FamilyIndex` before
  * any per-domain typing begins.
  *
  * Decision: this rule is a structural no-op in the Validator.  It is included
  * for symmetry with the legacy 7-rule set and serves as a placeholder should
  * the `Domain` IR later be extended to carry the import graph.
  *
  * Diagnostics-mode: never throws (C8/L1).
  */
object CyclicImportsRule {

  def apply(domain: Domain): Diagnostics = { val _ = domain; Diagnostics.empty }
}
