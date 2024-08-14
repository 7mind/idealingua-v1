package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import io.circe.{Json, Printer}
import izumi.functional.bio.Exit.{Error, Success, Termination}
import izumi.functional.bio.UnsafeRun2.FailureHandler
import izumi.functional.bio.impl.{AsyncZio, PrimitivesZio}
import izumi.functional.bio.{Async2, Entropy1, Entropy2, Exit, F, Primitives2, SyncSafe1, Temporal2, UnsafeRun2}
import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.functional.Identity
import izumi.fundamentals.platform.network.IzSockets
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.Http4sTransportTest.{Ctx, IO2R}
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.clients.HttpRpcDispatcher.IRTDispatcherRaw
import izumi.idealingua.runtime.rpc.http4s.clients.{HttpRpcDispatcher, HttpRpcDispatcherFactory, WsRpcDispatcher, WsRpcDispatcherFactory}
import izumi.idealingua.runtime.rpc.http4s.context.{HttpContextExtractor, WsContextExtractor}
import izumi.idealingua.runtime.rpc.http4s.fixtures.TestServices
import izumi.idealingua.runtime.rpc.http4s.ws.{RawResponse, WsClientSession, WsRequestState}
import izumi.logstage.api.routing.{ConfigurableLogRouter, StaticLogRouter}
import izumi.logstage.api.{IzLogger, Log}
import izumi.r2.idealingua.test.generated.*
import logstage.LogIO2
import org.http4s.*
import org.http4s.blaze.server.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.Router
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

final class Http4sTransportTest
  extends Http4sTransportTestBase[zio.IO]()(
    async2         = AsyncZio,
    primitives2    = PrimitivesZio,
    temporal2      = AsyncZio,
    unsafeRun2     = IO2R,
    asyncThrowable = zio.interop.catz.asyncInstance,
    entropy2       = Entropy1.fromImpureEntropy(using Entropy1.Standard, SyncSafe1.fromBIO(using AsyncZio)),
    entropy1       = Entropy1.Standard,
  )

object Http4sTransportTest {
  final val izLogger: IzLogger             = makeLogger()
  final val handler: FailureHandler.Custom = UnsafeRun2.FailureHandler.Custom(message => izLogger.trace(s"Fiber failed: $message"))
  final val IO2R: UnsafeRun2[zio.IO] = UnsafeRun2.createZIO(
    handler = handler,
    customCpuPool = Some(
      zio.Executor.fromJavaExecutor(
        Executors.newFixedThreadPool(2)
      )
    ),
  )

  final class Ctx[F[+_, +_]: Async2: Temporal2: Primitives2: UnsafeRun2](
    implicit asyncThrowable: Async[F[Throwable, _]],
    entropy2: Entropy2[F],
    entropy1: Entropy1[Identity],
  ) {
    private val logger: LogIO2[F]               = LogIO2.fromLogger(izLogger)
    private val printer: Printer                = Printer.noSpaces.copy(dropNullValues = true)
    private val dsl: Http4sDsl[F[Throwable, _]] = Http4sDsl.apply[F[Throwable, _]]

    def badAuth(): Header.ToRaw                   = Authorization(Credentials.Token(AuthScheme.Bearer, "token"))
    def publicAuth(user: String): Header.ToRaw    = Authorization(BasicCredentials(user, "public"))
    def protectedAuth(user: String): Header.ToRaw = Authorization(BasicCredentials(user, "protected"))
    def privateAuth(user: String): Header.ToRaw   = Authorization(BasicCredentials(user, "private"))

    def withServer(f: HttpServerContext[F] => F[Throwable, Unit]): Unit = {
      executeF {
        (for {
          testServices <- Lifecycle.liftF(F.syncThrowable(new TestServices[F](logger)))
          ioService <- Lifecycle.liftF {
            F.syncThrowable {
              new HttpServer[F, AuthContext](
                contextServices      = testServices.Server.contextServices,
                httpContextExtractor = HttpContextExtractor.authContext,
                wsContextExtractor   = WsContextExtractor.authContext,
                wsSessionsStorage    = testServices.Server.wsStorage,
                dsl                  = dsl,
                logger               = logger,
                printer              = printer,
                entropy1             = entropy1,
              )
            }
          }
          addr <- Lifecycle.liftF(F.sync(IzSockets.temporaryServerAddress()))
          port  = addr.getPort
          host  = addr.getHostName
          _ <- Lifecycle.fromCats {
            BlazeServerBuilder[F[Throwable, _]]
              .bindHttp(port, host)
              .withHttpWebSocketApp(ws => Router("/" -> ioService.service(ws)).orNotFound)
              .resource
          }
          execCtx = HttpExecutionContext(global)
          baseUri = Uri(Some(Uri.Scheme.http), Some(Uri.Authority(host = Uri.RegName(host), port = Some(port))))
          wsUri   = Uri.unsafeFromString(s"ws://$host:$port/ws")
        } yield HttpServerContext(baseUri, wsUri, testServices, execCtx, printer, logger, entropy2, entropy1)).use(f)
      }
    }

    def executeF(io: F[Throwable, Unit]): Unit = {
      UnsafeRun2[F].unsafeRunSync(io) match {
        case Success(())              => ()
        case failure: Exit.Failure[?] => throw failure.trace.toThrowable
      }
    }
  }

