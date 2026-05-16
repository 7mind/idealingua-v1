package mcpdemo

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait ShapesServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def ping(ctx: C): Just[Shapes.ping.Output]
  def upper(ctx: C, s: String): Just[String]
  def add(ctx: C, a: Long, b: Long): Just[Long]
  def echo(ctx: C, req: EchoRequest): Just[EchoResponse]
  def divmod(ctx: C, a: Long, b: Long): Just[Shapes.divmod.Output]
  def reverse(ctx: C, items: List[String]): Just[List[String]]
  def invertMap(ctx: C, m: Map[String, String]): Just[Map[String, String]]
  def maybeUpper(ctx: C, s: Option[String]): Just[Option[String]]
  def nextColor(ctx: C, c: Color): Just[ColorResult]
  def makeProfile(ctx: C, name: String, age: Int, color: Color): Just[Profile]
  def pay(ctx: C, amount: Long): Just[PaymentResult]
  def divideSafe(ctx: C, a: Long, b: Long): Or[ServiceError, Long]
  def noteValue(ctx: C, v: Int): Or[ServiceError, Shapes.NoteValueSuccess]
}

trait ShapesClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def ping(): Just[Shapes.ping.Output]
  def upper(s: String): Just[String]
  def add(a: Long, b: Long): Just[Long]
  def echo(req: EchoRequest): Just[EchoResponse]
  def divmod(a: Long, b: Long): Just[Shapes.divmod.Output]
  def reverse(items: List[String]): Just[List[String]]
  def invertMap(m: Map[String, String]): Just[Map[String, String]]
  def maybeUpper(s: Option[String]): Just[Option[String]]
  def nextColor(c: Color): Just[ColorResult]
  def makeProfile(name: String, age: Int, color: Color): Just[Profile]
  def pay(amount: Long): Just[PaymentResult]
  def divideSafe(a: Long, b: Long): Or[ServiceError, Long]
  def noteValue(v: Int): Or[ServiceError, Shapes.NoteValueSuccess]
}

class ShapesWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends ShapesClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.mcpdemo.Shapes as _M
  def ping(): Just[Shapes.ping.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.ping.Input()), _M.ping.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.ping.Output), method) if method == _M.ping.id =>
        _F.pure(v)
      case v =>
        val id = "Shapes.ShapesWrappedClient.ping"
        val expected = classOf[_M.ping.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def upper(s: String): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.upper.Input(s)), _M.upper.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.upper.Output), method) if method == _M.upper.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.upper"
        val expected = classOf[_M.upper.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def add(a: Long, b: Long): Just[Long] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.add.Input(a, b)), _M.add.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.add.Output), method) if method == _M.add.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.add"
        val expected = classOf[_M.add.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def echo(req: EchoRequest): Just[EchoResponse] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.echo.Input(req)), _M.echo.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.echo.Output), method) if method == _M.echo.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.echo"
        val expected = classOf[_M.echo.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def divmod(a: Long, b: Long): Just[Shapes.divmod.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.divmod.Input(a, b)), _M.divmod.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.divmod.Output), method) if method == _M.divmod.id =>
        _F.pure(v)
      case v =>
        val id = "Shapes.ShapesWrappedClient.divmod"
        val expected = classOf[_M.divmod.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def reverse(items: List[String]): Just[List[String]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.reverse.Input(items)), _M.reverse.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.reverse.Output), method) if method == _M.reverse.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.reverse"
        val expected = classOf[_M.reverse.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def invertMap(m: Map[String, String]): Just[Map[String, String]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.invertMap.Input(m)), _M.invertMap.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.invertMap.Output), method) if method == _M.invertMap.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.invertMap"
        val expected = classOf[_M.invertMap.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def maybeUpper(s: Option[String]): Just[Option[String]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.maybeUpper.Input(s)), _M.maybeUpper.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.maybeUpper.Output), method) if method == _M.maybeUpper.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.maybeUpper"
        val expected = classOf[_M.maybeUpper.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def nextColor(c: Color): Just[ColorResult] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.nextColor.Input(c)), _M.nextColor.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.nextColor.Output), method) if method == _M.nextColor.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.nextColor"
        val expected = classOf[_M.nextColor.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def makeProfile(name: String, age: Int, color: Color): Just[Profile] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.makeProfile.Input(name, age, color)), _M.makeProfile.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.makeProfile.Output), method) if method == _M.makeProfile.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.makeProfile"
        val expected = classOf[_M.makeProfile.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def pay(amount: Long): Just[PaymentResult] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.pay.Input(amount)), _M.pay.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.pay.Output), method) if method == _M.pay.id =>
        _F.pure(v.value)
      case v =>
        val id = "Shapes.ShapesWrappedClient.pay"
        val expected = classOf[_M.pay.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def divideSafe(a: Long, b: Long): Or[ServiceError, Long] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.divideSafe.Input(a, b)), _M.divideSafe.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(r), method) if method == _M.divideSafe.id =>
        r match {
          case va: Shapes.DivideSafeOutput.Failure =>
            _F.fail(va.value)
          case va: Shapes.DivideSafeOutput.Success =>
            _F.pure(va.value)
          case v =>
            val id = "Shapes.ShapesWrappedClient.divideSafe"
            val expected = classOf[_M.divideSafe.Input].toString
            _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
              v.getClass
            }", v, None))
        }
      case v =>
        val id = "Shapes.ShapesWrappedClient.divideSafe"
        val expected = classOf[_M.divideSafe.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def noteValue(v: Int): Or[ServiceError, Shapes.NoteValueSuccess] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.noteValue.Input(v)), _M.noteValue.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(r), method) if method == _M.noteValue.id =>
        r match {
          case va: Shapes.NoteValueOutput.Failure =>
            _F.fail(va.value)
          case va: Shapes.NoteValueOutput.Success =>
            _F.pure(va.value)
          case v =>
            val id = "Shapes.ShapesWrappedClient.noteValue"
            val expected = classOf[_M.noteValue.Input].toString
            _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
              v.getClass
            }", v, None))
        }
      case v =>
        val id = "Shapes.ShapesWrappedClient.noteValue"
        val expected = classOf[_M.noteValue.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object ShapesWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(Shapes.ping.id -> ShapesCodecs.ping, Shapes.upper.id -> ShapesCodecs.upper, Shapes.add.id -> ShapesCodecs.add, Shapes.echo.id -> ShapesCodecs.echo, Shapes.divmod.id -> ShapesCodecs.divmod, Shapes.reverse.id -> ShapesCodecs.reverse, Shapes.invertMap.id -> ShapesCodecs.invertMap, Shapes.maybeUpper.id -> ShapesCodecs.maybeUpper, Shapes.nextColor.id -> ShapesCodecs.nextColor, Shapes.makeProfile.id -> ShapesCodecs.makeProfile, Shapes.pay.id -> ShapesCodecs.pay, Shapes.divideSafe.id -> ShapesCodecs.divideSafe, Shapes.noteValue.id -> ShapesCodecs.noteValue)
  }
}

