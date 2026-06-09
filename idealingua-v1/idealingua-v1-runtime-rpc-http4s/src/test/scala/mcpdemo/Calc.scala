package mcpdemo

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait CalcServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def add(ctx: C, a: Long, b: Long): Just[Long]
  def sub(ctx: C, a: Long, b: Long): Just[Long]
  def mul(ctx: C, a: Long, b: Long): Just[Long]
}

trait CalcClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def add(a: Long, b: Long): Just[Long]
  def sub(a: Long, b: Long): Just[Long]
  def mul(a: Long, b: Long): Just[Long]
}

class CalcWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends CalcClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.mcpdemo.Calc as _M
  def add(a: Long, b: Long): Just[Long] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.add.Input(a, b)), _M.add.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.add.Output), method) if method == _M.add.id =>
        _F.pure(v.value)
      case v =>
        val id = "Calc.CalcWrappedClient.add"
        val expected = classOf[_M.add.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def sub(a: Long, b: Long): Just[Long] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.sub.Input(a, b)), _M.sub.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.sub.Output), method) if method == _M.sub.id =>
        _F.pure(v.value)
      case v =>
        val id = "Calc.CalcWrappedClient.sub"
        val expected = classOf[_M.sub.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def mul(a: Long, b: Long): Just[Long] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.mul.Input(a, b)), _M.mul.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.mul.Output), method) if method == _M.mul.id =>
        _F.pure(v.value)
      case v =>
        val id = "Calc.CalcWrappedClient.mul"
        val expected = classOf[_M.mul.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object CalcWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(Calc.add.id -> CalcCodecs.add, Calc.sub.id -> CalcCodecs.sub, Calc.mul.id -> CalcCodecs.mul)
  }
}

class CalcWrappedServer[Or[+_, +_]: IRTIO2, C](_service: CalcServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = Calc.serviceId
  override def mcpResource: Option[McpServiceResource] = Some(CalcMcp.resource)
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](add, sub, mul).map(m => m.signature.id -> m).toMap
  }
  object add extends IRTMethodWrapper[Or, C] {
    import Calc.add.*
    val signature: Calc.add.type = Calc.add
    val marshaller: CalcCodecs.add.type = CalcCodecs.add
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.add(ctx, input.a, input.b))(v => new Output(v))
    }
  }
  object sub extends IRTMethodWrapper[Or, C] {
    import Calc.sub.*
    val signature: Calc.sub.type = Calc.sub
    val marshaller: CalcCodecs.sub.type = CalcCodecs.sub
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.sub(ctx, input.a, input.b))(v => new Output(v))
    }
  }
  object mul extends IRTMethodWrapper[Or, C] {
    import Calc.mul.*
    val signature: Calc.mul.type = Calc.mul
    val marshaller: CalcCodecs.mul.type = CalcCodecs.mul
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.mul(ctx, input.a, input.b))(v => new Output(v))
    }
  }
}

object CalcWrappedServer

