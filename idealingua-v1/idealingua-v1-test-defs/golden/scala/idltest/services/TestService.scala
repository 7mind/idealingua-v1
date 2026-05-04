package idltest.services

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait TestServiceServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def unitToUnit(ctx: C): Just[Unit]
  def anotherVoid(ctx: C): Just[TestService.anotherVoid.Output]
  def unitResult(ctx: C, `package`: Package): Just[Unit]
  def parameterless(ctx: C): Just[String]
  def simpleMethod(ctx: C, a: String): Just[String]
  def simpleIntMethod(ctx: C, a: Int): Just[Int]
  def simpleMethodWithGenerics(ctx: C, a: List[String]): Just[List[String]]
  def simple(ctx: C, firstName: String, secondName: String): Just[TestService.simple.Output]
  def simpleEnum(ctx: C, v: TestServiceEnum): Just[String]
  def simpleEnum2(ctx: C, e: Environment): Just[String]
  def returnsList(ctx: C, e: Environment): Just[List[Package]]
  def returnsMap(ctx: C, e: Environment): Just[Map[String, Package]]
  def simpleGoReserved(ctx: C, `package`: Package): Just[Boolean]
  def simpleVoid(ctx: C, a: String): Just[Unit]
  def greetSingularOut(ctx: C, firstName: String, secondName: String): Just[String]
  def greetImplicitStructOut(ctx: C, firstName: String, secondName: String): Just[TestService.greetImplicitStructOut.Output]
  def greetImplicitStructMultilineSyntax(ctx: C, region: String, age: Byte): Just[TestService.greetImplicitStructMultilineSyntax.Output]
  def greetImplicitStructureMultilineCurlyBracesSyntax(ctx: C, region: String, age: Byte): Just[TestService.greetImplicitStructureMultilineCurlyBracesSyntax.Output]
  def greetAlgebraicOut(ctx: C, firstName: String, secondName: String): Just[TestService.greetAlgebraicOut.Output]
  def greetAlgebraicMultilineSyntax(ctx: C, firstName: String, secondName: String): Just[TestService.greetAlgebraicMultilineSyntax.Output]
  def alternative(ctx: C, firstName: String, secondName: String): Or[ErrorData, SuccessData]
  def alternativeSame(ctx: C, firstName: String, secondName: String): Or[SuccessData, SuccessData]
  def alternativeGeneric(ctx: C): Or[Set[ErrorData], List[SuccessData]]
  def alternativeGeneric2(ctx: C): Or[Map[String, ErrorData], Map[String, SuccessData]]
}

trait TestServiceClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def unitToUnit(): Just[Unit]
  def anotherVoid(): Just[TestService.anotherVoid.Output]
  def unitResult(`package`: Package): Just[Unit]
  def parameterless(): Just[String]
  def simpleMethod(a: String): Just[String]
  def simpleIntMethod(a: Int): Just[Int]
  def simpleMethodWithGenerics(a: List[String]): Just[List[String]]
  def simple(firstName: String, secondName: String): Just[TestService.simple.Output]
  def simpleEnum(v: TestServiceEnum): Just[String]
  def simpleEnum2(e: Environment): Just[String]
  def returnsList(e: Environment): Just[List[Package]]
  def returnsMap(e: Environment): Just[Map[String, Package]]
  def simpleGoReserved(`package`: Package): Just[Boolean]
  def simpleVoid(a: String): Just[Unit]
  def greetSingularOut(firstName: String, secondName: String): Just[String]
  def greetImplicitStructOut(firstName: String, secondName: String): Just[TestService.greetImplicitStructOut.Output]
  def greetImplicitStructMultilineSyntax(region: String, age: Byte): Just[TestService.greetImplicitStructMultilineSyntax.Output]
  def greetImplicitStructureMultilineCurlyBracesSyntax(region: String, age: Byte): Just[TestService.greetImplicitStructureMultilineCurlyBracesSyntax.Output]
  def greetAlgebraicOut(firstName: String, secondName: String): Just[TestService.greetAlgebraicOut.Output]
  def greetAlgebraicMultilineSyntax(firstName: String, secondName: String): Just[TestService.greetAlgebraicMultilineSyntax.Output]
  def alternative(firstName: String, secondName: String): Or[ErrorData, SuccessData]
  def alternativeSame(firstName: String, secondName: String): Or[SuccessData, SuccessData]
  def alternativeGeneric(): Or[Set[ErrorData], List[SuccessData]]
  def alternativeGeneric2(): Or[Map[String, ErrorData], Map[String, SuccessData]]
}

class TestServiceWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends TestServiceClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.idltest.services.TestService as _M
  def unitToUnit(): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.unitToUnit.Input()), _M.unitToUnit.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.unitToUnit.Output), method) if method == _M.unitToUnit.id =>
        _F.pure(())
      case v =>
        val id = "TestService.TestServiceWrappedClient.unitToUnit"
        val expected = classOf[_M.unitToUnit.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def anotherVoid(): Just[TestService.anotherVoid.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.anotherVoid.Input()), _M.anotherVoid.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.anotherVoid.Output), method) if method == _M.anotherVoid.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.anotherVoid"
        val expected = classOf[_M.anotherVoid.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def unitResult(`package`: Package): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.unitResult.Input(`package`)), _M.unitResult.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.unitResult.Output), method) if method == _M.unitResult.id =>
        _F.pure(())
      case v =>
        val id = "TestService.TestServiceWrappedClient.unitResult"
        val expected = classOf[_M.unitResult.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def parameterless(): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.parameterless.Input()), _M.parameterless.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.parameterless.Output), method) if method == _M.parameterless.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.parameterless"
        val expected = classOf[_M.parameterless.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleMethod(a: String): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleMethod.Input(a)), _M.simpleMethod.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simpleMethod.Output), method) if method == _M.simpleMethod.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleMethod"
        val expected = classOf[_M.simpleMethod.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleIntMethod(a: Int): Just[Int] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleIntMethod.Input(a)), _M.simpleIntMethod.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simpleIntMethod.Output), method) if method == _M.simpleIntMethod.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleIntMethod"
        val expected = classOf[_M.simpleIntMethod.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleMethodWithGenerics(a: List[String]): Just[List[String]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleMethodWithGenerics.Input(a)), _M.simpleMethodWithGenerics.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simpleMethodWithGenerics.Output), method) if method == _M.simpleMethodWithGenerics.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleMethodWithGenerics"
        val expected = classOf[_M.simpleMethodWithGenerics.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simple(firstName: String, secondName: String): Just[TestService.simple.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simple.Input(firstName, secondName)), _M.simple.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simple.Output), method) if method == _M.simple.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simple"
        val expected = classOf[_M.simple.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleEnum(v: TestServiceEnum): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleEnum.Input(v)), _M.simpleEnum.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simpleEnum.Output), method) if method == _M.simpleEnum.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleEnum"
        val expected = classOf[_M.simpleEnum.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleEnum2(e: Environment): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleEnum2.Input(e)), _M.simpleEnum2.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simpleEnum2.Output), method) if method == _M.simpleEnum2.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleEnum2"
        val expected = classOf[_M.simpleEnum2.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def returnsList(e: Environment): Just[List[Package]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.returnsList.Input(e)), _M.returnsList.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.returnsList.Output), method) if method == _M.returnsList.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.returnsList"
        val expected = classOf[_M.returnsList.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def returnsMap(e: Environment): Just[Map[String, Package]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.returnsMap.Input(e)), _M.returnsMap.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.returnsMap.Output), method) if method == _M.returnsMap.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.returnsMap"
        val expected = classOf[_M.returnsMap.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleGoReserved(`package`: Package): Just[Boolean] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleGoReserved.Input(`package`)), _M.simpleGoReserved.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.simpleGoReserved.Output), method) if method == _M.simpleGoReserved.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleGoReserved"
        val expected = classOf[_M.simpleGoReserved.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def simpleVoid(a: String): Just[Unit] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.simpleVoid.Input(a)), _M.simpleVoid.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(_: _M.simpleVoid.Output), method) if method == _M.simpleVoid.id =>
        _F.pure(())
      case v =>
        val id = "TestService.TestServiceWrappedClient.simpleVoid"
        val expected = classOf[_M.simpleVoid.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def greetSingularOut(firstName: String, secondName: String): Just[String] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.greetSingularOut.Input(firstName, secondName)), _M.greetSingularOut.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.greetSingularOut.Output), method) if method == _M.greetSingularOut.id =>
        _F.pure(v.value)
      case v =>
        val id = "TestService.TestServiceWrappedClient.greetSingularOut"
        val expected = classOf[_M.greetSingularOut.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def greetImplicitStructOut(firstName: String, secondName: String): Just[TestService.greetImplicitStructOut.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.greetImplicitStructOut.Input(firstName, secondName)), _M.greetImplicitStructOut.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.greetImplicitStructOut.Output), method) if method == _M.greetImplicitStructOut.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.greetImplicitStructOut"
        val expected = classOf[_M.greetImplicitStructOut.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def greetImplicitStructMultilineSyntax(region: String, age: Byte): Just[TestService.greetImplicitStructMultilineSyntax.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.greetImplicitStructMultilineSyntax.Input(region, age)), _M.greetImplicitStructMultilineSyntax.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.greetImplicitStructMultilineSyntax.Output), method) if method == _M.greetImplicitStructMultilineSyntax.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.greetImplicitStructMultilineSyntax"
        val expected = classOf[_M.greetImplicitStructMultilineSyntax.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def greetImplicitStructureMultilineCurlyBracesSyntax(region: String, age: Byte): Just[TestService.greetImplicitStructureMultilineCurlyBracesSyntax.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.greetImplicitStructureMultilineCurlyBracesSyntax.Input(region, age)), _M.greetImplicitStructureMultilineCurlyBracesSyntax.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.greetImplicitStructureMultilineCurlyBracesSyntax.Output), method) if method == _M.greetImplicitStructureMultilineCurlyBracesSyntax.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.greetImplicitStructureMultilineCurlyBracesSyntax"
        val expected = classOf[_M.greetImplicitStructureMultilineCurlyBracesSyntax.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def greetAlgebraicOut(firstName: String, secondName: String): Just[TestService.greetAlgebraicOut.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.greetAlgebraicOut.Input(firstName, secondName)), _M.greetAlgebraicOut.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.greetAlgebraicOut.Output), method) if method == _M.greetAlgebraicOut.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.greetAlgebraicOut"
        val expected = classOf[_M.greetAlgebraicOut.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def greetAlgebraicMultilineSyntax(firstName: String, secondName: String): Just[TestService.greetAlgebraicMultilineSyntax.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.greetAlgebraicMultilineSyntax.Input(firstName, secondName)), _M.greetAlgebraicMultilineSyntax.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.greetAlgebraicMultilineSyntax.Output), method) if method == _M.greetAlgebraicMultilineSyntax.id =>
        _F.pure(v)
      case v =>
        val id = "TestService.TestServiceWrappedClient.greetAlgebraicMultilineSyntax"
        val expected = classOf[_M.greetAlgebraicMultilineSyntax.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def alternative(firstName: String, secondName: String): Or[ErrorData, SuccessData] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.alternative.Input(firstName, secondName)), _M.alternative.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(r), method) if method == _M.alternative.id =>
        r match {
          case va: TestService.AlternativeOutput.Failure =>
            _F.fail(va.value)
          case va: TestService.AlternativeOutput.Success =>
            _F.pure(va.value)
          case v =>
            val id = "TestService.TestServiceWrappedClient.alternative"
            val expected = classOf[_M.alternative.Input].toString
            _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
              v.getClass
            }", v, None))
        }
      case v =>
        val id = "TestService.TestServiceWrappedClient.alternative"
        val expected = classOf[_M.alternative.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def alternativeSame(firstName: String, secondName: String): Or[SuccessData, SuccessData] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.alternativeSame.Input(firstName, secondName)), _M.alternativeSame.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(r), method) if method == _M.alternativeSame.id =>
        r match {
          case va: TestService.AlternativeSameOutput.Failure =>
            _F.fail(va.value)
          case va: TestService.AlternativeSameOutput.Success =>
            _F.pure(va.value)
          case v =>
            val id = "TestService.TestServiceWrappedClient.alternativeSame"
            val expected = classOf[_M.alternativeSame.Input].toString
            _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
              v.getClass
            }", v, None))
        }
      case v =>
        val id = "TestService.TestServiceWrappedClient.alternativeSame"
        val expected = classOf[_M.alternativeSame.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def alternativeGeneric(): Or[Set[ErrorData], List[SuccessData]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.alternativeGeneric.Input()), _M.alternativeGeneric.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(r), method) if method == _M.alternativeGeneric.id =>
        r match {
          case va: TestService.AlternativeGenericOutput.Failure =>
            _F.fail(va.value)
          case va: TestService.AlternativeGenericOutput.Success =>
            _F.pure(va.value)
          case v =>
            val id = "TestService.TestServiceWrappedClient.alternativeGeneric"
            val expected = classOf[_M.alternativeGeneric.Input].toString
            _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
              v.getClass
            }", v, None))
        }
      case v =>
        val id = "TestService.TestServiceWrappedClient.alternativeGeneric"
        val expected = classOf[_M.alternativeGeneric.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def alternativeGeneric2(): Or[Map[String, ErrorData], Map[String, SuccessData]] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.alternativeGeneric2.Input()), _M.alternativeGeneric2.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(r), method) if method == _M.alternativeGeneric2.id =>
        r match {
          case va: TestService.AlternativeGeneric2Output.Failure =>
            _F.fail(va.value)
          case va: TestService.AlternativeGeneric2Output.Success =>
            _F.pure(va.value)
          case v =>
            val id = "TestService.TestServiceWrappedClient.alternativeGeneric2"
            val expected = classOf[_M.alternativeGeneric2.Input].toString
            _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
              v.getClass
            }", v, None))
        }
      case v =>
        val id = "TestService.TestServiceWrappedClient.alternativeGeneric2"
        val expected = classOf[_M.alternativeGeneric2.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object TestServiceWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(TestService.unitToUnit.id -> TestServiceCodecs.unitToUnit, TestService.anotherVoid.id -> TestServiceCodecs.anotherVoid, TestService.unitResult.id -> TestServiceCodecs.unitResult, TestService.parameterless.id -> TestServiceCodecs.parameterless, TestService.simpleMethod.id -> TestServiceCodecs.simpleMethod, TestService.simpleIntMethod.id -> TestServiceCodecs.simpleIntMethod, TestService.simpleMethodWithGenerics.id -> TestServiceCodecs.simpleMethodWithGenerics, TestService.simple.id -> TestServiceCodecs.simple, TestService.simpleEnum.id -> TestServiceCodecs.simpleEnum, TestService.simpleEnum2.id -> TestServiceCodecs.simpleEnum2, TestService.returnsList.id -> TestServiceCodecs.returnsList, TestService.returnsMap.id -> TestServiceCodecs.returnsMap, TestService.simpleGoReserved.id -> TestServiceCodecs.simpleGoReserved, TestService.simpleVoid.id -> TestServiceCodecs.simpleVoid, TestService.greetSingularOut.id -> TestServiceCodecs.greetSingularOut, TestService.greetImplicitStructOut.id -> TestServiceCodecs.greetImplicitStructOut, TestService.greetImplicitStructMultilineSyntax.id -> TestServiceCodecs.greetImplicitStructMultilineSyntax, TestService.greetImplicitStructureMultilineCurlyBracesSyntax.id -> TestServiceCodecs.greetImplicitStructureMultilineCurlyBracesSyntax, TestService.greetAlgebraicOut.id -> TestServiceCodecs.greetAlgebraicOut, TestService.greetAlgebraicMultilineSyntax.id -> TestServiceCodecs.greetAlgebraicMultilineSyntax, TestService.alternative.id -> TestServiceCodecs.alternative, TestService.alternativeSame.id -> TestServiceCodecs.alternativeSame, TestService.alternativeGeneric.id -> TestServiceCodecs.alternativeGeneric, TestService.alternativeGeneric2.id -> TestServiceCodecs.alternativeGeneric2)
  }
}

