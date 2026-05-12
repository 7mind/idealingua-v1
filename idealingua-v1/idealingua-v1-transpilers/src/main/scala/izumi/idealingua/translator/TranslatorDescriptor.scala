package izumi.idealingua.translator

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.typespace.verification.VerificationRule

trait TranslatorDescriptor[TypedOptions] {
  def language: IDLLanguage
  def defaultExtensions: Seq[TranslatorExtension]
  def defaultManifest: BuildManifest
  def typedOptions(options: UntypedCompilerOptions): TypedOptions
  def makeDomain(domain: izumi.idealingua.typer.ir.Domain, parsed: DomainMeshResolved, options: UntypedCompilerOptions): Translator
  def makeHook(options: UntypedCompilerOptions): TranslationLayouter
  def rules: Seq[VerificationRule]
}
