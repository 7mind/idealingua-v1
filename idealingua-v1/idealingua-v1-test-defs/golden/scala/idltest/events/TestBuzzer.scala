package idltest.events

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait TestBuzzerServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def empty(ctx: C): Just[Unit]
  def userRegistered(ctx: C, firstName: String, secondName: String): Just[Unit]
  def hello(ctx: C, name: String): Just[String]
  def enumInput(ctx: C, value: EnumType): Just[String]
  def enumInputVoid(ctx: C, value: EnumType): Just[Unit]
  def adtInput(ctx: C, value: ADTType): Just[String]
  def adtInputVoid(ctx: C, value: ADTType): Just[Unit]
}

trait TestBuzzerClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def empty(): Just[Unit]
  def userRegistered(firstName: String, secondName: String): Just[Unit]
  def hello(name: String): Just[String]
  def enumInput(value: EnumType): Just[String]
  def enumInputVoid(value: EnumType): Just[Unit]
  def adtInput(value: ADTType): Just[String]
  def adtInputVoid(value: ADTType): Just[Unit]
}

class TestBuzzerWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends TestBuzzerClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.idltest.events.TestBuzzer as _M
  def empty(): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.empty.Input()), _M.empty.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.empty.Output), method) if method == _M.empty.id =>
        _F.pure(())
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.empty"
        val expected = classOf[_M.empty.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def userRegistered(firstName: String, secondName: String): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.userRegistered.Input(firstName, secondName)), _M.userRegistered.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.userRegistered.Output), method) if method == _M.userRegistered.id =>
        _F.pure(())
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.userRegistered"
        val expected = classOf[_M.userRegistered.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def hello(name: String): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.hello.Input(name)), _M.hello.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.hello.Output), method) if method == _M.hello.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.hello"
        val expected = classOf[_M.hello.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def enumInput(value: EnumType): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.enumInput.Input(value)), _M.enumInput.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.enumInput.Output), method) if method == _M.enumInput.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.enumInput"
        val expected = classOf[_M.enumInput.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def enumInputVoid(value: EnumType): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.enumInputVoid.Input(value)), _M.enumInputVoid.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.enumInputVoid.Output), method) if method == _M.enumInputVoid.id =>
        _F.pure(())
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.enumInputVoid"
        val expected = classOf[_M.enumInputVoid.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def adtInput(value: ADTType): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.adtInput.Input(value)), _M.adtInput.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.adtInput.Output), method) if method == _M.adtInput.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.adtInput"
        val expected = classOf[_M.adtInput.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def adtInputVoid(value: ADTType): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.adtInputVoid.Input(value)), _M.adtInputVoid.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.adtInputVoid.Output), method) if method == _M.adtInputVoid.id =>
        _F.pure(())
      case v =>
        val id = "TestBuzzer.TestBuzzerWrappedClient.adtInputVoid"
        val expected = classOf[_M.adtInputVoid.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object TestBuzzerWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(TestBuzzer.empty.id -> TestBuzzerCodecs.empty, TestBuzzer.userRegistered.id -> TestBuzzerCodecs.userRegistered, TestBuzzer.hello.id -> TestBuzzerCodecs.hello, TestBuzzer.enumInput.id -> TestBuzzerCodecs.enumInput, TestBuzzer.enumInputVoid.id -> TestBuzzerCodecs.enumInputVoid, TestBuzzer.adtInput.id -> TestBuzzerCodecs.adtInput, TestBuzzer.adtInputVoid.id -> TestBuzzerCodecs.adtInputVoid)
  }
}

