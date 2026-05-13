package izumi.idealingua.translator.toscala.domain

import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.typer.ir.TypeDef

/** Renders a new-IR `TypeDef.Alias` as a single rendered Scala source
  * line — `type X = Y` — composed via `TextTree[ScalaRefHandle]` and
  * lowered to `String` at the product boundary.
  *
  * F-TextTree M4 — first Scala-side renderer ported off legacy
  * quasiquotes. Pattern source: the TS port's `DomainTSAliasRenderer`
  * (F-TextTree M1) and the C# port's `DomainCSAliasRenderer` (M3). The
  * body composes `TextTree[ScalaRefHandle]` with type references flowing
  * as `TextTree.value(ScalaRefHandle.TypeName(i.id))` /
  * `TextTree.value(ScalaRefHandle.TypeFull(i.target))`, and is rendered
  * via `.mapRender(resolver.resolve)` at the product boundary.
  *
  * Byte parity with the legacy path: the legacy renderer produced a
  * `Defn.Type` AST joined into the package object via `Defn.toString()`
  * (NOT `dialect(tree).syntax`). The resolver routes both witness cases
  * through the same `ScalaTypeConverter` the legacy renderer used;
  * `.toString` on the converter's name shapes produces the same Scala
  * source the legacy `Defn.toString()` produced for the wrapping
  * `Defn.Type`, modulo the `type ${name} = ${target}` template which is
  * spelled out literally here. The new typer fully dealiases targets
  * before this point, matching the upstream dealias contract that the
  * legacy renderer also observed.
  *
  * Product carrier: the renderer returns `String` (one source line per
  * alias). `DomainScalaTranslator.translate()` collects per-package
  * alias strings and joins them into the `package object` body directly.
  */
final class DomainAliasRenderer(ctx: DomainSTContext) {

  private val resolver = new DomainScalaTextResolver(ctx.conv)

  /** Render an alias as one Scala source line (`type X = Y`). Returns
    * `Seq[String]` for parity with the legacy `Seq[Defn]` shape — the
    * single-line/single-defn invariant is the same.
    */
  def renderAlias(i: TypeDef.Alias): Seq[String] = {
    val lhs: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeName(i.id))
    val rhs: TextTree[ScalaRefHandle] = TextTree.value(ScalaRefHandle.TypeFull(i.target))
    val tree: TextTree[ScalaRefHandle] = q"type $lhs = $rhs"
    Seq(tree.mapRender(resolver.resolve))
  }
}
