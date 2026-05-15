package izumi.idealingua.typer.phase

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

final class Sha256Spec extends AnyFunSpec with Matchers {

  private def hex(bs: Array[Byte]): String =
    bs.map(b => f"${b & 0xff}%02x").mkString

  describe("Sha256") {
    it("matches the canonical SHA-256 digest for the empty input") {
      val d = new Sha256
      hex(d.digest()) shouldBe "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }

    it("matches the canonical SHA-256 digest for 'abc'") {
      val d = new Sha256
      d.update("abc".getBytes("UTF-8"))
      hex(d.digest()) shouldBe "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    }
  }
}
