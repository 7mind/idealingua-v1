package izumi.idealingua.model.publishing.manifests

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.BuildManifest.Common

/** Minimal build manifest for the JSON Schema + MCP emission target.
  *
  * The schema emitter is a data-only target — no runtime, no SDK, no project
  * scaffolding — so the manifest carries only the shared `Common` block. The
  * `info.version` of every emitted OpenAPI document falls back through:
  *   1. domain-level annotation `meta.version` (NodeMeta.annos, future M2+);
  *   2. `common.version` from this manifest (per plan D24);
  *   3. the compile-time default ("0.0.1" + UNSET qualifier).
  */
case class SchemaBuildManifest(
  common: Common
) extends BuildManifest

object SchemaBuildManifest {
  def example: SchemaBuildManifest = SchemaBuildManifest(
    common = BuildManifest.Common.example
  )
}
