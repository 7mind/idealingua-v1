package izumi.idealingua.translator

import izumi.idealingua.model.loader.LoadedDomain
import izumi.idealingua.translator.tocsharp.CSharpTranslatorDescriptor
import izumi.idealingua.translator.toscala.ScalaTranslatorDescriptor
import izumi.idealingua.translator.totypescript.TypescriptTranslatorDescriptor

class TypespaceCompilerBaseFacade(options: UntypedCompilerOptions) {
  def compile(toCompile: Seq[LoadedDomain.Success]): Layouted = {
    val descriptor = TypespaceCompilerBaseFacade.descriptor(options.language)
    val compiled = toCompile.map {
      loaded =>
        // PR-02 IMPL-13: the new-typer pipeline is run once per domain at
        // load time inside `ModelResolver`; its result is materialised on
        // `LoadedDomain.Success.domain`.  Translators read it directly — no
        // lazy re-run, no IDLException throw-bridge.
        descriptor.makeDomain(loaded.domain, loaded.parsed, options).translate()
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
