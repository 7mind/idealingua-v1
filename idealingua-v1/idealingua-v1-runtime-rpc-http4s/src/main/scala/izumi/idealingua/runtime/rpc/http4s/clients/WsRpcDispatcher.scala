package izumi.idealingua.runtime.rpc.http4s.clients

import io.circe.Json
import izumi.functional.bio.{Async2, F, Temporal2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.clients.WsRpcDispatcher.IRTDispatcherWs
import izumi.idealingua.runtime.rpc.http4s.clients.WsRpcDispatcherFactory.WsRpcClientConnection
import izumi.idealingua.runtime.rpc.http4s.ws.RawResponse
import logstage.LogIO2

import java.util.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration

class WsRpcDispatcher[F[+_, +_]: Async2](
  connection: WsRpcClientConnection[F],
  timeout: FiniteDuration,
  codec: IRTClientMultiplexor[F],
  logger: LogIO2[F],
) extends IRTDispatcherWs[F] {

  override def authorize(headers: Map[String, String]): F[Throwable, Unit] = {
    connection.authorize(headers, timeout)
  }

  override def dispatch(input: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
    dispatchFor(connection, codec)(timeout)(input)
  }

  protected def dispatchFor(
    connection: WsRpcClientConnection[F],
    codec: IRTClientMultiplexor[F],
  )(timeout: FiniteDuration
  )(request: IRTMuxRequest
  ): F[Throwable, IRTMuxResponse] = {
    for {
      _          <- logger.trace(s"${request.method -> "method"}: Going to perform $request")
      encoded    <- codec.encode(request)
      rpcPacketId = RpcPacketId.random()
      rpcPacket   = buildRequest(rpcPacketId, request.method, encoded)
      res <- connection.requestAndAwait(rpcPacketId, rpcPacket, Some(request.method), timeout).flatMap {
        case Some(value: RawResponse.EmptyRawResponse) =>
          F.fail(new IRTGenericFailure(s"${request.method -> "method"}, $rpcPacketId: empty response: $value"))

        case Some(value: RawResponse.GoodRawResponse) =>
          logger.debug(s"${request.method -> "method"}, $rpcPacketId: Have response: $value") *>
          codec.decode(value.data, value.method)

        case Some(value: RawResponse.BadRawResponse) =>
          logger.debug(s"${request.method -> "method"}, $rpcPacketId: Have response: $value") *>
          F.fail(new IRTGenericFailure(s"${request.method -> "method"}, $rpcPacketId: generic failure: ${value.error}"))

        case None =>
          F.fail(new TimeoutException(s"${request.method -> "method"}, $rpcPacketId: No response in $timeout"))
      }
    } yield res
  }

  protected def buildRequest(rpcPacketId: RpcPacketId, method: IRTMethodId, body: Json): RpcPacket = {
    RpcPacket.rpcRequest(rpcPacketId, method, body)
  }
}

object WsRpcDispatcher {
  trait IRTDispatcherWs[F[_, _]] extends IRTDispatcher[F] {
    def authorize(headers: Map[String, String]): F[Throwable, Unit]
  }
}
