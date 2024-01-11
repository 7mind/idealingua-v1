package izumi.idealingua.runtime.rpc

import io.circe.Json

trait IRTServerMiddleware[F[_, _], C] {
  def priority: Int
  def apply(
    methodId: IRTMethodId
  )(context: C,
    parsedBody: Json,
  )(next: => F[Throwable, Json]
  ): F[Throwable, Json]
}
