package idltest.upcasts



final case class Item(id: java.util.UUID, name: String, price: Int) extends Item.Defn

trait ItemCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeItem: Encoder.AsObject[Item] = deriveEncoder[Item]
  implicit val decodeItem: Decoder[Item] = deriveDecoder[Item]
}

object Item extends ItemCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def id: java.util.UUID
    def name: String
    def price: Int
  }
  def apply(identifiable: Identifiable, itemcontent: ItemContent, price: Int): Item = {
    assert((itemcontent.asInstanceOf[_root_.scala.AnyRef] ne null) && (identifiable.asInstanceOf[_root_.scala.AnyRef] ne null))
    new Item(id = identifiable.id, name = itemcontent.name, price = price)
  }
  def apply(defn: Item.Defn): Item = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Item(id = defn.id, name = defn.name, price = defn.price)
  }
  implicit object Item_upcast_Item extends izumi.idealingua.runtime.IRTCast[Item, Item] {
    override def convert(_value: Item): Item = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Item(id = _value.id, name = _value.name, price = _value.price)
    }
  }
  implicit object Item_upcast_ItemContent extends izumi.idealingua.runtime.IRTCast[Item, ItemContent] {
    override def convert(_value: Item): ItemContent = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ItemContent.Struct(id = _value.id, name = _value.name)
    }
  }
  implicit object Item_upcast_Identifiable extends izumi.idealingua.runtime.IRTCast[Item, Identifiable] {
    override def convert(_value: Item): Identifiable = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Identifiable.Struct(id = _value.id)
    }
  }
  implicit class ItemExtensions(override protected val _value: Item) extends izumi.idealingua.runtime.IRTConversions[Item]
}
       