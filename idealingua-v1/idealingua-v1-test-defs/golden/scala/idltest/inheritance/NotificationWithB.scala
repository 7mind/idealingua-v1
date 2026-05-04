package idltest.inheritance



trait NotificationWithB extends Any with izumi.idealingua.runtime.model.IDLGeneratedType with Notification

trait NotificationWithBCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotificationWithB: Encoder.AsObject[NotificationWithB] = Encoder.AsObject.instance {
    case v: NotificationWithAB.Struct =>
      Map("idltest.inheritance.NotificationWithAB.Struct" -> v).asJsonObject
    case v: DataWithAB =>
      Map("idltest.inheritance.DataWithAB" -> v).asJsonObject
    case v: NotificationWithB.Struct =>
      Map("idltest.inheritance.NotificationWithB.Struct" -> v).asJsonObject
  }
  implicit val decodeNotificationWithB: Decoder[NotificationWithB] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotificationWithAB.Struct" =>
        value.as[NotificationWithAB.Struct]
      case "idltest.inheritance.DataWithAB" =>
        value.as[DataWithAB]
      case "idltest.inheritance.NotificationWithB.Struct" =>
        value.as[NotificationWithB.Struct]
      case _ =>
        val cname = "idltest.inheritance.NotificationWithB"
        val alts = List("idltest.inheritance.NotificationWithAB.Struct", "idltest.inheritance.DataWithAB", "idltest.inheritance.NotificationWithB.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NotificationWithB extends NotificationWithBCirce {
  def apply() = Struct()
  final case class Struct() extends NotificationWithB
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends NotificationWithB.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, DataWithAB] {
      override def convert(_value: NotificationWithB.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, CovariantA.Struct] {
      override def convert(_value: NotificationWithB.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, Empty.Struct] {
      override def convert(_value: NotificationWithB.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, CovariantB.Struct] {
      override def convert(_value: NotificationWithB.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, NotificationWithAB.Struct] {
      override def convert(_value: NotificationWithB.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, NotificationWithA.Struct] {
      override def convert(_value: NotificationWithB.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, Covariant.Struct] {
      override def convert(_value: NotificationWithB.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, Notification.Struct] {
      override def convert(_value: NotificationWithB.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, NotificationWithB.Struct] {
      override def convert(_value: NotificationWithB.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_NotificationWithB extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, NotificationWithB] {
      override def convert(_value: NotificationWithB.Struct): NotificationWithB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_Notification extends izumi.idealingua.runtime.IRTCast[NotificationWithB.Struct, Notification] {
      override def convert(_value: NotificationWithB.Struct): Notification = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: NotificationWithB.Struct) extends izumi.idealingua.runtime.IRTConversions[NotificationWithB.Struct]
  }
  implicit object NotificationWithB_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB, CovariantA.Struct] {
    override def convert(_value: NotificationWithB): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object NotificationWithB_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB, Empty.Struct] {
    override def convert(_value: NotificationWithB): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object NotificationWithB_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB, CovariantB.Struct] {
    override def convert(_value: NotificationWithB): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object NotificationWithB_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB, NotificationWithA.Struct] {
    override def convert(_value: NotificationWithB): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object NotificationWithB_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB, Covariant.Struct] {
    override def convert(_value: NotificationWithB): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object NotificationWithB_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithB, Notification.Struct] {
    override def convert(_value: NotificationWithB): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object NotificationWithB_downcast_extend_NotificationWithABStruct extends izumi.idealingua.runtime.IRTExtend[NotificationWithB, NotificationWithAB.Struct] {
    class Call(private val _value: NotificationWithB) extends AnyVal {
      def using(): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithB): Call = new Call(_value)
  }
  implicit object NotificationWithB_downcast_extend_DataWithAB extends izumi.idealingua.runtime.IRTExtend[NotificationWithB, DataWithAB] {
    class Call(private val _value: NotificationWithB) extends AnyVal {
      def using(): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithB): Call = new Call(_value)
  }
  implicit object NotificationWithB_downcast_extend_NotificationWithBStruct extends izumi.idealingua.runtime.IRTExtend[NotificationWithB, NotificationWithB.Struct] {
    class Call(private val _value: NotificationWithB) extends AnyVal {
      def using(): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithB): Call = new Call(_value)
  }
  implicit object NotificationWithB_upcast_NotificationWithB extends izumi.idealingua.runtime.IRTCast[NotificationWithB, NotificationWithB] {
    override def convert(_value: NotificationWithB): NotificationWithB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object NotificationWithB_upcast_Notification extends izumi.idealingua.runtime.IRTCast[NotificationWithB, Notification] {
    override def convert(_value: NotificationWithB): Notification = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit class NotificationWithBExtensions(override protected val _value: NotificationWithB) extends izumi.idealingua.runtime.IRTConversions[NotificationWithB]
}
       