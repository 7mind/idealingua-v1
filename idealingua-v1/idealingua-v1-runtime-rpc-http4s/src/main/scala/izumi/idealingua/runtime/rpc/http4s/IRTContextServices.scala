package izumi.idealingua.runtime.rpc.http4s

import izumi.functional.bio.{IO2, Monad2}
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextSessions
import izumi.idealingua.runtime.rpc.{IRTServerMiddleware, IRTServerMultiplexor}

final case class IRTContextServices[F[+_, +_], AuthCtx, RequestCtx, WsCtx](
  authenticator: IRTAuthenticator[F, AuthCtx, RequestCtx],
  serverMuxer: IRTServerMultiplexor[F, RequestCtx],
  middlewares: Set[IRTServerMiddleware[F, RequestCtx]],
  wsSessions: WsContextSessions[F, RequestCtx, WsCtx],
) {
  def authorizedMuxer(implicit io2: IO2[F]): IRTServerMultiplexor[F, AuthCtx] = {
    val withMiddlewares: IRTServerMultiplexor[F, RequestCtx] = middlewares.toList.sortBy(_.priority).foldLeft(serverMuxer) {
      case (muxer, middleware) => muxer.wrap(middleware)
    }
    val authorized: IRTServerMultiplexor[F, AuthCtx] = withMiddlewares.contramap {
      case (authCtx, body) => authenticator.authenticate(authCtx, Some(body))
    }
    authorized
  }
  def authorizedWsSessions(implicit M: Monad2[F]): WsContextSessions[F, AuthCtx, WsCtx] = {
    val authorized: WsContextSessions[F, AuthCtx, WsCtx] = wsSessions.contramap {
      authCtx =>
        authenticator.authenticate(authCtx, None)
    }
    authorized
  }
}
