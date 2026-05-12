package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.output.Module
import izumi.idealingua.translator.CompilerOptions.CSharpTranslatorOptions
import izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain, TypeDef => NewTypeDef}

/** C# translator surface that consumes the new-typer `Domain` IR.
  *
  * IMPL-10-prep-Cs2: the C# new-typer path is fully `Typespace`-free.
  * The converter family (`DomainCSharpType`, `DomainCSClass`,
  * `DomainCSField`), the renderers (`DomainCS{Alias,Enum,Id,Composite,
  * Interface,Adt,Service}Renderer`), and the wire-format-critical
  * `DomainCSJsonNetExtension` all consume `Domain` directly. The
  * `DomainTypespaceFacade` is no longer instantiated on this path.
  */
final class DomainCSharpTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: CSharpTranslatorOptions,
) extends Translator {

  private val ctx = new DomainCSContext(domain, parsed, options)

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
  private def emitAlias(a: NewTypeDef.Alias): Seq[Module] = {
    val im      = DomainCSImports.forTypeDef(a, a.id.path.toPackage, domain)
    val product = ctx.aliasRenderer.renderAlias(a, im)
    ctx.modules.toSource(a.id.path.domain, ctx.modules.toModuleId(a.id), product)
  }

  // -- enum ---------------------------------------------------------------
  private def emitEnum(e: NewTypeDef.Enum): Seq[Module] = {
    val im     = DomainCSImports.forTypeDef(e, e.id.path.toPackage, domain)
    val post   = DomainCSJsonNetExtension.postEnum(e)
    val header = im.renderImports(List("System") ++ DomainCSJsonNetExtension.importsEnum)
    val product = ctx.enumRenderer.renderEnumeration(e, postSplice = post, header = header)
    ctx.modules.toSource(e.id.path.domain, ctx.modules.toModuleId(e.id), product)
  }

  // -- identifier ---------------------------------------------------------
  private def emitIdentifier(i: NewTypeDef.Identifier): Seq[Module] = {
    val im   = DomainCSImports.forTypeDef(i, i.id.path.toPackage, domain)
    val pre  = DomainCSJsonNetExtension.preIdentifier(i)
    val post = DomainCSJsonNetExtension.postIdentifier(i)
    val product = ctx.idRenderer.renderIdentifier(
      i, im,
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
    val post = DomainCSJsonNetExtension.postDto(domain, d, im)
    val product = ctx.compositeRenderer.renderDto(
      d, im,
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
    val compPost  = DomainCSJsonNetExtension.postInterfaceImplStruct(domain, i, im)
    val product = ctx.interfaceRenderer.renderInterface(
      i, im,
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
    implicit val _domain: NewDomain = domain
    val im   = DomainCSImports.forTypeDef(a, a.id.path.toPackage, domain)
    val pre  = DomainCSJsonNetExtension.preAdt(a)
    val post = DomainCSJsonNetExtension.postAdt(a, im)
    val product = ctx.adtRenderer.renderAdt(
      a, im,
      preSplice    = pre,
      postSplice   = post,
      extraImports = DomainCSJsonNetExtension.importsAdt,
    )
    ctx.modules.toSource(a.id.path.domain, ctx.modules.toModuleId(a.id), product)
  }

  // -- Service / Buzzer ---------------------------------------------------
  private def emitService(svc: NewTypeDef.Service): Seq[Module] = {
    val im      = DomainCSImports.forService(svc, svc.id.domain.toPackage, domain)
    val product = ctx.serviceRenderer.renderService(svc, im, withJsonNet = true)
    ctx.modules.toSource(svc.id.domain, ctx.modules.toModuleId(svc.id), product)
  }

  private def emitBuzzer(bz: NewTypeDef.Buzzer): Seq[Module] = {
    val im      = DomainCSImports.forBuzzer(bz, bz.id.domain.toPackage, domain)
    val product = ctx.serviceRenderer.renderBuzzer(bz, im, withJsonNet = true)
    ctx.modules.toSource(bz.id.domain, ctx.modules.toModuleId(bz.id), product)
  }

}
