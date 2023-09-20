package izumi.idealingua.runtime.rpc.http4s.clients

import io.circe.syntax.*
import io.circe.{Json, Printer}
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.{Async2, Exit, F, IO2, Primitives2, Temporal2, UnsafeRun2}
import izumi.functional.lifecycle.Lifecycle
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.clients.WsDispatcherFactory.{WsClientContextProvider, WsIRTDispatcher}
import izumi.idealingua.runtime.rpc.http4s.ws.{RawResponse, RequestState}
import logstage.LogIO2
import org.asynchttpclient.netty.ws.NettyWebSocket
import org.asynchttpclient.ws.{WebSocket, WebSocketListener, WebSocketUpgradeHandler}
import org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}
import org.http4s.{Headers, Uri}

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters.SeqHasAsJava

class WsDispatcherFactory[F[+_, +_]: Async2: Temporal2: Primitives2: UnsafeRun2](
  codec: IRTClientMultiplexor[F],
  printer: Printer,
  logger: LogIO2[F],
) {

  def dispatcher[ServerContext](
    uri: Uri,
    muxer: IRTServerMultiplexor[F, ServerContext],
    contextProvider: WsClientContextProvider[ServerContext],
    headers: Map[String, String] = Map.empty,
    timeout: FiniteDuration      = 30.seconds,
  ): Lifecycle[F[Throwable, *], WsIRTDispatcher[F]] = {
    for {
      client         <- WsDispatcherFactory.asyncHttpClient[F]
      requestState    = new RequestState[F]
      listener       <- Lifecycle.liftF(F.syncThrowable(createListener(muxer, contextProvider, requestState)))
      handler        <- Lifecycle.liftF(F.syncThrowable(new WebSocketUpgradeHandler(List(listener).asJava)))
      nettyWebSocket <- Lifecycle.liftF(F.fromFutureJava(client.prepareGet(uri.toString()).execute(handler).toCompletableFuture))
      _              <- Lifecycle.make(F.unit)(_ => F.sync(nettyWebSocket.sendCloseFrame().await()).void)
      _              <- Lifecycle.make(F.unit)(_ => requestState.clear())
    } yield {
      new WsIRTDispatcher[F] {
        override def dispatch(input: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
          dispatchFor(nettyWebSocket, codec, requestState)(headers, timeout)(input)
        }
        override def authorize(headers: Map[String, String]): F[Throwable, Unit] = {
          performAuthorization(nettyWebSocket, requestState)(headers, timeout)
        }
      }
    }
  }

  def dispatcher[ServerContext](
    uri: Uri,
    muxer: IRTServerMultiplexor[F, ServerContext],
    contextProvider: WsClientContextProvider[ServerContext],
    headers: Headers,
  ): Lifecycle[F[Throwable, *], WsIRTDispatcher[F]] = {
    dispatcher(uri, muxer, contextProvider, headers.headers.map(h => h.name.toString -> h.value).toMap)
  }

  def dispatcher[ServerContext](
    uri: Uri,
    muxer: IRTServerMultiplexor[F, ServerContext],
    contextProvider: WsClientContextProvider[ServerContext],
    headers: Headers,
    timeout: FiniteDuration,
  ): Lifecycle[F[Throwable, *], WsIRTDispatcher[F]] = {
    dispatcher(uri, muxer, contextProvider, headers.headers.map(h => h.name.toString -> h.value).toMap, timeout)
  }

  protected def performAuthorization(
    nettyWebSocket: NettyWebSocket,
    requestState: RequestState[F],
  )(headers: Map[String, String],
    timeout: FiniteDuration,
  ): F[Throwable, Unit] = {
    val packetId = RpcPacketId.random()
    F.bracket(requestState.requestEmpty(packetId))(_ => requestState.forget(packetId)) {
      _ =>
        val emptyBodyPacket = RpcPacket(
          kind    = RPCPacketKind.RpcRequest,
          data    = None,
          id      = Some(packetId),
          ref     = None,
          service = None,
          method  = None,
          headers = Some(headers),
        )
        val textFrame = printer.print(emptyBodyPacket.asJson)
        for {
          _ <- F.syncThrowable(nettyWebSocket.sendTextFrame(textFrame).await())
          _ <- requestState.awaitResponse(packetId, timeout).flatMap {
            case Some(_: RawResponse.GoodRawResponse)  => F.unit
            case Some(_: RawResponse.EmptyRawResponse) => F.unit
            case Some(_: RawResponse.BadRawResponse)   => F.fail(new IRTGenericFailure("Authorization failed."))
            case None                                  => F.fail(new IRTGenericFailure("Unable to authorize."))
          }
        } yield ()
    }
  }

  protected def createListener[ServerContext](
    muxer: IRTServerMultiplexor[F, ServerContext],
    contextProvider: WsClientContextProvider[ServerContext],
    requestState: RequestState[F],
  ): WebSocketListener = new WebSocketListener() {
    private val socketRef = new AtomicReference[Option[WebSocket]](None)
    override def onOpen(websocket: WebSocket): Unit = {
      socketRef.set(Some(websocket))
    }

    override def onClose(websocket: WebSocket, code: Int, reason: String): Unit = {
      socketRef.get().foreach(_.sendCloseFrame().await())
      socketRef.set(None)
    }

    override def onError(t: Throwable): Unit = {
      socketRef.get().foreach(_.sendCloseFrame().await())
      socketRef.set(None)
    }

    override def onPingFrame(payload: Array[Byte]): Unit = {
      socketRef.get().foreach(_.sendPongFrame().await())
    }

    override def onTextFrame(payload: String, finalFragment: Boolean, rsv: Int): Unit = {
      processTextFrame(muxer, contextProvider)(requestState, payload).foreach {
        response =>
          socketRef.get().foreach {
            ws => ws.sendTextFrame(printer.print(response.asJson))
          }
      }
    }
  }

  protected def buildRequest(requestId: RpcPacketId, methodId: IRTMethodId, encoded: Json, header: Option[Map[String, String]]): RpcPacket = {
    RpcPacket(
      kind    = RPCPacketKind.RpcRequest,
      data    = Some(encoded),
      id      = Some(requestId),
      ref     = None,
      service = Some(methodId.service.value),
      method  = Some(methodId.methodId.value),
      headers = header,
    )
  }

  protected def dispatchFor(
    nettyWebSocket: NettyWebSocket,
    codec: IRTClientMultiplexor[F],
    requestState: RequestState[F],
  )(headers: Map[String, String],
    timeout: FiniteDuration,
  )(request: IRTMuxRequest
  ): F[Throwable, IRTMuxResponse] = {
    val defaultHeaders = Option(headers).filter(_.nonEmpty)
    for {
      _          <- logger.trace(s"${request.method -> "method"}: Going to perform $request")
      encoded    <- codec.encode(request)
      rpcPacketId = RpcPacketId.random()
      rpcPacket   = buildRequest(rpcPacketId, request.method, encoded, defaultHeaders)
      res <- F.bracket(requestState.request(rpcPacketId, request.method))(_ => requestState.forget(rpcPacketId)) {
        _ =>
          for {
            _        <- F.syncThrowable(nettyWebSocket.sendTextFrame(printer.print(rpcPacket.asJson)))
            response <- requestState.awaitResponse(rpcPacketId, timeout)
            result <- response match {
              case Some(value: RawResponse.EmptyRawResponse) =>
                F.fail(new IRTGenericFailure(s"${request.method -> "method"}, $rpcPacketId: empty response: $value"))

              case Some(value: RawResponse.GoodRawResponse) =>
                logger.debug(s"${request.method -> "method"}, $rpcPacketId: Have response: $value") *>
                codec.decode(value.data, value.method)

              case Some(value: RawResponse.BadRawResponse) =>
                logger.debug(s"${request.method -> "method"}, $rpcPacketId: Have response: $value") *>
                F.fail(new IRTGenericFailure(s"${request.method -> "method"}, $rpcPacketId: generic failure: $value"))

              case None =>
                F.fail(new TimeoutException(s"${request.method -> "method"}, $rpcPacketId: No response in $timeout"))
            }
          } yield result
      }
    } yield res
  }

  private def processTextFrame[ServerContext](
    muxer: IRTServerMultiplexor[F, ServerContext],
    contextProvider: WsClientContextProvider[ServerContext],
  )(requestState: RequestState[F],
    payload: String,
  ): Option[RpcPacket] = {

    val effect = (for {
      _       <- logger.debug(s"Incoming WS message: $payload")
      decoded <- F.fromEither(io.circe.parser.decode[RpcPacket](payload))
      context  = contextProvider.toContext(decoded)
      res <- decoded match {
        // handle empty body response for authorization
        case RpcPacket(RPCPacketKind.RpcResponse, None, _, Some(ref), _, _, _) =>
          requestState.responseWith(ref, RawResponse.EmptyRawResponse()).as(None)

        case RpcPacket(RPCPacketKind.BuzzResponse, Some(data), _, ref, _, _, _) =>
          requestState.handleResponse(ref, data).as(None)

        case RpcPacket(RPCPacketKind.BuzzRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
          handleRequest(muxer, context, data, IRTMethodId(IRTServiceId(service), IRTMethodName(method)))(
            onSuccess = RpcPacket.buzzerResponse(id, _),
            onFail    = RpcPacket.buzzerFail(Some(id), _),
          )

        case RpcPacket(RPCPacketKind.RpcResponse, Some(data), _, ref, _, _, _) =>
          requestState.handleResponse(ref, data).as(None)

        case RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
          handleRequest(muxer, context, data, IRTMethodId(IRTServiceId(service), IRTMethodName(method)))(
            onSuccess = RpcPacket.rpcResponse(id, _),
            onFail    = RpcPacket.rpcFail(Some(id), _),
          )

        case RpcPacket(RPCPacketKind.RpcFail, data, _, Some(ref), _, _, _) =>
          requestState.responseWith(ref, RawResponse.BadRawResponse(data)).as(None)

        case RpcPacket(RPCPacketKind.BuzzFailure, data, _, Some(ref), _, _, _) =>
          requestState.responseWith(ref, RawResponse.BadRawResponse(data)).as(None)

        case o =>
          logger.error(s"No buzzer client handler for $o").as(None)
      }
    } yield res).sandbox.tapError {
      case Error(error, trace)           => logger.error(s"Failed to process request: $error $trace")
      case Termination(cause, _, trace)  => logger.error(s"Failed to process request, termination: $cause $trace")
      case Interruption(error, _, trace) => logger.error(s"Request processing was interrupted: $error $trace")
    }.leftMap(_ => Some(RpcPacket(RPCPacketKind.Fail, None, None, None, None, None, None)))

    UnsafeRun2[F].unsafeRun(effect)
  }

  protected def handleRequest[ServerContext](
    muxer: IRTServerMultiplexor[F, ServerContext],
    context: ServerContext,
    data: Json,
    methodId: IRTMethodId,
  )(onSuccess: Json => RpcPacket,
    onFail: String => RpcPacket,
  ): F[Nothing, Option[RpcPacket]] = {
    muxer.doInvoke(data, context, methodId).sandboxExit.flatMap {
      case Success(maybeResponse) =>
        F.pure(maybeResponse.map(onSuccess))

      case Exit.Termination(exception, allExceptions, trace) =>
        logger.error(s"WS processing terminated, $exception, $allExceptions, $trace").as(Some(onFail(exception.getMessage)))

      case Exit.Error(exception, trace) =>
        logger.error(s"WS processing failed, $exception $trace").as(Some(onFail(exception.getMessage)))

      case Exit.Interruption(exception, allExceptions, trace) =>
        logger.error(s"WS processing interrupted, $exception $allExceptions $trace").as(Some(onFail(exception.getMessage)))
    }
  }
}

object WsDispatcherFactory {
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

  trait WsClientContextProvider[Ctx] {
    def toContext(packet: RpcPacket): Ctx
  }
  object WsClientContextProvider {
    def unit: WsClientContextProvider[Unit] = _ => ()
  }

  trait WsIRTDispatcher[F[_, _]] extends IRTDispatcher[F] {
    def authorize(headers: Map[String, String]): F[Throwable, Unit]
  }
}
