package idltest.anyvals



trait Test01MixinAnyVal extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: String }

trait Test01MixinAnyValCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeTest01MixinAnyVal: Encoder.AsObject[Test01MixinAnyVal] = Encoder.AsObject.instance {
    case v: Test01DataAnyVal1 =>
      Map("idltest.anyvals.Test01DataAnyVal1" -> v).asJsonObject
    case v: Test01MixinAnyVal.Struct =>
      Map("idltest.anyvals.Test01MixinAnyVal.Struct" -> v).asJsonObject
  }
  implicit val decodeTest01MixinAnyVal: Decoder[Test01MixinAnyVal] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.anyvals.Test01DataAnyVal1" =>
        value.as[Test01DataAnyVal1]
      case "idltest.anyvals.Test01MixinAnyVal.Struct" =>
        value.as[Test01MixinAnyVal.Struct]
      case _ =>
        val cname = "idltest.anyvals.Test01MixinAnyVal"
        val alts = List("idltest.anyvals.Test01DataAnyVal1", "idltest.anyvals.Test01MixinAnyVal.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Test01MixinAnyVal extends Test01MixinAnyValCirce {
  def apply(value: String) = Struct(value)
  final case class Struct(value: String) extends AnyVal with Test01MixinAnyVal
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("value")((v: Struct) => v.value)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("value")((d: String) => new Struct(d))
  }
  object Struct extends Test01MixinAnyVal.StructCirce {
    def apply(test01mixinanyval: Test01MixinAnyVal): Test01MixinAnyVal.Struct = {
      assert(test01mixinanyval.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Test01MixinAnyVal.Struct(value = test01mixinanyval.value)
    }
    implicit object Struct_cast_into_SimpleAnyValRecord extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal.Struct, SimpleAnyValRecord] {
      override def convert(_value: Test01MixinAnyVal.Struct): SimpleAnyValRecord = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SimpleAnyValRecord(value = _value.value)
      }
    }
    implicit object Struct_cast_into_Test00Data1AnyVal extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal.Struct, Test00Data1AnyVal] {
      override def convert(_value: Test01MixinAnyVal.Struct): Test00Data1AnyVal = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test00Data1AnyVal(value = _value.value)
      }
    }
    implicit object Struct_cast_into_Test02DtoAnyVal extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal.Struct, Test02DtoAnyVal] {
      override def convert(_value: Test01MixinAnyVal.Struct): Test02DtoAnyVal = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test02DtoAnyVal(value = _value.value)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal.Struct, Test01MixinAnyVal.Struct] {
      override def convert(_value: Test01MixinAnyVal.Struct): Test01MixinAnyVal.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test01MixinAnyVal.Struct(value = _value.value)
      }
    }
    implicit object Struct_upcast_Test01MixinAnyVal extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal.Struct, Test01MixinAnyVal] {
      override def convert(_value: Test01MixinAnyVal.Struct): Test01MixinAnyVal = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test01MixinAnyVal.Struct(value = _value.value)
      }
    }
    implicit class StructExtensions(override protected val _value: Test01MixinAnyVal.Struct) extends izumi.idealingua.runtime.IRTConversions[Test01MixinAnyVal.Struct]
  }
  implicit object Test01MixinAnyVal_cast_into_SimpleAnyValRecord extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal, SimpleAnyValRecord] {
    override def convert(_value: Test01MixinAnyVal): SimpleAnyValRecord = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SimpleAnyValRecord(value = _value.value)
    }
  }
  implicit object Test01MixinAnyVal_cast_into_Test00Data1AnyVal extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal, Test00Data1AnyVal] {
    override def convert(_value: Test01MixinAnyVal): Test00Data1AnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test00Data1AnyVal(value = _value.value)
    }
  }
  implicit object Test01MixinAnyVal_cast_into_Test02DtoAnyVal extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal, Test02DtoAnyVal] {
    override def convert(_value: Test01MixinAnyVal): Test02DtoAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test02DtoAnyVal(value = _value.value)
    }
  }
  implicit object Test01MixinAnyVal_downcast_extend_Test01DataAnyVal1 extends izumi.idealingua.runtime.IRTExtend[Test01MixinAnyVal, Test01DataAnyVal1] {
    class Call(private val _value: Test01MixinAnyVal) extends AnyVal {
      def using(someInt: Byte): Test01DataAnyVal1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test01DataAnyVal1(value = _value.value, someInt = someInt)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Test01MixinAnyVal): Call = new Call(_value)
  }
  implicit object Test01MixinAnyVal_downcast_extend_Test01DataAnyVal2 extends izumi.idealingua.runtime.IRTExtend[Test01MixinAnyVal, Test01DataAnyVal2] {
    class Call(private val _value: Test01MixinAnyVal) extends AnyVal {
      def using(someInt: Byte): Test01DataAnyVal2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test01DataAnyVal2(value = _value.value, someInt = someInt)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Test01MixinAnyVal): Call = new Call(_value)
  }
  implicit object Test01MixinAnyVal_downcast_extend_Test01MixinAnyValStruct extends izumi.idealingua.runtime.IRTExtend[Test01MixinAnyVal, Test01MixinAnyVal.Struct] {
    class Call(private val _value: Test01MixinAnyVal) extends AnyVal {
      def using(): Test01MixinAnyVal.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Test01MixinAnyVal.Struct(value = _value.value)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Test01MixinAnyVal): Call = new Call(_value)
  }
  implicit object Test01MixinAnyVal_upcast_Test01MixinAnyVal extends izumi.idealingua.runtime.IRTCast[Test01MixinAnyVal, Test01MixinAnyVal] {
    override def convert(_value: Test01MixinAnyVal): Test01MixinAnyVal = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Test01MixinAnyVal.Struct(value = _value.value)
    }
  }
  implicit class Test01MixinAnyValExtensions(override protected val _value: Test01MixinAnyVal) extends izumi.idealingua.runtime.IRTConversions[Test01MixinAnyVal]
}
       