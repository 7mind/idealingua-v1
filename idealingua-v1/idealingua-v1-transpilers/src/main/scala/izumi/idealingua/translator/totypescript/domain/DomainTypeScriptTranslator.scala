package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions.TypescriptTranslatorOptions
import izumi.idealingua.translator.totypescript.TypeScriptTranslator
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain}

/** TypeScript translator surface that consumes the new-typer `Domain` IR
  * under the `--typer=new` path.
  *
  * IMPL-7b Phase A (structural delegation): mirrors the Scala
  * `DomainScalaTranslator` Phase A pattern from commit `bf451a7`. The
  * constructor takes only `Domain` + `DomainMeshResolved`; internally we
  * re-derive a legacy `Typespace` via `IDLTyper(parsed).perform()` and
  * delegate to the legacy `TypeScriptTranslator`. This achieves byte
  * parity trivially (the legacy renderer runs) and validates the facade
  * dispatch wiring for TS under `--typer=new`.
  *
  * Phase B (a follow-up multi-session effort analogous to the Scala
  * Phase B M1-M6) will replace this internal re-derivation with a
  * `Domain`-consuming renderer twin tree under `totypescript/domain/`.
  */
final class DomainTypeScriptTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: TypescriptTranslatorOptions,
) extends Translator {

  override def translate(): Translated = {
    val domainDef = new IDLTyper(parsed).perform() match {
      case Right(d) => d
      case Left(diag) =>
        throw new IDLException(
          s"DomainTypeScriptTranslator (IMPL-7b Phase A) could not re-derive " +
          s"legacy DomainDefinition from parsed AST for ${domain.id}: $diag"
        )
    }
    val typespace = new TypespaceImpl(domainDef)
    val legacy    = new TypeScriptTranslator(typespace, options)
    legacy.translate()
  }
}
