package izumi.idealingua.translator

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.publishing.BuildManifest

trait TranslatorDescriptor[TypedOptions] {
  def language: IDLLanguage
  def defaultManifest: BuildManifest
  def typedOptions(options: UntypedCompilerOptions): TypedOptions
  def makeDomain(domain: izumi.idealingua.typer.ir.Domain, parsed: DomainMeshResolved, options: UntypedCompilerOptions): Translator
  def makeHook(options: UntypedCompilerOptions): TranslationLayouter
}
