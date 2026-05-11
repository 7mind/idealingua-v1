package izumi.idealingua.typer.ir

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshLoaded

/** Cross-domain index built by Phase 0 (`IdealinguaFamilyManager`).
  *
  * Maps every `DomainId` in the family (root + all transitive imports) to its
  * parsed AST (`DomainMeshLoaded`) and records the dependency graph and
  * topological processing order.
  *
  * Consumed by Phase 1 (`ScopeBuilder`) to resolve cross-domain imports without
  * recursive re-typing (closing F18 — the IMPL-2 bypass).
  *
  * @param domains      Every domain in the family (root + transitive imports),
  *                     keyed by `DomainId`.
  * @param importGraph  Direct import edges: `domain → Set[directly imported domains]`.
  * @param loadOrder    Topological order (imports-first) so downstream phases can
  *                     process dependencies before dependants.  Cyclic SCCs are
  *                     expanded in alphabetic `DomainId.toString` order.
  * @param diagnostics  Import-graph diagnostics (cycles, missing imports).
  */
final case class FamilyIndex(
  domains: Map[DomainId, DomainMeshLoaded],
  importGraph: Map[DomainId, Set[DomainId]],
  loadOrder: List[DomainId],
  diagnostics: Diagnostics,
)
