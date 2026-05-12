package idltest.inheritance



trait NotiBase extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def at: java.time.ZonedDateTime
  def userID: String
  def userName: Option[String]
  def message: Option[String]
}

trait NotiBaseCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotiBase: Encoder.AsObject[NotiBase] = Encoder.AsObject.instance {
    case v: NotiBase.Struct =>
      Map("idltest.inheritance.NotiBase.Struct" -> v).asJsonObject
    case v: NotiWithFile.Struct =>
      Map("idltest.inheritance.NotiWithFile.Struct" -> v).asJsonObject
    case v: NotiWithFileRevision.Struct =>
      Map("idltest.inheritance.NotiWithFileRevision.Struct" -> v).asJsonObject
  }
  implicit val decodeNotiBase: Decoder[NotiBase] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotiBase.Struct" =>
        value.as[NotiBase.Struct]
      case "idltest.inheritance.NotiWithFile.Struct" =>
        value.as[NotiWithFile.Struct]
      case "idltest.inheritance.NotiWithFileRevision.Struct" =>
        value.as[NotiWithFileRevision.Struct]
      case _ =>
        val cname = "idltest.inheritance.NotiBase"
        val alts = List("idltest.inheritance.NotiBase.Struct", "idltest.inheritance.NotiWithFile.Struct", "idltest.inheritance.NotiWithFileRevision.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NotiBase extends NotiBaseCirce {
  def apply(at: java.time.ZonedDateTime, userID: String, userName: Option[String], message: Option[String]) = Struct(at, userID, userName, message)
  final case class Struct(at: java.time.ZonedDateTime, userID: String, userName: Option[String], message: Option[String]) extends NotiBase
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends NotiBase.StructCirce {
    def apply(notibase: NotiBase): NotiBase.Struct = {
      assert(notibase.asInstanceOf[_root_.scala.AnyRef] ne null)
      new NotiBase.Struct(at = notibase.at, userID = notibase.userID, userName = notibase.userName, message = notibase.message)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NotiBase.Struct, NotiBase.Struct] {
      override def convert(_value: NotiBase.Struct): NotiBase.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
      }
    }
    implicit object Struct_upcast_NotiBase extends izumi.idealingua.runtime.IRTCast[NotiBase.Struct, NotiBase] {
      override def convert(_value: NotiBase.Struct): NotiBase = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
      }
    }
    implicit class StructExtensions(override protected val _value: NotiBase.Struct) extends izumi.idealingua.runtime.IRTConversions[NotiBase.Struct]
  }
  implicit object NotiBase_downcast_extend_NotiBaseStruct extends izumi.idealingua.runtime.IRTExtend[NotiBase, NotiBase.Struct] {
    class Call(private val _value: NotiBase) extends AnyVal {
      def using(): NotiBase.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotiBase): Call = new Call(_value)
  }
  implicit object NotiBase_downcast_extend_NotiWithFileStruct extends izumi.idealingua.runtime.IRTExtend[NotiBase, NotiWithFile.Struct] {
    class Call(private val _value: NotiBase) extends AnyVal {
      def using(fileID: Long, fileName: String): NotiWithFile.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFile.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = fileID, fileName = fileName)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotiBase): Call = new Call(_value)
  }
  implicit object NotiBase_downcast_extend_NotiWithFileRevisionStruct extends izumi.idealingua.runtime.IRTExtend[NotiBase, NotiWithFileRevision.Struct] {
    class Call(private val _value: NotiBase) extends AnyVal {
      def using(fileID: Long, fileName: String, fileRevision: Long): NotiWithFileRevision.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFileRevision.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = fileID, fileName = fileName, fileRevision = fileRevision)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotiBase): Call = new Call(_value)
  }
  implicit object NotiBase_upcast_NotiBase extends izumi.idealingua.runtime.IRTCast[NotiBase, NotiBase] {
    override def convert(_value: NotiBase): NotiBase = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
    }
  }
  implicit class NotiBaseExtensions(override protected val _value: NotiBase) extends izumi.idealingua.runtime.IRTConversions[NotiBase]
}
       