class ShapesWrappedServer[Or[+_, +_]: IRTIO2, C](_service: ShapesServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = Shapes.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](ping, upper, add, echo, divmod, reverse, invertMap, maybeUpper, nextColor, makeProfile, pay, divideSafe, noteValue).map(m => m.signature.id -> m).toMap
  }
  object ping extends IRTMethodWrapper[Or, C] {
    import Shapes.ping.*
    val signature: Shapes.ping.type = Shapes.ping
    val marshaller: ShapesCodecs.ping.type = ShapesCodecs.ping
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.ping(ctx)
    }
  }
  object upper extends IRTMethodWrapper[Or, C] {
    import Shapes.upper.*
    val signature: Shapes.upper.type = Shapes.upper
    val marshaller: ShapesCodecs.upper.type = ShapesCodecs.upper
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.upper(ctx, input.s))(v => new Output(v))
    }
  }
  object add extends IRTMethodWrapper[Or, C] {
    import Shapes.add.*
    val signature: Shapes.add.type = Shapes.add
    val marshaller: ShapesCodecs.add.type = ShapesCodecs.add
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.add(ctx, input.a, input.b))(v => new Output(v))
    }
  }
  object echo extends IRTMethodWrapper[Or, C] {
    import Shapes.echo.*
    val signature: Shapes.echo.type = Shapes.echo
    val marshaller: ShapesCodecs.echo.type = ShapesCodecs.echo
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.echo(ctx, input.req))(v => new Output(v))
    }
  }
  object divmod extends IRTMethodWrapper[Or, C] {
    import Shapes.divmod.*
    val signature: Shapes.divmod.type = Shapes.divmod
    val marshaller: ShapesCodecs.divmod.type = ShapesCodecs.divmod
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.divmod(ctx, input.a, input.b)
    }
  }
  object reverse extends IRTMethodWrapper[Or, C] {
    import Shapes.reverse.*
    val signature: Shapes.reverse.type = Shapes.reverse
    val marshaller: ShapesCodecs.reverse.type = ShapesCodecs.reverse
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.reverse(ctx, input.items))(v => new Output(v))
    }
  }
  object invertMap extends IRTMethodWrapper[Or, C] {
    import Shapes.invertMap.*
    val signature: Shapes.invertMap.type = Shapes.invertMap
    val marshaller: ShapesCodecs.invertMap.type = ShapesCodecs.invertMap
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.invertMap(ctx, input.m))(v => new Output(v))
    }
  }
  object maybeUpper extends IRTMethodWrapper[Or, C] {
    import Shapes.maybeUpper.*
    val signature: Shapes.maybeUpper.type = Shapes.maybeUpper
    val marshaller: ShapesCodecs.maybeUpper.type = ShapesCodecs.maybeUpper
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.maybeUpper(ctx, input.s))(v => new Output(v))
    }
  }
  object nextColor extends IRTMethodWrapper[Or, C] {
    import Shapes.nextColor.*
    val signature: Shapes.nextColor.type = Shapes.nextColor
    val marshaller: ShapesCodecs.nextColor.type = ShapesCodecs.nextColor
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.nextColor(ctx, input.c))(v => new Output(v))
    }
  }
  object makeProfile extends IRTMethodWrapper[Or, C] {
    import Shapes.makeProfile.*
    val signature: Shapes.makeProfile.type = Shapes.makeProfile
    val marshaller: ShapesCodecs.makeProfile.type = ShapesCodecs.makeProfile
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.makeProfile(ctx, input.name, input.age, input.color))(v => new Output(v))
    }
  }
  object pay extends IRTMethodWrapper[Or, C] {
    import Shapes.pay.*
    val signature: Shapes.pay.type = Shapes.pay
    val marshaller: ShapesCodecs.pay.type = ShapesCodecs.pay
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.pay(ctx, input.amount))(v => new Output(v))
    }
  }
  object divideSafe extends IRTMethodWrapper[Or, C] {
    import Shapes.divideSafe.*
    val signature: Shapes.divideSafe.type = Shapes.divideSafe
    val marshaller: ShapesCodecs.divideSafe.type = ShapesCodecs.divideSafe
    def invoke(ctx: C, input: Input): Just[Output] = {
      _F.redeem(_service.divideSafe(ctx, input.a, input.b))(err => _F.pure(new Shapes.DivideSafeOutput.Failure(err)), succ => _F.pure(new Shapes.DivideSafeOutput.Success(succ)))
    }
  }
  object noteValue extends IRTMethodWrapper[Or, C] {
    import Shapes.noteValue.*
    val signature: Shapes.noteValue.type = Shapes.noteValue
    val marshaller: ShapesCodecs.noteValue.type = ShapesCodecs.noteValue
    def invoke(ctx: C, input: Input): Just[Output] = {
      _F.redeem(_service.noteValue(ctx, input.v))(err => _F.pure(new Shapes.NoteValueOutput.Failure(err)), succ => _F.pure(new Shapes.NoteValueOutput.Success(succ)))
    }
  }
}

object ShapesWrappedServer

