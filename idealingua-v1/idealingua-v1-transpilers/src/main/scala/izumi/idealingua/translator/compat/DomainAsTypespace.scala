package izumi.idealingua.translator.compat

import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.typer.ir.{Domain => NewDomain}

/** `Typespace`-shaped façade carrying the new-typer `Domain` IR alongside a
  * legacy `Typespace` over the same domain.
  *
  * IMPL-6 strategy is **delegate-by-default**: the still-unported Scala / TS /
  * C# translators read 25+ methods across `structure`, `inheritance`,
  * `resolver`, `tools`, `types`, and a top-level `domain: DomainDefinition`.
  * Re-implementing every one of those queries against the new IR is wasted
  * effort because IMPL-7a/b/c will delete the call sites (each translator
  * gets ported to read `Domain` directly).
  *
  * Implementation note: the adapter extends `TypespaceImpl(legacy.domain)`
  * so every existing query inherits its current behaviour for free.  This is
  * cheaper than overriding ~25 trait members (most of which are
  * `protected[typespace]` and not accessible from this package).  The new
  * `Domain` is carried as a public `newDomain` field; tests and the
  * forthcoming IMPL-7a/b/c translator ports can read it directly.
  *
  * As IMPL-7a/b/c lands and each translator stops calling the legacy
  * `Typespace` queries in favour of `newDomain`, the inherited methods stop
  * being called.  At IMPL-11 the adapter (and this file) are deleted
  * outright per plan §2-P5.
  *
  * Field-ordering invariant (C12 / L3): this adapter does not re-sort any
  * field list.  Ordering is preserved by the underlying `TypespaceImpl`
  * (legacy) until IMPL-7+ replaces the read paths with direct iteration of
  * `newDomain`'s ordered collections.
  */
final class DomainAsTypespace(
  val newDomain: NewDomain,
  legacy: Typespace,
) extends TypespaceImpl(legacy.domain)
