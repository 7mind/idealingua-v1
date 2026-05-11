package izumi.idealingua.translator.toscala

import izumi.idealingua.model.il.ast.typed.TypeDef.*
import izumi.idealingua.model.il.ast.typed.{Buzzer, Service, TypeDef}
import izumi.idealingua.model.output.Module
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.CompilerOptions.*
import izumi.idealingua.translator.toscala.extensions.*
import izumi.idealingua.translator.toscala.products.*
import izumi.idealingua.translator.toscala.types.ClassSource
import izumi.idealingua.translator.{Translated, Translator}

import scala.annotation.nowarn
import scala.meta.*

object ScalaTranslator {
  final val defaultExtensions = Seq(
    AnyvalExtension,
    CastSimilarExtension,
    CastDownExpandExtension,
    CastUpExtension,
    CirceDerivationTranslatorExtension,
  )
}

@nowarn("msg=Unused import")
class ScalaTranslator(ts: Typespace, options: ScalaTranslatorOptions) extends Translator {
  import scala.collection.compat.*

  protected val ctx: STContext = new STContext(ts, options.extensions, options.manifest.sbt)

  def translate(): Translated = {
    // Group aliases by their target ModuleId (package-object.scala path).
    // Using a Seq-based groupBy preserves declaration order within each group;
    // sorting by key.toString stabilises the iteration order of distinct groups.
    val aliasEntries = ctx.typespace.domain.types.collect {
      case a: Alias =>
        ctx.modules.toModuleId(a) -> renderAlias(a)
    }
    val aliases = aliasEntries
      .groupBy(_._1)
      .toSeq.sortBy(_._1.toString)
      .map { case (id, pairs) => id -> pairs.flatMap(_._2) }

    val packageObjects = aliases.map {
      case (id, content) =>
        val pkgName = id.name.split('.').head

        val code =
          s"""
             |package object $pkgName {
             |${content.map(_.toString()).mkString("\n\n")}
             |}
           """.stripMargin
        Module(id.copy(name = "package-object.scala"), ctx.modules.withPackage(id.path.init, code))
    }

    val modules = Seq(
      ctx.typespace.domain.types.flatMap(translateDef),
      ctx.typespace.domain.services.flatMap(translateService),
      ctx.typespace.domain.buzzers.flatMap(translateBuzzer),
      packageObjects,
    ).flatten

    Translated(ts.domain.id, ts.domain.meta, ctx.ext.extend(modules))
  }

  protected def translateBuzzer(definition: Buzzer): Seq[Module] = {
    ctx.modules.toSource(
      definition.id.domain,
      ctx.modules.toModuleId(definition.id),
      ctx.serviceRenderer.renderService(definition.asService),
      ctx.sbtOptions.scalaVersions,
    )
  }

  protected def translateService(definition: Service): Seq[Module] = {
    ctx.modules.toSource(
      definition.id.domain,
      ctx.modules.toModuleId(definition.id),
      ctx.serviceRenderer.renderService(definition),
      ctx.sbtOptions.scalaVersions,
    )
  }

  protected def translateDef(definition: TypeDef): Seq[Module] = {
    val defns = definition match {
      case i: Enumeration =>
        ctx.enumRenderer.renderEnumeration(i)
      case i: Identifier =>
        ctx.idRenderer.renderIdentifier(i)
      case i: Interface =>
        ctx.interfaceRenderer.renderInterface(i)
      case d: Adt =>
        ctx.adtRenderer.renderAdt(d)
      case d: DTO =>
        ctx.compositeRenderer.defns(ctx.tools.mkStructure(d.id), ClassSource.CsDTO(d))
      case _: Alias =>
        RenderableCogenProduct.empty
    }

    ctx.modules.toSource(definition.id.path.domain, ctx.modules.toModuleId(definition), defns, ctx.sbtOptions.scalaVersions)
  }

  protected def renderAlias(i: Alias): Seq[Defn] = {
    Seq(q"type ${ctx.conv.toScala(i.id).typeName} = ${ctx.conv.toScala(i.target).typeFull}")
  }

}
