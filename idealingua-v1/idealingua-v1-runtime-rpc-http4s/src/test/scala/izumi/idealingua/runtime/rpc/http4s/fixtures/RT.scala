package izumi.idealingua.runtime.rpc.http4s.fixtures

import io.circe.Printer
import izumi.functional.bio.UnsafeRun2
import izumi.idealingua.runtime.rpc.http4s.HttpExecutionContext
import izumi.logstage.api.routing.{ConfigurableLogRouter, StaticLogRouter}
import izumi.logstage.api.{IzLogger, Log}
import logstage.LogIO
import org.http4s.dsl.Http4sDsl
import zio.IO

import scala.concurrent.ExecutionContext.global

object RT {
  final val izLogger         = makeLogger()
  final val logger           = LogIO.fromLogger[IO[Nothing, _]](makeLogger())
  final val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  final val handler                     = UnsafeRun2.FailureHandler.Custom(message => izLogger.warn(s"Fiber failed: $message"))
  implicit val IO2R: UnsafeRun2[zio.IO] = UnsafeRun2.createZIO(handler = handler)
  final val dsl                         = Http4sDsl.apply[zio.IO[Throwable, _]]
  final val execCtx                     = HttpExecutionContext(global)

  private def makeLogger(): IzLogger = {
    val router = ConfigurableLogRouter(
      Log.Level.Debug,
      levels = Map(
        "org.http4s"                          -> Log.Level.Warn,
        "org.http4s.server.blaze"             -> Log.Level.Error,
        "org.http4s.blaze.channel.nio1"       -> Log.Level.Crit,
        "izumi.idealingua.runtime.rpc.http4s" -> Log.Level.Crit,
      ),
    )

    val out = IzLogger(router)
    StaticLogRouter.instance.setup(router)
    out
  }

}
