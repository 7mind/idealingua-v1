package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.functional.bio.{Exit, F}
import izumi.fundamentals.platform.language.Quirks.*
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.clients.HttpRpcDispatcher.IRTDispatcherRaw
import izumi.idealingua.runtime.rpc.http4s.ws.{RawResponse, WsRequestState}
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceMethods}
import org.http4s.*
import org.http4s.blaze.server.*
import org.http4s.headers.Authorization
import org.http4s.server.Router
import org.scalatest.wordspec.AnyWordSpec
import zio.interop.catz.asyncInstance
import zio.{IO, ZIO}

import java.util.Base64
import scala.concurrent.duration.DurationInt

class Http4sTransportTest extends AnyWordSpec {

  import fixtures.*
  import Http4sTestContext.*
  import RT.*

  "Http4s transport" should {
    "support http" in {
      withServer {
        for {
          // with credentials
          httpClient1   <- F.sync(httpRpcClientDispatcher(Headers(Authorization(BasicCredentials("user", "pass")))))
          greeterClient1 = new GreeterServiceClientWrapped(httpClient1)
          _             <- greeterClient1.greet("John", "Smith").map(res => assert(res == "Hi, John Smith!"))
          _             <- greeterClient1.alternative().either.map(res => assert(res == Right("value")))
          _             <- checkBadBody("{}", httpClient1)
          _             <- checkBadBody("{unparseable", httpClient1)

          // without credentials
          greeterClient2 <- F.sync(httpRpcClientDispatcher(Headers())).map(new GreeterServiceClientWrapped(_))
          _ <- F.sandboxExit(greeterClient2.alternative()).map {
            case Termination(exception: IRTUnexpectedHttpStatus, _, _) => assert(exception.status == Status.Forbidden)
            case o                                                     => fail(s"Expected IRTGenericFailure but got $o")
          }

          // with bad credentials
          greeterClient2 <- F.sync(httpRpcClientDispatcher(Headers(Authorization(BasicCredentials("user", "badpass"))))).map(new GreeterServiceClientWrapped(_))
          _ <- F.sandboxExit(greeterClient2.alternative()).map {
            case Termination(exception: IRTUnexpectedHttpStatus, _, _) => assert(exception.status == Status.Unauthorized)
            case o                                                     => fail(s"Expected IRTGenericFailure but got $o")
          }
        } yield ()
      }
    }

    "support websockets" in {
      withServer {
        wsRpcClientDispatcher().use {
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
                case o                                       => F.fail(s"Expected IRTGenericFailure but got $o")
              }
            } yield ()
        }
      }
    }

    "support request state clean" in {
      executeIO {
        val rs = new WsRequestState[IO]()
        for {
          id1 <- ZIO.succeed(RpcPacketId.random())
          id2 <- ZIO.succeed(RpcPacketId.random())
          _   <- rs.requestEmpty(id1, 0.minutes)
          _   <- rs.requestEmpty(id2)
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

  def withServer(f: IO[Throwable, Any]): Unit = {
    executeIO {
      BlazeServerBuilder[IO[Throwable, _]]
        .bindHttp(port, host)
        .withHttpWebSocketApp(ws => Router("/" -> ioService.service(ws)).orNotFound)
        .resource
        .use(_ => f)
        .unit
    }
  }

  def executeIO(io: IO[Throwable, Any]): Unit = {
    IO2R.unsafeRunSync(io.unit) match {
      case Success(())              => ()
      case failure: Exit.Failure[_] => throw failure.trace.toThrowable
    }
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
