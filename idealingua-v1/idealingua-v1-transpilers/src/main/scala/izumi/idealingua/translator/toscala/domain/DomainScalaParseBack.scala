package izumi.idealingua.translator.toscala.domain

import scala.meta.*

/** F-TextTree M5 — boundary helper that lowers a rendered Scala source
  * fragment (produced by a `TextTree[ScalaRefHandle].mapRender` call)
  * back to a `scala.meta.Defn` so it can be stored in the legacy
  * `EnumProduct` / `AdtProduct` carriers.
  *
  * **Why parse-back exists**: M5 ports the renderer interior off
  * `scala.meta` quasiquotes onto `TextTree[ScalaRefHandle]`, but the
  * downstream product carriers and extensions (Circe sibling +
  * `companion.prependBase`, AnyVal `defn.prependBase`, Cast* / Cast
  * sub-defs) still operate on `Defn`. Migrating those carriers and
  * extensions is the M6+ workstream. Until then, the renderer
  * lowers `TextTree → String → Defn` at its own boundary; downstream
  * code is unaffected.
  *
  * **Dialect choice**: `Scala30` is the superset; every shape this
  * helper parses (`sealed trait`, `final case class`, `object`,
  * `implicit def`, `case object`) is accepted under both 2.13 and 3
  * dialects, but Scala 3 is more permissive (e.g. soft keywords). Using
  * Scala 3 here is safe because the consumer (`ModuleTools.toSource`)
  * later re-serializes via the *target* dialect's `.syntax` printer.
  *
  * **Byte parity**: a parse → re-print roundtrip is NOT always byte-equal
  * with the original `q"…"`-quasiquoted Defn — the most common drift
  * is that `q"trait F extends X {}".syntax` drops the empty braces while
  * `parse("trait F extends X {}").syntax` preserves them. M5 renderers
  * accordingly omit empty `{}` in the rendered text so the parsed Defn
  * re-prints byte-equal to legacy. `verifyGoldens` is byte-equal across
  * the corpus on both Scala 2.13 and 3.x.
  */
private[domain] object DomainScalaParseBack {

  private val dialect = scala.meta.dialects.Scala30

  /** Parse a Scala source fragment to a `Stat`. Source must be a single
    * top-level statement. */
  private def parseStat(src: String): Stat = dialect(src).parse[Stat].get

  /** Parse `sealed trait … {}`. */
  def parseTrait(src: String): Defn.Trait = parseStat(src).asInstanceOf[Defn.Trait]

  /** Parse `object … {}` and `case object … {}`. */
  def parseObject(src: String): Defn.Object = parseStat(src).asInstanceOf[Defn.Object]

  /** Parse `final case class … {…}`. */
  def parseClass(src: String): Defn.Class = parseStat(src).asInstanceOf[Defn.Class]

  /** Parse `implicit def …`. */
  def parseDef(src: String): Defn.Def = parseStat(src).asInstanceOf[Defn.Def]

  /** Parse any `Defn` (e.g. the enum element case object). */
  def parseDefn(src: String): Defn = parseStat(src).asInstanceOf[Defn]
}
