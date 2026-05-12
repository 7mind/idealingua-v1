package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.model.common.TypeId.AliasId
import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions.TypescriptTranslatorOptions
import izumi.idealingua.translator.totypescript.domain.extensions.{DomainTSEnumHelpersExtension, DomainTSIntrospectionExtension}
import izumi.idealingua.translator.totypescript.products.RenderableCogenProduct
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain, TypeDef => NewTypeDef}

/** TypeScript translator surface that consumes the new-typer `Domain` IR
  * under the `--typer=new` path.
  *
  * IMPL-7b Phase B M5: production-path swap. Under `--typer=new`, the
  * TypeScript translator consumes the new-typer `Domain` IR directly via
  * the M1-M3 renderer family (`DomainTSContext` and its `aliasRenderer` /
  * `enumRenderer` / `idRenderer` / `compositeRenderer` /
  * `interfaceRenderer` / `adtRenderer` / `serviceRenderer`) plus the M4
  * extensions (`DomainTSEnumHelpersExtension` +
  * `DomainTSIntrospectionExtension`). The legacy `TypeScriptTranslator` is
  * no longer invoked on this path.
  *
  * `Typespace` is still threaded into the per-type renderers because the
  * `TypeScriptImports` and `TypeScriptTypeConverter` helpers reused from
  * the legacy tree expect it. We re-derive it once per `translate()` via
  * `IDLTyper(parsed).perform()`; the renderers themselves consume only
  * `Domain` for structural fields and use the Typespace exclusively for
  * import / dealias plumbing that has no new-IR equivalent yet.
  *
  * Iteration order: top-level user types are emitted in `parsed.members`
  * declaration order (the same order the legacy `IDLTyper.perform()`
  * preserves into `Typespace.domain.types`). Each `TLDBaseType` /
  * `TLDNewtype` matches by simple name to its normalized `TypeDef` in
  * `domain.userTypes`. Services + buzzers are emitted in the order they
  * appear in `parsed.members`.
  *
  * Extension wiring mirrors legacy `TypeScriptTranslator` defaults:
  *   - Enum: `EnumHelpersExtension` then `IntrospectionExtension`.
  *   - Identifier: `IntrospectionExtension` only.
  *   - DTO: `IntrospectionExtension` only.
  *   - Interface: `IntrospectionExtension` only.
  *   - ADT: `IntrospectionExtension` only.
  *   - Alias / Service / Buzzer: no default extension touches them.
  *
  * `index.ts` is emitted last, mirroring legacy
  * `TypeScriptTranslator.buildIndexModule` (aliases are excluded from the
  * re-export list, services + buzzers are included).
  */
