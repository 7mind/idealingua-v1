package idltest.identifiers



sealed trait DepartmentEnum extends izumi.idealingua.runtime.model.IDLEnumElement

trait DepartmentEnumCirce {
  import _root_.io.circe.{Encoder, Decoder, KeyEncoder, KeyDecoder}
  import scala.util.*
  implicit val encodeDepartmentEnum: Encoder[DepartmentEnum] = Encoder.encodeString.contramap(_.toString)
  implicit val decodeDepartmentEnum: Decoder[DepartmentEnum] = Decoder.decodeString.emapTry(v => Try(DepartmentEnum.parse(v)))
  implicit val encodeKeyDepartmentEnum: KeyEncoder[DepartmentEnum] = KeyEncoder.encodeKeyString.contramap(_.toString)
  implicit val decodeKeyDepartmentEnum: KeyDecoder[DepartmentEnum] = new KeyDecoder[DepartmentEnum] { final def apply(key: String): Option[DepartmentEnum] = Try(DepartmentEnum.parse(key)).toOption }
}

object DepartmentEnum extends DepartmentEnumCirce with izumi.idealingua.runtime.model.IDLEnum {
  type Element = DepartmentEnum
  override def all: Seq[DepartmentEnum] = Seq(Engineering, Sales)
  override def parse(value: String): DepartmentEnum = value match {
    case "Engineering" => Engineering
    case "Sales" => Sales
  }
  case object Engineering extends DepartmentEnum { override def toString: String = "Engineering" }
  case object Sales extends DepartmentEnum { override def toString: String = "Sales" }
}
       