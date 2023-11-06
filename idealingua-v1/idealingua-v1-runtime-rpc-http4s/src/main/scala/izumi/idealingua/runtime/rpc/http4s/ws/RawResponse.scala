package izumi.idealingua.runtime.rpc.http4s.ws

import io.circe.Json
import izumi.idealingua.runtime.rpc.*

sealed trait RawResponse

object RawResponse {
  final case class EmptyRawResponse() extends RawResponse
  final case class GoodRawResponse(data: Json, method: IRTMethodId) extends RawResponse
  final case class BadRawResponse(data: Option[Json]) extends RawResponse {
    def error: String = data.map(_.noSpaces).getOrElse("N/A")
  }
}
