package izumi.idealingua.translator.toschema

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.SchemaBuildManifest
import izumi.idealingua.translator._
import izumi.idealingua.translator.toschema.layout.SchemaLayouter
import izumi.idealingua.typer.ir.Domain

object SchemaTranslatorDescriptor extends TranslatorDescriptor[CompilerOptions[SchemaBuildManifest]] {
  override def language: IDLLanguage = IDLLanguage.JsonSchema

  override def defaultManifest: BuildManifest = SchemaBuildManifest.example

  override def typedOptions(options: UntypedCompilerOptions): CompilerOptions[SchemaBuildManifest] =
    CompilerOptions.from[SchemaBuildManifest](options)

  override def makeDomain(
    domain: Domain,
    parsed: DomainMeshResolved,
    options: UntypedCompilerOptions,
  ): Translator =
    new DomainSchemaTranslator(domain, parsed, typedOptions(options))

  override def makeHook(options: UntypedCompilerOptions): TranslationLayouter =
    new SchemaLayouter(typedOptions(options))
}
