package izumi.test.domain02

import _root_.scala.language.higherKinds
import _root_.izumi.functional.bio.{ IO2 => IRTIO2 }
import _root_.io.circe.{ Json => IRTJson }
import _root_.io.circe.{ DecodingFailure => IRTDecodingFailure }
import _root_.io.circe.syntax._
import _root_.izumi.idealingua.runtime.rpc._


trait ImportIdServiceServer[Or[+_, +_], C] {
  type Just[+T] = Or[Nothing, T]
  def some(ctx: C, id: izumi.test.domain01.ImportAppId): Just[Long]
  def mixi(ctx: C, par: izumi.test.domain01.GenericFailureData): Just[ImportIdService.mixi.Output]
  def update(ctx: C, id: izumi.test.domain01.ImportAppId): Just[ImportIdService.update.Output]
}

trait ImportIdServiceClient[Or[+_, +_]] {
  type Just[+T] = Or[Nothing, T]
  def some(id: izumi.test.domain01.ImportAppId): Just[Long]
  def mixi(par: izumi.test.domain01.GenericFailureData): Just[ImportIdService.mixi.Output]
  def update(id: izumi.test.domain01.ImportAppId): Just[ImportIdService.update.Output]
}

class ImportIdServiceWrappedClient[Or[+_, +_]: IRTIO2](_dispatcher: IRTDispatcher[Or]) extends ImportIdServiceClient[Or] {
  final val _F: IRTIO2[Or] = implicitly
  import _root_.izumi.test.domain02.ImportIdService as _M
  def some(id: izumi.test.domain01.ImportAppId): Just[Long] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.some.Input(id)), _M.some.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.some.Output), method) if method == _M.some.id =>
        _F.pure(v.value)
      case v =>
        val id = "ImportIdService.ImportIdServiceWrappedClient.some"
        val expected = classOf[_M.some.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def mixi(par: izumi.test.domain01.GenericFailureData): Just[ImportIdService.mixi.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.mixi.Input(par)), _M.mixi.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.mixi.Output), method) if method == _M.mixi.id =>
        _F.pure(v)
      case v =>
        val id = "ImportIdService.ImportIdServiceWrappedClient.mixi"
        val expected = classOf[_M.mixi.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
  def update(id: izumi.test.domain01.ImportAppId): Just[ImportIdService.update.Output] = {
    _F.redeem(_dispatcher.dispatch(IRTMuxRequest(IRTReqBody(new _M.update.Input(id)), _M.update.id)))({
      err => _F.terminate(err)
    }, {
      case IRTMuxResponse(IRTResBody(v: _M.update.Output), method) if method == _M.update.id =>
        _F.pure(v)
      case v =>
        val id = "ImportIdService.ImportIdServiceWrappedClient.update"
        val expected = classOf[_M.update.Input].toString
        _F.terminate(new IRTTypeMismatchException(s"Unexpected type in $id: $v, expected $expected got ${
          v.getClass
        }", v, None))
    })
  }
}

object ImportIdServiceWrappedClient extends IRTWrappedClient {
  val allCodecs: Map[IRTMethodId, IRTCirceMarshaller] = {
    Map(ImportIdService.some.id -> ImportIdServiceCodecs.some, ImportIdService.mixi.id -> ImportIdServiceCodecs.mixi, ImportIdService.update.id -> ImportIdServiceCodecs.update)
  }
}

class ImportIdServiceWrappedServer[Or[+_, +_]: IRTIO2, C](_service: ImportIdServiceServer[Or, C]) extends IRTWrappedService[Or, C] {
  final val _F: IRTIO2[Or] = implicitly
  final val serviceId: IRTServiceId = ImportIdService.serviceId
  val allMethods: Map[IRTMethodId, IRTMethodWrapper[Or, C]] = {
    Seq[IRTMethodWrapper[Or, C]](some, mixi, update).map(m => m.signature.id -> m).toMap
  }
  object some extends IRTMethodWrapper[Or, C] {
    import ImportIdService.some.*
    val signature: ImportIdService.some.type = ImportIdService.some
    val marshaller: ImportIdServiceCodecs.some.type = ImportIdServiceCodecs.some
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _F.map(_service.some(ctx, input.id))(v => new Output(v))
    }
  }
  object mixi extends IRTMethodWrapper[Or, C] {
    import ImportIdService.mixi.*
    val signature: ImportIdService.mixi.type = ImportIdService.mixi
    val marshaller: ImportIdServiceCodecs.mixi.type = ImportIdServiceCodecs.mixi
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.mixi(ctx, input.par)
    }
  }
  object update extends IRTMethodWrapper[Or, C] {
    import ImportIdService.update.*
    val signature: ImportIdService.update.type = ImportIdService.update
    val marshaller: ImportIdServiceCodecs.update.type = ImportIdServiceCodecs.update
    def invoke(ctx: C, input: Input): Just[Output] = {
      assert(ctx.asInstanceOf[_root_.scala.AnyRef] != null && input.asInstanceOf[_root_.scala.AnyRef] != null)
      _service.update(ctx, input.id)
    }
  }
}

