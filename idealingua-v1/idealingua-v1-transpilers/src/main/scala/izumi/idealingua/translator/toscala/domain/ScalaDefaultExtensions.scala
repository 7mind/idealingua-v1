package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.translator.TranslatorExtension

/** Default Scala translator extensions for the Domain-consuming pipeline.
  *
  * The legacy `ScalaTranslator.defaultExtensions` (a `Seq[ScalaTranslatorExtension]`)
  * was consumed by the legacy `STContext` extension dispatcher
  * (`ScalaTranslatorExtensions`) which iterated over it for every typedef.
  *
  * `DomainScalaTranslator` does NOT iterate `options.extensions`; the four
  * legacy `defaultExtensions` (`AnyvalExtension`, `CastSimilarExtension`,
  * `CastDownExpandExtension`, `CastUpExtension`, `CirceDerivationTranslatorExtension`)
  * were absorbed into renderer-internal call sites under
  * `toscala/domain/extensions/` (`DomainAnyvalExtension`, `DomainCastSimilarExtension`,
  * `DomainCastDownExpandExtension`, `DomainCastUpExtension`,
  * `DomainCirceDerivationTranslatorExtension`), each invoked directly by the
  * renderer that needs it. The default-extensions seq therefore plays no
  * runtime role on the new path and is empty.
  *
  * The descriptor still exposes `defaultExtensions: Seq[TranslatorExtension]`
  * so existing call sites (`CommandlineIDLCompiler.getExt`,
  * `HarnessOptions.optionsFor`) continue to compile without change. The
  * harvested seq round-trips through `UntypedCompilerOptions.extensions` and
  * is filtered to `Seq[ScalaTranslatorExtension]` by `CompilerOptions.from`,
  * which yields an empty seq — exactly what the new path expects.
  */
object ScalaDefaultExtensions {
  final val defaultExtensions: Seq[TranslatorExtension] = Seq.empty
}
