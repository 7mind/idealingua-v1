package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.il.ast.raw.defns.RawTopLevelDefn
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.translator.CompilerOptions.ScalaTranslatorOptions
import izumi.idealingua.translator.toscala.domain.extensions.{
  DomainAnyvalExtension,
  DomainCastDownExpandExtension,
  DomainCastSimilarExtension,
  DomainCastUpExtension,
  DomainCirceDerivationTranslatorExtension,
}
import izumi.idealingua.translator.toscala.products.CogenProduct.{AdtProduct, EnumProduct}
import izumi.idealingua.translator.toscala.products.CogenProduct
import izumi.idealingua.translator.toscala.tools.ScalaMetaTools
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain, TypeDef => NewTypeDef}

import scala.meta.*

/** PR-02 IMPL-7a.2 Phase B M6: production-path swap.
  *
  * Under `--typer=new`, the Scala translator consumes the new-typer `Domain`
  * IR directly via the new renderer family (`DomainSTContext` and its
  * `aliasRenderer` / `enumRenderer` / `idRenderer` / `compositeRenderer` /
  * `interfaceRenderer` / `adtRenderer` / `serviceRenderer`) plus the 5 new
  * extensions (Anyval + 3 casts + Circe derivation). The legacy
  * `ScalaTranslator` is no longer invoked on this path; `IDLTyper` is no
  * longer re-derived.
  *
  * The default `TyperImpl.Legacy` path remains the legacy translator. Four
  * FROZEN harness contracts (`verifyGoldens`, `runWireFixtures`,
  * `runCrossLangInterop`, `idealingua-v1-test-harness/test`) gate on the
  * default Legacy flag and therefore stay green trivially.
  *
  * Wire-format equivalence proof for the new path: the M3-M5 corpus-wide
  * exerciser (`ScalaTyperParitySpec`) reported zero structural divergences
  * across 28 domains x 2 Scala versions — every renderer's emitted
  * scala.meta tree round-trips through Scala 2.13 dialect `.syntax`. Source
  * goldens are not regenerated at M6; the Legacy default flag preserves the
  * stable source view for `verifyGoldens`.
  *
  * Iteration order (R1): top-level user types are emitted in `parsed.members`
  * declaration order (the same order the legacy `IDLTyper.perform()`
  * preserves into `Typespace.domain.types`). Each `TLDBaseType` / `TLDNewtype`
  * is matched to its normalized `TypeDef` in `domain.userTypes` by simple
  * name. Services + buzzers are emitted in the order they appear in
  * `parsed.members`. Aliases are grouped into a per-package `package-object.scala`
  * matching legacy `ScalaTranslator.translate()` lines 36-56.
  *
  * Extension wiring is per-renderer (no `handleModules` post-pass — the 5
  * new extensions do not need one). Each per-type product is augmented
  * inline:
  *   - Identifier: AnyVal bases on the case-class header; Circe trait
  *     appended to `more`, its init prepended to the companion's bases.
  *   - DTO: AnyVal bases; CastSimilar/CastUp implicit objects appended to
  *     the companion stats; Circe trait appended + init prepended to
  *     companion bases.
  *   - Interface: Any bases on the trait; CastSimilar/CastUp/CastDownExpand
  *     implicit objects appended to the companion; Circe trait appended
  *     (only when implementors exist) + init prepended.
  *   - Enum: Circe trait appended + init prepended.
  *   - ADT: Circe trait appended (only when alternatives non-empty) + init
  *     prepended.
  *   - Service / Buzzer: no extensions applied (legacy services emit through
  *     `handleService` but the default extension chain is a no-op there).
  */
