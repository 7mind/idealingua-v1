package izumi.idealingua.runtime.rpc.http4s.fixtures

import cats.data.{Kleisli, OptionT}
import com.comcast.ip4s.*
import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.language.Quirks
import izumi.fundamentals.platform.network.IzSockets
import izumi.idealingua.runtime.rpc.http4s.*
import izumi.idealingua.runtime.rpc.http4s.clients.WsRpcDispatcherFactory.WsRpcContextProvider
import izumi.idealingua.runtime.rpc.http4s.clients.{HttpRpcDispatcher, HttpRpcDispatcherFactory, WsRpcDispatcher, WsRpcDispatcherFactory}
import izumi.idealingua.runtime.rpc.http4s.ws.WsContextProvider.WsAuthResult
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionsStorage.WsSessionsStorageImpl
import izumi.idealingua.runtime.rpc.http4s.ws.{WsClientId, WsContextProvider, WsSessionListener}
import izumi.idealingua.runtime.rpc.{RPCPacketKind, RpcPacket}
import org.http4s.*
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import zio.interop.catz.*
import zio.{IO, ZIO}

object Http4sTestContext {
  import RT.IO2R

  //
  final val addr    = IzSockets.temporaryServerAddress()
  final val port    = addr.getPort
  final val host    = addr.getHostName
  final val baseUri = Uri(Some(Uri.Scheme.http), Some(Uri.Authority(host = Uri.RegName(host), port = Some(port))))
  final val wsUri   = Uri.unsafeFromString(s"ws://$host:$port/ws")

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
    override def toContext(id: WsClientId[String], initial: DummyRequestContext, packet: RpcPacket): zio.IO[Throwable, DummyRequestContext] = {
      ZIO.succeed {
        val fromState  = id.id.map(header => Map("Authorization" -> header)).getOrElse(Map.empty)
        val allHeaders = fromState ++ packet.headers.getOrElse(Map.empty)
        val creds      = allHeaders.get("Authorization").flatMap(Authorization.parse(_).toOption).map(_.credentials)
        DummyRequestContext(initial.ip, creds.orElse(initial.credentials))
      }
    }

    override def toId(initial: DummyRequestContext, currentId: WsClientId[String], packet: RpcPacket): zio.IO[Throwable, Option[String]] = {
      ZIO.attempt {
        val fromState  = currentId.id.map(header => Map("Authorization" -> header)).getOrElse(Map.empty)
        val allHeaders = fromState ++ packet.headers.getOrElse(Map.empty)
        allHeaders.get("Authorization")
      }
    }

    override def handleAuthorizationPacket(
      id: WsClientId[String],
      initial: DummyRequestContext,
      packet: RpcPacket,
    ): IO[Throwable, WsAuthResult[String]] = {
      Quirks.discard(id, initial)

      packet.headers.flatMap(_.get("Authorization")) match {
        case Some(value) if value.isEmpty =>
          // here we may clear internal state
          ZIO.succeed(WsAuthResult(None, RpcPacket(RPCPacketKind.RpcResponse, None, None, packet.id, None, None, None)))

        case Some(_) =>
          toId(initial, id, packet).flatMap {
            case Some(header) =>
              // here we may set internal state
              ZIO.succeed(WsAuthResult(Some(header), RpcPacket(RPCPacketKind.RpcResponse, None, None, packet.id, None, None, None)))

            case None =>
              ZIO.succeed(WsAuthResult(None, RpcPacket.rpcFail(packet.id, "Authorization failed")))
          }

        case None =>
          ZIO.succeed(WsAuthResult(None, RpcPacket(RPCPacketKind.RpcResponse, None, None, packet.id, None, None, None)))
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

  final val httpClientFactory: HttpRpcDispatcherFactory[IO] = {
    new HttpRpcDispatcherFactory[IO](demo.Client.codec, RT.execCtx, RT.printer, RT.logger)
  }
  final def httpRpcClientDispatcher(headers: Headers): HttpRpcDispatcher.IRTDispatcherRaw[IO] = {
    httpClientFactory.dispatcher(baseUri, headers)
  }

  final val wsClientFactory: WsRpcDispatcherFactory[IO] = {
    new WsRpcDispatcherFactory[IO](demo.Client.codec, RT.printer, RT.logger, RT.izLogger)
  }
  final def wsRpcClientDispatcher(): Lifecycle[IO[Throwable, _], WsRpcDispatcher.IRTDispatcherWs[IO]] = {
    wsClientFactory.dispatcher(wsUri, demo.Client.buzzerMultiplexor, WsRpcContextProvider.unit)
  }
}
