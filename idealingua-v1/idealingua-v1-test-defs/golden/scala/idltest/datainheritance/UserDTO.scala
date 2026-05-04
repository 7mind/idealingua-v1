package idltest.datainheritance



final case class UserDTO(value: ParameterDTO) extends AnyVal with UserDTO.Defn

trait UserDTOCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeUserDTO: Encoder.AsObject[UserDTO] = Encoder.forProduct1[UserDTO, ParameterDTO]("value")((v: UserDTO) => v.value)
  implicit val decodeUserDTO: Decoder[UserDTO] = Decoder.forProduct1[UserDTO, ParameterDTO]("value")((d: ParameterDTO) => new UserDTO(d))
}

object UserDTO extends UserDTOCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def value: ParameterDTO }
  def apply(value: ParameterDTO.Defn): UserDTO = {
    assert(value.asInstanceOf[_root_.scala.AnyRef] ne null)
    new UserDTO(value = ParameterDTO(value))
  }
  def apply(defn: UserDTO.Defn): UserDTO = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new UserDTO(value = defn.value)
  }
  implicit object UserDTO_upcast_UserDTO extends izumi.idealingua.runtime.IRTCast[UserDTO, UserDTO] {
    override def convert(_value: UserDTO): UserDTO = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      UserDTO(value = _value.value)
    }
  }
  implicit class UserDTOExtensions(override protected val _value: UserDTO) extends izumi.idealingua.runtime.IRTConversions[UserDTO]
}
       