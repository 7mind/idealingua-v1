// Hand-written sample app for the `broad` selftest corpus.
//
// Covers:
//   - idltest.dtofields.IntPair.Struct  (DTO from mixin) - 2 scenarios
//   - idltest.dtofields.WHPair.Struct   (DTO from mixin) - 1 scenario
//   - idltest.enums.TestEnum            (Enum) - 4 element scenarios
//   - idltest.enums.ShortSyntaxEnum     (Enum, short syntax) - 2 element scenarios
//   - idltest.enums.EnumHolder          (DTO containing Enum) - 1 scenario
//   - idltest.identifiers.CompanyId     (Identifier with uid + i64) - 1 scenario
//   - idltest.identifiers.UserId        (Identifier with two uids) - 1 scenario
//   - idltest.identifiers.BucketID      (Identifier with uids + str) - 1 scenario
//
// Determinism: fixed UUID literals, fixed integers, fixed strings. No randomness.
//
// Note: written to compile against BOTH v1.4.19 and HEAD generated code. Uses
// only the public `apply` factories (which exist as constructors in both
// AnyVal and case-class shapes).
package sample_app

import io.circe._
import io.circe.syntax._

object SampleApp {
  def main(args: Array[String]): Unit = {
    val P = Printer.noSpaces

    // Fixed reference UUIDs
    val U1 = java.util.UUID.fromString("3a7f0c12-1234-5678-9abc-fedcba987654")
    val U2 = java.util.UUID.fromString("4b8e1d23-2345-6789-abcd-edcba9876543")
    val U3 = java.util.UUID.fromString("5c9f2e34-3456-789a-bcde-dcba98765432")

    // idltest.dtofields.IntPair.Struct
    {
      import idltest.dtofields.IntPair
      val v1 = IntPair.Struct(x = 0,  y = 0)
      val v2 = IntPair.Struct(x = 17, y = -3)
      println(s"idltest.dtofields.IntPair.Struct\tzero\t${(v1: IntPair.Struct).asJson.printWith(P)}")
      println(s"idltest.dtofields.IntPair.Struct\tmixed\t${(v2: IntPair.Struct).asJson.printWith(P)}")
    }

    // idltest.dtofields.WHPair.Struct
    {
      import idltest.dtofields.WHPair
      val v = WHPair.Struct(w = 640, h = 480)
      println(s"idltest.dtofields.WHPair.Struct\tdefault\t${(v: WHPair.Struct).asJson.printWith(P)}")
    }

    // idltest.enums.TestEnum
    {
      import idltest.enums.TestEnum
      println(s"idltest.enums.TestEnum\tElement1\t${(TestEnum.Element1: TestEnum).asJson.printWith(P)}")
      println(s"idltest.enums.TestEnum\tElement2\t${(TestEnum.Element2: TestEnum).asJson.printWith(P)}")
      println(s"idltest.enums.TestEnum\tElement3\t${(TestEnum.Element3: TestEnum).asJson.printWith(P)}")
      println(s"idltest.enums.TestEnum\tElement4\t${(TestEnum.Element4: TestEnum).asJson.printWith(P)}")
    }

    // idltest.enums.ShortSyntaxEnum
    {
      import idltest.enums.ShortSyntaxEnum
      println(s"idltest.enums.ShortSyntaxEnum\tElement11\t${(ShortSyntaxEnum.Element11: ShortSyntaxEnum).asJson.printWith(P)}")
      println(s"idltest.enums.ShortSyntaxEnum\tElement22\t${(ShortSyntaxEnum.Element22: ShortSyntaxEnum).asJson.printWith(P)}")
    }

    // idltest.enums.EnumHolder
    {
      import idltest.enums.{EnumHolder, TestEnum}
      val v = EnumHolder(TestEnum.Element1)
      println(s"idltest.enums.EnumHolder\tdefault\t${v.asJson.printWith(P)}")
    }

    // idltest.identifiers.CompanyId
    {
      import idltest.identifiers.CompanyId
      val v = CompanyId(value = U1, iid = 42L)
      println(s"idltest.identifiers.CompanyId\tdefault\t${v.asJson.printWith(P)}")
    }

    // idltest.identifiers.UserId
    {
      import idltest.identifiers.UserId
      val v = UserId(value = U2, company = U1)
      println(s"idltest.identifiers.UserId\tdefault\t${v.asJson.printWith(P)}")
    }

    // idltest.identifiers.BucketID
    {
      import idltest.identifiers.BucketID
      val v = BucketID(app = U1, user = U2, bucket = "main")
      println(s"idltest.identifiers.BucketID\tdefault\t${v.asJson.printWith(P)}")
    }
  }
}
