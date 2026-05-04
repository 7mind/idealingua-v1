package idltest.substraction



final case class User2(ssn: String, password: String, name: String) extends User2.Defn

trait User2Circe extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeUser2: Encoder.AsObject[User2] = deriveEncoder[User2]
  implicit val decodeUser2: Decoder[User2] = deriveDecoder[User2]
}

object User2 extends User2Circe {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def ssn: String
    def password: String
    def name: String
  }
  def apply(personalattributes: PersonalAttributes, securityattributes: SecurityAttributes, name: String): User2 = {
    assert((securityattributes.asInstanceOf[_root_.scala.AnyRef] ne null) && (personalattributes.asInstanceOf[_root_.scala.AnyRef] ne null))
    new User2(ssn = personalattributes.ssn, password = securityattributes.password, name = name)
  }
  def apply(defn: User2.Defn): User2 = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new User2(ssn = defn.ssn, password = defn.password, name = defn.name)
  }
  implicit object User2_upcast_User2 extends izumi.idealingua.runtime.IRTCast[User2, User2] {
    override def convert(_value: User2): User2 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      User2(ssn = _value.ssn, password = _value.password, name = _value.name)
    }
  }
  implicit object User2_upcast_SecurityAttributes extends izumi.idealingua.runtime.IRTCast[User2, SecurityAttributes] {
    override def convert(_value: User2): SecurityAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      SecurityAttributes.Struct(ssn = _value.ssn, password = _value.password)
    }
  }
  implicit object User2_upcast_PersonalAttributes extends izumi.idealingua.runtime.IRTCast[User2, PersonalAttributes] {
    override def convert(_value: User2): PersonalAttributes = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      PersonalAttributes.Struct(ssn = _value.ssn)
    }
  }
  implicit class User2Extensions(override protected val _value: User2) extends izumi.idealingua.runtime.IRTConversions[User2]
}
       