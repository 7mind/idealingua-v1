package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{Applicative2, F}
import izumi.idealingua.runtime.rpc.IRTMethodId
import org.http4s.Headers

import java.net.InetAddress

abstract class IRTAuthenticator[F[_, _], AuthCtx, RequestCtx] {
  def authenticate(authContext: AuthCtx, body: Option[Json], methodId: Option[IRTMethodId]): F[Nothing, Option[RequestCtx]]
}

object IRTAuthenticator {
  def unit[F[+_, +_]: Applicative2, C]: IRTAuthenticator[F, C, Unit] = new IRTAuthenticator[F, C, Unit] {
    override def authenticate(authContext: C, body: Option[Json], methodId: Option[IRTMethodId]): F[Nothing, Option[Unit]] = F.pure(Some(()))
  }
  final case class AuthContext(headers: Headers, networkAddress: Option[InetAddress])
}
