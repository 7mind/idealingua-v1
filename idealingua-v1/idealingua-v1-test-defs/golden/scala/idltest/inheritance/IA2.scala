package idltest.inheritance



trait IA2 extends Any with izumi.idealingua.runtime.model.IDLGeneratedType with IA1 { def Int: Int }

trait IA2Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIA2: Encoder.AsObject[IA2] = Encoder.AsObject.instance {
    case v: IA2.Struct =>
      Map("idltest.inheritance.IA2.Struct" -> v).asJsonObject
  }
  implicit val decodeIA2: Decoder[IA2] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.IA2.Struct" =>
        value.as[IA2.Struct]
      case _ =>
        val cname = "idltest.inheritance.IA2"
        val alts = List("idltest.inheritance.IA2.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object IA2 extends IA2Circe {
  def apply(Int: Int) = Struct(Int)
  final case class Struct(Int: Int) extends AnyVal with IA2
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, Int]("Int")((v: Struct) => v.Int)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, Int]("Int")((d: Int) => new Struct(d))
  }
  object Struct extends IA2.StructCirce {
    def apply(Int: Int): IA2.Struct = {
      new IA2.Struct(Int = Int)
    }
    implicit object Struct_cast_into_IA1Struct extends izumi.idealingua.runtime.IRTCast[IA2.Struct, IA1.Struct] {
      override def convert(_value: IA2.Struct): IA1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA1.Struct(Int = _value.Int)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[IA2.Struct, IA2.Struct] {
      override def convert(_value: IA2.Struct): IA2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA2.Struct(Int = _value.Int)
      }
    }
    implicit object Struct_upcast_IA2 extends izumi.idealingua.runtime.IRTCast[IA2.Struct, IA2] {
      override def convert(_value: IA2.Struct): IA2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA2.Struct(Int = _value.Int)
      }
    }
    implicit object Struct_upcast_IA1 extends izumi.idealingua.runtime.IRTCast[IA2.Struct, IA1] {
      override def convert(_value: IA2.Struct): IA1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA1.Struct(Int = _value.Int)
      }
    }
    implicit class StructExtensions(override protected val _value: IA2.Struct) extends izumi.idealingua.runtime.IRTConversions[IA2.Struct]
  }
  implicit object IA2_cast_into_IA1Struct extends izumi.idealingua.runtime.IRTCast[IA2, IA1.Struct] {
    override def convert(_value: IA2): IA1.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IA1.Struct(Int = _value.Int)
    }
  }
  implicit object IA2_downcast_extend_IA2Struct extends izumi.idealingua.runtime.IRTExtend[IA2, IA2.Struct] {
    class Call(private val _value: IA2) extends AnyVal {
      def using(Int: Int): IA2.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        IA2.Struct(Int = Int)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: IA2): Call = new Call(_value)
  }
  implicit object IA2_upcast_IA2 extends izumi.idealingua.runtime.IRTCast[IA2, IA2] {
    override def convert(_value: IA2): IA2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IA2.Struct(Int = _value.Int)
    }
  }
  implicit object IA2_upcast_IA1 extends izumi.idealingua.runtime.IRTCast[IA2, IA1] {
    override def convert(_value: IA2): IA1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      IA1.Struct(Int = _value.Int)
    }
  }
  implicit class IA2Extensions(override protected val _value: IA2) extends izumi.idealingua.runtime.IRTConversions[IA2]
}
       