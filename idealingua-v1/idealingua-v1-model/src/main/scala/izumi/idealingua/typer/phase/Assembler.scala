package izumi.idealingua.typer.phase

import izumi.idealingua.typer.ir._

/** Phase 11 — `Assembler`.
  *
  * Pure projection from `ResolvedDomain` (with all Phase 1-10 annotations)
  * to the frozen `Domain` IR.  After this phase, no further mutation is
  * legal (case-class immutability + immutable collections enforce the
  * "frozen-after-assembly" invariant).
  *
  * No diagnostics — pure computation.
  */
object Assembler {

  def apply(rd: ResolvedDomain): Domain = Domain(
    id                = rd.id,
    meta              = rd.meta,
    members           = rd.members,
    roots             = rd.roots,
    ephemeralsOf      = rd.ephemeralsOf,
    ephemeralOwner    = rd.ephemeralOwner,
    flattenedStructs  = rd.flattenedStructs,
    parents           = rd.parents,
    implementingDtos  = rd.implementingDtos,
    loops             = rd.loops,
    fingerprints      = rd.fingerprints,
    domainFingerprint = rd.domainFingerprint,
    imports           = rd.imports,
    consts            = rd.typedConsts,
    aliases           = rd.aliases,
    userTypes         = rd.userTypes,
  )
}
