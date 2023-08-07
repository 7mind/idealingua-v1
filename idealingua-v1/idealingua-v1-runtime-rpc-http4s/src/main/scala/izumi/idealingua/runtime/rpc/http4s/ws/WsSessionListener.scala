package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.Applicative2

trait WsSessionListener[F[+_, +_], ClientId] {
  def onSessionOpened(context: WsClientId[ClientId]): F[Throwable, Unit]
  def onClientIdUpdate(context: WsClientId[ClientId], old: WsClientId[ClientId]): F[Throwable, Unit]
  def onSessionClosed(context: WsClientId[ClientId]): F[Throwable, Unit]
}

object WsSessionListener {
  def empty[F[+_, +_]: Applicative2, ClientId]: WsSessionListener[F, ClientId] = new WsSessionListener[F, ClientId] {
    import izumi.functional.bio.F
    override def onSessionOpened(context: WsClientId[ClientId]): F[Throwable, Unit]                             = F.unit
    override def onClientIdUpdate(context: WsClientId[ClientId], old: WsClientId[ClientId]): F[Throwable, Unit] = F.unit
    override def onSessionClosed(context: WsClientId[ClientId]): F[Throwable, Unit]                             = F.unit
  }
}
