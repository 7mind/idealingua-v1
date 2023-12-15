package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.*
import logstage.LogIO2

import java.util.concurrent.{ConcurrentHashMap, TimeoutException}
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

trait WsSessionsStorage[F[+_, +_], RequestCtx] {
  def addSession(session: WsClientSession[F, RequestCtx]): F[Throwable, WsClientSession[F, RequestCtx]]
  def deleteSession(sessionId: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestCtx]]]
  def allSessions(): F[Throwable, Seq[WsClientSession[F, RequestCtx]]]
  def getSession(sessionId: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestCtx]]]

  def dispatcherForSession(
    sessionId: WsSessionId,
    codec: IRTClientMultiplexor[F],
    timeout: FiniteDuration = 20.seconds,
  ): F[Throwable, Option[IRTDispatcher[F]]]
}

object WsSessionsStorage {

  class WsSessionsStorageImpl[F[+_, +_]: IO2, RequestCtx](logger: LogIO2[F]) extends WsSessionsStorage[F, RequestCtx] {
    protected val sessions = new ConcurrentHashMap[WsSessionId, WsClientSession[F, RequestCtx]]()

    override def addSession(session: WsClientSession[F, RequestCtx]): F[Throwable, WsClientSession[F, RequestCtx]] = {
      for {
        _ <- logger.debug(s"Adding a client with session - ${session.sessionId}")
        _ <- F.sync(sessions.put(session.sessionId, session))
      } yield session
    }

    override def deleteSession(sessionId: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestCtx]]] = {
      for {
        _   <- logger.debug(s"Deleting a client with session - $sessionId")
        res <- F.sync(Option(sessions.remove(sessionId)))
      } yield res
    }

    override def getSession(sessionId: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestCtx]]] = {
      F.sync(Option(sessions.get(sessionId)))
    }

    override def allSessions(): F[Throwable, Seq[WsClientSession[F, RequestCtx]]] = F.sync {
      sessions.values().asScala.toSeq
    }

    override def dispatcherForSession(
      sessionId: WsSessionId,
      codec: IRTClientMultiplexor[F],
      timeout: FiniteDuration,
    ): F[Throwable, Option[WsClientDispatcher[F, RequestCtx]]] = F.sync {
      Option(sessions.get(sessionId)).map(new WsClientDispatcher(_, codec, logger, timeout))
    }
  }

  class WsClientDispatcher[F[+_, +_]: IO2, RequestCtx](
    session: WsClientSession[F, RequestCtx],
    codec: IRTClientMultiplexor[F],
    logger: LogIO2[F],
    timeout: FiniteDuration,
  ) extends IRTDispatcher[F] {
    override def dispatch(request: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
      for {
        json     <- codec.encode(request)
        response <- session.requestAndAwaitResponse(request.method, json, timeout)
        res <- response match {
          case Some(value: RawResponse.EmptyRawResponse) =>
            F.fail(new IRTGenericFailure(s"${request.method}: empty response: $value"))

          case Some(value: RawResponse.GoodRawResponse) =>
            logger.debug(s"WS Session: ${request.method -> "method"}: Have response: $value.") *>
            codec.decode(value.data, value.method)

          case Some(value: RawResponse.BadRawResponse) =>
            logger.debug(s"WS Session: ${request.method -> "method"}: Generic failure response: ${value.error}.") *>
            F.fail(new IRTGenericFailure(s"${request.method}: generic failure: ${value.error}"))

          case None =>
            logger.warn(s"WS Session: ${request.method -> "method"}: Timeout exception $timeout.") *>
            F.fail(new TimeoutException(s"${request.method}: No response in $timeout"))
        }
      } yield res
    }
  }

}
