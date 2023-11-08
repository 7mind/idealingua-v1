package izumi.idealingua.runtime.rpc.http4s.ws

import io.circe.Json
import izumi.functional.bio.{Clock1, Clock2, F, IO2, Primitives2, Promise2, Temporal2}
import izumi.fundamentals.platform.language.Quirks.*
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.ws.RawResponse.BadRawResponse
import izumi.idealingua.runtime.rpc.http4s.ws.WsRequestState.RequestHandler
import izumi.idealingua.runtime.rpc.http4s.ws.WsRpcHandler.WsClientResponder

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters.*

class WsRequestState[F[+_, +_]: IO2: Temporal2: Primitives2] extends WsClientResponder[F] {
  // using custom clock to no allow to override it
  private[this] val clock: Clock2[F]                                             = Clock1.fromImpure(Clock1.Standard)
  private[this] val requests: ConcurrentHashMap[RpcPacketId, IRTMethodId]        = new ConcurrentHashMap[RpcPacketId, IRTMethodId]()
  private[this] val responses: ConcurrentHashMap[RpcPacketId, RequestHandler[F]] = new ConcurrentHashMap[RpcPacketId, RequestHandler[F]]()

  def requestEmpty(id: RpcPacketId, ttl: FiniteDuration = 3.minute): F[Throwable, Promise2[F, Nothing, RawResponse]] = {
    for {
      now     <- clock.nowOffset()
      _       <- forgetExpired(now)
      promise <- F.mkPromise[Nothing, RawResponse]
      handler  = RequestHandler(id, promise, ttl, now)
      _       <- F.sync(responses.put(id, handler))
    } yield promise
  }

  def request(id: RpcPacketId, methodId: IRTMethodId, ttl: FiniteDuration = 3.minute): F[Throwable, Promise2[F, Nothing, RawResponse]] = {
    for {
      _       <- F.sync(requests.put(id, methodId))
      promise <- requestEmpty(id, ttl)
    } yield promise
  }

  def forget(id: RpcPacketId): F[Nothing, Unit] = F.sync {
    requests.remove(id)
    responses.remove(id).discard()
  }

  def clear(): F[Nothing, Unit] = {
    for {
      _ <- F.sync(requests.clear())
      _ <- F.traverse(responses.values().asScala)(h => h.promise.succeed(BadRawResponse(None)))
      _ <- F.sync(responses.clear())
    } yield ()
  }

  def responseWith(packetId: RpcPacketId, response: RawResponse): F[Throwable, Unit] = {
    F.sync(Option(responses.get(packetId))).flatMap {
      case Some(handler) => handler.promise.succeed(response).void
      case None          => F.unit
    }
  }

  def responseWithData(packetId: RpcPacketId, data: Json): F[Throwable, Unit] = {
    for {
      method <- F.fromOption(new IRTMissingHandlerException(s"Cannot handle response for async request $packetId: no service handler", data)) {
        Option(requests.get(packetId))
      }
      _ <- responseWith(packetId, RawResponse.GoodRawResponse(data, method))
    } yield ()
  }

  def awaitResponse(id: RpcPacketId, timeout: FiniteDuration): F[Throwable, Option[RawResponse]] = {
    F.fromOption(new IRTMissingHandlerException(s"Can not await for async response: $id. Missing handler.", null)) {
      Option(responses.get(id))
    }.flatMap(_.promise.await.timeout(timeout))
  }

  private[this] def forgetExpired(now: OffsetDateTime): F[Nothing, Unit] = {
    for {
      removed <- F.sync {
        val removed = mutable.ArrayBuffer.empty[RequestHandler[F]]
        // it should be synchronized on node remove
        responses.values().removeIf {
          handler =>
            val isExpired = handler.expired(now)
            if (isExpired) removed.addOne(handler)
            isExpired
        }
        removed.toList
      }
      _ <- F.traverse(removed) {
        handler =>
          requests.remove(handler.id)
          handler.promise.poll.flatMap {
            case Some(_) => F.unit
            case None    => handler.promise.succeed(BadRawResponse(Some(Json.obj("error" -> Json.fromString(s"Request expired within ${handler.ttl}.")))))
          }
      }
    } yield ()
  }
}

object WsRequestState {
  final case class RequestHandler[F[+_, +_]](
    id: RpcPacketId,
    promise: Promise2[F, Nothing, RawResponse],
    ttl: FiniteDuration,
    firedAt: OffsetDateTime,
  ) {
    def expired(now: OffsetDateTime): Boolean = {
      ChronoUnit.MILLIS.between(firedAt, now) >= ttl.toMillis
    }
  }
}