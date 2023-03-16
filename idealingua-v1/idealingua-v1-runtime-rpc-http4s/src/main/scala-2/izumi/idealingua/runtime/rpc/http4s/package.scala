package izumi.idealingua.runtime.rpc

package object http4s {

  /**
    * This is a workaround that replaces usages of type projections with Match Types on Scala 3.
    *
    * See: https://github.com/lampepfl/dotty/issues/13416
    */
  type GetBiIO[C <: Http4sContext] = { type l[+E, +A] = C#BiIO[E, A] }
  type GetMonoIO[C <: Http4sContext] = { type l[+A] = C#BiIO[Throwable, A] }

  type GetMaterializedStream[C <: Http4sContext] = C#MaterializedStream
  type GetRequestContext[C <: Http4sContext] = C#RequestContext
  type GetClientContext[C <: Http4sContext] = C#ClientContext
  type GetMethodContext[C <: Http4sContext] = C#MethodContext
  type GetClientMethodContext[C <: Http4sContext] = C#ClientMethodContext
  type GetClientId[C <: Http4sContext] = C#ClientId
}
