package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{Error2, Exit, F, IO2}

trait IRTServerMethod[F[+_, +_], C] {
  self =>
  def methodId: IRTMethodId
  def invoke(context: C, parsedBody: Json): F[Throwable, Json]

  final def contramap[C2](updateContext: (C2, Json) => F[Throwable, Option[C]])(implicit E: Error2[F]): IRTServerMethod[F, C2] = new IRTServerMethod[F, C2] {
    override def methodId: IRTMethodId = self.methodId
    override def invoke(context: C2, parsedBody: Json): F[Throwable, Json] = {
      updateContext(context, parsedBody)
        .fromOption(new IRTUnathorizedRequestContextException(s"Unauthorized $methodId call. Context: $context."))
        .flatMap(self.invoke(_, parsedBody))
    }
  }
}

object IRTServerMethod {
  def apply[F[+_, +_]: IO2, C](method: IRTMethodWrapper[F, C]): IRTServerMethod[F, C] = FromWrapper.apply(method)

  final case class FromWrapper[F[+_, +_]: IO2, C](method: IRTMethodWrapper[F, C]) extends IRTServerMethod[F, C] {
    override def methodId: IRTMethodId = method.signature.id
    @inline override def invoke(context: C, parsedBody: Json): F[Throwable, Json] = {
      val methodId = method.signature.id
      for {
        requestBody <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(methodId, parsedBody))).flatten.sandbox.catchAll {
          case Exit.Interruption(decodingFailure, _, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON '${parsedBody.noSpaces}'.\nTrace: $trace", Some(decodingFailure)))
          case Exit.Termination(_, exceptions, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON '${parsedBody.noSpaces}'.\nTrace: $trace", exceptions.headOption))
          case Exit.Error(decodingFailure, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON '${parsedBody.noSpaces}'.\nTrace: $trace", Some(decodingFailure)))
        }
        result  <- F.syncThrowable(method.invoke(context, requestBody.value.asInstanceOf[method.signature.Input])).flatten
        encoded <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(result)))
      } yield encoded
    }
  }
}
