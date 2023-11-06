package izumi.idealingua.runtime.rpc

import izumi.functional.bio.IO2
import io.circe.{DecodingFailure, Json}

abstract class IRTCirceMarshaller {
  def encodeRequest: PartialFunction[IRTReqBody, Json]

  def encodeResponse: PartialFunction[IRTResBody, Json]

  def decodeRequest[Or[+_, +_]: IO2]: PartialFunction[IRTJsonBody, Or[DecodingFailure, IRTReqBody]]

  def decodeResponse[Or[+_, +_]: IO2]: PartialFunction[IRTJsonBody, Or[DecodingFailure, IRTResBody]]

  protected def decoded[Or[+_, +_]: IO2, V](result: Either[DecodingFailure, V]): Or[DecodingFailure, V] = {
    IO2[Or].fromEither(result)
  }
}
