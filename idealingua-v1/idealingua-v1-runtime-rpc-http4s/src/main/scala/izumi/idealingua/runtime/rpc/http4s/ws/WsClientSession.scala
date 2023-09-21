package izumi.idealingua.runtime.rpc.http4s.ws

import cats.effect.std.Queue
import io.circe.syntax.*
import io.circe.{Json, Printer}
import izumi.functional.bio.{F, IO2, Primitives2, Temporal2}
import izumi.fundamentals.platform.time.IzTime
import izumi.fundamentals.platform.uuid.UUIDGen
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsMessageHandler.WsClientResponder
import logstage.LogIO2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

trait WsClientSession[F[+_, +_], RequestCtx, ClientId] extends WsClientResponder[F] {
  def id: WsClientId[ClientId]
  def initialContext: RequestCtx

  def updateId(maybeNewId: Option[ClientId]): F[Throwable, Unit]
  def outQueue: Queue[F[Throwable, _], WebSocketFrame]

  def request(method: IRTMethodId, data: Json): F[Throwable, RpcPacketId]
  def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]]

  def finish(): F[Throwable, Unit]
}

object WsClientSession {
  class WsClientSessionImpl[F[+_, +_]: IO2: Temporal2: Primitives2, RequestCtx, ClientId](
    val outQueue: Queue[F[Throwable, _], WebSocketFrame],
    val initialContext: RequestCtx,
    listeners: Seq[WsSessionListener[F, ClientId]],
    wsSessionStorage: WsSessionsStorage[F, RequestCtx, ClientId],
    printer: Printer,
    logger: LogIO2[F],
  ) extends WsClientSession[F, RequestCtx, ClientId] {
    private val openingTime: ZonedDateTime = IzTime.utcNow
    private val sessionId                  = WsSessionId(UUIDGen.getTimeUUID())
    private val clientId                   = new AtomicReference[Option[ClientId]](None)
    private val requestState               = new WsRequestState()

    def id: WsClientId[ClientId] = WsClientId(sessionId, clientId.get())

    override def request(method: IRTMethodId, data: Json): F[Throwable, RpcPacketId] = {
      val id      = RpcPacketId.random()
      val request = RpcPacket.buzzerRequest(id, method, data)
      for {
        _ <- logger.debug(s"WS Session: enqueue $request with $id to request state & send queue.")
        _ <- outQueue.offer(Text(printer.print(request.asJson)))
        _ <- requestState.request(id, method)
      } yield id
    }

    def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]] = {
      for {
        id <- request(method, data)
        response <- requestState.awaitResponse(id, timeout).guarantee {
          logger.debug(s"WS Session: $method, ${id -> "id"}: cleaning request state.") *>
          requestState.forget(id)
        }
      } yield response
    }

    override def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit] = {
      requestState.responseWith(id, response)
    }

    override def responseWithData(id: RpcPacketId, data: Json): F[Throwable, Unit] = {
      requestState.responseWithData(id, data)
    }

    override def updateId(maybeNewId: Option[ClientId]): F[Throwable, Unit] = {
      for {
        old     <- F.sync(id)
        _       <- F.sync(clientId.set(maybeNewId))
        current <- F.sync(id)
        _       <- F.when(old != current)(F.traverse_(listeners)(_.onClientIdUpdate(current, old)))
      } yield ()
    }

    override def finish(): F[Throwable, Unit] = {
      F.fromEither(WebSocketFrame.Close(1000)).flatMap(outQueue.offer(_)) *>
      wsSessionStorage.deleteClient(sessionId) *>
      F.traverse_(listeners)(_.onSessionClosed(id)) *>
      requestState.clear()
    }

    protected[http4s] def start(): F[Throwable, Unit] = {
      wsSessionStorage.addClient(this) *>
      F.traverse_(listeners)(_.onSessionOpened(id))
    }

    override def toString: String = s"[${id.toString}, ${duration().toSeconds}s]"

    private[this] def duration(): FiniteDuration = {
      val now = IzTime.utcNow
      val d   = java.time.Duration.between(openingTime, now)
      FiniteDuration(d.toNanos, TimeUnit.NANOSECONDS)
    }
  }
}
