package izumi.idealingua.runtime.rpc.http4s

import _root_.io.circe.parser.*
import cats.effect.Async
import cats.effect.std.Queue
import fs2.Stream
import io.circe
import io.circe.syntax.EncoderOps
import io.circe.{Json, Printer}
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.{Exit, F, IO2, Primitives2, Temporal2}
import izumi.fundamentals.platform.language.Quirks
import izumi.fundamentals.platform.language.Quirks.*
import izumi.fundamentals.platform.time.IzTime
import izumi.idealingua.runtime.rpc
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.ws.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsClientSession.WsClientSessionImpl
import izumi.logstage.api.IzLogger
import logstage.LogIO2
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

import java.time.ZonedDateTime
import java.util.concurrent.RejectedExecutionException
import scala.concurrent.duration.DurationInt

class HttpServer[F[+_, +_]: IO2: Temporal2: Primitives2, RequestCtx, MethodCtx, ClientId](
  val muxer: IRTServerMultiplexor[F, RequestCtx],
  val codec: IRTClientMultiplexor[F],
  val contextProvider: AuthMiddleware[F[Throwable, _], RequestCtx],
  val wsContextProvider: WsContextProvider[F, RequestCtx, ClientId],
  val wsSessionStorage: WsSessionsStorage[F, RequestCtx, ClientId],
  val listeners: Seq[WsSessionListener[F, ClientId]],
  dsl: Http4sDsl[F[Throwable, _]],
  logger: LogIO2[F],
  izLogger: IzLogger,
  printer: Printer,
)(implicit val AT: Async[F[Throwable, _]]
) {
  import dsl.*

  protected def loggingMiddle(service: HttpRoutes[F[Throwable, _]]): HttpRoutes[F[Throwable, _]] = cats.data.Kleisli {
    (req: Request[F[Throwable, _]]) =>
      izLogger.trace(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: initiated")

      try {
        service(req).map {
          case Status.Successful(resp) =>
            izLogger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: success, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            resp
          case resp if resp.attributes.lookup(org.http4s.server.websocket.websocketKey[F[Throwable, _]]).isDefined =>
            izLogger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: websocket request")
            resp
          case resp =>
            izLogger.info(s"${req.method.name -> "method"} ${req.pathInfo -> "uri"}: rejection, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            resp
        }
      } catch {
        case cause: Throwable =>
          izLogger.error(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: failure, $cause")
          throw cause
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
        .evalMap(_ => F.sync(izLogger.debug("WS Server: Sending ping frame.")).as(WebSocketFrame.Ping()))
    }
    for {
      outQueue           <- Queue.unbounded[F[Throwable, _], WebSocketFrame]
      listenersWithGlobal = listeners.prepended(globalWsListener)
      context             = new WsClientSessionImpl(outQueue, initialContext, listenersWithGlobal, wsSessionStorage, printer, logger)
      _                  <- context.start()
      outStream           = Stream.fromQueueUnterminated(outQueue).merge(pingStream)
      inStream = {
        (inputStream: Stream[F[Throwable, _], WebSocketFrame]) =>
          inputStream.evalMap {
            handleWsFrame(context)(_).flatMap {
              case Some(v) => outQueue.offer(WebSocketFrame.Text(v))
              case None    => F.unit
            }
          }
      }
      response <- ws.withOnClose(handleWsClose(context)).build(outStream, inStream)
    } yield response
  }

  protected def onHeartbeat(requestTime: ZonedDateTime): F[Throwable, Unit] = {
    requestTime.discard()
    F.unit
  }

  protected def handleWsFrame(
    context: WsClientSession[F, RequestCtx, ClientId],
    requestTime: ZonedDateTime = IzTime.utcNow,
  )(frame: WebSocketFrame
  ): F[Throwable, Option[String]] = {
    (frame match {
      case WebSocketFrame.Text(msg, _) => makeWsResponse(context, msg).sandboxExit.flatMap(handleWsResult(context, _))
      case WebSocketFrame.Close(_)     => F.pure(None)
      case v: WebSocketFrame.Binary    => handleWsError(context, List.empty, Some(v.toString.take(100) + "..."), "badframe")
      case _: WebSocketFrame.Pong      => onHeartbeat(requestTime).as(None)
      case unknownMessage              => logger.error(s"Cannot handle unknown websocket message $unknownMessage").as(None)
    }).map(_.map(p => printer.print(p.asJson)))
  }

  protected def handleWsResult(
    context: WsClientSession[F, RequestCtx, ClientId],
    result: Exit[Throwable, Option[RpcPacket]],
  ): F[Throwable, Option[RpcPacket]] = {
    result match {
      case Success(v)                => F.pure(v)
      case Error(error, _)           => handleWsError(context, List(error), None, "failure")
      case Termination(cause, _, _)  => handleWsError(context, List(cause), None, "termination")
      case Interruption(cause, _, _) => handleWsError(context, List(cause), None, "interruption")
    }
  }

  protected def makeWsResponse(context: WsClientSession[F, RequestCtx, ClientId], message: String): F[Throwable, Option[RpcPacket]] = {
    for {
      packet <- F.fromEither(io.circe.parser.decode[RpcPacket](message))
      id     <- wsContextProvider.toId(context.initialContext, context.id, packet)
      _      <- context.updateId(id)
      response <- processWsRequest(context, packet).sandbox.catchAll {
        case Exit.Termination(exception, allExceptions, trace) =>
          logger.error(s"${context -> null}: WS processing terminated, $message, $exception, $allExceptions, $trace")
          F.pure(Some(rpc.RpcPacket.rpcFail(packet.id, exception.getMessage)))
        case Exit.Error(exception, trace) =>
          logger.error(s"${context -> null}: WS processing failed, $message, $exception $trace")
          F.pure(Some(rpc.RpcPacket.rpcFail(packet.id, exception.getMessage)))
        case Exit.Interruption(exception, _, trace) =>
          logger.error(s"${context -> null}: WS processing interrupted, $message, $exception $trace")
          F.pure(Some(rpc.RpcPacket.rpcFail(packet.id, exception.getMessage)))
      }
    } yield response
  }

  protected def processWsRequest(context: WsClientSession[F, RequestCtx, ClientId], input: RpcPacket): F[Throwable, Option[RpcPacket]] = {
    input match {
      case RpcPacket(RPCPacketKind.RpcRequest, None, _, _, _, _, _) =>
        wsContextProvider.handleEmptyBodyPacket(context.id, context.initialContext, input).flatMap {
          case (id, eff) =>
            context.updateId(id) *> eff
        }

      case RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
        for {
          userCtx <- wsContextProvider.toContext(context.id, context.initialContext, input)
          _       <- logger.debug(s"${context -> null}: $id, $userCtx")
          methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
          result  <- muxer.doInvoke(data, userCtx, methodId)
          packet <- result match {
            case None       => F.fail(new IRTMissingHandlerException(s"${context -> null}: No rpc handler for $methodId", input))
            case Some(resp) => F.pure(Some(rpc.RpcPacket.rpcResponse(id, resp)))
          }
        } yield packet

      case RpcPacket(RPCPacketKind.RpcResponse, Some(data), _, id, _, _, _) =>
        context.requestState.handleResponse(id, data).as(None)

      case RpcPacket(RPCPacketKind.RpcFail, Some(data), _, Some(id), _, _, _) =>
        context.requestState.respond(id, RawResponse.BadRawResponse()) *>
        F.fail(new IRTGenericFailure(s"Rpc returned failure: $data"))

      case RpcPacket(RPCPacketKind.BuzzResponse, Some(data), _, id, _, _, _) =>
        context.requestState.handleResponse(id, data).as(None)

      case RpcPacket(RPCPacketKind.BuzzFailure, Some(data), _, Some(id), _, _, _) =>
        context.requestState.respond(id, RawResponse.BadRawResponse()) *>
        F.fail(new IRTGenericFailure(s"Buzzer has returned failure: $data"))

      case k =>
        F.fail(new IRTMissingHandlerException(s"Can't handle $k", k))
    }
  }

  protected def handleWsError(
    context: WsClientSession[F, RequestCtx, ClientId],
    causes: List[Throwable],
    data: Option[String],
    kind: String,
  ): F[Nothing, Option[RpcPacket]] = {
    causes.headOption match {
      case Some(cause) =>
        logger
          .error(s"${context -> null}: WS Execution failed, $kind, $data, $cause")
          .as(Some(rpc.RpcPacket.rpcCritical(data.getOrElse(cause.getMessage), kind)))

      case None =>
        logger
          .error(s"${context -> null}: WS Execution failed, $kind, $data")
          .as(Some(rpc.RpcPacket.rpcCritical("?", kind)))
    }
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
      case Success(v) =>
        v match {
          case Some(value) =>
            dsl.Ok(printer.print(value))
          case None =>
            izLogger.warn(s"${context -> null}: No service handler for $method")
            dsl.NotFound()
        }

      case Error(error: circe.Error, trace) =>
        izLogger.info(s"${context -> null}: Parsing failure while handling $method: $error $trace")
        dsl.BadRequest()

      case Error(error: IRTDecodingException, trace) =>
        izLogger.info(s"${context -> null}: Parsing failure while handling $method: $error $trace")
        dsl.BadRequest()

      case Error(error: IRTLimitReachedException, trace) =>
        izLogger.debug(s"${context -> null}: Request failed because of request limit reached $method: $error $trace")
        dsl.TooManyRequests()

      case Error(error: IRTUnathorizedRequestContextException, trace) =>
        izLogger.debug(s"${context -> null}: Request failed because of unexpected request context reached $method: $error $trace")
        // workaarount because implicits conflict
        dsl.Forbidden().map(_.copy(status = dsl.Unauthorized))

      case Error(error, trace) =>
        izLogger.info(s"${context -> null}: Unexpected failure while handling $method: $error $trace")
        dsl.InternalServerError()

      case Termination(_, (cause: IRTHttpFailureException) :: _, trace) =>
        izLogger.debug(s"${context -> null}: Request rejected, $method, ${context.request}, $cause, $trace")
        F.pure(Response(status = cause.status))

      case Termination(_, (cause: RejectedExecutionException) :: _, trace) =>
        izLogger.warn(s"${context -> null}: Not enough capacity to handle $method: $cause $trace")
        dsl.TooManyRequests()

      case Termination(cause, _, trace) =>
        izLogger.error(s"${context -> null}: Execution failed, termination, $method, ${context.request}, $cause, $trace")
        dsl.InternalServerError()

      case Interruption(cause, _, trace) =>
        izLogger.info(s"${context -> null}: Unexpected interruption while handling $method: $cause $trace")
        dsl.InternalServerError()
    }
  }

}
