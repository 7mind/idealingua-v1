package idltest.services



trait Request extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def firstName: String
  def secondName: String
}

trait RequestCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeRequest: Encoder.AsObject[Request] = Encoder.AsObject.instance {
    case v: Request.Struct =>
      Map("idltest.services.Request.Struct" -> v).asJsonObject
  }
  implicit val decodeRequest: Decoder[Request] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.services.Request.Struct" =>
        value.as[Request.Struct]
      case _ =>
        val cname = "idltest.services.Request"
        val alts = List("idltest.services.Request.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Request extends RequestCirce {
  def apply(firstName: String, secondName: String) = Struct(firstName, secondName)
  final case class Struct(firstName: String, secondName: String) extends Request
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends Request.StructCirce {
    def apply(request: Request): Request.Struct = {
      assert(request.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Request.Struct(firstName = request.firstName, secondName = request.secondName)
    }
    implicit object Struct_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.SimpleInput] {
      override def convert(_value: Request.Struct): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.SimpleOutput] {
      override def convert(_value: Request.Struct): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.GreetSingularOutInput] {
      override def convert(_value: Request.Struct): TestService.GreetSingularOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.GreetImplicitStructOutInput] {
      override def convert(_value: Request.Struct): TestService.GreetImplicitStructOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.GreetAlgebraicOutInput] {
      override def convert(_value: Request.Struct): TestService.GreetAlgebraicOutInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.GreetAlgebraicMultilineSyntaxInput] {
      override def convert(_value: Request.Struct): TestService.GreetAlgebraicMultilineSyntaxInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.AlternativeInput] {
      override def convert(_value: Request.Struct): TestService.AlternativeInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[Request.Struct, TestService.AlternativeSameInput] {
      override def convert(_value: Request.Struct): TestService.AlternativeSameInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Request.Struct, Request.Struct] {
      override def convert(_value: Request.Struct): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit object Struct_upcast_Request extends izumi.idealingua.runtime.IRTCast[Request.Struct, Request] {
      override def convert(_value: Request.Struct): Request = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
      }
    }
    implicit class StructExtensions(override protected val _value: Request.Struct) extends izumi.idealingua.runtime.IRTConversions[Request.Struct]
  }
  implicit object Request_cast_into_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.SimpleInput] {
    override def convert(_value: Request): TestService.SimpleInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.SimpleInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTCast[Request, TestService.SimpleOutput] {
    override def convert(_value: Request): TestService.SimpleOutput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.SimpleOutput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceGreetSingularOutInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.GreetSingularOutInput] {
    override def convert(_value: Request): TestService.GreetSingularOutInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.GreetSingularOutInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceGreetImplicitStructOutInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.GreetImplicitStructOutInput] {
    override def convert(_value: Request): TestService.GreetImplicitStructOutInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.GreetImplicitStructOutInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceGreetAlgebraicOutInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.GreetAlgebraicOutInput] {
    override def convert(_value: Request): TestService.GreetAlgebraicOutInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.GreetAlgebraicOutInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceGreetAlgebraicMultilineSyntaxInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.GreetAlgebraicMultilineSyntaxInput] {
    override def convert(_value: Request): TestService.GreetAlgebraicMultilineSyntaxInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.GreetAlgebraicMultilineSyntaxInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceAlternativeInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.AlternativeInput] {
    override def convert(_value: Request): TestService.AlternativeInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.AlternativeInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_cast_into_TestServiceAlternativeSameInput extends izumi.idealingua.runtime.IRTCast[Request, TestService.AlternativeSameInput] {
    override def convert(_value: Request): TestService.AlternativeSameInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestService.AlternativeSameInput(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit object Request_downcast_extend_TestServiceSimpleInput extends izumi.idealingua.runtime.IRTExtend[Request, TestService.SimpleInput] {
    class Call(private val _value: Request) extends AnyVal {
      def using(): TestService.SimpleInput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleInput(secondName = _value.secondName, firstName = _value.firstName)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Request): Call = new Call(_value)
  }
  implicit object Request_downcast_extend_RequestStruct extends izumi.idealingua.runtime.IRTExtend[Request, Request.Struct] {
    class Call(private val _value: Request) extends AnyVal {
      def using(): Request.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Request.Struct(secondName = _value.secondName, firstName = _value.firstName)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Request): Call = new Call(_value)
  }
  implicit object Request_downcast_extend_TestServiceSimpleOutput extends izumi.idealingua.runtime.IRTExtend[Request, TestService.SimpleOutput] {
    class Call(private val _value: Request) extends AnyVal {
      def using(): TestService.SimpleOutput = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        TestService.SimpleOutput(secondName = _value.secondName, firstName = _value.firstName)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Request): Call = new Call(_value)
  }
  implicit object Request_upcast_Request extends izumi.idealingua.runtime.IRTCast[Request, Request] {
    override def convert(_value: Request): Request = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Request.Struct(firstName = _value.firstName, secondName = _value.secondName)
    }
  }
  implicit class RequestExtensions(override protected val _value: Request) extends izumi.idealingua.runtime.IRTConversions[Request]
}
       