package izumi.idealingua.runtime.rpc.http4s

import cats.data.OptionT
import cats.effect.Async
import cats.effect.std.Queue
import fs2.Stream
import io.circe
import io.circe.Printer
import io.circe.syntax.EncoderOps
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.{Exit, F, IO2, Primitives2, Temporal2, UnsafeRun2}
import izumi.fundamentals.platform.language.Quirks
import izumi.fundamentals.platform.time.IzTime
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.HttpServer.{ServerWsRpcHandler, WsResponseMarker}
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTServicesContext.{InvokeMethodFailure, InvokeMethodResult}
import izumi.idealingua.runtime.rpc.http4s.ws.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsClientSession.WsClientSessionImpl
import logstage.LogIO2
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`X-Forwarded-For`
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.typelevel.ci.CIString
import org.typelevel.vault.Key

import java.time.ZonedDateTime
import java.util.concurrent.RejectedExecutionException
import scala.concurrent.duration.DurationInt

class HttpServer[F[+_, +_]: IO2: Temporal2: Primitives2: UnsafeRun2](
  val muxer: IRTServicesContextMultiplexor[F],
  val wsSessionsStorage: WsSessionsStorage[F],
  dsl: Http4sDsl[F[Throwable, _]],
  logger: LogIO2[F],
  printer: Printer,
)(implicit val AT: Async[F[Throwable, _]]
) {
  import dsl.*

  // WS Response attribute key, to differ from usual HTTP responses
  private val wsAttributeKey = UnsafeRun2[F].unsafeRun(Key.newKey[F[Throwable, _], WsResponseMarker.type])

  def service(ws: WebSocketBuilder2[F[Throwable, _]]): HttpRoutes[F[Throwable, _]] = {
    val svc = HttpRoutes.of(router(ws))
    loggingMiddle(svc)
  }

  protected def router(ws: WebSocketBuilder2[F[Throwable, _]]): PartialFunction[Request[F[Throwable, _]], F[Throwable, Response[F[Throwable, _]]]] = {
    case request @ GET -> Root / "ws"              => setupWs(request, ws)
    case request @ GET -> Root / service / method  => processHttpRequest(request, service, method)("{}")
    case request @ POST -> Root / service / method => request.decode[String](processHttpRequest(request, service, method))
  }

  protected def handleWsClose(session: WsClientSession[F]): F[Throwable, Unit] = {
    logger.debug(s"WS Session: Websocket client disconnected ${session.sessionId}.") *>
    session.finish()
  }

  protected def globalWsListener[Ctx, WsCtx]: WsSessionListener[F, Ctx, WsCtx] = new WsSessionListener[F, Ctx, WsCtx] {
    override def onSessionOpened(sessionId: WsSessionId, reqCtx: Ctx, wsCtx: WsCtx): F[Throwable, Unit] = {
      logger.debug(s"WS Session: $sessionId opened $wsCtx on $reqCtx.")
    }
    override def onSessionUpdated(sessionId: WsSessionId, reqCtx: Ctx, prevStx: WsCtx, newCtx: WsCtx): F[Throwable, Unit] = {
      logger.debug(s"WS Session: $sessionId updated $newCtx from $prevStx on $reqCtx.")
    }
    override def onSessionClosed(sessionId: WsSessionId, wsCtx: WsCtx): F[Throwable, Unit] = {
      logger.debug(s"WS Session: $sessionId closed $wsCtx .")
    }
  }

  protected def setupWs(
    request: Request[F[Throwable, _]],
    ws: WebSocketBuilder2[F[Throwable, _]],
  ): F[Throwable, Response[F[Throwable, _]]] = {
    Quirks.discard(request)
    def pingStream: Stream[F[Throwable, _], WebSocketFrame.Ping] = {
      Stream
        .awakeEvery[F[Throwable, _]](5.second)
        .evalMap(_ => logger.debug("WS Server: Sending ping frame.").as(WebSocketFrame.Ping()))
    }
    for {
      outQueue     <- Queue.unbounded[F[Throwable, _], WebSocketFrame]
      authContext  <- F.syncThrowable(extractAuthContext(request))
      clientSession = new WsClientSessionImpl(outQueue, authContext, muxer, wsSessionsStorage, logger, printer)
      _            <- clientSession.start()

      outStream = Stream.fromQueueUnterminated(outQueue).merge(pingStream)
      inStream = {
        (inputStream: Stream[F[Throwable, _], WebSocketFrame]) =>
          inputStream.evalMap {
            processWsRequest(clientSession, IzTime.utcNow)(_).flatMap {
              case Some(v) => outQueue.offer(WebSocketFrame.Text(v))
              case None    => F.unit
            }
          }
      }
      response <- ws.withOnClose(handleWsClose(clientSession)).build(outStream, inStream)
    } yield {
      response.withAttribute(wsAttributeKey, WsResponseMarker)
    }
  }

  protected def processWsRequest(
    clientSession: WsClientSession[F],
    requestTime: ZonedDateTime,
  )(frame: WebSocketFrame
  ): F[Throwable, Option[String]] = {
    (frame match {
      case WebSocketFrame.Text(msg, _) => wsHandler(clientSession).processRpcMessage(msg)
      case WebSocketFrame.Close(_)     => F.pure(None)
      case _: WebSocketFrame.Pong      => onWsHeartbeat(requestTime).as(None)
      case unknownMessage =>
        val message = s"Unsupported WS frame: $unknownMessage."
        logger
          .error(s"WS request failed: $message.")
          .as(Some(RpcPacket.rpcCritical(message, None)))
    }).map(_.map(p => printer.print(p.asJson)))
  }

  protected def wsHandler(clientSession: WsClientSession[F]): WsRpcHandler[F] = {
    new ServerWsRpcHandler(clientSession, muxer, logger)
  }

  protected def onWsHeartbeat(requestTime: ZonedDateTime): F[Throwable, Unit] = {
    logger.debug(s"WS Session: pong frame at $requestTime")
  }

  protected def processHttpRequest(
    request: Request[F[Throwable, _]],
    serviceName: String,
    methodName: String,
  )(body: String
  ): F[Throwable, Response[F[Throwable, _]]] = {
    val methodId = IRTMethodId(IRTServiceId(serviceName), IRTMethodName(methodName))
    (for {
      authContext <- F.syncThrowable(extractAuthContext(request))
      parsedBody  <- F.fromEither(io.circe.parser.parse(body))
      invokeRes   <- muxer.invokeMethodWithAuth(methodId)(authContext, parsedBody)
    } yield invokeRes).sandboxExit.flatMap(handleHttpResult(request, methodId))
  }

  protected def handleHttpResult(
    request: Request[F[Throwable, _]],
    method: IRTMethodId,
  )(result: Exit[Throwable, InvokeMethodResult]
  ): F[Throwable, Response[F[Throwable, _]]] = {
    result match {
      case Success(InvokeMethodResult(_, res)) =>
        Ok(printer.print(res))

      case Error(err: InvokeMethodFailure.ServiceNotFound, _) =>
        logger.warn(s"No service handler for $method: $err") *> NotFound()

      case Error(err: InvokeMethodFailure.MethodNotFound, _) =>
        logger.warn(s"No method handler for $method: $err") *> NotFound()

      case Error(err: InvokeMethodFailure.AuthFailed, _) =>
        logger.warn(s"Auth failed for $method: $err") *> F.pure(Response(Status.Unauthorized))

      case Error(error: circe.Error, trace) =>
        logger.info(s"Parsing failure while handling $method: $error $trace") *>
        BadRequest()

      case Error(error: IRTDecodingException, trace) =>
        logger.info(s"Parsing failure while handling $method: $error $trace") *>
        BadRequest()

      case Error(error: IRTLimitReachedException, trace) =>
        logger.debug(s"$Request failed because of request limit reached $method: $error $trace") *>
        TooManyRequests()

      case Error(error: IRTUnathorizedRequestContextException, trace) =>
        logger.debug(s"$Request failed because of unexpected request context reached $method: $error $trace") *>
        F.pure(Response(status = Status.Unauthorized))

      case Error(error, trace) =>
        logger.info(s"Unexpected failure while handling $method: $error $trace") *>
        InternalServerError()

      case Termination(_, (cause: IRTHttpFailureException) :: _, trace) =>
        logger.debug(s"Request rejected, $method, $request, $cause, $trace") *>
        F.pure(Response(status = cause.status))

      case Termination(_, (cause: RejectedExecutionException) :: _, trace) =>
        logger.warn(s"Not enough capacity to handle $method: $cause $trace") *>
        TooManyRequests()

      case Termination(cause, _, trace) =>
        logger.error(s"Execution failed, termination, $method, $request, $cause, $trace") *>
        InternalServerError()

      case Interruption(cause, _, trace) =>
        logger.info(s"Unexpected interruption while handling $method: $cause $trace") *>
        InternalServerError()
    }
  }

  protected def extractAuthContext(request: Request[F[Throwable, _]]): AuthContext = {
    val networkAddress = request.headers
      .get[`X-Forwarded-For`]
      .flatMap(_.values.head.map(_.toInetAddress))
      .orElse(request.remote.map(_.host.toInetAddress))
    val headers = request.headers
    AuthContext(headers, networkAddress)
  }

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
}

object HttpServer {
  case object WsResponseMarker
  class ServerWsRpcHandler[F[+_, +_]: IO2, RequestCtx, ClientId](
    clientSession: WsClientSession[F],
    contextServicesMuxer: IRTServicesContextMultiplexor[F],
    logger: LogIO2[F],
  ) extends WsRpcHandler[F](contextServicesMuxer, clientSession, logger) {
    override protected def getAuthContext: AuthContext = clientSession.getAuthContext

    override def handlePacket(packet: RpcPacket): F[Throwable, Unit] = {
      F.traverse_(packet.headers) {
        headersMap =>
          val headers     = Headers.apply(headersMap.toSeq.map { case (k, v) => Header.Raw(CIString(k), v) })
          val authContext = AuthContext(headers, None)
          clientSession.updateAuthContext(authContext)
      }
    }

    override protected def handleAuthRequest(packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
      F.pure(Some(RpcPacket(RPCPacketKind.RpcResponse, None, None, packet.id, None, None, None)))
    }
  }
}
