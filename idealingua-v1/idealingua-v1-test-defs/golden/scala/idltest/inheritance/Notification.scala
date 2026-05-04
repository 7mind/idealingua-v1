package idltest.inheritance



trait Notification extends Any with izumi.idealingua.runtime.model.IDLGeneratedType

trait NotificationCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotification: Encoder.AsObject[Notification] = Encoder.AsObject.instance {
    case v: NotificationWithAB.Struct =>
      Map("idltest.inheritance.NotificationWithAB.Struct" -> v).asJsonObject
    case v: DataWithAB =>
      Map("idltest.inheritance.DataWithAB" -> v).asJsonObject
    case v: NotificationWithB.Struct =>
      Map("idltest.inheritance.NotificationWithB.Struct" -> v).asJsonObject
    case v: NotificationWithA.Struct =>
      Map("idltest.inheritance.NotificationWithA.Struct" -> v).asJsonObject
    case v: Notification.Struct =>
      Map("idltest.inheritance.Notification.Struct" -> v).asJsonObject
  }
  implicit val decodeNotification: Decoder[Notification] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotificationWithAB.Struct" =>
        value.as[NotificationWithAB.Struct]
      case "idltest.inheritance.DataWithAB" =>
        value.as[DataWithAB]
      case "idltest.inheritance.NotificationWithB.Struct" =>
        value.as[NotificationWithB.Struct]
      case "idltest.inheritance.NotificationWithA.Struct" =>
        value.as[NotificationWithA.Struct]
      case "idltest.inheritance.Notification.Struct" =>
        value.as[Notification.Struct]
      case _ =>
        val cname = "idltest.inheritance.Notification"
        val alts = List("idltest.inheritance.NotificationWithAB.Struct", "idltest.inheritance.DataWithAB", "idltest.inheritance.NotificationWithB.Struct", "idltest.inheritance.NotificationWithA.Struct", "idltest.inheritance.Notification.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Notification extends NotificationCirce {
  def apply() = Struct()
  final case class Struct() extends Notification
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Notification.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[Notification.Struct, DataWithAB] {
      override def convert(_value: Notification.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, NotificationWithB.Struct] {
      override def convert(_value: Notification.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, CovariantA.Struct] {
      override def convert(_value: Notification.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, Empty.Struct] {
      override def convert(_value: Notification.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, CovariantB.Struct] {
      override def convert(_value: Notification.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, NotificationWithAB.Struct] {
      override def convert(_value: Notification.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, NotificationWithA.Struct] {
      override def convert(_value: Notification.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, Covariant.Struct] {
      override def convert(_value: Notification.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Notification.Struct, Notification.Struct] {
      override def convert(_value: Notification.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_upcast_Notification extends izumi.idealingua.runtime.IRTCast[Notification.Struct, Notification] {
      override def convert(_value: Notification.Struct): Notification = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: Notification.Struct) extends izumi.idealingua.runtime.IRTConversions[Notification.Struct]
  }
  implicit object Notification_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[Notification, CovariantA.Struct] {
    override def convert(_value: Notification): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object Notification_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[Notification, Empty.Struct] {
    override def convert(_value: Notification): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object Notification_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[Notification, CovariantB.Struct] {
    override def convert(_value: Notification): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object Notification_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[Notification, Covariant.Struct] {
    override def convert(_value: Notification): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object Notification_downcast_extend_NotificationWithABStruct extends izumi.idealingua.runtime.IRTExtend[Notification, NotificationWithAB.Struct] {
    class Call(private val _value: Notification) extends AnyVal {
      def using(): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Notification): Call = new Call(_value)
  }
  implicit object Notification_downcast_extend_DataWithAB extends izumi.idealingua.runtime.IRTExtend[Notification, DataWithAB] {
    class Call(private val _value: Notification) extends AnyVal {
      def using(): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Notification): Call = new Call(_value)
  }
  implicit object Notification_downcast_extend_NotificationWithBStruct extends izumi.idealingua.runtime.IRTExtend[Notification, NotificationWithB.Struct] {
    class Call(private val _value: Notification) extends AnyVal {
      def using(): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Notification): Call = new Call(_value)
  }
  implicit object Notification_downcast_extend_NotificationWithAStruct extends izumi.idealingua.runtime.IRTExtend[Notification, NotificationWithA.Struct] {
    class Call(private val _value: Notification) extends AnyVal {
      def using(): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Notification): Call = new Call(_value)
  }
  implicit object Notification_downcast_extend_NotificationStruct extends izumi.idealingua.runtime.IRTExtend[Notification, Notification.Struct] {
    class Call(private val _value: Notification) extends AnyVal {
      def using(): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Notification): Call = new Call(_value)
  }
  implicit object Notification_upcast_Notification extends izumi.idealingua.runtime.IRTCast[Notification, Notification] {
    override def convert(_value: Notification): Notification = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit class NotificationExtensions(override protected val _value: Notification) extends izumi.idealingua.runtime.IRTConversions[Notification]
}
       