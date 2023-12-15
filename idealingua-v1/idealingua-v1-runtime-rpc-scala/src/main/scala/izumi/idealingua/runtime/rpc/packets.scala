package izumi.idealingua.runtime.rpc

import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import izumi.fundamentals.platform.uuid.UUIDGen

sealed trait RPCPacketKind

trait RPCPacketKindCirce {

  import _root_.io.circe.{Decoder, Encoder, KeyDecoder, KeyEncoder}

  implicit val encodeTestEnum: Encoder[RPCPacketKind]       = Encoder.encodeString.contramap(_.toString)
  implicit val decodeTestEnum: Decoder[RPCPacketKind]       = Decoder.decodeString.map(RPCPacketKind.parse)
  implicit val encodeKeyTestEnum: KeyEncoder[RPCPacketKind] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyTestEnum: KeyDecoder[RPCPacketKind] = KeyDecoder.decodeKeyString.map(RPCPacketKind.parse)
}

object RPCPacketKind extends RPCPacketKindCirce {
  type Element = RPCPacketKind

  def all: Map[String, Element] = Seq(
    RpcRequest,
    RpcResponse,
    RpcFail,
    BuzzRequest,
    BuzzResponse,
    BuzzFailure,
    S2CStream,
    C2SStream,
    Fail,
  ).map(e => e.toString -> e).toMap

  def parse(value: String): RPCPacketKind = all(value)

  case object Fail extends RPCPacketKind {
    override def toString: String = "?:failure"
  }

  case object RpcRequest extends RPCPacketKind {
    override def toString: String = "rpc:request"
  }

  case object RpcResponse extends RPCPacketKind {
    override def toString: String = "rpc:response"
  }

  case object RpcFail extends RPCPacketKind {
    override def toString: String = "rpc:failure"
  }

  case object BuzzRequest extends RPCPacketKind {
    override def toString: String = "buzzer:request"
  }

  case object BuzzResponse extends RPCPacketKind {
    override def toString: String = "buzzer:response"
  }

  case object BuzzFailure extends RPCPacketKind {
    override def toString: String = "buzzer:failure"
  }

  case object S2CStream extends RPCPacketKind {
    override def toString: String = "stream:s2c"
  }

  case object C2SStream extends RPCPacketKind {
    override def toString: String = "stream:c2s"
  }

}

case class RpcPacketId(v: String)

object RpcPacketId {
  def random(): RpcPacketId = RpcPacketId(UUIDGen.getTimeUUID().toString)

  implicit def dec0: Decoder[RpcPacketId] = Decoder.decodeString.map(RpcPacketId.apply)
  implicit def enc0: Encoder[RpcPacketId] = Encoder.encodeString.contramap(_.v)
}

case class RpcPacket(
  kind: RPCPacketKind,
  data: Option[Json],
  id: Option[RpcPacketId],
  ref: Option[RpcPacketId],
  service: Option[String],
  method: Option[String],
  headers: Option[Map[String, String]],
) {
  def withHeaders(h: Map[String, String]): RpcPacket = {
    copy(headers = Option(h).filter(_.nonEmpty))
  }
}

object RpcPacket {
  implicit def dec0: Decoder[RpcPacket] = deriveDecoder

  implicit def enc0: Encoder[RpcPacket] = deriveEncoder

  def rpcCritical(error: String, ref: Option[RpcPacketId]): RpcPacket = {
    RpcPacket(RPCPacketKind.Fail, Some(Json.obj("cause" -> error.asJson)), None, ref, None, None, None)
  }

  def auth(id: RpcPacketId, headers: Map[String, String]): RpcPacket = {
    RpcPacket(RPCPacketKind.RpcRequest, None, Some(id), None, None, None, Some(headers))
  }

  def rpcRequest(id: RpcPacketId, method: IRTMethodId, data: Json): RpcPacket = {
    RpcPacket(RPCPacketKind.RpcRequest, Some(data), Some(id), None, Some(method.service.value), Some(method.methodId.value), None)
  }

  def rpcResponse(ref: RpcPacketId, data: Json): RpcPacket = {
    RpcPacket(RPCPacketKind.RpcResponse, Some(data), None, Some(ref), None, None, None)
  }

  def rpcFail(ref: Option[RpcPacketId], cause: String): RpcPacket = {
    RpcPacket(RPCPacketKind.RpcFail, Some(Json.obj("cause" -> Json.fromString(cause))), None, ref, None, None, None)
  }

  def buzzerRequest(id: RpcPacketId, method: IRTMethodId, data: Json): RpcPacket = {
    RpcPacket(RPCPacketKind.BuzzRequest, Some(data), Some(id), None, Some(method.service.value), Some(method.methodId.value), None)
  }

  def buzzerResponse(ref: RpcPacketId, data: Json): RpcPacket = {
    RpcPacket(RPCPacketKind.BuzzResponse, Some(data), None, Some(ref), None, None, None)
  }

  def buzzerFail(ref: Option[RpcPacketId], cause: String): RpcPacket = {
    RpcPacket(RPCPacketKind.BuzzFailure, Some(Json.obj("cause" -> Json.fromString(cause))), None, ref, None, None, None)
  }
}
