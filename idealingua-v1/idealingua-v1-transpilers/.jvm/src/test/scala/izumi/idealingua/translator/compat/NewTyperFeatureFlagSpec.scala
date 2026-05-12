package izumi.idealingua.translator.compat

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.{
  CSharpBuildManifest,
  ScalaBuildManifest,
  ScalaProjectLayout,
  SbtOptions,
  TypeScriptBuildManifest,
}
import izumi.idealingua.model.publishing.ProjectVersion
import izumi.idealingua.translator.{IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.{Files, Paths}

/** Smoke test for the new-typer pipeline (PR-02 IMPL-6 / IMPL-10c).
  *
  * Originally tracked the `--typer=new` opt-in code path. IMPL-9 flipped the
  * default to new-typer; IMPL-10c retired the legacy translator tree and the
  * `TyperImpl` enum entirely, so this spec collapses to a per-language
  * end-to-end smoke test (the only path is the new-typer pipeline through
  * `Domain<Lang>Translator`).
  */
final class NewTyperFeatureFlagSpec extends AnyWordSpec {

  // Resolve the small `idltest/enums.domain` fixture from the test-defs
  // resource tree.  The harness corpus lives under
  // idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source.
  // We resolve via the file-system (not the classpath) so the loader sees a
  // real path; tests run from sbt's cwd at the repo root.
  private def repoRoot = {
    val cwd = Paths.get(System.getProperty("user.dir"))
    // sbt may invoke tests with cwd = repo root or the module dir.  Find the
    // outermost ancestor whose `idealingua-v1/idealingua-v1-test-defs` exists
    // (so we don't get fooled by the nested layout: the test-defs module
    // lives at `<repoRoot>/idealingua-v1/idealingua-v1-test-defs`, where
    // `<repoRoot>` already contains `idealingua-v1/`).  Walk up from cwd; on
    // each ancestor that resolves the target, record it; the OUTERMOST
    // such ancestor is the true repo root.
    var found: java.nio.file.Path = null
    var p: java.nio.file.Path     = cwd.toAbsolutePath
    while (p != null) {
      if (Files.exists(p.resolve("idealingua-v1/idealingua-v1-test-defs"))) {
        found = p
      }
      p = p.getParent
    }
    require(found != null, s"could not locate repo root from $cwd")
    found
  }

  private val corpusRoot = repoRoot.resolve(
    "idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source"
  )

  private val pinnedScala: ScalaBuildManifest = ScalaBuildManifest(
    common = BuildManifest.Common.example.copy(
      izumiVersion = "new-typer-smoke",
      version      = ProjectVersion(version = "0.0.0", release = true, snapshotQualifier = "smoke"),
    ),
    layout = ScalaProjectLayout.PLAIN,
    sbt    = SbtOptions.example.copy(scalaVersions = List("2.13.18", "3.8.3")),
  )

  private def loadEnumsDomain() = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val rules    = TypespaceCompilerBaseFacade.descriptors.flatMap(_.rules)
    val resolver = new ModelResolver(rules)
    val loaded   = context.loader.load()
    val resolved = resolver.resolve(loaded)
    val all      = resolved.successful
    val pick     = all.find(_.typespace.domain.id.toPackage.mkString(".") == "idltest.enums")
    require(
      pick.isDefined,
      s"idltest.enums not found. corpusRoot=$corpusRoot exists=${Files.exists(corpusRoot)} " +
        s"successful=${all.map(_.typespace.domain.id).mkString(",")}",
    )
    pick.get
  }

  private def scalaOptions: UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = IDLLanguage.Scala,
      extensions         = TypespaceCompilerBaseFacade.descriptor(IDLLanguage.Scala).defaultExtensions,
      target             = None,
      manifest           = pinnedScala,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )

  private def tsOptions: UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = IDLLanguage.Typescript,
      extensions         = TypespaceCompilerBaseFacade.descriptor(IDLLanguage.Typescript).defaultExtensions,
      target             = None,
      manifest           = TypeScriptBuildManifest.example,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )

  private def csOptions: UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = IDLLanguage.CSharp,
      extensions         = TypespaceCompilerBaseFacade.descriptor(IDLLanguage.CSharp).defaultExtensions,
      target             = None,
      manifest           = CSharpBuildManifest.example,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )

  "TypespaceCompilerBaseFacade (new-typer pipeline, post-IMPL-10c)" should {
    "compile a small fixture domain to Scala source" in {
      val loaded = loadEnumsDomain()
      val out    = new TypespaceCompilerBaseFacade(scalaOptions).compile(Seq(loaded))
      assert(out.emodules.nonEmpty, "new-typer Scala path produced zero output modules")
    }

    "compile a small fixture domain to TypeScript via DomainTypeScriptTranslator" in {
      val loaded = loadEnumsDomain()
      val out    = new TypespaceCompilerBaseFacade(tsOptions).compile(Seq(loaded))
      assert(out.emodules.nonEmpty, "new-typer TS path produced zero output modules")
    }

    "compile a small fixture domain to C# via DomainCSharpTranslator" in {
      val loaded = loadEnumsDomain()
      val out    = new TypespaceCompilerBaseFacade(csOptions).compile(Seq(loaded))
      assert(out.emodules.nonEmpty, "new-typer C# path produced zero output modules")
    }
  }
}
