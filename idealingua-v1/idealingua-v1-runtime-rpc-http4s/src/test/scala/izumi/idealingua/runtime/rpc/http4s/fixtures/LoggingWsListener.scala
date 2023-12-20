package izumi.idealingua.runtime.rpc.http4s.fixtures

import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.http4s.ws.{WsSessionId, WsSessionListener}

import scala.collection.mutable

final class LoggingWsListener[F[+_, +_]: IO2, RequestCtx, WsCtx] extends WsSessionListener[F, RequestCtx, WsCtx] {
  private val connections                  = mutable.Set.empty[(WsSessionId, WsCtx)]
  def connected: Set[(WsSessionId, WsCtx)] = connections.toSet
  def connectedContexts: Set[WsCtx]        = connections.map(_._2).toSet

  override def onSessionOpened(sessionId: WsSessionId, reqCtx: RequestCtx, wsCtx: WsCtx): F[Throwable, Unit] = F.sync {
    connections.add(sessionId -> wsCtx)
  }.void

  override def onSessionUpdated(sessionId: WsSessionId, reqCtx: RequestCtx, prevStx: WsCtx, newCtx: WsCtx): F[Throwable, Unit] = F.sync {
    connections.remove(sessionId -> prevStx)
    connections.add(sessionId    -> newCtx)
  }.void

  override def onSessionClosed(sessionId: WsSessionId, wsCtx: WsCtx): F[Throwable, Unit] = F.sync {
    connections.remove(sessionId -> wsCtx)
  }.void
}
