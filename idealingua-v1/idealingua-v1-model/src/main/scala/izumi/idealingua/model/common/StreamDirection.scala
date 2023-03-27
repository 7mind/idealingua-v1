package izumi.idealingua.model.common

sealed trait StreamDirection

object StreamDirection {
  case object ToServer extends StreamDirection
  case object ToClient extends StreamDirection
}
