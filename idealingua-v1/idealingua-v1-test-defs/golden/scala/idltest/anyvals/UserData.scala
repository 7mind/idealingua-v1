package idltest.anyvals



trait UserData extends izumi.idealingua.runtime.model.IDLGeneratedType { def id: WithRecordId }

trait UserDataCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeUserData: Encoder.AsObject[UserData] = Encoder.AsObject.instance {
    case v: UserData.Struct =>
      Map("idltest.anyvals.UserData.Struct" -> v).asJsonObject
  }
  implicit val decodeUserData: Decoder[UserData] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.anyvals.UserData.Struct" =>
        value.as[UserData.Struct]
      case _ =>
        val cname = "idltest.anyvals.UserData"
        val alts = List("idltest.anyvals.UserData.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object UserData extends UserDataCirce {
  def apply(id: WithRecordId) = Struct(id)
  final case class Struct(id: WithRecordId) extends UserData
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends UserData.StructCirce {
    def apply(userdata: UserData): UserData.Struct = {
      assert(userdata.asInstanceOf[_root_.scala.AnyRef] ne null)
      new UserData.Struct(id = userdata.id)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[UserData.Struct, UserData.Struct] {
      override def convert(_value: UserData.Struct): UserData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        UserData.Struct(id = _value.id)
      }
    }
    implicit object Struct_upcast_UserData extends izumi.idealingua.runtime.IRTCast[UserData.Struct, UserData] {
      override def convert(_value: UserData.Struct): UserData = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        UserData.Struct(id = _value.id)
      }
    }
    implicit class StructExtensions(override protected val _value: UserData.Struct) extends izumi.idealingua.runtime.IRTConversions[UserData.Struct]
  }
  implicit object UserData_downcast_extend_UserDataStruct extends izumi.idealingua.runtime.IRTExtend[UserData, UserData.Struct] {
    class Call(private val _value: UserData) extends AnyVal {
      def using(): UserData.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        UserData.Struct(id = _value.id)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: UserData): Call = new Call(_value)
  }
  implicit object UserData_upcast_UserData extends izumi.idealingua.runtime.IRTCast[UserData, UserData] {
    override def convert(_value: UserData): UserData = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      UserData.Struct(id = _value.id)
    }
  }
  implicit class UserDataExtensions(override protected val _value: UserData) extends izumi.idealingua.runtime.IRTConversions[UserData]
}
       