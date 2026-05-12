package idltest.inheritance



trait Covariant extends Any with izumi.idealingua.runtime.model.IDLGeneratedType

trait CovariantCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeCovariant: Encoder.AsObject[Covariant] = Encoder.AsObject.instance {
    case v: Covariant.Struct =>
      Map("idltest.inheritance.Covariant.Struct" -> v).asJsonObject
    case v: CovariantA.Struct =>
      Map("idltest.inheritance.CovariantA.Struct" -> v).asJsonObject
    case v: CovariantB.Struct =>
      Map("idltest.inheritance.CovariantB.Struct" -> v).asJsonObject
  }
  implicit val decodeCovariant: Decoder[Covariant] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.Covariant.Struct" =>
        value.as[Covariant.Struct]
      case "idltest.inheritance.CovariantA.Struct" =>
        value.as[CovariantA.Struct]
      case "idltest.inheritance.CovariantB.Struct" =>
        value.as[CovariantB.Struct]
      case _ =>
        val cname = "idltest.inheritance.Covariant"
        val alts = List("idltest.inheritance.Covariant.Struct", "idltest.inheritance.CovariantA.Struct", "idltest.inheritance.CovariantB.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Covariant extends CovariantCirce {
  def apply() = Struct()
  final case class Struct() extends Covariant
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Covariant.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, DataWithAB] {
      override def convert(_value: Covariant.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, CovariantA.Struct] {
      override def convert(_value: Covariant.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, CovariantB.Struct] {
      override def convert(_value: Covariant.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, Empty.Struct] {
      override def convert(_value: Covariant.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, Notification.Struct] {
      override def convert(_value: Covariant.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, NotificationWithA.Struct] {
      override def convert(_value: Covariant.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, NotificationWithAB.Struct] {
      override def convert(_value: Covariant.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, NotificationWithB.Struct] {
      override def convert(_value: Covariant.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, Covariant.Struct] {
      override def convert(_value: Covariant.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_upcast_Covariant extends izumi.idealingua.runtime.IRTCast[Covariant.Struct, Covariant] {
      override def convert(_value: Covariant.Struct): Covariant = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: Covariant.Struct) extends izumi.idealingua.runtime.IRTConversions[Covariant.Struct]
  }
  implicit object Covariant_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[Covariant, DataWithAB] {
    override def convert(_value: Covariant): DataWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DataWithAB()
    }
  }
  implicit object Covariant_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[Covariant, Empty.Struct] {
    override def convert(_value: Covariant): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object Covariant_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[Covariant, Notification.Struct] {
    override def convert(_value: Covariant): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object Covariant_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[Covariant, NotificationWithA.Struct] {
    override def convert(_value: Covariant): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object Covariant_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[Covariant, NotificationWithAB.Struct] {
    override def convert(_value: Covariant): NotificationWithAB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithAB.Struct()
    }
  }
  implicit object Covariant_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[Covariant, NotificationWithB.Struct] {
    override def convert(_value: Covariant): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object Covariant_downcast_extend_CovariantStruct extends izumi.idealingua.runtime.IRTExtend[Covariant, Covariant.Struct] {
    class Call(private val _value: Covariant) extends AnyVal {
      def using(): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Covariant): Call = new Call(_value)
  }
  implicit object Covariant_downcast_extend_CovariantAStruct extends izumi.idealingua.runtime.IRTExtend[Covariant, CovariantA.Struct] {
    class Call(private val _value: Covariant) extends AnyVal {
      def using(): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Covariant): Call = new Call(_value)
  }
  implicit object Covariant_downcast_extend_CovariantBStruct extends izumi.idealingua.runtime.IRTExtend[Covariant, CovariantB.Struct] {
    class Call(private val _value: Covariant) extends AnyVal {
      def using(): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Covariant): Call = new Call(_value)
  }
  implicit object Covariant_upcast_Covariant extends izumi.idealingua.runtime.IRTCast[Covariant, Covariant] {
    override def convert(_value: Covariant): Covariant = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit class CovariantExtensions(override protected val _value: Covariant) extends izumi.idealingua.runtime.IRTConversions[Covariant]
}
       