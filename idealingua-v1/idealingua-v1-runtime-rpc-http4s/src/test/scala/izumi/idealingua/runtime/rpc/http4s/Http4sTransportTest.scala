package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import io.circe.{Json, Printer}
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.UnsafeRun2.FailureHandler
import izumi.functional.bio.{Async2, Exit, F, Primitives2, Temporal2, UnsafeRun2}
import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.language.Quirks.*
import izumi.fundamentals.platform.network.IzSockets
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.Http4sTransportTest.{Ctx, IO2R}
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.clients.HttpRpcDispatcher.IRTDispatcherRaw
import izumi.idealingua.runtime.rpc.http4s.clients.{HttpRpcDispatcher, HttpRpcDispatcherFactory, WsRpcDispatcher, WsRpcDispatcherFactory}
import izumi.idealingua.runtime.rpc.http4s.context.{HttpContextExtractor, WsContextExtractor}
import izumi.idealingua.runtime.rpc.http4s.fixtures.TestServices
import izumi.idealingua.runtime.rpc.http4s.fixtures.defs.{PrivateTestServiceWrappedClient, ProtectedTestServiceWrappedClient}
import izumi.idealingua.runtime.rpc.http4s.ws.{RawResponse, WsRequestState}
import izumi.logstage.api.routing.{ConfigurableLogRouter, StaticLogRouter}
import izumi.logstage.api.{IzLogger, Log}
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceMethods}
import logstage.LogIO2
import org.http4s.*
import org.http4s.blaze.server.*
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.Router
import org.scalatest.wordspec.AnyWordSpec
import zio.IO
import zio.interop.catz.*

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

final class Http4sTransportTest extends Http4sTransportTestBase[IO]

object Http4sTransportTest {
  final val izLogger: IzLogger             = makeLogger()
  final val handler: FailureHandler.Custom = UnsafeRun2.FailureHandler.Custom(message => izLogger.trace(s"Fiber failed: $message"))
  implicit val IO2R: UnsafeRun2[zio.IO] = UnsafeRun2.createZIO(
    handler = handler,
    customCpuPool = Some(
      zio.Executor.fromJavaExecutor(
        Executors.newFixedThreadPool(2)
      )
    ),
  )
  final class Ctx[F[+_, +_]: Async2: Temporal2: Primitives2: UnsafeRun2](implicit asyncThrowable: Async[F[Throwable, _]]) {
    private val logger: LogIO2[F] = LogIO2.fromLogger(izLogger)
    private val printer: Printer  = Printer.noSpaces.copy(dropNullValues = true)

    final val dsl     = Http4sDsl.apply[F[Throwable, _]]
    final val execCtx = HttpExecutionContext(global)

    final val addr    = IzSockets.temporaryServerAddress()
    final val port    = addr.getPort
    final val host    = addr.getHostName
    final val baseUri = Uri(Some(Uri.Scheme.http), Some(Uri.Authority(host = Uri.RegName(host), port = Some(port))))
    final val wsUri   = Uri.unsafeFromString(s"ws://$host:$port/ws")

    final val demo = new TestServices[F](logger)

    final val ioService = new HttpServer[F, AuthContext](
      contextServices      = demo.Server.contextServices,
      httpContextExtractor = HttpContextExtractor.authContext,
      wsContextExtractor   = WsContextExtractor.authContext,
      wsSessionsStorage    = demo.Server.wsStorage,
      dsl                  = dsl,
      logger               = logger,
      printer              = printer,
    )

    def badAuth(): Header.ToRaw                   = Authorization(Credentials.Token(AuthScheme.Bearer, "token"))
    def publicAuth(user: String): Header.ToRaw    = Authorization(BasicCredentials(user, "public"))
    def protectedAuth(user: String): Header.ToRaw = Authorization(BasicCredentials(user, "protected"))
    def privateAuth(user: String): Header.ToRaw   = Authorization(BasicCredentials(user, "private"))

    val httpClientFactory: HttpRpcDispatcherFactory[F] = {
      new HttpRpcDispatcherFactory[F](demo.Client.codec, execCtx, printer, logger)
    }
    def httpRpcClientDispatcher(headers: Headers): HttpRpcDispatcher.IRTDispatcherRaw[F] = {
      httpClientFactory.dispatcher(baseUri, headers)
    }

