package izumi.idealingua.runtime.rpc.http4s.fixtures

import izumi.functional.bio.IO2
import izumi.idealingua.runtime.rpc.*
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceServerWrapped}
import izumi.r2.idealingua.test.impls.AbstractGreeterServer

class DummyServices[F[+_, +_]: IO2, Ctx] {

  object Server {
    private val greeterService                              = new AbstractGreeterServer.Impl[F, Ctx]
    private val greeterDispatcher                           = new GreeterServiceServerWrapped(greeterService)
    private val dispatchers: Set[IRTWrappedService[F, Ctx]] = Set(greeterDispatcher).map(d => new DummyAuthorizingDispatcher(d))
    val multiplexor                                         = new IRTServerMultiplexorImpl[F, Ctx, Ctx](dispatchers, ContextExtender.id)

    private val clients: Set[IRTWrappedClient] = Set(GreeterServiceClientWrapped)
    val codec                                  = new IRTClientMultiplexorImpl[F](clients)
  }

  object Client {
    private val greeterService                               = new AbstractGreeterServer.Impl[F, Unit]
    private val greeterDispatcher                            = new GreeterServiceServerWrapped(greeterService)
    private val dispatchers: Set[IRTWrappedService[F, Unit]] = Set(greeterDispatcher)

    private val clients: Set[IRTWrappedClient] = Set(GreeterServiceClientWrapped)
    val codec                                  = new IRTClientMultiplexorImpl[F](clients)
    val buzzerMultiplexor                      = new IRTServerMultiplexorImpl[F, Unit, Unit](dispatchers, ContextExtender.id)
  }
}
