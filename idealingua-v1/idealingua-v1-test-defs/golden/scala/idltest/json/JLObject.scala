package idltest.json



final case class JLObject(fields: Map[String, JSONLike]) extends JLObject.Defn

trait JLObjectCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeJLObject: Encoder.AsObject[JLObject] = deriveEncoder[JLObject]
  implicit val decodeJLObject: Decoder[JLObject] = deriveDecoder[JLObject]
}

object JLObject extends JLObjectCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def fields: Map[String, JSONLike] }
  def apply(fields: Map[String, JSONLike]): JLObject = {
    new JLObject(fields = fields)
  }
  def apply(defn: JLObject.Defn): JLObject = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new JLObject(fields = defn.fields)
  }
  implicit object JLObject_upcast_JLObject extends izumi.idealingua.runtime.IRTCast[JLObject, JLObject] {
    override def convert(_value: JLObject): JLObject = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      JLObject(fields = _value.fields)
    }
  }
  implicit class JLObjectExtensions(override protected val _value: JLObject) extends izumi.idealingua.runtime.IRTConversions[JLObject]
}
       