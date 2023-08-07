package izumi.idealingua.runtime.rpc.http4s.fixtures

import cats.data.{Kleisli, OptionT}
import com.comcast.ip4s.*
import io.circe.Json
import izumi.fundamentals.platform.language.Quirks
import izumi.fundamentals.platform.network.IzSockets
import izumi.idealingua.runtime.rpc.http4s.*
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionsStorage.WsSessionsStorageImpl
import izumi.idealingua.runtime.rpc.http4s.ws.{WsClientId, WsContextProvider, WsSessionListener}
import izumi.idealingua.runtime.rpc.{IRTMuxRequest, IRTMuxResponse, RpcPacket}
import izumi.r2.idealingua.test.generated.GreeterServiceClientWrapped
import org.http4s.*
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import zio.interop.catz.*
import zio.{IO, ZIO}

import java.net.URI
import java.util.concurrent.atomic.AtomicReference

object Http4sTestContext {
  import RT.IO2R

  //
  final val addr    = IzSockets.temporaryServerAddress()
  final val port    = addr.getPort
  final val host    = addr.getHostName
  final val baseUri = Uri(Some(Uri.Scheme.http), Some(Uri.Authority(host = Uri.RegName(host), port = Some(port))))
  final val wsUri   = new URI("ws", null, host, port, "/ws", null, null)

  //
//
//  import RT.rt
//  import rt.*

  final val demo = new DummyServices[IO, DummyRequestContext]()

  //
  final val authUser: Kleisli[OptionT[IO[Throwable, _], _], Request[IO[Throwable, _]], DummyRequestContext] =
    Kleisli {
      (request: Request[IO[Throwable, _]]) =>
        val context = DummyRequestContext(request.remoteAddr.getOrElse(ipv4"0.0.0.0"), request.headers.get[Authorization].map(_.credentials))
        OptionT.liftF(ZIO.attempt(context))
    }

  final val wsContextProvider: WsContextProvider[IO, DummyRequestContext, String] = new WsContextProvider[IO, DummyRequestContext, String] {
    // DON'T DO THIS IN PRODUCTION CODE !!!
    val knownAuthorization = new AtomicReference[Credentials](null)

    override def toContext(id: WsClientId[String], initial: DummyRequestContext, packet: RpcPacket): zio.IO[Throwable, DummyRequestContext] = {
      Quirks.discard(id)

      initial.credentials match {
        case Some(value) =>
          knownAuthorization.compareAndSet(null, value)
        case None =>
      }

      val maybeAuth = packet.headers.getOrElse(Map.empty).get("Authorization")

      maybeAuth.map(Authorization.parse).flatMap(_.toOption) match {
        case Some(value) =>
          knownAuthorization.set(value.credentials)
        case None =>
      }
      ZIO.succeed {
        initial.copy(credentials = Option(knownAuthorization.get()))
      }
    }

    override def toId(initial: DummyRequestContext, currentId: WsClientId[String], packet: RpcPacket): zio.IO[Throwable, Option[String]] = {
      ZIO.attempt {
        packet.headers
          .getOrElse(Map.empty).get("Authorization")
          .map(Authorization.parse)
          .flatMap(_.toOption)
          .collect {
            case Authorization(BasicCredentials((user, _))) => user
          }
      }
    }

    override def handleEmptyBodyPacket(
      id: WsClientId[String],
      initial: DummyRequestContext,
      packet: RpcPacket,
    ): IO[Throwable, (Option[String], zio.IO[Throwable, Option[RpcPacket]])] = {
      Quirks.discard(id, initial)

      packet.headers.getOrElse(Map.empty).get("Authorization") match {
        case Some(value) if value.isEmpty =>
          // here we may clear internal state
          ZIO.succeed(None -> ZIO.succeed(None))

        case Some(_) =>
          toId(initial, id, packet) flatMap {
            case id @ Some(_) =>
              // here we may set internal state
              ZIO.succeed {
                id -> ZIO.succeed(packet.ref.map {
                  ref =>
                    RpcPacket.rpcResponse(ref, Json.obj())
                })
              }

            case None =>
              ZIO.succeed(None -> ZIO.succeed(Some(RpcPacket.rpcFail(packet.ref, "Authorization failed"))))
          }

        case None =>
          ZIO.succeed(None -> ZIO.succeed(None))
      }
    }
  }

  final val storage = new WsSessionsStorageImpl[IO, DummyRequestContext, String](RT.logger, demo.Server.codec)
  final val ioService = new HttpServer[IO, DummyRequestContext, DummyRequestContext, String](
    demo.Server.multiplexor,
    demo.Server.codec,
    AuthMiddleware(authUser),
    wsContextProvider,
    storage,
    Seq(WsSessionListener.empty[IO, String]),
    RT.dsl,
    RT.logger,
    RT.printer,
  )

  final def clientDispatcher(): ClientDispatcher[IO] & TestHttpDispatcher =
    new ClientDispatcher[IO](RT.izLogger, RT.printer, baseUri, demo.Client.codec, RT.execCtx) with TestHttpDispatcher {

      override def sendRaw(request: IRTMuxRequest, body: Array[Byte]): BiIO[Throwable, IRTMuxResponse] = {
        val req = buildRequest(baseUri, request, body)
        runRequest(handleResponse(request, _), req)
      }

      override protected def transformRequest(request: Request[IO[Throwable, _]]): Request[IO[Throwable, _]] = {
        request.withHeaders(Headers(creds.get()))
      }
    }

  final val wsClientContextProvider = new WsClientContextProvider[Unit] {
    override def toContext(packet: RpcPacket): Unit = ()
  }

  final def wsClientDispatcher(): ClientWsDispatcher[IO, Unit] & TestDispatcher = {
    new ClientWsDispatcher[IO, Unit](wsUri, demo.Client.codec, demo.Client.buzzerMultiplexor, wsClientContextProvider, RT.izLogger, RT.printer) with TestDispatcher {

      import scala.concurrent.duration.*
      override protected val timeout: FiniteDuration = 5.seconds

      override protected def transformRequest(request: RpcPacket): RpcPacket = {
        Option(creds.get()) match {
          case Some(value) =>
            val update = value.values.map(h => (h.name.toString, h.value)).toMap
            request.copy(headers = Some(request.headers.getOrElse(Map.empty) ++ update))
          case None =>
            request
        }
      }
    }
  }

  final val greeterClient = new GreeterServiceClientWrapped(clientDispatcher())

}