final class DomainScalaTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: ScalaTranslatorOptions,
) extends Translator {

  import ScalaMetaTools._

  private val ctx = new DomainSTContext(domain, parsed, options)

  override def translate(): Translated = {
    val typesByName: Map[String, NewTypeDef] =
      domain.userTypes.toSeq.map { case (id, td) => id.name -> td }.toMap

    // Walk parsed.members in declaration order. Each TLDBaseType / TLDNewtype
    // matches by simple name to the normalized TypeDef in domain.userTypes
    // (the new typer's TypeId carries the owning DomainId post-F7; the raw
    // parsed id carries DomainId.Undefined).
    val aliasEntries = scala.collection.mutable.ArrayBuffer.empty[(ModuleId, Seq[Defn])]
    val typeModules  = scala.collection.mutable.ArrayBuffer.empty[Module]

    parsed.members.foreach {
      case RawTopLevelDefn.TLDBaseType(raw) =>
        typesByName.get(raw.id.name).foreach(emitTypeDef(_, aliasEntries, typeModules))
      case RawTopLevelDefn.TLDNewtype(raw) =>
        typesByName.get(raw.id.name).foreach(emitTypeDef(_, aliasEntries, typeModules))
      case RawTopLevelDefn.TLDService(raw) =>
        typesByName.get(raw.id.name).foreach {
          case svc: NewTypeDef.Service => typeModules ++= emitService(svc)
          case _                       => ()
        }
      case RawTopLevelDefn.TLDBuzzer(raw) =>
        typesByName.get(raw.id.name).foreach {
          case bz: NewTypeDef.Buzzer => typeModules ++= emitBuzzer(bz)
          case _                     => ()
        }
      case _ => ()
    }

    // Aliases assembled into a per-package package-object.scala — same
    // grouping and stable-sort shape as legacy `ScalaTranslator.translate()`.
    val packageObjects = aliasEntries.toSeq
      .groupBy(_._1)
      .toSeq.sortBy(_._1.toString)
      .map { case (id, pairs) =>
        val content = pairs.flatMap(_._2)
        val pkgName = id.name.split('.').head
        val code =
          s"""
             |package object $pkgName {
             |${content.map(_.toString()).mkString("\n\n")}
             |}
           """.stripMargin
        Module(id.copy(name = "package-object.scala"), ctx.modules.withPackage(id.path.init, code))
      }

    Translated(domain.id, domain.meta, typeModules.toSeq ++ packageObjects)
  }

  private def emitTypeDef(
    td: NewTypeDef,
    aliasEntries: scala.collection.mutable.ArrayBuffer[(ModuleId, Seq[Defn])],
    typeModules: scala.collection.mutable.ArrayBuffer[Module],
  ): Unit = td match {
    case a: NewTypeDef.Alias =>
      val defns = ctx.aliasRenderer.renderAlias(a)
      val mid   = aliasModuleId(a.id)
      aliasEntries += ((mid, defns))

    case e: NewTypeDef.Enum =>
      typeModules ++= emitEnum(e)

    case id: NewTypeDef.Identifier =>
      typeModules ++= emitIdentifier(id)

    case dto: NewTypeDef.Dto =>
      typeModules ++= emitDto(dto)

    case ifc: NewTypeDef.Interface =>
      typeModules ++= emitInterface(ifc)

    case adt: NewTypeDef.Adt =>
      typeModules ++= emitAdt(adt)

    case _ => ()
  }

  private def aliasModuleId(id: TypeId): ModuleId =
    ModuleId(id.path.toPackage, s"${id.path.toPackage.last}.scala")

  // --- Per-renderer emit wrappers with inline extension wiring. ---------

  private def emitEnum(e: NewTypeDef.Enum): Seq[Module] = {
    import ctx.conv._
    val base    = ctx.enumRenderer.renderEnumeration(e)
    val circe   = DomainCirceDerivationTranslatorExtension.emitForEnum(ctx, e)
    val sibling = ctx.conv.toScala(e.id).sibling(circe.name).init()

    val product = EnumProduct(
      defn          = base.defn,
      companionBase = base.companionBase.prependBase(sibling),
      elements      = base.elements,
      more          = base.more :+ circe.defn,
      preamble      = base.preamble,
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(e.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitIdentifier(id: NewTypeDef.Identifier): Seq[Module] = {
    import ctx.conv._
    val base = ctx.idRenderer.renderIdentifier(id).asInstanceOf[CogenProduct[Defn.Class]]

    val anyvalBases = DomainAnyvalExtension.withAnyvalForIdentifier(ctx, id)
    val withAnyVal  = base.defn.prependBase(anyvalBases)

    val circe   = DomainCirceDerivationTranslatorExtension.emitForIdentifier(ctx, id)
    val sibling = ctx.conv.toScala(id.id).sibling(circe.name).init()

    val product = CogenProduct[Defn.Class](
      defn          = withAnyVal,
      companionBase = base.companionBase.prependBase(sibling),
      tools         = base.tools,
      more          = base.more :+ circe.defn,
      preamble      = base.preamble,
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(id.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitDto(dto: NewTypeDef.Dto): Seq[Module] = {
    import ctx.conv._
    val base = ctx.compositeRenderer.renderDto(dto).asInstanceOf[CogenProduct[Defn.Class]]

    val anyvalBases = DomainAnyvalExtension.withAnyvalForComposite(ctx, dto)
    val withAnyVal  = base.defn.prependBase(anyvalBases)

    val sims               = DomainCastSimilarExtension.mkConvertersForDto(ctx, dto)
    val ups                = DomainCastUpExtension.generateUpcastsForDto(ctx, dto)
    val companionWithCasts = base.companionBase.appendDefinitions(sims ++ ups)

    val circe          = DomainCirceDerivationTranslatorExtension.emitForDto(ctx, dto, options.manifest.sbt.scalaVersions)
    val sibling        = ctx.conv.toScala(dto.id).sibling(circe.name).init()
    val companionFinal = companionWithCasts.prependBase(sibling)

    val product = CogenProduct[Defn.Class](
      defn          = withAnyVal,
      companionBase = companionFinal,
      tools         = base.tools,
      more          = base.more :+ circe.defn,
      preamble      = base.preamble,
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(dto.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitInterface(ifc: NewTypeDef.Interface): Seq[Module] = {
    import ctx.conv._
    val base = ctx.interfaceRenderer.renderInterface(ifc).asInstanceOf[CogenProduct[Defn.Trait]]

    // `Any` base for AnyVal-eligible interfaces is now applied inside
    // `DomainInterfaceRenderer.mkTrait` so the mirror `Defn` traits in DTO
    // companions also get the prepend (legacy parity). Top-level interface
    // `defn` from `renderInterface` already carries it.
    val withAny  = base.defn

    val sims               = DomainCastSimilarExtension.mkConvertersForInterface(ctx, ifc)
    val ups                = DomainCastUpExtension.generateUpcastsForInterface(ctx, ifc)
    val downs              = DomainCastDownExpandExtension.constructorsForInterface(ctx, ifc)
    val companionWithCasts = base.companionBase.appendDefinitions(sims ++ ups ++ downs)

    // Empty interfaces (no implementing DTOs) cannot appear as wire payloads —
    // skip the Circe tagged-union emit to avoid a scala.meta `cases.nonEmpty`
    // invariant trip. Same gating as the M5 exerciser.
    val hasImpls = domain.implementingDtos.getOrElse(ifc.id, Set.empty).nonEmpty
    val (companionFinal, moreFinal) = if (hasImpls) {
      val circe   = DomainCirceDerivationTranslatorExtension.emitForInterface(ctx, ifc)
      val sibling = ctx.conv.toScala(ifc.id).sibling(circe.name).init()
      (companionWithCasts.prependBase(sibling), base.more :+ circe.defn)
    } else {
      (companionWithCasts, base.more)
    }

    val product = CogenProduct[Defn.Trait](
      defn          = withAny,
      companionBase = companionFinal,
      tools         = base.tools,
      more          = moreFinal,
      preamble      = base.preamble,
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(ifc.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitAdt(adt: NewTypeDef.Adt): Seq[Module] = {
    import ctx.conv._
    val baseAdt = ctx.adtRenderer.renderAdt(adt).asInstanceOf[AdtProduct]

    // Empty ADTs (no alternatives — should not occur on the wire) skip the
    // tagged-union Circe emit. Matches M5 exerciser gating.
    val product = if (adt.alternatives.nonEmpty) {
      val circe   = DomainCirceDerivationTranslatorExtension.emitForAdt(ctx, adt)
      val sibling = ctx.conv.toScala(adt.id).sibling(circe.name).init()
      AdtProduct(
        defn          = baseAdt.defn,
        companionBase = baseAdt.companionBase.prependBase(sibling),
        elements      = baseAdt.elements,
        more          = baseAdt.more :+ circe.defn,
        preamble      = baseAdt.preamble,
      )
    } else baseAdt

    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(adt.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitService(svc: NewTypeDef.Service): Seq[Module] = {
    val product = ctx.serviceRenderer.renderService(svc)
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(svc.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitBuzzer(bz: NewTypeDef.Buzzer): Seq[Module] = {
    val product = ctx.serviceRenderer.renderBuzzer(bz)
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(bz.id), product, options.manifest.sbt.scalaVersions)
  }
}
