package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{Applicative2, F}

trait IRTOutputMiddleware[F[_, _], C] {
  def apply(
    methodId: IRTMethodId
  )(context: C,
    response: IRTResBody,
    encodedResponse: Json,
  ): F[Throwable, Json]
}

object IRTOutputMiddleware {
  def empty[F[+_, +_]: Applicative2, C] = new Empty[F, C]

  final class Empty[F[+_, +_]: Applicative2, C] extends IRTOutputMiddleware[F, C] {
    override def apply(methodId: IRTMethodId)(context: C, response: IRTResBody, encodedResponse: Json): F[Throwable, Json] = {
      F.pure(encodedResponse)
    }
  }
}
