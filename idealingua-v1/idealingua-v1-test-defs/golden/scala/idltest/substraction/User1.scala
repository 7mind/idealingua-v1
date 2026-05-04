package idltest.substraction



trait User1 extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def name: String
  def id: String
  def pass: String
}

trait User1Circe {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeUser1: Encoder.AsObject[User1] = Encoder.AsObject.instance {
    case v: User1.Struct =>
      Map("idltest.substraction.User1.Struct" -> v).asJsonObject
  }
  implicit val decodeUser1: Decoder[User1] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.substraction.User1.Struct" =>
        value.as[User1.Struct]
      case _ =>
        val cname = "idltest.substraction.User1"
        val alts = List("idltest.substraction.User1.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object User1 extends User1Circe {
  def apply(name: String, id: String, pass: String) = Struct(name, id, pass)
  final case class Struct(name: String, id: String, pass: String) extends User1
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends User1.StructCirce {
    def apply(user1: User1): User1.Struct = {
      assert(user1.asInstanceOf[_root_.scala.AnyRef] ne null)
      new User1.Struct(name = user1.name, id = user1.id, pass = user1.pass)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[User1.Struct, User1.Struct] {
      override def convert(_value: User1.Struct): User1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        User1.Struct(name = _value.name, id = _value.id, pass = _value.pass)
      }
    }
    implicit object Struct_upcast_User1 extends izumi.idealingua.runtime.IRTCast[User1.Struct, User1] {
      override def convert(_value: User1.Struct): User1 = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        User1.Struct(name = _value.name, id = _value.id, pass = _value.pass)
      }
    }
    implicit class StructExtensions(override protected val _value: User1.Struct) extends izumi.idealingua.runtime.IRTConversions[User1.Struct]
  }
  implicit object User1_downcast_extend_User1Struct extends izumi.idealingua.runtime.IRTExtend[User1, User1.Struct] {
    class Call(private val _value: User1) extends AnyVal {
      def using(): User1.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        User1.Struct(name = _value.name, id = _value.id, pass = _value.pass)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: User1): Call = new Call(_value)
  }
  implicit object User1_upcast_User1 extends izumi.idealingua.runtime.IRTCast[User1, User1] {
    override def convert(_value: User1): User1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      User1.Struct(name = _value.name, id = _value.id, pass = _value.pass)
    }
  }
  implicit class User1Extensions(override protected val _value: User1) extends izumi.idealingua.runtime.IRTConversions[User1]
}
       