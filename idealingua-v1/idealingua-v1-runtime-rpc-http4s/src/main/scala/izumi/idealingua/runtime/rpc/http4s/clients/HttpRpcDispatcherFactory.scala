package izumi.idealingua.runtime.rpc.http4s.clients

import cats.effect.Async
import io.circe
import izumi.functional.bio.{Applicative2, F, IO2}
import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.language.Quirks.Discarder
import izumi.idealingua.runtime.rpc.{IRTClientMultiplexor, IRTMethodId, IRTMuxRequest, IRTMuxResponse}
import izumi.logstage.api.rendering.AnyEncoded
import logstage.{Log, LogIO2}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Header, Headers, Request, Uri}
import org.typelevel.ci.CIString
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

class HttpRpcDispatcherFactory[F[+_, +_]: IO2](
  codec: IRTClientMultiplexor[F],
  printer: circe.Printer,
  logger: LogIO2[F],
)(implicit AT: Async[F[Throwable, _]]
) {
  self =>

  def dispatcher(
    uri: Uri,
    tweakRequest: Request[F[Throwable, _]] => Request[F[Throwable, _]] = (req: Request[F[Throwable, _]]) => req,
    resourceCheck: F[Throwable, Unit]                                  = F.unit,
  ): Lifecycle[F[Throwable, _], HttpRpcDispatcher[F]] = {
    emberClient.map {
      new HttpRpcDispatcher[F](_, uri, codec, printer, dispatcherLogger(uri, logger)) {
        override def dispatch(input: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
          resourceCheck *> super.dispatch(input)
        }
        override def dispatchRaw(method: IRTMethodId, request: String): F[Throwable, IRTMuxResponse] = {
          resourceCheck *> super.dispatchRaw(method, request)
        }
        override protected def buildRequest(baseUri: Uri)(method: IRTMethodId, body: Array[Byte]): Request[F[Throwable, _]] = {
          tweakRequest(super.buildRequest(baseUri)(method, body))
        }
      }
    }
  }

  final def dispatcher(
    uri: Uri,
    headers: Headers,
  ): Lifecycle[F[Throwable, _], HttpRpcDispatcher[F]] = {
    dispatcher(uri, tweakRequest = _.withHeaders(headers))
  }

  final def dispatcher(
    uri: Uri,
    headers: Map[String, String],
  ): Lifecycle[F[Throwable, _], HttpRpcDispatcher[F]] = {
    val httpHeaders = new Headers(headers.iterator.map { case (k, v) => Header.Raw(CIString(k), v) }.toList)
    dispatcher(uri, headers = httpHeaders)
  }

  protected def dispatcherLogger(uri: Uri, logger: LogIO2[F]): LogIO2[F] = {
    uri.discard()
    logger
  }

  protected def emberClient: Lifecycle[F[Throwable, _], Client[F[Throwable, _]]] = {
    Lifecycle.fromCats {
      emberClientBuilder {
        implicit def _logger: LogIO2[F] = logger
        import HttpRpcDispatcherFactory._hacky_loggerFactory
        EmberClientBuilder.default[F[Throwable, _]]
      }.build
    }
  }

  protected def emberClientBuilder(defaultBuilder: EmberClientBuilder[F[Throwable, _]]): EmberClientBuilder[F[Throwable, _]] = {
    defaultBuilder
  }

}

object HttpRpcDispatcherFactory {

  implicit def _hacky_loggerFactory[F[+_, +_]: Applicative2](implicit logger: LogIO2[F]): LoggerFactory[F[Throwable, _]] = new LoggerFactory[F[Throwable, _]] {
    override def getLoggerFromName(name: String): SelfAwareStructuredLogger[F[Throwable, _]] = new SelfAwareStructuredLogger[F[Throwable, _]] {
      override def isTraceEnabled: F[Throwable, Boolean] = F.pure(true)
      override def isDebugEnabled: F[Throwable, Boolean] = F.pure(true)
      override def isInfoEnabled: F[Throwable, Boolean]  = F.pure(true)
      override def isWarnEnabled: F[Throwable, Boolean]  = F.pure(true)
      override def isErrorEnabled: F[Throwable, Boolean] = F.pure(true)

      override def trace(ctx: Map[String, String])(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Trace, msg, ctx)
      override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Trace, msg, ctx, t)
      override def trace(t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Trace, msg, t = t)
      override def trace(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Trace, msg)

      override def debug(ctx: Map[String, String])(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Debug, msg, ctx)
      override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Debug, msg, ctx, t)
      override def debug(t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Debug, msg, t = t)
      override def debug(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Debug, msg)

      override def info(ctx: Map[String, String])(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Info, msg, ctx)
      override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Info, msg, ctx, t)
      override def info(t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Info, msg, t = t)
      override def info(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Info, msg)

      override def warn(ctx: Map[String, String])(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Warn, msg, ctx)
      override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Warn, msg, ctx, t)
      override def warn(t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Warn, msg, t = t)
      override def warn(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Warn, msg)

      override def error(ctx: Map[String, String])(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Error, msg, ctx)
      override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Error, msg, ctx, t)
      override def error(t: Throwable)(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Error, msg, t = t)
      override def error(msg: => String): F[Throwable, Unit] =
        log(Log.Level.Error, msg)

      private[this] def log(level: Log.Level, msg: => String, ctx: Map[String, String] = null, t: Throwable = null): F[Nothing, Unit] = {
        import scala.util.chaining.*
        logger
          .pipe(l => Option(ctx).fold(l)(c => l.withCustomContextMap(c.view.mapValues(AnyEncoded.to).toMap)))
          .pipe(l => Option(t).fold(l)(t => l.withCustomContext("error" -> t)))
          .log(level)(Log.Message.raw(msg))
      }
    }

    override def fromName(name: String): F[Throwable, SelfAwareStructuredLogger[F[Throwable, _]]] = F.pure(getLoggerFromName(name))
  }

}
