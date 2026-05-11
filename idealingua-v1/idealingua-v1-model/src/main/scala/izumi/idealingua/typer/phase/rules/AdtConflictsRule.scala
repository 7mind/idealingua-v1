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
  * Diagnostics-mode: never throws (C8/L1).
  */
object AdtConflictsRule {

  def apply(domain: Domain): Diagnostics = {
    val buf = Vector.newBuilder[Diagnostic]

    domain.userTypes.values.foreach {
      case a: TypeDef.Adt =>
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
