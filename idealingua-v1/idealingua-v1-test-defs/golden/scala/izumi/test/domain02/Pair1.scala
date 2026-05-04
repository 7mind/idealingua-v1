package izumi.test.domain02



trait Pair1 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def x: String
  def y: String
}

trait Pair1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePair1: Encoder.AsObject[Pair1] = Encoder.AsObject.instance {
    case v: Pair1.Struct =>
      Map("izumi.test.domain02.Pair1.Struct" -> v).asJsonObject
  }
  implicit val decodePair1: Decoder[Pair1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain02.Pair1.Struct" =>
        value.as[Pair1.Struct]
      case _ =>
        val cname = "izumi.test.domain02.Pair1"
        val alts = List("izumi.test.domain02.Pair1.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Pair1 extends Pair1Circe {
  def apply(x: String, y: String) = Struct(x, y)
  final case class Struct(x: String, y: String) extends Pair1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Pair1.StructCirce {
    def apply(pair1: Pair1): Pair1.Struct = {
      assert(pair1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Pair1.Struct(x = pair1.x, y = pair1.y)
    }
    implicit object Struct_cast_into_Pair2Struct extends izumi.idealingua.runtime.IRTCast[Pair1.Struct, Pair2.Struct] {
      override def convert(_value: Pair1.Struct): Pair2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair2.Struct(x = _value.x, y = _value.y)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Pair1.Struct, Pair1.Struct] {
      override def convert(_value: Pair1.Struct): Pair1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair1.Struct(x = _value.x, y = _value.y)
      }
    }
    implicit object Struct_upcast_Pair1 extends izumi.idealingua.runtime.IRTCast[Pair1.Struct, Pair1] {
      override def convert(_value: Pair1.Struct): Pair1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair1.Struct(x = _value.x, y = _value.y)
      }
    }
    implicit class StructExtensions(override protected val _value: Pair1.Struct) extends izumi.idealingua.runtime.IRTConversions[Pair1.Struct]
  }
  implicit object Pair1_cast_into_Pair2Struct extends izumi.idealingua.runtime.IRTCast[Pair1, Pair2.Struct] {
    override def convert(_value: Pair1): Pair2.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Pair2.Struct(x = _value.x, y = _value.y)
    }
  }
  implicit object Pair1_downcast_extend_Pair1Struct extends izumi.idealingua.runtime.IRTExtend[Pair1, Pair1.Struct] {
    class Call(private val _value: Pair1) extends AnyVal {
      def using(): Pair1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Pair1.Struct(x = _value.x, y = _value.y)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Pair1): Call = new Call(_value)
  }
  implicit object Pair1_upcast_Pair1 extends izumi.idealingua.runtime.IRTCast[Pair1, Pair1] {
    override def convert(_value: Pair1): Pair1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Pair1.Struct(x = _value.x, y = _value.y)
    }
  }
  implicit class Pair1Extensions(override protected val _value: Pair1) extends izumi.idealingua.runtime.IRTConversions[Pair1]
}
       