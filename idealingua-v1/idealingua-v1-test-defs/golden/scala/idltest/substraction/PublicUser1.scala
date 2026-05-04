package idltest.substraction



trait PublicUser1 extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def name: String }

trait PublicUser1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePublicUser1: Encoder.AsObject[PublicUser1] = Encoder.AsObject.instance {
    case v: PublicUser1.Struct =>
      Map("idltest.substraction.PublicUser1.Struct" -> v).asJsonObject
  }
  implicit val decodePublicUser1: Decoder[PublicUser1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.substraction.PublicUser1.Struct" =>
        value.as[PublicUser1.Struct]
      case _ =>
        val cname = "idltest.substraction.PublicUser1"
        val alts = List("idltest.substraction.PublicUser1.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object PublicUser1 extends PublicUser1Circe {
  def apply(name: String) = Struct(name)
  final case class Struct(name: String) extends AnyVal with PublicUser1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("name")((v: Struct) => v.name)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("name")((d: String) => new Struct(d))
  }
  object Struct extends PublicUser1.StructCirce {
    def apply(user1: User1): PublicUser1.Struct = {
      assert(user1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new PublicUser1.Struct(name = user1.name)
    }
    implicit object Struct_cast_into_PublicUser2 extends izumi.idealingua.runtime.IRTCast[PublicUser1.Struct, PublicUser2] {
      override def convert(_value: PublicUser1.Struct): PublicUser2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PublicUser2(name = _value.name)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[PublicUser1.Struct, PublicUser1.Struct] {
      override def convert(_value: PublicUser1.Struct): PublicUser1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PublicUser1.Struct(name = _value.name)
      }
    }
    implicit object Struct_upcast_PublicUser1 extends izumi.idealingua.runtime.IRTCast[PublicUser1.Struct, PublicUser1] {
      override def convert(_value: PublicUser1.Struct): PublicUser1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PublicUser1.Struct(name = _value.name)
      }
    }
    implicit class StructExtensions(override protected val _value: PublicUser1.Struct) extends izumi.idealingua.runtime.IRTConversions[PublicUser1.Struct]
  }
  implicit object PublicUser1_cast_into_PublicUser2 extends izumi.idealingua.runtime.IRTCast[PublicUser1, PublicUser2] {
    override def convert(_value: PublicUser1): PublicUser2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PublicUser2(name = _value.name)
    }
  }
  implicit object PublicUser1_downcast_extend_PublicUser1Struct extends izumi.idealingua.runtime.IRTExtend[PublicUser1, PublicUser1.Struct] {
    class Call(private val _value: PublicUser1) extends AnyVal {
      def using(): PublicUser1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PublicUser1.Struct(name = _value.name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PublicUser1): Call = new Call(_value)
  }
  implicit object PublicUser1_upcast_PublicUser1 extends izumi.idealingua.runtime.IRTCast[PublicUser1, PublicUser1] {
    override def convert(_value: PublicUser1): PublicUser1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PublicUser1.Struct(name = _value.name)
    }
  }
  implicit class PublicUser1Extensions(override protected val _value: PublicUser1) extends izumi.idealingua.runtime.IRTConversions[PublicUser1]
}
       