  final case class HttpServerContext[F[+_, +_]: Async2: Temporal2: Primitives2: UnsafeRun2](
    baseUri: Uri,
    wsUri: Uri,
    testServices: TestServices[F],
    execCtx: HttpExecutionContext,
    printer: Printer,
    logger: LogIO2[F],
    entropy2: Entropy2[F],
    entropy1: Entropy1[Identity],
  )(implicit asyncThrowable: Async[F[Throwable, _]]
  ) {
    val httpClientFactory: HttpRpcDispatcherFactory[F] = {
      new HttpRpcDispatcherFactory[F](testServices.Client.codec, execCtx, printer, logger)
    }
    def httpRpcClientDispatcher(headers: Headers): Lifecycle[F[Throwable, _], HttpRpcDispatcher.IRTDispatcherRaw[F]] = {
      httpClientFactory.dispatcher(baseUri, headers)
    }

    val wsClientFactory: WsRpcDispatcherFactory[F] = {
      new WsRpcDispatcherFactory[F](testServices.Client.codec, printer, logger, izLogger, entropy2, entropy1)
    }
    def wsRpcClientDispatcher(headers: Map[String, String] = Map.empty): Lifecycle[F[Throwable, _], WsRpcDispatcher.IRTDispatcherWs[F]] = {
      wsClientFactory.dispatcherSimple(wsUri, testServices.Client.buzzerMultiplexor, headers)
    }
  }

  private def makeLogger(): IzLogger = {
    val router = ConfigurableLogRouter(
      Log.Level.Debug,
      levels = Map(
        "io.netty"                      -> Log.Level.Error,
        "org.http4s.blaze.channel.nio1" -> Log.Level.Error,
        "org.http4s"                    -> Log.Level.Error,
        "org.asynchttpclient"           -> Log.Level.Error,
      ),
    )

    val out = IzLogger(router)
    StaticLogRouter.instance.setup(router)
    out
  }
}

