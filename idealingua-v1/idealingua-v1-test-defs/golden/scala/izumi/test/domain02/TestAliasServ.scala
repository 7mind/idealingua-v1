package izumi.test.domain02

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait TestAliasServServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def getMassCoupons(ctx: C, iterator: Option[RTestObject1]): Just[Int]
  def ifaceMethod(ctx: C, va: AnyValTest2): Just[Int]
  def testADTIdReturn(ctx: C): Just[TestAliasServ.testADTIdReturn.Output]
  def testADTIdImportedReturn(ctx: C): Just[TestAliasServ.testADTIdImportedReturn.Output]
}

trait TestAliasServClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def getMassCoupons(iterator: Option[RTestObject1]): Just[Int]
  def ifaceMethod(va: AnyValTest2): Just[Int]
  def testADTIdReturn(): Just[TestAliasServ.testADTIdReturn.Output]
  def testADTIdImportedReturn(): Just[TestAliasServ.testADTIdImportedReturn.Output]
}

class TestAliasServWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends TestAliasServClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.izumi.test.domain02.TestAliasServ as _M
  def getMassCoupons(iterator: Option[RTestObject1]): Just[Int] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.getMassCoupons.Input(iterator)), _M.getMassCoupons.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.getMassCoupons.Output), method) if method == _M.getMassCoupons.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestAliasServ.TestAliasServWrappedClient.getMassCoupons"
        val expected = classOf[_M.getMassCoupons.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def ifaceMethod(va: AnyValTest2): Just[Int] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.ifaceMethod.Input(va)), _M.ifaceMethod.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.ifaceMethod.Output), method) if method == _M.ifaceMethod.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestAliasServ.TestAliasServWrappedClient.ifaceMethod"
        val expected = classOf[_M.ifaceMethod.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def testADTIdReturn(): Just[TestAliasServ.testADTIdReturn.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.testADTIdReturn.Input()), _M.testADTIdReturn.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.testADTIdReturn.Output), method) if method == _M.testADTIdReturn.id =>
        _F.pure(v)
      case v =>
        val id = "TestAliasServ.TestAliasServWrappedClient.testADTIdReturn"
        val expected = classOf[_M.testADTIdReturn.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def testADTIdImportedReturn(): Just[TestAliasServ.testADTIdImportedReturn.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.testADTIdImportedReturn.Input()), _M.testADTIdImportedReturn.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.testADTIdImportedReturn.Output), method) if method == _M.testADTIdImportedReturn.id =>
        _F.pure(v)
      case v =>
        val id = "TestAliasServ.TestAliasServWrappedClient.testADTIdImportedReturn"
        val expected = classOf[_M.testADTIdImportedReturn.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object TestAliasServWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(TestAliasServ.getMassCoupons.id -> TestAliasServCodecs.getMassCoupons, TestAliasServ.ifaceMethod.id -> TestAliasServCodecs.ifaceMethod, TestAliasServ.testADTIdReturn.id -> TestAliasServCodecs.testADTIdReturn, TestAliasServ.testADTIdImportedReturn.id -> TestAliasServCodecs.testADTIdImportedReturn)
  }
}

class TestAliasServWrappedServer[Or[+_, +_]: IRTIO2, C](_service: TestAliasServServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = TestAliasServ.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](getMassCoupons, ifaceMethod, testADTIdReturn, testADTIdImportedReturn).map(m => m.signature.id -> m).toMap
  }
  object getMassCoupons extends IRTMethodWrapper[Or, C] {
    import TestAliasServ.getMassCoupons.*
    val signature: TestAliasServ.getMassCoupons.type = TestAliasServ.getMassCoupons
    val marshaller: TestAliasServCodecs.getMassCoupons.type = TestAliasServCodecs.getMassCoupons
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.getMassCoupons(ctx, input.iterator))(v => new Output(v))
    }
  }
  object ifaceMethod extends IRTMethodWrapper[Or, C] {
    import TestAliasServ.ifaceMethod.*
    val signature: TestAliasServ.ifaceMethod.type = TestAliasServ.ifaceMethod
    val marshaller: TestAliasServCodecs.ifaceMethod.type = TestAliasServCodecs.ifaceMethod
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.ifaceMethod(ctx, input.va))(v => new Output(v))
    }
  }
  object testADTIdReturn extends IRTMethodWrapper[Or, C] {
    import TestAliasServ.testADTIdReturn.*
    val signature: TestAliasServ.testADTIdReturn.type = TestAliasServ.testADTIdReturn
    val marshaller: TestAliasServCodecs.testADTIdReturn.type = TestAliasServCodecs.testADTIdReturn
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.testADTIdReturn(ctx)
    }
  }
  object testADTIdImportedReturn extends IRTMethodWrapper[Or, C] {
    import TestAliasServ.testADTIdImportedReturn.*
    val signature: TestAliasServ.testADTIdImportedReturn.type = TestAliasServ.testADTIdImportedReturn
    val marshaller: TestAliasServCodecs.testADTIdImportedReturn.type = TestAliasServCodecs.testADTIdImportedReturn
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.testADTIdImportedReturn(ctx)
    }
  }
}

