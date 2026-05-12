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

  /** Default-typer entrypoint: callers that don't pin a typer get
    * `TyperImpl.NewTyper` (the canonical default since IMPL-9). The
    * two-argument overload is retained for `GoldenCompile.compileAll`, which
    * pins `TyperImpl.Legacy` on the TypeScript backend until the latent
    * NewTyper-TS golden divergences surfaced by IMPL-10b are reconciled. */
  def optionsFor(lang: IDLLanguage): UntypedCompilerOptions = optionsFor(lang, TyperImpl.NewTyper)

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
