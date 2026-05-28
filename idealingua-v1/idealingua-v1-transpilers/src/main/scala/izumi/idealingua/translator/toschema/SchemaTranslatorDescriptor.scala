package izumi.idealingua.translator.toschema

import izumi.idealingua.model.il.ast.raw.domains.DomainMeshResolved
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.SchemaBuildManifest
import izumi.idealingua.translator._
import izumi.idealingua.translator.toschema.layout.SchemaLayouter
import izumi.idealingua.typer.ir.Domain
import izumi.idealingua.util.Parallel

object SchemaTranslatorDescriptor extends TranslatorDescriptor[CompilerOptions[SchemaBuildManifest]] {
  override def language: IDLLanguage = IDLLanguage.JsonSchema

  override def defaultManifest: BuildManifest = SchemaBuildManifest.example

  override def typedOptions(options: UntypedCompilerOptions): CompilerOptions[SchemaBuildManifest] =
    CompilerOptions.from[SchemaBuildManifest](options)

  override def makeDomain(
    domain: Domain,
    parsed: DomainMeshResolved,
    options: UntypedCompilerOptions,
    parallel: Parallel = Parallel.Default,
  ): Translator =
    // Schema translator threads a single mutable `LinkedHashMap[String, Json]`
    // in declaration order — not amenable to per-member parallelism. The
    // descriptor accepts `parallel` for signature uniformity but the schema
    // body remains sequential. Schema emit is ~65ms / 39 domains; not a hot spot.
    new DomainSchemaTranslator(domain, parsed, typedOptions(options))

  override def makeHook(options: UntypedCompilerOptions): TranslationLayouter =
    new SchemaLayouter(typedOptions(options))
}
