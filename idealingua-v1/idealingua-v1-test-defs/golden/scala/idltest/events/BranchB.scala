package idltest.events



final case class BranchB(b: String) extends AnyVal with BranchB.Defn

trait BranchBCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeBranchB: Encoder.AsObject[BranchB] = Encoder.forProduct1[BranchB, String]("b")((v: BranchB) => v.b)
  implicit val decodeBranchB: Decoder[BranchB] = Decoder.forProduct1[BranchB, String]("b")((d: String) => new BranchB(d))
}

object BranchB extends BranchBCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def b: String }
  def apply(b: String): BranchB = {
    new BranchB(b = b)
  }
  def apply(defn: BranchB.Defn): BranchB = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new BranchB(b = defn.b)
  }
  implicit object BranchB_upcast_BranchB extends izumi.idealingua.runtime.IRTCast[BranchB, BranchB] {
    override def convert(_value: BranchB): BranchB = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      BranchB(b = _value.b)
    }
  }
  implicit class BranchBExtensions(override protected val _value: BranchB) extends izumi.idealingua.runtime.IRTConversions[BranchB]
}
       