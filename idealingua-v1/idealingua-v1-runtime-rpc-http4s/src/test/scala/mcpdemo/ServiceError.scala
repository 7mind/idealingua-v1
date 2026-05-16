package mcpdemo



final case class ServiceError(code: Int, message: String) extends TimedFailure with ServiceError.Defn

trait ServiceErrorCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeServiceError: Encoder.AsObject[ServiceError] = deriveEncoder[ServiceError]
  implicit val decodeServiceError: Decoder[ServiceError] = deriveDecoder[ServiceError]
}

object ServiceError extends ServiceErrorCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def code: Int
    def message: String
  }
  def apply(timedfailure: TimedFailure): ServiceError = {
    assert(timedfailure.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ServiceError(code = timedfailure.code, message = timedfailure.message)
  }
  def apply(defn: ServiceError.Defn): ServiceError = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ServiceError(code = defn.code, message = defn.message)
  }
  implicit object ServiceError_cast_into_TimedFailureStruct extends izumi.idealingua.runtime.IRTCast[ServiceError, TimedFailure.Struct] {
    override def convert(_value: ServiceError): TimedFailure.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TimedFailure.Struct(code = _value.code, message = _value.message)
    }
  }
  implicit object ServiceError_upcast_ServiceError extends izumi.idealingua.runtime.IRTCast[ServiceError, ServiceError] {
    override def convert(_value: ServiceError): ServiceError = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ServiceError(code = _value.code, message = _value.message)
    }
  }
  implicit object ServiceError_upcast_TimedFailure extends izumi.idealingua.runtime.IRTCast[ServiceError, TimedFailure] {
    override def convert(_value: ServiceError): TimedFailure = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TimedFailure.Struct(code = _value.code, message = _value.message)
    }
  }
  implicit class ServiceErrorExtensions(override protected val _value: ServiceError) extends izumi.idealingua.runtime.IRTConversions[ServiceError]
}
       