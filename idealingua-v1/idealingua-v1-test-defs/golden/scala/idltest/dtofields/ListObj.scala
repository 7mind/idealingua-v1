package idltest.dtofields



final case class ListObj(all: List[NullObj]) extends ListObj.Defn

trait ListObjCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeListObj: Encoder.AsObject[ListObj] = deriveEncoder[ListObj]
  implicit val decodeListObj: Decoder[ListObj] = deriveDecoder[ListObj]
}

object ListObj extends ListObjCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def all: List[NullObj] }
  def apply(all: List[NullObj]): ListObj = {
    new ListObj(all = all)
  }
  def apply(defn: ListObj.Defn): ListObj = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ListObj(all = defn.all)
  }
  implicit object ListObj_upcast_ListObj extends izumi.idealingua.runtime.IRTCast[ListObj, ListObj] {
    override def convert(_value: ListObj): ListObj = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ListObj(all = _value.all)
    }
  }
  implicit class ListObjExtensions(override protected val _value: ListObj) extends izumi.idealingua.runtime.IRTConversions[ListObj]
}
       