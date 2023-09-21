package izumi.idealingua.runtime.rpc.http4s.clients

import cats.effect.Async
import io.circe
import izumi.functional.bio.{F, IO2}
import izumi.fundamentals.platform.language.Quirks.Discarder
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.HttpExecutionContext
import izumi.idealingua.runtime.rpc.http4s.clients.HttpRpcClientDispatcher.IRTDispatcherRaw
import logstage.LogIO2
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.ci.*

class HttpRpcClientDispatcherFactory[F[+_, +_]: IO2](
  codec: IRTClientMultiplexor[F],
  executionContext: HttpExecutionContext,
  printer: circe.Printer,
  logger: LogIO2[F],
)(implicit AT: Async[F[Throwable, _]]
) {
  def dispatcher(
    uri: Uri,
    tweakRequest: Request[F[Throwable, _]] => Request[F[Throwable, _]] = (req: Request[F[Throwable, _]]) => req,
    resourceCheck: F[Throwable, Unit]                                  = F.unit,
  ): IRTDispatcherRaw[F] = {
    new HttpRpcClientDispatcher[F](uri, codec, executionContext, printer, dispatcherLogger(uri, logger)) {
      override def dispatch(input: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
        resourceCheck *> super.dispatch(input)
      }
      override def dispatchRaw(method: IRTMethodId, request: String): F[Throwable, IRTMuxResponse] = {
        resourceCheck *> super.dispatchRaw(method, request)
      }
      override protected def buildRequest(baseUri: Uri)(method: IRTMethodId, body: Array[Byte]): Request[F[Throwable, _]] = {
        tweakRequest(super.buildRequest(baseUri)(method, body))
      }
      override protected def blazeClientBuilder(defaultBuilder: BlazeClientBuilder[F[Throwable, *]]): BlazeClientBuilder[F[Throwable, *]] = {
        super.blazeClientBuilder(defaultBuilder)
      }
    }
  }

  final def dispatcher(
    uri: Uri,
    headers: Headers,
  ): IRTDispatcherRaw[F] = {
    val tweakRequest = (req: Request[F[Throwable, _]]) => req.withHeaders(headers)
    dispatcher(uri, tweakRequest)
  }

  final def dispatcher(
    uri: Uri,
    headers: Map[String, String],
  ): IRTDispatcherRaw[F] = {
    val httpHeaders  = new Headers(headers.map { case (k, v) => Header.Raw(CIString(k), v) }.toList)
    val tweakRequest = (req: Request[F[Throwable, _]]) => req.withHeaders(httpHeaders)
    dispatcher(uri, tweakRequest)
  }

  protected def dispatcherLogger(uri: Uri, logger: LogIO2[F]): LogIO2[F] = {
    uri.discard()
    logger
  }

  protected def blazeClientBuilder(defaultBuilder: BlazeClientBuilder[F[Throwable, _]]): BlazeClientBuilder[F[Throwable, _]] = {
    defaultBuilder
  }
}
