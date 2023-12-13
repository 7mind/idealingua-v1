package izumi.idealingua.runtime.rpc.http4s.fixtures

import izumi.functional.bio.IO2
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.IRTServerMultiplexor.IRTServerMultiplexorImpl
import izumi.idealingua.runtime.rpc.http4s.{IRTAuthenticator, IRTContextServices, IRTContextServicesMuxer}
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceServerWrapped}
import izumi.r2.idealingua.test.impls.AbstractGreeterServer

class DummyServices[F[+_, +_]: IO2, Ctx] {

  object Server {
    private val greeterService                              = new AbstractGreeterServer.Impl[F, Ctx]
    private val greeterDispatcher                           = new GreeterServiceServerWrapped(greeterService)
    private val dispatchers: Set[IRTWrappedService[F, Ctx]] = Set(greeterDispatcher).map(d => new DummyAuthorizingDispatcher(d))
    val multiplexor                                         = new IRTServerMultiplexorImpl[F, Ctx](dispatchers)

    private val clients: Set[IRTWrappedClient] = Set(GreeterServiceClientWrapped)
    val codec                                  = new IRTClientMultiplexorImpl[F](clients)
  }

  object Client {
    private val greeterService                               = new AbstractGreeterServer.Impl[F, Unit]
    private val greeterDispatcher                            = new GreeterServiceServerWrapped(greeterService)
    private val dispatchers: Set[IRTWrappedService[F, Unit]] = Set(greeterDispatcher)

    private val clients: Set[IRTWrappedClient] = Set(GreeterServiceClientWrapped)
    val codec: IRTClientMultiplexorImpl[F]     = new IRTClientMultiplexorImpl[F](clients)
    val buzzerMultiplexor: IRTContextServicesMuxer[F] = {
      val contextMuxer    = new IRTServerMultiplexorImpl[F, Unit](dispatchers)
      val contextServices = new IRTContextServices[F, Unit](contextMuxer, IRTAuthenticator.unit, Set.empty)
      new IRTContextServicesMuxer[F](Set(contextServices))
    }
  }
}
