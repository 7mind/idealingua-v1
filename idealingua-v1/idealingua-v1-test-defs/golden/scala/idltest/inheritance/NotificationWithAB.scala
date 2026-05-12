package idltest.inheritance



trait NotificationWithAB extends Any with izumi.idealingua.runtime.model.IDLGeneratedType with NotificationWithA with NotificationWithB

trait NotificationWithABCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotificationWithAB: Encoder.AsObject[NotificationWithAB] = Encoder.AsObject.instance {
    case v: NotificationWithAB.Struct =>
      Map("idltest.inheritance.NotificationWithAB.Struct" -> v).asJsonObject
  }
  implicit val decodeNotificationWithAB: Decoder[NotificationWithAB] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotificationWithAB.Struct" =>
        value.as[NotificationWithAB.Struct]
      case _ =>
        val cname = "idltest.inheritance.NotificationWithAB"
        val alts = List("idltest.inheritance.NotificationWithAB.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NotificationWithAB extends NotificationWithABCirce {
  def apply() = Struct()
  final case class Struct() extends NotificationWithAB
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends NotificationWithAB.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, DataWithAB] {
      override def convert(_value: NotificationWithAB.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, Covariant.Struct] {
      override def convert(_value: NotificationWithAB.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, CovariantA.Struct] {
      override def convert(_value: NotificationWithAB.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, CovariantB.Struct] {
      override def convert(_value: NotificationWithAB.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, Empty.Struct] {
      override def convert(_value: NotificationWithAB.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, Notification.Struct] {
      override def convert(_value: NotificationWithAB.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, NotificationWithA.Struct] {
      override def convert(_value: NotificationWithAB.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, NotificationWithB.Struct] {
      override def convert(_value: NotificationWithAB.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, NotificationWithAB.Struct] {
      override def convert(_value: NotificationWithAB.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_upcast_NotificationWithAB extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, NotificationWithAB] {
      override def convert(_value: NotificationWithAB.Struct): NotificationWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_upcast_NotificationWithA extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, NotificationWithA] {
      override def convert(_value: NotificationWithAB.Struct): NotificationWithA = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_upcast_NotificationWithB extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, NotificationWithB] {
      override def convert(_value: NotificationWithAB.Struct): NotificationWithB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_Notification extends izumi.idealingua.runtime.IRTCast[NotificationWithAB.Struct, Notification] {
      override def convert(_value: NotificationWithAB.Struct): Notification = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: NotificationWithAB.Struct) extends izumi.idealingua.runtime.IRTConversions[NotificationWithAB.Struct]
  }
  implicit object NotificationWithAB_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, DataWithAB] {
    override def convert(_value: NotificationWithAB): DataWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DataWithAB()
    }
  }
  implicit object NotificationWithAB_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, Covariant.Struct] {
    override def convert(_value: NotificationWithAB): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object NotificationWithAB_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, CovariantA.Struct] {
    override def convert(_value: NotificationWithAB): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object NotificationWithAB_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, CovariantB.Struct] {
    override def convert(_value: NotificationWithAB): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object NotificationWithAB_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, Empty.Struct] {
    override def convert(_value: NotificationWithAB): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object NotificationWithAB_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, Notification.Struct] {
    override def convert(_value: NotificationWithAB): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object NotificationWithAB_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, NotificationWithA.Struct] {
    override def convert(_value: NotificationWithAB): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object NotificationWithAB_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, NotificationWithB.Struct] {
    override def convert(_value: NotificationWithAB): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object NotificationWithAB_downcast_extend_NotificationWithABStruct extends izumi.idealingua.runtime.IRTExtend[NotificationWithAB, NotificationWithAB.Struct] {
    class Call(private val _value: NotificationWithAB) extends AnyVal {
      def using(): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithAB): Call = new Call(_value)
  }
  implicit object NotificationWithAB_upcast_NotificationWithAB extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, NotificationWithAB] {
    override def convert(_value: NotificationWithAB): NotificationWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithAB.Struct()
    }
  }
  implicit object NotificationWithAB_upcast_NotificationWithA extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, NotificationWithA] {
    override def convert(_value: NotificationWithAB): NotificationWithA = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object NotificationWithAB_upcast_NotificationWithB extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, NotificationWithB] {
    override def convert(_value: NotificationWithAB): NotificationWithB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object NotificationWithAB_upcast_Notification extends izumi.idealingua.runtime.IRTCast[NotificationWithAB, Notification] {
    override def convert(_value: NotificationWithAB): Notification = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit class NotificationWithABExtensions(override protected val _value: NotificationWithAB) extends izumi.idealingua.runtime.IRTConversions[NotificationWithAB]
}
       