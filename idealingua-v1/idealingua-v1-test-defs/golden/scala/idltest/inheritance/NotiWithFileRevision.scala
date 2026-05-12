package idltest.inheritance



trait NotiWithFileRevision extends izumi.idealingua.runtime.model.IDLGeneratedType with NotiWithFile {
  def at: java.time.ZonedDateTime
  def userID: String
  def userName: Option[String]
  def message: Option[String]
  def fileID: Long
  def fileName: String
  def fileRevision: Long
}

trait NotiWithFileRevisionCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotiWithFileRevision: Encoder.AsObject[NotiWithFileRevision] = Encoder.AsObject.instance {
    case v: NotiWithFileRevision.Struct =>
      Map("idltest.inheritance.NotiWithFileRevision.Struct" -> v).asJsonObject
  }
  implicit val decodeNotiWithFileRevision: Decoder[NotiWithFileRevision] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotiWithFileRevision.Struct" =>
        value.as[NotiWithFileRevision.Struct]
      case _ =>
        val cname = "idltest.inheritance.NotiWithFileRevision"
        val alts = List("idltest.inheritance.NotiWithFileRevision.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NotiWithFileRevision extends NotiWithFileRevisionCirce {
  def apply(at: java.time.ZonedDateTime, userID: String, userName: Option[String], message: Option[String], fileID: Long, fileName: String, fileRevision: Long) = Struct(at, userID, userName, message, fileID, fileName, fileRevision)
  final case class Struct(at: java.time.ZonedDateTime, userID: String, userName: Option[String], message: Option[String], fileID: Long, fileName: String, fileRevision: Long) extends NotiWithFileRevision
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends NotiWithFileRevision.StructCirce {
    def apply(notibase: NotiBase, notiwithfile: NotiWithFile, notiwithfilerevision: NotiWithFileRevision): NotiWithFileRevision.Struct = {
      assert((notiwithfilerevision.asInstanceOf[_root_.scala.AnyRef] ne null) && ((notiwithfile.asInstanceOf[_root_.scala.AnyRef] ne null) && (notibase.asInstanceOf[_root_.scala.AnyRef] ne null)))
      new NotiWithFileRevision.Struct(at = notibase.at, userID = notibase.userID, userName = notibase.userName, message = notibase.message, fileID = notiwithfile.fileID, fileName = notiwithfile.fileName, fileRevision = notiwithfilerevision.fileRevision)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision.Struct, NotiWithFileRevision.Struct] {
      override def convert(_value: NotiWithFileRevision.Struct): NotiWithFileRevision.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFileRevision.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName, fileRevision = _value.fileRevision)
      }
    }
    implicit object Struct_upcast_NotiWithFileRevision extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision.Struct, NotiWithFileRevision] {
      override def convert(_value: NotiWithFileRevision.Struct): NotiWithFileRevision = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFileRevision.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName, fileRevision = _value.fileRevision)
      }
    }
    implicit object Struct_upcast_NotiWithFile extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision.Struct, NotiWithFile] {
      override def convert(_value: NotiWithFileRevision.Struct): NotiWithFile = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFile.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName)
      }
    }
    implicit object Struct_upcast_NotiBase extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision.Struct, NotiBase] {
      override def convert(_value: NotiWithFileRevision.Struct): NotiBase = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
      }
    }
    implicit class StructExtensions(override protected val _value: NotiWithFileRevision.Struct) extends izumi.idealingua.runtime.IRTConversions[NotiWithFileRevision.Struct]
  }
  implicit object NotiWithFileRevision_downcast_extend_NotiWithFileRevisionStruct extends izumi.idealingua.runtime.IRTExtend[NotiWithFileRevision, NotiWithFileRevision.Struct] {
    class Call(private val _value: NotiWithFileRevision) extends AnyVal {
      def using(): NotiWithFileRevision.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFileRevision.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName, fileRevision = _value.fileRevision)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotiWithFileRevision): Call = new Call(_value)
  }
  implicit object NotiWithFileRevision_upcast_NotiWithFileRevision extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision, NotiWithFileRevision] {
    override def convert(_value: NotiWithFileRevision): NotiWithFileRevision = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotiWithFileRevision.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName, fileRevision = _value.fileRevision)
    }
  }
  implicit object NotiWithFileRevision_upcast_NotiWithFile extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision, NotiWithFile] {
    override def convert(_value: NotiWithFileRevision): NotiWithFile = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotiWithFile.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName)
    }
  }
  implicit object NotiWithFileRevision_upcast_NotiBase extends izumi.idealingua.runtime.IRTCast[NotiWithFileRevision, NotiBase] {
    override def convert(_value: NotiWithFileRevision): NotiBase = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
    }
  }
  implicit class NotiWithFileRevisionExtensions(override protected val _value: NotiWithFileRevision) extends izumi.idealingua.runtime.IRTConversions[NotiWithFileRevision]
}
       