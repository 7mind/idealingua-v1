package idltest.services



final case class Package(name: String) extends AnyVal with Package.Defn

trait PackageCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
  import _root_.io.circe.{Encoder, Decoder}
  implicit val encodePackage: Encoder.AsObject[Package] = Encoder.forProduct1[Package, String]("name")((v: Package) => v.name)
  implicit val decodePackage: Decoder[Package] = Decoder.forProduct1[Package, String]("name")((d: String) => new Package(d))
}

object Package extends PackageCirce {
  trait Defn extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def name: String }
  def apply(name: String): Package = {
    new Package(name = name)
  }
  def apply(defn: Package.Defn): Package = {
    assert(defn.asInstanceOf[_root_.scala.AnyRef] ne null)
    new Package(name = defn.name)
  }
  implicit object Package_upcast_Package extends izumi.idealingua.runtime.IRTCast[Package, Package] {
    override def convert(_value: Package): Package = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      Package(name = _value.name)
    }
  }
  implicit class PackageExtensions(override protected val _value: Package) extends izumi.idealingua.runtime.IRTConversions[Package]
}
       