object TestAliasServWrappedServer

object TestAliasServ {
  final val serviceId: IRTServiceId = IRTServiceId("TestAliasServ")
  object getMassCoupons extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("getMassCoupons"))
    type Input = GetMassCouponsInput
    type Output = GetMassCouponsOutput
  }
  object ifaceMethod extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("ifaceMethod"))
    type Input = IfaceMethodInput
    type Output = IfaceMethodOutput
  }
  object testADTIdReturn extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("testADTIdReturn"))
    type Input = TestADTIdReturnInput
    type Output = TestADTIdReturnOutput
  }
  object testADTIdImportedReturn extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("testADTIdImportedReturn"))
    type Input = TestADTIdImportedReturnInput
    type Output = TestADTIdImportedReturnOutput
  }
  final case class GetMassCouponsInput(iterator: Option[RTestObject1]) extends TestAliasServ.GetMassCouponsInput.Defn
  trait GetMassCouponsInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGetMassCouponsInput: Encoder.AsObject[GetMassCouponsInput] = deriveEncoder[GetMassCouponsInput]
    implicit val decodeGetMassCouponsInput: Decoder[GetMassCouponsInput] = deriveDecoder[GetMassCouponsInput]
  }
  object GetMassCouponsInput extends TestAliasServ.GetMassCouponsInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def iterator: Option[RTestObject1] }
    def apply(iterator: Option[RTestObject1]): TestAliasServ.GetMassCouponsInput = {
      new TestAliasServ.GetMassCouponsInput(iterator = iterator)
    }
    def apply(defn: TestAliasServ.GetMassCouponsInput.Defn): TestAliasServ.GetMassCouponsInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.GetMassCouponsInput(iterator = defn.iterator)
    }
    implicit object GetMassCouponsInput_upcast_GetMassCouponsInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.GetMassCouponsInput, TestAliasServ.GetMassCouponsInput] {
      override def convert(_value: TestAliasServ.GetMassCouponsInput): TestAliasServ.GetMassCouponsInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.GetMassCouponsInput(iterator = _value.iterator)
      }
    }
    implicit class GetMassCouponsInputExtensions(override protected val _value: TestAliasServ.GetMassCouponsInput) extends izumi.idealingua.runtime.IRTConversions[TestAliasServ.GetMassCouponsInput]
  }
  final case class GetMassCouponsOutput(value: Int) extends AnyVal with TestAliasServ.GetMassCouponsOutput.Defn
  trait GetMassCouponsOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedGetMassCouponsOutput: Encoder[GetMassCouponsOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedGetMassCouponsOutput: Decoder[GetMassCouponsOutput] = Decoder.instance {
      v => v.as[Int].map(d => GetMassCouponsOutput(d))
    }
  }
  object GetMassCouponsOutput extends TestAliasServ.GetMassCouponsOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Int }
    def apply(value: Int): TestAliasServ.GetMassCouponsOutput = {
      new TestAliasServ.GetMassCouponsOutput(value = value)
    }
    def apply(defn: TestAliasServ.GetMassCouponsOutput.Defn): TestAliasServ.GetMassCouponsOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.GetMassCouponsOutput(value = defn.value)
    }
    implicit object GetMassCouponsOutput_cast_into_TestAliasServIfaceMethodOutput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.GetMassCouponsOutput, TestAliasServ.IfaceMethodOutput] {
      override def convert(_value: TestAliasServ.GetMassCouponsOutput): TestAliasServ.IfaceMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.IfaceMethodOutput(value = _value.value)
      }
    }
    implicit object GetMassCouponsOutput_upcast_GetMassCouponsOutput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.GetMassCouponsOutput, TestAliasServ.GetMassCouponsOutput] {
      override def convert(_value: TestAliasServ.GetMassCouponsOutput): TestAliasServ.GetMassCouponsOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.GetMassCouponsOutput(value = _value.value)
      }
    }
    implicit class GetMassCouponsOutputExtensions(override protected val _value: TestAliasServ.GetMassCouponsOutput) extends izumi.idealingua.runtime.IRTConversions[TestAliasServ.GetMassCouponsOutput]
  }
  final case class IfaceMethodInput(va: AnyValTest2) extends TestAliasServ.IfaceMethodInput.Defn
  trait IfaceMethodInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeIfaceMethodInput: Encoder.AsObject[IfaceMethodInput] = deriveEncoder[IfaceMethodInput]
    implicit val decodeIfaceMethodInput: Decoder[IfaceMethodInput] = deriveDecoder[IfaceMethodInput]
  }
  object IfaceMethodInput extends TestAliasServ.IfaceMethodInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def va: AnyValTest2 }
    def apply(va: AnyValTest2): TestAliasServ.IfaceMethodInput = {
      assert(va.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.IfaceMethodInput(va = va)
    }
    def apply(defn: TestAliasServ.IfaceMethodInput.Defn): TestAliasServ.IfaceMethodInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.IfaceMethodInput(va = defn.va)
    }
    implicit object IfaceMethodInput_upcast_IfaceMethodInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.IfaceMethodInput, TestAliasServ.IfaceMethodInput] {
      override def convert(_value: TestAliasServ.IfaceMethodInput): TestAliasServ.IfaceMethodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.IfaceMethodInput(va = _value.va)
      }
    }
    implicit class IfaceMethodInputExtensions(override protected val _value: TestAliasServ.IfaceMethodInput) extends izumi.idealingua.runtime.IRTConversions[TestAliasServ.IfaceMethodInput]
  }
  final case class IfaceMethodOutput(value: Int) extends AnyVal with TestAliasServ.IfaceMethodOutput.Defn
  trait IfaceMethodOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedIfaceMethodOutput: Encoder[IfaceMethodOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedIfaceMethodOutput: Decoder[IfaceMethodOutput] = Decoder.instance {
      v => v.as[Int].map(d => IfaceMethodOutput(d))
    }
  }
  object IfaceMethodOutput extends TestAliasServ.IfaceMethodOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Int }
    def apply(value: Int): TestAliasServ.IfaceMethodOutput = {
      new TestAliasServ.IfaceMethodOutput(value = value)
    }
    def apply(defn: TestAliasServ.IfaceMethodOutput.Defn): TestAliasServ.IfaceMethodOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.IfaceMethodOutput(value = defn.value)
    }
    implicit object IfaceMethodOutput_cast_into_TestAliasServGetMassCouponsOutput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.IfaceMethodOutput, TestAliasServ.GetMassCouponsOutput] {
      override def convert(_value: TestAliasServ.IfaceMethodOutput): TestAliasServ.GetMassCouponsOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.GetMassCouponsOutput(value = _value.value)
      }
    }
    implicit object IfaceMethodOutput_upcast_IfaceMethodOutput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.IfaceMethodOutput, TestAliasServ.IfaceMethodOutput] {
      override def convert(_value: TestAliasServ.IfaceMethodOutput): TestAliasServ.IfaceMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.IfaceMethodOutput(value = _value.value)
      }
    }
    implicit class IfaceMethodOutputExtensions(override protected val _value: TestAliasServ.IfaceMethodOutput) extends izumi.idealingua.runtime.IRTConversions[TestAliasServ.IfaceMethodOutput]
  }
  final case class TestADTIdReturnInput() extends TestAliasServ.TestADTIdReturnInput.Defn
  trait TestADTIdReturnInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeTestADTIdReturnInput: Encoder.AsObject[TestADTIdReturnInput] = deriveEncoder[TestADTIdReturnInput]
    implicit val decodeTestADTIdReturnInput: Decoder[TestADTIdReturnInput] = deriveDecoder[TestADTIdReturnInput]
  }
  object TestADTIdReturnInput extends TestAliasServ.TestADTIdReturnInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestAliasServ.TestADTIdReturnInput.Defn): TestAliasServ.TestADTIdReturnInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.TestADTIdReturnInput()
    }
    implicit object TestADTIdReturnInput_cast_into_NestedAdtsServiceAdtNestedInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.TestADTIdReturnInput, NestedAdtsService.AdtNestedInput] {
      override def convert(_value: TestAliasServ.TestADTIdReturnInput): NestedAdtsService.AdtNestedInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NestedAdtsService.AdtNestedInput()
      }
    }
    implicit object TestADTIdReturnInput_cast_into_TestAliasServTestADTIdImportedReturnInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.TestADTIdReturnInput, TestAliasServ.TestADTIdImportedReturnInput] {
      override def convert(_value: TestAliasServ.TestADTIdReturnInput): TestAliasServ.TestADTIdImportedReturnInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.TestADTIdImportedReturnInput()
      }
    }
    implicit object TestADTIdReturnInput_upcast_TestADTIdReturnInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.TestADTIdReturnInput, TestAliasServ.TestADTIdReturnInput] {
      override def convert(_value: TestAliasServ.TestADTIdReturnInput): TestAliasServ.TestADTIdReturnInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.TestADTIdReturnInput()
      }
    }
    implicit class TestADTIdReturnInputExtensions(override protected val _value: TestAliasServ.TestADTIdReturnInput) extends izumi.idealingua.runtime.IRTConversions[TestAliasServ.TestADTIdReturnInput]
  }
  sealed trait TestADTIdReturnOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait TestADTIdReturnOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeTestADTIdReturnOutput: Encoder.AsObject[TestAliasServ.TestADTIdReturnOutput] = Encoder.AsObject.instance {
      case v: TestAliasServ.TestADTIdReturnOutput.TestIDReturn =>
        Map("TestIDReturn" -> v.value).asJsonObject
      case v: TestAliasServ.TestADTIdReturnOutput.DTO1 =>
        Map("DTO1" -> v.value).asJsonObject
    }
    implicit val decodeTestADTIdReturnOutput: Decoder[TestAliasServ.TestADTIdReturnOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "TestIDReturn" =>
          value.as[_root_.izumi.test.domain02.TestIDReturn].map(TestAliasServ.TestADTIdReturnOutput.TestIDReturn.apply)
        case "DTO1" =>
          value.as[_root_.izumi.test.domain02.DTO1].map(TestAliasServ.TestADTIdReturnOutput.DTO1.apply)
        case _ =>
          val cname = "izumi.test.domain02.TestAliasServ.TestADTIdReturnOutput"
          val alts = List("TestIDReturn", "DTO1").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object TestADTIdReturnOutput extends TestAliasServ.TestADTIdReturnOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestAliasServ.TestADTIdReturnOutput
    final case class TestIDReturn(value: _root_.izumi.test.domain02.TestIDReturn) extends TestAliasServ.TestADTIdReturnOutput
    implicit def intoTestIDReturn(value: _root_.izumi.test.domain02.TestIDReturn): TestAliasServ.TestADTIdReturnOutput = TestAliasServ.TestADTIdReturnOutput.TestIDReturn(value)
    implicit def fromTestIDReturn(value: TestAliasServ.TestADTIdReturnOutput.TestIDReturn): _root_.izumi.test.domain02.TestIDReturn = value.value
    final case class DTO1(value: _root_.izumi.test.domain02.DTO1) extends TestAliasServ.TestADTIdReturnOutput
    implicit def intoDTO1(value: _root_.izumi.test.domain02.DTO1): TestAliasServ.TestADTIdReturnOutput = TestAliasServ.TestADTIdReturnOutput.DTO1(value)
    implicit def fromDTO1(value: TestAliasServ.TestADTIdReturnOutput.DTO1): _root_.izumi.test.domain02.DTO1 = value.value
  }
  final case class TestADTIdImportedReturnInput() extends TestAliasServ.TestADTIdImportedReturnInput.Defn
  trait TestADTIdImportedReturnInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeTestADTIdImportedReturnInput: Encoder.AsObject[TestADTIdImportedReturnInput] = deriveEncoder[TestADTIdImportedReturnInput]
    implicit val decodeTestADTIdImportedReturnInput: Decoder[TestADTIdImportedReturnInput] = deriveDecoder[TestADTIdImportedReturnInput]
  }
  object TestADTIdImportedReturnInput extends TestAliasServ.TestADTIdImportedReturnInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestAliasServ.TestADTIdImportedReturnInput.Defn): TestAliasServ.TestADTIdImportedReturnInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestAliasServ.TestADTIdImportedReturnInput()
    }
    implicit object TestADTIdImportedReturnInput_cast_into_NestedAdtsServiceAdtNestedInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.TestADTIdImportedReturnInput, NestedAdtsService.AdtNestedInput] {
      override def convert(_value: TestAliasServ.TestADTIdImportedReturnInput): NestedAdtsService.AdtNestedInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NestedAdtsService.AdtNestedInput()
      }
    }
    implicit object TestADTIdImportedReturnInput_cast_into_TestAliasServTestADTIdReturnInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.TestADTIdImportedReturnInput, TestAliasServ.TestADTIdReturnInput] {
      override def convert(_value: TestAliasServ.TestADTIdImportedReturnInput): TestAliasServ.TestADTIdReturnInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.TestADTIdReturnInput()
      }
    }
    implicit object TestADTIdImportedReturnInput_upcast_TestADTIdImportedReturnInput extends izumi.idealingua.runtime.IRTCast[TestAliasServ.TestADTIdImportedReturnInput, TestAliasServ.TestADTIdImportedReturnInput] {
      override def convert(_value: TestAliasServ.TestADTIdImportedReturnInput): TestAliasServ.TestADTIdImportedReturnInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.TestADTIdImportedReturnInput()
      }
    }
    implicit class TestADTIdImportedReturnInputExtensions(override protected val _value: TestAliasServ.TestADTIdImportedReturnInput) extends izumi.idealingua.runtime.IRTConversions[TestAliasServ.TestADTIdImportedReturnInput]
  }
  sealed trait TestADTIdImportedReturnOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait TestADTIdImportedReturnOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeTestADTIdImportedReturnOutput: Encoder.AsObject[TestAliasServ.TestADTIdImportedReturnOutput] = Encoder.AsObject.instance {
      case v: TestAliasServ.TestADTIdImportedReturnOutput.ImportedIDForDomain2 =>
        Map("ImportedIDForDomain2" -> v.value).asJsonObject
      case v: TestAliasServ.TestADTIdImportedReturnOutput.DTO1 =>
        Map("DTO1" -> v.value).asJsonObject
    }
    implicit val decodeTestADTIdImportedReturnOutput: Decoder[TestAliasServ.TestADTIdImportedReturnOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "ImportedIDForDomain2" =>
          value.as[_root_.izumi.test.domain02.ImportedIDForDomain2].map(TestAliasServ.TestADTIdImportedReturnOutput.ImportedIDForDomain2.apply)
        case "DTO1" =>
          value.as[_root_.izumi.test.domain02.DTO1].map(TestAliasServ.TestADTIdImportedReturnOutput.DTO1.apply)
        case _ =>
          val cname = "izumi.test.domain02.TestAliasServ.TestADTIdImportedReturnOutput"
          val alts = List("ImportedIDForDomain2", "DTO1").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object TestADTIdImportedReturnOutput extends TestAliasServ.TestADTIdImportedReturnOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestAliasServ.TestADTIdImportedReturnOutput
    final case class ImportedIDForDomain2(value: _root_.izumi.test.domain02.ImportedIDForDomain2) extends TestAliasServ.TestADTIdImportedReturnOutput
    implicit def intoImportedIDForDomain2(value: _root_.izumi.test.domain02.ImportedIDForDomain2): TestAliasServ.TestADTIdImportedReturnOutput = TestAliasServ.TestADTIdImportedReturnOutput.ImportedIDForDomain2(value)
    implicit def fromImportedIDForDomain2(value: TestAliasServ.TestADTIdImportedReturnOutput.ImportedIDForDomain2): _root_.izumi.test.domain02.ImportedIDForDomain2 = value.value
    final case class DTO1(value: _root_.izumi.test.domain02.DTO1) extends TestAliasServ.TestADTIdImportedReturnOutput
    implicit def intoDTO1(value: _root_.izumi.test.domain02.DTO1): TestAliasServ.TestADTIdImportedReturnOutput = TestAliasServ.TestADTIdImportedReturnOutput.DTO1(value)
    implicit def fromDTO1(value: TestAliasServ.TestADTIdImportedReturnOutput.DTO1): _root_.izumi.test.domain02.DTO1 = value.value
  }
}

object TestAliasServCodecs {
  object getMassCoupons extends IRTCirceMarshaller {
    import TestAliasServ.getMassCoupons.*
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
  object ifaceMethod extends IRTCirceMarshaller {
    import TestAliasServ.ifaceMethod.*
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
  object testADTIdReturn extends IRTCirceMarshaller {
    import TestAliasServ.testADTIdReturn.*
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
  object testADTIdImportedReturn extends IRTCirceMarshaller {
    import TestAliasServ.testADTIdImportedReturn.*
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
       