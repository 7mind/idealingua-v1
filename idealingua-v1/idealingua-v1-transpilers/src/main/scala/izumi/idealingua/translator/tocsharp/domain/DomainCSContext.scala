package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.translator.CompilerOptions.CSharpTranslatorOptions
import izumi.idealingua.translator.tocsharp.tools.ModuleTools
import izumi.idealingua.typer.ir.Domain

/** Minimal CSTContext for the Domain-consuming C# translator port.
  *
  * IMPL-7c Phase B M1 — mirrors `DomainSTContext` (Scala port) and
  * `DomainTSContext` (TS port). Carries the new `Domain` IR, the original
  * parsed AST (for declaration-order recovery in later milestones), and
  * the resolved translator options. Exposes the minimum surface the
  * alias + enum renderers need (`modules`, `options`, `manifest`,
  * `extensions`).
  *
  * Unlike `DomainTSContext`, no language-level type converter is held
  * here: `CSharpType` requires implicit `Typespace` + `CSharpImports`
  * per-call, so it is constructed at the renderer call-site. The alias
  * renderer accepts a `Typespace` parameter for byte-parity with the
  * legacy renderer's `CSharpType(i.target).renderType(true)` call; the
  * enum renderer needs no `Typespace` at all.
  *
  * Subsequent milestones (M2+) will add structural renderers (identifier,
  * DTO, interface), the service / buzzer family, the extension chain,
  * and finally swap the production
  * `DomainCSharpTranslator.translate()` path off Phase A delegation.
  */
final class DomainCSContext(
  val domain: Domain,
  val parsed: DomainMeshResolved,
  val options: CSharpTranslatorOptions,
) {
  final val modules  = new ModuleTools()
  final val manifest = options.manifest

  final def extensions = options.extensions

  final val aliasRenderer       = new DomainCSAliasRenderer(this)
  final val enumRenderer        = new DomainCSEnumRenderer(this)
  final val idRenderer          = new DomainCSIdRenderer(this)
  final val compositeRenderer   = new DomainCSCompositeRenderer(this)
  final val interfaceRenderer   = new DomainCSInterfaceRenderer(this)
  final val adtRenderer         = new DomainCSAdtRenderer(this)
  final val serviceMethodProduct = new DomainCSServiceMethodProduct(this, adtRenderer)
  final val serviceRenderer     = new DomainCSServiceRenderer(this, adtRenderer)
}
