package izumi.idealingua.runtime.rpc.http4s

import _root_.io.circe.parser.*
import fs2.Stream
import io.circe
import io.circe.syntax.*
import io.circe.{Json, Printer}
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.{Exit, IO2}
import izumi.fundamentals.platform.language.Quirks.*
import izumi.fundamentals.platform.time.IzTime
import izumi.idealingua.runtime.rpc
import izumi.idealingua.runtime.rpc.*
import izumi.logstage.api.IzLogger
import org.http4s.*
import org.http4s.server.AuthMiddleware
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Binary, Close, Pong, Text}

import java.time.ZonedDateTime
import java.util.concurrent.RejectedExecutionException

class HttpServer[C <: Http4sContext](
  val c: Http4sContextImpl[C],
  val muxer: IRTServerMultiplexor[GetBiIO[C]#l, GetRequestContext[C]],
  val codec: IRTClientMultiplexor[GetBiIO[C]#l],
  val contextProvider: AuthMiddleware[GetMonoIO[C]#l, GetRequestContext[C]],
  val wsContextProvider: WsContextProvider[GetBiIO[C]#l, GetRequestContext[C], GetClientId[C]],
  val wsSessionStorage: WsSessionsStorage[GetBiIO[C]#l, GetClientId[C], GetRequestContext[C]],
  val listeners: Seq[WsSessionListener[GetBiIO[C]#l, GetClientId[C]]],
  logger: IzLogger,
  printer: Printer
) {

  import c.*
  import c.dsl.*

  protected def loggingMiddle(service: HttpRoutes[MonoIO]): HttpRoutes[MonoIO] = cats.data.Kleisli {
    (req: Request[MonoIO]) =>
      logger.trace(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: initiated")

      try {
        service(req).map {
          case Status.Successful(resp) =>
            logger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: success, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            resp
          case resp if resp.attributes.lookup(org.http4s.server.websocket.websocketKey[MonoIO]).isDefined =>
            logger.debug(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: websocket request")
            resp
          case resp =>
            logger.info(s"${req.method.name -> "method"} ${req.pathInfo -> "uri"}: rejection, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
            resp
        }
      } catch {
        case cause: Throwable =>
          logger.error(s"${req.method.name -> "method"} ${req.pathInfo -> "path"}: failure, $cause")
          throw cause
      }
  }

  def service(ws: WebSocketBuilder2[MonoIO]): HttpRoutes[MonoIO] = {
    val svc = AuthedRoutes.of(handler(ws))
    val aservice: HttpRoutes[MonoIO] = contextProvider(svc)
    loggingMiddle(aservice)
  }

  protected def handler(ws: WebSocketBuilder2[MonoIO]): PartialFunction[AuthedRequest[MonoIO, RequestContext], MonoIO[Response[MonoIO]]] = {
    case request @ GET -> Root / "ws" as ctx =>
      val result = setupWs(request, ctx, ws)
      result

    case request @ GET -> Root / service / method as ctx =>
      val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
      run(new HttpRequestContext(request, ctx), body = "{}", methodId)

    case request @ POST -> Root / service / method as ctx =>
      val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
      request.req.decode[String] {
        body =>
          run(new HttpRequestContext(request, ctx), body, methodId)
      }
  }

  protected def handleWsClose(context: WebsocketClientContext[BiIO, ClientId, RequestContext]): MonoIO[Unit] = {
    F.sync(logger.debug(s"${context -> null}: Websocket client disconnected")) *>
    context.finish()
  }

  protected def onWsOpened(): MonoIO[Unit] = F.unit

  protected def onWsUpdate(maybeNewId: Option[ClientId], old: WsClientId[ClientId]): MonoIO[Unit] = F.sync {
    (maybeNewId, old).forget
  }

  protected def onWsClosed(): MonoIO[Unit] = F.unit

  protected def setupWs(request: AuthedRequest[MonoIO, RequestContext], initialContext: RequestContext, ws: WebSocketBuilder2[MonoIO]): MonoIO[Response[MonoIO]] = {
    val context = new WebsocketClientContextImpl[C](c, request, initialContext, listeners, wsSessionStorage, logger) {
      override def onWsSessionOpened(): MonoIO[Unit] = {
        onWsOpened() *> super.onWsSessionOpened()
      }
      override def onWsClientIdUpdate(maybeNewId: Option[ClientId], oldId: WsClientId[ClientId]): MonoIO[Unit] = {
        onWsUpdate(maybeNewId, oldId) *> super.onWsClientIdUpdate(maybeNewId, oldId)
      }
      override def onWsSessionClosed(): MonoIO[Unit] = {
        onWsClosed() *> super.onWsSessionClosed()
      }
    }
    for {
      _ <- context.start()
      _ <- F.sync(logger.debug(s"${context -> null}: Websocket client connected"))
      response <- context.queue.flatMap[Throwable, Response[MonoIO]] {
        q =>
          val dequeueStream = Stream.fromQueueUnterminated(q).through {
            stream =>
              stream
                .evalMap(handleWsMessage(context))
                .collect { case Some(v) => WebSocketFrame.Text(v) }
          }
          ws.withOnClose(onClose = handleWsClose(context))
            .build(
              send = dequeueStream.merge(context.outStream).merge(context.pingStream),
              receive = _.enqueueUnterminated(q),
            )
      }
    } yield response
  }

  protected def handleWsMessage(context: WebsocketClientContextImpl[C], requestTime: ZonedDateTime = IzTime.utcNow): WebSocketFrame => MonoIO[Option[String]] = {
    case Text(msg, _) =>
      makeResponse(context, msg).sandboxExit
        .map(handleResult(context, _))

    case Close(_) =>
      F.pure(None)

    case v: Binary =>
      F.pure(Some(handleWsError(context, List.empty, Some(v.toString.take(100) + "..."), "badframe")))

    case _: Pong =>
      onHeartbeat(requestTime).map(_ => None)

    case unknownMessage =>
      logger.error(s"Cannot handle unknown websocket message $unknownMessage")
      F.pure(None)
  }

  def onHeartbeat(requestTime: ZonedDateTime): MonoIO[Unit] = {
    requestTime.discard()
    F.unit
  }

  protected def handleResult(context: WebsocketClientContextImpl[C], result: Exit[Throwable, Option[RpcPacket]]): Option[String] = {
    result match {
      case Success(v) =>
        v.map(_.asJson).map(printer.print)

      case Error(error, _) =>
        Some(handleWsError(context, List(error), None, "failure"))

      case Termination(cause, _, _) =>
        Some(handleWsError(context, List(cause), None, "termination"))

      case Interruption(cause, _, _)=>
        Some(handleWsError(context, List(cause), None, "interruption"))
    }
  }

  protected def makeResponse(context: WebsocketClientContextImpl[C], message: String): BiIO[Throwable, Option[RpcPacket]] = {
    for {
      parsed <- F.fromEither(parse(message))
      unmarshalled <- F.fromEither(parsed.as[RpcPacket])
      id <- wsContextProvider.toId(context.initialContext, context.id, unmarshalled)
      _ <- context.updateId(id)
      response <- respond(context, unmarshalled).sandbox.catchAll {
        case Exit.Termination(exception, allExceptions, trace) =>
          logger.error(s"${context -> null}: WS processing terminated, $message, $exception, $allExceptions, $trace")
          F.pure(Some(rpc.RpcPacket.rpcFail(unmarshalled.id, exception.getMessage)))
        case Exit.Error(exception, trace) =>
          logger.error(s"${context -> null}: WS processing failed, $message, $exception $trace")
          F.pure(Some(rpc.RpcPacket.rpcFail(unmarshalled.id, exception.getMessage)))
        case Exit.Interruption(exception, _, trace) =>
          logger.error(s"${context -> null}: WS processing interrupted, $message, $exception $trace")
          F.pure(Some(rpc.RpcPacket.rpcFail(unmarshalled.id, exception.getMessage)))
      }
    } yield response
  }

  protected def respond(context: WebsocketClientContextImpl[C], input: RpcPacket): BiIO[Throwable, Option[RpcPacket]] = {
    input match {
      case RpcPacket(RPCPacketKind.RpcRequest, None, _, _, _, _, _) =>
        wsContextProvider.handleEmptyBodyPacket(context.id, context.initialContext, input).flatMap {
          case (id, eff) =>
            context.updateId(id) *> eff
        }

      case RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
        val methodId = IRTMethodId(IRTServiceId(service), IRTMethodName(method))
        for {
          userCtx <- wsContextProvider.toContext(context.id, context.initialContext, input)
          _ <- F.sync(logger.debug(s"${context -> null}: $id, $userCtx"))
          result <- muxer.doInvoke(data, userCtx, methodId)
          packet <- result match {
            case None =>
              F.fail(new IRTMissingHandlerException(s"${context -> null}: No rpc handler for $methodId", input))
            case Some(resp) =>
              F.pure(rpc.RpcPacket.rpcResponse(id, resp))
          }
        } yield {
          Some(packet)
        }

      case RpcPacket(RPCPacketKind.BuzzResponse, Some(data), _, id, _, _, _) =>
        context.requestState.handleResponse(id, data).as(None)

      case RpcPacket(RPCPacketKind.BuzzFailure, Some(data), _, Some(id), _, _, _) =>
        F.sync(context.requestState.respond(id, RawResponse.BadRawResponse())) *>
        F.fail(new IRTGenericFailure(s"Buzzer has returned failure: $data"))

      case k =>
        F.fail(new IRTMissingHandlerException(s"Can't handle $k", k))
    }
  }

  protected def handleWsError(context: WebsocketClientContextImpl[C], causes: List[Throwable], data: Option[String], kind: String): String = {
    causes.headOption match {
      case Some(cause) =>
        logger.error(s"${context -> null}: WS Execution failed, $kind, $data, $cause")
        printer.print(rpc.RpcPacket.rpcCritical(data.getOrElse(cause.getMessage), kind).asJson)

      case None =>
        logger.error(s"${context -> null}: WS Execution failed, $kind, $data")
        printer.print(rpc.RpcPacket.rpcCritical("?", kind).asJson)
    }
  }

  protected def run(context: HttpRequestContext[MonoIO, RequestContext], body: String, method: IRTMethodId): MonoIO[Response[MonoIO]] = {
    val ioR = for {
      parsed <- F.fromEither(parse(body))
      maybeResult <- muxer.doInvoke(parsed, context.context, method)
    } yield {
      maybeResult
    }

    ioR.sandboxExit
      .flatMap(handleResult(context, method, _))
  }

  private def handleResult(
    context: HttpRequestContext[MonoIO, RequestContext],
    method: IRTMethodId,
    result: Exit[Throwable, Option[Json]]
  ): MonoIO[Response[MonoIO]] = {
    result match {
      case Success(v) =>
        v match {
          case Some(value) =>
            dsl.Ok(printer.print(value))
          case None =>
            logger.warn(s"${context -> null}: No service handler for $method")
            dsl.NotFound()
        }

      case Error(error: circe.Error, trace) =>
        logger.info(s"${context -> null}: Parsing failure while handling $method: $error $trace")
        dsl.BadRequest()

      case Error(error: IRTDecodingException, trace) =>
        logger.info(s"${context -> null}: Parsing failure while handling $method: $error $trace")
        dsl.BadRequest()

      case Error(error: IRTLimitReachedException, trace) =>
        logger.debug(s"${context -> null}: Request failed because of request limit reached $method: $error $trace")
        dsl.TooManyRequests()

      case Error(error: IRTUnathorizedRequestContextException, trace) =>
        logger.debug(s"${context -> null}: Request failed because of unexpected request context reached $method: $error $trace")
        // workaarount because implicits conflict
        dsl.Forbidden().map(_.copy(status = dsl.Unauthorized))

      case Error(error, trace) =>
        logger.info(s"${context -> null}: Unexpected failure while handling $method: $error $trace")
        dsl.InternalServerError()

      case Termination(_, (cause: IRTHttpFailureException) :: _, trace) =>
        logger.debug(s"${context -> null}: Request rejected, $method, ${context.request}, $cause, $trace")
        F.pure(Response(status = cause.status))

      case Termination(_, (cause: RejectedExecutionException) :: _, trace) =>
        logger.warn(s"${context -> null}: Not enough capacity to handle $method: $cause $trace")
        dsl.TooManyRequests()

      case Termination(cause, _, trace) =>
        logger.error(s"${context -> null}: Execution failed, termination, $method, ${context.request}, $cause, $trace")
        dsl.InternalServerError()

      case Interruption(cause, _, trace) =>
        logger.info(s"${context -> null}: Unexpected interruption while handling $method: $cause $trace")
        dsl.InternalServerError()
    }
  }

}
