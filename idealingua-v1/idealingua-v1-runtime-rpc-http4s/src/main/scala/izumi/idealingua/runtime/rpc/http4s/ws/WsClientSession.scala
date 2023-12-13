package izumi.idealingua.runtime.rpc.http4s.ws

import izumi.functional.bio.{F, IO2, Temporal2}
import izumi.fundamentals.platform.time.IzTime
import izumi.fundamentals.platform.uuid.UUIDGen
import izumi.idealingua.runtime.rpc.http4s.IRTAuthenticator.AuthContext
import izumi.idealingua.runtime.rpc.http4s.IRTContextServicesMuxer

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.*

trait WsClientSession[F[+_, +_]] {
  def sessionId: WsSessionId
  def getAuthContext: AuthContext
  def requests: WsClientRequests[F]

  def servicesMuxer: IRTContextServicesMuxer[F]

  def updateAuthContext(newContext: AuthContext): F[Throwable, Unit]

  def start(): F[Throwable, Unit]
  def finish(): F[Throwable, Unit]
}

object WsClientSession {

  class WsClientSessionImpl[F[+_, +_]: IO2](
    val initialContext: AuthContext,
    val servicesMuxer: IRTContextServicesMuxer[F],
    val requests: WsClientRequests[F],
    wsSessionStorage: WsSessionsStorage[F],
  ) extends WsClientSession[F] {
    private val authContextRef             = new AtomicReference[AuthContext](initialContext)
    private val openingTime: ZonedDateTime = IzTime.utcNow

    override val sessionId: WsSessionId = WsSessionId(UUIDGen.getTimeUUID())

    override def getAuthContext: AuthContext = authContextRef.get()

    override def updateAuthContext(newContext: AuthContext): F[Throwable, Unit] = {
      for {
        contexts <- F.sync {
          authContextRef.synchronized {
            val oldContext = authContextRef.get()
            val updatedContext = authContextRef.updateAndGet {
              old => AuthContext(old.headers ++ newContext.headers, old.networkAddress.orElse(newContext.networkAddress))
            }
            oldContext -> updatedContext
          }
        }
        (oldContext, updatedContext) = contexts
        _ <- F.when(oldContext != updatedContext) {
          F.traverse_(servicesMuxer.contextServices)(_.onWsSessionUpdate(sessionId, updatedContext))
        }
      } yield ()
    }

    override def finish(): F[Throwable, Unit] = {
      wsSessionStorage.deleteSession(sessionId) *>
      F.traverse_(servicesMuxer.contextServices)(_.onWsSessionClosed(sessionId, getAuthContext))
    }

    override def start(): F[Throwable, Unit] = {
      wsSessionStorage.addSession(this) *>
      F.traverse_(servicesMuxer.contextServices)(_.onWsSessionOpened(sessionId, getAuthContext))
    }

    override def toString: String = s"[$sessionId, ${duration().toSeconds}s]"

    private[this] def duration(): FiniteDuration = {
      val now = IzTime.utcNow
      val d   = java.time.Duration.between(openingTime, now)
      FiniteDuration(d.toNanos, TimeUnit.NANOSECONDS)
    }
  }
}
