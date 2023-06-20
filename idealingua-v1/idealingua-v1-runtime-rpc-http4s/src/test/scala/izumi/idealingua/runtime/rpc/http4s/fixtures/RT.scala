package izumi.idealingua.runtime.rpc.http4s.fixtures

import io.circe.Printer
import izumi.functional.bio.UnsafeRun2
import izumi.idealingua.runtime.rpc.http4s.Http4sRuntime
import izumi.logstage.api.routing.{ConfigurableLogRouter, StaticLogRouter}
import izumi.logstage.api.{IzLogger, Log}
import zio.interop.catz.*

import scala.concurrent.ExecutionContext.global

object RT {
  final val logger = makeLogger()
  final val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  final val handler = UnsafeRun2.FailureHandler.Custom(message => logger.warn(s"Fiber failed: $message"))
  implicit val IO2R: UnsafeRun2[zio.IO] = UnsafeRun2.createZIO(handler = handler)
  final val rt = new Http4sRuntime[zio.IO, DummyRequestContext, DummyRequestContext, String, Unit, Unit](global)

  private def makeLogger(): IzLogger = {
    val router = ConfigurableLogRouter(Log.Level.Info, levels = Map(
      "org.http4s" -> Log.Level.Warn,
      "org.http4s.server.blaze" -> Log.Level.Error,
      "org.http4s.blaze.channel.nio1" -> Log.Level.Crit,
      "izumi.idealingua.runtime.rpc.http4s" -> Log.Level.Crit,
    ))

    val out = IzLogger(router)
    StaticLogRouter.instance.setup(router)
    out
  }

}
