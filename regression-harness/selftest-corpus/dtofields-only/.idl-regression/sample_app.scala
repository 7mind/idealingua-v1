// Hand-rolled minimal sample app for M1 acceptance of idl-regress.
// Covers two trivial concrete types from idltest.dtofields. A future
// LLM-generated sample app supersedes this.
package sample_app

import io.circe._
import io.circe.syntax._

object SampleApp {
  def main(args: Array[String]): Unit = {
    val P = Printer.noSpaces

    // idltest.dtofields.IntPair.Struct — two scenarios.
    {
      import idltest.dtofields.IntPair
      val v1 = IntPair.Struct(x = 0,  y = 0)
      val v2 = IntPair.Struct(x = 17, y = -3)
      println(s"idltest.dtofields.IntPair.Struct\tzero\t${(v1: IntPair.Struct).asJson.printWith(P)}")
      println(s"idltest.dtofields.IntPair.Struct\tmixed\t${(v2: IntPair.Struct).asJson.printWith(P)}")
    }

    // idltest.dtofields.WHPair.Struct — one scenario.
    {
      import idltest.dtofields.WHPair
      val v = WHPair.Struct(w = 640, h = 480)
      println(s"idltest.dtofields.WHPair.Struct\tdefault\t${(v: WHPair.Struct).asJson.printWith(P)}")
    }
  }
}
