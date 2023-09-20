package izumi.idealingua.runtime.rpc.http4s.ws

import io.circe.Json
import izumi.functional.bio.{F, IO2, Primitives2, Promise2, Temporal2}
import izumi.fundamentals.platform.language.Quirks.*
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.ws.RawResponse.BadRawResponse

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*

class RequestState[F[+_, +_]: IO2: Temporal2: Primitives2] {

  // TODO: stale item cleanups
  protected val requests: ConcurrentHashMap[RpcPacketId, IRTMethodId]                        = new ConcurrentHashMap[RpcPacketId, IRTMethodId]()
  protected val responses: ConcurrentHashMap[RpcPacketId, Promise2[F, Nothing, RawResponse]] = new ConcurrentHashMap[RpcPacketId, Promise2[F, Nothing, RawResponse]]()

  def requestEmpty(id: RpcPacketId): F[Throwable, Promise2[F, Nothing, RawResponse]] = {
    for {
      promise <- F.mkPromise[Nothing, RawResponse]
      _       <- F.sync(responses.put(id, promise))
    } yield promise
  }

  def request(id: RpcPacketId, methodId: IRTMethodId): F[Throwable, Promise2[F, Nothing, RawResponse]] = {
    for {
      _       <- F.sync(requests.put(id, methodId))
      promise <- requestEmpty(id)
    } yield promise
  }

  def forget(id: RpcPacketId): F[Nothing, Unit] = F.sync {
    requests.remove(id)
    responses.remove(id).discard()
  }

  def clear(): F[Nothing, Unit] = {
    // TODO: autocloseable + latch?
    for {
      _ <- F.sync(requests.clear())
      _ <- F.traverse(responses.values().asScala)(p => p.succeed(BadRawResponse(None)))
      _ <- F.sync(responses.clear())
    } yield ()
  }

  def responseWith(id: RpcPacketId, response: RawResponse): F[Throwable, Unit] = {
    F.sync(Option(responses.get(id))).flatMap {
      case Some(promise) => promise.succeed(response).void
      case None          => F.unit
    } *> forget(id)
  }

  def handleResponse(maybePacketId: Option[RpcPacketId], data: Json): F[Throwable, Unit] = {
    for {
      maybeMethod <- F.sync {
        for {
          id     <- maybePacketId
          method <- Option(requests.get(id))
        } yield method -> id
      }

      method <- maybeMethod match {
        case Some((method, id)) =>
          responseWith(id, RawResponse.GoodRawResponse(data, method))

        case None =>
          F.fail(new IRTMissingHandlerException(s"Cannot handle response for async request $maybePacketId: no service handler", data))
      }
    } yield method
  }

  def awaitResponse(id: RpcPacketId, timeout: FiniteDuration): F[Nothing, Option[RawResponse]] = {
    Option(responses.get(id)) match {
      case Some(value) => value.await.timeout(timeout)
      case None        => F.pure(None)
    }
  }
}
