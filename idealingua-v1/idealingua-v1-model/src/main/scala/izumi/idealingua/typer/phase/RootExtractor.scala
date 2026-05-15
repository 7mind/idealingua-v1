package izumi.idealingua.typer.phase

import izumi.idealingua.typer.ir._

/** Phase 10 — `RootExtractor`.
  *
  * Computes `roots = rd.userTypes.keySet -- rd.ephemeralOwner.keySet`. The
  * resulting set contains only user-declared top-level `TypeId`s; ephemerals
  * (which are inserted into `userTypes` by Phase 7 for ADT mirrors and
  * DTO→Interface mirrors) are excluded, as are builtins (which never enter
  * `userTypes`).
  *
  * No diagnostics — pure computation.
  */
object RootExtractor {

  def apply(rd: ResolvedDomain): ResolvedDomain = {
    val ephemerals = rd.ephemeralOwner.keySet
    rd.copy(roots = rd.userTypes.keySet -- ephemerals)
  }
}
