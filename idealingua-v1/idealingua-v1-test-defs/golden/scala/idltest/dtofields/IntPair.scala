package idltest.dtofields



trait IntPair extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def x: Int
  def y: Int
}

trait IntPairCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIntPair: Encoder.AsObject[IntPair] = Encoder.AsObject.instance {
    case v: IntPair.Struct =>
      Map("idltest.dtofields.IntPair.Struct" -> v).asJsonObject
  }
  implicit val decodeIntPair: Decoder[IntPair] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.dtofields.IntPair.Struct" =>
        value.as[IntPair.Struct]
      case _ =>
        val cname = "idltest.dtofields.IntPair"
        val alts = List("idltest.dtofields.IntPair.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object IntPair extends IntPairCirce {
  def apply(x: Int, y: Int) = Struct(x, y)
  final case class Struct(x: Int, y: Int) extends IntPair
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends IntPair.StructCirce {
    def apply(intpair: IntPair): IntPair.Struct = {
      assert(intpair.asInstanceOf[_root_.scala.AnyRef] ne null)
      new IntPair.Struct(x = intpair.x, y = intpair.y)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[IntPair.Struct, IntPair.Struct] {
      override def convert(_value: IntPair.Struct): IntPair.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntPair.Struct(x = _value.x, y = _value.y)
      }
    }
    implicit object Struct_upcast_IntPair extends izumi.idealingua.runtime.IRTCast[IntPair.Struct, IntPair] {
      override def convert(_value: IntPair.Struct): IntPair = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntPair.Struct(x = _value.x, y = _value.y)
      }
    }
    implicit class StructExtensions(override protected val _value: IntPair.Struct) extends izumi.idealingua.runtime.IRTConversions[IntPair.Struct]
  }
  implicit object IntPair_downcast_extend_IntPairStruct extends izumi.idealingua.runtime.IRTExtend[IntPair, IntPair.Struct] {
    class Call(private val _value: IntPair) extends AnyVal {
      def using(): IntPair.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IntPair.Struct(x = _value.x, y = _value.y)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IntPair): Call = new Call(_value)
  }
  implicit object IntPair_downcast_extend_Point extends izumi.idealingua.runtime.IRTExtend[IntPair, Point] {
    class Call(private val _value: IntPair) extends AnyVal {
      def using(name: String, ownfield: String, `export`: Boolean, metadata: Metadata, whpair: WHPair): Point = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert((whpair.asInstanceOf[_root_.scala.AnyRef] ne null) && (metadata.asInstanceOf[_root_.scala.AnyRef] ne null))
        Point(x = _value.x, y = _value.y, name = name, ownfield = ownfield, `export` = `export`, id = metadata.id, h = whpair.h, w = whpair.w)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IntPair): Call = new Call(_value)
  }
  implicit object IntPair_upcast_IntPair extends izumi.idealingua.runtime.IRTCast[IntPair, IntPair] {
    override def convert(_value: IntPair): IntPair = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IntPair.Struct(x = _value.x, y = _value.y)
    }
  }
  implicit class IntPairExtensions(override protected val _value: IntPair) extends izumi.idealingua.runtime.IRTConversions[IntPair]
}
       