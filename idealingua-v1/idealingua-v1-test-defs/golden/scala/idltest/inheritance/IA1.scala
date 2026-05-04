package idltest.inheritance



trait IA1 extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def Int: Int }

trait IA1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIA1: Encoder.AsObject[IA1] = Encoder.AsObject.instance {
    case v: IA2.Struct =>
      Map("idltest.inheritance.IA2.Struct" -> v).asJsonObject
    case v: IA1.Struct =>
      Map("idltest.inheritance.IA1.Struct" -> v).asJsonObject
  }
  implicit val decodeIA1: Decoder[IA1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.IA2.Struct" =>
        value.as[IA2.Struct]
      case "idltest.inheritance.IA1.Struct" =>
        value.as[IA1.Struct]
      case _ =>
        val cname = "idltest.inheritance.IA1"
        val alts = List("idltest.inheritance.IA2.Struct", "idltest.inheritance.IA1.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object IA1 extends IA1Circe {
  def apply(Int: Int) = Struct(Int)
  final case class Struct(Int: Int) extends AnyVal with IA1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Int]("Int")((v: Struct) => v.Int)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Int]("Int")((d: Int) => new Struct(d))
  }
  object Struct extends IA1.StructCirce {
    def apply(ia1: IA1): IA1.Struct = {
      assert(ia1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new IA1.Struct(Int = ia1.Int)
    }
    implicit object Struct_cast_into_IA2Struct extends izumi.idealingua.runtime.IRTCast[IA1.Struct, IA2.Struct] {
      override def convert(_value: IA1.Struct): IA2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA2.Struct(Int = _value.Int)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[IA1.Struct, IA1.Struct] {
      override def convert(_value: IA1.Struct): IA1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA1.Struct(Int = _value.Int)
      }
    }
    implicit object Struct_upcast_IA1 extends izumi.idealingua.runtime.IRTCast[IA1.Struct, IA1] {
      override def convert(_value: IA1.Struct): IA1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA1.Struct(Int = _value.Int)
      }
    }
    implicit class StructExtensions(override protected val _value: IA1.Struct) extends izumi.idealingua.runtime.IRTConversions[IA1.Struct]
  }
  implicit object IA1_downcast_extend_IA2Struct extends izumi.idealingua.runtime.IRTExtend[IA1, IA2.Struct] {
    class Call(private val _value: IA1) extends AnyVal {
      def using(Int: Int): IA2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA2.Struct(Int = Int)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IA1): Call = new Call(_value)
  }
  implicit object IA1_downcast_extend_IA1Struct extends izumi.idealingua.runtime.IRTExtend[IA1, IA1.Struct] {
    class Call(private val _value: IA1) extends AnyVal {
      def using(): IA1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA1.Struct(Int = _value.Int)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IA1): Call = new Call(_value)
  }
  implicit object IA1_upcast_IA1 extends izumi.idealingua.runtime.IRTCast[IA1, IA1] {
    override def convert(_value: IA1): IA1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IA1.Struct(Int = _value.Int)
    }
  }
  implicit class IA1Extensions(override protected val _value: IA1) extends izumi.idealingua.runtime.IRTConversions[IA1]
}
       