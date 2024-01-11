package izumi.r2.idealingua.test.generated

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import izumi.functional.bio.IO2 as IRTIO2
import izumi.idealingua.runtime.rpc.*

trait ProtectedTestServiceServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def test(ctx: C, str: String): Just[String]
}

trait ProtectedTestServiceClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def test(str: String): Just[String]
}

class ProtectedTestServiceWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends ProtectedTestServiceClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import izumi.r2.idealingua.test.generated.ProtectedTestService as _M
  def test(str: String): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.test.Input(str)), _M.test.id)))(
      {
        err => _F.terminate(err)
      },
      {
        case IRTMuxResponse(IRTResBody(v: _M.test.Output), method) if method == _M.test.id =>
          _F.pure(v.value)
        case v =>
          val id       = "ProtectedTestService.ProtectedTestServiceWrappedClient.test"
          val expected = classOf[_M.test.Input].toString
          _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${v.getClass}", v, None))
      },
    )
  }
}

object ProtectedTestServiceWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(ProtectedTestService.test.id -> ProtectedTestServiceCodecs.test)
  }
}

class ProtectedTestServiceWrappedServer[Or[+_, +_]: IRTIO2, C](_service: ProtectedTestServiceServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or]          = implicitly
  final val serviceId: IRTServiceId = ProtectedTestService.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](test).map(m => m.signature.id -> m).toMap
  }
  object test extends IRTMethodWrapper[Or, C] {
    import ProtectedTestService.test.*
    val signature: ProtectedTestService.test.type        = ProtectedTestService.test
    val marshaller: ProtectedTestServiceCodecs.test.type = ProtectedTestServiceCodecs.test
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.test(ctx, input.str))(v => new Output(v))
    }
  }
}

object ProtectedTestServiceWrappedServer

object ProtectedTestService {
  final val serviceId: IRTServiceId = IRTServiceId("ProtectedTestService")
  object test extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("test"))
    type Input  = TestInput
    type Output = TestOutput
  }
  final case class TestInput(str: String)
  object TestInput {
    implicit val encodeTestInput: Encoder.AsObject[TestInput] = deriveEncoder[TestInput]
    implicit val decodeTestInput: Decoder[TestInput]          = deriveDecoder[TestInput]
  }
  final case class TestOutput(value: String)
  object TestOutput {
    implicit val encodeUnwrappedTestOutput: Encoder[TestOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedTestOutput: Decoder[TestOutput] = Decoder.instance {
      v => v.as[String].map(d => TestOutput(d))
    }
  }
}

object ProtectedTestServiceCodecs {
  object test extends IRTCirceMarshaller {
    import ProtectedTestService.test.*
    def encodeRequest: PartialFunction[IRTReqBody, Json] = {
      case IRTReqBody(value: Input) =>
        value.asJson
    }
    def decodeRequest[Or[+_, +_]: IRTIO2]: PartialFunction[IRTJsonBody, Or[DecodingFailure, IRTReqBody]] = {
      case IRTJsonBody(m, packet) if m == id =>
        this.decoded[Or, IRTReqBody](packet.as[Input].map(v => IRTReqBody(v)))
    }
    def encodeResponse: PartialFunction[IRTResBody, Json] = {
      case IRTResBody(value: Output) =>
        value.asJson
    }
    def decodeResponse[Or[+_, +_]: IRTIO2]: PartialFunction[IRTJsonBody, Or[DecodingFailure, IRTResBody]] = {
      case IRTJsonBody(m, packet) if m == id =>
        decoded[Or, IRTResBody](packet.as[Output].map(v => IRTResBody(v)))
    }
  }
}
