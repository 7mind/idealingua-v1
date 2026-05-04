package idltest.inheritance



trait NotificationWithA extends Any with izumi.idealingua.runtime.model.IDLGeneratedType with Notification

trait NotificationWithACirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotificationWithA: Encoder.AsObject[NotificationWithA] = Encoder.AsObject.instance {
    case v: NotificationWithAB.Struct =>
      Map("idltest.inheritance.NotificationWithAB.Struct" -> v).asJsonObject
    case v: DataWithAB =>
      Map("idltest.inheritance.DataWithAB" -> v).asJsonObject
    case v: NotificationWithA.Struct =>
      Map("idltest.inheritance.NotificationWithA.Struct" -> v).asJsonObject
  }
  implicit val decodeNotificationWithA: Decoder[NotificationWithA] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotificationWithAB.Struct" =>
        value.as[NotificationWithAB.Struct]
      case "idltest.inheritance.DataWithAB" =>
        value.as[DataWithAB]
      case "idltest.inheritance.NotificationWithA.Struct" =>
        value.as[NotificationWithA.Struct]
      case _ =>
        val cname = "idltest.inheritance.NotificationWithA"
        val alts = List("idltest.inheritance.NotificationWithAB.Struct", "idltest.inheritance.DataWithAB", "idltest.inheritance.NotificationWithA.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NotificationWithA extends NotificationWithACirce {
  def apply() = Struct()
  final case class Struct() extends NotificationWithA
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends NotificationWithA.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, DataWithAB] {
      override def convert(_value: NotificationWithA.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, NotificationWithB.Struct] {
      override def convert(_value: NotificationWithA.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, CovariantA.Struct] {
      override def convert(_value: NotificationWithA.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, Empty.Struct] {
      override def convert(_value: NotificationWithA.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, CovariantB.Struct] {
      override def convert(_value: NotificationWithA.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, NotificationWithAB.Struct] {
      override def convert(_value: NotificationWithA.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, Covariant.Struct] {
      override def convert(_value: NotificationWithA.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, Notification.Struct] {
      override def convert(_value: NotificationWithA.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, NotificationWithA.Struct] {
      override def convert(_value: NotificationWithA.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_upcast_NotificationWithA extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, NotificationWithA] {
      override def convert(_value: NotificationWithA.Struct): NotificationWithA = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_upcast_Notification extends izumi.idealingua.runtime.IRTCast[NotificationWithA.Struct, Notification] {
      override def convert(_value: NotificationWithA.Struct): Notification = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: NotificationWithA.Struct) extends izumi.idealingua.runtime.IRTConversions[NotificationWithA.Struct]
  }
  implicit object NotificationWithA_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA, NotificationWithB.Struct] {
    override def convert(_value: NotificationWithA): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object NotificationWithA_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA, CovariantA.Struct] {
    override def convert(_value: NotificationWithA): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object NotificationWithA_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA, Empty.Struct] {
    override def convert(_value: NotificationWithA): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object NotificationWithA_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA, CovariantB.Struct] {
    override def convert(_value: NotificationWithA): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object NotificationWithA_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA, Covariant.Struct] {
    override def convert(_value: NotificationWithA): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object NotificationWithA_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[NotificationWithA, Notification.Struct] {
    override def convert(_value: NotificationWithA): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object NotificationWithA_downcast_extend_NotificationWithABStruct extends izumi.idealingua.runtime.IRTExtend[NotificationWithA, NotificationWithAB.Struct] {
    class Call(private val _value: NotificationWithA) extends AnyVal {
      def using(): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithA): Call = new Call(_value)
  }
  implicit object NotificationWithA_downcast_extend_DataWithAB extends izumi.idealingua.runtime.IRTExtend[NotificationWithA, DataWithAB] {
    class Call(private val _value: NotificationWithA) extends AnyVal {
      def using(): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithA): Call = new Call(_value)
  }
  implicit object NotificationWithA_downcast_extend_NotificationWithAStruct extends izumi.idealingua.runtime.IRTExtend[NotificationWithA, NotificationWithA.Struct] {
    class Call(private val _value: NotificationWithA) extends AnyVal {
      def using(): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotificationWithA): Call = new Call(_value)
  }
  implicit object NotificationWithA_upcast_NotificationWithA extends izumi.idealingua.runtime.IRTCast[NotificationWithA, NotificationWithA] {
    override def convert(_value: NotificationWithA): NotificationWithA = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object NotificationWithA_upcast_Notification extends izumi.idealingua.runtime.IRTCast[NotificationWithA, Notification] {
    override def convert(_value: NotificationWithA): Notification = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit class NotificationWithAExtensions(override protected val _value: NotificationWithA) extends izumi.idealingua.runtime.IRTConversions[NotificationWithA]
}
       