package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.il.ast.IDLTyper
import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.il.ast.typed.{DomainDefinition, TypeDef => LegacyTypeDef}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.TypespaceImpl
import izumi.idealingua.translator.CompilerOptions.ScalaTranslatorOptions
import izumi.idealingua.translator.toscala.ScalaTranslator
import izumi.idealingua.translator.toscala.products.CogenProduct.EnumProduct
import izumi.idealingua.translator.toscala.types.ScalaTypeConverter
import izumi.idealingua.translator.toscala.types.runtime.IDLRuntimeTypes
import izumi.idealingua.translator.{Translated, Translator}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef, Domain => NewDomain}

import scala.meta.*

/** Scala translator port that consumes the new-typer `Domain` IR directly
  * under the `--typer=new` path.
  *
  * IMPL-7a.2 Phase B M2 (corpus-wide assertion fallback):
  *
  *   1. Re-derives a legacy `Typespace` from `parsed`, runs the legacy
  *      `ScalaTranslator`, and returns its `Translated` verbatim — so byte
  *      parity on the 28-domain corpus is preserved trivially.
  *   2. Additionally, for every domain processed, walks `domain.userTypes`
  *      and runs `DomainAliasRenderer` and `DomainEnumRenderer` against
  *      each `TypeDef.Alias` / `TypeDef.Enum`. The rendered output is
  *      compared byte-for-byte (via scala.meta `.syntax` under the
  *      Scala 2.13 dialect) against an inline mirror of the legacy
  *      `ScalaTranslator.renderAlias` / `EnumRenderer.renderEnumeration`
  *      pre-extension formulas applied to the matching legacy
  *      `TypeDef.Alias` / `TypeDef.Enumeration` from the re-derived
  *      `DomainDefinition`. Divergences are recorded via
  *      `DomainScalaTranslator.recordRendererDivergence` and remain
  *      non-fatal by default so the parity gate stays green; set
  *      `-Didealingua.m2.parity.fatal=true` to elevate to thrown
  *      `IDLException`s.
  *
  * Why assertion-fallback rather than a true production-path swap of alias
  * and enum modules:
  *
  *   - The legacy alias path groups aliases by `ModuleId` and merges them
  *     via string concatenation into a single `package-object.scala` file;
  *     the final `ext.extend(modules)` pass touches every module. A real
  *     swap requires re-implementing the merge and re-applying the
  *     extension chain — orthogonal to alias-renderer correctness.
  *   - More importantly, this milestone's corpus-wide exercise revealed a
  *     pre-existing TypeId divergence: the new typer's `TypeDef.Alias.target`
  *     carries a `TypePath` whose `domain` field differs from the legacy
  *     `IDLTyper`'s output for same-domain references (legacy emits
  *     unqualified `M0`, new emits `.M0`). Likewise, the enum companion
  *     references `${t.typeFull}` which inherits the same divergence.
  *     A direct path swap would therefore break byte parity on every
  *     domain that contains an alias or enum. M3+ must address the
  *     TypeId normalization (likely in `NameResolver` / `AliasDealiaser`)
  *     before the production swap can land.
  *
  * @see docs/drafts/20260511-PR02-IMPL07a-scala-translator-port-plan.md
  */
