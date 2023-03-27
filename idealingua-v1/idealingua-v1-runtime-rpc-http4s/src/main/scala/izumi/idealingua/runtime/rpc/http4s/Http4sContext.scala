package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import izumi.functional.bio.{IO2, Temporal2, UnsafeRun2}
import org.http4s.dsl.*

import scala.concurrent.ExecutionContext

/**
  * This is a workaround that replaces usages of type projections with Match Types on Scala 3.
  *
  * See: https://github.com/lampepfl/dotty/issues/13416
  */
trait Http4sContextImpl[C <: Http4sContext] {
  final type BiIO[+E, +A] = GetBiIO[C]#l[E, A]
  final type MonoIO[+A] = GetBiIO[C]#l[Throwable, A]

  type MaterializedStream = GetMaterializedStream[C]
  type RequestContext = GetRequestContext[C]
  type MethodContext = GetMethodContext[C]
  type ClientContext = GetClientContext[C]
  type ClientMethodContext = GetClientMethodContext[C]
  type ClientId = GetClientId[C]

  implicit def F: IO2[GetBiIO[C]#l]
  implicit def FT: Temporal2[GetBiIO[C]#l]
  implicit def CIO: Async[MonoIO]

  def UnsafeRun2: UnsafeRun2[BiIO]
  val dsl: Http4sDsl[MonoIO]

  def clientExecutionContext: ExecutionContext

  final type DECL = C

  final def self: Http4sContextImpl[DECL] = this
}

trait Http4sContext { outer =>
  type BiIO[+E, +A]
  final type MonoIO[+A] = BiIO[Throwable, A]

  type MaterializedStream = String

  type RequestContext

  type MethodContext

  type ClientContext

  type ClientMethodContext

  type ClientId

//  type StreamDecoder = EntityDecoder[MonoIO, MaterializedStream]

//  implicit def F: IO2[BiIO]
//  implicit def FT: Temporal2[BiIO]
//  implicit def CIO: Async[MonoIO]

//  def UnsafeRun2: UnsafeRun2[BiIO]
//  val dsl: Http4sDsl[MonoIO]

//  def clientExecutionContext: ExecutionContext

//  final type DECL = this.type
//
//  final def self: IMPL[DECL] = this

  type Aux[_BiIO[+_, +_], _RequestContext, _MethodContext, _ClientId, _ClientContext, _ClientMethodContext] = Http4sContext {
    type BiIO[+E, +V] = _BiIO[E, V]
    type RequestContext = _RequestContext
    type MethodContext = _MethodContext
    type ClientContext = _ClientContext
    type ClientMethodContext = _ClientMethodContext
    type ClientId = _ClientId
  }

//  /**
//    * This is to prove type equality between a type `C <: Http4sContext` and `c: C`
//    * Scalac treats them as different, even when there's a Singleton upper bound!
//    *
//    * @see details: https://gist.github.com/pshirshov/1273add00d902a6cfebd72426d7ed758
//    * @see dotty: https://github.com/lampepfl/dotty/issues/4583#issuecomment-435382992
//    * @see intellij highlighting fixed: https://youtrack.jetbrains.net/issue/SCL-14533
//    */
//  final type IMPL[C <: Http4sContext] = Aux[C#BiIO, C#RequestContext, C#MethodContext, GetClientId[C], C#ClientContext, C#ClientMethodContext]
}
