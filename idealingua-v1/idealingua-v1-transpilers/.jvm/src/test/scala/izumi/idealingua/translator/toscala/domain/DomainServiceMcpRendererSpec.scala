package izumi.idealingua.translator.toscala.domain

import izumi.idealingua.il.loader.{LocalModelLoaderContext, ModelResolver}
import izumi.idealingua.model.publishing.BuildManifest
import izumi.idealingua.model.publishing.ProjectVersion
import izumi.idealingua.model.publishing.manifests.{SbtOptions, ScalaBuildManifest, ScalaProjectLayout}
import izumi.idealingua.translator.{ExtendedModule, IDLLanguage, TypespaceCompilerBaseFacade, UntypedCompilerOptions}
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.{Files, Paths}

/** The MCP bridge renderer emits a platform-neutral, JS-linkable POINTER
  * (`<Service>Mcp` holding a `McpServiceResource`) instead of inlining schemas
  * or reading the classpath resource. This spec pins the emitted Scala source
  * shape: it must construct an `McpServiceResource`, import it, and must NOT
  * carry schemas, resource-reading code (`getResources`), `McpServiceMeta` /
  * `McpToolMeta`, or any http4s transport symbols.
  */
final class DomainServiceMcpRendererSpec extends AnyWordSpec {

  private def repoRoot = {
    val cwd                       = Paths.get(System.getProperty("user.dir"))
    var found: java.nio.file.Path = null
    var p: java.nio.file.Path     = cwd.toAbsolutePath
    while (p != null) {
      if (Files.exists(p.resolve("idealingua-v1/idealingua-v1-test-defs"))) found = p
      p                                                                           = p.getParent
    }
    require(found != null, s"could not locate repo root from $cwd")
    found
  }

  private val corpusRoot = repoRoot.resolve(
    "idealingua-v1/idealingua-v1-test-defs/src/main/resources/defs/main-tests/source"
  )

  private val mcpScala: ScalaBuildManifest = ScalaBuildManifest(
    common = BuildManifest.Common.example.copy(
      izumiVersion = "mcp-renderer-spec",
      version      = ProjectVersion(version = "0.0.0", release = true, snapshotQualifier = "spec"),
    ),
    layout        = ScalaProjectLayout.PLAIN,
    sbt           = SbtOptions.example.copy(scalaVersions = List("3.9.0", "3.8.3")),
    emitMcpBridge = true,
  )

  private def loadServiceDomain() = {
    val context  = new LocalModelLoaderContext(Seq(corpusRoot), Seq.empty[File])
    val resolver = new ModelResolver()
    val resolved = resolver.resolve(context.loader.load())
    val pick     = resolved.successful.find(_.parsed.id.toPackage.mkString(".") == "coverage.services.allsouts")
    require(pick.isDefined, s"coverage.services.allsouts not found under $corpusRoot")
    pick.get
  }

  private def scalaOptions: UntypedCompilerOptions =
    UntypedCompilerOptions(
      language           = IDLLanguage.Scala,
      target             = None,
      manifest           = mcpScala,
      withBundledRuntime = false,
      providedRuntime    = None,
      zipOutput          = false,
    )

  "DomainServiceMcpRenderer (JS-linkable pointer)" should {
    "emit a McpServiceResource pointer object with no schemas, resource I/O, or http4s transport symbols" in {
      val loaded = loadServiceDomain()
      val out    = new TypespaceCompilerBaseFacade(scalaOptions).compile(Seq(loaded))

      val mcpSource = out.emodules.collectFirst {
        case ExtendedModule.DomainModule(_, m) if m.id.name == "AllOutputsServiceMcp.scala" => m.content
      }.getOrElse(fail("no AllOutputsServiceMcp.scala module emitted with emitMcpBridge = true"))

      assert(mcpSource.contains("McpServiceResource("), "emitted source must construct McpServiceResource")
      assert(
        mcpSource.contains("import izumi.idealingua.runtime.rpc.McpServiceResource"),
        "emitted source must import the pointer type",
      )

      // It is a pointer, NOT a meta: no schemas, no resource-reading, no http4s.
      assert(!mcpSource.contains("McpServiceMeta"), "pointer source must NOT reference McpServiceMeta")
      assert(!mcpSource.contains("McpToolMeta"), "pointer source must NOT reference McpToolMeta")
      assert(!mcpSource.contains("getResources"), "pointer source must NOT read the classpath resource")
      assert(!mcpSource.contains("getResourceAsStream"), "pointer source must NOT read the classpath resource")
      assert(!mcpSource.contains("inputSchema"), "pointer source must NOT inline schemas")
      assert(!mcpSource.contains("io.circe"), "pointer source must NOT depend on circe")

      assert(!mcpSource.contains("org.http4s"), "emitted source must NOT import org.http4s")
      assert(!mcpSource.contains("HttpRoutes"), "emitted source must NOT reference HttpRoutes")
      assert(!mcpSource.contains("Http4sDsl"), "emitted source must NOT reference Http4sDsl")
      assert(!mcpSource.contains("cats.effect"), "emitted source must NOT import cats.effect")
      assert(!mcpSource.contains("IRTServerMultiplexor"), "emitted source must NOT reference IRTServerMultiplexor")
    }

    // Both the Scala source module and the .mcp.json resource module must be
    // platform-neutral (no platform=jvm tag) so asSbtModule routes them to the
    // shared src/main/scala and src/main/resources sourcesets.
    "emit MCP modules without platform=jvm meta (shared sourceset)" in {
      val loaded = loadServiceDomain()
      val out    = new TypespaceCompilerBaseFacade(scalaOptions).compile(Seq(loaded))

      val mcpModules = out.emodules.collect {
        case ExtendedModule.DomainModule(_, m) if m.id.name.endsWith("Mcp.scala") || m.id.name.endsWith(".mcp.json") => m
      }
      assert(mcpModules.nonEmpty, "no MCP modules emitted with emitMcpBridge = true")

      mcpModules.foreach {
        m =>
          assert(
            !m.meta.get("platform").contains("jvm"),
            s"MCP module ${m.id.name} must NOT carry meta(platform=jvm); got meta=${m.meta}",
          )
      }

      // Verify the .mcp.json resource module still carries the resource=true tag.
      val resourceModules = mcpModules.filter(_.id.name.endsWith(".mcp.json"))
      assert(resourceModules.nonEmpty, "no .mcp.json resource module emitted")
      resourceModules.foreach {
        m =>
          assert(
            m.meta.get("resource").contains("true"),
            s".mcp.json module ${m.id.name} must carry meta(resource=true); got meta=${m.meta}",
          )
      }
    }
  }
}
