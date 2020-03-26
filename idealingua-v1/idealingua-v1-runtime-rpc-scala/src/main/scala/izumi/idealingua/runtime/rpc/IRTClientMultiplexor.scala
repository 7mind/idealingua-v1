package izumi.idealingua.runtime.rpc

import io.circe.Json
import izumi.functional.bio.{BIO, F}

trait IRTClientMultiplexor[F[+ _, + _]] {
  def encode(input: IRTMuxRequest): F[Throwable, Json]
  def decode(input: Json, method: IRTMethodId): F[Throwable, IRTMuxResponse]
}

class IRTClientMultiplexorImpl[F[+ _, + _] : BIO](clients: Set[IRTWrappedClient]) extends IRTClientMultiplexor[F] {
  val codecs: Map[IRTMethodId, IRTCirceMarshaller] = clients.flatMap(_.allCodecs).toMap

  def encode(input: IRTMuxRequest): F[Throwable, Json] = {
    codecs.get(input.method) match {
      case Some(marshaller) =>
        F.syncThrowable(marshaller.encodeRequest(input.body))
      case None =>
        F.fail(new IRTMissingHandlerException(s"No codec for $input", input, None))
    }
  }

  def decode(input: Json, method: IRTMethodId): F[Throwable, IRTMuxResponse] = {
    codecs.get(method) match {
      case Some(marshaller) =>
        for {
          decoder <- F.syncThrowable(marshaller.decodeResponse[F].apply(IRTJsonBody(method, input)))
          body <- decoder
        } yield {
          IRTMuxResponse(body, method)
        }

      case None =>
        F.fail(new IRTMissingHandlerException(s"No codec for $method, input=$input", input, None))
    }
  }
}
