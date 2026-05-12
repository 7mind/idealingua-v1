package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.translator.CompilerOptions.ScalaTranslatorOptions
import izumi.idealingua.translator.toscala.tools.ModuleTools
import izumi.idealingua.translator.toscala.types.ScalaTypeConverter
import izumi.idealingua.translator.toscala.types.runtime.IDLRuntimeTypes
import izumi.idealingua.typer.ir.Domain

/** Minimal STContext for the Domain-consuming Scala translator port.
  *
  * IMPL-7a.2 Phase B M1: carries the new `Domain` IR, the original parsed
  * AST (for declaration order recovery per R1), and the resolved translator
  * options. Exposes the small set of helpers the alias and enum renderers
  * need (`conv`, `rt`, `modules`, `extensions`) without dragging the
  * legacy `Typespace`-shaped `STContext` through the new path.
  *
  * Subsequent milestones (M2-M6) will add `aliasRenderer`/`enumRenderer`
  * fields, plus the structural / service-family renderers, until the
  * production translate() body can be flipped to consume `Domain` end to
  * end.
  *
  * @see docs/drafts/20260511-PR02-IMPL07a-scala-translator-port-plan.md
  */
final class DomainSTContext(
  val domain: Domain,
  val parsed: DomainMeshResolved,
  val options: ScalaTranslatorOptions,
) {
  final val conv                     = new ScalaTypeConverter(domain.id)
  final val rt: IDLRuntimeTypes.type = IDLRuntimeTypes
  final val modules                  = new ModuleTools()

  final def extensions = options.extensions

  final val aliasRenderer = new DomainAliasRenderer(this)
  final val enumRenderer  = new DomainEnumRenderer(this)

  // PR-02 IMPL-7a.2 Phase B M3: structural renderers (Identifier / DTO /
  // Interface). The composite + interface renderers cross-reference each
  // other (interface companion embeds the impl-DTO via `compositeRenderer`;
  // composite's mirror trait calls `interfaceRenderer.mkTrait`). Order of
  // val declarations does not matter — both are `final val`s on this trait
  // and the cross-refs are resolved at method-call time, not at init.
  final val idRenderer        = new DomainIdRenderer(this)
  final val compositeRenderer = new DomainCompositeRenderer(this)
  final val interfaceRenderer = new DomainInterfaceRenderer(this)
}
