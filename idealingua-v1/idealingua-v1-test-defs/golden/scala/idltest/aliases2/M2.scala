package idltest.aliases2



trait M2 extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def f2: String }

trait M2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeM2: Encoder.AsObject[M2] = Encoder.AsObject.instance {
    case v: M2.Struct =>
      Map("idltest.aliases2.M2.Struct" -> v).asJsonObject
  }
  implicit val decodeM2: Decoder[M2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.aliases2.M2.Struct" =>
        value.as[M2.Struct]
      case _ =>
        val cname = "idltest.aliases2.M2"
        val alts = List("idltest.aliases2.M2.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object M2 extends M2Circe {
  def apply(f2: String) = Struct(f2)
  final case class Struct(f2: String) extends AnyVal with M2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("f2")((v: Struct) => v.f2)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("f2")((d: String) => new Struct(d))
  }
  object Struct extends M2.StructCirce {
    def apply(m2: M2): M2.Struct = {
      assert(m2.asInstanceOf[_root_.scala.AnyRef] ne null)
      new M2.Struct(f2 = m2.f2)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[M2.Struct, M2.Struct] {
      override def convert(_value: M2.Struct): M2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M2.Struct(f2 = _value.f2)
      }
    }
    implicit object Struct_upcast_M2 extends izumi.idealingua.runtime.IRTCast[M2.Struct, M2] {
      override def convert(_value: M2.Struct): M2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M2.Struct(f2 = _value.f2)
      }
    }
    implicit class StructExtensions(override protected val _value: M2.Struct) extends izumi.idealingua.runtime.IRTConversions[M2.Struct]
  }
  implicit object M2_downcast_extend_M2Struct extends izumi.idealingua.runtime.IRTExtend[M2, M2.Struct] {
    class Call(private val _value: M2) extends AnyVal {
      def using(): M2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M2.Struct(f2 = _value.f2)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: M2): Call = new Call(_value)
  }
  implicit object M2_upcast_M2 extends izumi.idealingua.runtime.IRTCast[M2, M2] {
    override def convert(_value: M2): M2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      M2.Struct(f2 = _value.f2)
    }
  }
  implicit class M2Extensions(override protected val _value: M2) extends izumi.idealingua.runtime.IRTConversions[M2]
}
       