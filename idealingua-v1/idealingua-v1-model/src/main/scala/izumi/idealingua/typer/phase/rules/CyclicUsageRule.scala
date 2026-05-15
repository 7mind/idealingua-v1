package izumi.idealingua.typer.phase.rules

import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, Domain}

/** Validator rule (re-validation): non-terminating cycles in the assembled Domain.
  *
  * Phase 5 (CycleDetector) already detects cycles and emits `Diagnostic.CyclicUsage`
  * and `Diagnostic.CyclicInheritance` into `ResolvedDomain.diagnostics` before
  * assembly.  This Validator rule provides defense-in-depth by re-inspecting
  * `Domain.loops` and emitting `CyclicUsage` for every non-terminating cycle
  * found there.
  *
  * Overlap note: the assembled `Domain.loops` (`Set[Cycle[TypeId]]`) does not
  * carry per-edge kind metadata (the edge-kind annotation lives in `ResolvedDomain`
  * during Phase 5 only and is not projected into the frozen `Domain`).  This
  * makes it impossible to distinguish inheritance-only cycles from field-reference
  * cycles in the Validator.  Both are therefore reported as `CyclicUsage` here;
  * the authoritative per-kind split is Phase 5's responsibility.
  *
  * Terminating cycles (e.g. `List<Self>`) are skipped — they are legal.
  *
  * Diagnostics-mode: never throws (C8/L1).
  */
object CyclicUsageRule {

  def apply(domain: Domain): Diagnostics = {
    val buf = Vector.newBuilder[Diagnostic]

    domain.loops.foreach { cycle =>
      if (!cycle.terminating) {
        val pos = cycle.members.headOption
          .flatMap(id => domain.userTypes.get(id))
          .map(_.meta.pos)
          .getOrElse(InputPosition.Undefined)
        buf += Diagnostic.CyclicUsage(cycle.members, pos)
      }
    }

    Diagnostics(buf.result())
  }
}
