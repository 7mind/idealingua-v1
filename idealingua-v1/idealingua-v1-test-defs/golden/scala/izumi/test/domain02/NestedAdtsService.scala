package izumi.test.domain02

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait NestedAdtsServiceServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def adtNested(ctx: C): Just[NestedAdtsService.adtNested.Output]
}

trait NestedAdtsServiceClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def adtNested(): Just[NestedAdtsService.adtNested.Output]
}

class NestedAdtsServiceWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends NestedAdtsServiceClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.izumi.test.domain02.NestedAdtsService as _M
  def adtNested(): Just[NestedAdtsService.adtNested.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.adtNested.Input()), _M.adtNested.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.adtNested.Output), method) if method == _M.adtNested.id =>
        _F.pure(v)
      case v =>
        val id = "NestedAdtsService.NestedAdtsServiceWrappedClient.adtNested"
        val expected = classOf[_M.adtNested.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object NestedAdtsServiceWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(NestedAdtsService.adtNested.id -> NestedAdtsServiceCodecs.adtNested)
  }
}

class NestedAdtsServiceWrappedServer[Or[+_, +_]: IRTIO2, C](_service: NestedAdtsServiceServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = NestedAdtsService.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](adtNested).map(m => m.signature.id -> m).toMap
  }
  object adtNested extends IRTMethodWrapper[Or, C] {
    import NestedAdtsService.adtNested.*
    val signature: NestedAdtsService.adtNested.type = NestedAdtsService.adtNested
    val marshaller: NestedAdtsServiceCodecs.adtNested.type = NestedAdtsServiceCodecs.adtNested
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.adtNested(ctx)
    }
  }
}

object NestedAdtsServiceWrappedServer

object NestedAdtsService {
  final val serviceId: IRTServiceId = IRTServiceId("NestedAdtsService")
  object adtNested extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("adtNested"))
    type Input = AdtNestedInput
    type Output = AdtNestedOutput
  }
  final case class AdtNestedInput() extends NestedAdtsService.AdtNestedInput.Defn
  trait AdtNestedInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeAdtNestedInput: Encoder.AsObject[AdtNestedInput] = deriveEncoder[AdtNestedInput]
    implicit val decodeAdtNestedInput: Decoder[AdtNestedInput] = deriveDecoder[AdtNestedInput]
  }
  object AdtNestedInput extends NestedAdtsService.AdtNestedInputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
    def apply(defn: NestedAdtsService.AdtNestedInput.Defn): NestedAdtsService.AdtNestedInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new NestedAdtsService.AdtNestedInput()
    }
    implicit object AdtNestedInput_cast_into_TestAliasServTestADTIdImportedReturnInput extends izumi.idealingua.runtime.IRTCast[NestedAdtsService.AdtNestedInput, TestAliasServ.TestADTIdImportedReturnInput] {
      override def convert(_value: NestedAdtsService.AdtNestedInput): TestAliasServ.TestADTIdImportedReturnInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.TestADTIdImportedReturnInput()
      }
    }
    implicit object AdtNestedInput_cast_into_TestAliasServTestADTIdReturnInput extends izumi.idealingua.runtime.IRTCast[NestedAdtsService.AdtNestedInput, TestAliasServ.TestADTIdReturnInput] {
      override def convert(_value: NestedAdtsService.AdtNestedInput): TestAliasServ.TestADTIdReturnInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestAliasServ.TestADTIdReturnInput()
      }
    }
    implicit object AdtNestedInput_upcast_AdtNestedInput extends izumi.idealingua.runtime.IRTCast[NestedAdtsService.AdtNestedInput, NestedAdtsService.AdtNestedInput] {
      override def convert(_value: NestedAdtsService.AdtNestedInput): NestedAdtsService.AdtNestedInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NestedAdtsService.AdtNestedInput()
      }
    }
    implicit class AdtNestedInputExtensions(override protected val _value: NestedAdtsService.AdtNestedInput) extends izumi.idealingua.runtime.IRTConversions[NestedAdtsService.AdtNestedInput]
  }
  sealed trait AdtNestedOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait AdtNestedOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeAdtNestedOutput: Encoder.AsObject[NestedAdtsService.AdtNestedOutput] = Encoder.AsObject.instance {
      case v: NestedAdtsService.AdtNestedOutput.AdtA =>
        Map("AdtA" -> v.value).asJsonObject
      case v: NestedAdtsService.AdtNestedOutput.Adt2 =>
        Map("Adt2" -> v.value).asJsonObject
    }
    implicit val decodeAdtNestedOutput: Decoder[NestedAdtsService.AdtNestedOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "AdtA" =>
          value.as[_root_.izumi.test.domain02.AdtA].map(NestedAdtsService.AdtNestedOutput.AdtA.apply)
        case "Adt2" =>
          value.as[_root_.izumi.test.domain02.Adt2].map(NestedAdtsService.AdtNestedOutput.Adt2.apply)
        case _ =>
          val cname = "izumi.test.domain02.NestedAdtsService.AdtNestedOutput"
          val alts = List("AdtA", "Adt2").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object AdtNestedOutput extends NestedAdtsService.AdtNestedOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = NestedAdtsService.AdtNestedOutput
    final case class AdtA(value: _root_.izumi.test.domain02.AdtA) extends NestedAdtsService.AdtNestedOutput
    implicit def intoAdtA(value: _root_.izumi.test.domain02.AdtA): NestedAdtsService.AdtNestedOutput = NestedAdtsService.AdtNestedOutput.AdtA(value)
    implicit def fromAdtA(value: NestedAdtsService.AdtNestedOutput.AdtA): _root_.izumi.test.domain02.AdtA = value.value
    final case class Adt2(value: _root_.izumi.test.domain02.Adt2) extends NestedAdtsService.AdtNestedOutput
    implicit def intoAdt2(value: _root_.izumi.test.domain02.Adt2): NestedAdtsService.AdtNestedOutput = NestedAdtsService.AdtNestedOutput.Adt2(value)
    implicit def fromAdt2(value: NestedAdtsService.AdtNestedOutput.Adt2): _root_.izumi.test.domain02.Adt2 = value.value
  }
}

object NestedAdtsServiceCodecs {
  object adtNested extends IRTCirceMarshaller {
    import NestedAdtsService.adtNested.*
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
       