final class DomainTypeScriptTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: TypescriptTranslatorOptions,
) extends Translator {

  private val ctx = new DomainTSContext(domain, parsed, options)

  // Re-derive a legacy `Typespace` from the parsed AST so the renderers'
  // import/converter plumbing — still routed through `TypeScriptImports`
  // and `TypeScriptTypeConverter` — has a stable lookup surface. The new
  // IR consolidation (F16/F17) is not yet wired through every helper in
  // `idealingua-v1-model/types/`, so this single derivation per
  // `translate()` is the minimum-impact swap for M5.
  private lazy val ts: Typespace = {
    new IDLTyper(parsed).perform() match {
      case Right(d) => new TypespaceImpl(d)
      case Left(diag) =>
        throw new IDLException(
          s"DomainTypeScriptTranslator (IMPL-7b Phase B M5) could not re-derive " +
          s"legacy Typespace from parsed AST for ${domain.id}: $diag"
        )
    }
  }

  override def translate(): Translated = {
    val typesByName: Map[String, NewTypeDef] =
      domain.userTypes.toSeq.map { case (id, td) => id.name -> td }.toMap

    val modules = scala.collection.mutable.ArrayBuffer.empty[Module]

    parsed.members.foreach {
      case RawTopLevelDefn.TLDBaseType(raw) =>
        typesByName.get(raw.id.name).foreach(td => modules ++= emitTypeDef(td))
      case RawTopLevelDefn.TLDNewtype(raw) =>
        typesByName.get(raw.id.name).foreach(td => modules ++= emitTypeDef(td))
      case RawTopLevelDefn.TLDService(raw) =>
        typesByName.get(raw.id.name).foreach {
          case svc: NewTypeDef.Service =>
            val product = ctx.serviceRenderer.renderService(svc, ts)
            modules ++= ctx.modules.toSource(svc.id.domain, ctx.modules.toModuleId(svc.id), product)
          case _ => ()
        }
      case RawTopLevelDefn.TLDBuzzer(raw) =>
        typesByName.get(raw.id.name).foreach {
          case bz: NewTypeDef.Buzzer =>
            val product = ctx.serviceRenderer.renderBuzzer(bz, ts)
            modules ++= ctx.modules.toSource(bz.id.domain, ctx.modules.toModuleId(bz.id), product)
          case _ => ()
        }
      case _ => ()
    }

    Translated(domain.id, domain.meta, modules.toSeq :+ buildIndexModule())
  }

  private def emitTypeDef(td: NewTypeDef): Seq[Module] = {
    val product: RenderableCogenProduct = td match {
      case a: NewTypeDef.Alias =>
        ctx.aliasRenderer.renderAlias(a, ts)

      case e: NewTypeDef.Enum =>
        val base    = ctx.enumRenderer.renderEnumeration(e)
        val helpers = DomainTSEnumHelpersExtension.handleEnum(e, base)
        DomainTSIntrospectionExtension.handleEnum(options, e, helpers)

      case id: NewTypeDef.Identifier =>
        val base = ctx.idRenderer.renderIdentifier(id, ts)
        DomainTSIntrospectionExtension.handleIdentifier(domain, options, ctx.conv, id, base)

      case dto: NewTypeDef.Dto =>
        val base = ctx.compositeRenderer.renderDto(dto, ts)
        DomainTSIntrospectionExtension.handleDTO(domain, options, ctx.conv, dto, base)

      case ifc: NewTypeDef.Interface =>
        val base = ctx.interfaceRenderer.renderInterface(ifc, ts)
        DomainTSIntrospectionExtension.handleInterface(domain, options, ctx.conv, ifc, base)

      case adt: NewTypeDef.Adt =>
        val base = ctx.adtRenderer.renderAdt(adt, ts)
        DomainTSIntrospectionExtension.handleAdt(options, domain, adt, base)

      case _ => RenderableCogenProduct.empty
    }

    val moduleId = td match {
      case _: NewTypeDef.Alias       => ModuleId(td.id.path.toPackage, s"${td.id.name}.ts")
      case _: NewTypeDef.Enum        => ModuleId(td.id.path.toPackage, s"${td.id.name}.ts")
      case id: NewTypeDef.Identifier => ModuleId(id.id.path.toPackage, s"${id.id.name}.ts")
      case dto: NewTypeDef.Dto       => ModuleId(dto.id.path.toPackage, s"${dto.id.name}.ts")
      case ifc: NewTypeDef.Interface => ModuleId(ifc.id.path.toPackage, s"${ifc.id.name}.ts")
      case adt: NewTypeDef.Adt       => ModuleId(adt.id.path.toPackage, s"${adt.id.name}.ts")
      case _                         => ModuleId(td.id.path.toPackage, s"${td.id.name}.ts")
    }
    ctx.modules.toSource(td.id.path.domain, moduleId, product)
  }

  /** Mirror of legacy `TypeScriptTranslator.buildIndexModule` (lines 46-56).
    * Aliases are excluded from the re-export list (legacy
    * `typespace.domain.types.filterNot(_.id.isInstanceOf[AliasId])`).
    * Services + buzzers are included.
    */
  private def buildIndexModule(): Module = {
    val typeExports = parsed.members.collect {
      case RawTopLevelDefn.TLDBaseType(raw) if !raw.id.isInstanceOf[AliasId] => raw.id.name
      case RawTopLevelDefn.TLDNewtype(raw)                                   => raw.id.name
    }
    val serviceExports = parsed.members.collect { case RawTopLevelDefn.TLDService(raw) => raw.id.name }
    val buzzerExports  = parsed.members.collect { case RawTopLevelDefn.TLDBuzzer(raw)  => raw.id.name }

    val content =
      s"""// Auto-generated, any modifications may be overwritten in the future.
         |// Exporting module for domain ${domain.id.toPackage.mkString(".")}
         |${typeExports.map(n => s"export * from './$n';").mkString("\n")}
         |${serviceExports.map(n => s"export * from './$n';").mkString("\n")}
         |${buzzerExports.map(n => s"export * from './$n';").mkString("\n")}
         """.stripMargin

    Module(ModuleId(domain.id.toPackage, "index.ts"), content)
  }
}
