package izumi.test.domain01



final case class GoTimeImportOptional(o: Option[java.time.LocalDateTime]) extends GoTimeImportOptional.Defn

trait GoTimeImportOptionalCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeGoTimeImportOptional: Encoder.AsObject[GoTimeImportOptional] = deriveEncoder[GoTimeImportOptional]
  implicit val decodeGoTimeImportOptional: Decoder[GoTimeImportOptional] = deriveDecoder[GoTimeImportOptional]
}

object GoTimeImportOptional extends GoTimeImportOptionalCirce {
  trait Defn extends izumi.idealingua.runtime.model.IDLGeneratedType { def o: Option[java.time.LocalDateTime] }
  def apply(o: Option[java.time.LocalDateTime]): GoTimeImportOptional = {
    new GoTimeImportOptional(o = o)
  }
  def apply(defn: GoTimeImportOptional.Defn): GoTimeImportOptional = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new GoTimeImportOptional(o = defn.o)
  }
  implicit object GoTimeImportOptional_upcast_GoTimeImportOptional extends izumi.idealingua.runtime.IRTCast[GoTimeImportOptional, GoTimeImportOptional] {
    override def convert(_value: GoTimeImportOptional): GoTimeImportOptional = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      GoTimeImportOptional(o = _value.o)
    }
  }
  implicit class GoTimeImportOptionalExtensions(override protected val _value: GoTimeImportOptional) extends izumi.idealingua.runtime.IRTConversions[GoTimeImportOptional]
}
       