package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{Error2, Exit, F, IO2}

trait IRTServerMethod[F[+_, +_], C] {
  self =>
  def methodId: IRTMethodId
  def invoke(context: C, parsedBody: Json): F[Throwable, Json]

  /** Contramap eval on context C2 -> C. If context is missing IRTUnathorizedRequestContextException will raise. */
  final def contramap[C2](updateContext: (C2, Json, IRTMethodId) => F[Throwable, Option[C]])(implicit E: Error2[F]): IRTServerMethod[F, C2] = new IRTServerMethod[F, C2] {
    override def methodId: IRTMethodId = self.methodId
    override def invoke(context: C2, parsedBody: Json): F[Throwable, Json] = {
      updateContext(context, parsedBody, methodId)
        .fromOption(new IRTUnathorizedRequestContextException(s"Unauthorized $methodId call. Context: $context."))
        .flatMap(self.invoke(_, parsedBody))
    }
  }

  /** Wrap invocation with function '(Context, Body)(Method.Invoke) => Result' . */
  final def wrap(middleware: (C, Json) => F[Throwable, Json] => F[Throwable, Json]): IRTServerMethod[F, C] = new IRTServerMethod[F, C] {
    override def methodId: IRTMethodId = self.methodId
    override def invoke(context: C, parsedBody: Json): F[Throwable, Json] = {
      middleware(context, parsedBody)(self.invoke(context, parsedBody))
    }
  }
}

object IRTServerMethod {
  def apply[F[+_, +_]: IO2, C](
    method: IRTMethodWrapper[F, C],
    middleware: IRTOutputMiddleware[F, C],
  ): IRTServerMethod[F, C] = FromWrapper.apply(method, middleware)

  final case class FromWrapper[F[+_, +_]: IO2, C](
    method: IRTMethodWrapper[F, C],
    middleware: IRTOutputMiddleware[F, C],
  ) extends IRTServerMethod[F, C] {
    override def methodId: IRTMethodId = method.signature.id
    @inline override def invoke(context: C, parsedBody: Json): F[Throwable, Json] = {
      val methodId = method.signature.id
      for {
        requestBody <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(methodId, parsedBody))).flatten.sandbox.catchAll {
          case Exit.Termination(_, exceptions, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON '${parsedBody.noSpaces}'.\nTrace: $trace", exceptions.headOption))
          case Exit.Error(decodingFailure, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON '${parsedBody.noSpaces}'.\nTrace: $trace", Some(decodingFailure)))
        }
        result  <- F.syncThrowable(method.invoke(context, requestBody.value.asInstanceOf[method.signature.Input])).flatten
        response = IRTResBody(result)
        encoded <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(result)))
        result  <- middleware.apply(methodId)(context, response, encoded)
      } yield result
    }
  }
}
