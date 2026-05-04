package idltest.aliases



trait M1 extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }

trait M1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeM1: Encoder.AsObject[M1] = Encoder.AsObject.instance {
    case v: D1 =>
      Map("idltest.aliases.D1" -> v).asJsonObject
    case v: M1.Struct =>
      Map("idltest.aliases.M1.Struct" -> v).asJsonObject
  }
  implicit val decodeM1: Decoder[M1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.aliases.D1" =>
        value.as[D1]
      case "idltest.aliases.M1.Struct" =>
        value.as[M1.Struct]
      case _ =>
        val cname = "idltest.aliases.M1"
        val alts = List("idltest.aliases.D1", "idltest.aliases.M1.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object M1 extends M1Circe {
  def apply(value: String) = Struct(value)
  final case class Struct(value: String) extends AnyVal with M1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("value")((v: Struct) => v.value)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("value")((d: String) => new Struct(d))
  }
  object Struct extends M1.StructCirce {
    def apply(m1: M1): M1.Struct = {
      assert(m1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new M1.Struct(value = m1.value)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[M1.Struct, M1.Struct] {
      override def convert(_value: M1.Struct): M1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M1.Struct(value = _value.value)
      }
    }
    implicit object Struct_upcast_M1 extends izumi.idealingua.runtime.IRTCast[M1.Struct, M1] {
      override def convert(_value: M1.Struct): M1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M1.Struct(value = _value.value)
      }
    }
    implicit class StructExtensions(override protected val _value: M1.Struct) extends izumi.idealingua.runtime.IRTConversions[M1.Struct]
  }
  implicit object M1_downcast_extend_D1 extends izumi.idealingua.runtime.IRTExtend[M1, D1] {
    class Call(private val _value: M1) extends AnyVal {
      def using(m2: idltest.aliases2.M2): D1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(m2.asInstanceOf[_root_.scala.AnyRef] ne null)
        D1(value = _value.value, f2 = m2.f2)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: M1): Call = new Call(_value)
  }
  implicit object M1_downcast_extend_M1Struct extends izumi.idealingua.runtime.IRTExtend[M1, M1.Struct] {
    class Call(private val _value: M1) extends AnyVal {
      def using(): M1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M1.Struct(value = _value.value)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: M1): Call = new Call(_value)
  }
  implicit object M1_upcast_M1 extends izumi.idealingua.runtime.IRTCast[M1, M1] {
    override def convert(_value: M1): M1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      M1.Struct(value = _value.value)
    }
  }
  implicit class M1Extensions(override protected val _value: M1) extends izumi.idealingua.runtime.IRTConversions[M1]
}
       