package idltest.dtofields



trait PointLike extends izumi.idealingua.runtime.model.IDLGeneratedType with Metadata {
  def id: String
  def name: String
  def x: Int
  def y: Int
}

trait PointLikeCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePointLike: Encoder.AsObject[PointLike] = Encoder.AsObject.instance {
    case v: PointLike.Struct =>
      Map("idltest.dtofields.PointLike.Struct" -> v).asJsonObject
  }
  implicit val decodePointLike: Decoder[PointLike] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.dtofields.PointLike.Struct" =>
        value.as[PointLike.Struct]
      case _ =>
        val cname = "idltest.dtofields.PointLike"
        val alts = List("idltest.dtofields.PointLike.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object PointLike extends PointLikeCirce {
  def apply(id: String, name: String, x: Int, y: Int) = Struct(id, name, x, y)
  final case class Struct(id: String, name: String, x: Int, y: Int) extends PointLike
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends PointLike.StructCirce {
    def apply(metadata: Metadata, intpair: IntPair): PointLike.Struct = {
      assert((intpair.asInstanceOf[_root_.scala.AnyRef] ne null) && (metadata.asInstanceOf[_root_.scala.AnyRef] ne null))
      new PointLike.Struct(id = metadata.id, name = metadata.name, x = intpair.x, y = intpair.y)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[PointLike.Struct, PointLike.Struct] {
      override def convert(_value: PointLike.Struct): PointLike.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PointLike.Struct(id = _value.id, name = _value.name, x = _value.x, y = _value.y)
      }
    }
    implicit object Struct_upcast_PointLike extends izumi.idealingua.runtime.IRTCast[PointLike.Struct, PointLike] {
      override def convert(_value: PointLike.Struct): PointLike = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PointLike.Struct(id = _value.id, name = _value.name, x = _value.x, y = _value.y)
      }
    }
    implicit object Struct_upcast_Metadata extends izumi.idealingua.runtime.IRTCast[PointLike.Struct, Metadata] {
      override def convert(_value: PointLike.Struct): Metadata = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Metadata.Struct(id = _value.id, name = _value.name)
      }
    }
    implicit class StructExtensions(override protected val _value: PointLike.Struct) extends izumi.idealingua.runtime.IRTConversions[PointLike.Struct]
  }
  implicit object PointLike_downcast_extend_PointLikeStruct extends izumi.idealingua.runtime.IRTExtend[PointLike, PointLike.Struct] {
    class Call(private val _value: PointLike) extends AnyVal {
      def using(): PointLike.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PointLike.Struct(id = _value.id, name = _value.name, x = _value.x, y = _value.y)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PointLike): Call = new Call(_value)
  }
  implicit object PointLike_upcast_PointLike extends izumi.idealingua.runtime.IRTCast[PointLike, PointLike] {
    override def convert(_value: PointLike): PointLike = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PointLike.Struct(id = _value.id, name = _value.name, x = _value.x, y = _value.y)
    }
  }
  implicit object PointLike_upcast_Metadata extends izumi.idealingua.runtime.IRTCast[PointLike, Metadata] {
    override def convert(_value: PointLike): Metadata = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Metadata.Struct(id = _value.id, name = _value.name)
    }
  }
  implicit object PointLike_upcast_IntPair extends izumi.idealingua.runtime.IRTCast[PointLike, IntPair] {
    override def convert(_value: PointLike): IntPair = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IntPair.Struct(x = _value.x, y = _value.y)
    }
  }
  implicit class PointLikeExtensions(override protected val _value: PointLike) extends izumi.idealingua.runtime.IRTConversions[PointLike]
}
       