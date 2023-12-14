package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTServicesContext.{IRTServicesContextImpl, InvokeMethodFailure, InvokeMethodResult}
import izumi.idealingua.runtime.rpc.http4s.ws.{WsSessionId, WsSessionsContext}

trait IRTServicesContextMultiplexor[F[+_, +_]] {
  def updateWsSession(wsSessionId: WsSessionId, authContext: Option[AuthContext]): F[Throwable, Unit]
  def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult]
}

object IRTServicesContextMultiplexor {
  class Single[F[+_, +_]: IO2, RequestCtx](
    val services: Set[IRTWrappedService[F, RequestCtx]],
    val authenticator: IRTAuthenticator[F, RequestCtx],
  ) extends IRTServicesContextMultiplexor[F] {
    private val inner: IRTServicesContext[F, RequestCtx, Unit] = new IRTServicesContextImpl(services, authenticator, WsSessionsContext.empty)
    override def updateWsSession(wsSessionId: WsSessionId, authContext: Option[AuthContext]): F[Throwable, Unit] = {
      F.unit
    }
    override def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult] = {
      inner.invokeMethodWithAuth(method)(authContext, body)
    }
  }

  class MultiContext[F[+_, +_]: IO2](
    servicesContexts: Set[IRTServicesContext[F, ?, ?]]
  ) extends IRTServicesContextMultiplexor[F] {

    private val services: Map[IRTServiceId, IRTServicesContext[F, ?, ?]] = {
      servicesContexts.flatMap(c => c.services.map(_.serviceId -> c)).toMap
    }

    override def updateWsSession(wsSessionId: WsSessionId, authContext: Option[AuthContext]): F[Throwable, Unit] = {
      F.traverse_(servicesContexts)(_.updateWsSession(wsSessionId, authContext))
    }

    def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult] = {
      F.fromOption(InvokeMethodFailure.ServiceNotFound(method.service))(services.get(method.service))
        .flatMap(_.invokeMethodWithAuth(method)(authContext, body))
    }
  }
}
