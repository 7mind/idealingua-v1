package idltest.datainheritance



final case class ParameterDTO(i32: Int, str: String) extends ParameterDTO.Defn

trait ParameterDTOCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeParameterDTO: Encoder.AsObject[ParameterDTO] = deriveEncoder[ParameterDTO]
  implicit val decodeParameterDTO: Decoder[ParameterDTO] = deriveDecoder[ParameterDTO]
}

object ParameterDTO extends ParameterDTOCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType {
    def i32: Int
    def str: String
  }
  def apply(i32: Int, str: String): ParameterDTO = {
    new ParameterDTO(i32 = i32, str = str)
  }
  def apply(defn: ParameterDTO.Defn): ParameterDTO = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new ParameterDTO(i32 = defn.i32, str = defn.str)
  }
  implicit object ParameterDTO_cast_into_TestData1 extends izumi.idealingua.runtime.IRTCast[ParameterDTO, TestData1] {
    override def convert(_value: ParameterDTO): TestData1 = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      TestData1(i32 = _value.i32, str = _value.str)
    }
  }
  implicit object ParameterDTO_upcast_ParameterDTO extends izumi.idealingua.runtime.IRTCast[ParameterDTO, ParameterDTO] {
    override def convert(_value: ParameterDTO): ParameterDTO = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      ParameterDTO(i32 = _value.i32, str = _value.str)
    }
  }
  implicit class ParameterDTOExtensions(override protected val _value: ParameterDTO) extends izumi.idealingua.runtime.IRTConversions[ParameterDTO]
}
       