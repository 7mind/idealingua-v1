package izumi.idealingua.translator.totypescript.domain

import izumi.idealingua.translator.TranslatorExtension

/** Default TypeScript translator extensions for the Domain-consuming pipeline.
  *
  * Mirrors `ScalaDefaultExtensions`: the legacy
  * `TypeScriptTranslator.defaultExtensions`
  * (`EnumHelpersExtension`, `IntrospectionExtension`) is absorbed into
  * renderer-internal call sites under `totypescript/domain/extensions/`
  * (`DomainTSEnumHelpersExtension`, `DomainTSIntrospectionExtension`), each
  * invoked directly by the renderer that needs it. The default seq is empty
  * on the new path.
  */
object TypeScriptDefaultExtensions {
  final val defaultExtensions: Seq[TranslatorExtension] = Seq.empty
}
