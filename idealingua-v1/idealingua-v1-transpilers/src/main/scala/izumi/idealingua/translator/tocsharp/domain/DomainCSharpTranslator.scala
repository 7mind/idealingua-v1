package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.output.Module
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions.CSharpTranslatorOptions
import izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain, TypeDef => NewTypeDef}

/** C# translator surface that consumes the new-typer `Domain` IR under
  * the `--typer=new` path.
  *
  * IMPL-7c Phase B M5 (production-path swap): the translate() body
  * walks `parsed.members` in declaration order and dispatches each
  * `TLDBaseType` / `TLDNewtype` / `TLDService` / `TLDBuzzer` to the
  * matching M1-M3 renderer via `typesByName: Map[String, NewTypeDef]`
  * derived from `domain.userTypes`. The M4 JsonNet extension is spliced
  * into each renderer's raw-string template at the legacy splice points
  * (pre/post model emit) and into the per-product import list. The
  * legacy `CSharpTranslator` is no longer invoked on this path.
  *
  * `Typespace` is still re-derived once per `translate()` via
  * `IDLTyper(parsed).perform()` because the renderers' deep helpers
  * (`CSharpType` predicates, `ts.dealias` in nested calls, the legacy
  * `TypeScriptTypeConverter`-equivalent) consume it for import / dealias
  * plumbing that has no new-IR equivalent in scope yet. The
  * `DomainCSImports` shim landed in IMPL-7b/7c-post (PR-02) — it covers
  * the imports-collection path, but the renderers still take `ts:
  * Typespace` as a per-call parameter so the converter side remains
  * functional. IMPL-10 deletion of the legacy typer is gated on
  * eliminating that remaining `ts: Typespace` surface from the
  * renderers (tracked as F-followup).
  *
  * Extension wiring mirrors `CSharpTranslator.defaultExtensions` =
  * `Seq(JsonNetExtension)` (the wire-format-critical authority for the
  * C# leg of the cross-language matrix):
  *   - Identifier / Enum / DTO / Interface / ADT — splice JsonNet
  *     `[JsonConverter(typeof(<Name>_JsonNetConverter))]` attribute
  *     pre-emit and the corresponding `<Name>_JsonNetConverter` class
  *     post-emit; thread JsonNet imports
  *     (`Newtonsoft.Json` + sometimes `Newtonsoft.Json.Linq` /
  *     `System.Linq` + `IRT.Marshaller`) into the header import list.
  *   - Interface companion (synthetic impl-DTO) — splice the
  *     `<Iface>Struct_JsonNetConverter` block.
  *   - Alias / Service / Buzzer — no JsonNet handler in the legacy
  *     extension surface, so no splice (the per-method I/O DTO
  *     wire-format converter is not currently emitted by the M3 service
  *     renderer; this matches the M3 byte-parity contract under the
  *     empty extension list).
  */
