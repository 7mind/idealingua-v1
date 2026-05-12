package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.translator.CompilerOptions.TypescriptTranslatorOptions
import izumi.idealingua.translator.totypescript.tools.ModuleTools
import izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter
import izumi.idealingua.typer.ir.Domain

/** Minimal TSTContext for the Domain-consuming TypeScript translator port.
  *
  * IMPL-7b Phase B M1 — mirrors `DomainSTContext` (Scala port). Carries the
  * new `Domain` IR, the original parsed AST (for declaration order recovery
  * in later milestones), and the resolved translator options. Exposes the
  * minimum surface the alias + enum renderers need (`conv`, `modules`,
  * `manifest`).
  *
  * Subsequent milestones (M2+) will add structural renderers (identifier,
  * DTO, interface), the service / buzzer family, and the extension chain
  * until the production `DomainTypeScriptTranslator.translate()` body can
  * consume `Domain` end-to-end (currently Phase A delegation to legacy
  * `TypeScriptTranslator`).
  */
final class DomainTSContext(
  val domain: Domain,
  val parsed: DomainMeshResolved,
  val options: TypescriptTranslatorOptions,
) {
  final val conv     = new TypeScriptTypeConverter()
  final val modules  = new ModuleTools()
  final val manifest = options.manifest

  final def extensions = options.extensions

  final val aliasRenderer     = new DomainTSAliasRenderer(this)
  final val enumRenderer      = new DomainTSEnumRenderer(this)
  final val idRenderer        = new DomainTSIdRenderer(this)
  final val compositeRenderer = new DomainTSCompositeRenderer(this)
  final val interfaceRenderer = new DomainTSInterfaceRenderer(this)
  final val adtRenderer       = new DomainTSAdtRenderer(this)
  final val serviceMethodProduct = new DomainTSServiceMethodProduct(this, adtRenderer)
  final val serviceRenderer   = new DomainTSServiceRenderer(this, adtRenderer)
}
