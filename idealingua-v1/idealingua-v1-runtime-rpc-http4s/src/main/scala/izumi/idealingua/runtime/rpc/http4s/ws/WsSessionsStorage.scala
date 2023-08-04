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

    protected val dispatchers = new ConcurrentHashMap[WsSessionId, WsClientDispatcher[F, RequestContext, ClientId]]()
    protected val sessions    = new ConcurrentHashMap[WsSessionId, WsClientSession[F, RequestContext, ClientId]]()

    override def addClient(ctx: WsClientSession[F, RequestContext, ClientId]): F[Throwable, WsClientSession[F, RequestContext, ClientId]] = {
      for {
        _ <- logger.debug(s"Adding a client with session - ${ctx.id}")
        _ <- F.sync(sessions.put(ctx.id.sessionId, ctx))
      } yield ctx
    }

    override def deleteClient(id: WsSessionId): F[Throwable, Option[WsClientSession[F, RequestContext, ClientId]]] = {
      for {
        _ <- logger.debug(s"Deleting a client with session - $id")
        res <- F.sync {
          val ctx = Option(sessions.remove(id))
          dispatchers.remove(id)
          ctx
        }
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
      Option(
        dispatchers.compute(
          id,
          {
            case (_, null)       => Option(sessions.get(id)).map(WsClientDispatcher(_, codec, logger, timeout)).orNull
            case (_, dispatcher) => dispatcher
          },
        )
      )
    }
  }

  final case class WsClientDispatcher[F[+_, +_]: IO2, RequestContext, ClientId](
    session: WsClientSession[F, RequestContext, ClientId],
    codec: IRTClientMultiplexor[F],
    logger: LogIO2[F],
    timeout: FiniteDuration,
  ) extends IRTDispatcher[F] {
    override def dispatch(request: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
      for {
        json <- codec.encode(request)
        id   <- session.request(request.method, json)
        response <- session.requestState.awaitResponse(id, timeout).guarantee {
          logger.debug(s"WS Session: ${request.method -> "method"}, ${id -> "id"}: cleaning request state.") *>
          F.sync(session.requestState.forget(id))
        }
        res <- response match {
          case Some(value: RawResponse.GoodRawResponse) =>
            logger.debug(s"WS Session: ${request.method -> "method"}, $id: Have response: $value.") *>
            codec.decode(value.data, value.method)

          case Some(value: RawResponse.BadRawResponse) =>
            logger.debug(s"WS Session: ${request.method -> "method"}, $id: Generic failure response: $value.") *>
            F.fail(new IRTGenericFailure(s"${request.method -> "method"}, $id: generic failure: $value"))

          case None =>
            logger.warn(s"WS Session: ${request.method -> "method"}, $id: Timeout exception $timeout.") *>
            F.fail(new TimeoutException(s"${request.method -> "method"}, $id: No response in $timeout"))
        }
      } yield res
    }
  }
}
