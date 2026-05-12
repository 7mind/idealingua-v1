package izumi.idealingua.translator.tocsharp.domain

import izumi.idealingua.translator.TranslatorExtension

/** Default C# translator extensions for the Domain-consuming pipeline.
  *
  * Mirrors `ScalaDefaultExtensions`: the legacy
  * `CSharpTranslator.defaultExtensions` (`JsonNetExtension`) is absorbed into
  * renderer-internal call sites under `tocsharp/domain/extensions/`
  * (`DomainCSJsonNetExtension`), invoked directly by the renderer that needs
  * it. The default seq is empty on the new path.
  *
  * Note: the legacy `NUnitExtension` was opt-in through
  * `CSharpBuildManifest.enableNUnit`; that opt-in is not exercised on the
  * Domain path (no NUnit twin exists yet — out of scope for IMPL-10c). The
  * manifest flag is preserved but currently inert on the Domain path.
  */
object CSharpDefaultExtensions {
  final val defaultExtensions: Seq[TranslatorExtension] = Seq.empty
}