final class DomainCSharpTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: CSharpTranslatorOptions,
) extends Translator {

  private val ctx = new DomainCSContext(domain, parsed, options)

  // Re-derive a legacy `Typespace` from the parsed AST so the renderers'
  // import/converter plumbing (`CSharpImports.apply(definition, pkg)`,
  // `CSharpType`) has a stable lookup surface. The new IR (`Domain`)
  // already carries the structural data the renderers consume; this
  // single derivation per `translate()` is the minimum-impact swap for
  // M5 — IMPL-10/11 will replace it with a `DomainCSImports` shim.
  private lazy val ts: Typespace = {
    new IDLTyper(parsed).perform() match {
      case Right(d) => new TypespaceImpl(d)
      case Left(diag) =>
        throw new IDLException(
          s"DomainCSharpTranslator (IMPL-7c Phase B M5) could not re-derive " +
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
          case svc: NewTypeDef.Service => modules ++= emitService(svc)
          case _                       => ()
        }
      case RawTopLevelDefn.TLDBuzzer(raw) =>
        typesByName.get(raw.id.name).foreach {
          case bz: NewTypeDef.Buzzer => modules ++= emitBuzzer(bz)
          case _                     => ()
        }
      case _ => ()
    }

    Translated(domain.id, domain.meta, modules.toSeq)
  }

  private def emitTypeDef(td: NewTypeDef): Seq[Module] = td match {
    case a: NewTypeDef.Alias       => emitAlias(a)
    case e: NewTypeDef.Enum        => emitEnum(e)
    case id: NewTypeDef.Identifier => emitIdentifier(id)
    case dto: NewTypeDef.Dto       => emitDto(dto)
    case ifc: NewTypeDef.Interface => emitInterface(ifc)
    case adt: NewTypeDef.Adt       => emitAdt(adt)
    case _                         => Seq.empty
  }

  // -- alias --------------------------------------------------------------
  // Legacy `JsonNetExtension` has no Alias handler — no splice; matches
  // legacy `CSharpTranslator.translateDef`.
  private def emitAlias(a: NewTypeDef.Alias): Seq[Module] = {
    val im      = DomainCSImports.forTypeDef(a, a.id.path.toPackage, domain)
    val product = ctx.aliasRenderer.renderAlias(a, ts, im)
    ctx.modules.toSource(a.id.path.domain, ctx.modules.toModuleId(a.id), product)
  }

  // -- enum ---------------------------------------------------------------
  private def emitEnum(e: NewTypeDef.Enum): Seq[Module] = {
    val im     = DomainCSImports.forTypeDef(e, e.id.path.toPackage, domain)
    val post   = DomainCSJsonNetExtension.postEnum(e)
    val header = im.renderImports(List("System") ++ DomainCSJsonNetExtension.importsEnum)
    // Legacy `JsonNetExtension.preModelEmit(Enumeration)` IS defined
    // (`:46`) but the legacy enum renderer `:253-300` does NOT splice
    // it (only `${ext.postModelEmit(ctx, i)}` at line 292). We follow
    // the legacy asymmetry: splice ONLY `post` into the enum body.
    val product = ctx.enumRenderer.renderEnumeration(e, postSplice = post, header = header)
    ctx.modules.toSource(e.id.path.domain, ctx.modules.toModuleId(e.id), product)
  }

  // -- identifier ---------------------------------------------------------
  private def emitIdentifier(i: NewTypeDef.Identifier): Seq[Module] = {
    val im   = DomainCSImports.forTypeDef(i, i.id.path.toPackage, domain)
    val pre  = DomainCSJsonNetExtension.preIdentifier(i)
    val post = DomainCSJsonNetExtension.postIdentifier(i)
    val product = ctx.idRenderer.renderIdentifier(
      i, ts, im,
      preSplice    = pre,
      postSplice   = post,
      extraImports = DomainCSJsonNetExtension.importsIdentifier,
    )
    ctx.modules.toSource(i.id.path.domain, ctx.modules.toModuleId(i.id), product)
  }

  // -- DTO ----------------------------------------------------------------
  private def emitDto(d: NewTypeDef.Dto): Seq[Module] = {
    val im   = DomainCSImports.forTypeDef(d, d.id.path.toPackage, domain)
    val pre  = DomainCSJsonNetExtension.preDto(domain, d)
    val post = DomainCSJsonNetExtension.postDto(domain, d, ts, im)
    val product = ctx.compositeRenderer.renderDto(
      d, ts, im,
      preSplice    = pre,
      postSplice   = post,
      extraImports = DomainCSJsonNetExtension.importsDto,
    )
    ctx.modules.toSource(d.id.path.domain, ctx.modules.toModuleId(d.id), product)
  }

  // -- interface ----------------------------------------------------------
  private def emitInterface(i: NewTypeDef.Interface): Seq[Module] = {
    val im        = DomainCSImports.forTypeDef(i, i.id.path.toPackage, domain)
    val ifacePre  = DomainCSJsonNetExtension.preInterface(i)
    val ifacePost = DomainCSJsonNetExtension.postInterface(domain, i)
    val compPre   = DomainCSJsonNetExtension.preInterfaceImplStruct(i)
    val compPost  = DomainCSJsonNetExtension.postInterfaceImplStruct(domain, i, ts, im)
    val product = ctx.interfaceRenderer.renderInterface(
      i, ts, im,
      ifacePreSplice      = ifacePre,
      ifacePostSplice     = ifacePost,
      companionPreSplice  = compPre,
      companionPostSplice = compPost,
      extraImports        = DomainCSJsonNetExtension.importsInterface,
    )
    ctx.modules.toSource(i.id.path.domain, ctx.modules.toModuleId(i.id), product)
  }

  // -- ADT ----------------------------------------------------------------
  private def emitAdt(a: NewTypeDef.Adt): Seq[Module] = {
    val im   = DomainCSImports.forTypeDef(a, a.id.path.toPackage, domain)
    val pre  = DomainCSJsonNetExtension.preAdt(a)
    val post = DomainCSJsonNetExtension.postAdt(a, ts, im)
    val product = ctx.adtRenderer.renderAdt(
      a, ts, im,
      preSplice    = pre,
      postSplice   = post,
      extraImports = DomainCSJsonNetExtension.importsAdt,
    )
    ctx.modules.toSource(a.id.path.domain, ctx.modules.toModuleId(a.id), product)
  }

  // -- Service / Buzzer ---------------------------------------------------
  private def emitService(svc: NewTypeDef.Service): Seq[Module] = {
    val im      = DomainCSImports.forService(svc, svc.id.domain.toPackage, domain)
    val product = ctx.serviceRenderer.renderService(svc, ts, im, withJsonNet = true)
    ctx.modules.toSource(svc.id.domain, ctx.modules.toModuleId(svc.id), product)
  }

  private def emitBuzzer(bz: NewTypeDef.Buzzer): Seq[Module] = {
    val im      = DomainCSImports.forBuzzer(bz, bz.id.domain.toPackage, domain)
    val product = ctx.serviceRenderer.renderBuzzer(bz, ts, im, withJsonNet = true)
    ctx.modules.toSource(bz.id.domain, ctx.modules.toModuleId(bz.id), product)
  }

}
