package izumi.test.domain01



trait AnyValTest2 extends izumi.idealingua.runtime.model.IDLGeneratedType { def field: AnyValTest }

trait AnyValTest2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAnyValTest2: Encoder.AsObject[AnyValTest2] = Encoder.AsObject.instance {
    case v: AnyValTest2.Struct =>
      Map("izumi.test.domain01.AnyValTest2.Struct" -> v).asJsonObject
  }
  implicit val decodeAnyValTest2: Decoder[AnyValTest2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.AnyValTest2.Struct" =>
        value.as[AnyValTest2.Struct]
      case _ =>
        val cname = "izumi.test.domain01.AnyValTest2"
        val alts = List("izumi.test.domain01.AnyValTest2.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object AnyValTest2 extends AnyValTest2Circe {
  def apply(field: AnyValTest) = Struct(field)
  final case class Struct(field: AnyValTest) extends AnyValTest2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends AnyValTest2.StructCirce {
    def apply(anyvaltest2: AnyValTest2): AnyValTest2.Struct = {
      assert(anyvaltest2.asInstanceOf[_root_.scala.AnyRef] ne null)
      new AnyValTest2.Struct(field = anyvaltest2.field)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[AnyValTest2.Struct, AnyValTest2.Struct] {
      override def convert(_value: AnyValTest2.Struct): AnyValTest2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnyValTest2.Struct(field = _value.field)
      }
    }
    implicit object Struct_upcast_AnyValTest2 extends izumi.idealingua.runtime.IRTCast[AnyValTest2.Struct, AnyValTest2] {
      override def convert(_value: AnyValTest2.Struct): AnyValTest2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnyValTest2.Struct(field = _value.field)
      }
    }
    implicit class StructExtensions(override protected val _value: AnyValTest2.Struct) extends izumi.idealingua.runtime.IRTConversions[AnyValTest2.Struct]
  }
  implicit object AnyValTest2_downcast_extend_AnyValTest2Struct extends izumi.idealingua.runtime.IRTExtend[AnyValTest2, AnyValTest2.Struct] {
    class Call(private val _value: AnyValTest2) extends AnyVal {
      def using(): AnyValTest2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AnyValTest2.Struct(field = _value.field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AnyValTest2): Call = new Call(_value)
  }
  implicit object AnyValTest2_upcast_AnyValTest2 extends izumi.idealingua.runtime.IRTCast[AnyValTest2, AnyValTest2] {
    override def convert(_value: AnyValTest2): AnyValTest2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AnyValTest2.Struct(field = _value.field)
    }
  }
  implicit class AnyValTest2Extensions(override protected val _value: AnyValTest2) extends izumi.idealingua.runtime.IRTConversions[AnyValTest2]
}
       