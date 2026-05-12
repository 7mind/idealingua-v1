package idltest.clones



trait M2 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def value: String
  def str: String
  def i32: Int
}

trait M2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeM2: Encoder.AsObject[M2] = Encoder.AsObject.instance {
    case v: M2.Struct =>
      Map("idltest.clones.M2.Struct" -> v).asJsonObject
  }
  implicit val decodeM2: Decoder[M2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.clones.M2.Struct" =>
        value.as[M2.Struct]
      case _ =>
        val cname = "idltest.clones.M2"
        val alts = List("idltest.clones.M2.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object M2 extends M2Circe {
  def apply(value: String, str: String, i32: Int) = Struct(value, str, i32)
  final case class Struct(value: String, str: String, i32: Int) extends M2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends M2.StructCirce {
    def apply(m0: M0, m2: M2): M2.Struct = {
      assert((m2.asInstanceOf[_root_.scala.AnyRef] ne null) && (m0.asInstanceOf[_root_.scala.AnyRef] ne null))
      new M2.Struct(value = m0.value, str = m2.str, i32 = m2.i32)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[M2.Struct, M2.Struct] {
      override def convert(_value: M2.Struct): M2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M2.Struct(value = _value.value, str = _value.str, i32 = _value.i32)
      }
    }
    implicit object Struct_upcast_M2 extends izumi.idealingua.runtime.IRTCast[M2.Struct, M2] {
      override def convert(_value: M2.Struct): M2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M2.Struct(value = _value.value, str = _value.str, i32 = _value.i32)
      }
    }
    implicit class StructExtensions(override protected val _value: M2.Struct) extends izumi.idealingua.runtime.IRTConversions[M2.Struct]
  }
  implicit object M2_downcast_extend_M2Struct extends izumi.idealingua.runtime.IRTExtend[M2, M2.Struct] {
    class Call(private val _value: M2) extends AnyVal {
      def using(): M2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        M2.Struct(value = _value.value, str = _value.str, i32 = _value.i32)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: M2): Call = new Call(_value)
  }
  implicit object M2_upcast_M2 extends izumi.idealingua.runtime.IRTCast[M2, M2] {
    override def convert(_value: M2): M2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      M2.Struct(value = _value.value, str = _value.str, i32 = _value.i32)
    }
  }
  implicit object M2_upcast_M0 extends izumi.idealingua.runtime.IRTCast[M2, M0] {
    override def convert(_value: M2): M0 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      M0.Struct(value = _value.value)
    }
  }
  implicit class M2Extensions(override protected val _value: M2) extends izumi.idealingua.runtime.IRTConversions[M2]
}
       