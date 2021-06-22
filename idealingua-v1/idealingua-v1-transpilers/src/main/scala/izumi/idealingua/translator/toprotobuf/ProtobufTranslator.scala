package izumi.idealingua.translator.toprotobuf

import izumi.idealingua.model.il.ast.typed.TypeDef.*
import izumi.idealingua.model.il.ast.typed.{Buzzer, Service, Structure, TypeDef}
import izumi.idealingua.model.output.Module
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.CompilerOptions.*
import izumi.idealingua.translator.toprotobuf.products.RenderableCogenProduct
import izumi.idealingua.translator.{Translated, Translator}


object ProtobufTranslator {
  final val defaultExtensions = Seq(
  )
}



class ProtobufTranslator(ts: Typespace, options: ProtobufTranslatorOptions)
  extends Translator {
  protected val ctx: PBTContext = new PBTContext(ts, options.extensions)

  def translate(): Translated = {
    val modules = Seq(
      ctx.typespace.domain.types.flatMap(translateDef)
      , ctx.typespace.domain.services.flatMap(translateService)
      , ctx.typespace.domain.buzzers.flatMap(translateBuzzer)
    ).flatten

    Translated(ts, modules)
  }

  protected def translateBuzzer(definition: Buzzer): Seq[Module] = {
//    ctx.modules.toSource(
//      definition.id.domain
//      , ctx.modules.toModuleId(definition.id)
//      , ctx.serviceRenderer.renderService(definition.asService)
//    )
    Seq.empty
  }

  protected def translateService(definition: Service): Seq[Module] = {
//    ctx.modules.toSource(
//      definition.id.domain
//      , ctx.modules.toModuleId(definition.id)
//      , ctx.serviceRenderer.renderService(definition)
//    )
    Seq.empty
  }

  protected def translateDef(definition: TypeDef): Seq[Module] = {
    val defns = definition match {
      case i: Interface =>
        ctx.interfaceRenderer.defns(i)
      case d: DTO =>
        ctx.compositeRenderer.defns(d)
//      case i: Enumeration =>
//        i.id.path
//        ctx.enumRenderer.renderEnumeration(i)
//      case i: Identifier =>
//        ctx.idRenderer.renderIdentifier(i)

//      case d: Adt =>
//        ctx.adtRenderer.renderAdt(d)

      case _: Alias =>
        RenderableCogenProduct.empty

      case _ =>
        RenderableCogenProduct.empty
    }

    ctx.modules.toSource(definition.id.path.domain, ctx.modules.toModuleId(definition), defns)
  }
}

