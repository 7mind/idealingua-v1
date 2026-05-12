package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.typer.ir.TypeDef

import scala.meta.*

/** Renders a new-IR `TypeDef.Alias` as the same scala.meta `Defn` sequence
  * the legacy `ScalaTranslator.renderAlias` produces.
  *
  * IMPL-7a.2 Phase B M1: alias rendering is a single `type X = Y` line.
  * `TypeDef.Alias` exposes `id: AliasId` and `target: TypeId`, matching the
  * legacy `TypeDef.Alias` shape one-to-one. The `ScalaTypeConverter` does
  * the actual scala.meta construction; this renderer just dispatches.
  *
  * Byte parity with legacy is structural: same input `(AliasId, TypeId)`,
  * same `ScalaTypeConverter` calls, same `q"..."` quasiquote — modulo the
  * fact that the new typer fully dealiases targets before this point. The
  * legacy renderer reads `i.target` directly, which the new-typer pipeline
  * has already resolved through the alias graph (`AliasDealiaser`). For
  * direct aliases this is byte-identical to legacy. Chained / cross-domain
  * aliases that dealias to a different `TypeId` shape than legacy emits
  * will surface as parity diffs in the byte-parity unit test below — that
  * is a deliberate signal, not a defect of this renderer.
  */
final class DomainAliasRenderer(ctx: DomainSTContext) {

  def renderAlias(i: TypeDef.Alias): Seq[Defn] = {
    Seq(q"type ${ctx.conv.toScala(i.id).typeName} = ${ctx.conv.toScala(i.target).typeFull}")
  }
}
