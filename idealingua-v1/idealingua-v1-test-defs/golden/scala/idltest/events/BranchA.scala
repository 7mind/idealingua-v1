package idltest.events



final case class BranchA(a: String) extends AnyVal with BranchA.Defn

trait BranchACirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodeBranchA: Encoder.AsObject[BranchA] = Encoder.forProduct1[BranchA, String]("a")((v: BranchA) => v.a)
  implicit val decodeBranchA: Decoder[BranchA] = Decoder.forProduct1[BranchA, String]("a")((d: String) => new BranchA(d))
}

object BranchA extends BranchACirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: String }
  def apply(a: String): BranchA = {
    new BranchA(a = a)
  }
  def apply(defn: BranchA.Defn): BranchA = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new BranchA(a = defn.a)
  }
  implicit object BranchA_upcast_BranchA extends izumi.idealingua.runtime.IRTCast[BranchA, BranchA] {
    override def convert(_value: BranchA): BranchA = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      BranchA(a = _value.a)
    }
  }
  implicit class BranchAExtensions(override protected val _value: BranchA) extends izumi.idealingua.runtime.IRTConversions[BranchA]
}
       