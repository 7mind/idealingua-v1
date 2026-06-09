package izumi.idealingua.translator.toscala.layout

import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.il.ast.raw.domains.Import
import izumi.idealingua.model.il.ast.typed.{DomainMetadata, NodeMeta}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.manifests.{ScalaBuildManifest, ScalaProjectLayout, SbtOptions}
import izumi.idealingua.translator.{CompilerOptions, Translated}
import org.scalatest.wordspec.AnyWordSpec

/** Regression guard: `ScalaLayouter` must NOT emit an
  * `idealingua-v1-runtime-rpc-http4s` dependency or `.jvm`
  * `unmanagedSourceDirectories` into the generated build.sbt, even when a
  * module carrying `meta("platform") == "jvm"` is present in the input.
  *
  * The test exercises the JVM-only path (enableScalaJs = false, single Scala
  * version) so the non-cross `.settings(...)` branch is taken — the path that
  * historically injected the http4s dependency and `.jvm` unmanaged dirs for an
  * MCP bridge module. Emitted MCP support is now pure platform-neutral data, so
  * neither must appear.
  */
final class ScalaLayouterSbtSpec extends AnyWordSpec {

  private val testGroupId = "com.example.test"

  /** JVM-only SBT manifest with emitMcpBridge=true and a single Scala version,
    * so isCrossBuild = false and the non-cross `.settings(...)` path is taken.
    */
  private val jvmOnlyManifest: ScalaBuildManifest = ScalaBuildManifest(
    common = BuildManifest.Common.example.copy(
      group        = testGroupId,
      izumiVersion = "sbt-spec-test-version",
    ),
    layout        = ScalaProjectLayout.SBT,
    sbt           = SbtOptions.example.copy(
      enableScalaJs = false,
      scalaVersions = List("2.13.18"),
    ),
    emitMcpBridge = true,
  )

  private val options: CompilerOptions[ScalaBuildManifest] =
    CompilerOptions(
      language           = izumi.idealingua.translator.IDLLanguage.Scala,
      manifest           = jvmOnlyManifest,
      withBundledRuntime = false,
      providedRuntime    = None,
    )

  /** A synthetic Translated carrying one module tagged `meta("platform") ->
    * "jvm"` — the precondition that historically triggered the http4s
    * dependency + `.jvm` unmanagedSourceDirectories injection.
    */
  private def syntheticTranslated(): Translated = {
    val domainId = DomainId(Seq("com", "example", "test"), "myservice")
    val meta = DomainMetadata(
      origin            = FSPath.Name("test"),
      directInclusions  = Seq.empty,
      directImports     = Seq.empty[Import],
      meta              = NodeMeta.empty,
    )
    val bridgeModule = Module(
      id      = ModuleId(Seq.empty, "MyServiceMcpRoutes.scala"),
      content = "// synthetic jvm-only bridge source",
      meta    = Map("platform" -> "jvm"),
    )
    Translated(domainId, meta, Seq(bridgeModule))
  }

  /** asSbtModule must NOT prepend `.jvm` to the source path even when a module
    * is tagged `meta("platform") == "jvm"`; every emitted module routes to the
    * shared `src/main/scala` (or `src/main/resources`) sourceset regardless of
    * the `platform` tag.
    */
  "ScalaLayouter.asSbtModule" should {
    "route a platform=jvm MCP Scala module to shared src/main/scala (not .jvm/...)" in {
      val layouter = new ScalaLayouter(options)
      val result   = layouter.layout(Seq(syntheticTranslated()))

      // Collect all emitted domain modules (excludes the build.sbt / runtime entries)
      val domainModules = result.emodules.map(_.module).filter(_.id.name.endsWith(".scala"))

      assert(domainModules.nonEmpty, "expected at least one emitted Scala module")

      domainModules.foreach {
        mod =>
          val pathStr = mod.id.path.mkString("/")
          assert(
            !pathStr.contains(".jvm"),
            s"Module path must NOT contain '.jvm': $pathStr/${mod.id.name}",
          )
          assert(
            pathStr.contains("src/main/scala"),
            s"Module path must contain 'src/main/scala': $pathStr/${mod.id.name}",
          )
      }
    }

    "route a platform=jvm MCP resource module to shared src/main/resources (not .jvm/...)" in {
      val layouter = new ScalaLayouter(options)
      // Construct a translated with a resource module tagged platform=jvm + resource=true
      val domainId = DomainId(Seq("com", "example", "test"), "myservice")
      val meta = DomainMetadata(
        origin            = FSPath.Name("test"),
        directInclusions  = Seq.empty,
        directImports     = Seq.empty[Import],
        meta              = NodeMeta.empty,
      )
      val resourceModule = Module(
        id      = ModuleId(Seq("mcp"), "MyService.mcp.json"),
        content = "{}",
        meta    = Map("platform" -> "jvm", "resource" -> "true"),
      )
      val translated = Translated(domainId, meta, Seq(resourceModule))
      val result     = layouter.layout(Seq(translated))

      val resourceModules = result.emodules.map(_.module).filter(_.id.name.endsWith(".json"))
      assert(resourceModules.nonEmpty, "expected at least one emitted resource module")

      resourceModules.foreach {
        mod =>
          val pathStr = mod.id.path.mkString("/")
          assert(
            !pathStr.contains(".jvm"),
            s"Resource module path must NOT contain '.jvm': $pathStr/${mod.id.name}",
          )
          assert(
            pathStr.contains("src/main/resources"),
            s"Resource module path must contain 'src/main/resources': $pathStr/${mod.id.name}",
          )
      }
    }
  }

  "ScalaLayouter (JVM-only, emitMcpBridge=true)" should {
    "produce a build.sbt with no idealingua-v1-runtime-rpc-http4s dependency" in {
      val layouter = new ScalaLayouter(options)
      val result   = layouter.layout(Seq(syntheticTranslated()))

      val buildSbt = result.emodules
        .map(_.module)
        .find(_.id.name == "build.sbt")
        .getOrElse(fail("build.sbt not found in layouter output"))
        .content

      assert(
        !buildSbt.contains("idealingua-v1-runtime-rpc-http4s"),
        s"build.sbt must NOT reference idealingua-v1-runtime-rpc-http4s;\n$buildSbt",
      )
    }

    "produce a build.sbt with no .jvm unmanagedSourceDirectories" in {
      val layouter = new ScalaLayouter(options)
      val result   = layouter.layout(Seq(syntheticTranslated()))

      val buildSbt = result.emodules
        .map(_.module)
        .find(_.id.name == "build.sbt")
        .getOrElse(fail("build.sbt not found in layouter output"))
        .content

      assert(
        !buildSbt.contains("unmanagedSourceDirectories"),
        s"build.sbt must NOT contain unmanagedSourceDirectories;\n$buildSbt",
      )
    }
  }
}
