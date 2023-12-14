package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.{IRTClientMultiplexor, IRTDispatcher}

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait WsSessionsContext[F[+_, +_], RequestCtx, WsCtx] {
  def updateSession(wsSessionId: WsSessionId, requestContext: Option[RequestCtx]): F[Throwable, Unit]
  def dispatcherFor(
    ctx: WsCtx,
    codec: IRTClientMultiplexor[F],
    timeout: FiniteDuration = 20.seconds,
  ): F[Throwable, Option[IRTDispatcher[F]]]
}

object WsSessionsContext {
  def empty[F[+_, +_]: IO2, RequestCtx]: WsSessionsContext[F, RequestCtx, Unit] = new WsSessionsContext[F, RequestCtx, Unit] {
    override def updateSession(wsSessionId: WsSessionId, requestContext: Option[RequestCtx]): F[Throwable, Unit] = {
      F.unit
    }
    override def dispatcherFor(ctx: Unit, codec: IRTClientMultiplexor[F], timeout: FiniteDuration): F[Throwable, Option[IRTDispatcher[F]]] = {
      F.pure(None)
    }
  }

  class WsSessionsContextImpl[F[+_, +_]: IO2, RequestCtx, WsCtx](
    wsSessionsStorage: WsSessionsStorage[F],
    wsSessionListeners: Set[WsSessionListener[F, RequestCtx, WsCtx]],
    wsContextExtractor: WsContextExtractor[RequestCtx, WsCtx],
  ) extends WsSessionsContext[F, RequestCtx, WsCtx] {
    private val sessionToId = new ConcurrentHashMap[WsSessionId, WsCtx]()
    private val idToSession = new ConcurrentHashMap[WsCtx, WsSessionId]()

    override def updateSession(wsSessionId: WsSessionId, requestContext: Option[RequestCtx]): F[Throwable, Unit] = {
      updateCtx(wsSessionId, requestContext).flatMap {
        case (Some(ctx), Some(previous), Some(updated)) if previous != updated =>
          F.traverse_(wsSessionListeners)(_.onSessionUpdated(wsSessionId, ctx, previous, updated))
        case (Some(ctx), None, Some(updated)) =>
          F.traverse_(wsSessionListeners)(_.onSessionOpened(wsSessionId, ctx, updated))
        case (_, Some(prev), None) =>
          F.traverse_(wsSessionListeners)(_.onSessionClosed(wsSessionId, prev))
        case _ =>
          F.unit
      }
    }

    override def dispatcherFor(ctx: WsCtx, codec: IRTClientMultiplexor[F], timeout: FiniteDuration): F[Throwable, Option[IRTDispatcher[F]]] = {
      F.sync(Option(idToSession.get(ctx))).flatMap {
          F.traverse(_) {
            wsSessionsStorage.dispatcherForSession(_, codec, timeout)
          }
        }.map(_.flatten)
    }

    private def updateCtx(
      wsSessionId: WsSessionId,
      requestContext: Option[RequestCtx],
    ): F[Nothing, (Option[RequestCtx], Option[WsCtx], Option[WsCtx])] = F.sync {
      synchronized {
        val previous = Option(sessionToId.get(wsSessionId))
        val updated  = requestContext.flatMap(wsContextExtractor.extract)
        (updated, previous) match {
          case (Some(upd), _) =>
            sessionToId.put(wsSessionId, upd)
            idToSession.put(upd, wsSessionId)
            ()
          case (None, Some(prev)) =>
            sessionToId.remove(wsSessionId)
            idToSession.remove(prev)
            ()
          case _ =>
            ()
        }
        (requestContext, previous, updated)
      }
    }
  }
}