class TestBuzzerWrappedServer[Or[+_, +_]: IRTIO2, C](_service: TestBuzzerServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = TestBuzzer.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](empty, userRegistered, hello, enumInput, enumInputVoid, adtInput, adtInputVoid).map(m => m.signature.id -> m).toMap
  }
  object empty extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.empty.*
    val signature: TestBuzzer.empty.type = TestBuzzer.empty
    val marshaller: TestBuzzerCodecs.empty.type = TestBuzzerCodecs.empty
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.empty(ctx))(_ => new Output())
    }
  }
  object userRegistered extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.userRegistered.*
    val signature: TestBuzzer.userRegistered.type = TestBuzzer.userRegistered
    val marshaller: TestBuzzerCodecs.userRegistered.type = TestBuzzerCodecs.userRegistered
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.userRegistered(ctx, input.firstName, input.secondName))(_ => new Output())
    }
  }
  object hello extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.hello.*
    val signature: TestBuzzer.hello.type = TestBuzzer.hello
    val marshaller: TestBuzzerCodecs.hello.type = TestBuzzerCodecs.hello
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.hello(ctx, input.name))(v => new Output(v))
    }
  }
  object enumInput extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.enumInput.*
    val signature: TestBuzzer.enumInput.type = TestBuzzer.enumInput
    val marshaller: TestBuzzerCodecs.enumInput.type = TestBuzzerCodecs.enumInput
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.enumInput(ctx, input.value))(v => new Output(v))
    }
  }
  object enumInputVoid extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.enumInputVoid.*
    val signature: TestBuzzer.enumInputVoid.type = TestBuzzer.enumInputVoid
    val marshaller: TestBuzzerCodecs.enumInputVoid.type = TestBuzzerCodecs.enumInputVoid
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.enumInputVoid(ctx, input.value))(_ => new Output())
    }
  }
  object adtInput extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.adtInput.*
    val signature: TestBuzzer.adtInput.type = TestBuzzer.adtInput
    val marshaller: TestBuzzerCodecs.adtInput.type = TestBuzzerCodecs.adtInput
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.adtInput(ctx, input.value))(v => new Output(v))
    }
  }
  object adtInputVoid extends IRTMethodWrapper[Or, C] {
    import TestBuzzer.adtInputVoid.*
    val signature: TestBuzzer.adtInputVoid.type = TestBuzzer.adtInputVoid
    val marshaller: TestBuzzerCodecs.adtInputVoid.type = TestBuzzerCodecs.adtInputVoid
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.adtInputVoid(ctx, input.value))(_ => new Output())
    }
  }
}

object TestBuzzerWrappedServer

