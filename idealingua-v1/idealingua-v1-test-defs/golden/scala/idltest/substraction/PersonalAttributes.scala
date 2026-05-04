package idltest.substraction



trait PersonalAttributes extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def ssn: String }

trait PersonalAttributesCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodePersonalAttributes: Encoder.AsObject[PersonalAttributes] = Encoder.AsObject.instance {
    case v: PersonalAttributes.Struct =>
      Map("idltest.substraction.PersonalAttributes.Struct" -> v).asJsonObject
    case v: SecurityAttributes.Struct =>
      Map("idltest.substraction.SecurityAttributes.Struct" -> v).asJsonObject
  }
  implicit val decodePersonalAttributes: Decoder[PersonalAttributes] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.substraction.PersonalAttributes.Struct" =>
        value.as[PersonalAttributes.Struct]
      case "idltest.substraction.SecurityAttributes.Struct" =>
        value.as[SecurityAttributes.Struct]
      case _ =>
        val cname = "idltest.substraction.PersonalAttributes"
        val alts = List("idltest.substraction.PersonalAttributes.Struct", "idltest.substraction.SecurityAttributes.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object PersonalAttributes extends PersonalAttributesCirce {
  def apply(ssn: String) = Struct(ssn)
  final case class Struct(ssn: String) extends AnyVal with PersonalAttributes
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("ssn")((v: Struct) => v.ssn)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("ssn")((d: String) => new Struct(d))
  }
  object Struct extends PersonalAttributes.StructCirce {
    def apply(personalattributes: PersonalAttributes): PersonalAttributes.Struct = {
      assert(personalattributes.asInstanceOf[_root_.scala.AnyRef] ne null)
      new PersonalAttributes.Struct(ssn = personalattributes.ssn)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[PersonalAttributes.Struct, PersonalAttributes.Struct] {
      override def convert(_value: PersonalAttributes.Struct): PersonalAttributes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PersonalAttributes.Struct(ssn = _value.ssn)
      }
    }
    implicit object Struct_upcast_PersonalAttributes extends izumi.idealingua.runtime.IRTCast[PersonalAttributes.Struct, PersonalAttributes] {
      override def convert(_value: PersonalAttributes.Struct): PersonalAttributes = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PersonalAttributes.Struct(ssn = _value.ssn)
      }
    }
    implicit class StructExtensions(override protected val _value: PersonalAttributes.Struct) extends izumi.idealingua.runtime.IRTConversions[PersonalAttributes.Struct]
  }
  implicit object PersonalAttributes_downcast_extend_PersonalAttributesStruct extends izumi.idealingua.runtime.IRTExtend[PersonalAttributes, PersonalAttributes.Struct] {
    class Call(private val _value: PersonalAttributes) extends AnyVal {
      def using(): PersonalAttributes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        PersonalAttributes.Struct(ssn = _value.ssn)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PersonalAttributes): Call = new Call(_value)
  }
  implicit object PersonalAttributes_downcast_extend_PublicUser2 extends izumi.idealingua.runtime.IRTExtend[PersonalAttributes, PublicUser2] {
    class Call(private val _value: PersonalAttributes) extends AnyVal {
      def using(user2: User2.Defn): PublicUser2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(user2.asInstanceOf[_root_.scala.AnyRef] ne null)
        PublicUser2(name = user2.name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PersonalAttributes): Call = new Call(_value)
  }
  implicit object PersonalAttributes_downcast_extend_SecurityAttributesStruct extends izumi.idealingua.runtime.IRTExtend[PersonalAttributes, SecurityAttributes.Struct] {
    class Call(private val _value: PersonalAttributes) extends AnyVal {
      def using(securityattributes: SecurityAttributes): SecurityAttributes.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(securityattributes.asInstanceOf[_root_.scala.AnyRef] ne null)
        SecurityAttributes.Struct(ssn = _value.ssn, password = securityattributes.password)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PersonalAttributes): Call = new Call(_value)
  }
  implicit object PersonalAttributes_downcast_extend_User2 extends izumi.idealingua.runtime.IRTExtend[PersonalAttributes, User2] {
    class Call(private val _value: PersonalAttributes) extends AnyVal {
      def using(name: String, securityattributes: SecurityAttributes): User2 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(securityattributes.asInstanceOf[_root_.scala.AnyRef] ne null)
        User2(ssn = _value.ssn, name = name, password = securityattributes.password)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: PersonalAttributes): Call = new Call(_value)
  }
  implicit object PersonalAttributes_upcast_PersonalAttributes extends izumi.idealingua.runtime.IRTCast[PersonalAttributes, PersonalAttributes] {
    override def convert(_value: PersonalAttributes): PersonalAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PersonalAttributes.Struct(ssn = _value.ssn)
    }
  }
  implicit class PersonalAttributesExtensions(override protected val _value: PersonalAttributes) extends izumi.idealingua.runtime.IRTConversions[PersonalAttributes]
}
       