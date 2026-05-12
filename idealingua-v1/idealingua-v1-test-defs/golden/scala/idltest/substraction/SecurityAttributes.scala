package idltest.substraction



trait SecurityAttributes extends izumi.idealingua.runtime.model.IDLGeneratedType with PersonalAttributes {
  def ssn: String
  def password: String
}

trait SecurityAttributesCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeSecurityAttributes: Encoder.AsObject[SecurityAttributes] = Encoder.AsObject.instance {
    case v: SecurityAttributes.Struct =>
      Map("idltest.substraction.SecurityAttributes.Struct" -> v).asJsonObject
  }
  implicit val decodeSecurityAttributes: Decoder[SecurityAttributes] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.substraction.SecurityAttributes.Struct" =>
        value.as[SecurityAttributes.Struct]
      case _ =>
        val cname = "idltest.substraction.SecurityAttributes"
        val alts = List("idltest.substraction.SecurityAttributes.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object SecurityAttributes extends SecurityAttributesCirce {
  def apply(ssn: String, password: String) = Struct(ssn, password)
  final case class Struct(ssn: String, password: String) extends SecurityAttributes
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends SecurityAttributes.StructCirce {
    def apply(personalattributes: PersonalAttributes, securityattributes: SecurityAttributes): SecurityAttributes.Struct = {
      assert((securityattributes.asInstanceOf[_root_.scala.AnyRef] ne null) && (personalattributes.asInstanceOf[_root_.scala.AnyRef] ne null))
      new SecurityAttributes.Struct(ssn = personalattributes.ssn, password = securityattributes.password)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[SecurityAttributes.Struct, SecurityAttributes.Struct] {
      override def convert(_value: SecurityAttributes.Struct): SecurityAttributes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SecurityAttributes.Struct(ssn = _value.ssn, password = _value.password)
      }
    }
    implicit object Struct_upcast_SecurityAttributes extends izumi.idealingua.runtime.IRTCast[SecurityAttributes.Struct, SecurityAttributes] {
      override def convert(_value: SecurityAttributes.Struct): SecurityAttributes = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SecurityAttributes.Struct(ssn = _value.ssn, password = _value.password)
      }
    }
    implicit object Struct_upcast_PersonalAttributes extends izumi.idealingua.runtime.IRTCast[SecurityAttributes.Struct, PersonalAttributes] {
      override def convert(_value: SecurityAttributes.Struct): PersonalAttributes = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PersonalAttributes.Struct(ssn = _value.ssn)
      }
    }
    implicit class StructExtensions(override protected val _value: SecurityAttributes.Struct) extends izumi.idealingua.runtime.IRTConversions[SecurityAttributes.Struct]
  }
  implicit object SecurityAttributes_downcast_extend_PublicUser2 extends izumi.idealingua.runtime.IRTExtend[SecurityAttributes, PublicUser2] {
    class Call(private val _value: SecurityAttributes) extends AnyVal {
      def using(name: String): PublicUser2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PublicUser2(name = name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SecurityAttributes): Call = new Call(_value)
  }
  implicit object SecurityAttributes_downcast_extend_User2 extends izumi.idealingua.runtime.IRTExtend[SecurityAttributes, User2] {
    class Call(private val _value: SecurityAttributes) extends AnyVal {
      def using(name: String): User2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        User2(ssn = _value.ssn, password = _value.password, name = name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SecurityAttributes): Call = new Call(_value)
  }
  implicit object SecurityAttributes_downcast_extend_SecurityAttributesStruct extends izumi.idealingua.runtime.IRTExtend[SecurityAttributes, SecurityAttributes.Struct] {
    class Call(private val _value: SecurityAttributes) extends AnyVal {
      def using(): SecurityAttributes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        SecurityAttributes.Struct(ssn = _value.ssn, password = _value.password)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: SecurityAttributes): Call = new Call(_value)
  }
  implicit object SecurityAttributes_upcast_SecurityAttributes extends izumi.idealingua.runtime.IRTCast[SecurityAttributes, SecurityAttributes] {
    override def convert(_value: SecurityAttributes): SecurityAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SecurityAttributes.Struct(ssn = _value.ssn, password = _value.password)
    }
  }
  implicit object SecurityAttributes_upcast_PersonalAttributes extends izumi.idealingua.runtime.IRTCast[SecurityAttributes, PersonalAttributes] {
    override def convert(_value: SecurityAttributes): PersonalAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PersonalAttributes.Struct(ssn = _value.ssn)
    }
  }
  implicit class SecurityAttributesExtensions(override protected val _value: SecurityAttributes) extends izumi.idealingua.runtime.IRTConversions[SecurityAttributes]
}
       