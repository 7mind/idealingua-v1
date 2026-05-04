package idltest.clones



trait M0 extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }

trait M0Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeM0: Encoder.AsObject[M0] = Encoder.AsObject.instance {
    case v: M0.Struct =>
      Map("idltest.clones.M0.Struct" -> v).asJsonObject
  }
  implicit val decodeM0: Decoder[M0] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.clones.M0.Struct" =>
        value.as[M0.Struct]
      case _ =>
        val cname = "idltest.clones.M0"
        val alts = List("idltest.clones.M0.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object M0 extends M0Circe {
  def apply(value: String) = Struct(value)
  final case class Struct(value: String) extends AnyVal with M0
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("value")((v: Struct) => v.value)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("value")((d: String) => new Struct(d))
  }
  object Struct extends M0.StructCirce {
    def apply(m0: M0): M0.Struct = {
      assert(m0.asInstanceOf[_root_.scala.AnyRef] ne null)
      new M0.Struct(value = m0.value)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[M0.Struct, M0.Struct] {
      override def convert(_value: M0.Struct): M0.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M0.Struct(value = _value.value)
      }
    }
    implicit object Struct_upcast_M0 extends izumi.idealingua.runtime.IRTCast[M0.Struct, M0] {
      override def convert(_value: M0.Struct): M0 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M0.Struct(value = _value.value)
      }
    }
    implicit class StructExtensions(override protected val _value: M0.Struct) extends izumi.idealingua.runtime.IRTConversions[M0.Struct]
  }
  implicit object M0_downcast_extend_M0Struct extends izumi.idealingua.runtime.IRTExtend[M0, M0.Struct] {
    class Call(private val _value: M0) extends AnyVal {
      def using(): M0.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M0.Struct(value = _value.value)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: M0): Call = new Call(_value)
  }
  implicit object M0_upcast_M0 extends izumi.idealingua.runtime.IRTCast[M0, M0] {
    override def convert(_value: M0): M0 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      M0.Struct(value = _value.value)
    }
  }
  implicit class M0Extensions(override protected val _value: M0) extends izumi.idealingua.runtime.IRTConversions[M0]
}
       