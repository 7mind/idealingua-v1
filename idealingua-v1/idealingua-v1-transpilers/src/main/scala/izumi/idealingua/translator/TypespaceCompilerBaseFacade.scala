package izumi.idealingua.translator

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.compat.{DomainAsTypespace, NewTyperPipeline}
import izumi.idealingua.translator.tocsharp.CSharpTranslatorDescriptor
import izumi.idealingua.translator.toscala.ScalaTranslatorDescriptor
import izumi.idealingua.translator.totypescript.TypescriptTranslatorDescriptor

class TypespaceCompilerBaseFacade(options: UntypedCompilerOptions) {
  def compile(toCompile: Seq[LoadedDomain.Success]): Layouted = {
    val descriptor = TypespaceCompilerBaseFacade.descriptor(options.language)
    val compiled = toCompile.map {
      loaded =>
        val typespace = options.typerImpl match {
          case TyperImpl.Legacy =>
            loaded.typespace
          case TyperImpl.NewTyper =>
            val newDomain = NewTyperPipeline.run(loaded.parsed)
            new DomainAsTypespace(newDomain, loaded.typespace)
        }
        descriptor.make(typespace, options).translate()
    }

    val hook = descriptor.makeHook(options)

    val finalized = hook.layout(compiled)
    finalized
  }
}

object TypespaceCompilerBaseFacade {
  def descriptor(language: IDLLanguage): TranslatorDescriptor[?] = descriptorsMap(language)

  val descriptors: Seq[TranslatorDescriptor[?]] = Seq(
    ScalaTranslatorDescriptor,
    TypescriptTranslatorDescriptor,
    CSharpTranslatorDescriptor,
  )

  private def descriptorsMap: Map[IDLLanguage, TranslatorDescriptor[?]] = descriptors.map(d => d.language -> d).toMap
}
