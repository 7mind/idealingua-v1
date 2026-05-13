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
import izumi.idealingua.translator.toscala.products.CogenProduct
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{Domain => NewDomain, TypeDef => NewTypeDef}

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
  * each tree round-trips through Scala 2.13 dialect `.syntax`. Source
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

  private val ctx = new DomainSTContext(domain, parsed, options)

  override def translate(): Translated = {
    val typesByName: Map[String, NewTypeDef] =
      domain.userTypes.toSeq.map { case (id, td) => id.name -> td }.toMap

    // Walk parsed.members in declaration order. Each TLDBaseType / TLDNewtype
    // matches by simple name to the normalized TypeDef in domain.userTypes
    // (the new typer's TypeId carries the owning DomainId post-F7; the raw
    // parsed id carries DomainId.Undefined).
    // F-TextTree M4: alias entries carry `Seq[String]` — the alias renderer
    // emits rendered Scala source via `TextTree[ScalaRefHandle]` and no
    // longer round-trips through the legacy printer. The downstream join below
    // concatenates these strings into the `package object` body directly.
    val aliasEntries = scala.collection.mutable.ArrayBuffer.empty[(ModuleId, Seq[String])]
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
             |${content.mkString("\n\n")}
             |}
           """.stripMargin
        Module(id.copy(name = "package-object.scala"), ctx.modules.withPackage(id.path.init, code))
      }

    Translated(domain.id, domain.meta, typeModules.toSeq ++ packageObjects)
  }

  private def emitTypeDef(
    td: NewTypeDef,
    aliasEntries: scala.collection.mutable.ArrayBuffer[(ModuleId, Seq[String])],
    typeModules: scala.collection.mutable.ArrayBuffer[Module],
  ): Unit = td match {
    case a: NewTypeDef.Alias =>
      val rendered = ctx.aliasRenderer.renderAlias(a)
      val mid      = aliasModuleId(a.id)
      aliasEntries += ((mid, rendered))

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
    val base  = ctx.enumRenderer.renderEnumeration(e)
    val circe = DomainCirceDerivationTranslatorExtension.emitForEnum(ctx, e)

    // F-TextTree M8a: Circe sibling trait + companion-base init are
    // pushed into String slots; the carrier parses them back at render
    // time and splices into the inner outer shell.
    val product = base.copy(
      companionCirceBases = List(circe.initText),
      siblings            = List(circe.defnText),
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(e.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitIdentifier(id: NewTypeDef.Identifier): Seq[Module] = {
    val base = ctx.idRenderer.renderIdentifier(id).asInstanceOf[CogenProduct.CompositeProduct]

    val anyvalBases = DomainAnyvalExtension.withAnyvalForIdentifier(ctx, id)
    val circe       = DomainCirceDerivationTranslatorExtension.emitForIdentifier(ctx, id)

    val product = base.copy(
      defnAnyvalBases     = anyvalBases,
      companionCirceBases = List(circe.initText),
      siblings            = List(circe.defnText),
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(id.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitDto(dto: NewTypeDef.Dto): Seq[Module] = {
    val base = ctx.compositeRenderer.renderDto(dto).asInstanceOf[CogenProduct.CompositeProduct]

    val anyvalBases = DomainAnyvalExtension.withAnyvalForComposite(ctx, dto)
    val sims        = DomainCastSimilarExtension.mkConvertersForDto(ctx, dto)
    val ups         = DomainCastUpExtension.generateUpcastsForDto(ctx, dto)
    val circe       = DomainCirceDerivationTranslatorExtension.emitForDto(ctx, dto, options.manifest.sbt.scalaVersions)

    val product = base.copy(
      defnAnyvalBases     = anyvalBases,
      companionCirceBases = List(circe.initText),
      companionCasts      = sims ++ ups,
      siblings            = List(circe.defnText),
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(dto.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitInterface(ifc: NewTypeDef.Interface): Seq[Module] = {
    val base = ctx.interfaceRenderer.renderInterface(ifc).asInstanceOf[CogenProduct.InterfaceProduct]

    // `Any` base for AnyVal-eligible interfaces is applied inside
    // `DomainInterfaceRenderer.mkTrait` (legacy parity for mirror traits).
    val sims  = DomainCastSimilarExtension.mkConvertersForInterface(ctx, ifc)
    val ups   = DomainCastUpExtension.generateUpcastsForInterface(ctx, ifc)
    val downs = DomainCastDownExpandExtension.constructorsForInterface(ctx, ifc)
    // Legacy `ScalaTranslator.defaultExtensions` order:
    //   CastSimilarExtension, CastDownExpandExtension, CastUpExtension.

    // Every user-declared interface receives Circe boilerplate (legacy
    // parity); the mirror DTO ensures the encoder match is non-empty.
    val circe = DomainCirceDerivationTranslatorExtension.emitForInterface(ctx, ifc)

    val product = base.copy(
      companionCirceBases = List(circe.initText),
      companionCasts      = sims ++ downs ++ ups,
      siblings            = List(circe.defnText),
    )
    ctx.modules.toSource(domain.id, ctx.modules.toModuleId(ifc.id), product, options.manifest.sbt.scalaVersions)
  }

  private def emitAdt(adt: NewTypeDef.Adt): Seq[Module] = {
    val baseAdt = ctx.adtRenderer.renderAdt(adt).asInstanceOf[CogenProduct.AdtProduct]

    // Empty ADTs skip the tagged-union Circe emit.
    val product = if (adt.alternatives.nonEmpty) {
      val circe = DomainCirceDerivationTranslatorExtension.emitForAdt(ctx, adt)
      baseAdt.copy(
        companionCirceBases = List(circe.initText),
        siblings            = List(circe.defnText),
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
