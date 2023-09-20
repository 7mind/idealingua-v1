package izumi.idealingua.runtime.rpc.http4s

import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.F
import izumi.fundamentals.platform.language.Quirks.*
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.clients.HttpDispatcherFactory.IRTDispatcherRaw
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceMethods}
import org.http4s.*
import org.http4s.blaze.server.*
import org.http4s.headers.Authorization
import org.http4s.server.Router
import org.scalatest.wordspec.AnyWordSpec
import zio.interop.catz.asyncInstance
import zio.{IO, ZIO}

import java.util.Base64

class Http4sTransportTest extends AnyWordSpec {

  import fixtures.*
  import Http4sTestContext.*
  import RT.*

  "Http4s transport" should {
    "support http" in {
      withServer {
        for {
          // with credentials
          httpClient1   <- F.sync(httpClient(Headers(Authorization(BasicCredentials("user", "pass")))))
          greeterClient1 = new GreeterServiceClientWrapped(httpClient1)
          _             <- greeterClient1.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
          _             <- greeterClient1.alternative().either.map(res => assert(res == Right("value")))
          _             <- checkBadBody("{}", httpClient1)
          _             <- checkBadBody("{unparseable", httpClient1)

          // without credentials
          greeterClient2 <- F.sync(httpClient(Headers())).map(new GreeterServiceClientWrapped(_))
          _ <- F.sandboxExit(greeterClient2.alternative()).map {
            case Termination(exception: IRTUnexpectedHttpStatus, _, _) => assert(exception.status == Status.Forbidden)
            case o                                                     => fail(s"Expected IRTGenericFailure but got $o")
          }

          // with bad credentials
          greeterClient2 <- F.sync(httpClient(Headers(Authorization(BasicCredentials("user", "badpass"))))).map(new GreeterServiceClientWrapped(_))
          _ <- F.sandboxExit(greeterClient2.alternative()).map {
            case Termination(exception: IRTUnexpectedHttpStatus, _, _) => assert(exception.status == Status.Unauthorized)
            case o                                                     => fail(s"Expected IRTGenericFailure but got $o")
          }
        } yield ()
      }
    }

    "support websockets" in {
      withServer {
        wsClient(Headers.empty).use {
          dispatcher =>
            for {
              id1          <- ZIO.succeed(s"Basic ${Base64.getEncoder.encodeToString("user:pass".getBytes)}")
              _            <- dispatcher.authorize(Map("Authorization" -> id1))
              greeterClient = new GreeterServiceClientWrapped(dispatcher)
              _            <- greeterClient.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
              _            <- greeterClient.alternative().either.map(res => assert(res == Right("value")))
              buzzers      <- ioService.wsSessionStorage.dispatcherForClient(id1)
              _             = assert(buzzers.nonEmpty)
              _ <- ZIO.foreach(buzzers) {
                buzzer =>
                  val client = new GreeterServiceClientWrapped(buzzer)
                  client.greet("John", "Buzzer").map(res => assert(res == "Hi, John Buzzer!"))
              }
              _ <- dispatcher.authorize(Map("Authorization" -> s"Basic ${Base64.getEncoder.encodeToString("user:badpass".getBytes)}"))
              _ <- F.sandboxExit(greeterClient.alternative()).map {
                case Termination(_: IRTGenericFailure, _, _) =>
                case o                                       => fail(s"Expected IRTGenericFailure but got $o")
              }
            } yield ()
        }
      }
    }
  }

  def withServer(f: IO[Throwable, Any]): Unit = {
    val io = BlazeServerBuilder[IO[Throwable, _]]
      .bindHttp(port, host)
      .withHttpWebSocketApp(ws => Router("/" -> ioService.service(ws)).orNotFound)
      .resource
      .use(_ => f)
      .unit

    IO2R.unsafeRun(io)
  }

  def checkBadBody(body: String, disp: IRTDispatcherRaw[IO]): ZIO[Any, Nothing, Unit] = {
    F.sandboxExit(disp.dispatchRaw(GreeterServiceMethods.greet.id, body)).map {
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
