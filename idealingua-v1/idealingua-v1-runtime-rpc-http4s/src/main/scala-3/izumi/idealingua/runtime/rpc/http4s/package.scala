package izumi.idealingua.runtime.rpc

package object http4s {

  /**
    * This is a workaround that replaces usages of type projections with Match Types on Scala 3.
    *
    * See: https://github.com/lampepfl/dotty/issues/13416
    */
  type GetBiIO[C <: Http4sContext] = { type l[+E, +A] = C match {
    case GetBiIOPattern[f] => f[E, A]
  } }
  type GetMonoIO[C <: Http4sContext] = { type l[+A] = C match {
    case GetBiIOPattern[f] => f[Throwable, A]
  } }
  type GetMaterializedStream[C <: Http4sContext] = C match {case GetMaterializedStreamPattern[t] => t}
  type GetRequestContext[C <: Http4sContext] = C match {case GetRequestContextPattern[t] => t}
  type GetClientContext[C <: Http4sContext] = C match {case GetClientContextPattern[t] => t}
  type GetMethodContext[C <: Http4sContext] = C match {case GetMethodContextPattern[t] => t}
  type GetClientMethodContext[C <: Http4sContext] = C match {case GetClientMethodContextPattern[t] => t}
  type GetClientId[C <: Http4sContext] = C match {case GetClientIdPattern[t] => t}

  type GetBiIOPattern[F[+_, +_]] = Http4sContext { type BiIO[+E, +A] = F[E, A] }
  type GetMaterializedStreamPattern[T] = Http4sContext { type MaterializedStream = T }
  type GetRequestContextPattern[T] = Http4sContext { type RequestContext = T }
  type GetClientContextPattern[T] = Http4sContext { type ClientContext = T }
  type GetMethodContextPattern[T] = Http4sContext { type MethodContext = T }
  type GetClientMethodContextPattern[T] = Http4sContext { type ClientMethodContext = T }
  type GetClientIdPattern[T] = Http4sContext { type ClientId = T }

}
