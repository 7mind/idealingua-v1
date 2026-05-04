package idltest.dtofields



final case class Point(w: Int, h: Int, id: String, name: String, x: Int, y: Int, ownfield: String, `export`: Boolean) extends Metadata with WHPair with Point.Defn

trait PointCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePoint: Encoder.AsObject[Point] = deriveEncoder[Point]
  implicit val decodePoint: Decoder[Point] = deriveDecoder[Point]
}

object Point extends PointCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def w: Int
    def h: Int
    def id: String
    def name: String
    def x: Int
    def y: Int
    def ownfield: String
    def `export`: Boolean
  }
  def apply(whpair: WHPair, metadata: Metadata, intpair: IntPair, name: String, ownfield: String, `export`: Boolean): Point = {
    assert((intpair.asInstanceOf[_root_.scala.AnyRef] ne null) && ((metadata.asInstanceOf[_root_.scala.AnyRef] ne null) && (whpair.asInstanceOf[_root_.scala.AnyRef] ne null)))
    new Point(w = whpair.w, h = whpair.h, id = metadata.id, x = intpair.x, y = intpair.y, name = name, ownfield = ownfield, `export` = `export`)
  }
  def apply(defn: Point.Defn): Point = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Point(w = defn.w, h = defn.h, id = defn.id, name = defn.name, x = defn.x, y = defn.y, ownfield = defn.ownfield, `export` = defn.`export`)
  }
  implicit object Point_upcast_Point extends izumi.idealingua.runtime.IRTCast[Point, Point] {
    override def convert(_value: Point): Point = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Point(w = _value.w, h = _value.h, id = _value.id, name = _value.name, x = _value.x, y = _value.y, ownfield = _value.ownfield, `export` = _value.`export`)
    }
  }
  implicit object Point_upcast_Metadata extends izumi.idealingua.runtime.IRTCast[Point, Metadata] {
    override def convert(_value: Point): Metadata = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Metadata.Struct(id = _value.id, name = _value.name)
    }
  }
  implicit object Point_upcast_WHPair extends izumi.idealingua.runtime.IRTCast[Point, WHPair] {
    override def convert(_value: Point): WHPair = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WHPair.Struct(w = _value.w, h = _value.h)
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
       