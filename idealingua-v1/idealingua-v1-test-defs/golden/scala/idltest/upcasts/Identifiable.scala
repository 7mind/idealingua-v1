package idltest.upcasts



trait Identifiable extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def id: java.util.UUID }

trait IdentifiableCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeIdentifiable: Encoder.AsObject[Identifiable] = Encoder.AsObject.instance {
    case v: Identifiable.Struct =>
      Map("idltest.upcasts.Identifiable.Struct" -> v).asJsonObject
  }
  implicit val decodeIdentifiable: Decoder[Identifiable] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.upcasts.Identifiable.Struct" =>
        value.as[Identifiable.Struct]
      case _ =>
        val cname = "idltest.upcasts.Identifiable"
        val alts = List("idltest.upcasts.Identifiable.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object Identifiable extends IdentifiableCirce {
  def apply(id: java.util.UUID) = Struct(id)
  final case class Struct(id: java.util.UUID) extends AnyVal with Identifiable
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, java.util.UUID]("id")((v: Struct) => v.id)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, java.util.UUID]("id")((d: java.util.UUID) => new Struct(d))
  }
  object Struct extends Identifiable.StructCirce {
    def apply(identifiable: Identifiable): Identifiable.Struct = {
      assert(identifiable.asInstanceOf[_root_.scala.AnyRef] ne null)
      new Identifiable.Struct(id = identifiable.id)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[Identifiable.Struct, Identifiable.Struct] {
      override def convert(_value: Identifiable.Struct): Identifiable.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Identifiable.Struct(id = _value.id)
      }
    }
    implicit object Struct_upcast_Identifiable extends izumi.idealingua.runtime.IRTCast[Identifiable.Struct, Identifiable] {
      override def convert(_value: Identifiable.Struct): Identifiable = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Identifiable.Struct(id = _value.id)
      }
    }
    implicit class StructExtensions(override protected val _value: Identifiable.Struct) extends izumi.idealingua.runtime.IRTConversions[Identifiable.Struct]
  }
  implicit object Identifiable_downcast_extend_Item extends izumi.idealingua.runtime.IRTExtend[Identifiable, Item] {
    class Call(private val _value: Identifiable) extends AnyVal {
      def using(name: String, price: Int): Item = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Item(id = _value.id, name = name, price = price)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Identifiable): Call = new Call(_value)
  }
  implicit object Identifiable_downcast_extend_IdentifiableStruct extends izumi.idealingua.runtime.IRTExtend[Identifiable, Identifiable.Struct] {
    class Call(private val _value: Identifiable) extends AnyVal {
      def using(): Identifiable.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Identifiable.Struct(id = _value.id)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: Identifiable): Call = new Call(_value)
  }
  implicit object Identifiable_upcast_Identifiable extends izumi.idealingua.runtime.IRTCast[Identifiable, Identifiable] {
    override def convert(_value: Identifiable): Identifiable = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Identifiable.Struct(id = _value.id)
    }
  }
  implicit class IdentifiableExtensions(override protected val _value: Identifiable) extends izumi.idealingua.runtime.IRTConversions[Identifiable]
}
       