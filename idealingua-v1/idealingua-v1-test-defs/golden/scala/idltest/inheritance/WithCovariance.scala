package idltest.inheritance



trait WithCovariance extends izumi.idealingua.runtime.model.IDLGeneratedType { def field: Covariant }

trait WithCovarianceCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeWithCovariance: Encoder.AsObject[WithCovariance] = Encoder.AsObject.instance {
    case v: WithCovariance.Struct =>
      Map("idltest.inheritance.WithCovariance.Struct" -> v).asJsonObject
    case v: CovariantDTO2 =>
      Map("idltest.inheritance.CovariantDTO2" -> v).asJsonObject
    case v: CovariantDTO1 =>
      Map("idltest.inheritance.CovariantDTO1" -> v).asJsonObject
    case v: InheritedCovariant.Struct =>
      Map("idltest.inheritance.InheritedCovariant.Struct" -> v).asJsonObject
  }
  implicit val decodeWithCovariance: Decoder[WithCovariance] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.WithCovariance.Struct" =>
        value.as[WithCovariance.Struct]
      case "idltest.inheritance.CovariantDTO2" =>
        value.as[CovariantDTO2]
      case "idltest.inheritance.CovariantDTO1" =>
        value.as[CovariantDTO1]
      case "idltest.inheritance.InheritedCovariant.Struct" =>
        value.as[InheritedCovariant.Struct]
      case _ =>
        val cname = "idltest.inheritance.WithCovariance"
        val alts = List("idltest.inheritance.WithCovariance.Struct", "idltest.inheritance.CovariantDTO2", "idltest.inheritance.CovariantDTO1", "idltest.inheritance.InheritedCovariant.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object WithCovariance extends WithCovarianceCirce {
  def apply(field: Covariant) = Struct(field)
  final case class Struct(field: Covariant) extends WithCovariance
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends WithCovariance.StructCirce {
    def apply(withcovariance: WithCovariance): WithCovariance.Struct = {
      assert(withcovariance.asInstanceOf[_root_.scala.AnyRef] ne null)
      new WithCovariance.Struct(field = withcovariance.field)
    }
    implicit object Struct_cast_into_CovariantDTO1 extends izumi.idealingua.runtime.IRTCast[WithCovariance.Struct, CovariantDTO1] {
      override def convert(_value: WithCovariance.Struct): CovariantDTO1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantDTO1(field = _value.field)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[WithCovariance.Struct, WithCovariance.Struct] {
      override def convert(_value: WithCovariance.Struct): WithCovariance.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WithCovariance.Struct(field = _value.field)
      }
    }
    implicit object Struct_upcast_WithCovariance extends izumi.idealingua.runtime.IRTCast[WithCovariance.Struct, WithCovariance] {
      override def convert(_value: WithCovariance.Struct): WithCovariance = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WithCovariance.Struct(field = _value.field)
      }
    }
    implicit class StructExtensions(override protected val _value: WithCovariance.Struct) extends izumi.idealingua.runtime.IRTConversions[WithCovariance.Struct]
  }
  implicit object WithCovariance_downcast_extend_WithCovarianceStruct extends izumi.idealingua.runtime.IRTExtend[WithCovariance, WithCovariance.Struct] {
    class Call(private val _value: WithCovariance) extends AnyVal {
      def using(): WithCovariance.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WithCovariance.Struct(field = _value.field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WithCovariance): Call = new Call(_value)
  }
  implicit object WithCovariance_downcast_extend_CovariantDTO2 extends izumi.idealingua.runtime.IRTExtend[WithCovariance, CovariantDTO2] {
    class Call(private val _value: WithCovariance) extends AnyVal {
      def using(field: CovariantA): CovariantDTO2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(field.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantDTO2(field = field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WithCovariance): Call = new Call(_value)
  }
  implicit object WithCovariance_downcast_extend_CovariantDTO1 extends izumi.idealingua.runtime.IRTExtend[WithCovariance, CovariantDTO1] {
    class Call(private val _value: WithCovariance) extends AnyVal {
      def using(): CovariantDTO1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantDTO1(field = _value.field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WithCovariance): Call = new Call(_value)
  }
  implicit object WithCovariance_downcast_extend_InheritedCovariantStruct extends izumi.idealingua.runtime.IRTExtend[WithCovariance, InheritedCovariant.Struct] {
    class Call(private val _value: WithCovariance) extends AnyVal {
      def using(field: CovariantA): InheritedCovariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(field.asInstanceOf[_root_.scala.AnyRef] ne null)
        InheritedCovariant.Struct(field = field)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WithCovariance): Call = new Call(_value)
  }
  implicit object WithCovariance_upcast_WithCovariance extends izumi.idealingua.runtime.IRTCast[WithCovariance, WithCovariance] {
    override def convert(_value: WithCovariance): WithCovariance = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WithCovariance.Struct(field = _value.field)
    }
  }
  implicit class WithCovarianceExtensions(override protected val _value: WithCovariance) extends izumi.idealingua.runtime.IRTConversions[WithCovariance]
}
       