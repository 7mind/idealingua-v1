package izumi.idealingua.runtime.rpc.http4s.ws

import cats.effect.std.Queue
import io.circe.syntax.*
import io.circe.{Json, Printer}
import izumi.functional.bio.{F, IO2, Primitives2, Temporal2}
import izumi.fundamentals.platform.time.IzTime
import izumi.fundamentals.platform.uuid.UUIDGen
import izumi.idealingua.runtime.rpc.*
import logstage.LogIO2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

trait WsClientSession[F[+_, +_], RequestCtx, ClientId] {
  def id: WsClientId[ClientId]
  def initialContext: RequestCtx

  protected[http4s] def requestState: RequestState[F]

  def updateId(maybeNewId: Option[ClientId]): F[Throwable, Unit]
  def outQueue: Queue[F[Throwable, _], WebSocketFrame]
  def request(method: IRTMethodId, data: Json): F[Throwable, RpcPacketId]
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

    override val requestState = new RequestState()

    def id: WsClientId[ClientId] = WsClientId(sessionId, clientId.get())

    override def request(method: IRTMethodId, data: Json): F[Throwable, RpcPacketId] = {
      val request = RpcPacket.buzzerRequestRndId(method, data)
      val id      = request.id.get
      for {
        _ <- logger.debug(s"WS Session: enqueue $request with $id to request state & send queue.")
        _ <- outQueue.offer(Text(printer.print(request.asJson)))
        _ <- requestState.request(id, method)
      } yield id
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
      F.sync(requestState.clear())
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
