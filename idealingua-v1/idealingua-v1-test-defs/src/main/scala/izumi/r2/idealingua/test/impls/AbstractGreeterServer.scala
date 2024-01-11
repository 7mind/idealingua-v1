package izumi.r2.idealingua.test.impls

import izumi.functional.bio.{F, IO2}
import izumi.r2.idealingua.test.generated.*

abstract class AbstractGreeterServer[F[+_, +_]: IO2, C] extends GreeterServiceServer[F, C] {
  override def greet(ctx: C, name: String, surname: String): Just[String] = F.pure(s"Hi, $name $surname!")
  override def sayhi(ctx: C): Just[String] = F.pure(s"Hi! With $ctx.")
  override def alternative(ctx: C): F[Long, String] = F.fromEither(Right("value"))
  override def nothing(ctx: C): F[Nothing, String] = F.pure("")
}

object AbstractGreeterServer {
  class Impl[F[+_, +_]: IO2, C] extends AbstractGreeterServer[F, C]
}
