package izumi.idealingua.typer.phase.rules

import izumi.idealingua.typer.ir.{Diagnostic, Diagnostics, Domain, TypeDef}

/** Validator rule: no duplicate member names in enums or ADTs.
  *
  * Ported from the legacy `DuplicateMemberRule`. Two sub-checks:
  *   1. Enum: no two `EnumMember`s with the same `value` string.
  *   2. ADT:  no two `AdtMember`s whose `typename` (wire name after capitalize)
  *            collides.
  *
  * Emits one `Diagnostic.DuplicateEnumMember` per offending enum-value and one
  * `Diagnostic.DuplicateAdtBranch` per offending branch name.
  *
  * Diagnostics-mode: never throws (C8/L1).
  */
object DuplicateMemberRule {

  def apply(domain: Domain): Diagnostics = {
    val buf = Vector.newBuilder[Diagnostic]

    domain.userTypes.values.foreach {
      case e: TypeDef.Enum =>
        val pos = e.meta.pos
        e.members
          .groupBy(_.value)
          .filter(_._2.lengthCompare(1) > 0)
          .keys
          .foreach { dupValue =>
            buf += Diagnostic.DuplicateEnumMember(e.id, dupValue, pos)
          }

      case a: TypeDef.Adt =>
        val pos = a.meta.pos
        a.alternatives
          .groupBy(_.typename)
          .filter(_._2.lengthCompare(1) > 0)
          .keys
          .foreach { dupName =>
            buf += Diagnostic.DuplicateAdtBranch(a.id, dupName, pos)
          }

      case _ =>
    }

    Diagnostics(buf.result())
  }
}
