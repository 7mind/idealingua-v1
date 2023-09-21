package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.*
import logstage.LogIO2

import java.util.concurrent.{ConcurrentHashMap, TimeoutException}
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

trait WsSessionsStorage[F[+_, +_], RequestCtx, ClientId] {
  def addClient(ctx: WsClientSession[F, RequestCtx, ClientId]): F[Throwable, WsClientSession[F, RequestCtx, ClientId]]
  def deleteClient(id: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestCtx, ClientId]]]
  def allClients(): F[Throwable, Seq[WsClientSession[F, RequestCtx, ClientId]]]

  def dispatcherForSession(id: WsSessionId, timeout: FiniteDuration = 20.seconds): F[Throwable, Option[IRTDispatcher[F]]]
  def dispatcherForClient(id: ClientId, timeout: FiniteDuration     = 20.seconds): F[Throwable, Option[IRTDispatcher[F]]]
}

object WsSessionsStorage {

  class WsSessionsStorageImpl[F[+_, +_]: IO2, RequestContext, ClientId](
    logger: LogIO2[F],
    codec: IRTClientMultiplexor[F],
  ) extends WsSessionsStorage[F, RequestContext, ClientId] {

    protected val sessions = new ConcurrentHashMap[WsSessionId, WsClientSession[F, RequestContext, ClientId]]()

    override def addClient(ctx: WsClientSession[F, RequestContext, ClientId]): F[Throwable, WsClientSession[F, RequestContext, ClientId]] = {
      for {
        _ <- logger.debug(s"Adding a client with session - ${ctx.id}")
        _ <- F.sync(sessions.put(ctx.id.sessionId, ctx))
      } yield ctx
    }

    override def deleteClient(id: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestContext, ClientId]]] = {
      for {
        _   <- logger.debug(s"Deleting a client with session - $id")
        res <- F.sync(Option(sessions.remove(id)))
      } yield res
    }

    override def allClients(): F[Throwable, Seq[WsClientSession[F, RequestContext, ClientId]]] = F.sync {
      sessions.values().asScala.toSeq
    }

    override def dispatcherForClient(clientId: ClientId, timeout: FiniteDuration): F[Throwable, Option[WsClientDispatcher[F, RequestContext, ClientId]]] = {
      F.sync(sessions.values().asScala.find(_.id.id.contains(clientId))).flatMap {
        F.traverse(_) {
          session =>
            dispatcherForSession(session.id.sessionId, timeout)
        }.map(_.flatten)
      }
    }

    override def dispatcherForSession(id: WsSessionId, timeout: FiniteDuration): F[Throwable, Option[WsClientDispatcher[F, RequestContext, ClientId]]] = F.sync {
      Option(sessions.get(id)).map(new WsClientDispatcher(_, codec, logger, timeout))
    }
  }

  class WsClientDispatcher[F[+_, +_]: IO2, RequestContext, ClientId](
    session: WsClientSession[F, RequestContext, ClientId],
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
            F.fail(new IRTGenericFailure(s"${request.method -> "method"}: empty response: $value"))

          case Some(value: RawResponse.GoodRawResponse) =>
            logger.debug(s"WS Session: ${request.method -> "method"}: Have response: $value.") *>
            codec.decode(value.data, value.method)

          case Some(value: RawResponse.BadRawResponse) =>
            logger.debug(s"WS Session: ${request.method -> "method"}: Generic failure response: ${value.error}.") *>
            F.fail(new IRTGenericFailure(s"${request.method -> "method"}: generic failure: ${value.error}"))

          case None =>
            logger.warn(s"WS Session: ${request.method -> "method"}: Timeout exception $timeout.") *>
            F.fail(new TimeoutException(s"${request.method -> "method"}: No response in $timeout"))
        }
      } yield res
    }
  }


}