abstract class Http4sTransportTestBase[F[+_, +_]](
  implicit
  async2: Async2[F],
  primitives2: Primitives2[F],
  temporal2: Temporal2[F],
  unsafeRun2: UnsafeRun2[F],
  asyncThrowable: Async[F[Throwable, _]],
  entropy2: Entropy2[F],
  entropy1: Entropy1[Identity],
) extends AnyWordSpec {
  private val ctx = new Ctx[F]

  import ctx.*
  import fixtures.*

  "Http4s transport" should {
    "support http" in {
      withServer {
        ctx =>
          (for {
            // with credentials
            privateClient   <- ctx.httpRpcClientDispatcher(Headers(privateAuth("user1")))
            protectedClient <- ctx.httpRpcClientDispatcher(Headers(protectedAuth("user2")))
            publicClient    <- ctx.httpRpcClientDispatcher(Headers(publicAuth("user3")))
            publicOrcClient <- ctx.httpRpcClientDispatcher(Headers(publicAuth("orc")))
          } yield for {
            // Private API test
            _ <- new PrivateTestServiceWrappedClient(privateClient)
              .test("test").map(res => assert(res.startsWith("Private")))
            _ <- checkUnauthorizedHttpCall(new PrivateTestServiceWrappedClient(protectedClient).test("test"))
            _ <- checkUnauthorizedHttpCall(new ProtectedTestServiceWrappedClient(publicClient).test("test"))

            // Protected API test
            _ <- new ProtectedTestServiceWrappedClient(protectedClient)
              .test("test").map(res => assert(res.startsWith("Protected")))
            _ <- checkUnauthorizedHttpCall(new ProtectedTestServiceWrappedClient(privateClient).test("test"))
            _ <- checkUnauthorizedHttpCall(new ProtectedTestServiceWrappedClient(publicClient).test("test"))

            // Public API test
            _ <- new GreeterServiceClientWrapped(protectedClient)
              .greet("Protected", "Client").map(res => assert(res == "Hi, Protected Client!"))
            _ <- new GreeterServiceClientWrapped(privateClient)
              .greet("Protected", "Client").map(res => assert(res == "Hi, Protected Client!"))
            greaterClient = new GreeterServiceClientWrapped(publicClient)
            _            <- greaterClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
            _            <- greaterClient.alternative().attempt.map(res => assert(res == Right("value")))

            // server middleware test
            _ <- checkUnauthorizedHttpCall(new GreeterServiceClientWrapped(publicOrcClient).greet("Orc", "Smith"))

            // output middleware test
            _ <- greaterClient.greet("bad", "good").map(res => assert(res == "Hi, *** good!"))

            // bad body test
            _ <- checkBadBody("{}", publicClient)
            _ <- checkBadBody("{unparseable", publicClient)
          } yield ()).useEffect
      }
    }

    "support websockets" in {
      withServer {
        ctx =>
          import ctx.testServices.{Client, Server}
          ctx.wsRpcClientDispatcher().use {
            dispatcher =>
              for {
                publicHeaders     <- F.pure(Map("Authorization" -> publicAuth("user").values.head.value))
                privateHeaders    <- F.pure(Map("Authorization" -> privateAuth("user").values.head.value))
                protectedHeaders  <- F.pure(Map("Authorization" -> protectedAuth("user").values.head.value))
                protectedHeaders2 <- F.pure(Map("Authorization" -> protectedAuth("John").values.head.value))
                badHeaders        <- F.pure(Map("Authorization" -> badAuth().values.head.value))

                publicClient    = new GreeterServiceClientWrapped[F](dispatcher)
                privateClient   = new PrivateTestServiceWrappedClient[F](dispatcher)
                protectedClient = new ProtectedTestServiceWrappedClient[F](dispatcher)

                // session id is set
                sessionId <- F.fromOption(new RuntimeException("Missing Ws Session Id."))(dispatcher.sessionId)
                _         <- Server.wsStorage.getSession(sessionId).fromOption(new RuntimeException("Missing Ws Session."))

                // no dispatchers yet
                _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.publicWsStorage.dispatchersFor(PublicContext("user"), Client.codec).map(b => assert(b.isEmpty))
                // all listeners are empty
                _ = assert(Server.protectedWsListener.connectedContexts.isEmpty)
                _ = assert(Server.privateWsListener.connectedContexts.isEmpty)
                _ = assert(Server.publicWsListener.connectedContexts.isEmpty)

                // public authorization
                _ <- dispatcher.authorize(publicHeaders)
                // protected and private listeners are empty
                _ = assert(Server.protectedWsListener.connectedContexts.isEmpty)
                _ = assert(Server.privateWsListener.connectedContexts.isEmpty)
                _ = assert(Server.publicWsListener.connectedContexts.contains(PublicContext("user")))
                // protected and private sessions are empty
                _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("user"), Client.codec).map(b => assert(b.isEmpty))
                // public dispatcher works as expected
                publicContextBuzzer <- Server.publicWsStorage
                  .dispatchersFor(PublicContext("user"), Client.codec).map(_.headOption)
                  .fromOption(new RuntimeException("Missing Buzzer"))
                _ <- new GreeterServiceClientWrapped(publicContextBuzzer).greet("John", "Buzzer").map(res => assert(res == "Hi, John Buzzer!"))
                _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
                _ <- publicClient.alternative().attempt.map(res => assert(res == Right("value")))
                _ <- checkUnauthorizedWsCall(privateClient.test(""))
                _ <- checkUnauthorizedWsCall(protectedClient.test(""))

                // re-authorize with private
                _ <- dispatcher.authorize(privateHeaders)
                // protected listener is empty
                _ = assert(Server.protectedWsListener.connectedContexts.isEmpty)
                _ = assert(Server.privateWsListener.connectedContexts.contains(PrivateContext("user")))
                _ = assert(Server.publicWsListener.connectedContexts.contains(PublicContext("user")))
                // protected sessions is empty
                _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("user"), Client.codec).map(b => assert(b.nonEmpty))
                _ <- Server.publicWsStorage.dispatchersFor(PublicContext("user"), Client.codec).map(b => assert(b.nonEmpty))
                _ <- privateClient.test("test").map(res => assert(res.startsWith("Private")))
                _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
                _ <- checkUnauthorizedWsCall(protectedClient.test(""))

                // re-authorize with protected
                _ <- dispatcher.authorize(protectedHeaders)
                // private listener is empty
                _ = assert(Server.protectedWsListener.connectedContexts.contains(ProtectedContext("user")))
                _ = assert(Server.privateWsListener.connectedContexts.isEmpty)
                _ = assert(Server.publicWsListener.connectedContexts.contains(PublicContext("user")))
                // private sessions is empty
                _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.nonEmpty))
                _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.publicWsStorage.dispatchersFor(PublicContext("user"), Client.codec).map(b => assert(b.nonEmpty))
                _ <- protectedClient.test("test").map(res => assert(res.startsWith("Protected")))
                _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
                _ <- checkUnauthorizedWsCall(privateClient.test(""))

                // auth session context update
                _ <- dispatcher.authorize(protectedHeaders2)
                // session and listeners notified
                _  = assert(Server.protectedWsListener.connectedContexts.contains(ProtectedContext("John")))
                _  = assert(Server.protectedWsListener.connectedContexts.size == 1)
                _  = assert(Server.publicWsListener.connectedContexts.contains(PublicContext("John")))
                _  = assert(Server.publicWsListener.connectedContexts.size == 1)
                _  = assert(Server.privateWsListener.connectedContexts.isEmpty)
                _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("John"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.publicWsStorage.dispatchersFor(PublicContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.publicWsStorage.dispatchersFor(PublicContext("John"), Client.codec).map(b => assert(b.nonEmpty))
                _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.isEmpty))
                _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("John"), Client.codec).map(b => assert(b.nonEmpty))

                // bad authorization
                _ <- dispatcher.authorize(badHeaders)
                _ <- checkUnauthorizedWsCall(publicClient.alternative())
              } yield ()
          }
      }
    }

    "support websockets request auth" in {
      withServer {
        ctx =>
          import ctx.testServices.{Client, Server}
          for {
            privateHeaders <- F.pure(Map("Authorization" -> privateAuth("user").values.head.value))
            _ <- ctx.wsRpcClientDispatcher(privateHeaders).use {
              dispatcher =>
                val publicClient    = new GreeterServiceClientWrapped[F](dispatcher)
                val privateClient   = new PrivateTestServiceWrappedClient[F](dispatcher)
                val protectedClient = new ProtectedTestServiceWrappedClient[F](dispatcher)
                for {
                  _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.isEmpty))
                  _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("user"), Client.codec).map(b => assert(b.nonEmpty))
                  _ <- Server.publicWsStorage.dispatchersFor(PublicContext("user"), Client.codec).map(b => assert(b.nonEmpty))
                  _  = assert(Server.protectedWsListener.connectedContexts.isEmpty)
                  _  = assert(Server.privateWsListener.connectedContexts.size == 1)
                  _  = assert(Server.publicWsListener.connectedContexts.size == 1)

                  _ <- privateClient.test("test").map(res => assert(res.startsWith("Private")))
                  _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
                  _ <- checkUnauthorizedWsCall(protectedClient.test(""))
                } yield ()
            }
          } yield ()
      }
    }

    "support websockets multiple sessions on same context" in {
      withServer {
        ctx =>
          import ctx.testServices.{Client, Server}
          for {
            privateHeaders <- F.pure(Map("Authorization" -> privateAuth("user").values.head.value))
            _ <- {
              for {
                c1 <- ctx.wsRpcClientDispatcher(privateHeaders)
                c2 <- ctx.wsRpcClientDispatcher(privateHeaders)
              } yield (c1, c2)
            }.use {
              case (_, _) =>
                for {
                  _ <- Server.protectedWsStorage.dispatchersFor(ProtectedContext("user"), Client.codec).map(b => assert(b.isEmpty))
                  _ <- Server.privateWsStorage.dispatchersFor(PrivateContext("user"), Client.codec).map(b => assert(b.size == 2))
                  _ <- Server.publicWsStorage.dispatchersFor(PublicContext("user"), Client.codec).map(b => assert(b.size == 2))
                  _  = assert(Server.protectedWsListener.connected.isEmpty)
                  _  = assert(Server.privateWsListener.connected.size == 2)
                  _  = assert(Server.publicWsListener.connected.size == 2)
                } yield ()
            }
          } yield ()
      }
    }

    "support request state clean" in {
      executeF {
        val rs = new WsRequestState.Default[F]()
        for {
          id1 <- RpcPacketId.random(entropy2)
          id2 <- RpcPacketId.random(entropy2)
          _   <- rs.registerRequest(id1, None, 0.minutes)
          _   <- rs.registerRequest(id2, None, 5.minutes)
          _ <- F.attempt(rs.awaitResponse(id1, 5.seconds)).map {
            case Left(_: IRTMissingHandlerException) => ()
            case other                               => fail(s"Expected IRTMissingHandlerException, but got $other.")
          }
          _   <- rs.responseWith(id2, RawResponse.GoodRawResponse(Json.obj(), IRTMethodId(IRTServiceId(""), IRTMethodName(""))))
          res <- rs.awaitResponse(id2, 5.seconds)
          _    = assert(res.nonEmpty)
        } yield ()
      }
    }

    "support dummy ws client" in {
      // server not used here
      // but we need to construct test contexts
      withServer {
        ctx =>
          import ctx.testServices.{Client, Server}
          val client = new WsClientSession.Dummy[F, AuthContext](
            AuthContext(Headers(publicAuth("user")), None),
            Client.buzzerMultiplexor,
            Server.contextServices.map(_.authorizedWsSessions),
            Server.wsStorage,
            WsContextExtractor.authContext,
            ctx.logger,
            entropy1,
          )
          for {
            _ <- client.start(_ => F.unit)
            _  = assert(Server.protectedWsListener.connected.isEmpty)
            _  = assert(Server.privateWsListener.connected.isEmpty)
            _  = assert(Server.publicWsListener.connected.size == 1)
            dispatcher <- Server.publicWsStorage
              .dispatchersFor(PublicContext("user"), Client.codec).map(_.headOption)
              .fromOption(new RuntimeException("Missing dispatcher"))
            _ <- new GreeterServiceClientWrapped(dispatcher)
              .greet("John", "Buzzer")
              .map(res => assert(res == "Hi, John Buzzer!"))
            _ <- client.finish(_ => F.unit)
            _  = assert(Server.protectedWsListener.connected.isEmpty)
            _  = assert(Server.privateWsListener.connected.isEmpty)
            _  = assert(Server.publicWsListener.connected.isEmpty)
          } yield ()
      }
    }
  }

  def checkUnauthorizedHttpCall[E, A](call: F[E, A]): F[Throwable, Unit] = {
    call.sandboxExit.map {
      case Termination(exception: IRTUnexpectedHttpStatus, _, _) => assert(exception.status == Status.Unauthorized)
      case o                                                     => fail(s"Expected Unauthorized status but got $o")
    }.void
  }

  def checkUnauthorizedWsCall[E, A](call: F[E, A]): F[Throwable, Unit] = {
    call.sandboxExit.map {
      case Termination(f: IRTGenericFailure, _, _) => assert(f.getMessage.contains("""{"cause":"Unauthorized."}"""))
      case o                                       => fail(s"Expected IRTGenericFailure with Unauthorized message but got $o")
    }.void
  }

  def checkBadBody(body: String, disp: IRTDispatcherRaw[F]): F[Nothing, Unit] = {
    disp
      .dispatchRaw(GreeterServiceMethods.greet.id, body).sandboxExit.map {
        case Error(value: IRTUnexpectedHttpStatus, _) => assert(value.status == Status.BadRequest)
        case o                                        => fail(s"Expected IRTUnexpectedHttpStatus with BadRequest but got $o")
      }.void
  }
}
