package izumi.idealingua.runtime.rpc.http4s.clients

import io.circe.syntax.*
import io.circe.{Json, Printer}
import izumi.functional.bio.{Async2, Exit, F, IO2, Primitives2, Temporal2, UnsafeRun2}
import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.language.Quirks.Discarder
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTContextServicesMuxer
import izumi.idealingua.runtime.rpc.http4s.clients.WsRpcDispatcher.IRTDispatcherWs
import izumi.idealingua.runtime.rpc.http4s.clients.WsRpcDispatcherFactory.{ClientWsRpcHandler, WsRpcClientConnection, fromNettyFuture}
import izumi.idealingua.runtime.rpc.http4s.ws.{RawResponse, WsRequestState, WsRpcHandler}
import izumi.logstage.api.IzLogger
import logstage.LogIO2
import org.asynchttpclient.netty.ws.NettyWebSocket
import org.asynchttpclient.ws.{WebSocket, WebSocketListener, WebSocketUpgradeHandler}
import org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}
import org.http4s.{Headers, Uri}

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters.*

class WsRpcDispatcherFactory[F[+_, +_]: Async2: Temporal2: Primitives2: UnsafeRun2](
  codec: IRTClientMultiplexor[F],
  printer: Printer,
  logger: LogIO2[F],
  izLogger: IzLogger,
) {

  def connect(
    uri: Uri,
    muxer: IRTContextServicesMuxer[F],
  ): Lifecycle[F[Throwable, _], WsRpcClientConnection[F]] = {
    for {
      client       <- WsRpcDispatcherFactory.asyncHttpClient[F]
      requestState <- Lifecycle.liftF(F.syncThrowable(WsRequestState.create[F]))
      listener     <- Lifecycle.liftF(F.syncThrowable(createListener(muxer, requestState, dispatcherLogger(uri, logger))))
      handler      <- Lifecycle.liftF(F.syncThrowable(new WebSocketUpgradeHandler(List(listener).asJava)))
      nettyWebSocket <- Lifecycle.make(
        F.fromFutureJava(client.prepareGet(uri.toString()).execute(handler).toCompletableFuture)
      )(nettyWebSocket => fromNettyFuture(nettyWebSocket.sendCloseFrame()).void)
      // fill promises before closing WS connection, potentially giving a chance to send out an error response before closing
      _ <- Lifecycle.make(F.unit)(_ => requestState.clear())
    } yield {
      new WsRpcClientConnection.Netty(nettyWebSocket, requestState, printer)
    }
  }

  def dispatcher[ServerContext](
    uri: Uri,
    muxer: IRTContextServicesMuxer[F],
    tweakRequest: RpcPacket => RpcPacket = identity,
    timeout: FiniteDuration              = 30.seconds,
  ): Lifecycle[F[Throwable, _], IRTDispatcherWs[F]] = {
    connect(uri, muxer).map {
      new WsRpcDispatcher(_, timeout, codec, dispatcherLogger(uri, logger)) {
        override protected def buildRequest(rpcPacketId: RpcPacketId, method: IRTMethodId, body: Json): RpcPacket = {
          tweakRequest(super.buildRequest(rpcPacketId, method, body))
        }
      }
    }
  }

  protected def wsHandler[ServerContext](
    muxer: IRTContextServicesMuxer[F],
    requestState: WsRequestState[F],
    logger: LogIO2[F],
  ): WsRpcHandler[F] = {
    new ClientWsRpcHandler(muxer, requestState, logger)
  }

  protected def createListener(
    muxer: IRTContextServicesMuxer[F],
    requestState: WsRequestState[F],
    logger: LogIO2[F],
  ): WebSocketListener = new WebSocketListener() {
    private val handler   = wsHandler(muxer, requestState, logger)
    private val socketRef = new AtomicReference[Option[WebSocket]](None)

    override def onOpen(websocket: WebSocket): Unit = {
      socketRef.set(Some(websocket))
    }

    override def onClose(websocket: WebSocket, code: Int, reason: String): Unit = {
      socketRef.set(None)
      websocket.sendCloseFrame()
      ()
    }

    override def onError(t: Throwable): Unit = {
      socketRef.getAndSet(None).foreach(_.sendCloseFrame())
    }

    override def onPingFrame(payload: Array[Byte]): Unit = {
      socketRef.get().foreach(_.sendPongFrame())
    }

    override def onTextFrame(payload: String, finalFragment: Boolean, rsv: Int): Unit = {
      UnsafeRun2[F].unsafeRunAsync(handler.processRpcMessage(payload)) {
        exit =>
          val maybeResponse: Option[RpcPacket] = exit match {
            case Exit.Success(response)         => response
            case Exit.Error(error, _)           => handleWsError(List(error), "errored")
            case Exit.Termination(error, _, _)  => handleWsError(List(error), "terminated")
            case Exit.Interruption(error, _, _) => handleWsError(List(error), "interrupted")
          }
          maybeResponse.foreach {
            response =>
              socketRef.get().foreach {
                ws => ws.sendTextFrame(printer.print(response.asJson))
              }
          }
      }
    }
  }

  protected def dispatcherLogger(uri: Uri, logger: LogIO2[F]): LogIO2[F] = {
    uri.discard()
    logger
  }

  private def handleWsError(causes: List[Throwable], message: String): Option[RpcPacket] = {
    causes.headOption match {
      case Some(cause) =>
        izLogger.error(s"WS request failed: $message, $cause")
        Some(RpcPacket.rpcCritical(s"$message, cause: $cause", None))
      case None =>
        izLogger.error(s"WS request failed: $message.")
        Some(RpcPacket.rpcCritical(message, None))
    }
  }
}

