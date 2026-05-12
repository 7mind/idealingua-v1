package idltest.inheritance



trait CovariantA extends Any with izumi.idealingua.runtime.model.IDLGeneratedType with Covariant

trait CovariantACirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeCovariantA: Encoder.AsObject[CovariantA] = Encoder.AsObject.instance {
    case v: CovariantA.Struct =>
      Map("idltest.inheritance.CovariantA.Struct" -> v).asJsonObject
  }
  implicit val decodeCovariantA: Decoder[CovariantA] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.CovariantA.Struct" =>
        value.as[CovariantA.Struct]
      case _ =>
        val cname = "idltest.inheritance.CovariantA"
        val alts = List("idltest.inheritance.CovariantA.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object CovariantA extends CovariantACirce {
  def apply() = Struct()
  final case class Struct() extends CovariantA
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends CovariantA.StructCirce {
    implicit object Struct_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, DataWithAB] {
      override def convert(_value: CovariantA.Struct): DataWithAB = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        DataWithAB()
      }
    }
    implicit object Struct_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, Covariant.Struct] {
      override def convert(_value: CovariantA.Struct): Covariant.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit object Struct_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, CovariantB.Struct] {
      override def convert(_value: CovariantA.Struct): CovariantB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantB.Struct()
      }
    }
    implicit object Struct_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, Empty.Struct] {
      override def convert(_value: CovariantA.Struct): Empty.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Empty.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, Notification.Struct] {
      override def convert(_value: CovariantA.Struct): Notification.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Notification.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, NotificationWithA.Struct] {
      override def convert(_value: CovariantA.Struct): NotificationWithA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithA.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, NotificationWithAB.Struct] {
      override def convert(_value: CovariantA.Struct): NotificationWithAB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithAB.Struct()
      }
    }
    implicit object Struct_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, NotificationWithB.Struct] {
      override def convert(_value: CovariantA.Struct): NotificationWithB.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotificationWithB.Struct()
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, CovariantA.Struct] {
      override def convert(_value: CovariantA.Struct): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_upcast_CovariantA extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, CovariantA] {
      override def convert(_value: CovariantA.Struct): CovariantA = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    implicit object Struct_upcast_Covariant extends izumi.idealingua.runtime.IRTCast[CovariantA.Struct, Covariant] {
      override def convert(_value: CovariantA.Struct): Covariant = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Covariant.Struct()
      }
    }
    implicit class StructExtensions(override protected val _value: CovariantA.Struct) extends izumi.idealingua.runtime.IRTConversions[CovariantA.Struct]
  }
  implicit object CovariantA_cast_into_DataWithAB extends izumi.idealingua.runtime.IRTCast[CovariantA, DataWithAB] {
    override def convert(_value: CovariantA): DataWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DataWithAB()
    }
  }
  implicit object CovariantA_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, Covariant.Struct] {
    override def convert(_value: CovariantA): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object CovariantA_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, CovariantB.Struct] {
    override def convert(_value: CovariantA): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object CovariantA_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, Empty.Struct] {
    override def convert(_value: CovariantA): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object CovariantA_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, Notification.Struct] {
    override def convert(_value: CovariantA): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object CovariantA_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, NotificationWithA.Struct] {
    override def convert(_value: CovariantA): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object CovariantA_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, NotificationWithAB.Struct] {
    override def convert(_value: CovariantA): NotificationWithAB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithAB.Struct()
    }
  }
  implicit object CovariantA_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[CovariantA, NotificationWithB.Struct] {
    override def convert(_value: CovariantA): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object CovariantA_downcast_extend_CovariantAStruct extends izumi.idealingua.runtime.IRTExtend[CovariantA, CovariantA.Struct] {
    class Call(private val _value: CovariantA) extends AnyVal {
      def using(): CovariantA.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        CovariantA.Struct()
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: CovariantA): Call = new Call(_value)
  }
  implicit object CovariantA_upcast_CovariantA extends izumi.idealingua.runtime.IRTCast[CovariantA, CovariantA] {
    override def convert(_value: CovariantA): CovariantA = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object CovariantA_upcast_Covariant extends izumi.idealingua.runtime.IRTCast[CovariantA, Covariant] {
    override def convert(_value: CovariantA): Covariant = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit class CovariantAExtensions(override protected val _value: CovariantA) extends izumi.idealingua.runtime.IRTConversions[CovariantA]
}
       