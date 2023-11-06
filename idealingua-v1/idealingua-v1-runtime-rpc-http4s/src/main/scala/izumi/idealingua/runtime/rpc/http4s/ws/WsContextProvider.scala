package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{Applicative2, F}
import izumi.fundamentals.platform.language.Quirks
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextProvider.WsAuthResult
import izumi.idealingua.runtime.rpc.{RPCPacketKind, RpcPacket}

trait WsContextProvider[F[+_, +_], RequestCtx, ClientId] {
  def toContext(id: WsClientId[ClientId], initial: RequestCtx, packet: RpcPacket): F[Throwable, RequestCtx]

  def toId(initial: RequestCtx, currentId: WsClientId[ClientId], packet: RpcPacket): F[Throwable, Option[ClientId]]

  // TODO: we use this to mangle with authorization but it's dirty
  def handleAuthorizationPacket(id: WsClientId[ClientId], initial: RequestCtx, packet: RpcPacket): F[Throwable, WsAuthResult[ClientId]]
}

object WsContextProvider {

  final case class WsAuthResult[ClientId](client: Option[ClientId], response: RpcPacket)

  class IdContextProvider[F[+_, +_]: Applicative2, RequestCtx, ClientId] extends WsContextProvider[F, RequestCtx, ClientId] {
    override def handleAuthorizationPacket(
      id: WsClientId[ClientId],
      initial: RequestCtx,
      packet: RpcPacket,
    ): F[Throwable, WsAuthResult[ClientId]] = {
      val res = RpcPacket(RPCPacketKind.RpcResponse, None, None, packet.id, None, None, None)
      F.pure(WsAuthResult[ClientId](None, res))
    }

    override def toContext(id: WsClientId[ClientId], initial: RequestCtx, packet: RpcPacket): F[Throwable, RequestCtx] = {
      Quirks.discard(packet, id)
      F.pure(initial)
    }

    override def toId(initial: RequestCtx, currentId: WsClientId[ClientId], packet: RpcPacket): F[Throwable, Option[ClientId]] = {
      Quirks.discard(initial, packet)
      F.pure(None)
    }
  }
}
