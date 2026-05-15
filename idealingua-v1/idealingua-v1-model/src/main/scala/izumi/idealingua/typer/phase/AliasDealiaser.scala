package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.typer.ir._

import scala.collection.mutable

/** Phase 3 — `AliasDealiaser`.
  *
  * Walks every `TypeDef.Alias` in `ResolvedDomain.userTypes`, chases the
  * `.target` chain to a non-alias `TypeId`, and stores the result in
  * `ResolvedDomain.aliases`. Cycles emit a `Diagnostic.CyclicAlias` and the
  * involved aliases are omitted from the `aliases` map.
  *
  * Replaces `TypespaceImpl.dealias` (legacy `TypespaceImpl.scala:41-49`) plus
  * alias-chasing in `IDLPostTyper.fixSimpleId` (legacy `IDLTyper.scala:537-549`).
  *
  * Per C8/L1, never throws on cyclic input.
  */
object AliasDealiaser {

  def apply(resolved: ResolvedDomain): ResolvedDomain = {
    val aliases = resolved.userTypes.collect { case (id: AliasId, a: TypeDef.Alias) => id -> a }.toMap

    val resolvedTargets = mutable.LinkedHashMap.empty[AliasId, TypeId]
    val diagBuf         = mutable.ArrayBuffer.empty[Diagnostic]

    aliases.keys.foreach {
      start =>
        val visited = mutable.LinkedHashSet.empty[AliasId]
        var current: TypeId = start
        var continue = true
        while (continue) {
          current match {
            case a: AliasId if visited.contains(a) =>
              // Cycle: emit one diagnostic with the cycle members in visit order.
              val cycle = visited.toList.dropWhile(_ != a) :+ a
              diagBuf += Diagnostic.CyclicAlias(cycle, resolved.userTypes.get(a).map(_.meta.pos).getOrElse(izumi.idealingua.model.il.ast.InputPosition.Undefined))
              continue = false

            case a: AliasId =>
              val _ = visited.add(a)
              aliases.get(a) match {
                case Some(defn) =>
                  current = defn.target
                case None if a.path.domain != resolved.id =>
                  // Cross-domain alias: this domain's pipeline cannot see the
                  // foreign target. Record the boundary AliasId as the resolved
                  // value and let the family-level finalizer
                  // (`NewTyperPipeline.finalizeCrossDomainAliases`) chase it
                  // through every other domain's `aliases` map. No diagnostic —
                  // a missing FOREIGN AliasId at this point would already have
                  // surfaced as `UnknownTypeRef` in Phase 2.
                  resolvedTargets.update(start, a)
                  continue = false
                case None =>
                  diagBuf += Diagnostic.AliasTargetUnresolved(a, resolved.userTypes.get(start).map(_.meta.pos).getOrElse(izumi.idealingua.model.il.ast.InputPosition.Undefined))
                  continue = false
              }

            case nonAlias =>
              resolvedTargets.update(start, nonAlias)
              continue = false
          }
        }
    }

    resolved.copy(
      aliases     = resolvedTargets.toMap,
      diagnostics = resolved.diagnostics ++ Diagnostics(diagBuf.toVector),
    )
  }
}
