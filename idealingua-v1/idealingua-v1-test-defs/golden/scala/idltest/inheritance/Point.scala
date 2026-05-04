package idltest.inheritance



final case class Point(id: String, name: String, x: Int, y: Int) extends Metadata with Point.Defn

trait PointCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePoint: Encoder.AsObject[Point] = deriveEncoder[Point]
  implicit val decodePoint: Decoder[Point] = deriveDecoder[Point]
}

object Point extends PointCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def id: String
    def name: String
    def x: Int
    def y: Int
  }
  def apply(metadata: Metadata, intpair: IntPair): Point = {
    assert((intpair.asInstanceOf[_root_.scala.AnyRef] ne null) && (metadata.asInstanceOf[_root_.scala.AnyRef] ne null))
    new Point(id = metadata.id, name = metadata.name, x = intpair.x, y = intpair.y)
  }
  def apply(defn: Point.Defn): Point = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Point(id = defn.id, name = defn.name, x = defn.x, y = defn.y)
  }
  implicit object Point_cast_into_PointLikeStruct extends izumi.idealingua.runtime.IRTCast[Point, PointLike.Struct] {
    override def convert(_value: Point): PointLike.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PointLike.Struct(id = _value.id, name = _value.name, x = _value.x, y = _value.y)
    }
  }
  implicit object Point_upcast_Point extends izumi.idealingua.runtime.IRTCast[Point, Point] {
    override def convert(_value: Point): Point = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Point(id = _value.id, name = _value.name, x = _value.x, y = _value.y)
    }
  }
  implicit object Point_upcast_Metadata extends izumi.idealingua.runtime.IRTCast[Point, Metadata] {
    override def convert(_value: Point): Metadata = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Metadata.Struct(id = _value.id, name = _value.name)
    }
  }
  implicit object Point_upcast_IntPair extends izumi.idealingua.runtime.IRTCast[Point, IntPair] {
    override def convert(_value: Point): IntPair = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IntPair.Struct(x = _value.x, y = _value.y)
    }
  }
  implicit class PointExtensions(override protected val _value: Point) extends izumi.idealingua.runtime.IRTConversions[Point]
}
       