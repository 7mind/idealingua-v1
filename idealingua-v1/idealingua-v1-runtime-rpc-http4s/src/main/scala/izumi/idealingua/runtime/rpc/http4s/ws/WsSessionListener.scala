package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{Applicative2, F}

trait WsSessionListener[F[_, _], RequestCtx, WsCtx] {
  def onSessionOpened(sessionId: WsSessionId, reqCtx: RequestCtx, wsCtx: WsCtx): F[Throwable, Unit]
  def onSessionUpdated(sessionId: WsSessionId, reqCtx: RequestCtx, prevStx: WsCtx, newCtx: WsCtx): F[Throwable, Unit]
  def onSessionClosed(sessionId: WsSessionId, wsCtx: WsCtx): F[Throwable, Unit]
}

object WsSessionListener {
  def empty[F[+_, +_]: Applicative2, RequestCtx, WsCtx]: WsSessionListener[F, RequestCtx, WsCtx] = new WsSessionListener[F, RequestCtx, WsCtx] {
    override def onSessionOpened(sessionId: WsSessionId, reqCtx: RequestCtx, wsCtx: WsCtx): F[Throwable, Unit]                   = F.unit
    override def onSessionUpdated(sessionId: WsSessionId, reqCtx: RequestCtx, prevStx: WsCtx, newCtx: WsCtx): F[Throwable, Unit] = F.unit
    override def onSessionClosed(sessionId: WsSessionId, wsCtx: WsCtx): F[Throwable, Unit]                                       = F.unit
  }
}
