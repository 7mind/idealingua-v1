package izumi.idealingua.translator.compat

import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.typer.ir.{Domain => NewDomain}

/** Bridge for the new-typer Domain translators (`DomainTypeScriptTranslator`,
  * `DomainCSharpTranslator`) that still need a `Typespace`-shaped value to
  * feed into the legacy converter family
  * (`TypeScriptTypeConverter`, `CSharpType`, `CSharpClass`).
  *
  * PR-02 IMPL-10-prep: removes the last direct `IDLTyper(parsed).perform()`
  * call from the new translator domain branches. The `IDLTyper` invocation
  * is encapsulated here so the translators consume only `Domain` + the
  * parsed AST + this façade — no `IDLTyper` reference in
  * `translator/totypescript/domain/` or `translator/tocsharp/domain/`.
  *
  * Direction note: this is the reverse of [[DomainAsTypespace]].
  *   - `DomainAsTypespace` wraps a legacy `Typespace` and exposes the new
  *     `Domain` as a sidecar field for the still-unported renderers.
  *   - `DomainTypespaceFacade` wraps a new `Domain` and the parsed AST,
  *     and exposes the legacy `Typespace` (re-derived via `IDLTyper`) for
  *     the still-Typespace-shaped converter helpers.
  *
  * Why not back the façade against `Domain` directly (the original Option A
  * sketch in tasks.md)? The two consumers — `TypeScriptTypeConverter` and
  * `CSharpType` — call into `ts.dealias`, `ts.apply(TypeId)`,
  * `ts.tools.implId`, `ts.structure.structure`, and `ts.inheritance.allParents`.
  * The first two map cleanly to `Domain.aliases` / `Domain.userTypes`, but
  * the last three pull deep into the legacy `StructuralQueriesImpl` /
  * `InheritanceQueriesImpl` / `TypespaceToolsImpl` surface that has no
  * one-line `Domain` equivalent. Re-implementing those for the converters
  * is exactly what IMPL-10 / IMPL-11 will do by porting the converters
  * themselves. Until then, the minimum-impact unblock is to keep delegating
  * to a real `Typespace` and concentrate the `IDLTyper` call in one place.
  *
  * Field-ordering invariant (C12 / L3): this façade preserves the legacy
  * `TypespaceImpl` ordering as-is; it does not reorder anything.
  *
  * Deletion path: IMPL-10 ports the converters to consume `Domain`
  * directly; IMPL-11 deletes this file together with the legacy
  * `TypespaceImpl` / `IDLTyper` tree.
  */
final class DomainTypespaceFacade private (
  val newDomain: NewDomain,
  legacy: Typespace,
) extends TypespaceImpl(legacy.domain)

object DomainTypespaceFacade {

  /** Re-derive a legacy `Typespace` from the parsed AST and pair it with
    * the new-typer `Domain`. Throws `IDLException` if the legacy typer
    * rejects the AST (the new typer has already accepted it upstream, so
    * this is a defensive guard rather than an expected user-visible path).
    */
  def apply(domain: NewDomain, parsed: DomainMeshResolved): DomainTypespaceFacade = {
    new IDLTyper(parsed).perform() match {
      case Right(d) => new DomainTypespaceFacade(domain, new TypespaceImpl(d))
      case Left(diag) =>
        throw new IDLException(
          s"DomainTypespaceFacade (IMPL-10-prep) could not re-derive " +
          s"legacy Typespace from parsed AST for ${domain.id}: $diag"
        )
    }
  }
}