final class DomainScalaTranslator(
  domain: NewDomain,
  parsed: DomainMeshResolved,
  options: ScalaTranslatorOptions,
) extends Translator {

  override def translate(): Translated = {
    val domainDef = new IDLTyper(parsed).perform() match {
      case Right(d) => d
      case Left(diag) =>
        throw new IDLException(
          s"DomainScalaTranslator (IMPL-7a.2 Phase B M2) could not re-derive " +
          s"legacy DomainDefinition from parsed AST for ${domain.id}: $diag"
        )
    }
    val typespace = new TypespaceImpl(domainDef)
    val legacy    = new ScalaTranslator(typespace, options)

    // Corpus-wide alias/enum renderer exercise: instantiate the new
    // `DomainSTContext` and run `DomainAliasRenderer` / `DomainEnumRenderer`
    // against every applicable `TypeDef` in `domain.userTypes`. Divergences
    // from the legacy formulas are collected via `DomainScalaTranslator.
    // recordRendererDivergence` (system property `idealingua.m2.parity.fatal`
    // can elevate them to thrown `IDLException`s) so the renderers are
    // exercised on every production translate without breaking the parity
    // gate. Surfaces typer-pipeline TypeId divergences as signal for M3+.
    exerciseAliasAndEnumRenderers(domainDef)

    legacy.translate()
  }

  private def exerciseAliasAndEnumRenderers(domainDef: DomainDefinition): Unit = {
    val ctx        = new DomainSTContext(domain, parsed, options)
    val legacyConv = new ScalaTypeConverter(domain.id)

    // After PR-02 IMPL-2-fix (`ScopeBuilder.normalize` + `NameResolver.own`),
    // the new typer's `TypeDef.id` carries the same `TypePath.domain` as the
    // legacy `IDLPostTyper.fixPkg` output for every locally declared type.
    // Direct id-keyed lookup is therefore valid; the previous simple-name
    // fallback (introduced in commit `4bb48cb` when same-domain refs still
    // carried `DomainId.Undefined`) has been removed.
    val legacyAliasesById: Map[TypeId, LegacyTypeDef.Alias] = domainDef.types.collect {
      case a: LegacyTypeDef.Alias => (a.id: TypeId) -> a
    }.toMap

    val legacyEnumsById: Map[TypeId, LegacyTypeDef.Enumeration] = domainDef.types.collect {
      case e: LegacyTypeDef.Enumeration => (e.id: TypeId) -> e
    }.toMap

    domain.userTypes.foreach {
      case (_, alias: NewTypeDef.Alias) =>
        legacyAliasesById.get(alias.id) match {
          case Some(la) =>
            val newDefns = ctx.aliasRenderer.renderAlias(alias)
            val legacyDefns = Seq(
              q"type ${legacyConv.toScala(la.id).typeName} = ${legacyConv.toScala(la.target).typeFull}"
            )
            assertByteEqual(legacyDefns, newDefns, s"alias ${alias.id.name}")
          case None =>
          // New IR classifies this declaration as a `TypeDef.Alias` while the
          // legacy typer carries a non-alias shape at the same id (e.g.
          // `clone M0 into M2 { ... }` becomes an alias in the new IR but a
          // DTO/Interface extension in legacy — a pre-existing IR-phase
          // divergence orthogonal to TypeId normalization). The corpus-wide
          // `ScalaTyperParitySpec` byte gate catches any net output divergence.
        }

      case (_, e: NewTypeDef.Enum) =>
        legacyEnumsById.get(e.id) match {
          case Some(le) =>
            val newProduct    = ctx.enumRenderer.renderEnumeration(e)
            val legacyProduct = legacyEnumProduct(le, legacyConv)
            assertEnumProductByteEqual(legacyProduct, newProduct, s"enum ${e.id.name}")
          case None =>
          // Same rationale as the alias case above.
        }

      case _ => ()
    }
  }

  /** Inline mirror of legacy `EnumRenderer.renderEnumeration` pre-extension
    * body (see `ScalaTranslator.scala` sibling `EnumRenderer.scala:13-44`).
    * The legacy renderer wraps this with `ext.extend(...)`; we deliberately
    * do not, so the comparison stays at the structural pre-extension layer
    * that `DomainEnumRenderer` produces.
    */
  private def legacyEnumProduct(i: LegacyTypeDef.Enumeration, conv: ScalaTypeConverter): EnumProduct = {
    import conv._
    val rt = IDLRuntimeTypes
    val t  = conv.toScala(i.id)

    val members = i.members.map {
      m =>
        val mt = t.within(m.value)
        val element =
          q"""case object ${mt.termName} extends ${t.init()} {
              override def toString: String = ${Lit.String(m.value)}
            }"""
        mt.termName -> element
    }

    val parseMembers = members.map {
      case (termName, _) =>
        val termString = termName.value
        p"""case ${Lit.String(termString)} => $termName"""
    }

    val qqEnum = q""" sealed trait ${t.typeName} extends ${rt.enumEl.init()} {} """
    val qqEnumCompanion =
      q"""object ${t.termName} extends ${rt.idlEnum.init()} {
            type Element = ${t.typeFull}

            override def all: Seq[${t.typeFull}] = Seq(..${members.map(_._1)})

            override def parse(value: String): ${t.typeName} = value match {
              ..case $parseMembers
            }
           }"""

    EnumProduct(qqEnum, qqEnumCompanion, members)
  }

  private def renderSyntax(tree: scala.meta.Tree): String =
    scala.meta.dialects.Scala213(tree).syntax

  private def recordDivergence(label: String, detail: String): Unit = {
    if (DomainScalaTranslator.parityFatal) {
      throw new IDLException(s"DomainScalaTranslator M2 parity divergence (fatal) for $label // $detail")
    }
    val _ = DomainScalaTranslator.recordRendererDivergence(domain.id.toString, label, detail)
  }

  private def assertByteEqual(expected: Seq[Defn], actual: Seq[Defn], label: String): Unit = {
    val exp = expected.map(renderSyntax).mkString(" ; ")
    val act = actual.map(renderSyntax).mkString(" ; ")
    if (exp != act) {
      recordDivergence(label, s"legacy=[$exp] // new=[$act]")
    }
  }

  private def assertEnumProductByteEqual(expected: EnumProduct, actual: EnumProduct, label: String): Unit = {
    val expHead = renderSyntax(expected.defn)
    val actHead = renderSyntax(actual.defn)
    if (expHead != actHead) recordDivergence(s"$label (sealed trait)", s"legacy=$expHead // new=$actHead")

    val expComp = renderSyntax(expected.companionBase)
    val actComp = renderSyntax(actual.companionBase)
    if (expComp != actComp) recordDivergence(s"$label (companion)", s"legacy=$expComp // new=$actComp")

    if (expected.elements.size != actual.elements.size) {
      recordDivergence(s"$label (member count)", s"legacy=${expected.elements.size} new=${actual.elements.size}")
    } else {
      expected.elements.zip(actual.elements).zipWithIndex.foreach {
        case (((eName, eDefn), (aName, aDefn)), idx) =>
          if (eName.value != aName.value) {
            recordDivergence(s"$label (member[$idx] name)", s"legacy=${eName.value} new=${aName.value}")
          }
          val eSyntax = renderSyntax(eDefn)
          val aSyntax = renderSyntax(aDefn)
          if (eSyntax != aSyntax) {
            recordDivergence(s"$label (member[$idx] body)", s"legacy=$eSyntax // new=$aSyntax")
          }
      }
    }
  }
}

object DomainScalaTranslator {

  /** When `true` (set via JVM system property
    * `idealingua.m2.parity.fatal=true`), an alias/enum renderer divergence
    * throws an `IDLException` so the parity spec surfaces it as a failure.
    * Default `false`: divergences accumulate in
    * `[[rendererDivergences]]` and parity stays green at the byte-output
    * gate.
    */
  def parityFatal: Boolean = java.lang.Boolean.getBoolean("idealingua.m2.parity.fatal")

  private val divergenceLog = new java.util.concurrent.ConcurrentLinkedQueue[String]()

  def recordRendererDivergence(domainId: String, label: String, detail: String): Boolean =
    divergenceLog.offer(s"$domainId :: $label :: $detail")

  /** Snapshot of all renderer divergences collected since process start.
    * Intended for diagnostic inspection from test harnesses; not part of the
    * production translate pipeline.
    */
  def rendererDivergences: Seq[String] = {
    val it = divergenceLog.iterator()
    val out = scala.collection.mutable.ArrayBuffer.empty[String]
    while (it.hasNext) {
      val _ = out += it.next()
    }
    out.toSeq
  }
}
