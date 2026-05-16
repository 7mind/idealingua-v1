package mcpdemo

import izumi.functional.bio.{Error2, F}

/** Deterministic in-memory implementation of `ShapesServer` for the
  * `McpBridgeRealServerSpec` end-to-end test. Every method is pure — no
  * side effects, no clock, no random source — so the assertion JSON in
  * the spec can be inlined as exact expected bytes.
  *
  * Generic over the IRT bifunctor `F[+_, +_]: Error2` and a context
  * type `C` (the spec uses `Unit` to match `McpBridgeRoundtripSpec`'s
  * wiring).
  */
final class ShapesServerImpl[F[+_, +_]: Error2, C] extends ShapesServer[F, C] {

  override def ping(ctx: C): Just[Shapes.ping.Output] =
    F.pure(Shapes.PingOutput())

  override def upper(ctx: C, s: String): Just[String] =
    F.pure(s.toUpperCase)

  override def add(ctx: C, a: Long, b: Long): Just[Long] =
    F.pure(a + b)

  override def echo(ctx: C, req: EchoRequest): Just[EchoResponse] =
    F.pure(EchoResponse(echo = req.msg, count = req.n))

  override def divmod(ctx: C, a: Long, b: Long): Just[Shapes.divmod.Output] =
    F.pure(Shapes.DivmodOutput(quotient = a / b, remainder = a % b))

  override def reverse(ctx: C, items: List[String]): Just[List[String]] =
    F.pure(items.reverse)

  override def invertMap(ctx: C, m: Map[String, String]): Just[Map[String, String]] =
    F.pure(m.map { case (k, v) => v -> k })

  override def maybeUpper(ctx: C, s: Option[String]): Just[Option[String]] =
    F.pure(s.map(_.toUpperCase))

  override def nextColor(ctx: C, c: Color): Just[ColorResult] = F.pure {
    val next = c match {
      case Color.Red   => Color.Green
      case Color.Green => Color.Blue
      case Color.Blue  => Color.Red
    }
    ColorResult(color = next)
  }

  override def makeProfile(ctx: C, name: String, age: Int, color: Color): Just[Profile] =
    F.pure(Profile(name = name, age = age, color = color))

  override def pay(ctx: C, amount: Long): Just[PaymentResult] = F.pure {
    if (amount > 0) PaymentResult.PaymentOk(PaymentOk(txId = s"tx-$amount", amount = amount))
    else PaymentResult.PaymentRejected(PaymentRejected(reason = "non-positive amount", code = 422))
  }

  override def divideSafe(ctx: C, a: Long, b: Long): F[ServiceError, Long] = {
    if (b == 0L) F.fail(ServiceError(code = 422, message = "division by zero"))
    else F.pure(a / b)
  }

  override def noteValue(ctx: C, v: Int): F[ServiceError, Shapes.NoteValueSuccess] = {
    if (v < 0) F.fail(ServiceError(code = 400, message = "negative"))
    else F.pure(Shapes.NoteValueSuccess())
  }
}
