package izumi.idealingua.runtime.rpc.http4s

import cats.effect.Async
import fs2.Stream
import io.circe
import io.circe.parser.parse
import izumi.functional.bio.{Exit, F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.logstage.api.IzLogger
import org.http4s.*
import org.http4s.blaze.client.*

class ClientDispatcher[F[+_, +_]: IO2](
  logger: IzLogger,
  printer: circe.Printer,
  baseUri: Uri,
  codec: IRTClientMultiplexor[F],
  executionContext: HttpExecutionContext,
)(implicit AT: Async[F[Throwable, _]]
) extends IRTDispatcher[F] {

  def dispatch(request: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
    val handler = handleResponse(request, _: Response[F[Throwable, _]])

    logger.trace(s"${request.method -> "method"}: Goint to perform $request")

    codec.encode(request).flatMap {
      encoded =>
        val outBytes: Array[Byte] = printer.print(encoded).getBytes
        val req                   = buildRequest(baseUri, request, outBytes)

        logger.debug(s"${request.method -> "method"}: Prepared request $encoded")
        runRequest[IRTMuxResponse](handler, req)
    }
  }

  protected def runRequest[T](handler: Response[F[Throwable, _]] => F[Throwable, T], req: Request[F[Throwable, _]]): F[Throwable, T] = {
    val clientBuilder = blazeClientBuilder(BlazeClientBuilder[F[Throwable, _]].withExecutionContext(executionContext.clientExecutionContext))
    clientBuilder.resource.use {
      _.run(req).use(handler)
    }
  }

  protected def blazeClientBuilder(defaultBuilder: BlazeClientBuilder[F[Throwable, _]]): BlazeClientBuilder[F[Throwable, _]] = defaultBuilder

  protected def handleResponse(input: IRTMuxRequest, resp: Response[F[Throwable, _]]): F[Throwable, IRTMuxResponse] = {
    logger.trace(s"${input.method -> "method"}: Received response, going to materialize, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")

    if (resp.status != Status.Ok) {
      logger.info(s"${input.method -> "method"}: unexpected HTTP response, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
      F.fail(IRTUnexpectedHttpStatus(resp.status))
    } else {
      resp
        .as[String]
        .flatMap {
          body =>
            logger.trace(s"${input.method -> "method"}: Received response: $body")
            val decoded = for {
              parsed  <- F.fromEither(parse(body))
              product <- codec.decode(parsed, input.method)
            } yield {
              logger.trace(s"${input.method -> "method"}: decoded response: $product")
              product
            }

            decoded.sandbox.catchAll {
              case Exit.Error(error, trace) =>
                logger.info(s"${input.method -> "method"}: decoder returned failure on $body: $error $trace")
                F.fail(new IRTUnparseableDataException(s"${input.method}: decoder returned failure on body=$body: error=$error trace=$trace", Option(error)))

              case Exit.Termination(f, _, trace) =>
                logger.info(s"${input.method -> "method"}: decoder failed on $body: $f $trace")
                F.fail(new IRTUnparseableDataException(s"${input.method}: decoder failed on body=$body: f=$f trace=$trace", Option(f)))

              case Exit.Interruption(error, _, trace) =>
                logger.info(s"${input.method -> "method"}: decoder interrupted on $body: $error $trace")
                F.fail(new IRTUnparseableDataException(s"${input.method}: decoder interrupted on body=$body: error=$error trace=$trace", Option(error)))
            }
        }
    }
  }

  protected final def buildRequest(baseUri: Uri, input: IRTMuxRequest, body: Array[Byte]): Request[F[Throwable, _]] = {
    val entityBody: EntityBody[F[Throwable, _]] = Stream.emits(body).covary[F[Throwable, _]]
    buildRequest(baseUri, input, entityBody)
  }

  protected final def buildRequest(baseUri: Uri, input: IRTMuxRequest, body: EntityBody[F[Throwable, _]]): Request[F[Throwable, _]] = {
    val uri = baseUri / input.method.service.value / input.method.methodId.value

    val base: Request[F[Throwable, _]] = if (input.body.value.productArity > 0) {
      Request(org.http4s.Method.POST, uri, body = body)
    } else {
      Request(org.http4s.Method.GET, uri)
    }

    transformRequest(base)
  }

  protected def transformRequest(request: Request[F[Throwable, _]]): Request[F[Throwable, _]] = request
}
