package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{Applicative2, F}

trait WsSessionListener[F[+_, +_], RequestCtx] {
  def onSessionOpened(sessionId: WsSessionId, context: Option[RequestCtx]): F[Throwable, Unit]
  def onSessionAuthUpdate(sessionId: WsSessionId, context: Option[RequestCtx]): F[Throwable, Unit]
  def onSessionClosed(sessionId: WsSessionId, context: Option[RequestCtx]): F[Throwable, Unit]
}

object WsSessionListener {
  def empty[F[+_, +_]: Applicative2, RequestCtx]: WsSessionListener[F, RequestCtx] = new WsSessionListener[F, RequestCtx] {
    override def onSessionOpened(sessionId: WsSessionId, context: Option[RequestCtx]): F[Throwable, Unit]     = F.unit
    override def onSessionAuthUpdate(sessionId: WsSessionId, context: Option[RequestCtx]): F[Throwable, Unit] = F.unit
    override def onSessionClosed(sessionId: WsSessionId, context: Option[RequestCtx]): F[Throwable, Unit]     = F.unit
  }
}
