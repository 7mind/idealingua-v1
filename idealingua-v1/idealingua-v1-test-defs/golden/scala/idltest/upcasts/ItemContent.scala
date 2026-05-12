package idltest.upcasts



trait ItemContent extends izumi.idealingua.runtime.model.IDLGeneratedType {
  def id: java.util.UUID
  def name: String
}

trait ItemContentCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeItemContent: Encoder.AsObject[ItemContent] = Encoder.AsObject.instance {
    case v: ItemContent.Struct =>
      Map("idltest.upcasts.ItemContent.Struct" -> v).asJsonObject
  }
  implicit val decodeItemContent: Decoder[ItemContent] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "idltest.upcasts.ItemContent.Struct" =>
        value.as[ItemContent.Struct]
      case _ =>
        val cname = "idltest.upcasts.ItemContent"
        val alts = List("idltest.upcasts.ItemContent.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object ItemContent extends ItemContentCirce {
  def apply(id: java.util.UUID, name: String) = Struct(id, name)
  final case class Struct(id: java.util.UUID, name: String) extends ItemContent
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = deriveEncoder[Struct]
    implicit val decodeStruct: Decoder[Struct] = deriveDecoder[Struct]
  }
  object Struct extends ItemContent.StructCirce {
    def apply(identifiable: Identifiable, itemcontent: ItemContent): ItemContent.Struct = {
      assert((itemcontent.asInstanceOf[_root_.scala.AnyRef] ne null) && (identifiable.asInstanceOf[_root_.scala.AnyRef] ne null))
      new ItemContent.Struct(id = identifiable.id, name = itemcontent.name)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[ItemContent.Struct, ItemContent.Struct] {
      override def convert(_value: ItemContent.Struct): ItemContent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ItemContent.Struct(id = _value.id, name = _value.name)
      }
    }
    implicit object Struct_upcast_ItemContent extends izumi.idealingua.runtime.IRTCast[ItemContent.Struct, ItemContent] {
      override def convert(_value: ItemContent.Struct): ItemContent = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ItemContent.Struct(id = _value.id, name = _value.name)
      }
    }
    implicit class StructExtensions(override protected val _value: ItemContent.Struct) extends izumi.idealingua.runtime.IRTConversions[ItemContent.Struct]
  }
  implicit object ItemContent_downcast_extend_Item extends izumi.idealingua.runtime.IRTExtend[ItemContent, Item] {
    class Call(private val _value: ItemContent) extends AnyVal {
      def using(price: Int): Item = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        Item(id = _value.id, name = _value.name, price = price)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ItemContent): Call = new Call(_value)
  }
  implicit object ItemContent_downcast_extend_ItemContentStruct extends izumi.idealingua.runtime.IRTExtend[ItemContent, ItemContent.Struct] {
    class Call(private val _value: ItemContent) extends AnyVal {
      def using(): ItemContent.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        ItemContent.Struct(id = _value.id, name = _value.name)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: ItemContent): Call = new Call(_value)
  }
  implicit object ItemContent_upcast_ItemContent extends izumi.idealingua.runtime.IRTCast[ItemContent, ItemContent] {
    override def convert(_value: ItemContent): ItemContent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ItemContent.Struct(id = _value.id, name = _value.name)
    }
  }
  implicit object ItemContent_upcast_Identifiable extends izumi.idealingua.runtime.IRTCast[ItemContent, Identifiable] {
    override def convert(_value: ItemContent): Identifiable = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Identifiable.Struct(id = _value.id)
    }
  }
  implicit class ItemContentExtensions(override protected val _value: ItemContent) extends izumi.idealingua.runtime.IRTConversions[ItemContent]
}
       