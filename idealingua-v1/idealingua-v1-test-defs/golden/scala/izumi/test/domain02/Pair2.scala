package izumi.test.domain02



trait Pair2 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def y: String
  def x: String
}

trait Pair2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePair2: Encoder.AsObject[Pair2] = Encoder.AsObject.instance {
    case v: Pair2.Struct =>
      Map("izumi.test.domain02.Pair2.Struct" -> v).asJsonObject
  }
  implicit val decodePair2: Decoder[Pair2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain02.Pair2.Struct" =>
        value.as[Pair2.Struct]
      case _ =>
        val cname = "izumi.test.domain02.Pair2"
        val alts = List("izumi.test.domain02.Pair2.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Pair2 extends Pair2Circe {
  def apply(y: String, x: String) = Struct(y, x)
  final case class Struct(y: String, x: String) extends Pair2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Pair2.StructCirce {
    def apply(pair2: Pair2): Pair2.Struct = {
      assert(pair2.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Pair2.Struct(y = pair2.y, x = pair2.x)
    }
    implicit object Struct_cast_into_Pair1Struct extends izumi.idealingua.runtime.IRTCast[Pair2.Struct, Pair1.Struct] {
      override def convert(_value: Pair2.Struct): Pair1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair1.Struct(y = _value.y, x = _value.x)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Pair2.Struct, Pair2.Struct] {
      override def convert(_value: Pair2.Struct): Pair2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair2.Struct(y = _value.y, x = _value.x)
      }
    }
    implicit object Struct_upcast_Pair2 extends izumi.idealingua.runtime.IRTCast[Pair2.Struct, Pair2] {
      override def convert(_value: Pair2.Struct): Pair2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair2.Struct(y = _value.y, x = _value.x)
      }
    }
    implicit class StructExtensions(override protected val _value: Pair2.Struct) extends izumi.idealingua.runtime.IRTConversions[Pair2.Struct]
  }
  implicit object Pair2_cast_into_Pair1Struct extends izumi.idealingua.runtime.IRTCast[Pair2, Pair1.Struct] {
    override def convert(_value: Pair2): Pair1.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Pair1.Struct(y = _value.y, x = _value.x)
    }
  }
  implicit object Pair2_downcast_extend_Pair2Struct extends izumi.idealingua.runtime.IRTExtend[Pair2, Pair2.Struct] {
    class Call(private val _value: Pair2) extends AnyVal {
      def using(): Pair2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair2.Struct(x = _value.x, y = _value.y)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Pair2): Call = new Call(_value)
  }
  implicit object Pair2_upcast_Pair2 extends izumi.idealingua.runtime.IRTCast[Pair2, Pair2] {
    override def convert(_value: Pair2): Pair2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Pair2.Struct(y = _value.y, x = _value.x)
    }
  }
  implicit class Pair2Extensions(override protected val _value: Pair2) extends izumi.idealingua.runtime.IRTConversions[Pair2]
}
       