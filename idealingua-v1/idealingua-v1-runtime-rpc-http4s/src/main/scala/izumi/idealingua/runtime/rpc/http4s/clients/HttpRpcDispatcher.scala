package izumi.idealingua.runtime.rpc.http4s.clients

import cats.effect.Async
import io.circe
import io.circe.parser.parse
import izumi.functional.bio.{Exit, F, IO2}
import izumi.idealingua.runtime.rpc.*
import izumi.idealingua.runtime.rpc.http4s.IRTUnexpectedHttpStatus
import izumi.idealingua.runtime.rpc.http4s.clients.HttpRpcDispatcher.IRTDispatcherRaw
import logstage.LogIO2
import org.http4s.client.Client
import org.http4s.{Entity, Request, Response, Status, Uri}
import scodec.bits.ByteVector

class HttpRpcDispatcher[F[+_, +_]: IO2](
  client: Client[F[Throwable, _]],
  uri: Uri,
  codec: IRTClientMultiplexor[F],
  printer: circe.Printer,
  logger: LogIO2[F],
)(implicit AT: Async[F[Throwable, _]]
) extends IRTDispatcherRaw[F] {

  override def dispatchRaw(method: IRTMethodId, request: String): F[Throwable, IRTMuxResponse] = {
    dispatchRawWith(uri, codec)(method, request)
  }

  override def dispatch(input: IRTMuxRequest): F[Throwable, IRTMuxResponse] = {
    dispatchWith(uri, codec)(input)
  }

  protected def dispatchRawWith(
    uri: Uri,
    codec: IRTClientMultiplexor[F],
  )(method: IRTMethodId,
    request: String,
  ): F[Throwable, IRTMuxResponse] = {
    for {
      req <- F.sync(buildRequest(uri)(method, request.getBytes))
      _   <- logger.trace(s"$method: Prepared request $req")
      res <- client.run(req).use(handleResponse(codec, method))
    } yield res
  }

  protected def dispatchWith(
    uri: Uri,
    codec: IRTClientMultiplexor[F],
  )(request: IRTMuxRequest
  ): F[Throwable, IRTMuxResponse] = {
    for {
      _       <- logger.trace(s"${request.method -> "method"}: Going to perform $request")
      encoded <- codec.encode(request)
      res     <- dispatchRawWith(uri, codec)(request.method, printer.print(encoded))
    } yield res
  }

  protected def handleResponse(
    codec: IRTClientMultiplexor[F],
    method: IRTMethodId,
  )(resp: Response[F[Throwable, _]]
  ): F[Throwable, IRTMuxResponse] = {
    for {
      _ <- logger.trace(s"$method: Received response, going to materialize, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}")
      _ <- F.when(resp.status != Status.Ok) {
        logger.info(s"$method: unexpected HTTP response, ${resp.status.code -> "code"} ${resp.status.reason -> "reason"}") *>
        F.fail(IRTUnexpectedHttpStatus(resp.status))
      }
      body <- resp.as[String]
      _    <- logger.trace(s"$method: Received response: $body")
      res <- (for {
        parsed  <- F.fromEither(parse(body))
        product <- codec.decode(parsed, method)
        _       <- logger.trace(s"$method: decoded response: $product")
      } yield product).sandbox.leftFlatMap {
        case Exit.Error(error, trace) =>
          logger
            .info(s"$method: decoder returned failure on $body: $error $trace")
            .as(new IRTUnparseableDataException(s"$method: decoder returned failure on body=$body: error=$error trace=$trace", Option(error)))

        case Exit.Termination(f, _, trace) =>
          logger
            .info(s"$method: decoder failed on $body: $f $trace")
            .as(new IRTUnparseableDataException(s"$method: decoder failed on body=$body: f=$f trace=$trace", Option(f)))
      }
    } yield res
  }

  protected def buildRequest(
    baseUri: Uri
  )(method: IRTMethodId,
    body: Array[Byte],
  ): Request[F[Throwable, _]] = {
    val uri = baseUri / method.service.value / method.methodId.value
    if (body.nonEmpty) {
      Request[F[Throwable, _]](org.http4s.Method.POST, uri, entity = Entity.strict(ByteVector.view(body)))
    } else {
      Request[F[Throwable, _]](org.http4s.Method.GET, uri)
    }
  }
}

object HttpRpcDispatcher {
  trait IRTDispatcherRaw[F[_, _]] extends IRTDispatcher[F] {
    def dispatchRaw(method: IRTMethodId, body: String): F[Throwable, IRTMuxResponse]
  }
}
