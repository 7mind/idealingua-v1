package overlaytest.withoverlay



final case class User(id: java.util.UUID, attributes: OverlayUserAttributes) extends User.Defn

trait UserCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeUser: Encoder.AsObject[User] = deriveEncoder[User]
  implicit val decodeUser: Decoder[User] = deriveDecoder[User]
}

object User extends UserCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def id: java.util.UUID
    def attributes: OverlayUserAttributes
  }
  def apply(id: java.util.UUID, attributes: OverlayUserAttributes.Defn): User = {
    assert(attributes.asInstanceOf[_root_.scala.AnyRef] ne null)
    new User(id = id, attributes = OverlayUserAttributes(attributes))
  }
  def apply(defn: User.Defn): User = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new User(id = defn.id, attributes = defn.attributes)
  }
  implicit object User_upcast_User extends izumi.idealingua.runtime.IRTCast[User, User] {
    override def convert(_value: User): User = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      User(id = _value.id, attributes = _value.attributes)
    }
  }
  implicit class UserExtensions(override protected val _value: User) extends izumi.idealingua.runtime.IRTConversions[User]
}
       