object Calc {
  final val serviceId: IRTServiceId = IRTServiceId("Calc")
  object add extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("add"))
    type Input = AddInput
    type Output = AddOutput
  }
  object sub extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("sub"))
    type Input = SubInput
    type Output = SubOutput
  }
  object mul extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("mul"))
    type Input = MulInput
    type Output = MulOutput
  }
  final case class AddInput(a: Long, b: Long) extends Calc.AddInput.Defn
  trait AddInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAddInput: Encoder.AsObject[AddInput] = deriveEncoder[AddInput]
    implicit val decodeAddInput: Decoder[AddInput] = deriveDecoder[AddInput]
  }
  object AddInput extends Calc.AddInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Long
      def b: Long
    }
    def apply(a: Long, b: Long): Calc.AddInput = {
      new Calc.AddInput(a = a, b = b)
    }
    def apply(defn: Calc.AddInput.Defn): Calc.AddInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Calc.AddInput(a = defn.a, b = defn.b)
    }
    implicit object AddInput_cast_into_CalcSubInput extends izumi.idealingua.runtime.IRTCast[Calc.AddInput, Calc.SubInput] {
      override def convert(_value: Calc.AddInput): Calc.SubInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.SubInput(a = _value.a, b = _value.b)
      }
    }
    implicit object AddInput_cast_into_CalcMulInput extends izumi.idealingua.runtime.IRTCast[Calc.AddInput, Calc.MulInput] {
      override def convert(_value: Calc.AddInput): Calc.MulInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.MulInput(a = _value.a, b = _value.b)
      }
    }
    implicit object AddInput_upcast_AddInput extends izumi.idealingua.runtime.IRTCast[Calc.AddInput, Calc.AddInput] {
      override def convert(_value: Calc.AddInput): Calc.AddInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.AddInput(a = _value.a, b = _value.b)
      }
    }
    implicit class AddInputExtensions(override protected val _value: Calc.AddInput) extends izumi.idealingua.runtime.IRTConversions[Calc.AddInput]
  }
  final case class AddOutput(value: Long) extends AnyVal with Calc.AddOutput.Defn
  trait AddOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedAddOutput: Encoder[AddOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedAddOutput: Decoder[AddOutput] = Decoder.instance {
      v => v.as[Long].map(d => AddOutput(d))
    }
  }
  object AddOutput extends Calc.AddOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Long }
    def apply(value: Long): Calc.AddOutput = {
      new Calc.AddOutput(value = value)
    }
    def apply(defn: Calc.AddOutput.Defn): Calc.AddOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Calc.AddOutput(value = defn.value)
    }
    implicit object AddOutput_upcast_AddOutput extends izumi.idealingua.runtime.IRTCast[Calc.AddOutput, Calc.AddOutput] {
      override def convert(_value: Calc.AddOutput): Calc.AddOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.AddOutput(value = _value.value)
      }
    }
    implicit class AddOutputExtensions(override protected val _value: Calc.AddOutput) extends izumi.idealingua.runtime.IRTConversions[Calc.AddOutput]
  }
  final case class SubInput(a: Long, b: Long) extends Calc.SubInput.Defn
  trait SubInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSubInput: Encoder.AsObject[SubInput] = deriveEncoder[SubInput]
    implicit val decodeSubInput: Decoder[SubInput] = deriveDecoder[SubInput]
  }
  object SubInput extends Calc.SubInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Long
      def b: Long
    }
    def apply(a: Long, b: Long): Calc.SubInput = {
      new Calc.SubInput(a = a, b = b)
    }
    def apply(defn: Calc.SubInput.Defn): Calc.SubInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Calc.SubInput(a = defn.a, b = defn.b)
    }
    implicit object SubInput_cast_into_CalcAddInput extends izumi.idealingua.runtime.IRTCast[Calc.SubInput, Calc.AddInput] {
      override def convert(_value: Calc.SubInput): Calc.AddInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.AddInput(a = _value.a, b = _value.b)
      }
    }
    implicit object SubInput_cast_into_CalcMulInput extends izumi.idealingua.runtime.IRTCast[Calc.SubInput, Calc.MulInput] {
      override def convert(_value: Calc.SubInput): Calc.MulInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.MulInput(a = _value.a, b = _value.b)
      }
    }
    implicit object SubInput_upcast_SubInput extends izumi.idealingua.runtime.IRTCast[Calc.SubInput, Calc.SubInput] {
      override def convert(_value: Calc.SubInput): Calc.SubInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.SubInput(a = _value.a, b = _value.b)
      }
    }
    implicit class SubInputExtensions(override protected val _value: Calc.SubInput) extends izumi.idealingua.runtime.IRTConversions[Calc.SubInput]
  }
  final case class SubOutput(value: Long) extends AnyVal with Calc.SubOutput.Defn
  trait SubOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSubOutput: Encoder[SubOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSubOutput: Decoder[SubOutput] = Decoder.instance {
      v => v.as[Long].map(d => SubOutput(d))
    }
  }
  object SubOutput extends Calc.SubOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Long }
    def apply(value: Long): Calc.SubOutput = {
      new Calc.SubOutput(value = value)
    }
    def apply(defn: Calc.SubOutput.Defn): Calc.SubOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Calc.SubOutput(value = defn.value)
    }
    implicit object SubOutput_upcast_SubOutput extends izumi.idealingua.runtime.IRTCast[Calc.SubOutput, Calc.SubOutput] {
      override def convert(_value: Calc.SubOutput): Calc.SubOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.SubOutput(value = _value.value)
      }
    }
    implicit class SubOutputExtensions(override protected val _value: Calc.SubOutput) extends izumi.idealingua.runtime.IRTConversions[Calc.SubOutput]
  }
  final case class MulInput(a: Long, b: Long) extends Calc.MulInput.Defn
  trait MulInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeMulInput: Encoder.AsObject[MulInput] = deriveEncoder[MulInput]
    implicit val decodeMulInput: Decoder[MulInput] = deriveDecoder[MulInput]
  }
  object MulInput extends Calc.MulInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Long
      def b: Long
    }
    def apply(a: Long, b: Long): Calc.MulInput = {
      new Calc.MulInput(a = a, b = b)
    }
    def apply(defn: Calc.MulInput.Defn): Calc.MulInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Calc.MulInput(a = defn.a, b = defn.b)
    }
    implicit object MulInput_cast_into_CalcAddInput extends izumi.idealingua.runtime.IRTCast[Calc.MulInput, Calc.AddInput] {
      override def convert(_value: Calc.MulInput): Calc.AddInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.AddInput(a = _value.a, b = _value.b)
      }
    }
    implicit object MulInput_cast_into_CalcSubInput extends izumi.idealingua.runtime.IRTCast[Calc.MulInput, Calc.SubInput] {
      override def convert(_value: Calc.MulInput): Calc.SubInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.SubInput(a = _value.a, b = _value.b)
      }
    }
    implicit object MulInput_upcast_MulInput extends izumi.idealingua.runtime.IRTCast[Calc.MulInput, Calc.MulInput] {
      override def convert(_value: Calc.MulInput): Calc.MulInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.MulInput(a = _value.a, b = _value.b)
      }
    }
    implicit class MulInputExtensions(override protected val _value: Calc.MulInput) extends izumi.idealingua.runtime.IRTConversions[Calc.MulInput]
  }
  final case class MulOutput(value: Long) extends AnyVal with Calc.MulOutput.Defn
  trait MulOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedMulOutput: Encoder[MulOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedMulOutput: Decoder[MulOutput] = Decoder.instance {
      v => v.as[Long].map(d => MulOutput(d))
    }
  }
  object MulOutput extends Calc.MulOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Long }
    def apply(value: Long): Calc.MulOutput = {
      new Calc.MulOutput(value = value)
    }
    def apply(defn: Calc.MulOutput.Defn): Calc.MulOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Calc.MulOutput(value = defn.value)
    }
    implicit object MulOutput_upcast_MulOutput extends izumi.idealingua.runtime.IRTCast[Calc.MulOutput, Calc.MulOutput] {
      override def convert(_value: Calc.MulOutput): Calc.MulOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Calc.MulOutput(value = _value.value)
      }
    }
    implicit class MulOutputExtensions(override protected val _value: Calc.MulOutput) extends izumi.idealingua.runtime.IRTConversions[Calc.MulOutput]
  }
}

object CalcCodecs {
  object add extends IRTCirceMarshaller {
    import Calc.add.*
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
  object sub extends IRTCirceMarshaller {
    import Calc.sub.*
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
  object mul extends IRTCirceMarshaller {
    import Calc.mul.*
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
