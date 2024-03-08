package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextStorage.{WsContextSessionId, WsCtxUpdate}
import izumi.idealingua.runtime.rpc.{IRTClientMultiplexor, IRTDispatcher}

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters.*

/** Sessions storage based on WS context.
  * Supports [one session - one context] and [one context - many sessions mapping]
  * It is possible to support [one sessions - many contexts] mapping (for generic context storages),
  * but in such case we would able to choose one from many to update session context data.
  */
trait WsContextStorage[F[+_, +_], WsCtx] {
  def getContext(wsSessionId: WsSessionId): F[Throwable, Option[WsCtx]]
  def allSessions(): F[Throwable, Set[WsContextSessionId[WsCtx]]]
  /** Updates session context using [updateCtx] function (maybeOldContext => maybeNewContext) */
  def updateContext(wsSessionId: WsSessionId)(updateCtx: Option[WsCtx] => Option[WsCtx]): F[Throwable, WsCtxUpdate[WsCtx]]

  def getSessions(ctx: WsCtx): F[Throwable, List[WsClientSession[F, ?]]]
  def dispatchersFor(ctx: WsCtx, codec: IRTClientMultiplexor[F], timeout: FiniteDuration = 20.seconds): F[Throwable, List[IRTDispatcher[F]]]
}

object WsContextStorage {
  final case class WsCtxUpdate[WsCtx](previous: Option[WsCtx], updated: Option[WsCtx])
  final case class WsContextSessionId[WsCtx](sessionId: WsSessionId, ctx: WsCtx)

  class WsContextStorageImpl[F[+_, +_]: IO2, WsCtx](
    wsSessionsStorage: WsSessionsStorage[F, ?]
  ) extends WsContextStorage[F, WsCtx] {
    private val sessionToId  = new ConcurrentHashMap[WsSessionId, WsCtx]()
    private val idToSessions = new ConcurrentHashMap[WsCtx, Set[WsSessionId]]()

    override def allSessions(): F[Throwable, Set[WsContextSessionId[WsCtx]]] = F.sync {
      sessionToId.asScala.map { case (s, c) => WsContextSessionId(s, c) }.toSet
    }

    override def getContext(wsSessionId: WsSessionId): F[Throwable, Option[WsCtx]] = F.sync {
      Option(sessionToId.get(wsSessionId))
    }

    override def updateContext(wsSessionId: WsSessionId)(updateCtx: Option[WsCtx] => Option[WsCtx]): F[Nothing, WsCtxUpdate[WsCtx]] = {
      updateCtxImpl(wsSessionId)(updateCtx)
    }

    override def getSessions(ctx: WsCtx): F[Throwable, List[WsClientSession[F, ?]]] = {
      F.sync(synchronized(Option(idToSessions.get(ctx)).getOrElse(Set.empty).toList)).flatMap {
        sessions =>
          F.traverse[Throwable, WsSessionId, Option[WsClientSession[F, ?]]](sessions) {
            wsSessionId => wsSessionsStorage.getSession(wsSessionId)
          }.map(_.flatten)
      }
    }

    override def dispatchersFor(ctx: WsCtx, codec: IRTClientMultiplexor[F], timeout: FiniteDuration): F[Throwable, List[IRTDispatcher[F]]] = {
      F.sync(synchronized(Option(idToSessions.get(ctx)).getOrElse(Set.empty)).toList).flatMap {
        sessions =>
          F.traverse[Throwable, WsSessionId, Option[IRTDispatcher[F]]](sessions) {
            wsSessionId => wsSessionsStorage.dispatcherForSession(wsSessionId, codec, timeout)
          }.map(_.flatten)
      }
    }

    @inline private final def updateCtxImpl(
      wsSessionId: WsSessionId
    )(updateCtx: Option[WsCtx] => Option[WsCtx]
    ): F[Nothing, WsCtxUpdate[WsCtx]] = F.sync {
      synchronized {
        val mbPrevCtx = Option(sessionToId.get(wsSessionId))
        val mbNewCtx  = updateCtx(mbPrevCtx)
        (mbNewCtx, mbPrevCtx) match {
          case (Some(updCtx), mbPrevCtx) =>
            mbPrevCtx.foreach(removeSessionFromCtx(_, wsSessionId))
            sessionToId.put(wsSessionId, updCtx)
            addSessionToCtx(updCtx, wsSessionId)
            ()
          case (None, Some(prevCtx)) =>
            sessionToId.remove(wsSessionId)
            removeSessionFromCtx(prevCtx, wsSessionId)
            ()
          case _ =>
            ()
        }
        WsCtxUpdate(mbPrevCtx, mbNewCtx)
      }
    }

    @inline private final def addSessionToCtx(wsCtx: WsCtx, wsSessionId: WsSessionId): Unit = {
      idToSessions.compute(
        wsCtx,
        {
          case (_, null) => Set(wsSessionId)
          case (_, s)    => s + wsSessionId
        },
      )
      ()
    }

    @inline private final def removeSessionFromCtx(wsCtx: WsCtx, wsSessionId: WsSessionId): Unit = {
      idToSessions.compute(
        wsCtx,
        {
          case (_, null) => null
          case (_, s)    => Option(s - wsSessionId).filter(_.nonEmpty).orNull
        },
      )
      ()
    }
  }
}
