package izumi.idealingua.translator

import izumi.idealingua.model.output.Module
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.{CSharpBuildManifest, ScalaBuildManifest, TypeScriptBuildManifest}

import java.nio.file.Path

case class ProvidedRuntime(modules: Seq[Module]) {
  def isEmpty: Boolean = modules.isEmpty

  def maybe: Option[ProvidedRuntime] = {
    if (isEmpty) {
      None
    } else {
      Some(this)
    }
  }

  def ++(other: ProvidedRuntime): ProvidedRuntime = {
    ProvidedRuntime(modules ++ other.modules)
  }
}

object ProvidedRuntime {
  def empty: ProvidedRuntime = ProvidedRuntime(Seq.empty)
}

sealed trait AbstractCompilerOptions[M <: BuildManifest] {
  def language: IDLLanguage

  def withBundledRuntime: Boolean

  def manifest: M

  def providedRuntime: Option[ProvidedRuntime]
}

final case class CompilerOptions[M <: BuildManifest](
  language: IDLLanguage,
  manifest: M,
  withBundledRuntime: Boolean              = true,
  providedRuntime: Option[ProvidedRuntime] = None,
) extends AbstractCompilerOptions[M]

object CompilerOptions {
  type TypescriptTranslatorOptions = CompilerOptions[TypeScriptBuildManifest]
  type CSharpTranslatorOptions     = CompilerOptions[CSharpBuildManifest]
  type ScalaTranslatorOptions      = CompilerOptions[ScalaBuildManifest]

  // IMPL-12 (2026-05-12): the `E` extension-type parameter retired. The
  // legacy `<Lang>TranslatorExtension` marker traits and their
  // `defaultExtensions` seqs were inert post-IMPL-10c (no renderer iterated
  // `options.extensions`). `from` is now a parameter-less projection that
  // narrows the `BuildManifest` payload via cast.
  def from[M <: BuildManifest](options: UntypedCompilerOptions): CompilerOptions[M] = {
    val manifest = options.manifest.asInstanceOf[M]
    CompilerOptions(options.language, manifest, options.withBundledRuntime, options.providedRuntime)
  }
}

final case class UntypedCompilerOptions(
  language: IDLLanguage,
  target: Option[Path],
  manifest: BuildManifest,
  withBundledRuntime: Boolean              = true,
  providedRuntime: Option[ProvidedRuntime] = None,
  zipOutput: Boolean                       = true,
) extends AbstractCompilerOptions[BuildManifest] {
  override def toString: String = {
    val rtRepr  = Option(withBundledRuntime).filter(_ == true).map(_ => "+rtb").getOrElse("-rtb")
    val rtfRepr = providedRuntime.map(rt => s"rtu=${rt.modules.size}").getOrElse("-rtu")
    Seq(language, rtRepr, rtfRepr).mkString(" ")
  }
}
