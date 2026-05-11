package izumi.idealingua.typer.phase.rules

import izumi.idealingua.model.common.Builtin
import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, Domain, Member, TypeDef}

/** Validator rule: ADT branches must be user types, not primitives.
  *
  * Ported from the legacy `AdtMembersRule`. An ADT branch whose `typeId`
  * resolves to a `Member.Builtin` (i.e. a primitive such as `Primitive.TString`)
  * is invalid — ADT members must be named user types (DTO, Interface, or
  * Identifier).
  *
  * Note: Phase 4 (KindChecker) rejects nested ADTs via `NestedAdtMemberUnsupported`.
  * This rule covers the orthogonal invariant: primitive branches.
  *
  * Emits `Diagnostic.PrimitiveAdtMember` for each primitive branch found.
  * Diagnostics-mode: never throws (C8/L1).
  */
object AdtMembersRule {

  def apply(domain: Domain): Diagnostics = {
    val buf = Vector.newBuilder[Diagnostic]

    domain.userTypes.values.foreach {
      case a: TypeDef.Adt =>
        val pos = a.meta.pos
        a.alternatives.foreach { branch =>
          val isBuiltin = branch.typeId.isInstanceOf[Builtin] ||
            domain.members.get(branch.typeId).exists {
              case _: Member.Builtin => true
              case _                 => false
            }
          if (isBuiltin) {
            buf += Diagnostic.PrimitiveAdtMember(a.id, branch.typeId, pos)
          }
        }

      case _ =>
    }

    Diagnostics(buf.result())
  }
}