    val wsClientFactory: WsRpcDispatcherFactory[F] = {
      new WsRpcDispatcherFactory[F](demo.Client.codec, printer, logger, izLogger)
    }
    def wsRpcClientDispatcher(): Lifecycle[F[Throwable, _], WsRpcDispatcher.IRTDispatcherWs[F]] = {
      wsClientFactory.dispatcher(wsUri, demo.Client.buzzerMultiplexor, WsContextExtractor.unit)
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

abstract class Http4sTransportTestBase[F[+_, +_]: Async2: Primitives2: Temporal2: UnsafeRun2](
  implicit asyncThrowable: Async[F[Throwable, _]]
) extends AnyWordSpec {
  private val ctx = new Ctx[F]

  import ctx.*
  import fixtures.*

  "Http4s transport" should {
    "support http" in {
      withServer {
        for {
          // with credentials
          privateClient   <- F.sync(httpRpcClientDispatcher(Headers(privateAuth("user1"))))
          protectedClient <- F.sync(httpRpcClientDispatcher(Headers(protectedAuth("user2"))))
          publicClient    <- F.sync(httpRpcClientDispatcher(Headers(publicAuth("user3"))))
          publicOrcClient <- F.sync(httpRpcClientDispatcher(Headers(publicAuth("orc"))))

          // Private API test
          _ <- new PrivateTestServiceWrappedClient(privateClient).test("test").map {
            res => assert(res.startsWith("Private"))
          }
          _ <- checkUnauthorizedHttpCall(new PrivateTestServiceWrappedClient(protectedClient).test("test"))
          _ <- checkUnauthorizedHttpCall(new ProtectedTestServiceWrappedClient(publicClient).test("test"))

          // Protected API test
          _ <- new ProtectedTestServiceWrappedClient(protectedClient).test("test").map {
            res => assert(res.startsWith("Protected"))
          }
          _ <- checkUnauthorizedHttpCall(new ProtectedTestServiceWrappedClient(privateClient).test("test"))
          _ <- checkUnauthorizedHttpCall(new ProtectedTestServiceWrappedClient(publicClient).test("test"))

          // Public API test
          _ <- new GreeterServiceClientWrapped(protectedClient)
            .greet("Protected", "Client")
            .map(res => assert(res == "Hi, Protected Client!"))
          _ <- new GreeterServiceClientWrapped(privateClient)
            .greet("Protected", "Client")
            .map(res => assert(res == "Hi, Protected Client!"))
          greaterClient = new GreeterServiceClientWrapped(publicClient)
          _            <- greaterClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
          _            <- greaterClient.alternative().attempt.map(res => assert(res == Right("value")))

          // middleware test
          _ <- checkUnauthorizedHttpCall(new GreeterServiceClientWrapped(publicOrcClient).greet("Orc", "Smith"))

          // bad body test
          _ <- checkBadBody("{}", publicClient)
          _ <- checkBadBody("{unparseable", publicClient)
        } yield ()
      }
    }

    "support websockets" in {
      withServer {
        wsRpcClientDispatcher().use {
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

              // no dispatchers yet
              _ <- demo.Server.protectedWsSession.dispatcherFor(ProtectedContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.privateWsSession.dispatcherFor(PrivateContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.publicWsSession.dispatcherFor(PublicContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              // all listeners are empty
              _ = assert(demo.Server.protectedWsListener.connected.isEmpty)
              _ = assert(demo.Server.privateWsListener.connected.isEmpty)
              _ = assert(demo.Server.publicWsListener.connected.isEmpty)

              // public authorization
              _ <- dispatcher.authorize(publicHeaders)
              // protected and private listeners are empty
              _ = assert(demo.Server.protectedWsListener.connected.isEmpty)
              _ = assert(demo.Server.privateWsListener.connected.isEmpty)
              _ = assert(demo.Server.publicWsListener.connected.contains(PublicContext("user")))
              // protected and private sessions are empty
              _ <- demo.Server.protectedWsSession.dispatcherFor(ProtectedContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.privateWsSession.dispatcherFor(PrivateContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              // public dispatcher works as expected
              publicContextBuzzer <- demo.Server.publicWsSession
                .dispatcherFor(PublicContext("user"), demo.Client.codec)
                .fromOption(new RuntimeException("Missing Buzzer"))
              _ <- new GreeterServiceClientWrapped(publicContextBuzzer).greet("John", "Buzzer").map(res => assert(res == "Hi, John Buzzer!"))
              _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
              _ <- publicClient.alternative().attempt.map(res => assert(res == Right("value")))
              _ <- checkUnauthorizedWsCall(privateClient.test(""))
              _ <- checkUnauthorizedWsCall(protectedClient.test(""))

              // re-authorize with private
              _ <- dispatcher.authorize(privateHeaders)
              // protected listener is empty
              _ = assert(demo.Server.protectedWsListener.connected.isEmpty)
              _ = assert(demo.Server.privateWsListener.connected.contains(PrivateContext("user")))
              _ = assert(demo.Server.publicWsListener.connected.contains(PublicContext("user")))
              // protected sessions is empty
              _ <- demo.Server.protectedWsSession.dispatcherFor(ProtectedContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.privateWsSession.dispatcherFor(PrivateContext("user"), demo.Client.codec).map(b => assert(b.nonEmpty))
              _ <- demo.Server.publicWsSession.dispatcherFor(PublicContext("user"), demo.Client.codec).map(b => assert(b.nonEmpty))
              _ <- privateClient.test("test").map(res => assert(res.startsWith("Private")))
              _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
              _ <- checkUnauthorizedWsCall(protectedClient.test(""))

              // re-authorize with protected
              _ <- dispatcher.authorize(protectedHeaders)
              // private listener is empty
              _ = assert(demo.Server.protectedWsListener.connected.contains(ProtectedContext("user")))
              _ = assert(demo.Server.privateWsListener.connected.isEmpty)
              _ = assert(demo.Server.publicWsListener.connected.contains(PublicContext("user")))
              // private sessions is empty
              _ <- demo.Server.protectedWsSession.dispatcherFor(ProtectedContext("user"), demo.Client.codec).map(b => assert(b.nonEmpty))
              _ <- demo.Server.privateWsSession.dispatcherFor(PrivateContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.publicWsSession.dispatcherFor(PublicContext("user"), demo.Client.codec).map(b => assert(b.nonEmpty))
              _ <- protectedClient.test("test").map(res => assert(res.startsWith("Protected")))
              _ <- publicClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
              _ <- checkUnauthorizedWsCall(privateClient.test(""))

              // auth session context update
              _ <- dispatcher.authorize(protectedHeaders2)
              // session and listeners notified
              _  = assert(demo.Server.protectedWsListener.connected.contains(ProtectedContext("John")))
              _  = assert(demo.Server.protectedWsListener.connected.size == 1)
              _  = assert(demo.Server.publicWsListener.connected.contains(PublicContext("John")))
              _  = assert(demo.Server.publicWsListener.connected.size == 1)
              _  = assert(demo.Server.privateWsListener.connected.isEmpty)
              _ <- demo.Server.privateWsSession.dispatcherFor(PrivateContext("John"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.publicWsSession.dispatcherFor(PublicContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.publicWsSession.dispatcherFor(PublicContext("John"), demo.Client.codec).map(b => assert(b.nonEmpty))
              _ <- demo.Server.protectedWsSession.dispatcherFor(ProtectedContext("user"), demo.Client.codec).map(b => assert(b.isEmpty))
              _ <- demo.Server.protectedWsSession.dispatcherFor(ProtectedContext("John"), demo.Client.codec).map(b => assert(b.nonEmpty))

              // bad authorization
              _ <- dispatcher.authorize(badHeaders)
              _ <- checkUnauthorizedWsCall(publicClient.alternative())
            } yield ()
        }
      }
    }

    "support request state clean" in {
      executeF {
        val rs = new WsRequestState.Default[F]()
        for {
          id1 <- F.pure(RpcPacketId.random())
          id2 <- F.pure(RpcPacketId.random())
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
  }

  def withServer(f: F[Throwable, Any]): Unit = {
    executeF {
      BlazeServerBuilder[F[Throwable, _]]
        .bindHttp(port, host)
        .withHttpWebSocketApp(ws => Router("/" -> ioService.service(ws)).orNotFound)
        .resource
        .use(_ => f)
        .void
    }
  }

  def executeF(io: F[Throwable, Any]): Unit = {
    UnsafeRun2[F].unsafeRunSync(io.void) match {
      case Success(())              => ()
      case failure: Exit.Failure[?] => throw failure.trace.toThrowable
    }
  }

  def checkUnauthorizedHttpCall[E, A](call: F[E, A]): F[Throwable, Unit] = {
    call.sandboxExit.flatMap {
      case Termination(exception: IRTUnexpectedHttpStatus, _, _) =>
        F.sync(assert(exception.status == Status.Unauthorized)).void
      case o =>
        F.fail(new RuntimeException(s"Expected Unauthorized status but got $o"))
    }
  }

  def checkUnauthorizedWsCall[E, A](call: F[E, A]): F[Throwable, Unit] = {
    call.sandboxExit.flatMap {
      case Termination(f: IRTGenericFailure, _, _) if f.getMessage.contains("""{"cause":"Unauthorized."}""") =>
        F.unit
      case o =>
        F.fail(new RuntimeException(s"Expected IRTGenericFailure with Unauthorized message but got $o"))
    }
  }

  def checkBadBody(body: String, disp: IRTDispatcherRaw[F]): F[Nothing, Unit] = {
    disp.dispatchRaw(GreeterServiceMethods.greet.id, body).sandboxExit.map {
      case Error(value: IRTUnexpectedHttpStatus, _) =>
        assert(value.status == Status.BadRequest).discard()
      case Error(value, _) =>
        fail(s"Unexpected error: $value")
      case Success(value) =>
        fail(s"Unexpected success: $value")
      case Termination(exception, _, _) =>
        fail("Unexpected failure", exception)
      case Interruption(value, _, _) =>
        fail(s"Interrupted: $value")
    }
  }
}
