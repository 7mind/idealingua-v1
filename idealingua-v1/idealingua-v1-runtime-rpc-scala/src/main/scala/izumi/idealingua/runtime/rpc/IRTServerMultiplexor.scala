package izumi.idealingua.runtime.rpc

import izumi.functional.bio.{BIO, BIOExit, F}
import izumi.fundamentals.platform.language.Quirks
import io.circe.Json

trait ContextExtender[-Ctx, +Ctx2] {
  def extend(context: Ctx, body: Json, irtMethodId: IRTMethodId): Ctx2
}

object ContextExtender {
  def id[Ctx]: ContextExtender[Ctx, Ctx] = (context, _, _) => context
}

trait IRTServerMultiplexor[F[+_, +_], -C] {
  def doInvoke(parsedBody: Json, context: C, toInvoke: IRTMethodId): F[Throwable, Option[Json]]
}

class IRTServerMultiplexorImpl[F[+_, +_]: BIO, -C, -C2](
  list: Set[IRTWrappedService[F, C2]],
  extender: ContextExtender[C, C2],
) extends IRTServerMultiplexor[F, C] {
  val services: Map[IRTServiceId, IRTWrappedService[F, C2]] = list.map(s => s.serviceId -> s).toMap

  def doInvoke(parsedBody: Json, context: C, toInvoke: IRTMethodId): F[Throwable, Option[Json]] = {
    (for {
      service <- services.get(toInvoke.service)
      method <- service.allMethods.get(toInvoke)
    } yield method) match {
      case Some(value) =>
        invoke(extender.extend(context, parsedBody, toInvoke), toInvoke, value, parsedBody).map(Some.apply)
      case None =>
        F.pure(None)
    }
  }

  @inline private[this] def invoke(context: C2, toInvoke: IRTMethodId, method: IRTMethodWrapper[F, C2], parsedBody: Json): F[Throwable, Json] = {
    for {
      decodeAction <- F.syncThrowable(method.marshaller.decodeRequest[F].apply(IRTJsonBody(toInvoke, parsedBody)))
      safeDecoded <- decodeAction.sandbox.catchAll {
        case BIOExit.Termination(_, exceptions, trace) =>
          F.fail(new IRTDecodingException(s"$toInvoke: Failed to decode JSON ${parsedBody.toString()} $trace", exceptions.headOption))
        case BIOExit.Error(decodingFailure, trace) =>
          F.fail(new IRTDecodingException(s"$toInvoke: Failed to decode JSON ${parsedBody.toString()} $trace", Some(decodingFailure)))
      }
      casted = safeDecoded.value.asInstanceOf[method.signature.Input]
      resultAction <- F.syncThrowable(method.invoke(context, casted))
      safeResult <- resultAction
      encoded <- F.syncThrowable(method.marshaller.encodeResponse.apply(IRTResBody(safeResult)))
    } yield encoded
  }
}
