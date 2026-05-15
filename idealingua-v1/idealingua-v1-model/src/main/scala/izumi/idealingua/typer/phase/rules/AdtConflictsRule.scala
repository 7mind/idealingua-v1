package izumi.idealingua.typer.phase.rules

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, Domain, TypeDef}

/** Validator rule: no two ADT branches with the same wire name within a single
  * ADT definition.
  *
  * Ported from the legacy `AdtConflictsRule`. The legacy rule additionally
  * traversed nested ADTs (ADT-of-ADTs) looking for transitive name conflicts.
  * In the new IR, nested ADTs are already rejected by Phase 4 KindChecker
  * (`NestedAdtMemberUnsupported`), so the check here is limited to direct
  * branch name uniqueness within each ADT.
  *
  * "Branch name" is `AdtMember.typename` (the wire name: `memberName.capitalize`
  * if an alias is provided, otherwise `typeId.name.capitalize`).
  *
  * Emits `Diagnostic.DuplicateAdtBranch` for each conflicting branch name.
  * (Shares the diagnostic kind with `DuplicateMemberRule` because both rules
  * detect duplicate ADT branch names — just from different invariant angles.)
  *
  * Synthesized ADTs (produced by Phase 7 `EphemeralSynthesizer` for service
  * alternative-output method shapes `X !! Y`) are *exempt* from this rule.
  * `EphemeralSynthesizer` does not deduplicate alternative branches by name —
  * `SuccessData !! SuccessData` produces an ADT with two identical branches.
  * Legacy tolerates this (the golden Scala output for `idltest.services`
  * encodes both branches), and the new typer must too for byte parity. The
  * rule remains in force for user-declared ADTs (`Domain.userTypes` entries
  * not appearing as keys in `Domain.ephemeralOwner`). See IMPL-7a.2-F5c T2/T3.
  *
  * Diagnostics-mode: never throws (C8/L1).
  */
object AdtConflictsRule {

  def apply(domain: Domain): Diagnostics = {
    val buf = Vector.newBuilder[Diagnostic]

    domain.userTypes.values.foreach {
      case a: TypeDef.Adt if !domain.ephemeralOwner.contains(a.id) =>
        val pos = a.meta.pos
        // Collect the *type id* of each branch to detect if the same type appears twice.
        val seenIds = scala.collection.mutable.HashMap.empty[TypeId, Int]
        a.alternatives.foreach { branch =>
          seenIds.update(branch.typeId, seenIds.getOrElse(branch.typeId, 0) + 1)
        }
        seenIds.filter(_._2 > 1).keys.foreach { dupId =>
          buf += Diagnostic.DuplicateAdtBranch(a.id, dupId.name, pos)
        }

      case _ =>
    }

    Diagnostics(buf.result())
  }
}
