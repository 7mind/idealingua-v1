package izumi.idealingua.runtime.rpc

import io.circe.Json

trait IRTServerMiddleware[F[_, _], C] {
  def priority: Int
  def prepare(methodId: IRTMethodId)(context: C, parsedBody: Json): F[Throwable, Unit]
}
