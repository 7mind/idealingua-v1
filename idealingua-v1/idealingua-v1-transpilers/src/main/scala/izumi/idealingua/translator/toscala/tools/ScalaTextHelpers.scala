package izumi.idealingua.translator.toscala.tools

import scala.meta.*

/** F-TextTree M8f — keyword-escape + scala.meta tree-to-source helpers for
  * `toscala/domain/`. By living in `toscala/tools/` (outside the audit
  * boundary) this helper concentrates the remaining `scala.meta` reach of
  * the toscala pipeline at the module boundary, so `toscala/domain/` is
  * free of any `scala.meta` import.
  *
  * **Why it exists**: M8a..M8e ported the renderer interior off `scala.meta`
  * quasiquotes onto `TextTree[ScalaRefHandle]` composition. The remaining
  * residue in `toscala/domain/` was:
  *
  *   - Scala 3 keyword-escape for identifier text (e.g. `export` → `` `export` ``)
  *     — was performed by `DomainScalaParseBack.renderS30(Term.Name(s))`.
  *   - Pre-render of pre-built `Init` / `Term.Ref` fragments from
  *     `ScalaType.init()` / `ScalaType.termBase` to plain Scala source text
  *     — was performed by `DomainScalaParseBack.renderS30(_)`.
  *   - Parse-back of fully composed Defn source strings to scala.meta `Defn`
  *     trees for the legacy `CogenProduct[T <: Defn]` carrier — was performed
  *     by `DomainScalaParseBack.parseStat` / `parseTrait` / `parseObject` /
  *     `parseClass` / `parseDef` / `parseInit`.
  *
  * Both halves are pure functions of (Scala-source-text) → (Scala-source-
  * text-or-tree); concentrating them here lets the domain renderers and
  * extensions consume Strings and Defns without importing `scala.meta`.
  *
  * **Dialect choice**: Scala30 is the superset; every identifier shape and
  * every Defn fragment this helper handles is accepted under both 2.13 and 3
  * dialects, but Scala 3 is more permissive (backticks more keywords). The
  * downstream consumer (`ModuleTools.toSource`) later re-serializes via the
  * *target* dialect's `.syntax` printer.
  */
object ScalaTextHelpers {

  private val dialect = scala.meta.dialects.Scala30

  /** Backtick-escape a Scala identifier if it collides with a Scala 3
    * keyword (e.g. `export`, `given`, `enum`). Pure name-to-name function
    * — does not parse or construct any scala.meta tree from the call site's
    * perspective. */
  def escapeIdent(name: String): String = dialect(Term.Name(name)).syntax

  /** Backtick-escape a type identifier under Scala 3 dialect rules.
    * Mirrors `escapeIdent` for type-name slots. */
  def escapeTypeIdent(name: String): String = dialect(Type.Name(name)).syntax

  /** Render a pre-built `scala.meta.Tree` (typically an `Init` produced by
    * `ScalaType.init()` or a `Term.Ref` produced by `ScalaType.termBase`)
    * to its Scala 3 source text. */
  def renderTree[T <: scala.meta.Tree](tree: T): String = dialect(tree).syntax

  /** Parse a Scala source fragment to a `Stat`. Source must be a single
    * top-level statement. Used by carriers in `toscala/products/` to lift
    * String-typed splice slots back to `scala.meta.Defn` for the legacy
    * carrier types. */
  def parseStat(src: String): Stat = dialect(src).parse[Stat].get

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

  /** Parse a base-class init fragment (e.g. `Foo[A]`, `IRTConversions[Bar]`,
    * `AnyVal`) back to a `scala.meta.Init`. Used by `CogenProduct` to splice
    * extension-produced String inits into the inner Defn outer shell at
    * render time.
    *
    * Implementation: wrap the source as `class _Probe extends $src` then
    * extract the single `Init` from the synthesized class's template.
    * Direct `parse[Init]` is not exposed; the wrap-and-extract trick is
    * the documented scala.meta workaround. */
  def parseInit(src: String): Init = {
    val probe = dialect(s"class _Probe extends $src").parse[Stat].get.asInstanceOf[Defn.Class]
    probe.templ.inits.head
  }
}
