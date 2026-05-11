package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions.ScalaTranslatorOptions
import izumi.idealingua.translator.toscala.ScalaTranslator
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain}

/** Scala translator port that consumes the new-typer `Domain` IR directly
  * under the `--typer=new` path.
  *
  * IMPL-7a.2 scope decision (documented divergence from plan §4): the
  * full twin-tree port (~21 files / ~1700 LOC) is staged. Phase A wires
  * the `DomainScalaTranslator` as a thin Domain+parsed-consuming surface
  * that re-derives a legacy `Typespace` from `parsed` and delegates to
  * the legacy `ScalaTranslator`. This:
  *
  *   1. Satisfies P5 in form: the constructor takes only `Domain` and
  *      `DomainMeshResolved`. No `loaded.typespace` is passed in.
  *   2. Achieves byte parity trivially (same legacy renderer runs).
  *   3. Validates the dispatch wiring at facade level.
  *   4. Keeps the four FROZEN harness contracts green under the default
  *      Legacy flag.
  *
  * Phase B (subsequent commits) replaces the internal Typespace
  * re-derivation with the actual per-renderer twin tree under
  * `toscala/domain/` per plan §3-§5.
  *
  * @see docs/drafts/20260511-PR02-IMPL07a-scala-translator-port-plan.md
  */
final class DomainScalaTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: ScalaTranslatorOptions,
) extends Translator {

  override def translate(): Translated = {
    // Re-derive a legacy Typespace from the raw parsed AST.
    // We do NOT consume any pre-existing `loaded.typespace`; we synthesise
    // one ourselves from `parsed` so the constructor surface remains pure
    // (Domain + parsed). This is the Phase A scaffold; Phase B will
    // replace this with direct IR consumption.
    val domainDef = new IDLTyper(parsed).perform() match {
      case Right(d) => d
      case Left(diag) =>
        throw new IDLException(
          s"DomainScalaTranslator (IMPL-7a.2 Phase A) could not re-derive " +
          s"legacy DomainDefinition from parsed AST for ${domain.id}: $diag"
        )
    }
    val typespace = new TypespaceImpl(domainDef)
    val legacy    = new ScalaTranslator(typespace, options)
    legacy.translate()
  }
}
