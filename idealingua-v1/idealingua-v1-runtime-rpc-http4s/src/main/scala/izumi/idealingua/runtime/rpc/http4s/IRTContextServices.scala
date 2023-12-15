package izumi.idealingua.runtime.rpc.http4s

import izumi.functional.bio.{Error2, Monad2}
import izumi.idealingua.runtime.rpc.IRTServerMultiplexor
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextSessions

final case class IRTContextServices[F[+_, +_], AuthCtx, RequestCtx, WsCtx](
  authenticator: IRTAuthenticator[F, AuthCtx, RequestCtx],
  serverMuxer: IRTServerMultiplexor[F, RequestCtx],
  wsSessions: WsContextSessions[F, RequestCtx, WsCtx],
) {
  def authorizedMuxer(implicit E: Error2[F]): IRTServerMultiplexor[F, AuthCtx] = serverMuxer.contramap {
    case (authCtx, body) =>
      authenticator.authenticate(authCtx, Some(body))
  }
  def authorizedWsSessions(implicit M: Monad2[F]): WsContextSessions[F, AuthCtx, WsCtx] = wsSessions.contramap {
    authCtx =>
      authenticator.authenticate(authCtx, None)
  }
}
