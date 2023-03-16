package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import izumi.functional.bio.{IO2, Temporal2, UnsafeRun2}
import org.http4s.dsl.*

import scala.concurrent.ExecutionContext

class Http4sRuntime[
  _BiIO[+ _, + _]: IO2 : Temporal2 : UnsafeRun2
, _RequestContext
, _MethodContext
, _ClientId
, _ClientContext
, _ClientMethodContext
]
(
  override val clientExecutionContext: ExecutionContext
)(implicit
  C: Async[_BiIO[Throwable, _]]
) extends Http4sContextImpl[Http4sContext#Aux[_BiIO, _RequestContext, _MethodContext, _ClientId, _ClientContext, _ClientMethodContext]] {
  override val F: IO2[BiIO] = implicitly
  override val FT: Temporal2[BiIO] = implicitly
  override val CIO: Async[MonoIO] = implicitly
  override val UnsafeRun2: UnsafeRun2[BiIO] = implicitly
  override val dsl: Http4sDsl[MonoIO] = Http4sDsl[MonoIO]
}
