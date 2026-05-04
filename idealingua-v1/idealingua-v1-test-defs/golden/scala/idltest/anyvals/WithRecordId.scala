package idltest.anyvals



trait WithRecordId extends izumi.idealingua.runtime.model.IDLGeneratedType { def id: RecordId }

trait WithRecordIdCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeWithRecordId: Encoder.AsObject[WithRecordId] = Encoder.AsObject.instance {
    case v: WithRecordId.Struct =>
      Map("idltest.anyvals.WithRecordId.Struct" -> v).asJsonObject
  }
  implicit val decodeWithRecordId: Decoder[WithRecordId] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.anyvals.WithRecordId.Struct" =>
        value.as[WithRecordId.Struct]
      case _ =>
        val cname = "idltest.anyvals.WithRecordId"
        val alts = List("idltest.anyvals.WithRecordId.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object WithRecordId extends WithRecordIdCirce {
  def apply(id: RecordId) = Struct(id)
  final case class Struct(id: RecordId) extends WithRecordId
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends WithRecordId.StructCirce {
    def apply(withrecordid: WithRecordId): WithRecordId.Struct = {
      assert(withrecordid.asInstanceOf[_root_.scala.AnyRef] ne null)
      new WithRecordId.Struct(id = withrecordid.id)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[WithRecordId.Struct, WithRecordId.Struct] {
      override def convert(_value: WithRecordId.Struct): WithRecordId.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WithRecordId.Struct(id = _value.id)
      }
    }
    implicit object Struct_upcast_WithRecordId extends izumi.idealingua.runtime.IRTCast[WithRecordId.Struct, WithRecordId] {
      override def convert(_value: WithRecordId.Struct): WithRecordId = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WithRecordId.Struct(id = _value.id)
      }
    }
    implicit class StructExtensions(override protected val _value: WithRecordId.Struct) extends izumi.idealingua.runtime.IRTConversions[WithRecordId.Struct]
  }
  implicit object WithRecordId_downcast_extend_WithRecordIdStruct extends izumi.idealingua.runtime.IRTExtend[WithRecordId, WithRecordId.Struct] {
    class Call(private val _value: WithRecordId) extends AnyVal {
      def using(): WithRecordId.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        WithRecordId.Struct(id = _value.id)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: WithRecordId): Call = new Call(_value)
  }
  implicit object WithRecordId_upcast_WithRecordId extends izumi.idealingua.runtime.IRTCast[WithRecordId, WithRecordId] {
    override def convert(_value: WithRecordId): WithRecordId = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      WithRecordId.Struct(id = _value.id)
    }
  }
  implicit class WithRecordIdExtensions(override protected val _value: WithRecordId) extends izumi.idealingua.runtime.IRTConversions[WithRecordId]
}
       