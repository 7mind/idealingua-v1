package izumi.idealingua.translator.toscala.domain

/** Aggregate context surfacing the per-service `DomainServiceContext` plus
  * the per-method `DomainServiceMethodProduct`s, mirroring legacy
  * `types.FullServiceContext`.
  *
  * IMPL-7a.2 Phase B M4: kept as a value-only twin so any future
  * extension hook that consumes a `FullServiceContext`-shaped value can be
  * adapted to consume this twin without re-running the per-method
  * computation.
  */
final case class DomainFullServiceContext(
  service: DomainServiceContext,
  methods: List[DomainServiceMethodProduct],
)
