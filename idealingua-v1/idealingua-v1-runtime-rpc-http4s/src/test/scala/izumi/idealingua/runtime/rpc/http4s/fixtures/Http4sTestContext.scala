package izumi.idealingua.runtime.rpc.http4s.fixtures

import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.network.IzSockets
import izumi.idealingua.runtime.rpc.http4s.*
import izumi.idealingua.runtime.rpc.http4s.clients.{HttpRpcDispatcher, HttpRpcDispatcherFactory, WsRpcDispatcher, WsRpcDispatcherFactory}
import izumi.idealingua.runtime.rpc.http4s.ws.WsSessionsStorage.WsSessionsStorageImpl
import org.http4s.*
import org.http4s.headers.Authorization
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

  final val authenticator = new IRTAuthenticator[IO, DummyRequestContext] {
    override def authenticate(request: IRTAuthenticator.AuthContext): IO[Throwable, Option[DummyRequestContext]] = ZIO.succeed {
      val creds = request.headers.get[Authorization].map(_.credentials)
      Some(DummyRequestContext(request.networkAddress, creds))
    }
  }
  final val storage        = new WsSessionsStorageImpl[IO](RT.logger)
  final val contextService = new IRTContextServices[IO, DummyRequestContext](demo.Server.multiplexor, authenticator, Set.empty)
  final val contextMuxer   = new IRTContextServicesMuxer[IO](Set(contextService))
  final val ioService = new HttpServer[IO](
    contextMuxer,
    storage,
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
    wsClientFactory.dispatcher(wsUri, demo.Client.buzzerMultiplexor)
  }
}
