package idltest.inheritance



trait CovariantB extends Any with izumi.idealingua.runtime.model.IDLGeneratedType with Covariant

trait CovariantBCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeCovariantB: Encoder.AsObject[CovariantB] = Encoder.AsObject.instance {
    case v: CovariantB.Struct =>
      Map("idltest.inheritance.CovariantB.Struct" -> v).asJsonObject
  }
  implicit val decodeCovariantB: Decoder[CovariantB] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.CovariantB.Struct" =>
        value.as[CovariantB.Struct]
      case _ =>
        val cname = "idltest.inheritance.CovariantB"
        val alts = List("idltest.inheritance.CovariantB.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object CovariantB extends CovariantBCirce {
  def apply() = Struct()
  final case class Struct() extends CovariantB
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends CovariantB.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, DataWithAB] {
      override def convert(_value: CovariantB.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, Covariant.Struct] {
      override def convert(_value: CovariantB.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, CovariantA.Struct] {
      override def convert(_value: CovariantB.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, Empty.Struct] {
      override def convert(_value: CovariantB.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, Notification.Struct] {
      override def convert(_value: CovariantB.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, NotificationWithA.Struct] {
      override def convert(_value: CovariantB.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, NotificationWithAB.Struct] {
      override def convert(_value: CovariantB.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, NotificationWithB.Struct] {
      override def convert(_value: CovariantB.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, CovariantB.Struct] {
      override def convert(_value: CovariantB.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_upcast_CovariantB extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, CovariantB] {
      override def convert(_value: CovariantB.Struct): CovariantB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_upcast_Covariant extends izumi.idealingua.runtime.IRTCast[CovariantB.Struct, Covariant] {
      override def convert(_value: CovariantB.Struct): Covariant = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: CovariantB.Struct) extends izumi.idealingua.runtime.IRTConversions[CovariantB.Struct]
  }
  implicit object CovariantB_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[CovariantB, DataWithAB] {
    override def convert(_value: CovariantB): DataWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DataWithAB()
    }
  }
  implicit object CovariantB_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, Covariant.Struct] {
    override def convert(_value: CovariantB): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object CovariantB_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, CovariantA.Struct] {
    override def convert(_value: CovariantB): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object CovariantB_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, Empty.Struct] {
    override def convert(_value: CovariantB): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object CovariantB_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, Notification.Struct] {
    override def convert(_value: CovariantB): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object CovariantB_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, NotificationWithA.Struct] {
    override def convert(_value: CovariantB): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object CovariantB_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, NotificationWithAB.Struct] {
    override def convert(_value: CovariantB): NotificationWithAB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithAB.Struct()
    }
  }
  implicit object CovariantB_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[CovariantB, NotificationWithB.Struct] {
    override def convert(_value: CovariantB): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object CovariantB_downcast_extend_CovariantBStruct extends izumi.idealingua.runtime.IRTExtend[CovariantB, CovariantB.Struct] {
    class Call(private val _value: CovariantB) extends AnyVal {
      def using(): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CovariantB): Call = new Call(_value)
  }
  implicit object CovariantB_upcast_CovariantB extends izumi.idealingua.runtime.IRTCast[CovariantB, CovariantB] {
    override def convert(_value: CovariantB): CovariantB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object CovariantB_upcast_Covariant extends izumi.idealingua.runtime.IRTCast[CovariantB, Covariant] {
    override def convert(_value: CovariantB): Covariant = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit class CovariantBExtensions(override protected val _value: CovariantB) extends izumi.idealingua.runtime.IRTConversions[CovariantB]
}
       