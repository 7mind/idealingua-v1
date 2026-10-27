package izumi.idealingua.typer.phase

import izumi.idealingua.model.common.{DomainId, TypeId}
import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.typer.ir.{FamilyIndex, ResolvedDomain, TypeDef}

import scala.annotation.tailrec
import scala.collection.mutable

/** One-hop alias lookup: the immediate target of an alias, resolved in the
  * scope of the domain that declares the alias.
  *
  * The per-domain phases see only the current domain's definitions, so a
  * reference to an alias declared in another domain (`otherDomain#Ref`) could
  * not be dealiased before the family-level finalizer
  * (`NewTyperPipeline.finalizeCrossDomainAliases`) ran, and was rejected as an
  * ADT branch or mixin target.
  */
trait AliasTargets {
  def target(alias: AliasId): Option[TypeId]

  /** Follows alias hops; stops at the first non-alias, at an alias whose
    * target is unknown, or at an alias that is revisited (a cycle, which
    * `AliasDealiaser` reports).
    */
  final def dealias(id: TypeId): TypeId = chase(id, Set.empty)

  @tailrec
  private def chase(id: TypeId, visited: Set[AliasId]): TypeId = id match {
    case a: AliasId if !visited.contains(a) =>
      target(a) match {
        case Some(next) => chase(next, visited + a)
        case None       => a
      }
    case other => other
  }
}

object AliasTargets {

  /** Aliases of every domain in the family. Each domain's alias targets are
    * resolved on first use with that domain's own scope.
    */
  final class Family(family: FamilyIndex) extends AliasTargets {
    private val perDomain = mutable.HashMap.empty[DomainId, Map[AliasId, TypeId]]

    override def target(alias: AliasId): Option[TypeId] =
      perDomain.getOrElseUpdate(alias.path.domain, targetsOf(alias.path.domain)).get(alias)

    private def targetsOf(domainId: DomainId): Map[AliasId, TypeId] =
      family.domains.get(domainId) match {
        case Some(domain) => NameResolver.aliasTargets(ScopeBuilder(domainId, domain, family), family, this)
        case None         => Map.empty
      }
  }

  /** Aliases declared in a single resolved domain. */
  final class Local(resolved: ResolvedDomain) extends AliasTargets {
    private val targets: Map[TypeId, TypeId] = resolved.userTypes.collect { case (id, a: TypeDef.Alias) => id -> a.target }

    override def target(alias: AliasId): Option[TypeId] = targets.get(alias)
  }
}
