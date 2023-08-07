package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{Applicative2, F}
import izumi.fundamentals.platform.language.Quirks
import izumi.idealingua.runtime.rpc.RpcPacket

trait WsContextProvider[F[+_, +_], RequestCtx, ClientId] {
  def toContext(id: WsClientId[ClientId], initial: RequestCtx, packet: RpcPacket): F[Throwable, RequestCtx]

  def toId(initial: RequestCtx, currentId: WsClientId[ClientId], packet: RpcPacket): F[Throwable, Option[ClientId]]

  // TODO: we use this to mangle with authorization but it's dirty
  def handleEmptyBodyPacket(id: WsClientId[ClientId], initial: RequestCtx, packet: RpcPacket): F[Throwable, (Option[ClientId], F[Throwable, Option[RpcPacket]])]
}

object WsContextProvider {

  class IdContextProvider[F[+_, +_]: Applicative2, RequestCtx, ClientId] extends WsContextProvider[F, RequestCtx, ClientId] {
    override def handleEmptyBodyPacket(
      id: WsClientId[ClientId],
      initial: RequestCtx,
      packet: RpcPacket,
    ): F[Throwable, (Option[ClientId], F[Throwable, Option[RpcPacket]])] = {
      Quirks.discard(id, initial, packet)
      F.pure((None, F.pure(None)))
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