object WsRpcDispatcherFactory {
  def asyncHttpClient[F[+_, +_]: IO2]: Lifecycle[F[Throwable, _], DefaultAsyncHttpClient] = {
    Lifecycle.fromAutoCloseable(F.syncThrowable {
      new DefaultAsyncHttpClient(
        new DefaultAsyncHttpClientConfig.Builder()
          .setWebSocketMaxBufferSize(64 * 1024 * 1024 * 8) // increase buffer size for 64MB, 128000000 - is default value
          .setWebSocketMaxFrameSize(64 * 1024 * 1024 * 8) // increase frame size for 64MB
          .setKeepAlive(true)
          .setSoKeepAlive(true)
          .setRequestTimeout(30 * 1000) // 60 seconds is default
          .setPooledConnectionIdleTimeout(60 * 1000) // 60 seconds is default
          .setConnectTimeout(30 * 1000) // 5 seconds is default
          .setReadTimeout(60 * 1000) // 60 seconds is default
          .setShutdownTimeout(15 * 1000) // 15 seconds is default
          .build()
      )
    })
  }

  class ClientWsRpcHandler[F[+_, +_]: IO2](
    muxer: IRTContextServicesMuxer[F],
    requestState: WsRequestState[F],
    logger: LogIO2[F],
  ) extends WsRpcHandler[F](muxer, requestState, logger) {
    override protected def handlePacket(packet: RpcPacket): F[Throwable, Unit] = {
      F.unit
    }

    override protected def handleAuthRequest(packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
      F.pure(None)
    }

    override protected def getAuthContext: AuthContext = {
      AuthContext(Headers.empty, None)
    }
  }

  trait WsRpcClientConnection[F[_, _]] {
    private[clients] def requestAndAwait(id: RpcPacketId, packet: RpcPacket, method: Option[IRTMethodId], timeout: FiniteDuration): F[Throwable, Option[RawResponse]]
    def authorize(headers: Map[String, String], timeout: FiniteDuration = 30.seconds): F[Throwable, Unit]
  }
  object WsRpcClientConnection {
    class Netty[F[+_, +_]: Async2](
      nettyWebSocket: NettyWebSocket,
      requestState: WsRequestState[F],
      printer: Printer,
    ) extends WsRpcClientConnection[F] {

      override def authorize(headers: Map[String, String], timeout: FiniteDuration): F[Throwable, Unit] = {
        val packetId = RpcPacketId.random()
        requestAndAwait(packetId, RpcPacket.auth(packetId, headers), None, timeout).flatMap {
          case Some(_: RawResponse.GoodRawResponse)    => F.unit
          case Some(_: RawResponse.EmptyRawResponse)   => F.unit
          case Some(value: RawResponse.BadRawResponse) => F.fail(new IRTGenericFailure(s"Authorization failed: ${value.error}."))
          case None                                    => F.fail(new IRTGenericFailure("Unable to authorize."))
        }
      }

      override private[clients] def requestAndAwait(
        id: RpcPacketId,
        packet: RpcPacket,
        method: Option[IRTMethodId],
        timeout: FiniteDuration,
      ): F[Throwable, Option[RawResponse]] = {
        requestState.requestAndAwait(id, method, timeout) {
          fromNettyFuture(nettyWebSocket.sendTextFrame(printer.print(packet.asJson)))
        }
      }
    }
  }

  trait WsRpcContextProvider[Ctx] {
    def toContext(packet: RpcPacket): Ctx
  }
  object WsRpcContextProvider {
    def unit: WsRpcContextProvider[Unit] = _ => ()
  }

  private def fromNettyFuture[F[+_, +_]: Async2, A](mkNettyFuture: => io.netty.util.concurrent.Future[A]): F[Throwable, A] = {
    F.syncThrowable(mkNettyFuture).flatMap {
      nettyFuture =>
        F.asyncCancelable {
          callback =>
            nettyFuture.addListener {
              (completedFuture: io.netty.util.concurrent.Future[A]) =>
                try {
                  if (!completedFuture.isDone) {
                    // shouldn't be possible, future should already be completed
                    completedFuture.await(1000L)
                  }
                  if (completedFuture.isSuccess) {
                    callback(Right(completedFuture.getNow))
                  } else {
                    Option(completedFuture.cause()) match {
                      case Some(error) => callback(Left(error))
                      case None        => callback(Left(new RuntimeException("Awaiting NettyFuture failed, but no exception was available.")))
                    }
                  }
                } catch {
                  case exception: Throwable =>
                    callback(Left(new RuntimeException(s"Awaiting NettyFuture threw an exception=$exception")))
                }
            }
            val canceler = F.sync {
              nettyFuture.cancel(false);
              ()
            }
            canceler
        }
    }
  }

}
