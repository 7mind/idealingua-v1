package izumi.idealingua.runtime.rpc.http4s

import izumi.functional.bio.{Applicative2, F}
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import org.http4s.Headers

import java.net.InetAddress

abstract class IRTAuthenticator[F[+_, +_], RequestCtx] {
  def authenticate(request: AuthContext): F[Throwable, Option[RequestCtx]]
}

object IRTAuthenticator {
  def unit[F[+_, +_]: Applicative2]: IRTAuthenticator[F, Unit] = new IRTAuthenticator[F, Unit] {
    override def authenticate(request: AuthContext): F[Throwable, Option[Unit]] = F.pure(Some(()))
  }
  final case class AuthContext(headers: Headers, networkAddress: Option[InetAddress])
}
