package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef
import izumi.idealingua.model.il.ast.typed.TypeDef._
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.CompilerOptions._
import izumi.idealingua.translator.toprotobuf.extensions.ProtobufTranslatorExtension
import izumi.idealingua.translator.toprotobuf.products.RenderableCogenProduct
import izumi.idealingua.translator.{Translated, Translator}

object ProtobufTranslator {
  final val defaultExtensions: Seq[ProtobufTranslatorExtension] = Seq(
  )
}

class ProtobufTranslator(ts: Typespace, options: ProtobufTranslatorOptions) extends Translator {
  protected val ctx: PBTContext = new PBTContext(ts, options.extensions, options.manifest.options)

  def translate(): Translated = {
    val cogenTypes    = translateDefinitions(ctx.typespace.domain.types)
    val cogenServices = ctx.typespace.domain.services.map(ctx.serviceRenderer.defns)
    val cogen         = cogenTypes ++ cogenServices
    val modules       = ctx.modules.toSource(ctx.typespace.domain.id, ctx.modules.toModuleId(ctx.typespace.domain.id), cogen)
    Translated(ts, modules)
  }

  protected def translateDefinitions(definitions: Seq[TypeDef]): Seq[RenderableCogenProduct] = {
    checkEnumScopes(definitions)
    definitions.map {
      case i: Interface =>
        ctx.interfaceRenderer.defns(i)
      case d: DTO =>
        ctx.compositeRenderer.defns(d)
      case i: Enumeration =>
        ctx.enumRenderer.defns(i)
      case i: Identifier =>
        ctx.identifierRenderer.defns(i)
      case d: Adt =>
        ctx.adtRenderer.defns(d)
      case a: Alias =>
        ctx.aliasRenderer.defns(a)
    }
  }

  private def checkEnumScopes(definitions: Seq[TypeDef]): Unit = {
    val m = definitions.collect { case i: Enumeration => i }.map {
      e =>
        e.id.name -> e.members.map(_.value).toSet
    }
    m.foreach {
      case (name, members) =>
        m.foreach {
          case (name1, members1) if name1 != name =>
            val intersect = members1.intersect(members)
            if (intersect.nonEmpty) {
              throw new IDLException(
                s"[${ctx.typespace.domain.id}]: Protobuf can not generate ENUMs with same internals under one domain. $name1 and $name contains same elements: ${intersect
                    .mkString(",")}"
              )
            }
          case _ =>
        }
    }
  }
}
