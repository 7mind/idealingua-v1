package izumi.idealingua.runtime.rpc.http4s.context

import izumi.idealingua.runtime.rpc.RpcPacket
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionId
import org.http4s.{Header, Headers}
import org.typelevel.ci.CIString

trait WsContextExtractor[RequestCtx] {
  def extract(wsSessionId: WsSessionId, packet: RpcPacket): RequestCtx
  def merge(previous: RequestCtx, updated: RequestCtx): RequestCtx
}

object WsContextExtractor {
  def unit: WsContextExtractor[Unit] = new WsContextExtractor[Unit] {
    override def extract(wsSessionId: WsSessionId, packet: RpcPacket): Unit = ()
    override def merge(previous: Unit, updated: Unit): Unit                 = ()
  }
  def authContext: WsContextExtractor[AuthContext] = new WsContextExtractor[AuthContext] {
    override def extract(wsSessionId: WsSessionId, packet: RpcPacket): AuthContext = {
      val headersMap = packet.headers.getOrElse(Map.empty)
      val headers    = Headers.apply(headersMap.toSeq.map { case (k, v) => Header.Raw(CIString(k), v) })
      AuthContext(headers, None)
    }

    override def merge(previous: AuthContext, updated: AuthContext): AuthContext = {
      AuthContext(previous.headers ++ updated.headers, updated.networkAddress.orElse(previous.networkAddress))
    }
  }
}
