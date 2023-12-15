package izumi.idealingua.runtime.rpc.http4s.fixtures

import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.http4s.ws.{WsSessionId, WsSessionListener}

import scala.collection.mutable

final class LoggingWsListener[F[+_, +_]: IO2, RequestCtx, WsCtx] extends WsSessionListener[F, RequestCtx, WsCtx] {
  private val connections   = mutable.Set.empty[WsCtx]
  def connected: Set[WsCtx] = connections.toSet

  override def onSessionOpened(sessionId: WsSessionId, reqCtx: RequestCtx, wsCtx: WsCtx): F[Throwable, Unit] = F.sync {
    connections.add(wsCtx)
  }.void

  override def onSessionUpdated(sessionId: WsSessionId, reqCtx: RequestCtx, prevStx: WsCtx, newCtx: WsCtx): F[Throwable, Unit] = F.sync {
    connections.remove(prevStx)
    connections.add(newCtx)
  }.void

  override def onSessionClosed(sessionId: WsSessionId, wsCtx: WsCtx): F[Throwable, Unit] = F.sync {
    connections.remove(wsCtx)
  }.void
}