class TestServiceWrappedServer[Or[+_, +_]: IRTIO2, C](_service: TestServiceServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = TestService.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](unitToUnit, anotherVoid, unitResult, parameterless, simpleMethod, simpleIntMethod, simpleMethodWithGenerics, simple, simpleEnum, simpleEnum2, returnsList, returnsMap, simpleGoReserved, simpleVoid, greetSingularOut, greetImplicitStructOut, greetImplicitStructMultilineSyntax, greetImplicitStructureMultilineCurlyBracesSyntax, greetAlgebraicOut, greetAlgebraicMultilineSyntax, alternative, alternativeSame, alternativeGeneric, alternativeGeneric2).map(m => m.signature.id -> m).toMap
  }
  object unitToUnit extends IRTMethodWrapper[Or, C] {
    import TestService.unitToUnit.*
    val signature: TestService.unitToUnit.type = TestService.unitToUnit
    val marshaller: TestServiceCodecs.unitToUnit.type = TestServiceCodecs.unitToUnit
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.unitToUnit(ctx))(_ => new Output())
    }
  }
  object anotherVoid extends IRTMethodWrapper[Or, C] {
    import TestService.anotherVoid.*
    val signature: TestService.anotherVoid.type = TestService.anotherVoid
    val marshaller: TestServiceCodecs.anotherVoid.type = TestServiceCodecs.anotherVoid
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.anotherVoid(ctx)
    }
  }
  object unitResult extends IRTMethodWrapper[Or, C] {
    import TestService.unitResult.*
    val signature: TestService.unitResult.type = TestService.unitResult
    val marshaller: TestServiceCodecs.unitResult.type = TestServiceCodecs.unitResult
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.unitResult(ctx, input.`package`))(_ => new Output())
    }
  }
  object parameterless extends IRTMethodWrapper[Or, C] {
    import TestService.parameterless.*
    val signature: TestService.parameterless.type = TestService.parameterless
    val marshaller: TestServiceCodecs.parameterless.type = TestServiceCodecs.parameterless
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.parameterless(ctx))(v => new Output(v))
    }
  }
  object simpleMethod extends IRTMethodWrapper[Or, C] {
    import TestService.simpleMethod.*
    val signature: TestService.simpleMethod.type = TestService.simpleMethod
    val marshaller: TestServiceCodecs.simpleMethod.type = TestServiceCodecs.simpleMethod
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleMethod(ctx, input.a))(v => new Output(v))
    }
  }
  object simpleIntMethod extends IRTMethodWrapper[Or, C] {
    import TestService.simpleIntMethod.*
    val signature: TestService.simpleIntMethod.type = TestService.simpleIntMethod
    val marshaller: TestServiceCodecs.simpleIntMethod.type = TestServiceCodecs.simpleIntMethod
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleIntMethod(ctx, input.a))(v => new Output(v))
    }
  }
  object simpleMethodWithGenerics extends IRTMethodWrapper[Or, C] {
    import TestService.simpleMethodWithGenerics.*
    val signature: TestService.simpleMethodWithGenerics.type = TestService.simpleMethodWithGenerics
    val marshaller: TestServiceCodecs.simpleMethodWithGenerics.type = TestServiceCodecs.simpleMethodWithGenerics
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleMethodWithGenerics(ctx, input.a))(v => new Output(v))
    }
  }
  object simple extends IRTMethodWrapper[Or, C] {
    import TestService.simple.*
    val signature: TestService.simple.type = TestService.simple
    val marshaller: TestServiceCodecs.simple.type = TestServiceCodecs.simple
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.simple(ctx, input.firstName, input.secondName)
    }
  }
  object simpleEnum extends IRTMethodWrapper[Or, C] {
    import TestService.simpleEnum.*
    val signature: TestService.simpleEnum.type = TestService.simpleEnum
    val marshaller: TestServiceCodecs.simpleEnum.type = TestServiceCodecs.simpleEnum
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleEnum(ctx, input.v))(v => new Output(v))
    }
  }
  object simpleEnum2 extends IRTMethodWrapper[Or, C] {
    import TestService.simpleEnum2.*
    val signature: TestService.simpleEnum2.type = TestService.simpleEnum2
    val marshaller: TestServiceCodecs.simpleEnum2.type = TestServiceCodecs.simpleEnum2
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleEnum2(ctx, input.e))(v => new Output(v))
    }
  }
  object returnsList extends IRTMethodWrapper[Or, C] {
    import TestService.returnsList.*
    val signature: TestService.returnsList.type = TestService.returnsList
    val marshaller: TestServiceCodecs.returnsList.type = TestServiceCodecs.returnsList
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.returnsList(ctx, input.e))(v => new Output(v))
    }
  }
  object returnsMap extends IRTMethodWrapper[Or, C] {
    import TestService.returnsMap.*
    val signature: TestService.returnsMap.type = TestService.returnsMap
    val marshaller: TestServiceCodecs.returnsMap.type = TestServiceCodecs.returnsMap
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.returnsMap(ctx, input.e))(v => new Output(v))
    }
  }
  object simpleGoReserved extends IRTMethodWrapper[Or, C] {
    import TestService.simpleGoReserved.*
    val signature: TestService.simpleGoReserved.type = TestService.simpleGoReserved
    val marshaller: TestServiceCodecs.simpleGoReserved.type = TestServiceCodecs.simpleGoReserved
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleGoReserved(ctx, input.`package`))(v => new Output(v))
    }
  }
  object simpleVoid extends IRTMethodWrapper[Or, C] {
    import TestService.simpleVoid.*
    val signature: TestService.simpleVoid.type = TestService.simpleVoid
    val marshaller: TestServiceCodecs.simpleVoid.type = TestServiceCodecs.simpleVoid
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.simpleVoid(ctx, input.a))(_ => new Output())
    }
  }
  object greetSingularOut extends IRTMethodWrapper[Or, C] {
    import TestService.greetSingularOut.*
    val signature: TestService.greetSingularOut.type = TestService.greetSingularOut
    val marshaller: TestServiceCodecs.greetSingularOut.type = TestServiceCodecs.greetSingularOut
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.greetSingularOut(ctx, input.firstName, input.secondName))(v => new Output(v))
    }
  }
  object greetImplicitStructOut extends IRTMethodWrapper[Or, C] {
    import TestService.greetImplicitStructOut.*
    val signature: TestService.greetImplicitStructOut.type = TestService.greetImplicitStructOut
    val marshaller: TestServiceCodecs.greetImplicitStructOut.type = TestServiceCodecs.greetImplicitStructOut
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.greetImplicitStructOut(ctx, input.firstName, input.secondName)
    }
  }
  object greetImplicitStructMultilineSyntax extends IRTMethodWrapper[Or, C] {
    import TestService.greetImplicitStructMultilineSyntax.*
    val signature: TestService.greetImplicitStructMultilineSyntax.type = TestService.greetImplicitStructMultilineSyntax
    val marshaller: TestServiceCodecs.greetImplicitStructMultilineSyntax.type = TestServiceCodecs.greetImplicitStructMultilineSyntax
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.greetImplicitStructMultilineSyntax(ctx, input.region, input.age)
    }
  }
  object greetImplicitStructureMultilineCurlyBracesSyntax extends IRTMethodWrapper[Or, C] {
    import TestService.greetImplicitStructureMultilineCurlyBracesSyntax.*
    val signature: TestService.greetImplicitStructureMultilineCurlyBracesSyntax.type = TestService.greetImplicitStructureMultilineCurlyBracesSyntax
    val marshaller: TestServiceCodecs.greetImplicitStructureMultilineCurlyBracesSyntax.type = TestServiceCodecs.greetImplicitStructureMultilineCurlyBracesSyntax
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.greetImplicitStructureMultilineCurlyBracesSyntax(ctx, input.region, input.age)
    }
  }
  object greetAlgebraicOut extends IRTMethodWrapper[Or, C] {
    import TestService.greetAlgebraicOut.*
    val signature: TestService.greetAlgebraicOut.type = TestService.greetAlgebraicOut
    val marshaller: TestServiceCodecs.greetAlgebraicOut.type = TestServiceCodecs.greetAlgebraicOut
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.greetAlgebraicOut(ctx, input.firstName, input.secondName)
    }
  }
  object greetAlgebraicMultilineSyntax extends IRTMethodWrapper[Or, C] {
    import TestService.greetAlgebraicMultilineSyntax.*
    val signature: TestService.greetAlgebraicMultilineSyntax.type = TestService.greetAlgebraicMultilineSyntax
    val marshaller: TestServiceCodecs.greetAlgebraicMultilineSyntax.type = TestServiceCodecs.greetAlgebraicMultilineSyntax
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.greetAlgebraicMultilineSyntax(ctx, input.firstName, input.secondName)
    }
  }
  object alternative extends IRTMethodWrapper[Or, C] {
    import TestService.alternative.*
    val signature: TestService.alternative.type = TestService.alternative
    val marshaller: TestServiceCodecs.alternative.type = TestServiceCodecs.alternative
    def invoke(ctx: C, input: Input): Just[Output] = {
      _F.redeem(_service.alternative(ctx, input.firstName, input.secondName))(err => _F.pure(new TestService.AlternativeOutput.Failure(err)), succ => _F.pure(new TestService.AlternativeOutput.Success(succ)))
    }
  }
  object alternativeSame extends IRTMethodWrapper[Or, C] {
    import TestService.alternativeSame.*
    val signature: TestService.alternativeSame.type = TestService.alternativeSame
    val marshaller: TestServiceCodecs.alternativeSame.type = TestServiceCodecs.alternativeSame
    def invoke(ctx: C, input: Input): Just[Output] = {
      _F.redeem(_service.alternativeSame(ctx, input.firstName, input.secondName))(err => _F.pure(new TestService.AlternativeSameOutput.Failure(err)), succ => _F.pure(new TestService.AlternativeSameOutput.Success(succ)))
    }
  }
  object alternativeGeneric extends IRTMethodWrapper[Or, C] {
    import TestService.alternativeGeneric.*
    val signature: TestService.alternativeGeneric.type = TestService.alternativeGeneric
    val marshaller: TestServiceCodecs.alternativeGeneric.type = TestServiceCodecs.alternativeGeneric
    def invoke(ctx: C, input: Input): Just[Output] = {
      _F.redeem(_service.alternativeGeneric(ctx))(err => _F.pure(new TestService.AlternativeGenericOutput.Failure(err)), succ => _F.pure(new TestService.AlternativeGenericOutput.Success(succ)))
    }
  }
  object alternativeGeneric2 extends IRTMethodWrapper[Or, C] {
    import TestService.alternativeGeneric2.*
    val signature: TestService.alternativeGeneric2.type = TestService.alternativeGeneric2
    val marshaller: TestServiceCodecs.alternativeGeneric2.type = TestServiceCodecs.alternativeGeneric2
    def invoke(ctx: C, input: Input): Just[Output] = {
      _F.redeem(_service.alternativeGeneric2(ctx))(err => _F.pure(new TestService.AlternativeGeneric2Output.Failure(err)), succ => _F.pure(new TestService.AlternativeGeneric2Output.Success(succ)))
    }
  }
}

object TestServiceWrappedServer

