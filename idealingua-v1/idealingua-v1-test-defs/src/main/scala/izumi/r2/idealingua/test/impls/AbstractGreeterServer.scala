package izumi.r2.idealingua.test.impls

import izumi.functional.bio.IO2
import izumi.r2.idealingua.test.generated._

abstract class AbstractGreeterServer[F[+_, +_]: IO2, C] extends GreeterServiceServer[F, C] {

  val R: IO2[F] = implicitly

  override def greet(ctx: C, name: String, surname: String): Just[String] = R.pure {
    s"Hi, $name $surname!"
  }

  override def sayhi(ctx: C): Just[String] = R.pure {
    "Hi!"
  }

  override def alternative(ctx: C): F[Long, String] = R.fromEither {
    Right("value")
  }

  override def nothing(ctx: C): F[Nothing, String] = R.pure {
    ""
  }
}

object AbstractGreeterServer {
  class Impl[F[+_, +_]: IO2, C] extends AbstractGreeterServer[F, C]
}
