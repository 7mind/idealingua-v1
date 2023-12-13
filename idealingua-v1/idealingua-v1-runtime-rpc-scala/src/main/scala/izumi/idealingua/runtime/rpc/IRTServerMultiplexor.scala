package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{Exit, F, IO2}

trait IRTServerMultiplexor[F[+_, +_], C] {
  def services: Set[IRTWrappedService[F, C]]
  def doInvoke(parsedBody: Json, context: C, toInvoke: IRTMethodId): F[Throwable, Option[Json]]
}

object IRTServerMultiplexor {
  class IRTServerMultiplexorImpl[F[+_, +_]: IO2, C](
    val services: Set[IRTWrappedService[F, C]]
  ) extends IRTServerMultiplexor[F, C] {
    private val serviceToWrapped: Map[IRTServiceId, IRTWrappedService[F, C]] = {
      services.map(s => s.serviceId -> s).toMap
    }

    def doInvoke(parsedBody: Json, context: C, toInvoke: IRTMethodId): F[Throwable, Option[Json]] = {
      (for {
        service <- serviceToWrapped.get(toInvoke.service)
        method  <- service.allMethods.get(toInvoke)
      } yield method) match {
        case Some(value) =>
          invoke(context, toInvoke, value, parsedBody).map(Some.apply)
        case None =>
          F.pure(None)
      }
    }

    @inline private[this] def invoke(context: C, toInvoke: IRTMethodId, method: IRTMethodWrapper[F, C], parsedBody: Json): F[Throwable, Json] = {
      for {
        decodeAction <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(toInvoke, parsedBody)))
        safeDecoded <- decodeAction.sandbox.catchAll {
          case Exit.Interruption(decodingFailure, _, trace) =>
            F.fail(new IRTDecodingException(s"$toInvoke: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
          case Exit.Termination(_, exceptions, trace) =>
            F.fail(new IRTDecodingException(s"$toInvoke: Failed to decode JSON ${parsedBody.toString()} $trace", exceptions.headOption))
          case Exit.Error(decodingFailure, trace) =>
            F.fail(new IRTDecodingException(s"$toInvoke: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
        }
        casted        = safeDecoded.value.asInstanceOf[method.signature.Input]
        resultAction <- F.syncThrowable(method.invoke(context, casted))
        safeResult   <- resultAction
        encoded      <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(safeResult)))
      } yield encoded
    }
  }
}
