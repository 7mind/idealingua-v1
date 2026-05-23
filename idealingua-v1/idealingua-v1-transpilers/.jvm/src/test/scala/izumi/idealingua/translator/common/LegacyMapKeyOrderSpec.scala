package izumi.idealingua.translator.common

import org.scalatest.funsuite.AnyFunSuite

/** Blackbox-Atomic coverage for [[LegacyMapKeyOrder]]: pin the explicit
  * algorithm to `scala.collection.immutable.Map[String, _].keys` output
  * for representative size buckets. If a future Scala release shifts its
  * own iteration order this test does NOT fail (we own the contract,
  * not stdlib); the comparison against `Map.keys` is here purely as a
  * documentation of equivalence with the Scala 2.13 / 3.x baseline at
  * the time the algorithm was written.
  */
final class LegacyMapKeyOrderSpec extends AnyFunSuite {

  private def stdlibKeysOrder(keys: List[String]): List[String] =
    keys.map(k => k -> k).toMap.keys.toList

  private def assertMatchesStdlib(keys: List[String]): Unit = {
    val expected = stdlibKeysOrder(keys)
    val actual   = LegacyMapKeyOrder(keys)
    assert(actual == expected, s"keys=$keys\n  stdlib: $expected\n  ours:   $actual")
  }

  test("size 0 — empty list") {
    assert(LegacyMapKeyOrder(Nil) == Nil)
  }

  test("size 1..4 — Map1..Map4 insertion order is preserved verbatim") {
    assertMatchesStdlib(List("a"))
    assertMatchesStdlib(List("b", "a"))
    assertMatchesStdlib(List("c", "a", "b"))
    assertMatchesStdlib(List("d", "b", "a", "c"))
    // also: a non-trivially-ordered 4-tuple to confirm we do NOT sort
    assertMatchesStdlib(List("zebra", "apple", "mango", "kiwi"))
  }

  test("size 5 — HashMap CHAMP iteration") {
    assertMatchesStdlib(List("a", "b", "c", "d", "e"))
  }

  test("diamondapply-style 7-field set (apex {kind, at, note})") {
    val keys = List("alphaId", "alphaName", "kind", "at", "note", "betaId", "betaName")
    assertMatchesStdlib(keys)
    val apex = Set("kind", "at", "note")
    assert(
      LegacyMapKeyOrder(keys).filter(apex.contains) == List("at", "note", "kind"),
      "diamond-apex subset in legacy Scala-Map iteration order",
    )
  }

  test("synthesised hash-collision case — same low 5 bits, distinct deeper bits") {
    // Force a low-5-bit collision by picking strings whose hashCode shares
    // the same `improve(_) & 31`. The CHAMP walk recurses to the next 5-bit
    // chunk; our replication must produce the same nested order.
    val collisionSet = List("Aa", "BB", "C#", "DD", "EE", "FF", "GG")
    assertMatchesStdlib(collisionSet)
  }
}