object TestService {
  final val serviceId: IRTServiceId = IRTServiceId("TestService")
  object unitToUnit extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("unitToUnit"))
    type Input = UnitToUnitInput
    type Output = UnitToUnitOutput
  }
  object anotherVoid extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("anotherVoid"))
    type Input = AnotherVoidInput
    type Output = AnotherVoidOutput
  }
  object unitResult extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("unitResult"))
    type Input = UnitResultInput
    type Output = UnitResultOutput
  }
  object parameterless extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("parameterless"))
    type Input = ParameterlessInput
    type Output = ParameterlessOutput
  }
  object simpleMethod extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleMethod"))
    type Input = SimpleMethodInput
    type Output = SimpleMethodOutput
  }
  object simpleIntMethod extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleIntMethod"))
    type Input = SimpleIntMethodInput
    type Output = SimpleIntMethodOutput
  }
  object simpleMethodWithGenerics extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleMethodWithGenerics"))
    type Input = SimpleMethodWithGenericsInput
    type Output = SimpleMethodWithGenericsOutput
  }
  object simple extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simple"))
    type Input = SimpleInput
    type Output = SimpleOutput
  }
  object simpleEnum extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleEnum"))
    type Input = SimpleEnumInput
    type Output = SimpleEnumOutput
  }
  object simpleEnum2 extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleEnum2"))
    type Input = SimpleEnum2Input
    type Output = SimpleEnum2Output
  }
  object returnsList extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("returnsList"))
    type Input = ReturnsListInput
    type Output = ReturnsListOutput
  }
  object returnsMap extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("returnsMap"))
    type Input = ReturnsMapInput
    type Output = ReturnsMapOutput
  }
  object simpleGoReserved extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleGoReserved"))
    type Input = SimpleGoReservedInput
    type Output = SimpleGoReservedOutput
  }
  object simpleVoid extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("simpleVoid"))
    type Input = SimpleVoidInput
    type Output = SimpleVoidOutput
  }
  object greetSingularOut extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("greetSingularOut"))
    type Input = GreetSingularOutInput
    type Output = GreetSingularOutOutput
  }
  object greetImplicitStructOut extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("greetImplicitStructOut"))
    type Input = GreetImplicitStructOutInput
    type Output = GreetImplicitStructOutOutput
  }
  object greetImplicitStructMultilineSyntax extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("greetImplicitStructMultilineSyntax"))
    type Input = GreetImplicitStructMultilineSyntaxInput
    type Output = GreetImplicitStructMultilineSyntaxOutput
  }
  object greetImplicitStructureMultilineCurlyBracesSyntax extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("greetImplicitStructureMultilineCurlyBracesSyntax"))
    type Input = GreetImplicitStructureMultilineCurlyBracesSyntaxInput
    type Output = GreetImplicitStructureMultilineCurlyBracesSyntaxOutput
  }
  object greetAlgebraicOut extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("greetAlgebraicOut"))
    type Input = GreetAlgebraicOutInput
    type Output = GreetAlgebraicOutOutput
  }
  object greetAlgebraicMultilineSyntax extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("greetAlgebraicMultilineSyntax"))
    type Input = GreetAlgebraicMultilineSyntaxInput
    type Output = GreetAlgebraicMultilineSyntaxOutput
  }
  object alternative extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("alternative"))
    type Input = AlternativeInput
    type Output = AlternativeOutput
  }
  object alternativeSame extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("alternativeSame"))
    type Input = AlternativeSameInput
    type Output = AlternativeSameOutput
  }
  object alternativeGeneric extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("alternativeGeneric"))
    type Input = AlternativeGenericInput
    type Output = AlternativeGenericOutput
  }
  object alternativeGeneric2 extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("alternativeGeneric2"))
    type Input = AlternativeGeneric2Input
    type Output = AlternativeGeneric2Output
  }
  final case class UnitToUnitInput() extends TestService.UnitToUnitInput.Defn
  trait UnitToUnitInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUnitToUnitInput: Encoder.AsObject[UnitToUnitInput] = deriveEncoder[UnitToUnitInput]
    implicit val decodeUnitToUnitInput: Decoder[UnitToUnitInput] = deriveDecoder[UnitToUnitInput]
  }
  object UnitToUnitInput extends TestService.UnitToUnitInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.UnitToUnitInput.Defn): TestService.UnitToUnitInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.UnitToUnitInput()
    }
    implicit object UnitToUnitInput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object UnitToUnitInput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object UnitToUnitInput_upcast_UnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitInput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.UnitToUnitInput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit class UnitToUnitInputExtensions(override protected val _value: TestService.UnitToUnitInput) extends izumi.idealingua.runtime.IRTConversions[TestService.UnitToUnitInput]
  }
  final case class UnitToUnitOutput() extends TestService.UnitToUnitOutput.Defn
  trait UnitToUnitOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUnitToUnitOutput: Encoder.AsObject[UnitToUnitOutput] = deriveEncoder[UnitToUnitOutput]
    implicit val decodeUnitToUnitOutput: Decoder[UnitToUnitOutput] = deriveDecoder[UnitToUnitOutput]
  }
  object UnitToUnitOutput extends TestService.UnitToUnitOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.UnitToUnitOutput.Defn): TestService.UnitToUnitOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.UnitToUnitOutput()
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object UnitToUnitOutput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object UnitToUnitOutput_upcast_UnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitToUnitOutput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.UnitToUnitOutput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit class UnitToUnitOutputExtensions(override protected val _value: TestService.UnitToUnitOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.UnitToUnitOutput]
  }
  final case class AnotherVoidInput() extends TestService.AnotherVoidInput.Defn
  trait AnotherVoidInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAnotherVoidInput: Encoder.AsObject[AnotherVoidInput] = deriveEncoder[AnotherVoidInput]
    implicit val decodeAnotherVoidInput: Decoder[AnotherVoidInput] = deriveDecoder[AnotherVoidInput]
  }
  object AnotherVoidInput extends TestService.AnotherVoidInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.AnotherVoidInput.Defn): TestService.AnotherVoidInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.AnotherVoidInput()
    }
    implicit object AnotherVoidInput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object AnotherVoidInput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object AnotherVoidInput_upcast_AnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidInput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.AnotherVoidInput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit class AnotherVoidInputExtensions(override protected val _value: TestService.AnotherVoidInput) extends izumi.idealingua.runtime.IRTConversions[TestService.AnotherVoidInput]
  }
  final case class AnotherVoidOutput() extends TestService.AnotherVoidOutput.Defn
  trait AnotherVoidOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAnotherVoidOutput: Encoder.AsObject[AnotherVoidOutput] = deriveEncoder[AnotherVoidOutput]
    implicit val decodeAnotherVoidOutput: Decoder[AnotherVoidOutput] = deriveDecoder[AnotherVoidOutput]
  }
  object AnotherVoidOutput extends TestService.AnotherVoidOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.AnotherVoidOutput.Defn): TestService.AnotherVoidOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.AnotherVoidOutput()
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object AnotherVoidOutput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object AnotherVoidOutput_upcast_AnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AnotherVoidOutput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.AnotherVoidOutput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit class AnotherVoidOutputExtensions(override protected val _value: TestService.AnotherVoidOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.AnotherVoidOutput]
  }
  final case class UnitResultInput(`package`: Package) extends TestService.UnitResultInput.Defn
  trait UnitResultInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUnitResultInput: Encoder.AsObject[UnitResultInput] = deriveEncoder[UnitResultInput]
    implicit val decodeUnitResultInput: Decoder[UnitResultInput] = deriveDecoder[UnitResultInput]
  }
  object UnitResultInput extends TestService.UnitResultInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def `package`: Package }
    def apply(`package`: Package.Defn): TestService.UnitResultInput = {
      assert(`package`.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.UnitResultInput(`package` = Package(`package`))
    }
    def apply(defn: TestService.UnitResultInput.Defn): TestService.UnitResultInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.UnitResultInput(`package` = defn.`package`)
    }
    implicit object UnitResultInput_cast_into_TestServiceSimpleGoReservedInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultInput, TestService.SimpleGoReservedInput] {
      override def convert(_value: TestService.UnitResultInput): TestService.SimpleGoReservedInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleGoReservedInput(`package` = _value.`package`)
      }
    }
    implicit object UnitResultInput_upcast_UnitResultInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultInput, TestService.UnitResultInput] {
      override def convert(_value: TestService.UnitResultInput): TestService.UnitResultInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultInput(`package` = _value.`package`)
      }
    }
    implicit class UnitResultInputExtensions(override protected val _value: TestService.UnitResultInput) extends izumi.idealingua.runtime.IRTConversions[TestService.UnitResultInput]
  }
  final case class UnitResultOutput() extends TestService.UnitResultOutput.Defn
  trait UnitResultOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUnitResultOutput: Encoder.AsObject[UnitResultOutput] = deriveEncoder[UnitResultOutput]
    implicit val decodeUnitResultOutput: Decoder[UnitResultOutput] = deriveDecoder[UnitResultOutput]
  }
  object UnitResultOutput extends TestService.UnitResultOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.UnitResultOutput.Defn): TestService.UnitResultOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.UnitResultOutput()
    }
    implicit object UnitResultOutput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object UnitResultOutput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.UnitResultOutput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object UnitResultOutput_upcast_UnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.UnitResultOutput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.UnitResultOutput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit class UnitResultOutputExtensions(override protected val _value: TestService.UnitResultOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.UnitResultOutput]
  }
  final case class ParameterlessInput() extends TestService.ParameterlessInput.Defn
  trait ParameterlessInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeParameterlessInput: Encoder.AsObject[ParameterlessInput] = deriveEncoder[ParameterlessInput]
    implicit val decodeParameterlessInput: Decoder[ParameterlessInput] = deriveDecoder[ParameterlessInput]
  }
  object ParameterlessInput extends TestService.ParameterlessInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.ParameterlessInput.Defn): TestService.ParameterlessInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ParameterlessInput()
    }
    implicit object ParameterlessInput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object ParameterlessInput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.ParameterlessInput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object ParameterlessInput_upcast_ParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessInput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.ParameterlessInput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit class ParameterlessInputExtensions(override protected val _value: TestService.ParameterlessInput) extends izumi.idealingua.runtime.IRTConversions[TestService.ParameterlessInput]
  }
  final case class ParameterlessOutput(value: String) extends AnyVal with TestService.ParameterlessOutput.Defn
  trait ParameterlessOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedParameterlessOutput: Encoder[ParameterlessOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedParameterlessOutput: Decoder[ParameterlessOutput] = Decoder.instance {
      v => v.as[String].map(d => ParameterlessOutput(d))
    }
  }
  object ParameterlessOutput extends TestService.ParameterlessOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestService.ParameterlessOutput = {
      new TestService.ParameterlessOutput(value = value)
    }
    def apply(defn: TestService.ParameterlessOutput.Defn): TestService.ParameterlessOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ParameterlessOutput(value = defn.value)
    }
    implicit object ParameterlessOutput_cast_into_TestServiceSimpleMethodOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessOutput, TestService.SimpleMethodOutput] {
      override def convert(_value: TestService.ParameterlessOutput): TestService.SimpleMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodOutput(value = _value.value)
      }
    }
    implicit object ParameterlessOutput_cast_into_TestServiceSimpleEnumOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessOutput, TestService.SimpleEnumOutput] {
      override def convert(_value: TestService.ParameterlessOutput): TestService.SimpleEnumOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnumOutput(value = _value.value)
      }
    }
    implicit object ParameterlessOutput_cast_into_TestServiceSimpleEnum2Output extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessOutput, TestService.SimpleEnum2Output] {
      override def convert(_value: TestService.ParameterlessOutput): TestService.SimpleEnum2Output = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Output(value = _value.value)
      }
    }
    implicit object ParameterlessOutput_cast_into_TestServiceGreetSingularOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessOutput, TestService.GreetSingularOutOutput] {
      override def convert(_value: TestService.ParameterlessOutput): TestService.GreetSingularOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutOutput(value = _value.value)
      }
    }
    implicit object ParameterlessOutput_upcast_ParameterlessOutput extends izumi.idealingua.runtime.IRTCast[TestService.ParameterlessOutput, TestService.ParameterlessOutput] {
      override def convert(_value: TestService.ParameterlessOutput): TestService.ParameterlessOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessOutput(value = _value.value)
      }
    }
    implicit class ParameterlessOutputExtensions(override protected val _value: TestService.ParameterlessOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.ParameterlessOutput]
  }
  final case class SimpleMethodInput(a: String) extends AnyVal with TestService.SimpleMethodInput.Defn
  trait SimpleMethodInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleMethodInput: Encoder.AsObject[SimpleMethodInput] = Encoder.forProduct1[SimpleMethodInput, String]("a")((v: SimpleMethodInput) => v.a)
    implicit val decodeSimpleMethodInput: Decoder[SimpleMethodInput] = Decoder.forProduct1[SimpleMethodInput, String]("a")((d: String) => new SimpleMethodInput(d))
  }
  object SimpleMethodInput extends TestService.SimpleMethodInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: String }
    def apply(a: String): TestService.SimpleMethodInput = {
      new TestService.SimpleMethodInput(a = a)
    }
    def apply(defn: TestService.SimpleMethodInput.Defn): TestService.SimpleMethodInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleMethodInput(a = defn.a)
    }
    implicit object SimpleMethodInput_cast_into_TestServiceSimpleVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodInput, TestService.SimpleVoidInput] {
      override def convert(_value: TestService.SimpleMethodInput): TestService.SimpleVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidInput(a = _value.a)
      }
    }
    implicit object SimpleMethodInput_cast_into_TestServiceGreetImplicitStructOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodInput, TestService.GreetImplicitStructOutOutput] {
      override def convert(_value: TestService.SimpleMethodInput): TestService.GreetImplicitStructOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutOutput(a = _value.a)
      }
    }
    implicit object SimpleMethodInput_upcast_SimpleMethodInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodInput, TestService.SimpleMethodInput] {
      override def convert(_value: TestService.SimpleMethodInput): TestService.SimpleMethodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodInput(a = _value.a)
      }
    }
    implicit class SimpleMethodInputExtensions(override protected val _value: TestService.SimpleMethodInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleMethodInput]
  }
  final case class SimpleMethodOutput(value: String) extends AnyVal with TestService.SimpleMethodOutput.Defn
  trait SimpleMethodOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSimpleMethodOutput: Encoder[SimpleMethodOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSimpleMethodOutput: Decoder[SimpleMethodOutput] = Decoder.instance {
      v => v.as[String].map(d => SimpleMethodOutput(d))
    }
  }
  object SimpleMethodOutput extends TestService.SimpleMethodOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestService.SimpleMethodOutput = {
      new TestService.SimpleMethodOutput(value = value)
    }
    def apply(defn: TestService.SimpleMethodOutput.Defn): TestService.SimpleMethodOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleMethodOutput(value = defn.value)
    }
    implicit object SimpleMethodOutput_cast_into_TestServiceParameterlessOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodOutput, TestService.ParameterlessOutput] {
      override def convert(_value: TestService.SimpleMethodOutput): TestService.ParameterlessOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessOutput(value = _value.value)
      }
    }
    implicit object SimpleMethodOutput_cast_into_TestServiceSimpleEnumOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodOutput, TestService.SimpleEnumOutput] {
      override def convert(_value: TestService.SimpleMethodOutput): TestService.SimpleEnumOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnumOutput(value = _value.value)
      }
    }
    implicit object SimpleMethodOutput_cast_into_TestServiceSimpleEnum2Output extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodOutput, TestService.SimpleEnum2Output] {
      override def convert(_value: TestService.SimpleMethodOutput): TestService.SimpleEnum2Output = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Output(value = _value.value)
      }
    }
    implicit object SimpleMethodOutput_cast_into_TestServiceGreetSingularOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodOutput, TestService.GreetSingularOutOutput] {
      override def convert(_value: TestService.SimpleMethodOutput): TestService.GreetSingularOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutOutput(value = _value.value)
      }
    }
    implicit object SimpleMethodOutput_upcast_SimpleMethodOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodOutput, TestService.SimpleMethodOutput] {
      override def convert(_value: TestService.SimpleMethodOutput): TestService.SimpleMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodOutput(value = _value.value)
      }
    }
    implicit class SimpleMethodOutputExtensions(override protected val _value: TestService.SimpleMethodOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleMethodOutput]
  }
  final case class SimpleIntMethodInput(a: Int) extends AnyVal with TestService.SimpleIntMethodInput.Defn
  trait SimpleIntMethodInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleIntMethodInput: Encoder.AsObject[SimpleIntMethodInput] = Encoder.forProduct1[SimpleIntMethodInput, Int]("a")((v: SimpleIntMethodInput) => v.a)
    implicit val decodeSimpleIntMethodInput: Decoder[SimpleIntMethodInput] = Decoder.forProduct1[SimpleIntMethodInput, Int]("a")((d: Int) => new SimpleIntMethodInput(d))
  }
  object SimpleIntMethodInput extends TestService.SimpleIntMethodInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: Int }
    def apply(a: Int): TestService.SimpleIntMethodInput = {
      new TestService.SimpleIntMethodInput(a = a)
    }
    def apply(defn: TestService.SimpleIntMethodInput.Defn): TestService.SimpleIntMethodInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleIntMethodInput(a = defn.a)
    }
    implicit object SimpleIntMethodInput_upcast_SimpleIntMethodInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleIntMethodInput, TestService.SimpleIntMethodInput] {
      override def convert(_value: TestService.SimpleIntMethodInput): TestService.SimpleIntMethodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleIntMethodInput(a = _value.a)
      }
    }
    implicit class SimpleIntMethodInputExtensions(override protected val _value: TestService.SimpleIntMethodInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleIntMethodInput]
  }
  final case class SimpleIntMethodOutput(value: Int) extends AnyVal with TestService.SimpleIntMethodOutput.Defn
  trait SimpleIntMethodOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSimpleIntMethodOutput: Encoder[SimpleIntMethodOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSimpleIntMethodOutput: Decoder[SimpleIntMethodOutput] = Decoder.instance {
      v => v.as[Int].map(d => SimpleIntMethodOutput(d))
    }
  }
  object SimpleIntMethodOutput extends TestService.SimpleIntMethodOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Int }
    def apply(value: Int): TestService.SimpleIntMethodOutput = {
      new TestService.SimpleIntMethodOutput(value = value)
    }
    def apply(defn: TestService.SimpleIntMethodOutput.Defn): TestService.SimpleIntMethodOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleIntMethodOutput(value = defn.value)
    }
    implicit object SimpleIntMethodOutput_upcast_SimpleIntMethodOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleIntMethodOutput, TestService.SimpleIntMethodOutput] {
      override def convert(_value: TestService.SimpleIntMethodOutput): TestService.SimpleIntMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleIntMethodOutput(value = _value.value)
      }
    }
    implicit class SimpleIntMethodOutputExtensions(override protected val _value: TestService.SimpleIntMethodOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleIntMethodOutput]
  }
  final case class SimpleMethodWithGenericsInput(a: List[String]) extends TestService.SimpleMethodWithGenericsInput.Defn
  trait SimpleMethodWithGenericsInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleMethodWithGenericsInput: Encoder.AsObject[SimpleMethodWithGenericsInput] = deriveEncoder[SimpleMethodWithGenericsInput]
    implicit val decodeSimpleMethodWithGenericsInput: Decoder[SimpleMethodWithGenericsInput] = deriveDecoder[SimpleMethodWithGenericsInput]
  }
  object SimpleMethodWithGenericsInput extends TestService.SimpleMethodWithGenericsInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def a: List[String] }
    def apply(a: List[String]): TestService.SimpleMethodWithGenericsInput = {
      new TestService.SimpleMethodWithGenericsInput(a = a)
    }
    def apply(defn: TestService.SimpleMethodWithGenericsInput.Defn): TestService.SimpleMethodWithGenericsInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleMethodWithGenericsInput(a = defn.a)
    }
    implicit object SimpleMethodWithGenericsInput_upcast_SimpleMethodWithGenericsInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodWithGenericsInput, TestService.SimpleMethodWithGenericsInput] {
      override def convert(_value: TestService.SimpleMethodWithGenericsInput): TestService.SimpleMethodWithGenericsInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodWithGenericsInput(a = _value.a)
      }
    }
    implicit class SimpleMethodWithGenericsInputExtensions(override protected val _value: TestService.SimpleMethodWithGenericsInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleMethodWithGenericsInput]
  }
  final case class SimpleMethodWithGenericsOutput(value: List[String]) extends TestService.SimpleMethodWithGenericsOutput.Defn
  trait SimpleMethodWithGenericsOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSimpleMethodWithGenericsOutput: Encoder[SimpleMethodWithGenericsOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSimpleMethodWithGenericsOutput: Decoder[SimpleMethodWithGenericsOutput] = Decoder.instance {
      v => v.as[List[String]].map(d => SimpleMethodWithGenericsOutput(d))
    }
  }
  object SimpleMethodWithGenericsOutput extends TestService.SimpleMethodWithGenericsOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: List[String] }
    def apply(value: List[String]): TestService.SimpleMethodWithGenericsOutput = {
      new TestService.SimpleMethodWithGenericsOutput(value = value)
    }
    def apply(defn: TestService.SimpleMethodWithGenericsOutput.Defn): TestService.SimpleMethodWithGenericsOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleMethodWithGenericsOutput(value = defn.value)
    }
    implicit object SimpleMethodWithGenericsOutput_upcast_SimpleMethodWithGenericsOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleMethodWithGenericsOutput, TestService.SimpleMethodWithGenericsOutput] {
      override def convert(_value: TestService.SimpleMethodWithGenericsOutput): TestService.SimpleMethodWithGenericsOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodWithGenericsOutput(value = _value.value)
      }
    }
    implicit class SimpleMethodWithGenericsOutputExtensions(override protected val _value: TestService.SimpleMethodWithGenericsOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleMethodWithGenericsOutput]
  }
  final case class SimpleInput(firstName: String, secondName: String) extends TestService.SimpleInput.Defn
  trait SimpleInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleInput: Encoder.AsObject[SimpleInput] = deriveEncoder[SimpleInput]
    implicit val decodeSimpleInput: Decoder[SimpleInput] = deriveDecoder[SimpleInput]
  }
  object SimpleInput extends TestService.SimpleInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(request: Request): TestService.SimpleInput = {
      assert(request.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleInput(firstName = request.firstName, secondName = request.secondName)
    }
    def apply(defn: TestService.SimpleInput.Defn): TestService.SimpleInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object SimpleInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.SimpleInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.SimpleInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.SimpleInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.SimpleInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.SimpleInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.SimpleInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.SimpleInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, Request.Struct] {
      override def convert(_value: TestService.SimpleInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_upcast_SimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, TestService.SimpleInput] {
      override def convert(_value: TestService.SimpleInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleInput_upcast_Request extends izumi.idealingua.runtime.IRTCast[TestService.SimpleInput, Request] {
      override def convert(_value: TestService.SimpleInput): Request = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class SimpleInputExtensions(override protected val _value: TestService.SimpleInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleInput]
  }
  final case class SimpleOutput(firstName: String, secondName: String) extends TestService.SimpleOutput.Defn
  trait SimpleOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleOutput: Encoder.AsObject[SimpleOutput] = deriveEncoder[SimpleOutput]
    implicit val decodeSimpleOutput: Decoder[SimpleOutput] = deriveDecoder[SimpleOutput]
  }
  object SimpleOutput extends TestService.SimpleOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(request: Request): TestService.SimpleOutput = {
      assert(request.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleOutput(firstName = request.firstName, secondName = request.secondName)
    }
    def apply(defn: TestService.SimpleOutput.Defn): TestService.SimpleOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleOutput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object SimpleOutput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.SimpleInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.AlternativeInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.SimpleOutput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, Request.Struct] {
      override def convert(_value: TestService.SimpleOutput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_upcast_SimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, TestService.SimpleOutput] {
      override def convert(_value: TestService.SimpleOutput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object SimpleOutput_upcast_Request extends izumi.idealingua.runtime.IRTCast[TestService.SimpleOutput, Request] {
      override def convert(_value: TestService.SimpleOutput): Request = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class SimpleOutputExtensions(override protected val _value: TestService.SimpleOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleOutput]
  }
  final case class SimpleEnumInput(v: TestServiceEnum) extends AnyVal with TestService.SimpleEnumInput.Defn
  trait SimpleEnumInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleEnumInput: Encoder.AsObject[SimpleEnumInput] = Encoder.forProduct1[SimpleEnumInput, TestServiceEnum]("v")((v: SimpleEnumInput) => v.v)
    implicit val decodeSimpleEnumInput: Decoder[SimpleEnumInput] = Decoder.forProduct1[SimpleEnumInput, TestServiceEnum]("v")((d: TestServiceEnum) => new SimpleEnumInput(d))
  }
  object SimpleEnumInput extends TestService.SimpleEnumInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def v: TestServiceEnum }
    def apply(v: TestServiceEnum): TestService.SimpleEnumInput = {
      assert(v.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleEnumInput(v = v)
    }
    def apply(defn: TestService.SimpleEnumInput.Defn): TestService.SimpleEnumInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleEnumInput(v = defn.v)
    }
    implicit object SimpleEnumInput_upcast_SimpleEnumInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnumInput, TestService.SimpleEnumInput] {
      override def convert(_value: TestService.SimpleEnumInput): TestService.SimpleEnumInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnumInput(v = _value.v)
      }
    }
    implicit class SimpleEnumInputExtensions(override protected val _value: TestService.SimpleEnumInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleEnumInput]
  }
  final case class SimpleEnumOutput(value: String) extends AnyVal with TestService.SimpleEnumOutput.Defn
  trait SimpleEnumOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSimpleEnumOutput: Encoder[SimpleEnumOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSimpleEnumOutput: Decoder[SimpleEnumOutput] = Decoder.instance {
      v => v.as[String].map(d => SimpleEnumOutput(d))
    }
  }
  object SimpleEnumOutput extends TestService.SimpleEnumOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestService.SimpleEnumOutput = {
      new TestService.SimpleEnumOutput(value = value)
    }
    def apply(defn: TestService.SimpleEnumOutput.Defn): TestService.SimpleEnumOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleEnumOutput(value = defn.value)
    }
    implicit object SimpleEnumOutput_cast_into_TestServiceParameterlessOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnumOutput, TestService.ParameterlessOutput] {
      override def convert(_value: TestService.SimpleEnumOutput): TestService.ParameterlessOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessOutput(value = _value.value)
      }
    }
    implicit object SimpleEnumOutput_cast_into_TestServiceSimpleMethodOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnumOutput, TestService.SimpleMethodOutput] {
      override def convert(_value: TestService.SimpleEnumOutput): TestService.SimpleMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodOutput(value = _value.value)
      }
    }
    implicit object SimpleEnumOutput_cast_into_TestServiceSimpleEnum2Output extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnumOutput, TestService.SimpleEnum2Output] {
      override def convert(_value: TestService.SimpleEnumOutput): TestService.SimpleEnum2Output = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Output(value = _value.value)
      }
    }
    implicit object SimpleEnumOutput_cast_into_TestServiceGreetSingularOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnumOutput, TestService.GreetSingularOutOutput] {
      override def convert(_value: TestService.SimpleEnumOutput): TestService.GreetSingularOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutOutput(value = _value.value)
      }
    }
    implicit object SimpleEnumOutput_upcast_SimpleEnumOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnumOutput, TestService.SimpleEnumOutput] {
      override def convert(_value: TestService.SimpleEnumOutput): TestService.SimpleEnumOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnumOutput(value = _value.value)
      }
    }
    implicit class SimpleEnumOutputExtensions(override protected val _value: TestService.SimpleEnumOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleEnumOutput]
  }
  final case class SimpleEnum2Input(e: Environment) extends AnyVal with TestService.SimpleEnum2Input.Defn
  trait SimpleEnum2InputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleEnum2Input: Encoder.AsObject[SimpleEnum2Input] = Encoder.forProduct1[SimpleEnum2Input, Environment]("e")((v: SimpleEnum2Input) => v.e)
    implicit val decodeSimpleEnum2Input: Decoder[SimpleEnum2Input] = Decoder.forProduct1[SimpleEnum2Input, Environment]("e")((d: Environment) => new SimpleEnum2Input(d))
  }
  object SimpleEnum2Input extends TestService.SimpleEnum2InputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def e: Environment }
    def apply(e: Environment): TestService.SimpleEnum2Input = {
      assert(e.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleEnum2Input(e = e)
    }
    def apply(defn: TestService.SimpleEnum2Input.Defn): TestService.SimpleEnum2Input = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleEnum2Input(e = defn.e)
    }
    implicit object SimpleEnum2Input_cast_into_TestServiceReturnsListInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Input, TestService.ReturnsListInput] {
      override def convert(_value: TestService.SimpleEnum2Input): TestService.ReturnsListInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsListInput(e = _value.e)
      }
    }
    implicit object SimpleEnum2Input_cast_into_TestServiceReturnsMapInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Input, TestService.ReturnsMapInput] {
      override def convert(_value: TestService.SimpleEnum2Input): TestService.ReturnsMapInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsMapInput(e = _value.e)
      }
    }
    implicit object SimpleEnum2Input_upcast_SimpleEnum2Input extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Input, TestService.SimpleEnum2Input] {
      override def convert(_value: TestService.SimpleEnum2Input): TestService.SimpleEnum2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Input(e = _value.e)
      }
    }
    implicit class SimpleEnum2InputExtensions(override protected val _value: TestService.SimpleEnum2Input) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleEnum2Input]
  }
  final case class SimpleEnum2Output(value: String) extends AnyVal with TestService.SimpleEnum2Output.Defn
  trait SimpleEnum2OutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSimpleEnum2Output: Encoder[SimpleEnum2Output] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSimpleEnum2Output: Decoder[SimpleEnum2Output] = Decoder.instance {
      v => v.as[String].map(d => SimpleEnum2Output(d))
    }
  }
  object SimpleEnum2Output extends TestService.SimpleEnum2OutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestService.SimpleEnum2Output = {
      new TestService.SimpleEnum2Output(value = value)
    }
    def apply(defn: TestService.SimpleEnum2Output.Defn): TestService.SimpleEnum2Output = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleEnum2Output(value = defn.value)
    }
    implicit object SimpleEnum2Output_cast_into_TestServiceParameterlessOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Output, TestService.ParameterlessOutput] {
      override def convert(_value: TestService.SimpleEnum2Output): TestService.ParameterlessOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessOutput(value = _value.value)
      }
    }
    implicit object SimpleEnum2Output_cast_into_TestServiceSimpleMethodOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Output, TestService.SimpleMethodOutput] {
      override def convert(_value: TestService.SimpleEnum2Output): TestService.SimpleMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodOutput(value = _value.value)
      }
    }
    implicit object SimpleEnum2Output_cast_into_TestServiceSimpleEnumOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Output, TestService.SimpleEnumOutput] {
      override def convert(_value: TestService.SimpleEnum2Output): TestService.SimpleEnumOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnumOutput(value = _value.value)
      }
    }
    implicit object SimpleEnum2Output_cast_into_TestServiceGreetSingularOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Output, TestService.GreetSingularOutOutput] {
      override def convert(_value: TestService.SimpleEnum2Output): TestService.GreetSingularOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutOutput(value = _value.value)
      }
    }
    implicit object SimpleEnum2Output_upcast_SimpleEnum2Output extends izumi.idealingua.runtime.IRTCast[TestService.SimpleEnum2Output, TestService.SimpleEnum2Output] {
      override def convert(_value: TestService.SimpleEnum2Output): TestService.SimpleEnum2Output = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Output(value = _value.value)
      }
    }
    implicit class SimpleEnum2OutputExtensions(override protected val _value: TestService.SimpleEnum2Output) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleEnum2Output]
  }
  final case class ReturnsListInput(e: Environment) extends AnyVal with TestService.ReturnsListInput.Defn
  trait ReturnsListInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeReturnsListInput: Encoder.AsObject[ReturnsListInput] = Encoder.forProduct1[ReturnsListInput, Environment]("e")((v: ReturnsListInput) => v.e)
    implicit val decodeReturnsListInput: Decoder[ReturnsListInput] = Decoder.forProduct1[ReturnsListInput, Environment]("e")((d: Environment) => new ReturnsListInput(d))
  }
  object ReturnsListInput extends TestService.ReturnsListInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def e: Environment }
    def apply(e: Environment): TestService.ReturnsListInput = {
      assert(e.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ReturnsListInput(e = e)
    }
    def apply(defn: TestService.ReturnsListInput.Defn): TestService.ReturnsListInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ReturnsListInput(e = defn.e)
    }
    implicit object ReturnsListInput_cast_into_TestServiceSimpleEnum2Input extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsListInput, TestService.SimpleEnum2Input] {
      override def convert(_value: TestService.ReturnsListInput): TestService.SimpleEnum2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Input(e = _value.e)
      }
    }
    implicit object ReturnsListInput_cast_into_TestServiceReturnsMapInput extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsListInput, TestService.ReturnsMapInput] {
      override def convert(_value: TestService.ReturnsListInput): TestService.ReturnsMapInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsMapInput(e = _value.e)
      }
    }
    implicit object ReturnsListInput_upcast_ReturnsListInput extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsListInput, TestService.ReturnsListInput] {
      override def convert(_value: TestService.ReturnsListInput): TestService.ReturnsListInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsListInput(e = _value.e)
      }
    }
    implicit class ReturnsListInputExtensions(override protected val _value: TestService.ReturnsListInput) extends izumi.idealingua.runtime.IRTConversions[TestService.ReturnsListInput]
  }
  final case class ReturnsListOutput(value: List[Package]) extends TestService.ReturnsListOutput.Defn
  trait ReturnsListOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedReturnsListOutput: Encoder[ReturnsListOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedReturnsListOutput: Decoder[ReturnsListOutput] = Decoder.instance {
      v => v.as[List[Package]].map(d => ReturnsListOutput(d))
    }
  }
  object ReturnsListOutput extends TestService.ReturnsListOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: List[Package] }
    def apply(value: List[Package]): TestService.ReturnsListOutput = {
      new TestService.ReturnsListOutput(value = value)
    }
    def apply(defn: TestService.ReturnsListOutput.Defn): TestService.ReturnsListOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ReturnsListOutput(value = defn.value)
    }
    implicit object ReturnsListOutput_upcast_ReturnsListOutput extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsListOutput, TestService.ReturnsListOutput] {
      override def convert(_value: TestService.ReturnsListOutput): TestService.ReturnsListOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsListOutput(value = _value.value)
      }
    }
    implicit class ReturnsListOutputExtensions(override protected val _value: TestService.ReturnsListOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.ReturnsListOutput]
  }
  final case class ReturnsMapInput(e: Environment) extends AnyVal with TestService.ReturnsMapInput.Defn
  trait ReturnsMapInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeReturnsMapInput: Encoder.AsObject[ReturnsMapInput] = Encoder.forProduct1[ReturnsMapInput, Environment]("e")((v: ReturnsMapInput) => v.e)
    implicit val decodeReturnsMapInput: Decoder[ReturnsMapInput] = Decoder.forProduct1[ReturnsMapInput, Environment]("e")((d: Environment) => new ReturnsMapInput(d))
  }
  object ReturnsMapInput extends TestService.ReturnsMapInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def e: Environment }
    def apply(e: Environment): TestService.ReturnsMapInput = {
      assert(e.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ReturnsMapInput(e = e)
    }
    def apply(defn: TestService.ReturnsMapInput.Defn): TestService.ReturnsMapInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ReturnsMapInput(e = defn.e)
    }
    implicit object ReturnsMapInput_cast_into_TestServiceSimpleEnum2Input extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsMapInput, TestService.SimpleEnum2Input] {
      override def convert(_value: TestService.ReturnsMapInput): TestService.SimpleEnum2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Input(e = _value.e)
      }
    }
    implicit object ReturnsMapInput_cast_into_TestServiceReturnsListInput extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsMapInput, TestService.ReturnsListInput] {
      override def convert(_value: TestService.ReturnsMapInput): TestService.ReturnsListInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsListInput(e = _value.e)
      }
    }
    implicit object ReturnsMapInput_upcast_ReturnsMapInput extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsMapInput, TestService.ReturnsMapInput] {
      override def convert(_value: TestService.ReturnsMapInput): TestService.ReturnsMapInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsMapInput(e = _value.e)
      }
    }
    implicit class ReturnsMapInputExtensions(override protected val _value: TestService.ReturnsMapInput) extends izumi.idealingua.runtime.IRTConversions[TestService.ReturnsMapInput]
  }
  final case class ReturnsMapOutput(value: Map[String, Package]) extends TestService.ReturnsMapOutput.Defn
  trait ReturnsMapOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedReturnsMapOutput: Encoder.AsObject[ReturnsMapOutput] = Encoder.AsObject.instance {
      v => v.value.asJsonObject
    }
    implicit val decodeUnwrappedReturnsMapOutput: Decoder[ReturnsMapOutput] = Decoder.instance {
      v => v.as[Map[String, Package]].map(d => ReturnsMapOutput(d))
    }
  }
  object ReturnsMapOutput extends TestService.ReturnsMapOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def value: Map[String, Package] }
    def apply(value: Map[String, Package]): TestService.ReturnsMapOutput = {
      new TestService.ReturnsMapOutput(value = value)
    }
    def apply(defn: TestService.ReturnsMapOutput.Defn): TestService.ReturnsMapOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.ReturnsMapOutput(value = defn.value)
    }
    implicit object ReturnsMapOutput_upcast_ReturnsMapOutput extends izumi.idealingua.runtime.IRTCast[TestService.ReturnsMapOutput, TestService.ReturnsMapOutput] {
      override def convert(_value: TestService.ReturnsMapOutput): TestService.ReturnsMapOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ReturnsMapOutput(value = _value.value)
      }
    }
    implicit class ReturnsMapOutputExtensions(override protected val _value: TestService.ReturnsMapOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.ReturnsMapOutput]
  }
  final case class SimpleGoReservedInput(`package`: Package) extends TestService.SimpleGoReservedInput.Defn
  trait SimpleGoReservedInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleGoReservedInput: Encoder.AsObject[SimpleGoReservedInput] = deriveEncoder[SimpleGoReservedInput]
    implicit val decodeSimpleGoReservedInput: Decoder[SimpleGoReservedInput] = deriveDecoder[SimpleGoReservedInput]
  }
  object SimpleGoReservedInput extends TestService.SimpleGoReservedInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def `package`: Package }
    def apply(`package`: Package.Defn): TestService.SimpleGoReservedInput = {
      assert(`package`.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleGoReservedInput(`package` = Package(`package`))
    }
    def apply(defn: TestService.SimpleGoReservedInput.Defn): TestService.SimpleGoReservedInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleGoReservedInput(`package` = defn.`package`)
    }
    implicit object SimpleGoReservedInput_cast_into_TestServiceUnitResultInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleGoReservedInput, TestService.UnitResultInput] {
      override def convert(_value: TestService.SimpleGoReservedInput): TestService.UnitResultInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultInput(`package` = _value.`package`)
      }
    }
    implicit object SimpleGoReservedInput_upcast_SimpleGoReservedInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleGoReservedInput, TestService.SimpleGoReservedInput] {
      override def convert(_value: TestService.SimpleGoReservedInput): TestService.SimpleGoReservedInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleGoReservedInput(`package` = _value.`package`)
      }
    }
    implicit class SimpleGoReservedInputExtensions(override protected val _value: TestService.SimpleGoReservedInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleGoReservedInput]
  }
  final case class SimpleGoReservedOutput(value: Boolean) extends AnyVal with TestService.SimpleGoReservedOutput.Defn
  trait SimpleGoReservedOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSimpleGoReservedOutput: Encoder[SimpleGoReservedOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSimpleGoReservedOutput: Decoder[SimpleGoReservedOutput] = Decoder.instance {
      v => v.as[Boolean].map(d => SimpleGoReservedOutput(d))
    }
  }
  object SimpleGoReservedOutput extends TestService.SimpleGoReservedOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Boolean }
    def apply(value: Boolean): TestService.SimpleGoReservedOutput = {
      new TestService.SimpleGoReservedOutput(value = value)
    }
    def apply(defn: TestService.SimpleGoReservedOutput.Defn): TestService.SimpleGoReservedOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleGoReservedOutput(value = defn.value)
    }
    implicit object SimpleGoReservedOutput_upcast_SimpleGoReservedOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleGoReservedOutput, TestService.SimpleGoReservedOutput] {
      override def convert(_value: TestService.SimpleGoReservedOutput): TestService.SimpleGoReservedOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleGoReservedOutput(value = _value.value)
      }
    }
    implicit class SimpleGoReservedOutputExtensions(override protected val _value: TestService.SimpleGoReservedOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleGoReservedOutput]
  }
  final case class SimpleVoidInput(a: String) extends AnyVal with TestService.SimpleVoidInput.Defn
  trait SimpleVoidInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleVoidInput: Encoder.AsObject[SimpleVoidInput] = Encoder.forProduct1[SimpleVoidInput, String]("a")((v: SimpleVoidInput) => v.a)
    implicit val decodeSimpleVoidInput: Decoder[SimpleVoidInput] = Decoder.forProduct1[SimpleVoidInput, String]("a")((d: String) => new SimpleVoidInput(d))
  }
  object SimpleVoidInput extends TestService.SimpleVoidInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: String }
    def apply(a: String): TestService.SimpleVoidInput = {
      new TestService.SimpleVoidInput(a = a)
    }
    def apply(defn: TestService.SimpleVoidInput.Defn): TestService.SimpleVoidInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleVoidInput(a = defn.a)
    }
    implicit object SimpleVoidInput_cast_into_TestServiceSimpleMethodInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidInput, TestService.SimpleMethodInput] {
      override def convert(_value: TestService.SimpleVoidInput): TestService.SimpleMethodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodInput(a = _value.a)
      }
    }
    implicit object SimpleVoidInput_cast_into_TestServiceGreetImplicitStructOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidInput, TestService.GreetImplicitStructOutOutput] {
      override def convert(_value: TestService.SimpleVoidInput): TestService.GreetImplicitStructOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutOutput(a = _value.a)
      }
    }
    implicit object SimpleVoidInput_upcast_SimpleVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidInput, TestService.SimpleVoidInput] {
      override def convert(_value: TestService.SimpleVoidInput): TestService.SimpleVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidInput(a = _value.a)
      }
    }
    implicit class SimpleVoidInputExtensions(override protected val _value: TestService.SimpleVoidInput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleVoidInput]
  }
  final case class SimpleVoidOutput() extends TestService.SimpleVoidOutput.Defn
  trait SimpleVoidOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSimpleVoidOutput: Encoder.AsObject[SimpleVoidOutput] = deriveEncoder[SimpleVoidOutput]
    implicit val decodeSimpleVoidOutput: Decoder[SimpleVoidOutput] = deriveDecoder[SimpleVoidOutput]
  }
  object SimpleVoidOutput extends TestService.SimpleVoidOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.SimpleVoidOutput.Defn): TestService.SimpleVoidOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.SimpleVoidOutput()
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object SimpleVoidOutput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object SimpleVoidOutput_upcast_SimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.SimpleVoidOutput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.SimpleVoidOutput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit class SimpleVoidOutputExtensions(override protected val _value: TestService.SimpleVoidOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.SimpleVoidOutput]
  }
  final case class GreetSingularOutInput(firstName: String, secondName: String) extends TestService.GreetSingularOutInput.Defn
  trait GreetSingularOutInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetSingularOutInput: Encoder.AsObject[GreetSingularOutInput] = deriveEncoder[GreetSingularOutInput]
    implicit val decodeGreetSingularOutInput: Decoder[GreetSingularOutInput] = deriveDecoder[GreetSingularOutInput]
  }
  object GreetSingularOutInput extends TestService.GreetSingularOutInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestService.GreetSingularOutInput = {
      new TestService.GreetSingularOutInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestService.GreetSingularOutInput.Defn): TestService.GreetSingularOutInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetSingularOutInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.SimpleInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, Request.Struct] {
      override def convert(_value: TestService.GreetSingularOutInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetSingularOutInput_upcast_GreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.GreetSingularOutInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class GreetSingularOutInputExtensions(override protected val _value: TestService.GreetSingularOutInput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetSingularOutInput]
  }
  final case class GreetSingularOutOutput(value: String) extends AnyVal with TestService.GreetSingularOutOutput.Defn
  trait GreetSingularOutOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedGreetSingularOutOutput: Encoder[GreetSingularOutOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedGreetSingularOutOutput: Decoder[GreetSingularOutOutput] = Decoder.instance {
      v => v.as[String].map(d => GreetSingularOutOutput(d))
    }
  }
  object GreetSingularOutOutput extends TestService.GreetSingularOutOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }
    def apply(value: String): TestService.GreetSingularOutOutput = {
      new TestService.GreetSingularOutOutput(value = value)
    }
    def apply(defn: TestService.GreetSingularOutOutput.Defn): TestService.GreetSingularOutOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetSingularOutOutput(value = defn.value)
    }
    implicit object GreetSingularOutOutput_cast_into_TestServiceParameterlessOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutOutput, TestService.ParameterlessOutput] {
      override def convert(_value: TestService.GreetSingularOutOutput): TestService.ParameterlessOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessOutput(value = _value.value)
      }
    }
    implicit object GreetSingularOutOutput_cast_into_TestServiceSimpleMethodOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutOutput, TestService.SimpleMethodOutput] {
      override def convert(_value: TestService.GreetSingularOutOutput): TestService.SimpleMethodOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodOutput(value = _value.value)
      }
    }
    implicit object GreetSingularOutOutput_cast_into_TestServiceSimpleEnumOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutOutput, TestService.SimpleEnumOutput] {
      override def convert(_value: TestService.GreetSingularOutOutput): TestService.SimpleEnumOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnumOutput(value = _value.value)
      }
    }
    implicit object GreetSingularOutOutput_cast_into_TestServiceSimpleEnum2Output extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutOutput, TestService.SimpleEnum2Output] {
      override def convert(_value: TestService.GreetSingularOutOutput): TestService.SimpleEnum2Output = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleEnum2Output(value = _value.value)
      }
    }
    implicit object GreetSingularOutOutput_upcast_GreetSingularOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetSingularOutOutput, TestService.GreetSingularOutOutput] {
      override def convert(_value: TestService.GreetSingularOutOutput): TestService.GreetSingularOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutOutput(value = _value.value)
      }
    }
    implicit class GreetSingularOutOutputExtensions(override protected val _value: TestService.GreetSingularOutOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetSingularOutOutput]
  }
  final case class GreetImplicitStructOutInput(firstName: String, secondName: String) extends TestService.GreetImplicitStructOutInput.Defn
  trait GreetImplicitStructOutInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetImplicitStructOutInput: Encoder.AsObject[GreetImplicitStructOutInput] = deriveEncoder[GreetImplicitStructOutInput]
    implicit val decodeGreetImplicitStructOutInput: Decoder[GreetImplicitStructOutInput] = deriveDecoder[GreetImplicitStructOutInput]
  }
  object GreetImplicitStructOutInput extends TestService.GreetImplicitStructOutInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestService.GreetImplicitStructOutInput = {
      new TestService.GreetImplicitStructOutInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestService.GreetImplicitStructOutInput.Defn): TestService.GreetImplicitStructOutInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructOutInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.SimpleInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, Request.Struct] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetImplicitStructOutInput_upcast_GreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.GreetImplicitStructOutInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class GreetImplicitStructOutInputExtensions(override protected val _value: TestService.GreetImplicitStructOutInput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetImplicitStructOutInput]
  }
  final case class GreetImplicitStructOutOutput(a: String) extends AnyVal with TestService.GreetImplicitStructOutOutput.Defn
  trait GreetImplicitStructOutOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetImplicitStructOutOutput: Encoder.AsObject[GreetImplicitStructOutOutput] = Encoder.forProduct1[GreetImplicitStructOutOutput, String]("a")((v: GreetImplicitStructOutOutput) => v.a)
    implicit val decodeGreetImplicitStructOutOutput: Decoder[GreetImplicitStructOutOutput] = Decoder.forProduct1[GreetImplicitStructOutOutput, String]("a")((d: String) => new GreetImplicitStructOutOutput(d))
  }
  object GreetImplicitStructOutOutput extends TestService.GreetImplicitStructOutOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: String }
    def apply(a: String): TestService.GreetImplicitStructOutOutput = {
      new TestService.GreetImplicitStructOutOutput(a = a)
    }
    def apply(defn: TestService.GreetImplicitStructOutOutput.Defn): TestService.GreetImplicitStructOutOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructOutOutput(a = defn.a)
    }
    implicit object GreetImplicitStructOutOutput_cast_into_TestServiceSimpleMethodInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutOutput, TestService.SimpleMethodInput] {
      override def convert(_value: TestService.GreetImplicitStructOutOutput): TestService.SimpleMethodInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleMethodInput(a = _value.a)
      }
    }
    implicit object GreetImplicitStructOutOutput_cast_into_TestServiceSimpleVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutOutput, TestService.SimpleVoidInput] {
      override def convert(_value: TestService.GreetImplicitStructOutOutput): TestService.SimpleVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidInput(a = _value.a)
      }
    }
    implicit object GreetImplicitStructOutOutput_upcast_GreetImplicitStructOutOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructOutOutput, TestService.GreetImplicitStructOutOutput] {
      override def convert(_value: TestService.GreetImplicitStructOutOutput): TestService.GreetImplicitStructOutOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutOutput(a = _value.a)
      }
    }
    implicit class GreetImplicitStructOutOutputExtensions(override protected val _value: TestService.GreetImplicitStructOutOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetImplicitStructOutOutput]
  }
  final case class GreetImplicitStructMultilineSyntaxInput(region: String, age: Byte) extends TestService.GreetImplicitStructMultilineSyntaxInput.Defn
  trait GreetImplicitStructMultilineSyntaxInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetImplicitStructMultilineSyntaxInput: Encoder.AsObject[GreetImplicitStructMultilineSyntaxInput] = deriveEncoder[GreetImplicitStructMultilineSyntaxInput]
    implicit val decodeGreetImplicitStructMultilineSyntaxInput: Decoder[GreetImplicitStructMultilineSyntaxInput] = deriveDecoder[GreetImplicitStructMultilineSyntaxInput]
  }
  object GreetImplicitStructMultilineSyntaxInput extends TestService.GreetImplicitStructMultilineSyntaxInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def region: String
      def age: Byte
    }
    def apply(region: String, age: Byte): TestService.GreetImplicitStructMultilineSyntaxInput = {
      new TestService.GreetImplicitStructMultilineSyntaxInput(region = region, age = age)
    }
    def apply(defn: TestService.GreetImplicitStructMultilineSyntaxInput.Defn): TestService.GreetImplicitStructMultilineSyntaxInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructMultilineSyntaxInput(region = defn.region, age = defn.age)
    }
    implicit object GreetImplicitStructMultilineSyntaxInput_cast_into_TestServiceGreetImplicitStructureMultilineCurlyBracesSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructMultilineSyntaxInput, TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput] {
      override def convert(_value: TestService.GreetImplicitStructMultilineSyntaxInput): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput(region = _value.region, age = _value.age)
      }
    }
    implicit object GreetImplicitStructMultilineSyntaxInput_upcast_GreetImplicitStructMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructMultilineSyntaxInput, TestService.GreetImplicitStructMultilineSyntaxInput] {
      override def convert(_value: TestService.GreetImplicitStructMultilineSyntaxInput): TestService.GreetImplicitStructMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructMultilineSyntaxInput(region = _value.region, age = _value.age)
      }
    }
    implicit class GreetImplicitStructMultilineSyntaxInputExtensions(override protected val _value: TestService.GreetImplicitStructMultilineSyntaxInput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetImplicitStructMultilineSyntaxInput]
  }
  final case class GreetImplicitStructMultilineSyntaxOutput(greeting: String, bullshit: String) extends TestService.GreetImplicitStructMultilineSyntaxOutput.Defn
  trait GreetImplicitStructMultilineSyntaxOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetImplicitStructMultilineSyntaxOutput: Encoder.AsObject[GreetImplicitStructMultilineSyntaxOutput] = deriveEncoder[GreetImplicitStructMultilineSyntaxOutput]
    implicit val decodeGreetImplicitStructMultilineSyntaxOutput: Decoder[GreetImplicitStructMultilineSyntaxOutput] = deriveDecoder[GreetImplicitStructMultilineSyntaxOutput]
  }
  object GreetImplicitStructMultilineSyntaxOutput extends TestService.GreetImplicitStructMultilineSyntaxOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def greeting: String
      def bullshit: String
    }
    def apply(successdata: SuccessData, bullshit: String): TestService.GreetImplicitStructMultilineSyntaxOutput = {
      assert(successdata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructMultilineSyntaxOutput(greeting = successdata.greeting, bullshit = bullshit)
    }
    def apply(defn: TestService.GreetImplicitStructMultilineSyntaxOutput.Defn): TestService.GreetImplicitStructMultilineSyntaxOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructMultilineSyntaxOutput(greeting = defn.greeting, bullshit = defn.bullshit)
    }
    implicit object GreetImplicitStructMultilineSyntaxOutput_cast_into_TestServiceGreetImplicitStructureMultilineCurlyBracesSyntaxOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructMultilineSyntaxOutput, TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput] {
      override def convert(_value: TestService.GreetImplicitStructMultilineSyntaxOutput): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput(greeting = _value.greeting, bullshit = _value.bullshit)
      }
    }
    implicit object GreetImplicitStructMultilineSyntaxOutput_upcast_GreetImplicitStructMultilineSyntaxOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructMultilineSyntaxOutput, TestService.GreetImplicitStructMultilineSyntaxOutput] {
      override def convert(_value: TestService.GreetImplicitStructMultilineSyntaxOutput): TestService.GreetImplicitStructMultilineSyntaxOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructMultilineSyntaxOutput(greeting = _value.greeting, bullshit = _value.bullshit)
      }
    }
    implicit object GreetImplicitStructMultilineSyntaxOutput_upcast_SuccessData extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructMultilineSyntaxOutput, SuccessData] {
      override def convert(_value: TestService.GreetImplicitStructMultilineSyntaxOutput): SuccessData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessData.Struct(greeting = _value.greeting)
      }
    }
    implicit class GreetImplicitStructMultilineSyntaxOutputExtensions(override protected val _value: TestService.GreetImplicitStructMultilineSyntaxOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetImplicitStructMultilineSyntaxOutput]
  }
  final case class GreetImplicitStructureMultilineCurlyBracesSyntaxInput(region: String, age: Byte) extends TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput.Defn
  trait GreetImplicitStructureMultilineCurlyBracesSyntaxInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetImplicitStructureMultilineCurlyBracesSyntaxInput: Encoder.AsObject[GreetImplicitStructureMultilineCurlyBracesSyntaxInput] = deriveEncoder[GreetImplicitStructureMultilineCurlyBracesSyntaxInput]
    implicit val decodeGreetImplicitStructureMultilineCurlyBracesSyntaxInput: Decoder[GreetImplicitStructureMultilineCurlyBracesSyntaxInput] = deriveDecoder[GreetImplicitStructureMultilineCurlyBracesSyntaxInput]
  }
  object GreetImplicitStructureMultilineCurlyBracesSyntaxInput extends TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def region: String
      def age: Byte
    }
    def apply(region: String, age: Byte): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput = {
      new TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput(region = region, age = age)
    }
    def apply(defn: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput.Defn): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput(region = defn.region, age = defn.age)
    }
    implicit object GreetImplicitStructureMultilineCurlyBracesSyntaxInput_cast_into_TestServiceGreetImplicitStructMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput, TestService.GreetImplicitStructMultilineSyntaxInput] {
      override def convert(_value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput): TestService.GreetImplicitStructMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructMultilineSyntaxInput(region = _value.region, age = _value.age)
      }
    }
    implicit object GreetImplicitStructureMultilineCurlyBracesSyntaxInput_upcast_GreetImplicitStructureMultilineCurlyBracesSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput, TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput] {
      override def convert(_value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput(region = _value.region, age = _value.age)
      }
    }
    implicit class GreetImplicitStructureMultilineCurlyBracesSyntaxInputExtensions(override protected val _value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxInput]
  }
  final case class GreetImplicitStructureMultilineCurlyBracesSyntaxOutput(greeting: String, bullshit: String) extends TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput.Defn
  trait GreetImplicitStructureMultilineCurlyBracesSyntaxOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetImplicitStructureMultilineCurlyBracesSyntaxOutput: Encoder.AsObject[GreetImplicitStructureMultilineCurlyBracesSyntaxOutput] = deriveEncoder[GreetImplicitStructureMultilineCurlyBracesSyntaxOutput]
    implicit val decodeGreetImplicitStructureMultilineCurlyBracesSyntaxOutput: Decoder[GreetImplicitStructureMultilineCurlyBracesSyntaxOutput] = deriveDecoder[GreetImplicitStructureMultilineCurlyBracesSyntaxOutput]
  }
  object GreetImplicitStructureMultilineCurlyBracesSyntaxOutput extends TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def greeting: String
      def bullshit: String
    }
    def apply(successdata: SuccessData, bullshit: String): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput = {
      assert(successdata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput(greeting = successdata.greeting, bullshit = bullshit)
    }
    def apply(defn: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput.Defn): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput(greeting = defn.greeting, bullshit = defn.bullshit)
    }
    implicit object GreetImplicitStructureMultilineCurlyBracesSyntaxOutput_cast_into_TestServiceGreetImplicitStructMultilineSyntaxOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput, TestService.GreetImplicitStructMultilineSyntaxOutput] {
      override def convert(_value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput): TestService.GreetImplicitStructMultilineSyntaxOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructMultilineSyntaxOutput(greeting = _value.greeting, bullshit = _value.bullshit)
      }
    }
    implicit object GreetImplicitStructureMultilineCurlyBracesSyntaxOutput_upcast_GreetImplicitStructureMultilineCurlyBracesSyntaxOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput, TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput] {
      override def convert(_value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput): TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput(greeting = _value.greeting, bullshit = _value.bullshit)
      }
    }
    implicit object GreetImplicitStructureMultilineCurlyBracesSyntaxOutput_upcast_SuccessData extends izumi.idealingua.runtime.IRTCast[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput, SuccessData] {
      override def convert(_value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput): SuccessData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SuccessData.Struct(greeting = _value.greeting)
      }
    }
    implicit class GreetImplicitStructureMultilineCurlyBracesSyntaxOutputExtensions(override protected val _value: TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetImplicitStructureMultilineCurlyBracesSyntaxOutput]
  }
  final case class GreetAlgebraicOutInput(firstName: String, secondName: String) extends TestService.GreetAlgebraicOutInput.Defn
  trait GreetAlgebraicOutInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetAlgebraicOutInput: Encoder.AsObject[GreetAlgebraicOutInput] = deriveEncoder[GreetAlgebraicOutInput]
    implicit val decodeGreetAlgebraicOutInput: Decoder[GreetAlgebraicOutInput] = deriveDecoder[GreetAlgebraicOutInput]
  }
  object GreetAlgebraicOutInput extends TestService.GreetAlgebraicOutInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestService.GreetAlgebraicOutInput = {
      new TestService.GreetAlgebraicOutInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestService.GreetAlgebraicOutInput.Defn): TestService.GreetAlgebraicOutInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetAlgebraicOutInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.SimpleInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, Request.Struct] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicOutInput_upcast_GreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicOutInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.GreetAlgebraicOutInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class GreetAlgebraicOutInputExtensions(override protected val _value: TestService.GreetAlgebraicOutInput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetAlgebraicOutInput]
  }
  sealed trait GreetAlgebraicOutOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait GreetAlgebraicOutOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeGreetAlgebraicOutOutput: Encoder.AsObject[TestService.GreetAlgebraicOutOutput] = Encoder.AsObject.instance {
      case v: TestService.GreetAlgebraicOutOutput.SuccessDataData =>
        Map("SuccessDataData" -> v.value).asJsonObject
      case v: TestService.GreetAlgebraicOutOutput.ErrorData =>
        Map("ErrorData" -> v.value).asJsonObject
    }
    implicit val decodeGreetAlgebraicOutOutput: Decoder[TestService.GreetAlgebraicOutOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "SuccessDataData" =>
          value.as[_root_.idltest.services.SuccessDataData].map(TestService.GreetAlgebraicOutOutput.SuccessDataData.apply)
        case "ErrorData" =>
          value.as[_root_.idltest.services.ErrorData].map(TestService.GreetAlgebraicOutOutput.ErrorData.apply)
        case _ =>
          val cname = "idltest.services.TestService.GreetAlgebraicOutOutput"
          val alts = List("SuccessDataData", "ErrorData").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object GreetAlgebraicOutOutput extends TestService.GreetAlgebraicOutOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestService.GreetAlgebraicOutOutput
    final case class SuccessDataData(value: _root_.idltest.services.SuccessDataData) extends TestService.GreetAlgebraicOutOutput
    implicit def intoSuccessDataData(value: _root_.idltest.services.SuccessDataData): TestService.GreetAlgebraicOutOutput = TestService.GreetAlgebraicOutOutput.SuccessDataData(value)
    implicit def fromSuccessDataData(value: TestService.GreetAlgebraicOutOutput.SuccessDataData): _root_.idltest.services.SuccessDataData = value.value
    final case class ErrorData(value: _root_.idltest.services.ErrorData) extends TestService.GreetAlgebraicOutOutput
    implicit def intoErrorData(value: _root_.idltest.services.ErrorData): TestService.GreetAlgebraicOutOutput = TestService.GreetAlgebraicOutOutput.ErrorData(value)
    implicit def fromErrorData(value: TestService.GreetAlgebraicOutOutput.ErrorData): _root_.idltest.services.ErrorData = value.value
  }
  final case class GreetAlgebraicMultilineSyntaxInput(firstName: String, secondName: String) extends TestService.GreetAlgebraicMultilineSyntaxInput.Defn
  trait GreetAlgebraicMultilineSyntaxInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeGreetAlgebraicMultilineSyntaxInput: Encoder.AsObject[GreetAlgebraicMultilineSyntaxInput] = deriveEncoder[GreetAlgebraicMultilineSyntaxInput]
    implicit val decodeGreetAlgebraicMultilineSyntaxInput: Decoder[GreetAlgebraicMultilineSyntaxInput] = deriveDecoder[GreetAlgebraicMultilineSyntaxInput]
  }
  object GreetAlgebraicMultilineSyntaxInput extends TestService.GreetAlgebraicMultilineSyntaxInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestService.GreetAlgebraicMultilineSyntaxInput = {
      new TestService.GreetAlgebraicMultilineSyntaxInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestService.GreetAlgebraicMultilineSyntaxInput.Defn): TestService.GreetAlgebraicMultilineSyntaxInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.GreetAlgebraicMultilineSyntaxInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.SimpleInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, Request.Struct] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object GreetAlgebraicMultilineSyntaxInput_upcast_GreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.GreetAlgebraicMultilineSyntaxInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.GreetAlgebraicMultilineSyntaxInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class GreetAlgebraicMultilineSyntaxInputExtensions(override protected val _value: TestService.GreetAlgebraicMultilineSyntaxInput) extends izumi.idealingua.runtime.IRTConversions[TestService.GreetAlgebraicMultilineSyntaxInput]
  }
  sealed trait GreetAlgebraicMultilineSyntaxOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait GreetAlgebraicMultilineSyntaxOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeGreetAlgebraicMultilineSyntaxOutput: Encoder.AsObject[TestService.GreetAlgebraicMultilineSyntaxOutput] = Encoder.AsObject.instance {
      case v: TestService.GreetAlgebraicMultilineSyntaxOutput.SuccessDataData =>
        Map("SuccessDataData" -> v.value).asJsonObject
      case v: TestService.GreetAlgebraicMultilineSyntaxOutput.ErrorData =>
        Map("ErrorData" -> v.value).asJsonObject
    }
    implicit val decodeGreetAlgebraicMultilineSyntaxOutput: Decoder[TestService.GreetAlgebraicMultilineSyntaxOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "SuccessDataData" =>
          value.as[_root_.idltest.services.SuccessDataData].map(TestService.GreetAlgebraicMultilineSyntaxOutput.SuccessDataData.apply)
        case "ErrorData" =>
          value.as[_root_.idltest.services.ErrorData].map(TestService.GreetAlgebraicMultilineSyntaxOutput.ErrorData.apply)
        case _ =>
          val cname = "idltest.services.TestService.GreetAlgebraicMultilineSyntaxOutput"
          val alts = List("SuccessDataData", "ErrorData").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object GreetAlgebraicMultilineSyntaxOutput extends TestService.GreetAlgebraicMultilineSyntaxOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestService.GreetAlgebraicMultilineSyntaxOutput
    final case class SuccessDataData(value: _root_.idltest.services.SuccessDataData) extends TestService.GreetAlgebraicMultilineSyntaxOutput
    implicit def intoSuccessDataData(value: _root_.idltest.services.SuccessDataData): TestService.GreetAlgebraicMultilineSyntaxOutput = TestService.GreetAlgebraicMultilineSyntaxOutput.SuccessDataData(value)
    implicit def fromSuccessDataData(value: TestService.GreetAlgebraicMultilineSyntaxOutput.SuccessDataData): _root_.idltest.services.SuccessDataData = value.value
    final case class ErrorData(value: _root_.idltest.services.ErrorData) extends TestService.GreetAlgebraicMultilineSyntaxOutput
    implicit def intoErrorData(value: _root_.idltest.services.ErrorData): TestService.GreetAlgebraicMultilineSyntaxOutput = TestService.GreetAlgebraicMultilineSyntaxOutput.ErrorData(value)
    implicit def fromErrorData(value: TestService.GreetAlgebraicMultilineSyntaxOutput.ErrorData): _root_.idltest.services.ErrorData = value.value
  }
  final case class AlternativeInput(firstName: String, secondName: String) extends TestService.AlternativeInput.Defn
  trait AlternativeInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAlternativeInput: Encoder.AsObject[AlternativeInput] = deriveEncoder[AlternativeInput]
    implicit val decodeAlternativeInput: Decoder[AlternativeInput] = deriveDecoder[AlternativeInput]
  }
  object AlternativeInput extends TestService.AlternativeInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestService.AlternativeInput = {
      new TestService.AlternativeInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestService.AlternativeInput.Defn): TestService.AlternativeInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.AlternativeInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object AlternativeInput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.SimpleInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.AlternativeInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, Request.Struct] {
      override def convert(_value: TestService.AlternativeInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeInput_upcast_AlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.AlternativeInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class AlternativeInputExtensions(override protected val _value: TestService.AlternativeInput) extends izumi.idealingua.runtime.IRTConversions[TestService.AlternativeInput]
  }
  sealed trait AlternativeOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait AlternativeOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeAlternativeOutput: Encoder.AsObject[TestService.AlternativeOutput] = Encoder.AsObject.instance {
      case v: TestService.AlternativeOutput.Success =>
        Map("Success" -> v.value).asJsonObject
      case v: TestService.AlternativeOutput.Failure =>
        Map("Failure" -> v.value).asJsonObject
    }
    implicit val decodeAlternativeOutput: Decoder[TestService.AlternativeOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "Success" =>
          value.as[_root_.idltest.services.SuccessData].map(TestService.AlternativeOutput.Success.apply)
        case "Failure" =>
          value.as[_root_.idltest.services.ErrorData].map(TestService.AlternativeOutput.Failure.apply)
        case _ =>
          val cname = "idltest.services.TestService.AlternativeOutput"
          val alts = List("Success", "Failure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object AlternativeOutput extends TestService.AlternativeOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestService.AlternativeOutput
    final case class Success(value: _root_.idltest.services.SuccessData) extends TestService.AlternativeOutput
    implicit def intoSuccess(value: _root_.idltest.services.SuccessData): TestService.AlternativeOutput = TestService.AlternativeOutput.Success(value)
    implicit def fromSuccess(value: TestService.AlternativeOutput.Success): _root_.idltest.services.SuccessData = value.value
    final case class Failure(value: _root_.idltest.services.ErrorData) extends TestService.AlternativeOutput
    implicit def intoFailure(value: _root_.idltest.services.ErrorData): TestService.AlternativeOutput = TestService.AlternativeOutput.Failure(value)
    implicit def fromFailure(value: TestService.AlternativeOutput.Failure): _root_.idltest.services.ErrorData = value.value
  }
  final case class AlternativeSameInput(firstName: String, secondName: String) extends TestService.AlternativeSameInput.Defn
  trait AlternativeSameInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAlternativeSameInput: Encoder.AsObject[AlternativeSameInput] = deriveEncoder[AlternativeSameInput]
    implicit val decodeAlternativeSameInput: Decoder[AlternativeSameInput] = deriveDecoder[AlternativeSameInput]
  }
  object AlternativeSameInput extends TestService.AlternativeSameInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
      def firstName: String
      def secondName: String
    }
    def apply(firstName: String, secondName: String): TestService.AlternativeSameInput = {
      new TestService.AlternativeSameInput(firstName = firstName, secondName = secondName)
    }
    def apply(defn: TestService.AlternativeSameInput.Defn): TestService.AlternativeSameInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.AlternativeSameInput(firstName = defn.firstName, secondName = defn.secondName)
    }
    implicit object AlternativeSameInput_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.SimpleInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.SimpleOutput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.GreetSingularOutInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.AlternativeInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_cast_into_RequestStruct extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, Request.Struct] {
      override def convert(_value: TestService.AlternativeSameInput): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object AlternativeSameInput_upcast_AlternativeSameInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeSameInput, TestService.AlternativeSameInput] {
      override def convert(_value: TestService.AlternativeSameInput): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class AlternativeSameInputExtensions(override protected val _value: TestService.AlternativeSameInput) extends izumi.idealingua.runtime.IRTConversions[TestService.AlternativeSameInput]
  }
  sealed trait AlternativeSameOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait AlternativeSameOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeAlternativeSameOutput: Encoder.AsObject[TestService.AlternativeSameOutput] = Encoder.AsObject.instance {
      case v: TestService.AlternativeSameOutput.Success =>
        Map("Success" -> v.value).asJsonObject
      case v: TestService.AlternativeSameOutput.Failure =>
        Map("Failure" -> v.value).asJsonObject
    }
    implicit val decodeAlternativeSameOutput: Decoder[TestService.AlternativeSameOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "Success" =>
          value.as[_root_.idltest.services.SuccessData].map(TestService.AlternativeSameOutput.Success.apply)
        case "Failure" =>
          value.as[_root_.idltest.services.SuccessData].map(TestService.AlternativeSameOutput.Failure.apply)
        case _ =>
          val cname = "idltest.services.TestService.AlternativeSameOutput"
          val alts = List("Success", "Failure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object AlternativeSameOutput extends TestService.AlternativeSameOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestService.AlternativeSameOutput
    final case class Success(value: _root_.idltest.services.SuccessData) extends TestService.AlternativeSameOutput
    implicit def intoSuccess(value: _root_.idltest.services.SuccessData): TestService.AlternativeSameOutput = TestService.AlternativeSameOutput.Success(value)
    implicit def fromSuccess(value: TestService.AlternativeSameOutput.Success): _root_.idltest.services.SuccessData = value.value
    final case class Failure(value: _root_.idltest.services.SuccessData) extends TestService.AlternativeSameOutput
    implicit def intoFailure(value: _root_.idltest.services.SuccessData): TestService.AlternativeSameOutput = TestService.AlternativeSameOutput.Failure(value)
    implicit def fromFailure(value: TestService.AlternativeSameOutput.Failure): _root_.idltest.services.SuccessData = value.value
  }
  final case class AlternativeGenericInput() extends TestService.AlternativeGenericInput.Defn
  trait AlternativeGenericInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAlternativeGenericInput: Encoder.AsObject[AlternativeGenericInput] = deriveEncoder[AlternativeGenericInput]
    implicit val decodeAlternativeGenericInput: Decoder[AlternativeGenericInput] = deriveDecoder[AlternativeGenericInput]
  }
  object AlternativeGenericInput extends TestService.AlternativeGenericInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.AlternativeGenericInput.Defn): TestService.AlternativeGenericInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.AlternativeGenericInput()
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.UnitResultOutput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.ParameterlessInput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object AlternativeGenericInput_cast_into_TestServiceAlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit object AlternativeGenericInput_upcast_AlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGenericInput, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.AlternativeGenericInput): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit class AlternativeGenericInputExtensions(override protected val _value: TestService.AlternativeGenericInput) extends izumi.idealingua.runtime.IRTConversions[TestService.AlternativeGenericInput]
  }
  sealed trait AlternativeGenericOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait AlternativeGenericOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeAlternativeGenericOutput: Encoder.AsObject[TestService.AlternativeGenericOutput] = Encoder.AsObject.instance {
      case v: TestService.AlternativeGenericOutput.Success =>
        Map("Success" -> v.value).asJsonObject
      case v: TestService.AlternativeGenericOutput.Failure =>
        Map("Failure" -> v.value).asJsonObject
    }
    implicit val decodeAlternativeGenericOutput: Decoder[TestService.AlternativeGenericOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "Success" =>
          value.as[List[SuccessData]].map(TestService.AlternativeGenericOutput.Success.apply)
        case "Failure" =>
          value.as[Set[ErrorData]].map(TestService.AlternativeGenericOutput.Failure.apply)
        case _ =>
          val cname = "idltest.services.TestService.AlternativeGenericOutput"
          val alts = List("Success", "Failure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object AlternativeGenericOutput extends TestService.AlternativeGenericOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestService.AlternativeGenericOutput
    final case class Success(value: List[SuccessData]) extends TestService.AlternativeGenericOutput
    implicit def intoSuccess(value: List[SuccessData]): TestService.AlternativeGenericOutput = TestService.AlternativeGenericOutput.Success(value)
    implicit def fromSuccess(value: TestService.AlternativeGenericOutput.Success): List[SuccessData] = value.value
    final case class Failure(value: Set[ErrorData]) extends TestService.AlternativeGenericOutput
    implicit def intoFailure(value: Set[ErrorData]): TestService.AlternativeGenericOutput = TestService.AlternativeGenericOutput.Failure(value)
    implicit def fromFailure(value: TestService.AlternativeGenericOutput.Failure): Set[ErrorData] = value.value
  }
  final case class AlternativeGeneric2Input() extends TestService.AlternativeGeneric2Input.Defn
  trait AlternativeGeneric2InputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAlternativeGeneric2Input: Encoder.AsObject[AlternativeGeneric2Input] = deriveEncoder[AlternativeGeneric2Input]
    implicit val decodeAlternativeGeneric2Input: Decoder[AlternativeGeneric2Input] = deriveDecoder[AlternativeGeneric2Input]
  }
  object AlternativeGeneric2Input extends TestService.AlternativeGeneric2InputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: TestService.AlternativeGeneric2Input.Defn): TestService.AlternativeGeneric2Input = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new TestService.AlternativeGeneric2Input()
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceUnitToUnitInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.UnitToUnitInput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.UnitToUnitInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitInput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceUnitToUnitOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.UnitToUnitOutput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.UnitToUnitOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitToUnitOutput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceAnotherVoidInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.AnotherVoidInput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.AnotherVoidInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidInput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceAnotherVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.AnotherVoidOutput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.AnotherVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AnotherVoidOutput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceUnitResultOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.UnitResultOutput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.UnitResultOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.UnitResultOutput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceParameterlessInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.ParameterlessInput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.ParameterlessInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.ParameterlessInput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceSimpleVoidOutput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.SimpleVoidOutput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.SimpleVoidOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleVoidOutput()
      }
    }
    implicit object AlternativeGeneric2Input_cast_into_TestServiceAlternativeGenericInput extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.AlternativeGenericInput] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.AlternativeGenericInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGenericInput()
      }
    }
    implicit object AlternativeGeneric2Input_upcast_AlternativeGeneric2Input extends izumi.idealingua.runtime.IRTCast[TestService.AlternativeGeneric2Input, TestService.AlternativeGeneric2Input] {
      override def convert(_value: TestService.AlternativeGeneric2Input): TestService.AlternativeGeneric2Input = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeGeneric2Input()
      }
    }
    implicit class AlternativeGeneric2InputExtensions(override protected val _value: TestService.AlternativeGeneric2Input) extends izumi.idealingua.runtime.IRTConversions[TestService.AlternativeGeneric2Input]
  }
  sealed trait AlternativeGeneric2Output extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait AlternativeGeneric2OutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeAlternativeGeneric2Output: Encoder.AsObject[TestService.AlternativeGeneric2Output] = Encoder.AsObject.instance {
      case v: TestService.AlternativeGeneric2Output.Success =>
        Map("Success" -> v.value).asJsonObject
      case v: TestService.AlternativeGeneric2Output.Failure =>
        Map("Failure" -> v.value).asJsonObject
    }
    implicit val decodeAlternativeGeneric2Output: Decoder[TestService.AlternativeGeneric2Output] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "Success" =>
          value.as[Map[String, SuccessData]].map(TestService.AlternativeGeneric2Output.Success.apply)
        case "Failure" =>
          value.as[Map[String, ErrorData]].map(TestService.AlternativeGeneric2Output.Failure.apply)
        case _ =>
          val cname = "idltest.services.TestService.AlternativeGeneric2Output"
          val alts = List("Success", "Failure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object AlternativeGeneric2Output extends TestService.AlternativeGeneric2OutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = TestService.AlternativeGeneric2Output
    final case class Success(value: Map[String, SuccessData]) extends TestService.AlternativeGeneric2Output
    implicit def intoSuccess(value: Map[String, SuccessData]): TestService.AlternativeGeneric2Output = TestService.AlternativeGeneric2Output.Success(value)
    implicit def fromSuccess(value: TestService.AlternativeGeneric2Output.Success): Map[String, SuccessData] = value.value
    final case class Failure(value: Map[String, ErrorData]) extends TestService.AlternativeGeneric2Output
    implicit def intoFailure(value: Map[String, ErrorData]): TestService.AlternativeGeneric2Output = TestService.AlternativeGeneric2Output.Failure(value)
    implicit def fromFailure(value: TestService.AlternativeGeneric2Output.Failure): Map[String, ErrorData] = value.value
  }
}

