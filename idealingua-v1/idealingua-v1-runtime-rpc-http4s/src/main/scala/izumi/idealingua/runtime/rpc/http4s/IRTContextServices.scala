package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{Exit, F, Panic2, Temporal2}
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTContextServices.AuthInvokeResult
import izumi.idealingua.runtime.rpc.http4s.ws.*
import izumi.idealingua.runtime.rpc.{IRTMethodId, IRTServerMultiplexor}

final class IRTContextServices[F[+_, +_], RequestCtx](
  val serverMuxer: IRTServerMultiplexor[F, RequestCtx],
  val authenticator: IRTAuthenticator[F, RequestCtx],
  val wsSessionListeners: Set[WsSessionListener[F, RequestCtx]],
) {
  def doAuthInvoke(
    methodId: IRTMethodId,
    authContext: AuthContext,
    body: Json,
  )(implicit E: Panic2[F]
  ): F[Throwable, AuthInvokeResult[RequestCtx]] = {
    authenticator.authenticate(authContext).attempt.flatMap {
      case Right(Some(context)) =>
        serverMuxer.doInvoke(body, context, methodId).sandboxExit.map(AuthInvokeResult.Success(context, _))
      case _ =>
        F.pure(AuthInvokeResult.Failed)
    }
  }

  def onWsSessionUpdate(
    wsSessionId: WsSessionId,
    authContext: AuthContext,
  )(implicit E: Panic2[F]
  ): F[Throwable, Unit] = {
    authenticator.authenticate(authContext).flatMap {
      maybeRequestContext =>
        F.traverse_(wsSessionListeners)(_.onSessionClosed(wsSessionId, maybeRequestContext))
    }
  }
  def onWsSessionOpened(
    wsSessionId: WsSessionId,
    authContext: AuthContext,
  )(implicit E: Panic2[F]
  ): F[Throwable, Unit] = {
    authenticator.authenticate(authContext).flatMap {
      maybeRequestContext =>
        F.traverse_(wsSessionListeners)(_.onSessionOpened(wsSessionId, maybeRequestContext))
    }
  }

  def onWsSessionClosed(
    wsSessionId: WsSessionId,
    authContext: AuthContext,
  )(implicit E: Panic2[F]
  ): F[Throwable, Unit] = {
    authenticator.authenticate(authContext).flatMap {
      maybeRequestContext =>
        F.traverse_(wsSessionListeners)(_.onSessionClosed(wsSessionId, maybeRequestContext))
    }
  }
}

object IRTContextServices {
  sealed trait AuthInvokeResult[+Ctx]
  object AuthInvokeResult {
    final case class Success[Ctx](context: Ctx, invocationResult: Exit[Throwable, Option[Json]]) extends AuthInvokeResult[Ctx]
    case object Failed extends AuthInvokeResult[Nothing]
  }
}
