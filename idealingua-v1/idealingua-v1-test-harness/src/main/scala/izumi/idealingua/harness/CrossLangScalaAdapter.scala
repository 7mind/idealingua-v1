package izumi.idealingua.harness

import io.circe._
import io.circe.parser._
import io.circe.syntax._

/** PR-03.4 — In-JVM Scala "daemon" using WireDispatch.entries directly. T1 implementation. */
private[harness] object CrossLangScalaAdapter {

  final case class RoundtripResponse(
    id: String,
    ok: Boolean,
    kind: Option[String] = None,
    detail: Option[String] = None,
    reEncodedJson: Option[String] = None,
  )

  def roundtrip(id: String, wireId: String, json: String): RoundtripResponse = {
    WireDispatch.entries.get(wireId) match {
      case None =>
        RoundtripResponse(id = id, ok = false, kind = Some("UnknownWireId"), detail = Some(s"No dispatch entry for $wireId"))
      case Some(entry) =>
        parse(json) match {
          case Left(err) =>
            RoundtripResponse(id = id, ok = false, kind = Some("DecodeFailed"), detail = Some(s"parse: ${err.message}"))
          case Right(j) =>
            entry.decode(j) match {
              case Left(df) =>
                RoundtripResponse(id = id, ok = false, kind = Some("DecodeFailed"), detail = Some(df.getMessage))
              case Right(v) =>
                try {
                  val reEncoded = entry.encode(v).noSpaces
                  RoundtripResponse(id = id, ok = true, reEncodedJson = Some(reEncoded))
                } catch {
                  case e: Throwable =>
                    RoundtripResponse(id = id, ok = false, kind = Some("DecodeFailed"), detail = Some(s"encode: ${e.getMessage}"))
                }
            }
        }
    }
  }
}
