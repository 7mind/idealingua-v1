package izumi.test.domain01

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait OptionalServiceServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def optionalMethod(ctx: C, a: Option[NestedClass], b: Option[Int], c: Option[NestedClass]): Just[Int]
}

trait OptionalServiceClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def optionalMethod(a: Option[NestedClass], b: Option[Int], c: Option[NestedClass]): Just[Int]
}

class OptionalServiceWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends OptionalServiceClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.izumi.test.domain01.OptionalService as _M
  def optionalMethod(a: Option[NestedClass], b: Option[Int], c: Option[NestedClass]): Just[Int] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.optionalMethod.Input(a, b, c)), _M.optionalMethod.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.optionalMethod.Output), method) if method == _M.optionalMethod.id =>
        _F.pure(v.value)
      case v =>
        val id = "OptionalService.OptionalServiceWrappedClient.optionalMethod"
        val expected = classOf[_M.optionalMethod.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object OptionalServiceWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(OptionalService.optionalMethod.id -> OptionalServiceCodecs.optionalMethod)
  }
}

class OptionalServiceWrappedServer[Or[+_, +_]: IRTIO2, C](_service: OptionalServiceServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = OptionalService.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](optionalMethod).map(m => m.signature.id -> m).toMap
  }
  object optionalMethod extends IRTMethodWrapper[Or, C] {
    import OptionalService.optionalMethod.*
    val signature: OptionalService.optionalMethod.type = OptionalService.optionalMethod
    val marshaller: OptionalServiceCodecs.optionalMethod.type = OptionalServiceCodecs.optionalMethod
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.optionalMethod(ctx, input.a, input.b, input.c))(v => new Output(v))
    }
  }
}

object OptionalServiceWrappedServer

object OptionalService {
  final val serviceId: IRTServiceId = IRTServiceId("OptionalService")
  object optionalMethod extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("optionalMethod"))
    type Input = OptionalMethodInput
    type Output = OptionalMethodOutput
  }
  final case class OptionalMethodInput(a: Option[NestedClass], b: Option[Int], c: Option[NestedClass]) extends OptionalService.OptionalMethodInput.Defn
  trait OptionalMethodInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeOptionalMethodInput: Encoder.AsObject[OptionalMethodInput] = deriveEncoder[OptionalMethodInput]
    implicit val decodeOptionalMethodInput: Decoder[OptionalMethodInput] = deriveDecoder[OptionalMethodInput]
  }
  object OptionalMethodInput extends OptionalService.OptionalMethodInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Option[NestedClass]
      def b: Option[Int]
      def c: Option[NestedClass]
    }
    def apply(a: Option[NestedClass], b: Option[Int], c: Option[NestedClass]): OptionalService.OptionalMethodInput = {
      new OptionalService.OptionalMethodInput(a = a, b = b, c = c)
    }
    def apply(defn: OptionalService.OptionalMethodInput.Defn): OptionalService.OptionalMethodInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new OptionalService.OptionalMethodInput(a = defn.a, b = defn.b, c = defn.c)
    }
    implicit object OptionalMethodInput_upcast_OptionalMethodInput extends izumi.idealingua.runtime.IRTCast[OptionalService.OptionalMethodInput, OptionalService.OptionalMethodInput] {
      override def convert(_value: OptionalService.OptionalMethodInput): OptionalService.OptionalMethodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        OptionalService.OptionalMethodInput(a = _value.a, b = _value.b, c = _value.c)
      }
    }
    implicit class OptionalMethodInputExtensions(override protected val _value: OptionalService.OptionalMethodInput) extends izumi.idealingua.runtime.IRTConversions[OptionalService.OptionalMethodInput]
  }
  final case class OptionalMethodOutput(value: Int) extends AnyVal with OptionalService.OptionalMethodOutput.Defn
  trait OptionalMethodOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedOptionalMethodOutput: Encoder[OptionalMethodOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedOptionalMethodOutput: Decoder[OptionalMethodOutput] = Decoder.instance {
      v => v.as[Int].map(d => OptionalMethodOutput(d))
    }
  }
  object OptionalMethodOutput extends OptionalService.OptionalMethodOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Int }
    def apply(value: Int): OptionalService.OptionalMethodOutput = {
      new OptionalService.OptionalMethodOutput(value = value)
    }
    def apply(defn: OptionalService.OptionalMethodOutput.Defn): OptionalService.OptionalMethodOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new OptionalService.OptionalMethodOutput(value = defn.value)
    }
    implicit object OptionalMethodOutput_upcast_OptionalMethodOutput extends izumi.idealingua.runtime.IRTCast[OptionalService.OptionalMethodOutput, OptionalService.OptionalMethodOutput] {
      override def convert(_value: OptionalService.OptionalMethodOutput): OptionalService.OptionalMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        OptionalService.OptionalMethodOutput(value = _value.value)
      }
    }
    implicit class OptionalMethodOutputExtensions(override protected val _value: OptionalService.OptionalMethodOutput) extends izumi.idealingua.runtime.IRTConversions[OptionalService.OptionalMethodOutput]
  }
}

object OptionalServiceCodecs {
  object optionalMethod extends IRTCirceMarshaller {
    import OptionalService.optionalMethod.*
    def encodeRequest: PartialFunction[IRTReqBody, IRTJson] = {
      case IRTReqBody(value: Input) =>
        value.asJson
    }
    def decodeRequest[Or[+_, +_]: IRTIO2]: PartialFunction[IRTJsonBody, Or[IRTDecodingFailure, IRTReqBody]] = {
      case IRTJsonBody(m, packet) if m == id =>
        this.decoded[Or, IRTReqBody](packet.as[Input].map(v => IRTReqBody(v)))
    }
    def encodeResponse: PartialFunction[IRTResBody, IRTJson] = {
      case IRTResBody(value: Output) =>
        value.asJson
    }
    def decodeResponse[Or[+_, +_]: IRTIO2]: PartialFunction[IRTJsonBody, Or[IRTDecodingFailure, IRTResBody]] = {
      case IRTJsonBody(m, packet) if m == id =>
        decoded[Or, IRTResBody](packet.as[Output].map(v => IRTResBody(v)))
    }
  }
}
       