object Shapes {
  final val serviceId: IRTServiceId = IRTServiceId("Shapes")
  object ping extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("ping"))
    type Input = PingInput
    type Output = PingOutput
  }
  object upper extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("upper"))
    type Input = UpperInput
    type Output = UpperOutput
  }
  object add extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("add"))
    type Input = AddInput
    type Output = AddOutput
  }
  object echo extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("echo"))
    type Input = EchoInput
    type Output = EchoOutput
  }
  object divmod extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("divmod"))
    type Input = DivmodInput
    type Output = DivmodOutput
  }
  object reverse extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("reverse"))
    type Input = ReverseInput
    type Output = ReverseOutput
  }
  object invertMap extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("invertMap"))
    type Input = InvertMapInput
    type Output = InvertMapOutput
  }
  object maybeUpper extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("maybeUpper"))
    type Input = MaybeUpperInput
    type Output = MaybeUpperOutput
  }
  object nextColor extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("nextColor"))
    type Input = NextColorInput
    type Output = NextColorOutput
  }
  object makeProfile extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("makeProfile"))
    type Input = MakeProfileInput
    type Output = MakeProfileOutput
  }
  object pay extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("pay"))
    type Input = PayInput
    type Output = PayOutput
  }
  object divideSafe extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("divideSafe"))
    type Input = DivideSafeInput
    type Output = DivideSafeOutput
  }
  object noteValue extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("noteValue"))
    type Input = NoteValueInput
    type Output = NoteValueOutput
  }
  final case class PingInput() extends Shapes.PingInput.Defn
  trait PingInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodePingInput: Encoder.AsObject[PingInput] = deriveEncoder[PingInput]
    implicit val decodePingInput: Decoder[PingInput] = deriveDecoder[PingInput]
  }
  object PingInput extends Shapes.PingInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: Shapes.PingInput.Defn): Shapes.PingInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.PingInput()
    }
    implicit object PingInput_cast_into_ShapesNoteValueSuccess extends izumi.idealingua.runtime.IRTCast[Shapes.PingInput, Shapes.NoteValueSuccess] {
      override def convert(_value: Shapes.PingInput): Shapes.NoteValueSuccess = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.NoteValueSuccess()
      }
    }
    implicit object PingInput_cast_into_ShapesPingOutput extends izumi.idealingua.runtime.IRTCast[Shapes.PingInput, Shapes.PingOutput] {
      override def convert(_value: Shapes.PingInput): Shapes.PingOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PingOutput()
      }
    }
    implicit object PingInput_upcast_PingInput extends izumi.idealingua.runtime.IRTCast[Shapes.PingInput, Shapes.PingInput] {
      override def convert(_value: Shapes.PingInput): Shapes.PingInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PingInput()
      }
    }
    implicit class PingInputExtensions(override protected val _value: Shapes.PingInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.PingInput]
  }
  final case class PingOutput() extends Shapes.PingOutput.Defn
  trait PingOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodePingOutput: Encoder.AsObject[PingOutput] = deriveEncoder[PingOutput]
    implicit val decodePingOutput: Decoder[PingOutput] = deriveDecoder[PingOutput]
  }
  object PingOutput extends Shapes.PingOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: Shapes.PingOutput.Defn): Shapes.PingOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.PingOutput()
    }
    implicit object PingOutput_cast_into_ShapesNoteValueSuccess extends izumi.idealingua.runtime.IRTCast[Shapes.PingOutput, Shapes.NoteValueSuccess] {
      override def convert(_value: Shapes.PingOutput): Shapes.NoteValueSuccess = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.NoteValueSuccess()
      }
    }
    implicit object PingOutput_cast_into_ShapesPingInput extends izumi.idealingua.runtime.IRTCast[Shapes.PingOutput, Shapes.PingInput] {
      override def convert(_value: Shapes.PingOutput): Shapes.PingInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PingInput()
      }
    }
    implicit object PingOutput_upcast_PingOutput extends izumi.idealingua.runtime.IRTCast[Shapes.PingOutput, Shapes.PingOutput] {
      override def convert(_value: Shapes.PingOutput): Shapes.PingOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PingOutput()
      }
    }
    implicit class PingOutputExtensions(override protected val _value: Shapes.PingOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.PingOutput]
  }
  final case class UpperInput(s: String) extends AnyVal with Shapes.UpperInput.Defn
  trait UpperInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUpperInput: Encoder.AsObject[UpperInput] = Encoder.forProduct1[UpperInput, String]("s")((v: UpperInput) => v.s)
    implicit val decodeUpperInput: Decoder[UpperInput] = Decoder.forProduct1[UpperInput, String]("s")((d: String) => new UpperInput(d))
  }
  object UpperInput extends Shapes.UpperInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def s: String }
    def apply(s: String): Shapes.UpperInput = {
      new Shapes.UpperInput(s = s)
    }
    def apply(defn: Shapes.UpperInput.Defn): Shapes.UpperInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.UpperInput(s = defn.s)
    }
    implicit object UpperInput_upcast_UpperInput extends izumi.idealingua.runtime.IRTCast[Shapes.UpperInput, Shapes.UpperInput] {
      override def convert(_value: Shapes.UpperInput): Shapes.UpperInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.UpperInput(s = _value.s)
      }
    }
    implicit class UpperInputExtensions(override protected val _value: Shapes.UpperInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.UpperInput]
  }
  final case class UpperOutput(value: String) extends AnyVal with Shapes.UpperOutput.Defn
  trait UpperOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedUpperOutput: Encoder[UpperOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedUpperOutput: Decoder[UpperOutput] = Decoder.instance {
      v => v.as[String].map(d => UpperOutput(d))
    }
  }
  object UpperOutput extends Shapes.UpperOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): Shapes.UpperOutput = {
      new Shapes.UpperOutput(value = value)
    }
    def apply(defn: Shapes.UpperOutput.Defn): Shapes.UpperOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.UpperOutput(value = defn.value)
    }
    implicit object UpperOutput_upcast_UpperOutput extends izumi.idealingua.runtime.IRTCast[Shapes.UpperOutput, Shapes.UpperOutput] {
      override def convert(_value: Shapes.UpperOutput): Shapes.UpperOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.UpperOutput(value = _value.value)
      }
    }
    implicit class UpperOutputExtensions(override protected val _value: Shapes.UpperOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.UpperOutput]
  }
  final case class AddInput(a: Long, b: Long) extends Shapes.AddInput.Defn
  trait AddInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAddInput: Encoder.AsObject[AddInput] = deriveEncoder[AddInput]
    implicit val decodeAddInput: Decoder[AddInput] = deriveDecoder[AddInput]
  }
  object AddInput extends Shapes.AddInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Long
      def b: Long
    }
    def apply(a: Long, b: Long): Shapes.AddInput = {
      new Shapes.AddInput(a = a, b = b)
    }
    def apply(defn: Shapes.AddInput.Defn): Shapes.AddInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.AddInput(a = defn.a, b = defn.b)
    }
    implicit object AddInput_cast_into_ShapesDivideSafeInput extends izumi.idealingua.runtime.IRTCast[Shapes.AddInput, Shapes.DivideSafeInput] {
      override def convert(_value: Shapes.AddInput): Shapes.DivideSafeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivideSafeInput(a = _value.a, b = _value.b)
      }
    }
    implicit object AddInput_cast_into_ShapesDivmodInput extends izumi.idealingua.runtime.IRTCast[Shapes.AddInput, Shapes.DivmodInput] {
      override def convert(_value: Shapes.AddInput): Shapes.DivmodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivmodInput(a = _value.a, b = _value.b)
      }
    }
    implicit object AddInput_upcast_AddInput extends izumi.idealingua.runtime.IRTCast[Shapes.AddInput, Shapes.AddInput] {
      override def convert(_value: Shapes.AddInput): Shapes.AddInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.AddInput(a = _value.a, b = _value.b)
      }
    }
    implicit class AddInputExtensions(override protected val _value: Shapes.AddInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.AddInput]
  }
  final case class AddOutput(value: Long) extends AnyVal with Shapes.AddOutput.Defn
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
  object AddOutput extends Shapes.AddOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Long }
    def apply(value: Long): Shapes.AddOutput = {
      new Shapes.AddOutput(value = value)
    }
    def apply(defn: Shapes.AddOutput.Defn): Shapes.AddOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.AddOutput(value = defn.value)
    }
    implicit object AddOutput_upcast_AddOutput extends izumi.idealingua.runtime.IRTCast[Shapes.AddOutput, Shapes.AddOutput] {
      override def convert(_value: Shapes.AddOutput): Shapes.AddOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.AddOutput(value = _value.value)
      }
    }
    implicit class AddOutputExtensions(override protected val _value: Shapes.AddOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.AddOutput]
  }
  final case class EchoInput(req: EchoRequest) extends AnyVal with Shapes.EchoInput.Defn
  trait EchoInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeEchoInput: Encoder.AsObject[EchoInput] = Encoder.forProduct1[EchoInput, EchoRequest]("req")((v: EchoInput) => v.req)
    implicit val decodeEchoInput: Decoder[EchoInput] = Decoder.forProduct1[EchoInput, EchoRequest]("req")((d: EchoRequest) => new EchoInput(d))
  }
  object EchoInput extends Shapes.EchoInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def req: EchoRequest }
    def apply(req: EchoRequest.Defn): Shapes.EchoInput = {
      assert(req.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.EchoInput(req = EchoRequest(req))
    }
    def apply(defn: Shapes.EchoInput.Defn): Shapes.EchoInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.EchoInput(req = defn.req)
    }
    implicit object EchoInput_upcast_EchoInput extends izumi.idealingua.runtime.IRTCast[Shapes.EchoInput, Shapes.EchoInput] {
      override def convert(_value: Shapes.EchoInput): Shapes.EchoInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.EchoInput(req = _value.req)
      }
    }
    implicit class EchoInputExtensions(override protected val _value: Shapes.EchoInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.EchoInput]
  }
  final case class EchoOutput(value: EchoResponse) extends AnyVal with Shapes.EchoOutput.Defn
  trait EchoOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedEchoOutput: Encoder.AsObject[EchoOutput] = Encoder.AsObject.instance {
      v => v.value.asJsonObject
    }
    implicit val decodeUnwrappedEchoOutput: Decoder[EchoOutput] = Decoder.instance {
      v => v.as[EchoResponse].map(d => EchoOutput(d))
    }
  }
  object EchoOutput extends Shapes.EchoOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: EchoResponse }
    def apply(value: EchoResponse.Defn): Shapes.EchoOutput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.EchoOutput(value = EchoResponse(value))
    }
    def apply(defn: Shapes.EchoOutput.Defn): Shapes.EchoOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.EchoOutput(value = defn.value)
    }
    implicit object EchoOutput_upcast_EchoOutput extends izumi.idealingua.runtime.IRTCast[Shapes.EchoOutput, Shapes.EchoOutput] {
      override def convert(_value: Shapes.EchoOutput): Shapes.EchoOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.EchoOutput(value = _value.value)
      }
    }
    implicit class EchoOutputExtensions(override protected val _value: Shapes.EchoOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.EchoOutput]
  }
  final case class DivmodInput(a: Long, b: Long) extends Shapes.DivmodInput.Defn
  trait DivmodInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeDivmodInput: Encoder.AsObject[DivmodInput] = deriveEncoder[DivmodInput]
    implicit val decodeDivmodInput: Decoder[DivmodInput] = deriveDecoder[DivmodInput]
  }
  object DivmodInput extends Shapes.DivmodInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Long
      def b: Long
    }
    def apply(a: Long, b: Long): Shapes.DivmodInput = {
      new Shapes.DivmodInput(a = a, b = b)
    }
    def apply(defn: Shapes.DivmodInput.Defn): Shapes.DivmodInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.DivmodInput(a = defn.a, b = defn.b)
    }
    implicit object DivmodInput_cast_into_ShapesAddInput extends izumi.idealingua.runtime.IRTCast[Shapes.DivmodInput, Shapes.AddInput] {
      override def convert(_value: Shapes.DivmodInput): Shapes.AddInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.AddInput(a = _value.a, b = _value.b)
      }
    }
    implicit object DivmodInput_cast_into_ShapesDivideSafeInput extends izumi.idealingua.runtime.IRTCast[Shapes.DivmodInput, Shapes.DivideSafeInput] {
      override def convert(_value: Shapes.DivmodInput): Shapes.DivideSafeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivideSafeInput(a = _value.a, b = _value.b)
      }
    }
    implicit object DivmodInput_upcast_DivmodInput extends izumi.idealingua.runtime.IRTCast[Shapes.DivmodInput, Shapes.DivmodInput] {
      override def convert(_value: Shapes.DivmodInput): Shapes.DivmodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivmodInput(a = _value.a, b = _value.b)
      }
    }
    implicit class DivmodInputExtensions(override protected val _value: Shapes.DivmodInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.DivmodInput]
  }
  final case class DivmodOutput(quotient: Long, remainder: Long) extends Shapes.DivmodOutput.Defn
  trait DivmodOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeDivmodOutput: Encoder.AsObject[DivmodOutput] = deriveEncoder[DivmodOutput]
    implicit val decodeDivmodOutput: Decoder[DivmodOutput] = deriveDecoder[DivmodOutput]
  }
  object DivmodOutput extends Shapes.DivmodOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def quotient: Long
      def remainder: Long
    }
    def apply(quotient: Long, remainder: Long): Shapes.DivmodOutput = {
      new Shapes.DivmodOutput(quotient = quotient, remainder = remainder)
    }
    def apply(defn: Shapes.DivmodOutput.Defn): Shapes.DivmodOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.DivmodOutput(quotient = defn.quotient, remainder = defn.remainder)
    }
    implicit object DivmodOutput_upcast_DivmodOutput extends izumi.idealingua.runtime.IRTCast[Shapes.DivmodOutput, Shapes.DivmodOutput] {
      override def convert(_value: Shapes.DivmodOutput): Shapes.DivmodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivmodOutput(quotient = _value.quotient, remainder = _value.remainder)
      }
    }
    implicit class DivmodOutputExtensions(override protected val _value: Shapes.DivmodOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.DivmodOutput]
  }
  final case class ReverseInput(items: List[String]) extends Shapes.ReverseInput.Defn
  trait ReverseInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeReverseInput: Encoder.AsObject[ReverseInput] = deriveEncoder[ReverseInput]
    implicit val decodeReverseInput: Decoder[ReverseInput] = deriveDecoder[ReverseInput]
  }
  object ReverseInput extends Shapes.ReverseInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def items: List[String] }
    def apply(items: List[String]): Shapes.ReverseInput = {
      new Shapes.ReverseInput(items = items)
    }
    def apply(defn: Shapes.ReverseInput.Defn): Shapes.ReverseInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.ReverseInput(items = defn.items)
    }
    implicit object ReverseInput_upcast_ReverseInput extends izumi.idealingua.runtime.IRTCast[Shapes.ReverseInput, Shapes.ReverseInput] {
      override def convert(_value: Shapes.ReverseInput): Shapes.ReverseInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.ReverseInput(items = _value.items)
      }
    }
    implicit class ReverseInputExtensions(override protected val _value: Shapes.ReverseInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.ReverseInput]
  }
  final case class ReverseOutput(value: List[String]) extends Shapes.ReverseOutput.Defn
  trait ReverseOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedReverseOutput: Encoder[ReverseOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedReverseOutput: Decoder[ReverseOutput] = Decoder.instance {
      v => v.as[List[String]].map(d => ReverseOutput(d))
    }
  }
  object ReverseOutput extends Shapes.ReverseOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: List[String] }
    def apply(value: List[String]): Shapes.ReverseOutput = {
      new Shapes.ReverseOutput(value = value)
    }
    def apply(defn: Shapes.ReverseOutput.Defn): Shapes.ReverseOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.ReverseOutput(value = defn.value)
    }
    implicit object ReverseOutput_upcast_ReverseOutput extends izumi.idealingua.runtime.IRTCast[Shapes.ReverseOutput, Shapes.ReverseOutput] {
      override def convert(_value: Shapes.ReverseOutput): Shapes.ReverseOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.ReverseOutput(value = _value.value)
      }
    }
    implicit class ReverseOutputExtensions(override protected val _value: Shapes.ReverseOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.ReverseOutput]
  }
  final case class InvertMapInput(m: Map[String, String]) extends Shapes.InvertMapInput.Defn
  trait InvertMapInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeInvertMapInput: Encoder.AsObject[InvertMapInput] = deriveEncoder[InvertMapInput]
    implicit val decodeInvertMapInput: Decoder[InvertMapInput] = deriveDecoder[InvertMapInput]
  }
  object InvertMapInput extends Shapes.InvertMapInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def m: Map[String, String] }
    def apply(m: Map[String, String]): Shapes.InvertMapInput = {
      new Shapes.InvertMapInput(m = m)
    }
    def apply(defn: Shapes.InvertMapInput.Defn): Shapes.InvertMapInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.InvertMapInput(m = defn.m)
    }
    implicit object InvertMapInput_upcast_InvertMapInput extends izumi.idealingua.runtime.IRTCast[Shapes.InvertMapInput, Shapes.InvertMapInput] {
      override def convert(_value: Shapes.InvertMapInput): Shapes.InvertMapInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.InvertMapInput(m = _value.m)
      }
    }
    implicit class InvertMapInputExtensions(override protected val _value: Shapes.InvertMapInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.InvertMapInput]
  }
  final case class InvertMapOutput(value: Map[String, String]) extends Shapes.InvertMapOutput.Defn
  trait InvertMapOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedInvertMapOutput: Encoder.AsObject[InvertMapOutput] = Encoder.AsObject.instance {
      v => v.value.asJsonObject
    }
    implicit val decodeUnwrappedInvertMapOutput: Decoder[InvertMapOutput] = Decoder.instance {
      v => v.as[Map[String, String]].map(d => InvertMapOutput(d))
    }
  }
  object InvertMapOutput extends Shapes.InvertMapOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: Map[String, String] }
    def apply(value: Map[String, String]): Shapes.InvertMapOutput = {
      new Shapes.InvertMapOutput(value = value)
    }
    def apply(defn: Shapes.InvertMapOutput.Defn): Shapes.InvertMapOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.InvertMapOutput(value = defn.value)
    }
    implicit object InvertMapOutput_upcast_InvertMapOutput extends izumi.idealingua.runtime.IRTCast[Shapes.InvertMapOutput, Shapes.InvertMapOutput] {
      override def convert(_value: Shapes.InvertMapOutput): Shapes.InvertMapOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.InvertMapOutput(value = _value.value)
      }
    }
    implicit class InvertMapOutputExtensions(override protected val _value: Shapes.InvertMapOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.InvertMapOutput]
  }
  final case class MaybeUpperInput(s: Option[String]) extends Shapes.MaybeUpperInput.Defn
  trait MaybeUpperInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeMaybeUpperInput: Encoder.AsObject[MaybeUpperInput] = deriveEncoder[MaybeUpperInput]
    implicit val decodeMaybeUpperInput: Decoder[MaybeUpperInput] = deriveDecoder[MaybeUpperInput]
  }
  object MaybeUpperInput extends Shapes.MaybeUpperInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def s: Option[String] }
    def apply(s: Option[String]): Shapes.MaybeUpperInput = {
      new Shapes.MaybeUpperInput(s = s)
    }
    def apply(defn: Shapes.MaybeUpperInput.Defn): Shapes.MaybeUpperInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.MaybeUpperInput(s = defn.s)
    }
    implicit object MaybeUpperInput_upcast_MaybeUpperInput extends izumi.idealingua.runtime.IRTCast[Shapes.MaybeUpperInput, Shapes.MaybeUpperInput] {
      override def convert(_value: Shapes.MaybeUpperInput): Shapes.MaybeUpperInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.MaybeUpperInput(s = _value.s)
      }
    }
    implicit class MaybeUpperInputExtensions(override protected val _value: Shapes.MaybeUpperInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.MaybeUpperInput]
  }
  final case class MaybeUpperOutput(value: Option[String]) extends Shapes.MaybeUpperOutput.Defn
  trait MaybeUpperOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedMaybeUpperOutput: Encoder[MaybeUpperOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedMaybeUpperOutput: Decoder[MaybeUpperOutput] = Decoder.instance {
      v => v.as[Option[String]].map(d => MaybeUpperOutput(d))
    }
  }
  object MaybeUpperOutput extends Shapes.MaybeUpperOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: Option[String] }
    def apply(value: Option[String]): Shapes.MaybeUpperOutput = {
      new Shapes.MaybeUpperOutput(value = value)
    }
    def apply(defn: Shapes.MaybeUpperOutput.Defn): Shapes.MaybeUpperOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.MaybeUpperOutput(value = defn.value)
    }
    implicit object MaybeUpperOutput_upcast_MaybeUpperOutput extends izumi.idealingua.runtime.IRTCast[Shapes.MaybeUpperOutput, Shapes.MaybeUpperOutput] {
      override def convert(_value: Shapes.MaybeUpperOutput): Shapes.MaybeUpperOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.MaybeUpperOutput(value = _value.value)
      }
    }
    implicit class MaybeUpperOutputExtensions(override protected val _value: Shapes.MaybeUpperOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.MaybeUpperOutput]
  }
  final case class NextColorInput(c: Color) extends AnyVal with Shapes.NextColorInput.Defn
  trait NextColorInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeNextColorInput: Encoder.AsObject[NextColorInput] = Encoder.forProduct1[NextColorInput, Color]("c")((v: NextColorInput) => v.c)
    implicit val decodeNextColorInput: Decoder[NextColorInput] = Decoder.forProduct1[NextColorInput, Color]("c")((d: Color) => new NextColorInput(d))
  }
  object NextColorInput extends Shapes.NextColorInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def c: Color }
    def apply(c: Color): Shapes.NextColorInput = {
      assert(c.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.NextColorInput(c = c)
    }
    def apply(defn: Shapes.NextColorInput.Defn): Shapes.NextColorInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.NextColorInput(c = defn.c)
    }
    implicit object NextColorInput_upcast_NextColorInput extends izumi.idealingua.runtime.IRTCast[Shapes.NextColorInput, Shapes.NextColorInput] {
      override def convert(_value: Shapes.NextColorInput): Shapes.NextColorInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.NextColorInput(c = _value.c)
      }
    }
    implicit class NextColorInputExtensions(override protected val _value: Shapes.NextColorInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.NextColorInput]
  }
  final case class NextColorOutput(value: ColorResult) extends Shapes.NextColorOutput.Defn
  trait NextColorOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedNextColorOutput: Encoder.AsObject[NextColorOutput] = Encoder.AsObject.instance {
      v => v.value.asJsonObject
    }
    implicit val decodeUnwrappedNextColorOutput: Decoder[NextColorOutput] = Decoder.instance {
      v => v.as[ColorResult].map(d => NextColorOutput(d))
    }
  }
  object NextColorOutput extends Shapes.NextColorOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: ColorResult }
    def apply(value: ColorResult.Defn): Shapes.NextColorOutput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.NextColorOutput(value = ColorResult(value))
    }
    def apply(defn: Shapes.NextColorOutput.Defn): Shapes.NextColorOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.NextColorOutput(value = defn.value)
    }
    implicit object NextColorOutput_upcast_NextColorOutput extends izumi.idealingua.runtime.IRTCast[Shapes.NextColorOutput, Shapes.NextColorOutput] {
      override def convert(_value: Shapes.NextColorOutput): Shapes.NextColorOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.NextColorOutput(value = _value.value)
      }
    }
    implicit class NextColorOutputExtensions(override protected val _value: Shapes.NextColorOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.NextColorOutput]
  }
  final case class MakeProfileInput(name: String, age: Int, color: Color) extends Shapes.MakeProfileInput.Defn
  trait MakeProfileInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeMakeProfileInput: Encoder.AsObject[MakeProfileInput] = deriveEncoder[MakeProfileInput]
    implicit val decodeMakeProfileInput: Decoder[MakeProfileInput] = deriveDecoder[MakeProfileInput]
  }
  object MakeProfileInput extends Shapes.MakeProfileInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def name: String
      def age: Int
      def color: Color
    }
    def apply(name: String, age: Int, color: Color): Shapes.MakeProfileInput = {
      assert(color.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.MakeProfileInput(name = name, age = age, color = color)
    }
    def apply(defn: Shapes.MakeProfileInput.Defn): Shapes.MakeProfileInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.MakeProfileInput(name = defn.name, age = defn.age, color = defn.color)
    }
    implicit object MakeProfileInput_cast_into_Profile extends izumi.idealingua.runtime.IRTCast[Shapes.MakeProfileInput, Profile] {
      override def convert(_value: Shapes.MakeProfileInput): Profile = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Profile(name = _value.name, age = _value.age, color = _value.color)
      }
    }
    implicit object MakeProfileInput_upcast_MakeProfileInput extends izumi.idealingua.runtime.IRTCast[Shapes.MakeProfileInput, Shapes.MakeProfileInput] {
      override def convert(_value: Shapes.MakeProfileInput): Shapes.MakeProfileInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.MakeProfileInput(name = _value.name, age = _value.age, color = _value.color)
      }
    }
    implicit class MakeProfileInputExtensions(override protected val _value: Shapes.MakeProfileInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.MakeProfileInput]
  }
  final case class MakeProfileOutput(value: Profile) extends AnyVal with Shapes.MakeProfileOutput.Defn
  trait MakeProfileOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedMakeProfileOutput: Encoder.AsObject[MakeProfileOutput] = Encoder.AsObject.instance {
      v => v.value.asJsonObject
    }
    implicit val decodeUnwrappedMakeProfileOutput: Decoder[MakeProfileOutput] = Decoder.instance {
      v => v.as[Profile].map(d => MakeProfileOutput(d))
    }
  }
  object MakeProfileOutput extends Shapes.MakeProfileOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Profile }
    def apply(value: Profile.Defn): Shapes.MakeProfileOutput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.MakeProfileOutput(value = Profile(value))
    }
    def apply(defn: Shapes.MakeProfileOutput.Defn): Shapes.MakeProfileOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.MakeProfileOutput(value = defn.value)
    }
    implicit object MakeProfileOutput_upcast_MakeProfileOutput extends izumi.idealingua.runtime.IRTCast[Shapes.MakeProfileOutput, Shapes.MakeProfileOutput] {
      override def convert(_value: Shapes.MakeProfileOutput): Shapes.MakeProfileOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.MakeProfileOutput(value = _value.value)
      }
    }
    implicit class MakeProfileOutputExtensions(override protected val _value: Shapes.MakeProfileOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.MakeProfileOutput]
  }
  final case class PayInput(amount: Long) extends AnyVal with Shapes.PayInput.Defn
  trait PayInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodePayInput: Encoder.AsObject[PayInput] = Encoder.forProduct1[PayInput, Long]("amount")((v: PayInput) => v.amount)
    implicit val decodePayInput: Decoder[PayInput] = Decoder.forProduct1[PayInput, Long]("amount")((d: Long) => new PayInput(d))
  }
  object PayInput extends Shapes.PayInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def amount: Long }
    def apply(amount: Long): Shapes.PayInput = {
      new Shapes.PayInput(amount = amount)
    }
    def apply(defn: Shapes.PayInput.Defn): Shapes.PayInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.PayInput(amount = defn.amount)
    }
    implicit object PayInput_upcast_PayInput extends izumi.idealingua.runtime.IRTCast[Shapes.PayInput, Shapes.PayInput] {
      override def convert(_value: Shapes.PayInput): Shapes.PayInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PayInput(amount = _value.amount)
      }
    }
    implicit class PayInputExtensions(override protected val _value: Shapes.PayInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.PayInput]
  }
  final case class PayOutput(value: PaymentResult) extends Shapes.PayOutput.Defn
  trait PayOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedPayOutput: Encoder.AsObject[PayOutput] = Encoder.AsObject.instance {
      v => v.value.asJsonObject
    }
    implicit val decodeUnwrappedPayOutput: Decoder[PayOutput] = Decoder.instance {
      v => v.as[PaymentResult].map(d => PayOutput(d))
    }
  }
  object PayOutput extends Shapes.PayOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: PaymentResult }
    def apply(value: PaymentResult): Shapes.PayOutput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.PayOutput(value = value)
    }
    def apply(defn: Shapes.PayOutput.Defn): Shapes.PayOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.PayOutput(value = defn.value)
    }
    implicit object PayOutput_upcast_PayOutput extends izumi.idealingua.runtime.IRTCast[Shapes.PayOutput, Shapes.PayOutput] {
      override def convert(_value: Shapes.PayOutput): Shapes.PayOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PayOutput(value = _value.value)
      }
    }
    implicit class PayOutputExtensions(override protected val _value: Shapes.PayOutput) extends izumi.idealingua.runtime.IRTConversions[Shapes.PayOutput]
  }
  final case class DivideSafeInput(a: Long, b: Long) extends Shapes.DivideSafeInput.Defn
  trait DivideSafeInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeDivideSafeInput: Encoder.AsObject[DivideSafeInput] = deriveEncoder[DivideSafeInput]
    implicit val decodeDivideSafeInput: Decoder[DivideSafeInput] = deriveDecoder[DivideSafeInput]
  }
  object DivideSafeInput extends Shapes.DivideSafeInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def a: Long
      def b: Long
    }
    def apply(a: Long, b: Long): Shapes.DivideSafeInput = {
      new Shapes.DivideSafeInput(a = a, b = b)
    }
    def apply(defn: Shapes.DivideSafeInput.Defn): Shapes.DivideSafeInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.DivideSafeInput(a = defn.a, b = defn.b)
    }
    implicit object DivideSafeInput_cast_into_ShapesAddInput extends izumi.idealingua.runtime.IRTCast[Shapes.DivideSafeInput, Shapes.AddInput] {
      override def convert(_value: Shapes.DivideSafeInput): Shapes.AddInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.AddInput(a = _value.a, b = _value.b)
      }
    }
    implicit object DivideSafeInput_cast_into_ShapesDivmodInput extends izumi.idealingua.runtime.IRTCast[Shapes.DivideSafeInput, Shapes.DivmodInput] {
      override def convert(_value: Shapes.DivideSafeInput): Shapes.DivmodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivmodInput(a = _value.a, b = _value.b)
      }
    }
    implicit object DivideSafeInput_upcast_DivideSafeInput extends izumi.idealingua.runtime.IRTCast[Shapes.DivideSafeInput, Shapes.DivideSafeInput] {
      override def convert(_value: Shapes.DivideSafeInput): Shapes.DivideSafeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.DivideSafeInput(a = _value.a, b = _value.b)
      }
    }
    implicit class DivideSafeInputExtensions(override protected val _value: Shapes.DivideSafeInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.DivideSafeInput]
  }
  sealed trait DivideSafeOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait DivideSafeOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeDivideSafeOutput: Encoder.AsObject[Shapes.DivideSafeOutput] = Encoder.AsObject.instance {
      case v: Shapes.DivideSafeOutput.Success =>
        Map("Success" -> v.value).asJsonObject
      case v: Shapes.DivideSafeOutput.Failure =>
        Map("Failure" -> v.value).asJsonObject
    }
    implicit val decodeDivideSafeOutput: Decoder[Shapes.DivideSafeOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "Success" =>
          value.as[Long].map(Shapes.DivideSafeOutput.Success.apply)
        case "Failure" =>
          value.as[_root_.mcpdemo.ServiceError].map(Shapes.DivideSafeOutput.Failure.apply)
        case _ =>
          val cname = "mcpdemo.Shapes.DivideSafeOutput"
          val alts = List("Success", "Failure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object DivideSafeOutput extends Shapes.DivideSafeOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = Shapes.DivideSafeOutput
    final case class Success(value: Long) extends Shapes.DivideSafeOutput
    implicit def intoSuccess(value: Long): Shapes.DivideSafeOutput = Shapes.DivideSafeOutput.Success(value)
    implicit def fromSuccess(value: Shapes.DivideSafeOutput.Success): Long = value.value
    final case class Failure(value: _root_.mcpdemo.ServiceError) extends Shapes.DivideSafeOutput
    implicit def intoFailure(value: _root_.mcpdemo.ServiceError): Shapes.DivideSafeOutput = Shapes.DivideSafeOutput.Failure(value)
    implicit def fromFailure(value: Shapes.DivideSafeOutput.Failure): _root_.mcpdemo.ServiceError = value.value
  }
  final case class NoteValueInput(v: Int) extends AnyVal with Shapes.NoteValueInput.Defn
  trait NoteValueInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeNoteValueInput: Encoder.AsObject[NoteValueInput] = Encoder.forProduct1[NoteValueInput, Int]("v")((v: NoteValueInput) => v.v)
    implicit val decodeNoteValueInput: Decoder[NoteValueInput] = Decoder.forProduct1[NoteValueInput, Int]("v")((d: Int) => new NoteValueInput(d))
  }
  object NoteValueInput extends Shapes.NoteValueInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def v: Int }
    def apply(v: Int): Shapes.NoteValueInput = {
      new Shapes.NoteValueInput(v = v)
    }
    def apply(defn: Shapes.NoteValueInput.Defn): Shapes.NoteValueInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.NoteValueInput(v = defn.v)
    }
    implicit object NoteValueInput_upcast_NoteValueInput extends izumi.idealingua.runtime.IRTCast[Shapes.NoteValueInput, Shapes.NoteValueInput] {
      override def convert(_value: Shapes.NoteValueInput): Shapes.NoteValueInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.NoteValueInput(v = _value.v)
      }
    }
    implicit class NoteValueInputExtensions(override protected val _value: Shapes.NoteValueInput) extends izumi.idealingua.runtime.IRTConversions[Shapes.NoteValueInput]
  }
  sealed trait NoteValueOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait NoteValueOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeNoteValueOutput: Encoder.AsObject[Shapes.NoteValueOutput] = Encoder.AsObject.instance {
      case v: Shapes.NoteValueOutput.Success =>
        Map("Success" -> v.value).asJsonObject
      case v: Shapes.NoteValueOutput.Failure =>
        Map("Failure" -> v.value).asJsonObject
    }
    implicit val decodeNoteValueOutput: Decoder[Shapes.NoteValueOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "Success" =>
          value.as[_root_.mcpdemo.Shapes.NoteValueSuccess].map(Shapes.NoteValueOutput.Success.apply)
        case "Failure" =>
          value.as[_root_.mcpdemo.ServiceError].map(Shapes.NoteValueOutput.Failure.apply)
        case _ =>
          val cname = "mcpdemo.Shapes.NoteValueOutput"
          val alts = List("Success", "Failure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object NoteValueOutput extends Shapes.NoteValueOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = Shapes.NoteValueOutput
    final case class Success(value: _root_.mcpdemo.Shapes.NoteValueSuccess) extends Shapes.NoteValueOutput
    implicit def intoSuccess(value: _root_.mcpdemo.Shapes.NoteValueSuccess): Shapes.NoteValueOutput = Shapes.NoteValueOutput.Success(value)
    implicit def fromSuccess(value: Shapes.NoteValueOutput.Success): _root_.mcpdemo.Shapes.NoteValueSuccess = value.value
    final case class Failure(value: _root_.mcpdemo.ServiceError) extends Shapes.NoteValueOutput
    implicit def intoFailure(value: _root_.mcpdemo.ServiceError): Shapes.NoteValueOutput = Shapes.NoteValueOutput.Failure(value)
    implicit def fromFailure(value: Shapes.NoteValueOutput.Failure): _root_.mcpdemo.ServiceError = value.value
  }
  final case class NoteValueSuccess() extends Shapes.NoteValueSuccess.Defn
  trait NoteValueSuccessCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeNoteValueSuccess: Encoder.AsObject[NoteValueSuccess] = deriveEncoder[NoteValueSuccess]
    implicit val decodeNoteValueSuccess: Decoder[NoteValueSuccess] = deriveDecoder[NoteValueSuccess]
  }
  object NoteValueSuccess extends Shapes.NoteValueSuccessCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: Shapes.NoteValueSuccess.Defn): Shapes.NoteValueSuccess = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Shapes.NoteValueSuccess()
    }
    implicit object NoteValueSuccess_cast_into_ShapesPingInput extends izumi.idealingua.runtime.IRTCast[Shapes.NoteValueSuccess, Shapes.PingInput] {
      override def convert(_value: Shapes.NoteValueSuccess): Shapes.PingInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PingInput()
      }
    }
    implicit object NoteValueSuccess_cast_into_ShapesPingOutput extends izumi.idealingua.runtime.IRTCast[Shapes.NoteValueSuccess, Shapes.PingOutput] {
      override def convert(_value: Shapes.NoteValueSuccess): Shapes.PingOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.PingOutput()
      }
    }
    implicit object NoteValueSuccess_upcast_NoteValueSuccess extends izumi.idealingua.runtime.IRTCast[Shapes.NoteValueSuccess, Shapes.NoteValueSuccess] {
      override def convert(_value: Shapes.NoteValueSuccess): Shapes.NoteValueSuccess = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Shapes.NoteValueSuccess()
      }
    }
    implicit class NoteValueSuccessExtensions(override protected val _value: Shapes.NoteValueSuccess) extends izumi.idealingua.runtime.IRTConversions[Shapes.NoteValueSuccess]
  }
}

object ShapesCodecs {
  object ping extends IRTCirceMarshaller {
    import Shapes.ping.*
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
  object upper extends IRTCirceMarshaller {
    import Shapes.upper.*
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
  object add extends IRTCirceMarshaller {
    import Shapes.add.*
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
  object echo extends IRTCirceMarshaller {
    import Shapes.echo.*
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
  object divmod extends IRTCirceMarshaller {
    import Shapes.divmod.*
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
  object reverse extends IRTCirceMarshaller {
    import Shapes.reverse.*
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
  object invertMap extends IRTCirceMarshaller {
    import Shapes.invertMap.*
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
  object maybeUpper extends IRTCirceMarshaller {
    import Shapes.maybeUpper.*
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
  object nextColor extends IRTCirceMarshaller {
    import Shapes.nextColor.*
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
  object makeProfile extends IRTCirceMarshaller {
    import Shapes.makeProfile.*
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
  object pay extends IRTCirceMarshaller {
    import Shapes.pay.*
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
  object divideSafe extends IRTCirceMarshaller {
    import Shapes.divideSafe.*
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
  object noteValue extends IRTCirceMarshaller {
    import Shapes.noteValue.*
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
       