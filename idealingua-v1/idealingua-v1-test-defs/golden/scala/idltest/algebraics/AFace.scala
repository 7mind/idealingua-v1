package idltest.algebraics



trait AFace extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: Int }

trait AFaceCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeAFace: Encoder.AsObject[AFace] = Encoder.AsObject.instance {
    case v: AFace.Struct =>
      Map("idltest.algebraics.AFace.Struct" -> v).asJsonObject
  }
  implicit val decodeAFace: Decoder[AFace] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.algebraics.AFace.Struct" =>
        value.as[AFace.Struct]
      case _ =>
        val cname = "idltest.algebraics.AFace"
        val alts = List("idltest.algebraics.AFace.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object AFace extends AFaceCirce {
  def apply(a: Int) = Struct(a)
  final case class Struct(a: Int) extends AnyVal with AFace
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Int]("a")((v: Struct) => v.a)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Int]("a")((d: Int) => new Struct(d))
  }
  object Struct extends AFace.StructCirce {
    def apply(aface: AFace): AFace.Struct = {
      assert(aface.asInstanceOf[_root_.scala.AnyRef] ne null)
      new AFace.Struct(a = aface.a)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[AFace.Struct, AFace.Struct] {
      override def convert(_value: AFace.Struct): AFace.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AFace.Struct(a = _value.a)
      }
    }
    implicit object Struct_upcast_AFace extends izumi.idealingua.runtime.IRTCast[AFace.Struct, AFace] {
      override def convert(_value: AFace.Struct): AFace = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AFace.Struct(a = _value.a)
      }
    }
    implicit class StructExtensions(override protected val _value: AFace.Struct) extends izumi.idealingua.runtime.IRTConversions[AFace.Struct]
  }
  implicit object AFace_downcast_extend_AFaceStruct extends izumi.idealingua.runtime.IRTExtend[AFace, AFace.Struct] {
    class Call(private val _value: AFace) extends AnyVal {
      def using(): AFace.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        AFace.Struct(a = _value.a)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: AFace): Call = new Call(_value)
  }
  implicit object AFace_upcast_AFace extends izumi.idealingua.runtime.IRTCast[AFace, AFace] {
    override def convert(_value: AFace): AFace = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      AFace.Struct(a = _value.a)
    }
  }
  implicit class AFaceExtensions(override protected val _value: AFace) extends izumi.idealingua.runtime.IRTConversions[AFace]
}
       