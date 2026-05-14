package izumi.idealingua.translator.toschema.layout

import izumi.idealingua.model.publishing.manifests.SchemaBuildManifest
import izumi.idealingua.translator.{CompilerOptions, Layouted, TranslationLayouter, Translated}

/** Minimal layouter for the JSON Schema target. M1 emits one `schema.json`
  * per domain at `<output>/<domain.package-as-path>/schema.json`. No
  * provided-runtime modules are emitted (the schema target ships no
  * runtime artifacts).
  */
final class SchemaLayouter(options: CompilerOptions[SchemaBuildManifest]) extends TranslationLayouter {
  override def layout(outputs: Seq[Translated]): Layouted = {
    val modules = toDomainModules(outputs) ++ toRuntimeModules(options)
    Layouted(modules)
  }
}
