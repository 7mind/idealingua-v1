package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{Exit, F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTServicesContext.InvokeMethodResult
import izumi.idealingua.runtime.rpc.http4s.ws.*

trait IRTServicesContext[F[_, _], RequestCtx, WsCtx] {
  val services: Set[IRTWrappedService[F, RequestCtx]]
  def updateWsSession(wsSessionId: WsSessionId, authContext: Option[AuthContext]): F[Throwable, Unit]
  def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult]
}

object IRTServicesContext {
  final case class InvokeMethodResult(context: Any, res: Json)

  abstract class InvokeMethodFailure(message: String) extends RuntimeException(s"Method invokation failed: $message.")
  object InvokeMethodFailure {
    final case class ServiceNotFound(serviceId: IRTServiceId) extends InvokeMethodFailure(s"Service $serviceId not found .")
    final case class MethodNotFound(methodId: IRTMethodId) extends InvokeMethodFailure(s"Method $methodId not found .")
    final case class AuthFailed(context: AuthContext) extends InvokeMethodFailure(s"Authorization with $context failed.")
  }

  final class IRTServicesContextImpl[F[+_, +_]: IO2, RequestCtx, WsCtx](
    val services: Set[IRTWrappedService[F, RequestCtx]],
    val authenticator: IRTAuthenticator[F, RequestCtx],
    val wsContext: WsSessionsContext[F, RequestCtx, WsCtx],
  ) extends IRTServicesContext[F, RequestCtx, WsCtx] {
    def methods: Map[IRTMethodId, IRTMethodWrapper[F, RequestCtx]] = services.flatMap(_.allMethods).toMap

    override def updateWsSession(wsSessionId: WsSessionId, authContext: Option[AuthContext]): F[Throwable, Unit] = {
      F.traverse(authContext)(authenticator.authenticate(_, None)).map(_.flatten).sandboxExit.flatMap {
        case Exit.Success(ctx) => wsContext.updateSession(wsSessionId, ctx)
        case _                 => wsContext.updateSession(wsSessionId, None)
      }
    }

    override def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult] = {
      for {
        wrappedMethod <- F.fromOption(InvokeMethodFailure.MethodNotFound(method))(methods.get(method))
        requestCtx    <- authenticator.authenticate(authContext, Some(body)).fromOption(InvokeMethodFailure.AuthFailed(authContext))
        res           <- invoke(wrappedMethod)(requestCtx, body)
      } yield InvokeMethodResult(requestCtx, res)
    }

    @inline private[this] def invoke[C](method: IRTMethodWrapper[F, C])(context: C, parsedBody: Json): F[Throwable, Json] = {
      val methodId = method.signature.id
      for {
        decodeAction <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(methodId, parsedBody)))
        safeDecoded <- decodeAction.sandbox.catchAll {
          case Exit.Interruption(decodingFailure, _, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
          case Exit.Termination(_, exceptions, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", exceptions.headOption))
          case Exit.Error(decodingFailure, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
        }
        casted        = safeDecoded.value.asInstanceOf[method.signature.Input]
        resultAction <- F.syncThrowable(method.invoke(context, casted))
        safeResult   <- resultAction
        encoded      <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(safeResult)))
      } yield encoded
    }
  }
}
