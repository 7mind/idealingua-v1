package izumi.idealingua.translator

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.compat.NewTyperPipeline
import izumi.idealingua.translator.tocsharp.CSharpTranslatorDescriptor
import izumi.idealingua.translator.toscala.ScalaTranslatorDescriptor
import izumi.idealingua.translator.totypescript.TypescriptTranslatorDescriptor

class TypespaceCompilerBaseFacade(options: UntypedCompilerOptions) {
  def compile(toCompile: Seq[LoadedDomain.Success]): Layouted = {
    val descriptor = TypespaceCompilerBaseFacade.descriptor(options.language)
    val compiled = toCompile.map {
      loaded =>
        options.typerImpl match {
          case TyperImpl.Legacy =>
            descriptor.make(loaded.typespace, options).translate()
          case TyperImpl.NewTyper =>
            // IMPL-7b/7c Phase A: all three languages (Scala/TypeScript/CSharp)
            // now have Domain-consuming translator surfaces, so the per-language
            // match collapses. The `DomainAsTypespace` adapter is left in place
            // for IMPL-10/11 to delete alongside the legacy adapter machinery.
            val newDomain = NewTyperPipeline.run(loaded.parsed)
            descriptor.makeDomain(newDomain, loaded.parsed, options).translate()
        }
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
