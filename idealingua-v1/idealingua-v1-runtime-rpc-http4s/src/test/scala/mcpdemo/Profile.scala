package mcpdemo



final case class Profile(name: String, age: Int, color: Color) extends Profile.Defn

trait ProfileCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeProfile: Encoder.AsObject[Profile] = deriveEncoder[Profile]
  implicit val decodeProfile: Decoder[Profile] = deriveDecoder[Profile]
}

object Profile extends ProfileCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def name: String
    def age: Int
    def color: Color
  }
  def apply(name: String, age: Int, color: Color): Profile = {
    assert(color.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Profile(name = name, age = age, color = color)
  }
  def apply(defn: Profile.Defn): Profile = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Profile(name = defn.name, age = defn.age, color = defn.color)
  }
  implicit object Profile_cast_into_ShapesMakeProfileInput extends izumi.idealingua.runtime.IRTCast[Profile, Shapes.MakeProfileInput] {
    override def convert(_value: Profile): Shapes.MakeProfileInput = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Shapes.MakeProfileInput(name = _value.name, age = _value.age, color = _value.color)
    }
  }
  implicit object Profile_upcast_Profile extends izumi.idealingua.runtime.IRTCast[Profile, Profile] {
    override def convert(_value: Profile): Profile = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Profile(name = _value.name, age = _value.age, color = _value.color)
    }
  }
  implicit class ProfileExtensions(override protected val _value: Profile) extends izumi.idealingua.runtime.IRTConversions[Profile]
}
       