package izumi.idealingua.runtime.rpc.http4s

import io.circe.Json
import izumi.functional.bio.retry.Scheduler2
import izumi.functional.bio.{F, IO2, Temporal2}
import izumi.fundamentals.platform.language.Quirks.*
import izumi.idealingua.runtime.rpc.*

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.FiniteDuration

class RequestState[F[+ _, + _]: IO2: Temporal2] {

  // TODO: stale item cleanups
  protected val requests: ConcurrentHashMap[RpcPacketId, IRTMethodId] = new ConcurrentHashMap[RpcPacketId, IRTMethodId]()
  protected val responses: ConcurrentHashMap[RpcPacketId, RawResponse] = new ConcurrentHashMap[RpcPacketId, RawResponse]()

  private val scheduler2: Scheduler2[F] = implicitly

  def methodOf(id: RpcPacketId): Option[IRTMethodId] = {
    Option(requests.get(id))
  }

  def request(id: RpcPacketId, methodId: IRTMethodId): Unit = {
    requests.put(id, methodId).discard()
  }

  def checkResponse(id: RpcPacketId): Option[RawResponse] = {
    Option(responses.get(id))
  }

  def forget(id: RpcPacketId): Unit = {
    requests.remove(id)
    responses.remove(id).discard()
  }

  def respond(id: RpcPacketId, response: RawResponse): Unit = {
    Option(requests.remove(id)) match {
      case Some(_) =>
        responses.put(id, response).discard()
      case None =>
      // We ignore responses for unknown requests
    }
  }

  def clear(): Unit = {
    // TODO: autocloseable + latch?
    requests.clear()
    responses.clear().discard()
  }

  def handleResponse(maybePacketId: Option[RpcPacketId], data: Json): F[Throwable, PacketInfo] = {
    for {
      maybeMethod <- F.sync {
        for {
          id <- maybePacketId
          method <- methodOf(id)
        } yield PacketInfo(method, id)
      }

      method <- maybeMethod match {
        case Some(m@PacketInfo(method, id)) =>
          respond(id, RawResponse.GoodRawResponse(data, method))
          F.pure(m): F[IRTMissingHandlerException, PacketInfo]

        case None =>
          F.fail(new IRTMissingHandlerException(s"Cannot handle response for async request $maybePacketId: no service handler", data))
      }
    } yield {
      method
    }
  }

  def poll(id: RpcPacketId, interval: FiniteDuration, timeout: FiniteDuration): F[Nothing, Option[RawResponse]] =
    scheduler2.retryOrElseUntil {
      F.sleep(interval).flatMap {
        _ =>
          checkResponse(id) match {
            case None =>
              F.fail(())
            case Some(value) =>
              F.pure(Some(value)): F[Unit, Option[RawResponse]]
          }
        }
    }(timeout, _ => F.pure(None))
}
