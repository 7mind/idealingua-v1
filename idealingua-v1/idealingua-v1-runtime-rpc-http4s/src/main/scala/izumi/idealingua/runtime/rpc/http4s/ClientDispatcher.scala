package izumi.idealingua.runtime.rpc.http4s

import izumi.functional.bio.IO2
import izumi.functional.bio.Exit
import izumi.idealingua.runtime.rpc._
import izumi.logstage.api.IzLogger
import fs2.Stream
import io.circe
import io.circe.parser.parse
import org.http4s._
import org.http4s.blaze.client._

class ClientDispatcher[C <: Http4sContext]
(
  val c: C#IMPL[C]
, logger: IzLogger
, printer: circe.Printer
, baseUri: Uri
, codec: IRTClientMultiplexor[C#BiIO]
) extends IRTDispatcher[C#BiIO] {
  import c._

  def dispatch(request: IRTMuxRequest): BiIO[Throwable, IRTMuxResponse] = {
    val handler = handleResponse(request, _: Response[MonoIO])

    logger.trace(s"${request.method -> "method"}: Goint to perform $request")

    codec.encode(request)
      .flatMap {
        encoded =>
          val outBytes: Array[Byte] = printer.print(encoded).getBytes
          val req = buildRequest(baseUri, request, outBytes)

          logger.debug(s"${request.method -> "method"}: Prepared request $encoded")
          runRequest[IRTMuxResponse](handler, req)
      }
  }

  protected def runRequest[T](handler: Response[MonoIO] => MonoIO[T], req: Request[MonoIO]): BiIO[Throwable, T] = {
    val clientBuilder = blazeClientBuilder(BlazeClientBuilder[MonoIO](c.clientExecutionContext))
    clientBuilder.resource.use {
      _.run(req).use(handler)
    }
  }

  protected def blazeClientBuilder(defaultBuilder: BlazeClientBuilder[MonoIO]): BlazeClientBuilder[MonoIO] = defaultBuilder

  protected def handleResponse(input: IRTMuxRequest, resp: Response[MonoIO]): MonoIO[IRTMuxResponse] = {
    logger.trace(s"${input.method -> "method"}: Received response, going to materialize, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")

    if (resp.status != Status.Ok) {
      logger.info(s"${input.method -> "method"}: unexpected HTTP response, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
      F.fail(IRTUnexpectedHttpStatus(resp.status))
    } else {
      resp
      .as[MaterializedStream]
      .flatMap {
        body =>
          logger.trace(s"${input.method -> "method"}: Received response: $body")
          val decoded = for {
            parsed <- c.F.fromEither(parse(body))
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

            case Exit.Interruption(error, trace) =>
              logger.info(s"${input.method -> "method"}: decoder interrupted on $body: $error $trace")
              F.fail(new IRTUnparseableDataException(s"${input.method}: decoder interrupted on body=$body: error=$error trace=$trace", Option(error)))
          }
      }
    }
  }

  protected final def buildRequest(baseUri: Uri, input: IRTMuxRequest, body: Array[Byte]): Request[MonoIO] = {
    val entityBody: EntityBody[MonoIO] = Stream.emits(body).covary[MonoIO]
    buildRequest(baseUri, input, entityBody)
  }

  protected final def buildRequest(baseUri: Uri, input: IRTMuxRequest, body: EntityBody[MonoIO]): Request[MonoIO] = {
    val uri = baseUri / input.method.service.value / input.method.methodId.value

    val base: Request[MonoIO] = if (input.body.value.productArity > 0) {
      Request(org.http4s.Method.POST, uri, body = body)
    } else {
      Request(org.http4s.Method.GET, uri)
    }

    transformRequest(base)
  }

  protected def transformRequest(request: Request[MonoIO]): Request[MonoIO] = request
}
