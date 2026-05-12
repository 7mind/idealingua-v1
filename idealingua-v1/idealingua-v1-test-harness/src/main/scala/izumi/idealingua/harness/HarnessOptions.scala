package izumi.idealingua.harness

import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.BuildManifest.Common
import izumi.idealingua.model.publishing.manifests.{
  CSharpBuildManifest,
  CSharpProjectLayout,
  NugetOptions,
  SbtOptions,
  ScalaBuildManifest,
  ScalaProjectLayout,
  TypeScriptBuildManifest,
  TypeScriptProjectLayout,
  YarnOptions,
}
import izumi.idealingua.model.publishing.{ProjectVersion}
import izumi.idealingua.translator.{IDLLanguage, TyperImpl, TypespaceCompilerBaseFacade, UntypedCompilerOptions}

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
    sbt    = SbtOptions.example.copy(scalaVersions = List("2.13.18", "3.8.3")),
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

  def manifestFor(lang: IDLLanguage): BuildManifest = lang match {
    case IDLLanguage.Scala      => scala
    case IDLLanguage.Typescript => typescript
    case IDLLanguage.CSharp     => csharp
  }

  def optionsFor(lang: IDLLanguage): UntypedCompilerOptions = optionsFor(lang, TyperImpl.NewTyper)

  /** PR-02 IMPL-7a.2 IMPL-9 compile gate: `regenerateGoldens` / `verifyGoldens`
    * use this entry point with `TyperImpl.NewTyper` so the on-disk Layer A
    * Scala goldens (already on `Compile / unmanagedSourceDirectories`) are
    * the new-typer output. Standard sbt compile then transitively type-checks
    * every emitted module across the 28-domain corpus, surfacing any type
    * error in the new-typer Scala backend that bytewise-equality alone
    * (`ScalaTranslatorByteParitySpec`) cannot detect. */
  def optionsFor(lang: IDLLanguage, typer: TyperImpl): UntypedCompilerOptions = UntypedCompilerOptions(
    language           = lang,
    extensions         = TypespaceCompilerBaseFacade.descriptor(lang).defaultExtensions,
    target             = None,
    manifest           = manifestFor(lang),
    withBundledRuntime = false,
    providedRuntime    = None,
    zipOutput          = false,
    typerImpl          = typer,
  )
}
