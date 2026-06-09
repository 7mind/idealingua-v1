package mcpdemo

import izumi.functional.bio.{Error2, F}

/** Deterministic in-memory implementation of `CalcServer` for the
  * multi-service MCP-bridge test. Every method is pure arithmetic — no side
  * effects — so integration tests can use exact expected values.
  *
  * Generic over the IRT bifunctor `F[+_, +_]: Error2` and a context type `C`.
  */
final class CalcServerImpl[F[+_, +_]: Error2, C] extends CalcServer[F, C] {

  override def add(ctx: C, a: Long, b: Long): Just[Long] =
    F.pure(a + b)

  override def sub(ctx: C, a: Long, b: Long): Just[Long] =
    F.pure(a - b)

  override def mul(ctx: C, a: Long, b: Long): Just[Long] =
    F.pure(a * b)
}
