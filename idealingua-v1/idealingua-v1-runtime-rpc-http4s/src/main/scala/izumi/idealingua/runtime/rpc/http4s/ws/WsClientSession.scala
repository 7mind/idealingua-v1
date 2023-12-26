package izumi.idealingua.runtime.rpc.http4s.ws

import cats.effect.std.Queue
import io.circe.syntax.EncoderOps
import io.circe.{Json, Printer}
import izumi.functional.bio.{Applicative2, F, IO2, Primitives2, Temporal2}
import izumi.fundamentals.platform.time.IzTime
import izumi.fundamentals.platform.uuid.UUIDGen
import izumi.idealingua.runtime.rpc.http4s.context.WsContextExtractor
import izumi.idealingua.runtime.rpc.http4s.ws.WsRpcHandler.WsResponder
import izumi.idealingua.runtime.rpc.{IRTMethodId, RpcPacket, RpcPacketId}
import logstage.LogIO2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

trait WsClientSession[F[+_, +_], SessionCtx] extends WsResponder[F] {
  def sessionId: WsSessionId

  def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]]

  def updateRequestCtx(newContext: SessionCtx): F[Throwable, SessionCtx]

  def start(onStart: SessionCtx => F[Throwable, Unit]): F[Throwable, Unit]
  def finish(onFinish: SessionCtx => F[Throwable, Unit]): F[Throwable, Unit]
}

object WsClientSession {

  def empty[F[+_, +_]: Applicative2, Ctx](wsSessionId: WsSessionId): WsClientSession[F, Ctx] = new WsClientSession[F, Ctx] {
    override def sessionId: WsSessionId                                                                                               = wsSessionId
    override def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]] = F.pure(None)
    override def updateRequestCtx(newContext: Ctx): F[Throwable, Ctx]                                                                 = F.pure(newContext)
    override def start(onStart: Ctx => F[Throwable, Unit]): F[Throwable, Unit]                                                        = F.unit
    override def finish(onFinish: Ctx => F[Throwable, Unit]): F[Throwable, Unit]                                                      = F.unit
    override def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit]                                             = F.unit
    override def responseWithData(id: RpcPacketId, data: Json): F[Throwable, Unit]                                                    = F.unit
  }

  abstract class Base[F[+_, +_]: IO2: Temporal2: Primitives2, SessionCtx](
    initialContext: SessionCtx,
    wsSessionsContext: Set[WsContextSessions.AnyContext[F, SessionCtx]],
    wsSessionStorage: WsSessionsStorage[F, SessionCtx],
    wsContextExtractor: WsContextExtractor[SessionCtx],
    logger: LogIO2[F],
  ) extends WsClientSession[F, SessionCtx] {
    private val requestCtxRef                   = new AtomicReference[SessionCtx](initialContext)
    private val openingTime: ZonedDateTime      = IzTime.utcNow
    private val requestState: WsRequestState[F] = WsRequestState.create[F]

    override val sessionId: WsSessionId = WsSessionId(UUIDGen.getTimeUUID())

    protected def sendMessage(message: RpcPacket): F[Throwable, Unit]
    protected def sendCloseMessage(): F[Throwable, Unit]

    override def updateRequestCtx(newContext: SessionCtx): F[Throwable, SessionCtx] = {
      for {
        contexts <- F.sync {
          requestCtxRef.synchronized {
            val oldContext = requestCtxRef.get()
            val updatedContext = requestCtxRef.updateAndGet {
              old =>
                wsContextExtractor.merge(old, newContext)
            }
            oldContext -> updatedContext
          }
        }
        (oldContext, updatedContext) = contexts
        _ <- F.when(oldContext != updatedContext) {
          F.traverse_(wsSessionsContext)(_.updateSession(sessionId, Some(updatedContext)))
        }
      } yield updatedContext
    }

    def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]] = {
      val id      = RpcPacketId.random()
      val request = RpcPacket.buzzerRequest(id, method, data)
      for {
        _        <- logger.debug(s"WS Session: enqueue $request with $id to request state & send queue.")
        response <- requestState.requestAndAwait(id, Some(method), timeout)(sendMessage(request))
        _        <- logger.debug(s"WS Session: $method, ${id -> "id"}: cleaning request state.")
      } yield response
    }

    override def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit] = {
      requestState.responseWith(id, response)
    }

    override def responseWithData(id: RpcPacketId, data: Json): F[Throwable, Unit] = {
      requestState.responseWithData(id, data)
    }

    override def finish(onFinish: SessionCtx => F[Throwable, Unit]): F[Throwable, Unit] = {
      val requestCtx = requestCtxRef.get()
      sendCloseMessage() *>
      requestState.clear() *>
      wsSessionStorage.deleteSession(sessionId) *>
      F.traverse_(wsSessionsContext)(_.updateSession(sessionId, None)) *>
      onFinish(requestCtx)
    }

    override def start(onStart: SessionCtx => F[Throwable, Unit]): F[Throwable, Unit] = {
      val requestCtx = requestCtxRef.get()
      wsSessionStorage.addSession(this) *>
      F.traverse_(wsSessionsContext)(_.updateSession(sessionId, Some(requestCtx))) *>
      onStart(requestCtx)
    }

    override def toString: String = s"[$sessionId, ${duration().toSeconds}s]"

    private[this] def duration(): FiniteDuration = {
      val now = IzTime.utcNow
      val d   = java.time.Duration.between(openingTime, now)
      FiniteDuration(d.toNanos, TimeUnit.NANOSECONDS)
    }
  }

  final class Dummy[F[+_, +_]: IO2: Temporal2: Primitives2, SessionCtx](
    initialContext: SessionCtx,
    wsSessionsContext: Set[WsContextSessions.AnyContext[F, SessionCtx]],
    wsSessionStorage: WsSessionsStorage[F, SessionCtx],
    wsContextExtractor: WsContextExtractor[SessionCtx],
    logger: LogIO2[F],
  ) extends Base[F, SessionCtx](initialContext, wsSessionsContext, wsSessionStorage, wsContextExtractor, logger) {
    override protected def sendMessage(message: RpcPacket): F[Throwable, Unit] = F.unit
    override protected def sendCloseMessage(): F[Throwable, Unit]              = F.unit
  }

  final class Queued[F[+_, +_]: IO2: Temporal2: Primitives2, SessionCtx](
    outQueue: Queue[F[Throwable, _], WebSocketFrame],
    initialContext: SessionCtx,
    wsSessionsContext: Set[WsContextSessions.AnyContext[F, SessionCtx]],
    wsSessionStorage: WsSessionsStorage[F, SessionCtx],
    wsContextExtractor: WsContextExtractor[SessionCtx],
    logger: LogIO2[F],
    printer: Printer,
  ) extends Base[F, SessionCtx](initialContext, wsSessionsContext, wsSessionStorage, wsContextExtractor, logger) {
    override protected def sendMessage(message: RpcPacket): F[Throwable, Unit] = {
      outQueue.offer(Text(printer.print(message.asJson)))
    }
    override protected def sendCloseMessage(): F[Throwable, Unit] = {
      F.fromEither(WebSocketFrame.Close(1000)).flatMap(outQueue.offer(_))
    }
  }
}
