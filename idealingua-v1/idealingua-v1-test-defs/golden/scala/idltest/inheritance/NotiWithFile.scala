package idltest.inheritance



trait NotiWithFile extends izumi.idealingua.runtime.model.IDLGeneratedType with NotiBase {
  def at: java.time.ZonedDateTime
  def userID: String
  def userName: Option[String]
  def message: Option[String]
  def fileID: Long
  def fileName: String
}

trait NotiWithFileCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeNotiWithFile: Encoder.AsObject[NotiWithFile] = Encoder.AsObject.instance {
    case v: NotiWithFile.Struct =>
      Map("idltest.inheritance.NotiWithFile.Struct" -> v).asJsonObject
    case v: NotiWithFileRevision.Struct =>
      Map("idltest.inheritance.NotiWithFileRevision.Struct" -> v).asJsonObject
  }
  implicit val decodeNotiWithFile: Decoder[NotiWithFile] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.inheritance.NotiWithFile.Struct" =>
        value.as[NotiWithFile.Struct]
      case "idltest.inheritance.NotiWithFileRevision.Struct" =>
        value.as[NotiWithFileRevision.Struct]
      case _ =>
        val cname = "idltest.inheritance.NotiWithFile"
        val alts = List("idltest.inheritance.NotiWithFile.Struct", "idltest.inheritance.NotiWithFileRevision.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object NotiWithFile extends NotiWithFileCirce {
  def apply(at: java.time.ZonedDateTime, userID: String, userName: Option[String], message: Option[String], fileID: Long, fileName: String) = Struct(at, userID, userName, message, fileID, fileName)
  final case class Struct(at: java.time.ZonedDateTime, userID: String, userName: Option[String], message: Option[String], fileID: Long, fileName: String) extends NotiWithFile
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends NotiWithFile.StructCirce {
    def apply(notibase: NotiBase, notiwithfile: NotiWithFile): NotiWithFile.Struct = {
      assert((notiwithfile.asInstanceOf[_root_.scala.AnyRef] ne null) && (notibase.asInstanceOf[_root_.scala.AnyRef] ne null))
      new NotiWithFile.Struct(at = notibase.at, userID = notibase.userID, userName = notibase.userName, message = notibase.message, fileID = notiwithfile.fileID, fileName = notiwithfile.fileName)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[NotiWithFile.Struct, NotiWithFile.Struct] {
      override def convert(_value: NotiWithFile.Struct): NotiWithFile.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFile.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName)
      }
    }
    implicit object Struct_upcast_NotiWithFile extends izumi.idealingua.runtime.IRTCast[NotiWithFile.Struct, NotiWithFile] {
      override def convert(_value: NotiWithFile.Struct): NotiWithFile = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFile.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName)
      }
    }
    implicit object Struct_upcast_NotiBase extends izumi.idealingua.runtime.IRTCast[NotiWithFile.Struct, NotiBase] {
      override def convert(_value: NotiWithFile.Struct): NotiBase = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
      }
    }
    implicit class StructExtensions(override protected val _value: NotiWithFile.Struct) extends izumi.idealingua.runtime.IRTConversions[NotiWithFile.Struct]
  }
  implicit object NotiWithFile_downcast_extend_NotiWithFileStruct extends izumi.idealingua.runtime.IRTExtend[NotiWithFile, NotiWithFile.Struct] {
    class Call(private val _value: NotiWithFile) extends AnyVal {
      def using(): NotiWithFile.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFile.Struct(fileName = _value.fileName, userID = _value.userID, message = _value.message, at = _value.at, userName = _value.userName, fileID = _value.fileID)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotiWithFile): Call = new Call(_value)
  }
  implicit object NotiWithFile_downcast_extend_NotiWithFileRevisionStruct extends izumi.idealingua.runtime.IRTExtend[NotiWithFile, NotiWithFileRevision.Struct] {
    class Call(private val _value: NotiWithFile) extends AnyVal {
      def using(notiwithfilerevision: NotiWithFileRevision): NotiWithFileRevision.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        assert(notiwithfilerevision.asInstanceOf[_root_.scala.AnyRef] ne null)
        NotiWithFileRevision.Struct(fileName = _value.fileName, userID = _value.userID, message = _value.message, at = _value.at, userName = _value.userName, fileID = _value.fileID, fileRevision = notiwithfilerevision.fileRevision)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: NotiWithFile): Call = new Call(_value)
  }
  implicit object NotiWithFile_upcast_NotiWithFile extends izumi.idealingua.runtime.IRTCast[NotiWithFile, NotiWithFile] {
    override def convert(_value: NotiWithFile): NotiWithFile = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotiWithFile.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message, fileID = _value.fileID, fileName = _value.fileName)
    }
  }
  implicit object NotiWithFile_upcast_NotiBase extends izumi.idealingua.runtime.IRTCast[NotiWithFile, NotiBase] {
    override def convert(_value: NotiWithFile): NotiBase = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      NotiBase.Struct(at = _value.at, userID = _value.userID, userName = _value.userName, message = _value.message)
    }
  }
  implicit class NotiWithFileExtensions(override protected val _value: NotiWithFile) extends izumi.idealingua.runtime.IRTConversions[NotiWithFile]
}
       