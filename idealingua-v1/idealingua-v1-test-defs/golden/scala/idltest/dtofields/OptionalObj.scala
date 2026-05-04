package idltest.dtofields



final case class OptionalObj(no: Option[NullableObj]) extends OptionalObj.Defn

trait OptionalObjCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeOptionalObj: Encoder.AsObject[OptionalObj] = deriveEncoder[OptionalObj]
  implicit val decodeOptionalObj: Decoder[OptionalObj] = deriveDecoder[OptionalObj]
}

object OptionalObj extends OptionalObjCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def no: Option[NullableObj] }
  def apply(no: Option[NullableObj]): OptionalObj = {
    new OptionalObj(no = no)
  }
  def apply(defn: OptionalObj.Defn): OptionalObj = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new OptionalObj(no = defn.no)
  }
  implicit object OptionalObj_upcast_OptionalObj extends izumi.idealingua.runtime.IRTCast[OptionalObj, OptionalObj] {
    override def convert(_value: OptionalObj): OptionalObj = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      OptionalObj(no = _value.no)
    }
  }
  implicit class OptionalObjExtensions(override protected val _value: OptionalObj) extends izumi.idealingua.runtime.IRTConversions[OptionalObj]
}
       