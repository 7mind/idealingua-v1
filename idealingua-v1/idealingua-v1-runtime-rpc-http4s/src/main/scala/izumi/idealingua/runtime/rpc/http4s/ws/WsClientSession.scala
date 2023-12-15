package izumi.idealingua.runtime.rpc.http4s.ws

import cats.effect.std.Queue
import io.circe.syntax.EncoderOps
import io.circe.{Json, Printer}
import izumi.functional.bio.{F, IO2, Primitives2, Temporal2}
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

trait WsClientSession[F[+_, +_], RequestCtx] extends WsResponder[F] {
  def sessionId: WsSessionId
  def getRequestCtx: RequestCtx

  def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]]

  def updateRequestCtx(newContext: RequestCtx): F[Throwable, Unit]

  def start(onStart: RequestCtx => F[Throwable, Unit]): F[Throwable, Unit]
  def finish(onFinish: RequestCtx => F[Throwable, Unit]): F[Throwable, Unit]
}

object WsClientSession {

  class WsClientSessionImpl[F[+_, +_]: IO2: Temporal2: Primitives2, RequestCtx](
    outQueue: Queue[F[Throwable, _], WebSocketFrame],
    initialContext: RequestCtx,
    wsSessionsContext: Set[WsContextSessions[F, RequestCtx, ?]],
    wsSessionStorage: WsSessionsStorage[F, RequestCtx],
    wsContextExtractor: WsContextExtractor[RequestCtx],
    logger: LogIO2[F],
    printer: Printer,
  ) extends WsClientSession[F, RequestCtx] {
    private val requestCtxRef                   = new AtomicReference[RequestCtx](initialContext)
    private val openingTime: ZonedDateTime      = IzTime.utcNow
    private val requestState: WsRequestState[F] = WsRequestState.create[F]

    override val sessionId: WsSessionId = WsSessionId(UUIDGen.getTimeUUID())

    override def getRequestCtx: RequestCtx = requestCtxRef.get()

    override def updateRequestCtx(newContext: RequestCtx): F[Throwable, Unit] = {
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
      } yield ()
    }

    def requestAndAwaitResponse(method: IRTMethodId, data: Json, timeout: FiniteDuration): F[Throwable, Option[RawResponse]] = {
      val id      = RpcPacketId.random()
      val request = RpcPacket.buzzerRequest(id, method, data)
      for {
        _ <- logger.debug(s"WS Session: enqueue $request with $id to request state & send queue.")
        response <- requestState.requestAndAwait(id, Some(method), timeout) {
          outQueue.offer(Text(printer.print(request.asJson)))
        }
        _ <- logger.debug(s"WS Session: $method, ${id -> "id"}: cleaning request state.")
      } yield response
    }

    override def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit] = {
      requestState.responseWith(id, response)
    }

    override def responseWithData(id: RpcPacketId, data: Json): F[Throwable, Unit] = {
      requestState.responseWithData(id, data)
    }

    override def finish(onFinish: RequestCtx => F[Throwable, Unit]): F[Throwable, Unit] = {
      F.fromEither(WebSocketFrame.Close(1000)).flatMap(outQueue.offer(_)) *>
      requestState.clear() *>
      wsSessionStorage.deleteSession(sessionId) *>
      F.traverse_(wsSessionsContext)(_.updateSession(sessionId, None)) *>
      onFinish(getRequestCtx)
    }

    override def start(onStart: RequestCtx => F[Throwable, Unit]): F[Throwable, Unit] = {
      wsSessionStorage.addSession(this) *>
      F.traverse_(wsSessionsContext)(_.updateSession(sessionId, Some(getRequestCtx))) *>
      onStart(getRequestCtx)
    }

    override def toString: String = s"[$sessionId, ${duration().toSeconds}s]"

    private[this] def duration(): FiniteDuration = {
      val now = IzTime.utcNow
      val d   = java.time.Duration.between(openingTime, now)
      FiniteDuration(d.toNanos, TimeUnit.NANOSECONDS)
    }
  }
}
