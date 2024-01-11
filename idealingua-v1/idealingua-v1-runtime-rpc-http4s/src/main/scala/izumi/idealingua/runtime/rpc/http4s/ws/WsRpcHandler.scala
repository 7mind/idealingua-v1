package izumi.idealingua.runtime.rpc.http4s.ws

import io.circe.Json
import izumi.functional.bio.Exit.Success
import izumi.functional.bio.{Exit, F, IO2}
import izumi.fundamentals.platform.language.Quirks.Discarder
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsRpcHandler.WsResponder
import logstage.LogIO2

abstract class WsRpcHandler[F[+_, +_]: IO2, RequestCtx](
  muxer: IRTServerMultiplexor[F, RequestCtx],
  responder: WsResponder[F],
  logger: LogIO2[F],
) {

  /** Update context based on RpcPacket (or extract).
    * Called on each RpcPacket messages before packet handling
    */
  protected def updateRequestCtx(packet: RpcPacket): F[Throwable, RequestCtx]

  def processRpcMessage(message: String): F[Throwable, Option[RpcPacket]] = {
    for {
      packet <- F
        .fromEither(io.circe.parser.decode[RpcPacket](message))
        .leftMap(err => new IRTDecodingException(s"Can not decode Rpc Packet '$message'.\nError: $err."))
      response <- processRpcPacket(packet)
    } yield response
  }

  def processRpcPacket(packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
    for {
      requestCtx <- updateRequestCtx(packet)
      response <- packet match {
        // auth
        case RpcPacket(RPCPacketKind.RpcRequest, None, _, _, _, _, _) =>
          handleAuthRequest(requestCtx, packet)

        case RpcPacket(RPCPacketKind.RpcResponse, None, _, Some(ref), _, _, _) =>
          handleAuthResponse(ref, packet)

        // rpc
        case RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
          handleWsRequest(IRTMethodId(IRTServiceId(service), IRTMethodName(method)), requestCtx, data)(
            onSuccess = RpcPacket.rpcResponse(id, _),
            onFail    = RpcPacket.rpcFail(Some(id), _),
          )

        case RpcPacket(RPCPacketKind.RpcResponse, Some(data), _, Some(ref), _, _, _) =>
          responder.responseWithData(ref, data).as(None)

        case RpcPacket(RPCPacketKind.RpcFail, data, _, Some(ref), _, _, _) =>
          responder.responseWith(ref, RawResponse.BadRawResponse(data)).as(None)

        // buzzer
        case RpcPacket(RPCPacketKind.BuzzRequest, Some(data), Some(id), _, Some(service), Some(method), _) =>
          handleWsRequest(IRTMethodId(IRTServiceId(service), IRTMethodName(method)), requestCtx, data)(
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
    } yield response
  }

  protected def handleWsRequest(
    methodId: IRTMethodId,
    requestCtx: RequestCtx,
    data: Json,
  )(onSuccess: Json => RpcPacket,
    onFail: String => RpcPacket,
  ): F[Throwable, Option[RpcPacket]] = {
    muxer.invokeMethod(methodId)(requestCtx, data).sandboxExit.flatMap {
      case Success(res) =>
        F.pure(Some(onSuccess(res)))

      case Exit.Error(error: IRTMissingHandlerException, trace) =>
        logger
          .error(s"WS Request failed - no method handler for $methodId:\n$error\n$trace")
          .as(Some(onFail("Not Found.")))

      case Exit.Error(error: IRTUnathorizedRequestContextException, trace) =>
        logger
          .warn(s"WS Request failed - unauthorized $methodId call:\n$error\n$trace")
          .as(Some(onFail("Unauthorized.")))

      case Exit.Error(error: IRTDecodingException, trace) =>
        logger
          .warn(s"WS Request failed - decoding failed:\n$error\n$trace")
          .as(Some(onFail("BadRequest.")))

      case Exit.Termination(exception, allExceptions, trace) =>
        logger
          .error(s"WS Request terminated:\n$exception\n$allExceptions\n$trace")
          .as(Some(onFail(exception.getMessage)))

      case Exit.Error(exception, trace) =>
        logger
          .error(s"WS Request unexpectedly failed:\n$exception\n$trace")
          .as(Some(onFail(exception.getMessage)))

      case Exit.Interruption(exception, allExceptions, trace) =>
        logger
          .error(s"WS Request unexpectedly interrupted:\n$exception\n$allExceptions\n$trace")
          .as(Some(onFail(exception.getMessage)))
    }
  }

  protected def handleAuthRequest(requestCtx: RequestCtx, packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
    requestCtx.discard()
    F.pure(Some(RpcPacket(RPCPacketKind.RpcResponse, None, None, packet.id, None, None, None)))
  }

  protected def handleAuthResponse(ref: RpcPacketId, packet: RpcPacket): F[Throwable, Option[RpcPacket]] = {
    packet.discard()
    responder.responseWith(ref, RawResponse.EmptyRawResponse()).as(None)
  }
}

object WsRpcHandler {
  trait WsResponder[F[_, _]] {
    def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit]
    def responseWithData(id: RpcPacketId, data: Json): F[Throwable, Unit]
  }
}