object TestBuzzer {
  final val serviceId: IRTServiceId = IRTServiceId("TestBuzzer")
  object empty extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("empty"))
    type Input = EmptyInput
    type Output = EmptyOutput
  }
  object userRegistered extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("userRegistered"))
    type Input = UserRegisteredInput
    type Output = UserRegisteredOutput
  }
  object hello extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("hello"))
    type Input = HelloInput
    type Output = HelloOutput
  }
  object enumInput extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("enumInput"))
    type Input = EnumInputInput
    type Output = EnumInputOutput
  }
  object enumInputVoid extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("enumInputVoid"))
    type Input = EnumInputVoidInput
    type Output = EnumInputVoidOutput
  }
  object adtInput extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("adtInput"))
    type Input = AdtInputInput
    type Output = AdtInputOutput
  }
  object adtInputVoid extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("adtInputVoid"))
    type Input = AdtInputVoidInput
    type Output = AdtInputVoidOutput
  }
  final case class EmptyInput() extends TestBuzzer.EmptyInput.Defn
  trait EmptyInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeEmptyInput: Encoder.AsObject[EmptyInput] = deriveEncoder[EmptyInput]
    implicit val decodeEmptyInput: Decoder[EmptyInput] = deriveDecoder[EmptyInput]
  }
  object EmptyInput extends TestBuzzer.EmptyInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestBuzzer.EmptyInput.Defn): TestBuzzer.EmptyInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EmptyInput()
    }
    implicit object EmptyInput_cast_into_TestBuzzerAdtInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyInput, TestBuzzer.AdtInputVoidOutput] {
      override def convert(_value: TestBuzzer.EmptyInput): TestBuzzer.AdtInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidOutput()
      }
    }
    implicit object EmptyInput_cast_into_TestBuzzerEmptyOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyInput, TestBuzzer.EmptyOutput] {
      override def convert(_value: TestBuzzer.EmptyInput): TestBuzzer.EmptyOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyOutput()
      }
    }
    implicit object EmptyInput_cast_into_TestBuzzerEnumInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyInput, TestBuzzer.EnumInputVoidOutput] {
      override def convert(_value: TestBuzzer.EmptyInput): TestBuzzer.EnumInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidOutput()
      }
    }
    implicit object EmptyInput_cast_into_TestBuzzerUserRegisteredOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyInput, TestBuzzer.UserRegisteredOutput] {
      override def convert(_value: TestBuzzer.EmptyInput): TestBuzzer.UserRegisteredOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.UserRegisteredOutput()
      }
    }
    implicit object EmptyInput_upcast_EmptyInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyInput, TestBuzzer.EmptyInput] {
      override def convert(_value: TestBuzzer.EmptyInput): TestBuzzer.EmptyInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyInput()
      }
    }
    implicit class EmptyInputExtensions(override protected val _value: TestBuzzer.EmptyInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.EmptyInput]
  }
  final case class EmptyOutput() extends TestBuzzer.EmptyOutput.Defn
  trait EmptyOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeEmptyOutput: Encoder.AsObject[EmptyOutput] = deriveEncoder[EmptyOutput]
    implicit val decodeEmptyOutput: Decoder[EmptyOutput] = deriveDecoder[EmptyOutput]
  }
  object EmptyOutput extends TestBuzzer.EmptyOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestBuzzer.EmptyOutput.Defn): TestBuzzer.EmptyOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EmptyOutput()
    }
    implicit object EmptyOutput_cast_into_TestBuzzerAdtInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyOutput, TestBuzzer.AdtInputVoidOutput] {
      override def convert(_value: TestBuzzer.EmptyOutput): TestBuzzer.AdtInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidOutput()
      }
    }
    implicit object EmptyOutput_cast_into_TestBuzzerEmptyInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyOutput, TestBuzzer.EmptyInput] {
      override def convert(_value: TestBuzzer.EmptyOutput): TestBuzzer.EmptyInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyInput()
      }
    }
    implicit object EmptyOutput_cast_into_TestBuzzerEnumInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyOutput, TestBuzzer.EnumInputVoidOutput] {
      override def convert(_value: TestBuzzer.EmptyOutput): TestBuzzer.EnumInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidOutput()
      }
    }
    implicit object EmptyOutput_cast_into_TestBuzzerUserRegisteredOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyOutput, TestBuzzer.UserRegisteredOutput] {
      override def convert(_value: TestBuzzer.EmptyOutput): TestBuzzer.UserRegisteredOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.UserRegisteredOutput()
      }
    }
    implicit object EmptyOutput_upcast_EmptyOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EmptyOutput, TestBuzzer.EmptyOutput] {
      override def convert(_value: TestBuzzer.EmptyOutput): TestBuzzer.EmptyOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyOutput()
      }
    }
    implicit class EmptyOutputExtensions(override protected val _value: TestBuzzer.EmptyOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.EmptyOutput]
  }
  final case class UserRegisteredInput(firstName: String, secondName: String) extends TestBuzzer.UserRegisteredInput.Defn
  trait UserRegisteredInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUserRegisteredInput: Encoder.AsObject[UserRegisteredInput] = deriveEncoder[UserRegisteredInput]
    implicit val decodeUserRegisteredInput: Decoder[UserRegisteredInput] = deriveDecoder[UserRegisteredInput]
  }
  object UserRegisteredInput extends TestBuzzer.UserRegisteredInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestBuzzer.UserRegisteredInput = {
      new TestBuzzer.UserRegisteredInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestBuzzer.UserRegisteredInput.Defn): TestBuzzer.UserRegisteredInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.UserRegisteredInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object UserRegisteredInput_upcast_UserRegisteredInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.UserRegisteredInput, TestBuzzer.UserRegisteredInput] {
      override def convert(_value: TestBuzzer.UserRegisteredInput): TestBuzzer.UserRegisteredInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.UserRegisteredInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class UserRegisteredInputExtensions(override protected val _value: TestBuzzer.UserRegisteredInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.UserRegisteredInput]
  }
  final case class UserRegisteredOutput() extends TestBuzzer.UserRegisteredOutput.Defn
  trait UserRegisteredOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUserRegisteredOutput: Encoder.AsObject[UserRegisteredOutput] = deriveEncoder[UserRegisteredOutput]
    implicit val decodeUserRegisteredOutput: Decoder[UserRegisteredOutput] = deriveDecoder[UserRegisteredOutput]
  }
  object UserRegisteredOutput extends TestBuzzer.UserRegisteredOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestBuzzer.UserRegisteredOutput.Defn): TestBuzzer.UserRegisteredOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.UserRegisteredOutput()
    }
    implicit object UserRegisteredOutput_cast_into_TestBuzzerAdtInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.UserRegisteredOutput, TestBuzzer.AdtInputVoidOutput] {
      override def convert(_value: TestBuzzer.UserRegisteredOutput): TestBuzzer.AdtInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidOutput()
      }
    }
    implicit object UserRegisteredOutput_cast_into_TestBuzzerEmptyInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.UserRegisteredOutput, TestBuzzer.EmptyInput] {
      override def convert(_value: TestBuzzer.UserRegisteredOutput): TestBuzzer.EmptyInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyInput()
      }
    }
    implicit object UserRegisteredOutput_cast_into_TestBuzzerEmptyOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.UserRegisteredOutput, TestBuzzer.EmptyOutput] {
      override def convert(_value: TestBuzzer.UserRegisteredOutput): TestBuzzer.EmptyOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyOutput()
      }
    }
    implicit object UserRegisteredOutput_cast_into_TestBuzzerEnumInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.UserRegisteredOutput, TestBuzzer.EnumInputVoidOutput] {
      override def convert(_value: TestBuzzer.UserRegisteredOutput): TestBuzzer.EnumInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidOutput()
      }
    }
    implicit object UserRegisteredOutput_upcast_UserRegisteredOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.UserRegisteredOutput, TestBuzzer.UserRegisteredOutput] {
      override def convert(_value: TestBuzzer.UserRegisteredOutput): TestBuzzer.UserRegisteredOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.UserRegisteredOutput()
      }
    }
    implicit class UserRegisteredOutputExtensions(override protected val _value: TestBuzzer.UserRegisteredOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.UserRegisteredOutput]
  }
  final case class HelloInput(name: String) extends AnyVal with TestBuzzer.HelloInput.Defn
  trait HelloInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeHelloInput: Encoder.AsObject[HelloInput] = Encoder.forProduct1[HelloInput, String]("name")((v: HelloInput) => v.name)
    implicit val decodeHelloInput: Decoder[HelloInput] = Decoder.forProduct1[HelloInput, String]("name")((d: String) => new HelloInput(d))
  }
  object HelloInput extends TestBuzzer.HelloInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def name: String }
    def apply(name: String): TestBuzzer.HelloInput = {
      new TestBuzzer.HelloInput(name = name)
    }
    def apply(defn: TestBuzzer.HelloInput.Defn): TestBuzzer.HelloInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.HelloInput(name = defn.name)
    }
    implicit object HelloInput_upcast_HelloInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.HelloInput, TestBuzzer.HelloInput] {
      override def convert(_value: TestBuzzer.HelloInput): TestBuzzer.HelloInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.HelloInput(name = _value.name)
      }
    }
    implicit class HelloInputExtensions(override protected val _value: TestBuzzer.HelloInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.HelloInput]
  }
  final case class HelloOutput(value: String) extends AnyVal with TestBuzzer.HelloOutput.Defn
  trait HelloOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedHelloOutput: Encoder[HelloOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedHelloOutput: Decoder[HelloOutput] = Decoder.instance {
      v => v.as[String].map(d => HelloOutput(d))
    }
  }
  object HelloOutput extends TestBuzzer.HelloOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestBuzzer.HelloOutput = {
      new TestBuzzer.HelloOutput(value = value)
    }
    def apply(defn: TestBuzzer.HelloOutput.Defn): TestBuzzer.HelloOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.HelloOutput(value = defn.value)
    }
    implicit object HelloOutput_cast_into_TestBuzzerAdtInputOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.HelloOutput, TestBuzzer.AdtInputOutput] {
      override def convert(_value: TestBuzzer.HelloOutput): TestBuzzer.AdtInputOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputOutput(value = _value.value)
      }
    }
    implicit object HelloOutput_cast_into_TestBuzzerEnumInputOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.HelloOutput, TestBuzzer.EnumInputOutput] {
      override def convert(_value: TestBuzzer.HelloOutput): TestBuzzer.EnumInputOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputOutput(value = _value.value)
      }
    }
    implicit object HelloOutput_upcast_HelloOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.HelloOutput, TestBuzzer.HelloOutput] {
      override def convert(_value: TestBuzzer.HelloOutput): TestBuzzer.HelloOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.HelloOutput(value = _value.value)
      }
    }
    implicit class HelloOutputExtensions(override protected val _value: TestBuzzer.HelloOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.HelloOutput]
  }
  final case class EnumInputInput(value: EnumType) extends AnyVal with TestBuzzer.EnumInputInput.Defn
  trait EnumInputInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeEnumInputInput: Encoder.AsObject[EnumInputInput] = Encoder.forProduct1[EnumInputInput, EnumType]("value")((v: EnumInputInput) => v.value)
    implicit val decodeEnumInputInput: Decoder[EnumInputInput] = Decoder.forProduct1[EnumInputInput, EnumType]("value")((d: EnumType) => new EnumInputInput(d))
  }
  object EnumInputInput extends TestBuzzer.EnumInputInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: EnumType }
    def apply(value: EnumType): TestBuzzer.EnumInputInput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EnumInputInput(value = value)
    }
    def apply(defn: TestBuzzer.EnumInputInput.Defn): TestBuzzer.EnumInputInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EnumInputInput(value = defn.value)
    }
    implicit object EnumInputInput_cast_into_TestBuzzerEnumInputVoidInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputInput, TestBuzzer.EnumInputVoidInput] {
      override def convert(_value: TestBuzzer.EnumInputInput): TestBuzzer.EnumInputVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidInput(value = _value.value)
      }
    }
    implicit object EnumInputInput_upcast_EnumInputInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputInput, TestBuzzer.EnumInputInput] {
      override def convert(_value: TestBuzzer.EnumInputInput): TestBuzzer.EnumInputInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputInput(value = _value.value)
      }
    }
    implicit class EnumInputInputExtensions(override protected val _value: TestBuzzer.EnumInputInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.EnumInputInput]
  }
  final case class EnumInputOutput(value: String) extends AnyVal with TestBuzzer.EnumInputOutput.Defn
  trait EnumInputOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedEnumInputOutput: Encoder[EnumInputOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedEnumInputOutput: Decoder[EnumInputOutput] = Decoder.instance {
      v => v.as[String].map(d => EnumInputOutput(d))
    }
  }
  object EnumInputOutput extends TestBuzzer.EnumInputOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestBuzzer.EnumInputOutput = {
      new TestBuzzer.EnumInputOutput(value = value)
    }
    def apply(defn: TestBuzzer.EnumInputOutput.Defn): TestBuzzer.EnumInputOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EnumInputOutput(value = defn.value)
    }
    implicit object EnumInputOutput_cast_into_TestBuzzerAdtInputOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputOutput, TestBuzzer.AdtInputOutput] {
      override def convert(_value: TestBuzzer.EnumInputOutput): TestBuzzer.AdtInputOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputOutput(value = _value.value)
      }
    }
    implicit object EnumInputOutput_cast_into_TestBuzzerHelloOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputOutput, TestBuzzer.HelloOutput] {
      override def convert(_value: TestBuzzer.EnumInputOutput): TestBuzzer.HelloOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.HelloOutput(value = _value.value)
      }
    }
    implicit object EnumInputOutput_upcast_EnumInputOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputOutput, TestBuzzer.EnumInputOutput] {
      override def convert(_value: TestBuzzer.EnumInputOutput): TestBuzzer.EnumInputOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputOutput(value = _value.value)
      }
    }
    implicit class EnumInputOutputExtensions(override protected val _value: TestBuzzer.EnumInputOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.EnumInputOutput]
  }
  final case class EnumInputVoidInput(value: EnumType) extends AnyVal with TestBuzzer.EnumInputVoidInput.Defn
  trait EnumInputVoidInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeEnumInputVoidInput: Encoder.AsObject[EnumInputVoidInput] = Encoder.forProduct1[EnumInputVoidInput, EnumType]("value")((v: EnumInputVoidInput) => v.value)
    implicit val decodeEnumInputVoidInput: Decoder[EnumInputVoidInput] = Decoder.forProduct1[EnumInputVoidInput, EnumType]("value")((d: EnumType) => new EnumInputVoidInput(d))
  }
  object EnumInputVoidInput extends TestBuzzer.EnumInputVoidInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: EnumType }
    def apply(value: EnumType): TestBuzzer.EnumInputVoidInput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EnumInputVoidInput(value = value)
    }
    def apply(defn: TestBuzzer.EnumInputVoidInput.Defn): TestBuzzer.EnumInputVoidInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EnumInputVoidInput(value = defn.value)
    }
    implicit object EnumInputVoidInput_cast_into_TestBuzzerEnumInputInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidInput, TestBuzzer.EnumInputInput] {
      override def convert(_value: TestBuzzer.EnumInputVoidInput): TestBuzzer.EnumInputInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputInput(value = _value.value)
      }
    }
    implicit object EnumInputVoidInput_upcast_EnumInputVoidInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidInput, TestBuzzer.EnumInputVoidInput] {
      override def convert(_value: TestBuzzer.EnumInputVoidInput): TestBuzzer.EnumInputVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidInput(value = _value.value)
      }
    }
    implicit class EnumInputVoidInputExtensions(override protected val _value: TestBuzzer.EnumInputVoidInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.EnumInputVoidInput]
  }
  final case class EnumInputVoidOutput() extends TestBuzzer.EnumInputVoidOutput.Defn
  trait EnumInputVoidOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeEnumInputVoidOutput: Encoder.AsObject[EnumInputVoidOutput] = deriveEncoder[EnumInputVoidOutput]
    implicit val decodeEnumInputVoidOutput: Decoder[EnumInputVoidOutput] = deriveDecoder[EnumInputVoidOutput]
  }
  object EnumInputVoidOutput extends TestBuzzer.EnumInputVoidOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestBuzzer.EnumInputVoidOutput.Defn): TestBuzzer.EnumInputVoidOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.EnumInputVoidOutput()
    }
    implicit object EnumInputVoidOutput_cast_into_TestBuzzerAdtInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidOutput, TestBuzzer.AdtInputVoidOutput] {
      override def convert(_value: TestBuzzer.EnumInputVoidOutput): TestBuzzer.AdtInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidOutput()
      }
    }
    implicit object EnumInputVoidOutput_cast_into_TestBuzzerEmptyInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidOutput, TestBuzzer.EmptyInput] {
      override def convert(_value: TestBuzzer.EnumInputVoidOutput): TestBuzzer.EmptyInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyInput()
      }
    }
    implicit object EnumInputVoidOutput_cast_into_TestBuzzerEmptyOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidOutput, TestBuzzer.EmptyOutput] {
      override def convert(_value: TestBuzzer.EnumInputVoidOutput): TestBuzzer.EmptyOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyOutput()
      }
    }
    implicit object EnumInputVoidOutput_cast_into_TestBuzzerUserRegisteredOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidOutput, TestBuzzer.UserRegisteredOutput] {
      override def convert(_value: TestBuzzer.EnumInputVoidOutput): TestBuzzer.UserRegisteredOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.UserRegisteredOutput()
      }
    }
    implicit object EnumInputVoidOutput_upcast_EnumInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.EnumInputVoidOutput, TestBuzzer.EnumInputVoidOutput] {
      override def convert(_value: TestBuzzer.EnumInputVoidOutput): TestBuzzer.EnumInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidOutput()
      }
    }
    implicit class EnumInputVoidOutputExtensions(override protected val _value: TestBuzzer.EnumInputVoidOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.EnumInputVoidOutput]
  }
  final case class AdtInputInput(value: ADTType) extends TestBuzzer.AdtInputInput.Defn
  trait AdtInputInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAdtInputInput: Encoder.AsObject[AdtInputInput] = deriveEncoder[AdtInputInput]
    implicit val decodeAdtInputInput: Decoder[AdtInputInput] = deriveDecoder[AdtInputInput]
  }
  object AdtInputInput extends TestBuzzer.AdtInputInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: ADTType }
    def apply(value: ADTType): TestBuzzer.AdtInputInput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.AdtInputInput(value = value)
    }
    def apply(defn: TestBuzzer.AdtInputInput.Defn): TestBuzzer.AdtInputInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.AdtInputInput(value = defn.value)
    }
    implicit object AdtInputInput_cast_into_TestBuzzerAdtInputVoidInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputInput, TestBuzzer.AdtInputVoidInput] {
      override def convert(_value: TestBuzzer.AdtInputInput): TestBuzzer.AdtInputVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidInput(value = _value.value)
      }
    }
    implicit object AdtInputInput_upcast_AdtInputInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputInput, TestBuzzer.AdtInputInput] {
      override def convert(_value: TestBuzzer.AdtInputInput): TestBuzzer.AdtInputInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputInput(value = _value.value)
      }
    }
    implicit class AdtInputInputExtensions(override protected val _value: TestBuzzer.AdtInputInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.AdtInputInput]
  }
  final case class AdtInputOutput(value: String) extends AnyVal with TestBuzzer.AdtInputOutput.Defn
  trait AdtInputOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedAdtInputOutput: Encoder[AdtInputOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedAdtInputOutput: Decoder[AdtInputOutput] = Decoder.instance {
      v => v.as[String].map(d => AdtInputOutput(d))
    }
  }
  object AdtInputOutput extends TestBuzzer.AdtInputOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestBuzzer.AdtInputOutput = {
      new TestBuzzer.AdtInputOutput(value = value)
    }
    def apply(defn: TestBuzzer.AdtInputOutput.Defn): TestBuzzer.AdtInputOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.AdtInputOutput(value = defn.value)
    }
    implicit object AdtInputOutput_cast_into_TestBuzzerEnumInputOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputOutput, TestBuzzer.EnumInputOutput] {
      override def convert(_value: TestBuzzer.AdtInputOutput): TestBuzzer.EnumInputOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputOutput(value = _value.value)
      }
    }
    implicit object AdtInputOutput_cast_into_TestBuzzerHelloOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputOutput, TestBuzzer.HelloOutput] {
      override def convert(_value: TestBuzzer.AdtInputOutput): TestBuzzer.HelloOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.HelloOutput(value = _value.value)
      }
    }
    implicit object AdtInputOutput_upcast_AdtInputOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputOutput, TestBuzzer.AdtInputOutput] {
      override def convert(_value: TestBuzzer.AdtInputOutput): TestBuzzer.AdtInputOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputOutput(value = _value.value)
      }
    }
    implicit class AdtInputOutputExtensions(override protected val _value: TestBuzzer.AdtInputOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.AdtInputOutput]
  }
  final case class AdtInputVoidInput(value: ADTType) extends TestBuzzer.AdtInputVoidInput.Defn
  trait AdtInputVoidInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAdtInputVoidInput: Encoder.AsObject[AdtInputVoidInput] = deriveEncoder[AdtInputVoidInput]
    implicit val decodeAdtInputVoidInput: Decoder[AdtInputVoidInput] = deriveDecoder[AdtInputVoidInput]
  }
  object AdtInputVoidInput extends TestBuzzer.AdtInputVoidInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: ADTType }
    def apply(value: ADTType): TestBuzzer.AdtInputVoidInput = {
      assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.AdtInputVoidInput(value = value)
    }
    def apply(defn: TestBuzzer.AdtInputVoidInput.Defn): TestBuzzer.AdtInputVoidInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.AdtInputVoidInput(value = defn.value)
    }
    implicit object AdtInputVoidInput_cast_into_TestBuzzerAdtInputInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidInput, TestBuzzer.AdtInputInput] {
      override def convert(_value: TestBuzzer.AdtInputVoidInput): TestBuzzer.AdtInputInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputInput(value = _value.value)
      }
    }
    implicit object AdtInputVoidInput_upcast_AdtInputVoidInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidInput, TestBuzzer.AdtInputVoidInput] {
      override def convert(_value: TestBuzzer.AdtInputVoidInput): TestBuzzer.AdtInputVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidInput(value = _value.value)
      }
    }
    implicit class AdtInputVoidInputExtensions(override protected val _value: TestBuzzer.AdtInputVoidInput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.AdtInputVoidInput]
  }
  final case class AdtInputVoidOutput() extends TestBuzzer.AdtInputVoidOutput.Defn
  trait AdtInputVoidOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAdtInputVoidOutput: Encoder.AsObject[AdtInputVoidOutput] = deriveEncoder[AdtInputVoidOutput]
    implicit val decodeAdtInputVoidOutput: Decoder[AdtInputVoidOutput] = deriveDecoder[AdtInputVoidOutput]
  }
  object AdtInputVoidOutput extends TestBuzzer.AdtInputVoidOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestBuzzer.AdtInputVoidOutput.Defn): TestBuzzer.AdtInputVoidOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestBuzzer.AdtInputVoidOutput()
    }
    implicit object AdtInputVoidOutput_cast_into_TestBuzzerEmptyInput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidOutput, TestBuzzer.EmptyInput] {
      override def convert(_value: TestBuzzer.AdtInputVoidOutput): TestBuzzer.EmptyInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyInput()
      }
    }
    implicit object AdtInputVoidOutput_cast_into_TestBuzzerEmptyOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidOutput, TestBuzzer.EmptyOutput] {
      override def convert(_value: TestBuzzer.AdtInputVoidOutput): TestBuzzer.EmptyOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EmptyOutput()
      }
    }
    implicit object AdtInputVoidOutput_cast_into_TestBuzzerEnumInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidOutput, TestBuzzer.EnumInputVoidOutput] {
      override def convert(_value: TestBuzzer.AdtInputVoidOutput): TestBuzzer.EnumInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.EnumInputVoidOutput()
      }
    }
    implicit object AdtInputVoidOutput_cast_into_TestBuzzerUserRegisteredOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidOutput, TestBuzzer.UserRegisteredOutput] {
      override def convert(_value: TestBuzzer.AdtInputVoidOutput): TestBuzzer.UserRegisteredOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.UserRegisteredOutput()
      }
    }
    implicit object AdtInputVoidOutput_upcast_AdtInputVoidOutput extends izumi.idealingua.runtime.IRTCast[TestBuzzer.AdtInputVoidOutput, TestBuzzer.AdtInputVoidOutput] {
      override def convert(_value: TestBuzzer.AdtInputVoidOutput): TestBuzzer.AdtInputVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestBuzzer.AdtInputVoidOutput()
      }
    }
    implicit class AdtInputVoidOutputExtensions(override protected val _value: TestBuzzer.AdtInputVoidOutput) extends izumi.idealingua.runtime.IRTConversions[TestBuzzer.AdtInputVoidOutput]
  }
}

object TestBuzzerCodecs {
  object empty extends IRTCirceMarshaller {
    import TestBuzzer.empty.*
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
  object userRegistered extends IRTCirceMarshaller {
    import TestBuzzer.userRegistered.*
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
  object hello extends IRTCirceMarshaller {
    import TestBuzzer.hello.*
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
  object enumInput extends IRTCirceMarshaller {
    import TestBuzzer.enumInput.*
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
  object enumInputVoid extends IRTCirceMarshaller {
    import TestBuzzer.enumInputVoid.*
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
  object adtInput extends IRTCirceMarshaller {
    import TestBuzzer.adtInput.*
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
  object adtInputVoid extends IRTCirceMarshaller {
    import TestBuzzer.adtInputVoid.*
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
       