package izumi.idealingua.harness

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.BuildManifest.Common
import izumi.idealingua.model.publishing.manifests.{CSharpBuildManifest, CSharpProjectLayout, NugetOptions, SbtOptions, ScalaBuildManifest, ScalaProjectLayout, SchemaBuildManifest, TypeScriptBuildManifest, TypeScriptProjectLayout, YarnOptions}
import izumi.idealingua.model.publishing.ProjectVersion
import izumi.idealingua.translator.{IDLLanguage, UntypedCompilerOptions}

object HarnessOptions {

  private val pinnedVersion: ProjectVersion =
    ProjectVersion(version = "0.0.0", release = true, snapshotQualifier = "test")

  private val pinnedCommon: Common =
    BuildManifest.Common.example.copy(
      izumiVersion = "test-harness",
      version      = pinnedVersion,
    )

  val scala: ScalaBuildManifest = ScalaBuildManifest(
    common = pinnedCommon,
    layout = ScalaProjectLayout.PLAIN,
    sbt    = SbtOptions.example.copy(scalaVersions = List("3.9.0", "3.8.3")),
  )

  val typescript: TypeScriptBuildManifest = TypeScriptBuildManifest(
    common = pinnedCommon,
    layout = TypeScriptProjectLayout.PLAIN,
    yarn   = YarnOptions.example,
  )

  val csharp: CSharpBuildManifest = CSharpBuildManifest(
    common      = pinnedCommon,
    nuget       = NugetOptions.example,
    layout      = CSharpProjectLayout.PLAIN,
    enableNUnit = false,
  )

  val schema: SchemaBuildManifest = SchemaBuildManifest(common = pinnedCommon)

  def manifestFor(lang: IDLLanguage): BuildManifest = lang match {
    case IDLLanguage.Scala      => scala
    case IDLLanguage.Typescript => typescript
    case IDLLanguage.CSharp     => csharp
    case IDLLanguage.JsonSchema => schema
  }

  /** Standard harness options. IMPL-10c retired the `TyperImpl` enum (only
    * the new-typer pipeline remains), so this entrypoint no longer takes a
    * typer parameter.
    */
  def optionsFor(lang: IDLLanguage): UntypedCompilerOptions = UntypedCompilerOptions(
    language           = lang,
    target             = None,
    manifest           = manifestFor(lang),
    withBundledRuntime = false,
    providedRuntime    = None,
    zipOutput          = false,
  )
}
