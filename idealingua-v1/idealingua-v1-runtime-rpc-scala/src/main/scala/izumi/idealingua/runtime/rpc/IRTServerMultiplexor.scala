package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{Error2, Exit, F, IO2}

trait IRTServerMultiplexor[F[+_, +_], C] {
  self =>
  def allMethods: Set[IRTMethodId]

  def invokeMethod(method: IRTMethodId)(context: C, parsedBody: Json): F[Throwable, Json]

  final def contramap[C2](updateContext: (C2, Json) => F[Throwable, Option[C]])(implicit M: Error2[F]): IRTServerMultiplexor[F, C2] = new IRTServerMultiplexor[F, C2] {
    override def allMethods: Set[IRTMethodId] = self.allMethods
    override def invokeMethod(method: IRTMethodId)(context: C2, parsedBody: Json): F[Throwable, Json] = {
      updateContext(context, parsedBody)
        .fromOption(new IRTUnathorizedRequestContextException(s"Unauthorized $method call. Context: $context."))
        .flatMap(self.invokeMethod(method)(_, parsedBody))
    }
  }
}

object IRTServerMultiplexor {

  def combine[F[+_, +_]: Error2, C](multiplexors: Iterable[IRTServerMultiplexor[F, C]]): IRTServerMultiplexor[F, C] = new IRTServerMultiplexor[F, C] {
    private val all: Map[IRTMethodId, IRTMethodId => (C, Json) => F[Throwable, Json]] = {
      multiplexors.toList.flatMap {
        muxer => muxer.allMethods.map(method => method -> muxer.invokeMethod)
      }.toMap
    }
    override def allMethods: Set[IRTMethodId] = all.keySet
    override def invokeMethod(method: IRTMethodId)(context: C, parsedBody: Json): F[Throwable, Json] = {
      F.fromOption(new IRTMissingHandlerException(s"Method $method not found.", parsedBody))(all.get(method))
        .flatMap(invoke => invoke.apply(method).apply(context, parsedBody))
    }
  }

  class IRTServerMultiplexorImpl[F[+_, +_]: IO2, C](
    services: Set[IRTWrappedService[F, C]]
  ) extends IRTServerMultiplexor[F, C] {
    private val methodToWrapped: Map[IRTMethodId, IRTMethodWrapper[F, C]] = services.flatMap(_.allMethods).toMap

    override def allMethods: Set[IRTMethodId] = methodToWrapped.keySet

    override def invokeMethod(method: IRTMethodId)(context: C, parsedBody: Json): F[Throwable, Json] = {
      F.fromOption(new IRTMissingHandlerException(s"Method $method not found.", parsedBody))(methodToWrapped.get(method))
        .flatMap(invoke(_)(context, parsedBody))
    }

    @inline private[this] def invoke(method: IRTMethodWrapper[F, C])(context: C, parsedBody: Json): F[Throwable, Json] = {
      val methodId = method.signature.id
      for {
        requestBody <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(methodId, parsedBody))).flatten.sandbox.catchAll {
          case Exit.Interruption(decodingFailure, _, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
          case Exit.Termination(_, exceptions, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", exceptions.headOption))
          case Exit.Error(decodingFailure, trace) =>
            F.fail(new IRTDecodingException(s"$methodId: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
        }
        result  <- F.syncThrowable(method.invoke(context, requestBody.value.asInstanceOf[method.signature.Input])).flatten
        encoded <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(result)))
      } yield encoded
    }
  }
}
