package izumi.idealingua.runtime.rpc.http4s.ws

import io.circe.Json
import izumi.functional.bio.Exit.Success
import izumi.functional.bio.{Exit, F, IO2}
import izumi.fundamentals.platform.language.Quirks.Discarder
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsRpcHandler.WsClientResponder
import logstage.LogIO2

abstract class WsRpcHandler[F[+_, +_]: IO2, RequestCtx](
  muxer: IRTServerMultiplexor[F, RequestCtx],
  responder: WsClientResponder[F],
  logger: LogIO2[F],
) {

  protected def handlePacket(packet: RpcPacket): F[Throwable, Unit]

  protected def handleAuthRequest(packet: RpcPacket): F[Throwable, Option[RpcPacket]]

  protected def handleAuthResponse(ref: RpcPacketId, packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
    packet.discard()
    responder.responseWith(ref, RawResponse.EmptyRawResponse()).as(None)
  }

  protected def extractContext(packet: RpcPacket): F[Throwable, RequestCtx]

  def processRpcMessage(message: String): F[Nothing, Option[RpcPacket]] = {
    (for {
      packet <- F.fromEither(io.circe.parser.decode[RpcPacket](message))
      _      <- handlePacket(packet)
      response <- packet match {
        // auth
        case RpcPacket(RPCPacketKind.RpcRequest, None, _, _, _, _, _) =>
          handleAuthRequest(packet)

        case RpcPacket(RPCPacketKind.RpcResponse, None, _, Some(ref), _, _, _) =>
          handleAuthResponse(ref, packet)

        // rpc
        case RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
          handleWsRequest(packet, data, IRTMethodId(IRTServiceId(service), IRTMethodName(method)))(
            onSuccess = RpcPacket.rpcResponse(id, _),
            onFail    = RpcPacket.rpcFail(Some(id), _),
          )

        case RpcPacket(RPCPacketKind.RpcResponse, Some(data), _, Some(ref), _, _, _) =>
          responder.responseWithData(ref, data).as(None)

        case RpcPacket(RPCPacketKind.RpcFail, data, _, Some(ref), _, _, _) =>
          responder.responseWith(ref, RawResponse.BadRawResponse(data)).as(None)

        // buzzer
        case RpcPacket(RPCPacketKind.BuzzRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
          handleWsRequest(packet, data, IRTMethodId(IRTServiceId(service), IRTMethodName(method)))(
            onSuccess = RpcPacket.buzzerResponse(id, _),
            onFail    = RpcPacket.buzzerFail(Some(id), _),
          )

        case RpcPacket(RPCPacketKind.BuzzResponse, Some(data), _, Some(ref), _, _, _) =>
          responder.responseWithData(ref, data).as(None)

        case RpcPacket(RPCPacketKind.BuzzFailure, data, _, Some(ref), _, _, _) =>
          responder.responseWith(ref, RawResponse.BadRawResponse(data)).as(None)

        // critical failures
        case RpcPacket(RPCPacketKind.Fail, data, _, Some(ref), _, _, _) =>
          responder.responseWith(ref, RawResponse.BadRawResponse(data)).as(None)

        case RpcPacket(RPCPacketKind.Fail, data, _, None, _, _, _) =>
          logger.error(s"WS request failed: Unknown RPC failure: $data.").as(None)

        // unknown
        case packet =>
          logger
            .error(s"WS request failed: No buzzer client handler for $packet")
            .as(Some(RpcPacket.rpcCritical("No buzzer client handler", packet.ref)))
      }
    } yield response).sandbox.catchAll {
      case Exit.Error(error, _)           => handleWsError(List(error), "errored")
      case Exit.Termination(error, _, _)  => handleWsError(List(error), "terminated")
      case Exit.Interruption(error, _, _) => handleWsError(List(error), "interrupted")
    }
  }

  protected def handleWsRequest(
    input: RpcPacket,
    data: Json,
    methodId: IRTMethodId,
  )(onSuccess: Json => RpcPacket,
    onFail: String => RpcPacket,
  ): F[Throwable, Option[RpcPacket]] = {
    for {
      userCtx <- extractContext(input)
      res <- muxer.doInvoke(data, userCtx, methodId).sandboxExit.flatMap {
        case Success(Some(res)) =>
          F.pure(Some(onSuccess(res)))

        case Success(None) =>
          logger.error(s"WS request errored: No rpc handler for $methodId").as(Some(onFail("No rpc handler.")))

        case Exit.Termination(exception, allExceptions, trace) =>
          logger.error(s"WS request terminated, $exception, $allExceptions, $trace").as(Some(onFail(exception.getMessage)))

        case Exit.Error(exception, trace) =>
          logger.error(s"WS request failed, $exception $trace").as(Some(onFail(exception.getMessage)))

        case Exit.Interruption(exception, allExceptions, trace) =>
          logger.error(s"WS request interrupted, $exception $allExceptions $trace").as(Some(onFail(exception.getMessage)))
      }
    } yield res
  }

  def handleWsError(causes: List[Throwable], message: String): F[Nothing, Option[RpcPacket]] = {
    causes.headOption match {
      case Some(cause) =>
        logger
          .error(s"WS request failed: $message, $cause")
          .as(Some(RpcPacket.rpcCritical(s"$message, cause: $cause", None)))
      case None =>
        logger
          .error(s"WS request failed: $message.")
          .as(Some(RpcPacket.rpcCritical(message, None)))
    }
  }
}

object WsRpcHandler {
  trait WsClientResponder[F[_, _]] {
    def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit]
    def responseWithData(id: RpcPacketId, data: Json): F[Throwable, Unit]
  }
}
