package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{Applicative2, F}
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import org.http4s.Headers

import java.net.InetAddress

abstract class IRTAuthenticator[F[_, _], RequestCtx] {
  def authenticate(authContext: AuthContext, body: Option[Json]): F[Nothing, Option[RequestCtx]]
}

object IRTAuthenticator {
  def unit[F[+_, +_]: Applicative2]: IRTAuthenticator[F, Unit] = new IRTAuthenticator[F, Unit] {
    override def authenticate(authContext: AuthContext, body: Option[Json]): F[Nothing, Option[Unit]] = F.pure(Some(()))
  }
  final case class AuthContext(headers: Headers, networkAddress: Option[InetAddress])
}
