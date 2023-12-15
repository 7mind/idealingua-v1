package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{Error2, F, IO2}

trait IRTServerMultiplexor[F[+_, +_], C] {
  self =>
  def methods: Map[IRTMethodId, IRTServerMethod[F, C]]

  def invokeMethod(method: IRTMethodId)(context: C, parsedBody: Json)(implicit E: Error2[F]): F[Throwable, Json] = {
    F.fromOption(new IRTMissingHandlerException(s"Method $method not found.", parsedBody))(methods.get(method))
      .flatMap(_.invoke(context, parsedBody))
  }

  /** Contramap eval on context C2 -> C. If context is missing IRTUnathorizedRequestContextException will raise. */
  final def contramap[C2](
    updateContext: (C2, Json) => F[Throwable, Option[C]]
  )(implicit io2: IO2[F]
  ): IRTServerMultiplexor[F, C2] = {
    val mappedMethods = self.methods.map { case (k, v) => k -> v.contramap(updateContext) }
    new IRTServerMultiplexor.FromMethods(mappedMethods)
  }
  /** Wrap invocation with function '(Context, Body)(Method.Invoke) => Result' . */
  final def wrap(middleware: IRTServerMiddleware[F, C]): IRTServerMultiplexor[F, C] = {
    val wrappedMethods = self.methods.map {
      case (methodId, method) =>
        val wrappedMethod: IRTServerMethod[F, C] = method.wrap {
          case (ctx, body) =>
            next => middleware(method.methodId)(ctx, body)(next)
        }
        methodId -> wrappedMethod
    }
    new IRTServerMultiplexor.FromMethods(wrappedMethods)
  }
}

object IRTServerMultiplexor {
  def combine[F[+_, +_], C](multiplexors: Iterable[IRTServerMultiplexor[F, C]]): IRTServerMultiplexor[F, C] = {
    new FromMethods(multiplexors.flatMap(_.methods).toMap)
  }

  class FromMethods[F[+_, +_], C](val methods: Map[IRTMethodId, IRTServerMethod[F, C]]) extends IRTServerMultiplexor[F, C]

  class FromServices[F[+_, +_]: IO2, C](val services: Set[IRTWrappedService[F, C]])
    extends FromMethods[F, C](services.flatMap(_.allMethods.map { case (k, v) => k -> IRTServerMethod(v) }).toMap)
}
