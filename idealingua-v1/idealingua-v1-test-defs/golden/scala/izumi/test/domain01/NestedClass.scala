package izumi.test.domain01



final case class NestedClass(c: Option[NestedClass]) extends NestedClass.Defn

trait NestedClassCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeNestedClass: Encoder.AsObject[NestedClass] = deriveEncoder[NestedClass]
  implicit val decodeNestedClass: Decoder[NestedClass] = deriveDecoder[NestedClass]
}

object NestedClass extends NestedClassCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def c: Option[NestedClass] }
  def apply(c: Option[NestedClass]): NestedClass = {
    new NestedClass(c = c)
  }
  def apply(defn: NestedClass.Defn): NestedClass = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new NestedClass(c = defn.c)
  }
  implicit object NestedClass_upcast_NestedClass extends izumi.idealingua.runtime.IRTCast[NestedClass, NestedClass] {
    override def convert(_value: NestedClass): NestedClass = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NestedClass(c = _value.c)
    }
  }
  implicit class NestedClassExtensions(override protected val _value: NestedClass) extends izumi.idealingua.runtime.IRTConversions[NestedClass]
}
       