object ImportIdServiceWrappedServer

object ImportIdService {
  final val serviceId: IRTServiceId = IRTServiceId("ImportIdService")
  object some extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("some"))
    type Input = SomeInput
    type Output = SomeOutput
  }
  object mixi extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("mixi"))
    type Input = MixiInput
    type Output = MixiOutput
  }
  object update extends IRTMethodSignature {
    final val id: IRTMethodId = IRTMethodId(serviceId, IRTMethodName("update"))
    type Input = UpdateInput
    type Output = UpdateOutput
  }
  final case class SomeInput(id: izumi.test.domain01.ImportAppId) extends ImportIdService.SomeInput.Defn
  trait SomeInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeSomeInput: Encoder.AsObject[SomeInput] = deriveEncoder[SomeInput]
    implicit val decodeSomeInput: Decoder[SomeInput] = deriveDecoder[SomeInput]
  }
  object SomeInput extends ImportIdService.SomeInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def id: izumi.test.domain01.ImportAppId }
    def apply(id: izumi.test.domain01.ImportAppId): ImportIdService.SomeInput = {
      assert(id.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.SomeInput(id = id)
    }
    def apply(defn: ImportIdService.SomeInput.Defn): ImportIdService.SomeInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.SomeInput(id = defn.id)
    }
    implicit object SomeInput_cast_into_ImportIdServiceUpdateInput extends izumi.idealingua.runtime.IRTCast[ImportIdService.SomeInput, ImportIdService.UpdateInput] {
      override def convert(_value: ImportIdService.SomeInput): ImportIdService.UpdateInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdService.UpdateInput(id = _value.id)
      }
    }
    implicit object SomeInput_upcast_SomeInput extends izumi.idealingua.runtime.IRTCast[ImportIdService.SomeInput, ImportIdService.SomeInput] {
      override def convert(_value: ImportIdService.SomeInput): ImportIdService.SomeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdService.SomeInput(id = _value.id)
      }
    }
    implicit class SomeInputExtensions(override protected val _value: ImportIdService.SomeInput) extends izumi.idealingua.runtime.IRTConversions[ImportIdService.SomeInput]
  }
  final case class SomeOutput(value: Long) extends AnyVal with ImportIdService.SomeOutput.Defn
  trait SomeOutputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.*
    import _root_.io.circe.syntax.*
    implicit val encodeUnwrappedSomeOutput: Encoder[SomeOutput] = Encoder.instance {
      v => v.value.asJson
    }
    implicit val decodeUnwrappedSomeOutput: Decoder[SomeOutput] = Decoder.instance {
      v => v.as[Long].map(d => SomeOutput(d))
    }
  }
  object SomeOutput extends ImportIdService.SomeOutputCirce {
    trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: Long }
    def apply(value: Long): ImportIdService.SomeOutput = {
      new ImportIdService.SomeOutput(value = value)
    }
    def apply(defn: ImportIdService.SomeOutput.Defn): ImportIdService.SomeOutput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.SomeOutput(value = defn.value)
    }
    implicit object SomeOutput_upcast_SomeOutput extends izumi.idealingua.runtime.IRTCast[ImportIdService.SomeOutput, ImportIdService.SomeOutput] {
      override def convert(_value: ImportIdService.SomeOutput): ImportIdService.SomeOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdService.SomeOutput(value = _value.value)
      }
    }
    implicit class SomeOutputExtensions(override protected val _value: ImportIdService.SomeOutput) extends izumi.idealingua.runtime.IRTConversions[ImportIdService.SomeOutput]
  }
  final case class MixiInput(par: izumi.test.domain01.GenericFailureData) extends ImportIdService.MixiInput.Defn
  trait MixiInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeMixiInput: Encoder.AsObject[MixiInput] = deriveEncoder[MixiInput]
    implicit val decodeMixiInput: Decoder[MixiInput] = deriveDecoder[MixiInput]
  }
  object MixiInput extends ImportIdService.MixiInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def par: izumi.test.domain01.GenericFailureData }
    def apply(par: izumi.test.domain01.GenericFailureData): ImportIdService.MixiInput = {
      assert(par.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.MixiInput(par = par)
    }
    def apply(defn: ImportIdService.MixiInput.Defn): ImportIdService.MixiInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.MixiInput(par = defn.par)
    }
    implicit object MixiInput_upcast_MixiInput extends izumi.idealingua.runtime.IRTCast[ImportIdService.MixiInput, ImportIdService.MixiInput] {
      override def convert(_value: ImportIdService.MixiInput): ImportIdService.MixiInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdService.MixiInput(par = _value.par)
      }
    }
    implicit class MixiInputExtensions(override protected val _value: ImportIdService.MixiInput) extends izumi.idealingua.runtime.IRTConversions[ImportIdService.MixiInput]
  }
  sealed trait MixiOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait MixiOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeMixiOutput: Encoder.AsObject[ImportIdService.MixiOutput] = Encoder.AsObject.instance {
      case v: ImportIdService.MixiOutput.AdtA2 =>
        Map("AdtA2" -> v.value).asJsonObject
      case v: ImportIdService.MixiOutput.GenericFailureData =>
        Map("GenericFailureData" -> v.value).asJsonObject
    }
    implicit val decodeMixiOutput: Decoder[ImportIdService.MixiOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "AdtA2" =>
          value.as[_root_.izumi.test.domain02.AdtA2].map(ImportIdService.MixiOutput.AdtA2.apply)
        case "GenericFailureData" =>
          value.as[_root_.izumi.test.domain01.GenericFailureData].map(ImportIdService.MixiOutput.GenericFailureData.apply)
        case _ =>
          val cname = "izumi.test.domain02.ImportIdService.MixiOutput"
          val alts = List("AdtA2", "GenericFailureData").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object MixiOutput extends ImportIdService.MixiOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = ImportIdService.MixiOutput
    final case class AdtA2(value: _root_.izumi.test.domain02.AdtA2) extends ImportIdService.MixiOutput
    implicit def intoAdtA2(value: _root_.izumi.test.domain02.AdtA2): ImportIdService.MixiOutput = ImportIdService.MixiOutput.AdtA2(value)
    implicit def fromAdtA2(value: ImportIdService.MixiOutput.AdtA2): _root_.izumi.test.domain02.AdtA2 = value.value
    final case class GenericFailureData(value: _root_.izumi.test.domain01.GenericFailureData) extends ImportIdService.MixiOutput
    implicit def intoGenericFailureData(value: _root_.izumi.test.domain01.GenericFailureData): ImportIdService.MixiOutput = ImportIdService.MixiOutput.GenericFailureData(value)
    implicit def fromGenericFailureData(value: ImportIdService.MixiOutput.GenericFailureData): _root_.izumi.test.domain01.GenericFailureData = value.value
  }
  final case class UpdateInput(id: izumi.test.domain01.ImportAppId) extends ImportIdService.UpdateInput.Defn
  trait UpdateInputCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeUpdateInput: Encoder.AsObject[UpdateInput] = deriveEncoder[UpdateInput]
    implicit val decodeUpdateInput: Decoder[UpdateInput] = deriveDecoder[UpdateInput]
  }
  object UpdateInput extends ImportIdService.UpdateInputCirce {
    trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def id: izumi.test.domain01.ImportAppId }
    def apply(id: izumi.test.domain01.ImportAppId): ImportIdService.UpdateInput = {
      assert(id.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.UpdateInput(id = id)
    }
    def apply(defn: ImportIdService.UpdateInput.Defn): ImportIdService.UpdateInput = {
      assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
      new ImportIdService.UpdateInput(id = defn.id)
    }
    implicit object UpdateInput_cast_into_ImportIdServiceSomeInput extends izumi.idealingua.runtime.IRTCast[ImportIdService.UpdateInput, ImportIdService.SomeInput] {
      override def convert(_value: ImportIdService.UpdateInput): ImportIdService.SomeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdService.SomeInput(id = _value.id)
      }
    }
    implicit object UpdateInput_upcast_UpdateInput extends izumi.idealingua.runtime.IRTCast[ImportIdService.UpdateInput, ImportIdService.UpdateInput] {
      override def convert(_value: ImportIdService.UpdateInput): ImportIdService.UpdateInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ImportIdService.UpdateInput(id = _value.id)
      }
    }
    implicit class UpdateInputExtensions(override protected val _value: ImportIdService.UpdateInput) extends izumi.idealingua.runtime.IRTConversions[ImportIdService.UpdateInput]
  }
  sealed trait UpdateOutput extends izumi.idealingua.runtime.model.IDLAdtElement with scala.Product
  trait UpdateOutputCirce {
    import _root_.io.circe.syntax.*
    import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
    implicit val encodeUpdateOutput: Encoder.AsObject[ImportIdService.UpdateOutput] = Encoder.AsObject.instance {
      case v: ImportIdService.UpdateOutput.AdtA2 =>
        Map("AdtA2" -> v.value).asJsonObject
      case v: ImportIdService.UpdateOutput.GenericFailure =>
        Map("GenericFailure" -> v.value).asJsonObject
    }
    implicit val decodeUpdateOutput: Decoder[ImportIdService.UpdateOutput] = Decoder.instance(c => {
      val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
      for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
        case "AdtA2" =>
          value.as[_root_.izumi.test.domain02.AdtA2].map(ImportIdService.UpdateOutput.AdtA2.apply)
        case "GenericFailure" =>
          value.as[_root_.izumi.test.domain01.GenericFailure].map(ImportIdService.UpdateOutput.GenericFailure.apply)
        case _ =>
          val cname = "izumi.test.domain02.ImportIdService.UpdateOutput"
          val alts = List("AdtA2", "GenericFailure").mkString(",")
          Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
      }) yield {
        result
      }
    })
  }
  object UpdateOutput extends ImportIdService.UpdateOutputCirce with izumi.idealingua.runtime.model.IDLAdt {
    import _root_.scala.language.implicitConversions
    type Element = ImportIdService.UpdateOutput
    final case class AdtA2(value: _root_.izumi.test.domain02.AdtA2) extends ImportIdService.UpdateOutput
    implicit def intoAdtA2(value: _root_.izumi.test.domain02.AdtA2): ImportIdService.UpdateOutput = ImportIdService.UpdateOutput.AdtA2(value)
    implicit def fromAdtA2(value: ImportIdService.UpdateOutput.AdtA2): _root_.izumi.test.domain02.AdtA2 = value.value
    final case class GenericFailure(value: _root_.izumi.test.domain01.GenericFailure) extends ImportIdService.UpdateOutput
    implicit def intoGenericFailure(value: _root_.izumi.test.domain01.GenericFailure): ImportIdService.UpdateOutput = ImportIdService.UpdateOutput.GenericFailure(value)
    implicit def fromGenericFailure(value: ImportIdService.UpdateOutput.GenericFailure): _root_.izumi.test.domain01.GenericFailure = value.value
  }
}

object ImportIdServiceCodecs {
  object some extends IRTCirceMarshaller {
    import ImportIdService.some.*
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
  object mixi extends IRTCirceMarshaller {
    import ImportIdService.mixi.*
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
  object update extends IRTCirceMarshaller {
    import ImportIdService.update.*
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
       