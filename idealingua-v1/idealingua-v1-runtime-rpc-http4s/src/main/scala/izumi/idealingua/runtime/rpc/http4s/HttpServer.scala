package izumi.idealingua.runtime.rpc.http4s

import _root_.io.circe.parser.*
import cats.data.OptionT
import cats.effect.Async
import cats.effect.std.Queue
import fs2.Stream
import io.circe
import io.circe.syntax.EncoderOps
import io.circe.{Json, Printer}
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.{Exit, F, IO2, Primitives2, Temporal2, UnsafeRun2}
import izumi.fundamentals.platform.language.Quirks
import izumi.fundamentals.platform.time.IzTime
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.HttpServer.{ServerWsRpcHandler, WsResponseMarker}
import izumi.idealingua.runtime.rpc.http4s.ws.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsClientSession.WsClientSessionImpl
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextProvider.WsAuthResult
import logstage.LogIO2
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.typelevel.vault.Key

import java.time.ZonedDateTime
import java.util.concurrent.RejectedExecutionException
import scala.concurrent.duration.DurationInt

class HttpServer[F[+_, +_]: IO2: Temporal2: Primitives2: UnsafeRun2, RequestCtx, MethodCtx, ClientId](
  val muxer: IRTServerMultiplexor[F, RequestCtx],
  val codec: IRTClientMultiplexor[F],
  val contextProvider: AuthMiddleware[F[Throwable, _], RequestCtx],
  val wsContextProvider: WsContextProvider[F, RequestCtx, ClientId],
  val wsSessionStorage: WsSessionsStorage[F, RequestCtx, ClientId],
  val listeners: Seq[WsSessionListener[F, ClientId]],
  dsl: Http4sDsl[F[Throwable, _]],
  logger: LogIO2[F],
  printer: Printer,
)(implicit val AT: Async[F[Throwable, _]]
) {
  import dsl.*

  // WS Response attribute key, to differ from usual HTTP responses
  private val wsAttributeKey = UnsafeRun2[F].unsafeRun(Key.newKey[F[Throwable, _], WsResponseMarker.type])

  protected def loggingMiddle(service: HttpRoutes[F[Throwable, _]]): HttpRoutes[F[Throwable, _]] = {
    cats.data.Kleisli {
      (req: Request[F[Throwable, _]]) =>
        OptionT.apply {
          (for {
            _    <- logger.trace(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: initiated")
            resp <- service(req).value
            _ <- F.traverse(resp) {
              case Status.Successful(resp) =>
                logger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: success, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
              case resp if resp.attributes.contains(wsAttributeKey) =>
                logger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: websocket request")
              case resp =>
                logger.info(s"${req.method.name -> "method"} ${req.pathInfo -> "uri"}: rejection, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            }
          } yield resp).tapError {
            cause =>
              logger.error(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: failure, $cause")
          }
        }
    }
  }

  def service(ws: WebSocketBuilder2[F[Throwable, _]]): HttpRoutes[F[Throwable, _]] = {
    val svc                                   = AuthedRoutes.of(router(ws))
    val aservice: HttpRoutes[F[Throwable, _]] = contextProvider(svc)
    loggingMiddle(aservice)
  }

  protected def router(ws: WebSocketBuilder2[F[Throwable, _]]): PartialFunction[AuthedRequest[F[Throwable, _], RequestCtx], F[Throwable, Response[F[Throwable, _]]]] = {
    case request @ GET -> Root / "ws" as ctx =>
      setupWs(request, ctx, ws)

    case request @ GET -> Root / service / method as ctx =>
      val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
      processHttpRequest(new HttpRequestContext(request, ctx), body = "{}", methodId)

    case request @ POST -> Root / service / method as ctx =>
      val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
      request.req.decode[String] {
        body =>
          processHttpRequest(new HttpRequestContext(request, ctx), body, methodId)
      }
  }

  protected def handleWsClose(context: WsClientSession[F, RequestCtx, ClientId]): F[Throwable, Unit] = {
    logger.debug(s"WS Session: Websocket client disconnected ${context.id}.") *>
    context.finish()
  }

  protected def globalWsListener: WsSessionListener[F, ClientId] = new WsSessionListener[F, ClientId] {
    def onSessionOpened(context: WsClientId[ClientId]): F[Throwable, Unit] = {
      logger.debug(s"WS Session: opened ${context.id}.")
    }
    def onClientIdUpdate(context: WsClientId[ClientId], old: WsClientId[ClientId]): F[Throwable, Unit] = {
      logger.debug(s"WS Session: Id updated to ${context.id}, was: ${old.id}.")
    }
    def onSessionClosed(context: WsClientId[ClientId]): F[Throwable, Unit] = {
      logger.debug(s"WS Session: closed ${context.id}.")
    }
  }

  protected def setupWs(
    request: AuthedRequest[F[Throwable, _], RequestCtx],
    initialContext: RequestCtx,
    ws: WebSocketBuilder2[F[Throwable, _]],
  ): F[Throwable, Response[F[Throwable, _]]] = {
    Quirks.discard(request)
    def pingStream: Stream[F[Throwable, _], WebSocketFrame.Ping] = {
      Stream
        .awakeEvery[F[Throwable, _]](5.second)
        .evalMap(_ => logger.debug("WS Server: Sending ping frame.").as(WebSocketFrame.Ping()))
    }
    for {
      outQueue           <- Queue.unbounded[F[Throwable, _], WebSocketFrame]
      listenersWithGlobal = Seq(globalWsListener) ++ listeners
      context             = new WsClientSessionImpl(outQueue, initialContext, listenersWithGlobal, wsSessionStorage, printer, logger)
      _                  <- context.start()
      outStream           = Stream.fromQueueUnterminated(outQueue).merge(pingStream)
      inStream = {
        (inputStream: Stream[F[Throwable, _], WebSocketFrame]) =>
          inputStream.evalMap {
            processWsRequest(context)(_).flatMap {
              case Some(v) => outQueue.offer(WebSocketFrame.Text(v))
              case None    => F.unit
            }
          }
      }
      response <- ws.withOnClose(handleWsClose(context)).build(outStream, inStream)
    } yield {
      response.withAttribute(wsAttributeKey, WsResponseMarker)
    }
  }

  protected def processWsRequest(
    context: WsClientSession[F, RequestCtx, ClientId],
    requestTime: ZonedDateTime = IzTime.utcNow,
  )(frame: WebSocketFrame
  ): F[Throwable, Option[String]] = {
    (frame match {
      case WebSocketFrame.Text(msg, _) => wsHandler(context).processRpcMessage(msg)
      case WebSocketFrame.Close(_)     => F.pure(None)
      case _: WebSocketFrame.Pong      => onWsHeartbeat(requestTime).as(None)
      case unknownMessage              => wsHandler(context).handleWsError(List.empty, s"Unsupported WS frame: $unknownMessage.")
    }).map(_.map(p => printer.print(p.asJson)))
  }

  protected def wsHandler(context: WsClientSession[F, RequestCtx, ClientId]): WsRpcHandler[F, RequestCtx] = {
    new ServerWsRpcHandler(muxer, wsContextProvider, context, logger)
  }

  protected def onWsHeartbeat(requestTime: ZonedDateTime): F[Throwable, Unit] = {
    logger.debug(s"WS Session: pong frame at $requestTime")
  }

  protected def processHttpRequest(
    context: HttpRequestContext[F[Throwable, _], RequestCtx],
    body: String,
    method: IRTMethodId,
  ): F[Throwable, Response[F[Throwable, _]]] = {
    val ioR = for {
      parsed      <- F.fromEither(parse(body))
      maybeResult <- muxer.doInvoke(parsed, context.context, method)
    } yield {
      maybeResult
    }

    ioR.sandboxExit.flatMap(handleHttpResult(context, method, _))
  }

  private def handleHttpResult(
    context: HttpRequestContext[F[Throwable, _], RequestCtx],
    method: IRTMethodId,
    result: Exit[Throwable, Option[Json]],
  ): F[Throwable, Response[F[Throwable, _]]] = {
    result match {
      case Success(Some(value)) =>
        dsl.Ok(printer.print(value))

      case Success(None) =>
        logger.warn(s"${context -> null}: No service handler for $method") *>
        dsl.NotFound()

      case Error(error: circe.Error, trace) =>
        logger.info(s"${context -> null}: Parsing failure while handling $method: $error $trace") *>
        dsl.BadRequest()

      case Error(error: IRTDecodingException, trace) =>
        logger.info(s"${context -> null}: Parsing failure while handling $method: $error $trace") *>
        dsl.BadRequest()

      case Error(error: IRTLimitReachedException, trace) =>
        logger.debug(s"${context -> null}: Request failed because of request limit reached $method: $error $trace") *>
        dsl.TooManyRequests()

      case Error(error: IRTUnathorizedRequestContextException, trace) =>
        // workarount because implicits conflict
        logger.debug(s"${context -> null}: Request failed because of unexpected request context reached $method: $error $trace") *>
        dsl.Forbidden().map(_.copy(status = dsl.Unauthorized))

      case Error(error, trace) =>
        logger.info(s"${context -> null}: Unexpected failure while handling $method: $error $trace") *>
        dsl.InternalServerError()

      case Termination(_, (cause: IRTHttpFailureException) :: _, trace) =>
        logger.debug(s"${context -> null}: Request rejected, $method, ${context.request}, $cause, $trace") *>
        F.pure(Response(status = cause.status))

      case Termination(_, (cause: RejectedExecutionException) :: _, trace) =>
        logger.warn(s"${context -> null}: Not enough capacity to handle $method: $cause $trace") *>
        dsl.TooManyRequests()

      case Termination(cause, _, trace) =>
        logger.error(s"${context -> null}: Execution failed, termination, $method, ${context.request}, $cause, $trace") *>
        dsl.InternalServerError()

      case Interruption(cause, _, trace) =>
        logger.info(s"${context -> null}: Unexpected interruption while handling $method: $cause $trace") *>
        dsl.InternalServerError()
    }
  }

}

object HttpServer {
  case object WsResponseMarker
  class ServerWsRpcHandler[F[+_, +_]: IO2, RequestCtx, ClientId](
    muxer: IRTServerMultiplexor[F, RequestCtx],
    wsContextProvider: WsContextProvider[F, RequestCtx, ClientId],
    context: WsClientSession[F, RequestCtx, ClientId],
    logger: LogIO2[F],
  ) extends WsRpcHandler[F, RequestCtx](muxer, context, logger) {
    override def handlePacket(packet: RpcPacket): F[Throwable, Unit] = {
      wsContextProvider.toId(context.initialContext, context.id, packet).flatMap(context.updateId)
    }
    override def handleAuthRequest(packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
      wsContextProvider.handleAuthorizationPacket(context.id, context.initialContext, packet).flatMap {
        case WsAuthResult(id, packet) => context.updateId(id).as(Some(packet))
      }
    }
    override def extractContext(packet: RpcPacket): F[Throwable, RequestCtx] = {
      wsContextProvider.toContext(context.id, context.initialContext, packet)
    }
  }
}
