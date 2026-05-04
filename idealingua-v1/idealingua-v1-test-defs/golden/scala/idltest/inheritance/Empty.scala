package idltest.inheritance



trait Empty extends Any with izumi.idealingua.runtime.model.IDLGeneratedType

trait EmptyCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeEmpty: Encoder.AsObject[Empty] = Encoder.AsObject.instance {
    case v: Empty.Struct =>
      Map("idltest.inheritance.Empty.Struct" -> v).asJsonObject
    case v: Str =>
      Map("idltest.inheritance.Str" -> v).asJsonObject
  }
  implicit val decodeEmpty: Decoder[Empty] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.Empty.Struct" =>
        value.as[Empty.Struct]
      case "idltest.inheritance.Str" =>
        value.as[Str]
      case _ =>
        val cname = "idltest.inheritance.Empty"
        val alts = List("idltest.inheritance.Empty.Struct", "idltest.inheritance.Str").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Empty extends EmptyCirce {
  def apply() = Struct()
  final case class Struct() extends Empty
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Empty.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[Empty.Struct, DataWithAB] {
      override def convert(_value: Empty.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, NotificationWithB.Struct] {
      override def convert(_value: Empty.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, CovariantA.Struct] {
      override def convert(_value: Empty.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, CovariantB.Struct] {
      override def convert(_value: Empty.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, NotificationWithAB.Struct] {
      override def convert(_value: Empty.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, NotificationWithA.Struct] {
      override def convert(_value: Empty.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, Covariant.Struct] {
      override def convert(_value: Empty.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, Notification.Struct] {
      override def convert(_value: Empty.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Empty.Struct, Empty.Struct] {
      override def convert(_value: Empty.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_upcast_Empty extends izumi.idealingua.runtime.IRTCast[Empty.Struct, Empty] {
      override def convert(_value: Empty.Struct): Empty = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: Empty.Struct) extends izumi.idealingua.runtime.IRTConversions[Empty.Struct]
  }
  implicit object Empty_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[Empty, DataWithAB] {
    override def convert(_value: Empty): DataWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DataWithAB()
    }
  }
  implicit object Empty_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[Empty, NotificationWithB.Struct] {
    override def convert(_value: Empty): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object Empty_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[Empty, CovariantA.Struct] {
    override def convert(_value: Empty): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object Empty_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[Empty, CovariantB.Struct] {
    override def convert(_value: Empty): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object Empty_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[Empty, NotificationWithAB.Struct] {
    override def convert(_value: Empty): NotificationWithAB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithAB.Struct()
    }
  }
  implicit object Empty_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[Empty, NotificationWithA.Struct] {
    override def convert(_value: Empty): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object Empty_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[Empty, Covariant.Struct] {
    override def convert(_value: Empty): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object Empty_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[Empty, Notification.Struct] {
    override def convert(_value: Empty): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object Empty_downcast_extend_EmptyStruct extends izumi.idealingua.runtime.IRTExtend[Empty, Empty.Struct] {
    class Call(private val _value: Empty) extends AnyVal {
      def using(): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Empty): Call = new Call(_value)
  }
  implicit object Empty_downcast_extend_Str extends izumi.idealingua.runtime.IRTExtend[Empty, Str] {
    class Call(private val _value: Empty) extends AnyVal {
      def using(str: String): Str = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Str(str = str)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Empty): Call = new Call(_value)
  }
  implicit object Empty_upcast_Empty extends izumi.idealingua.runtime.IRTCast[Empty, Empty] {
    override def convert(_value: Empty): Empty = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit class EmptyExtensions(override protected val _value: Empty) extends izumi.idealingua.runtime.IRTConversions[Empty]
}
       