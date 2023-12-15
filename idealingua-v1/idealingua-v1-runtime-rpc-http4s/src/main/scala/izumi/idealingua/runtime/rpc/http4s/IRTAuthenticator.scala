package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{Applicative2, F}
import org.http4s.Headers

import java.net.InetAddress

abstract class IRTAuthenticator[F[_, _], AuthCtx, RequestCtx] {
  def authenticate(authContext: AuthCtx, body: Option[Json]): F[Nothing, Option[RequestCtx]]
}

object IRTAuthenticator {
  def unit[F[+_, +_]: Applicative2, C]: IRTAuthenticator[F, C, Unit] = new IRTAuthenticator[F, C, Unit] {
    override def authenticate(authContext: C, body: Option[Json]): F[Nothing, Option[Unit]] = F.pure(Some(()))
  }
  final case class AuthContext(headers: Headers, networkAddress: Option[InetAddress])
}
