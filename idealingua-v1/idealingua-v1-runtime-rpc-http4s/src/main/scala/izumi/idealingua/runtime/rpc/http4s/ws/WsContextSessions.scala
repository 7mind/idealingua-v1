package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{F, IO2, Monad2}
import izumi.idealingua.runtime.rpc.http4s.context.WsIdExtractor

trait WsContextSessions[F[+_, +_], RequestCtx, WsCtx] {
  self =>
  /** Updates session context if can be updated and sends callbacks to Set[WsSessionListener] if context were changed. */
  def updateSession(wsSessionId: WsSessionId, requestContext: Option[RequestCtx]): F[Throwable, Unit]
  /** Contramap with F over context type. Useful for authentications and context extensions. */
  final def contramap[C](updateCtx: C => F[Throwable, Option[RequestCtx]])(implicit M: Monad2[F]): WsContextSessions[F, C, WsCtx] = new WsContextSessions[F, C, WsCtx] {
    override def updateSession(wsSessionId: WsSessionId, requestContext: Option[C]): F[Throwable, Unit] = {
      F.traverse(requestContext)(updateCtx).flatMap(mbCtx => self.updateSession(wsSessionId, mbCtx.flatten))
    }
  }
}

object WsContextSessions {
  type AnyContext[F[+_, +_], RequestCtx] = WsContextSessions[F, RequestCtx, ?]

  def empty[F[+_, +_]: IO2, RequestCtx]: WsContextSessions[F, RequestCtx, Unit] = new WsContextSessions[F, RequestCtx, Unit] {
    override def updateSession(wsSessionId: WsSessionId, requestContext: Option[RequestCtx]): F[Throwable, Unit] = F.unit
  }

  class WsContextSessionsImpl[F[+_, +_]: IO2, RequestCtx, WsCtx](
    wsContextStorage: WsContextStorage[F, WsCtx],
    globalWsListeners: Set[WsSessionListener.Global[F]],
    wsSessionListeners: Set[WsSessionListener[F, RequestCtx, WsCtx]],
    wsIdExtractor: WsIdExtractor[RequestCtx, WsCtx],
  ) extends WsContextSessions[F, RequestCtx, WsCtx] {
    override def updateSession(wsSessionId: WsSessionId, requestContext: Option[RequestCtx]): F[Throwable, Unit] = {
      for {
        ctxUpdate <- wsContextStorage.updateContext(wsSessionId) {
          mbPrevCtx =>
            requestContext.flatMap(wsIdExtractor.extract(_, mbPrevCtx))
        }
        _ <- (requestContext, ctxUpdate.previous, ctxUpdate.updated) match {
          case (Some(ctx), Some(previous), Some(updated)) if previous != updated =>
            F.traverse_(wsSessionListeners)(_.onSessionUpdated(wsSessionId, ctx, previous, updated)) *>
            F.traverse_(globalWsListeners)(_.onSessionUpdated(wsSessionId, ctx, previous, updated))
          case (Some(ctx), None, Some(updated)) =>
            F.traverse_(wsSessionListeners)(_.onSessionOpened(wsSessionId, ctx, updated)) *>
            F.traverse_(globalWsListeners)(_.onSessionOpened(wsSessionId, ctx, updated))
          case (_, Some(prev), None) =>
            F.traverse_(wsSessionListeners)(_.onSessionClosed(wsSessionId, prev)) *>
            F.traverse_(globalWsListeners)(_.onSessionClosed(wsSessionId, prev))
          case _ =>
            F.unit
        }
      } yield ()
    }
  }
}
