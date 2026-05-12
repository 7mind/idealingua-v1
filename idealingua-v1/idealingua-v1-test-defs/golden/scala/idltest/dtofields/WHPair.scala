package idltest.dtofields



trait WHPair extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def w: Int
  def h: Int
}

trait WHPairCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeWHPair: Encoder.AsObject[WHPair] = Encoder.AsObject.instance {
    case v: Point =>
      Map("idltest.dtofields.Point" -> v).asJsonObject
    case v: WHPair.Struct =>
      Map("idltest.dtofields.WHPair.Struct" -> v).asJsonObject
  }
  implicit val decodeWHPair: Decoder[WHPair] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.dtofields.Point" =>
        value.as[Point]
      case "idltest.dtofields.WHPair.Struct" =>
        value.as[WHPair.Struct]
      case _ =>
        val cname = "idltest.dtofields.WHPair"
        val alts = List("idltest.dtofields.Point", "idltest.dtofields.WHPair.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object WHPair extends WHPairCirce {
  def apply(w: Int, h: Int) = Struct(w, h)
  final case class Struct(w: Int, h: Int) extends WHPair
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends WHPair.StructCirce {
    def apply(whpair: WHPair): WHPair.Struct = {
      assert(whpair.asInstanceOf[_root_.scala.AnyRef] ne null)
      new WHPair.Struct(w = whpair.w, h = whpair.h)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[WHPair.Struct, WHPair.Struct] {
      override def convert(_value: WHPair.Struct): WHPair.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WHPair.Struct(w = _value.w, h = _value.h)
      }
    }
    implicit object Struct_upcast_WHPair extends izumi.idealingua.runtime.IRTCast[WHPair.Struct, WHPair] {
      override def convert(_value: WHPair.Struct): WHPair = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WHPair.Struct(w = _value.w, h = _value.h)
      }
    }
    implicit class StructExtensions(override protected val _value: WHPair.Struct) extends izumi.idealingua.runtime.IRTConversions[WHPair.Struct]
  }
  implicit object WHPair_downcast_extend_Point extends izumi.idealingua.runtime.IRTExtend[WHPair, Point] {
    class Call(private val _value: WHPair) extends AnyVal {
      def using(id: String, name: String, x: Int, y: Int, ownfield: String, `export`: Boolean): Point = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Point(w = _value.w, h = _value.h, id = id, name = name, x = x, y = y, ownfield = ownfield, `export` = `export`)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WHPair): Call = new Call(_value)
  }
  implicit object WHPair_downcast_extend_WHPairStruct extends izumi.idealingua.runtime.IRTExtend[WHPair, WHPair.Struct] {
    class Call(private val _value: WHPair) extends AnyVal {
      def using(): WHPair.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WHPair.Struct(w = _value.w, h = _value.h)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WHPair): Call = new Call(_value)
  }
  implicit object WHPair_upcast_WHPair extends izumi.idealingua.runtime.IRTCast[WHPair, WHPair] {
    override def convert(_value: WHPair): WHPair = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WHPair.Struct(w = _value.w, h = _value.h)
    }
  }
  implicit class WHPairExtensions(override protected val _value: WHPair) extends izumi.idealingua.runtime.IRTConversions[WHPair]
}
       