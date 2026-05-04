package idltest.dtofields



trait Metadata extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def id: String
  def name: String
}

trait MetadataCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeMetadata: Encoder.AsObject[Metadata] = Encoder.AsObject.instance {
    case v: Metadata.Struct =>
      Map("idltest.dtofields.Metadata.Struct" -> v).asJsonObject
    case v: PointLike.Struct =>
      Map("idltest.dtofields.PointLike.Struct" -> v).asJsonObject
    case v: Point =>
      Map("idltest.dtofields.Point" -> v).asJsonObject
  }
  implicit val decodeMetadata: Decoder[Metadata] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.dtofields.Metadata.Struct" =>
        value.as[Metadata.Struct]
      case "idltest.dtofields.PointLike.Struct" =>
        value.as[PointLike.Struct]
      case "idltest.dtofields.Point" =>
        value.as[Point]
      case _ =>
        val cname = "idltest.dtofields.Metadata"
        val alts = List("idltest.dtofields.Metadata.Struct", "idltest.dtofields.PointLike.Struct", "idltest.dtofields.Point").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Metadata extends MetadataCirce {
  def apply(id: String, name: String) = Struct(id, name)
  final case class Struct(id: String, name: String) extends Metadata
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Metadata.StructCirce {
    def apply(metadata: Metadata): Metadata.Struct = {
      assert(metadata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Metadata.Struct(id = metadata.id, name = metadata.name)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Metadata.Struct, Metadata.Struct] {
      override def convert(_value: Metadata.Struct): Metadata.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Metadata.Struct(id = _value.id, name = _value.name)
      }
    }
    implicit object Struct_upcast_Metadata extends izumi.idealingua.runtime.IRTCast[Metadata.Struct, Metadata] {
      override def convert(_value: Metadata.Struct): Metadata = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Metadata.Struct(id = _value.id, name = _value.name)
      }
    }
    implicit class StructExtensions(override protected val _value: Metadata.Struct) extends izumi.idealingua.runtime.IRTConversions[Metadata.Struct]
  }
  implicit object Metadata_downcast_extend_MetadataStruct extends izumi.idealingua.runtime.IRTExtend[Metadata, Metadata.Struct] {
    class Call(private val _value: Metadata) extends AnyVal {
      def using(): Metadata.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Metadata.Struct(name = _value.name, id = _value.id)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Metadata): Call = new Call(_value)
  }
  implicit object Metadata_downcast_extend_PointLikeStruct extends izumi.idealingua.runtime.IRTExtend[Metadata, PointLike.Struct] {
    class Call(private val _value: Metadata) extends AnyVal {
      def using(intpair: IntPair): PointLike.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(intpair.asInstanceOf[_root_.scala.AnyRef] ne null)
        PointLike.Struct(name = _value.name, id = _value.id, x = intpair.x, y = intpair.y)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Metadata): Call = new Call(_value)
  }
  implicit object Metadata_downcast_extend_Point extends izumi.idealingua.runtime.IRTExtend[Metadata, Point] {
    class Call(private val _value: Metadata) extends AnyVal {
      def using(name: String, ownfield: String, `export`: Boolean, intpair: IntPair, whpair: WHPair): Point = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert((whpair.asInstanceOf[_root_.scala.AnyRef] ne null) && (intpair.asInstanceOf[_root_.scala.AnyRef] ne null))
        Point(id = _value.id, name = name, ownfield = ownfield, `export` = `export`, x = intpair.x, y = intpair.y, h = whpair.h, w = whpair.w)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Metadata): Call = new Call(_value)
  }
  implicit object Metadata_upcast_Metadata extends izumi.idealingua.runtime.IRTCast[Metadata, Metadata] {
    override def convert(_value: Metadata): Metadata = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Metadata.Struct(id = _value.id, name = _value.name)
    }
  }
  implicit class MetadataExtensions(override protected val _value: Metadata) extends izumi.idealingua.runtime.IRTConversions[Metadata]
}
       