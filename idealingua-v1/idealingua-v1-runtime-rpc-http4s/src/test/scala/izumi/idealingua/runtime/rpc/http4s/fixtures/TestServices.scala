package izumi.idealingua.runtime.rpc.http4s.fixtures

import io.circe.Json
import izumi.functional.bio.{F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.fixtures.defs.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextSessions.WsContextSessionsImpl
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionsStorage.WsSessionsStorageImpl
import izumi.idealingua.runtime.rpc.http4s.ws.{WsContextExtractor, WsContextSessions, WsSessionsStorage}
import izumi.idealingua.runtime.rpc.http4s.{IRTAuthenticator, IRTServicesMultiplexor}
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
    final val privateWsSession: WsContextSessions[F, PrivateContext, PrivateContext] = new WsContextSessionsImpl(privateAuth, wsStorage, Set.empty, WsContextExtractor.id)
    final val privateService: IRTWrappedService[F, PrivateContext] = new PrivateTestServiceWrappedServer(new PrivateTestServiceServer[F, PrivateContext] {
      def test(ctx: PrivateContext, str: String): Just[String] = F.pure(s"Private: $str")
    })
    final val privateServices: IRTServicesMultiplexor.SingleContext[F, PrivateContext, PrivateContext] = {
      new IRTServicesMultiplexor.SingleContext.Impl(Set(privateService), privateAuth)
    }

    // PROTECTED
    private val protectedAuth = new IRTAuthenticator[F, ProtectedContext] {
      override def authenticate(authContext: IRTAuthenticator.AuthContext, body: Option[Json]): F[Nothing, Option[ProtectedContext]] = F.sync {
        authContext.headers.get[Authorization].map(_.credentials).collect {
          case BasicCredentials(user, "protected") => ProtectedContext(user)
        }
      }
    }
    final val protectedWsSession: WsContextSessions[F, ProtectedContext, ProtectedContext] = {
      new WsContextSessionsImpl(protectedAuth, wsStorage, Set.empty, WsContextExtractor.id)
    }
    final val protectedService: IRTWrappedService[F, ProtectedContext] = new ProtectedTestServiceWrappedServer(new ProtectedTestServiceServer[F, ProtectedContext] {
      def test(ctx: ProtectedContext, str: String): Just[String] = F.pure(s"Protected: $str")
    })
    final val protectedServices: IRTServicesMultiplexor.SingleContext[F, ProtectedContext, ProtectedContext] = {
      new IRTServicesMultiplexor.SingleContext.Impl(Set(protectedService), protectedAuth)
    }

    // PUBLIC
    private val publicAuth = new IRTAuthenticator[F, PublicContext] {
      override def authenticate(authContext: IRTAuthenticator.AuthContext, body: Option[Json]): F[Nothing, Option[PublicContext]] = F.sync {
        authContext.headers.get[Authorization].map(_.credentials).collect {
          case BasicCredentials(user, _) => PublicContext(user)
        }
      }
    }
    final val publicWsSession: WsContextSessions[F, PublicContext, PublicContext] = new WsContextSessionsImpl(publicAuth, wsStorage, Set.empty, WsContextExtractor.id)
    final val publicService: IRTWrappedService[F, PublicContext]                  = new GreeterServiceServerWrapped(new AbstractGreeterServer.Impl[F, PublicContext])
    final val publicServices: IRTServicesMultiplexor.SingleContext[F, PublicContext, PublicContext] = {
      new IRTServicesMultiplexor.SingleContext.Impl(Set(publicService), publicAuth)
    }

    final val contextMuxer: IRTServicesMultiplexor[F, Unit, Unit] = new IRTServicesMultiplexor.MultiContext.Impl(Set(privateServices, protectedServices, publicServices))
    final val wsContextsSessions: Set[WsContextSessions[F, ?, ?]] = Set(privateWsSession, protectedWsSession, publicWsSession)
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
    val buzzerMultiplexor: IRTServicesMultiplexor[F, Unit, Unit] = {
      new IRTServicesMultiplexor.SingleContext.Impl(dispatchers, IRTAuthenticator.unit)
    }
  }
}
