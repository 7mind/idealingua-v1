package izumi.idealingua.runtime.rpc.http4s.fixtures

import io.circe.Json
import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.IRTServicesContext.IRTServicesContextImpl
import izumi.idealingua.runtime.rpc.http4s.IRTServicesContextMultiplexor.MultiContext
import izumi.idealingua.runtime.rpc.http4s.fixtures.defs.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionsContext.WsSessionsContextImpl
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionsStorage.WsSessionsStorageImpl
import izumi.idealingua.runtime.rpc.http4s.ws.{WsContextExtractor, WsSessionsContext, WsSessionsStorage}
import izumi.idealingua.runtime.rpc.http4s.{IRTAuthenticator, IRTServicesContext, IRTServicesContextMultiplexor}
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceServerWrapped}
import izumi.r2.idealingua.test.impls.AbstractGreeterServer
import logstage.LogIO2
import org.http4s.BasicCredentials
import org.http4s.headers.Authorization

class TestServices[F[+_, +_]: IO2](
  logger: LogIO2[F]
) {

  object Server {
    final val wsStorage: WsSessionsStorage[F] = new WsSessionsStorageImpl[F](logger)

    // PRIVATE
    private val privateAuth = new IRTAuthenticator[F, PrivateContext] {
      override def authenticate(authContext: IRTAuthenticator.AuthContext, body: Option[Json]): F[Nothing, Option[PrivateContext]] = F.sync {
        authContext.headers.get[Authorization].map(_.credentials).collect {
          case BasicCredentials(user, "private") => PrivateContext(user)
        }
      }
    }
    final val privateWsSession: WsSessionsContext[F, PrivateContext, PrivateContext] = new WsSessionsContextImpl(wsStorage, Set.empty, WsContextExtractor.id)
    final val privateService: IRTWrappedService[F, PrivateContext] = new PrivateTestServiceWrappedServer(new PrivateTestServiceServer[F, PrivateContext] {
      def test(ctx: PrivateContext, str: String): Just[String] = F.pure(s"Private: $str")
    })
    final val privateServices: IRTServicesContext[F, PrivateContext, PrivateContext] = {
      new IRTServicesContextImpl(Set(privateService), privateAuth, privateWsSession)
    }

    // PROTECTED
    private val protectedAuth = new IRTAuthenticator[F, ProtectedContext] {
      override def authenticate(authContext: IRTAuthenticator.AuthContext, body: Option[Json]): F[Nothing, Option[ProtectedContext]] = F.sync {
        authContext.headers.get[Authorization].map(_.credentials).collect {
          case BasicCredentials(user, "protected") => ProtectedContext(user)
        }
      }
    }
    final val protectedWsSession: WsSessionsContext[F, ProtectedContext, ProtectedContext] = {
      new WsSessionsContextImpl(wsStorage, Set.empty, WsContextExtractor.id)
    }
    final val protectedService: IRTWrappedService[F, ProtectedContext] = new ProtectedTestServiceWrappedServer(new ProtectedTestServiceServer[F, ProtectedContext] {
      def test(ctx: ProtectedContext, str: String): Just[String] = F.pure(s"Protected: $str")
    })
    final val protectedServices: IRTServicesContext[F, ProtectedContext, ProtectedContext] = {
      new IRTServicesContextImpl(Set(protectedService), protectedAuth, protectedWsSession)
    }

    // PUBLIC
    private val publicAuth = new IRTAuthenticator[F, PublicContext] {
      override def authenticate(authContext: IRTAuthenticator.AuthContext, body: Option[Json]): F[Nothing, Option[PublicContext]] = F.sync {
        authContext.headers.get[Authorization].map(_.credentials).collect {
          case BasicCredentials(user, _) => PublicContext(user)
        }
      }
    }
    final val publicWsSession: WsSessionsContext[F, PublicContext, PublicContext] = new WsSessionsContextImpl(wsStorage, Set.empty, WsContextExtractor.id)
    final val publicService: IRTWrappedService[F, PublicContext]                  = new GreeterServiceServerWrapped(new AbstractGreeterServer.Impl[F, PublicContext])
    final val publicServices: IRTServicesContext[F, PublicContext, PublicContext] = {
      new IRTServicesContextImpl(Set(publicService), publicAuth, publicWsSession)
    }

    final val contextMuxer: IRTServicesContextMultiplexor[F] = new MultiContext[F](Set(privateServices, protectedServices, publicServices))
  }

  object Client {
    private val greeterService                               = new AbstractGreeterServer.Impl[F, Unit]
    private val greeterDispatcher                            = new GreeterServiceServerWrapped(greeterService)
    private val dispatchers: Set[IRTWrappedService[F, Unit]] = Set(greeterDispatcher)

    private val clients: Set[IRTWrappedClient] = Set(
      GreeterServiceClientWrapped,
      ProtectedTestServiceWrappedClient,
      PrivateTestServiceWrappedClient,
    )
    val codec: IRTClientMultiplexorImpl[F] = new IRTClientMultiplexorImpl[F](clients)
    val buzzerMultiplexor: IRTServicesContextMultiplexor[F] = {
      new IRTServicesContextMultiplexor.Single[F, Unit](dispatchers, IRTAuthenticator.unit)
    }
  }
}
