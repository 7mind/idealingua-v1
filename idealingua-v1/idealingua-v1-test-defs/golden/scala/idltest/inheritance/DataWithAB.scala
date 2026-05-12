package idltest.inheritance



final case class DataWithAB() extends NotificationWithA with NotificationWithB with DataWithAB.Defn

trait DataWithABCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeDataWithAB: Encoder.AsObject[DataWithAB] = deriveEncoder[DataWithAB]
  implicit val decodeDataWithAB: Decoder[DataWithAB] = deriveDecoder[DataWithAB]
}

object DataWithAB extends DataWithABCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType
  def apply(defn: DataWithAB.Defn): DataWithAB = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new DataWithAB()
  }
  implicit object DataWithAB_cast_into_CovariantStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, Covariant.Struct] {
    override def convert(_value: DataWithAB): Covariant.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Covariant.Struct()
    }
  }
  implicit object DataWithAB_cast_into_CovariantAStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, CovariantA.Struct] {
    override def convert(_value: DataWithAB): CovariantA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantA.Struct()
    }
  }
  implicit object DataWithAB_cast_into_CovariantBStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, CovariantB.Struct] {
    override def convert(_value: DataWithAB): CovariantB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      CovariantB.Struct()
    }
  }
  implicit object DataWithAB_cast_into_EmptyStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, Empty.Struct] {
    override def convert(_value: DataWithAB): Empty.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Empty.Struct()
    }
  }
  implicit object DataWithAB_cast_into_NotificationStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, Notification.Struct] {
    override def convert(_value: DataWithAB): Notification.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit object DataWithAB_cast_into_NotificationWithAStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, NotificationWithA.Struct] {
    override def convert(_value: DataWithAB): NotificationWithA.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object DataWithAB_cast_into_NotificationWithABStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, NotificationWithAB.Struct] {
    override def convert(_value: DataWithAB): NotificationWithAB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithAB.Struct()
    }
  }
  implicit object DataWithAB_cast_into_NotificationWithBStruct extends izumi.idealingua.runtime.IRTCast[DataWithAB, NotificationWithB.Struct] {
    override def convert(_value: DataWithAB): NotificationWithB.Struct = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object DataWithAB_upcast_DataWithAB extends izumi.idealingua.runtime.IRTCast[DataWithAB, DataWithAB] {
    override def convert(_value: DataWithAB): DataWithAB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      DataWithAB()
    }
  }
  implicit object DataWithAB_upcast_NotificationWithA extends izumi.idealingua.runtime.IRTCast[DataWithAB, NotificationWithA] {
    override def convert(_value: DataWithAB): NotificationWithA = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithA.Struct()
    }
  }
  implicit object DataWithAB_upcast_NotificationWithB extends izumi.idealingua.runtime.IRTCast[DataWithAB, NotificationWithB] {
    override def convert(_value: DataWithAB): NotificationWithB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotificationWithB.Struct()
    }
  }
  implicit object DataWithAB_upcast_Notification extends izumi.idealingua.runtime.IRTCast[DataWithAB, Notification] {
    override def convert(_value: DataWithAB): Notification = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Notification.Struct()
    }
  }
  implicit class DataWithABExtensions(override protected val _value: DataWithAB) extends izumi.idealingua.runtime.IRTConversions[DataWithAB]
}
       