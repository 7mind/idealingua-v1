package izumi.idealingua.translator.compat

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.{ScalaBuildManifest, ScalaProjectLayout, SbtOptions}
import izumi.idealingua.model.publishing.ProjectVersion
import izumi.idealingua.translator.{IDLLanguage, TypespaceCompilerBaseFacade, TyperImpl, UntypedCompilerOptions}
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.{Files, Paths}

/** Smoke test for the `--typer=new` code path (PR-02 IMPL-6).
  *
  * Asserts the new-typer pipeline (`NewTyperPipeline`) runs end-to-end
  * against a real `.domain` fixture and that the Scala translator (driven
  * through `DomainAsTypespace`) produces non-empty output.
  *
  * Byte-parity vs the legacy path is intentionally NOT asserted here — that
  * is the harness's job in PR-03's parity gate (before IMPL-9 flips the
  * default to `New`).
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

  private def optionsFor(impl: TyperImpl): UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = IDLLanguage.Scala,
      extensions         = TypespaceCompilerBaseFacade.descriptor(IDLLanguage.Scala).defaultExtensions,
      target             = None,
      manifest           = pinnedScala,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
      typerImpl          = impl,
    )

  "TypespaceCompilerBaseFacade with TyperImpl.NewTyper" should {
    "compile a small fixture domain to Scala source via the new typer pipeline" in {
      val loaded = loadEnumsDomain()
      val out    = new TypespaceCompilerBaseFacade(optionsFor(TyperImpl.NewTyper)).compile(Seq(loaded))
      assert(out.emodules.nonEmpty, "new-typer path produced zero output modules")
    }

    "default to TyperImpl.Legacy when typerImpl is not provided" in {
      val defaults = UntypedCompilerOptions(
        language   = IDLLanguage.Scala,
        extensions = Seq.empty,
        target     = None,
        manifest   = pinnedScala,
      )
      assert(defaults.typerImpl == TyperImpl.Legacy)
    }
  }
}
