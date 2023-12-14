package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.{Exit, F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTServicesMultiplexor.InvokeMethodResult

trait IRTServicesMultiplexor[F[_, _], RequestCtx, WsCtx] {
  def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult]
}

object IRTServicesMultiplexor {
  final case class InvokeMethodResult(context: Any, res: Json)
  abstract class InvokeMethodFailure(message: String) extends RuntimeException(s"Method invokation failed: $message.")
  object InvokeMethodFailure {
    final case class ServiceNotFound(serviceId: IRTServiceId) extends InvokeMethodFailure(s"Service $serviceId not found .")
    final case class MethodNotFound(methodId: IRTMethodId) extends InvokeMethodFailure(s"Method $methodId not found .")
    final case class AuthFailed(context: AuthContext) extends InvokeMethodFailure(s"Authorization with $context failed.")
  }

  trait MultiContext[F[_, _]] extends IRTServicesMultiplexor[F, Unit, Unit]
  object MultiContext {
    class Impl[F[+_, +_]: IO2](servicesContexts: Set[IRTServicesMultiplexor.SingleContext[F, ?, ?]]) extends IRTServicesMultiplexor.MultiContext[F] {
      private val services: Map[IRTServiceId, IRTServicesMultiplexor[F, ?, ?]] = {
        servicesContexts.flatMap(c => c.services.map(_.serviceId -> c)).toMap
      }
      def invokeMethodWithAuth(method: IRTMethodId)(authContext: AuthContext, body: Json): F[Throwable, InvokeMethodResult] = {
        F.fromOption(InvokeMethodFailure.ServiceNotFound(method.service))(services.get(method.service))
          .flatMap(_.invokeMethodWithAuth(method)(authContext, body))
      }
    }
  }

  trait SingleContext[F[_, _], RequestCtx, WsCtx] extends IRTServicesMultiplexor[F, RequestCtx, WsCtx] {
    val services: Set[IRTWrappedService[F, RequestCtx]]
  }
  object SingleContext {
    class Impl[F[+_, +_]: IO2, RequestCtx, WsCtx](
      val services: Set[IRTWrappedService[F, RequestCtx]],
      val authenticator: IRTAuthenticator[F, RequestCtx],
    ) extends SingleContext[F, RequestCtx, WsCtx] {
      def methods: Map[IRTMethodId, IRTMethodWrapper[F, RequestCtx]] = services.flatMap(_.allMethods).toMap

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
          request <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(methodId, parsedBody))).flatten.sandbox.catchAll {
            case Exit.Interruption(decodingFailure, _, trace) =>
              F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
            case Exit.Termination(_, exceptions, trace) =>
              F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", exceptions.headOption))
            case Exit.Error(decodingFailure, trace) =>
              F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
          }
          methodInput   = request.value.asInstanceOf[method.signature.Input]
          methodOutput <- F.syncThrowable(method.invoke(context, methodInput)).flatten
          response     <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(methodOutput)))
        } yield response
      }
    }
  }
}
