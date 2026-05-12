package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions.CSharpTranslatorOptions
import izumi.idealingua.translator.tocsharp.CSharpTranslator
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain}

/** C# translator surface that consumes the new-typer `Domain` IR under
  * the `--typer=new` path.
  *
  * IMPL-7c Phase A (structural delegation): mirrors the Scala
  * `DomainScalaTranslator` Phase A pattern from commit `bf451a7`. The
  * constructor takes only `Domain` + `DomainMeshResolved`; internally we
  * re-derive a legacy `Typespace` via `IDLTyper(parsed).perform()` and
  * delegate to the legacy `CSharpTranslator`. This achieves byte parity
  * trivially (the legacy renderer runs) and validates the facade
  * dispatch wiring for C# under `--typer=new`.
  *
  * Phase B (a follow-up multi-session effort analogous to the Scala
  * Phase B M1-M6) will replace this internal re-derivation with a
  * `Domain`-consuming renderer twin tree under `tocsharp/domain/`.
  *
  * Note: the descriptor's `makeDomain` applies the same
  * `NUnitExtension` augmentation that `make` does (when
  * `manifest.enableNUnit` is set) before constructing this class, so
  * the Phase A path matches the legacy path exactly.
  */
final class DomainCSharpTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: CSharpTranslatorOptions,
) extends Translator {

  override def translate(): Translated = {
    val domainDef = new IDLTyper(parsed).perform() match {
      case Right(d) => d
      case Left(diag) =>
        throw new IDLException(
          s"DomainCSharpTranslator (IMPL-7c Phase A) could not re-derive " +
          s"legacy DomainDefinition from parsed AST for ${domain.id}: $diag"
        )
    }
    val typespace = new TypespaceImpl(domainDef)
    val legacy    = new CSharpTranslator(typespace, options)
    legacy.translate()
  }
}
