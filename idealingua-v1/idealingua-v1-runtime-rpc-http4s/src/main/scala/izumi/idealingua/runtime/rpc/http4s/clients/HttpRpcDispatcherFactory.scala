package izumi.idealingua.runtime.rpc.http4s.clients

import cats.effect.Async
import io.circe
import izumi.functional.bio.{F, IO2}
import izumi.functional.lifecycle.Lifecycle
import izumi.fundamentals.platform.language.Quirks.Discarder
import izumi.idealingua.runtime.rpc.http4s.HttpExecutionContext
import izumi.idealingua.runtime.rpc.{IRTClientMultiplexor, IRTMethodId, IRTMuxRequest, IRTMuxResponse}
import logstage.LogIO2
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.{Header, Headers, Request, Uri}
import org.typelevel.ci.CIString

class HttpRpcDispatcherFactory[F[+_, +_]: IO2](
  codec: IRTClientMultiplexor[F],
  executionContext: HttpExecutionContext,
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
    blazeClient.map {
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

  protected def blazeClient: Lifecycle[F[Throwable, _], Client[F[Throwable, _]]] = {
    Lifecycle.fromCats {
      blazeClientBuilder {
        BlazeClientBuilder[F[Throwable, _]]
          .withExecutionContext(executionContext.clientExecutionContext)
      }.resource
    }
  }

  protected def blazeClientBuilder(defaultBuilder: BlazeClientBuilder[F[Throwable, _]]): BlazeClientBuilder[F[Throwable, _]] = {
    defaultBuilder
  }

}
