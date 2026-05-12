package izumi.idealingua.translator.toscala.extensions

import izumi.idealingua.translator.TranslatorExtension

/** Marker trait for Scala translator extensions.
  *
  * Pre-IMPL-10c this trait carried legacy callback methods consumed by the
  * legacy `STContext`-based dispatcher (`ScalaTranslatorExtensions`). With
  * IMPL-10c (legacy-translator-tree deletion) the dispatcher and every
  * concrete subclass moved into `toscala/domain/extensions/` as
  * direct-invocation helpers (`DomainAnyvalExtension`,
  * `DomainCastSimilarExtension`, `DomainCastDownExpandExtension`,
  * `DomainCastUpExtension`, `DomainCirceDerivationTranslatorExtension`),
  * each called explicitly by the renderer that needs it.
  *
  * The trait survives only to keep
  * `CompilerOptions[ScalaTranslatorExtension, ScalaBuildManifest]`'s type
  * parameter inhabited (and the parallel test-spec call-sites compiling).
  * It will be retired when `CompilerOptions[E, M]` collapses to a single
  * extension-less parameter list.
  */
trait ScalaTranslatorExtension extends TranslatorExtension
