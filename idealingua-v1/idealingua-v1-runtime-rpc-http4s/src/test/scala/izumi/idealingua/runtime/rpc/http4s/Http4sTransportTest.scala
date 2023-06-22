package izumi.idealingua.runtime.rpc.http4s

import izumi.functional.bio.Exit.{Error, Interruption, Success, Termination}
import izumi.fundamentals.platform.language.Quirks.*
import izumi.idealingua.runtime.rpc.*
import izumi.r2.idealingua.test.generated.{GreeterServiceClientWrapped, GreeterServiceMethods}
import org.http4s.*
import org.http4s.blaze.server.*
import org.http4s.server.Router
import org.scalatest.wordspec.AnyWordSpec
import zio.ZIO
import zio.interop.catz.asyncInstance

class Http4sTransportTest extends AnyWordSpec {

  import fixtures.*
  import Http4sTestContext.*
  import RT.*

  "Http4s transport" should {
    "support http" in {
        withServer {
          val disp = clientDispatcher()
          val greeterClient = new GreeterServiceClientWrapped(disp)

          disp.setupCredentials("user", "pass")

          assert(IO2R.unsafeRun(greeterClient.greet("John", "Smith")) == "Hi, John Smith!")
          assert(IO2R.unsafeRun(greeterClient.alternative()) == "value")

          checkBadBody("{}", disp)
          checkBadBody("{unparseable", disp)


          disp.cancelCredentials()
          IO2R.unsafeRunSync(greeterClient.alternative()) match {
            case Termination(exception: IRTUnexpectedHttpStatus, _, _) =>
              assert(exception.status == Status.Forbidden)
            case o =>
              fail(s"Expected IRTGenericFailure but got $o")
          }

          //
          disp.setupCredentials("user", "badpass")
          IO2R.unsafeRunSync(greeterClient.alternative()) match {
            case Termination(exception: IRTUnexpectedHttpStatus, _, _) =>
              assert(exception.status == Status.Unauthorized)
            case o =>
              fail(s"Expected IRTGenericFailure but got $o")
          }

          ()
        }
    }

    "support websockets" in {
      withServer {
        val disp = wsClientDispatcher()

        val greeterClient = new GreeterServiceClientWrapped(disp)

        disp.setupCredentials("user", "pass")

        assert(IO2R.unsafeRun(greeterClient.greet("John", "Smith")) == "Hi, John Smith!")
        assert(IO2R.unsafeRun(greeterClient.alternative()) == "value")

        IO2R.unsafeRunSync(ioService.wsSessionStorage.buzzersFor("user")) match {
          case Success(buzzers) =>
            buzzers.foreach {
              buzzer =>
                val client = new GreeterServiceClientWrapped(buzzer)
                assert(IO2R.unsafeRun(client.greet("John", "Buzzer")) == "Hi, John Buzzer!")
            }
          case v => fail(s"Expected success result but got $v")
        }

        disp.setupCredentials("user", "badpass")
        IO2R.unsafeRunSync(greeterClient.alternative()) match {
          case Termination(_: IRTGenericFailure, _, _) =>
          case o =>
            fail(s"Expected IRTGenericFailure but got $o")
        }

        disp.close()
        ()
      }
    }

  }

  def withServer(f: => Unit): Unit = {
    val io = BlazeServerBuilder[rt.MonoIO]
      .bindHttp(port, host)
      .withHttpWebSocketApp(ws => Router("/" -> ioService.service(ws)).orNotFound)
      .stream
      .evalMap(_ => ZIO.attempt(f))
      .compile.drain

    IO2R.unsafeRun(io)
  }

  def checkBadBody(body: String, disp: IRTDispatcher[rt.BiIO] with TestHttpDispatcher): Unit = {
    val dummy = IRTMuxRequest(IRTReqBody((1, 2)), GreeterServiceMethods.greet.id)
    val badJson = IO2R.unsafeRunSync(disp.sendRaw(dummy, body.getBytes))
    badJson match {
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