object TestServiceCodecs {
  object unitToUnit extends IRTCirceMarshaller {
    import TestService.unitToUnit.*
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
  object anotherVoid extends IRTCirceMarshaller {
    import TestService.anotherVoid.*
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
  object unitResult extends IRTCirceMarshaller {
    import TestService.unitResult.*
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
  object parameterless extends IRTCirceMarshaller {
    import TestService.parameterless.*
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
  object simpleMethod extends IRTCirceMarshaller {
    import TestService.simpleMethod.*
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
  object simpleIntMethod extends IRTCirceMarshaller {
    import TestService.simpleIntMethod.*
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
  object simpleMethodWithGenerics extends IRTCirceMarshaller {
    import TestService.simpleMethodWithGenerics.*
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
  object simple extends IRTCirceMarshaller {
    import TestService.simple.*
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
  object simpleEnum extends IRTCirceMarshaller {
    import TestService.simpleEnum.*
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
  object simpleEnum2 extends IRTCirceMarshaller {
    import TestService.simpleEnum2.*
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
  object returnsList extends IRTCirceMarshaller {
    import TestService.returnsList.*
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
  object returnsMap extends IRTCirceMarshaller {
    import TestService.returnsMap.*
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
  object simpleGoReserved extends IRTCirceMarshaller {
    import TestService.simpleGoReserved.*
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
  object simpleVoid extends IRTCirceMarshaller {
    import TestService.simpleVoid.*
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
  object greetSingularOut extends IRTCirceMarshaller {
    import TestService.greetSingularOut.*
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
  object greetImplicitStructOut extends IRTCirceMarshaller {
    import TestService.greetImplicitStructOut.*
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
  object greetImplicitStructMultilineSyntax extends IRTCirceMarshaller {
    import TestService.greetImplicitStructMultilineSyntax.*
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
  object greetImplicitStructureMultilineCurlyBracesSyntax extends IRTCirceMarshaller {
    import TestService.greetImplicitStructureMultilineCurlyBracesSyntax.*
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
  object greetAlgebraicOut extends IRTCirceMarshaller {
    import TestService.greetAlgebraicOut.*
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
  object greetAlgebraicMultilineSyntax extends IRTCirceMarshaller {
    import TestService.greetAlgebraicMultilineSyntax.*
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
  object alternative extends IRTCirceMarshaller {
    import TestService.alternative.*
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
  object alternativeSame extends IRTCirceMarshaller {
    import TestService.alternativeSame.*
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
  object alternativeGeneric extends IRTCirceMarshaller {
    import TestService.alternativeGeneric.*
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
  object alternativeGeneric2 extends IRTCirceMarshaller {
    import TestService.alternativeGeneric2.*
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
       