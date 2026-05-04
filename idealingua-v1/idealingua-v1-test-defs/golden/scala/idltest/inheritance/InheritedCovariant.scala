package idltest.inheritance



trait InheritedCovariant extends izumi.idealingua.runtime.model.IDLGeneratedType with WithCovariance { def field: CovariantA }

trait InheritedCovariantCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeInheritedCovariant: Encoder.AsObject[InheritedCovariant] = Encoder.AsObject.instance {
    case v: CovariantDTO2 =>
      Map("idltest.inheritance.CovariantDTO2" -> v).asJsonObject
    case v: InheritedCovariant.Struct =>
      Map("idltest.inheritance.InheritedCovariant.Struct" -> v).asJsonObject
  }
  implicit val decodeInheritedCovariant: Decoder[InheritedCovariant] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.CovariantDTO2" =>
        value.as[CovariantDTO2]
      case "idltest.inheritance.InheritedCovariant.Struct" =>
        value.as[InheritedCovariant.Struct]
      case _ =>
        val cname = "idltest.inheritance.InheritedCovariant"
        val alts = List("idltest.inheritance.CovariantDTO2", "idltest.inheritance.InheritedCovariant.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object InheritedCovariant extends InheritedCovariantCirce {
  def apply(field: CovariantA) = Struct(field)
  final case class Struct(field: CovariantA) extends InheritedCovariant
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends InheritedCovariant.StructCirce {
    def apply(field: CovariantA): InheritedCovariant.Struct = {
      assert(field.asInstanceOf[_root_.scala.AnyRef] ne null)
      new InheritedCovariant.Struct(field = field)
    }
    implicit object Struct_cast_into_CovariantDTO2 extends izumi.idealingua.runtime.IRTCast[InheritedCovariant.Struct, CovariantDTO2] {
      override def convert(_value: InheritedCovariant.Struct): CovariantDTO2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantDTO2(field = _value.field)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[InheritedCovariant.Struct, InheritedCovariant.Struct] {
      override def convert(_value: InheritedCovariant.Struct): InheritedCovariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        InheritedCovariant.Struct(field = _value.field)
      }
    }
    implicit object Struct_upcast_InheritedCovariant extends izumi.idealingua.runtime.IRTCast[InheritedCovariant.Struct, InheritedCovariant] {
      override def convert(_value: InheritedCovariant.Struct): InheritedCovariant = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        InheritedCovariant.Struct(field = _value.field)
      }
    }
    implicit class StructExtensions(override protected val _value: InheritedCovariant.Struct) extends izumi.idealingua.runtime.IRTConversions[InheritedCovariant.Struct]
  }
  implicit object InheritedCovariant_downcast_extend_CovariantDTO2 extends izumi.idealingua.runtime.IRTExtend[InheritedCovariant, CovariantDTO2] {
    class Call(private val _value: InheritedCovariant) extends AnyVal {
      def using(field: CovariantA): CovariantDTO2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(field.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantDTO2(field = field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: InheritedCovariant): Call = new Call(_value)
  }
  implicit object InheritedCovariant_downcast_extend_InheritedCovariantStruct extends izumi.idealingua.runtime.IRTExtend[InheritedCovariant, InheritedCovariant.Struct] {
    class Call(private val _value: InheritedCovariant) extends AnyVal {
      def using(field: CovariantA): InheritedCovariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(field.asInstanceOf[_root_.scala.AnyRef] ne null)
        InheritedCovariant.Struct(field = field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: InheritedCovariant): Call = new Call(_value)
  }
  implicit object InheritedCovariant_upcast_InheritedCovariant extends izumi.idealingua.runtime.IRTCast[InheritedCovariant, InheritedCovariant] {
    override def convert(_value: InheritedCovariant): InheritedCovariant = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      InheritedCovariant.Struct(field = _value.field)
    }
  }
  implicit class InheritedCovariantExtensions(override protected val _value: InheritedCovariant) extends izumi.idealingua.runtime.IRTConversions[InheritedCovariant]
}
       