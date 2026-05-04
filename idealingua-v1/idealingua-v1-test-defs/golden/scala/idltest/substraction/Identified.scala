package idltest.substraction



trait Identified extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def id: String }

trait IdentifiedCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIdentified: Encoder.AsObject[Identified] = Encoder.AsObject.instance {
    case v: Identified.Struct =>
      Map("idltest.substraction.Identified.Struct" -> v).asJsonObject
  }
  implicit val decodeIdentified: Decoder[Identified] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.substraction.Identified.Struct" =>
        value.as[Identified.Struct]
      case _ =>
        val cname = "idltest.substraction.Identified"
        val alts = List("idltest.substraction.Identified.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Identified extends IdentifiedCirce {
  def apply(id: String) = Struct(id)
  final case class Struct(id: String) extends AnyVal with Identified
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("id")((v: Struct) => v.id)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("id")((d: String) => new Struct(d))
  }
  object Struct extends Identified.StructCirce {
    def apply(identified: Identified): Identified.Struct = {
      assert(identified.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Identified.Struct(id = identified.id)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Identified.Struct, Identified.Struct] {
      override def convert(_value: Identified.Struct): Identified.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Identified.Struct(id = _value.id)
      }
    }
    implicit object Struct_upcast_Identified extends izumi.idealingua.runtime.IRTCast[Identified.Struct, Identified] {
      override def convert(_value: Identified.Struct): Identified = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Identified.Struct(id = _value.id)
      }
    }
    implicit class StructExtensions(override protected val _value: Identified.Struct) extends izumi.idealingua.runtime.IRTConversions[Identified.Struct]
  }
  implicit object Identified_downcast_extend_IdentifiedStruct extends izumi.idealingua.runtime.IRTExtend[Identified, Identified.Struct] {
    class Call(private val _value: Identified) extends AnyVal {
      def using(): Identified.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Identified.Struct(id = _value.id)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Identified): Call = new Call(_value)
  }
  implicit object Identified_upcast_Identified extends izumi.idealingua.runtime.IRTCast[Identified, Identified] {
    override def convert(_value: Identified): Identified = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Identified.Struct(id = _value.id)
    }
  }
  implicit class IdentifiedExtensions(override protected val _value: Identified) extends izumi.idealingua.runtime.IRTConversions[Identified]
}
       