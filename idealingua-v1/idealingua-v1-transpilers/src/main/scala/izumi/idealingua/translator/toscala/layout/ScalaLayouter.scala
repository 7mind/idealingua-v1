package izumi.idealingua.translator.toscala.layout

import izumi.fundamentals.platform.build.MacroParameters
import izumi.idealingua.model.common.DomainId
import izumi.idealingua.model.output.{Module, ModuleId}
import izumi.idealingua.model.publishing.manifests.ScalaProjectLayout
import izumi.idealingua.translator.CompilerOptions.ScalaTranslatorOptions
import izumi.idealingua.translator.*

case class RawExpr(e: String)

class ScalaLayouter(options: ScalaTranslatorOptions) extends TranslationLayouter {
  private val naming      = new ScalaNamingConvention(options.manifest.sbt.projectNaming)
  private val idlcGroupId = MacroParameters.projectGroupId().getOrElse("UNSET-GROUP-ID")

  override def layout(outputs: Seq[Translated]): Layouted = {
    def regularProject(id: String): String = {
      s"""(project in file("$id"))"""
    }

    def crossProject(id: String): String = {
      if (options.manifest.sbt.enableScalaJs) {
        s"""(crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).in(file("$id")))"""
      } else {
        regularProject(id)
      }
    }

    import SbtDslOp.*

    val modules = options.manifest.layout match {
      case ScalaProjectLayout.PLAIN =>
        withRuntime(options, outputs)

      case ScalaProjectLayout.SBT =>
        val projectModules = outputs.flatMap {
          out =>
            val did = out.domainId

            asSbtModule(out.modules, did)
              .map(m => ExtendedModule.DomainModule(did, m))
        }

        val rtid = DomainId(idlcGroupId.split('.').toIndexedSeq, "irt")

        val runtimeModules = asSbtModule(toRuntimeModules(options).map(_.module), rtid)
          .map(m => ExtendedModule.RuntimeModule(m))

        val projects = outputs.map {
          out =>
            naming.projectId(out.domainId) -> out
        }.toMap

        val projIds = projects.keys.toList.sorted

        val idlVersion = options.manifest.common.izumiVersion

        val renderer = new SbtRenderer()

        val projectDeps = renderer.renderOp {
          val versionOp = if (options.manifest.sbt.enableScalaJs) "%%%" else "%%"

          "libraryDependencies" -> Append(
            Seq(
              RawExpr(s""" "$idlcGroupId" $versionOp "idealingua-v1-runtime-rpc-scala" % "$idlVersion" """),
              RawExpr(s""" "$idlcGroupId" $versionOp "idealingua-v1-model" % "$idlVersion" """),
            ),
            List(Scope.Project),
          )
        }

        val moduleSettings = crossModuleSettings(projectDeps)

        val projDefs = projIds.map {
          id =>
            val d    = projects(id)
            val deps = d.meta.directImports.map(i => s"`${naming.projectId(i.id)}`")

            val depends = if (deps.nonEmpty) {
              deps.mkString("\n  .dependsOn(\n    ", ",\n    ", "\n  )")
            } else {
              ""
            }

            // A project carrying an emitMcpBridge http4s bridge (tagged
            // `platform=jvm`) needs http4s on the classpath + kind-projector,
            // and — in the JVM-only layout — its `.jvm` sources wired in.
            val hasMcpBridge   = d.modules.exists(_.meta.get("platform").contains("jvm"))
            val bridgeSettings = if (hasMcpBridge) mcpBridgeSettings(idlVersion) else ""

            s"""lazy val `$id` = ${crossProject(id)}$depends$moduleSettings$bridgeSettings"""
        }

        val bundleId = naming.bundleId

        val root = {
          val rootId = naming.pkgId
          val rootSettings = {
            s""".settings(
               |   crossScalaVersions := Nil,
               |   publish / skip := true
               |)""".stripMargin
          }
          val aggregatedProjects = (projIds ++ Seq(bundleId))
            .flatMap(
              id =>
                if (options.manifest.sbt.enableScalaJs) {
                  Seq(s"`$id`.js", s"`$id`.jvm")
                } else {
                  Seq(s"`$id`")
                }
            ).mkString(",\n    ")

          s"""
             |lazy val `$rootId` = ${regularProject(".")}
             |  .aggregate(
             |    $aggregatedProjects
             |  )$rootSettings
             |         """.stripMargin
        }

        val allDeps = projIds.map(i => s"`$i`")
        val depends = if (allDeps.nonEmpty) {
          allDeps.mkString("\n  .dependsOn(\n    ", ",\n    ", "\n  )")
        } else ""

        val bundle = s"lazy val `$bundleId` = ${crossProject(bundleId)}$depends$moduleSettings"

        // Workaround for sbt error due to circe-core version 0.14+ being too far away from required by circe-derivation 0.13.0-M5
        val circeDerivationWorkaround = Seq(
          "libraryDependencySchemes" -> Append(
            Seq(
              RawExpr(""""io.circe" %% "circe-core" % VersionScheme.Always"""),
              RawExpr(""""io.circe" %% "circe-core_sjs1" % VersionScheme.Always"""),
            )
          )
        )

        val resolvers = if (idlVersion.endsWith("SNAPSHOT")) {
          Seq("resolvers" -> Append(RawExpr("Opts.resolver.sonatypeSnapshots")))
        } else {
          Seq("resolvers" -> Append(RawExpr("Opts.resolver.sonatypeReleases")))
        }

        val docs = Seq(
          "publishArtifact" -> Assign(options.manifest.sbt.enableDocs.getOrElse(false), List(Scope.ThisBuild, Scope.Custom("packageDoc")))
        )

        val metadata = Seq(
          "name"         -> Assign(options.manifest.common.name, Scope.Project),
          "organization" -> Assign(options.manifest.common.group),
          "version"      -> Assign(renderVersion(options.manifest.common.version)),
          "homepage"     -> Assign(Some(options.manifest.common.website)),
          "licenses"     -> Append(options.manifest.common.licenses),
        )

        val keys = (docs ++ metadata ++ resolvers ++ circeDerivationWorkaround).map(renderer.renderOp)

        val content = keys ++ projDefs ++ Seq(bundle, root)

        val sbtModules = Seq(
          ExtendedModule.RuntimeModule(Module(ModuleId(Seq.empty, "build.sbt"), content.map(_.trim).mkString("\n\n"))),
          ExtendedModule.RuntimeModule(
            Module(
              ModuleId(Seq("project"), "build.properties"),
              s"sbt.version = ${options.manifest.sbt.sbtVersion.getOrElse(MacroParameters.sbtVersion().getOrElse("1.8.0"))}",
            )
          ),
          ExtendedModule.RuntimeModule(
            Module(
              ModuleId(Seq("project"), "plugins.sbt"),
              s"""
                 |// https://www.scala-js.org/
                 |addSbtPlugin("org.scala-js" % "sbt-scalajs" % "${MacroParameters.macroSetting("scalajs-version").getOrElse("undefined-version")}")
                 |
                 |// https://github.com/portable-scala/sbt-crossproject
                 |addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "${MacroParameters
                  .macroSetting("crossproject-version").getOrElse("undefined-version")}")
                 |
                 |// https://scalacenter.github.io/scalajs-bundler/
                 |addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "${MacroParameters.macroSetting("bundler-version").getOrElse("undefined-version")}")
                 |
                 |// https://github.com/scala-js/jsdependencies
                 |addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "${MacroParameters.macroSetting("sbt-js-version").getOrElse("undefined-version")}")
                 |
                 |""".stripMargin,
            )
          ),
        )

        projectModules ++ runtimeModules ++ sbtModules
    }
    Layouted(modules)
  }

  private def asSbtModule(out: Seq[Module], did: DomainId): Seq[Module] = {
    out.map {
      m =>
        val pid = naming.projectId(did)
        // PR-04 MCP Mb1: modules tagged `meta("resource") == "true"` (e.g. the
        // per-service `mcp/<Name>.mcp.json` companion the bridge code reads
        // via `getResourceAsStream`) route to `src/main/resources/` so they
        // land on the classpath as resources, not as Scala sources.
        val srcDir = if (m.meta.get("resource").contains("true")) "resources" else "scala"
        // PR-04 MCP Mb1: modules tagged `meta("platform") == "jvm"` (the http4s
        // bridge source + its resource) route to the crossproject's `.jvm`
        // sourceset so the Scala.js build skips them; everything else stays in
        // the shared sourceset and cross-compiles as before.
        val platformPrefix = if (m.meta.get("platform").contains("jvm")) Seq(".jvm") else Seq.empty[String]
        m.copy(id = m.id.copy(path = Seq(pid) ++ platformPrefix ++ Seq("src", "main", srcDir) ++ m.id.path))
    }
  }

  private def crossModuleSettings(depsSetting: String): String = {
    if (options.manifest.sbt.isCrossBuild) {
      s""".settings(
         |  $crossScalaVersionsSetting,
         |  scalaVersion := crossScalaVersions.value.head,
         |  $crossScalacOptions,
         |  $depsSetting,
         |)""".stripMargin
    } else {
      val soleScalaVersionSetting = options.manifest.sbt.scalaVersions match {
        case v :: Nil =>
          s"""scalaVersion := "$v","""
        case Nil =>
          throw new IllegalArgumentException("SBT layout requires at least one Scala version in scalaVersions manifest setting")
        case _ => ""
      }
      s""".settings(
         |  $depsSetting,
         |  $soleScalaVersionSetting
         |)""".stripMargin
    }
  }

  private def crossScalaVersionsSetting: String = {
    val asString = options.manifest.sbt.scalaVersions.map(v => s""""$v"""").mkString(", ")
    s"crossScalaVersions := Seq($asString)"
  }

  private def crossScalacOptions: String = {
    val versions    = options.manifest.sbt.scalaVersions
    val defaultCase = "case _ => Seq.empty"
    val perScalaVersionOptions =
      (versions.flatMap {
        case v if v.startsWith("2") => Some(s"""case "$v" => Seq("-Xsource:3-cross")""".stripMargin)
        case _                      => None
      } :+ defaultCase).mkString("    ", "\n    ", "")

    s"""scalacOptions ++= { scalaVersion.value match {
       |$perScalaVersionOptions
       |  }}""".stripMargin
  }

  /** Extra `.settings(...)` appended to a project that carries an `emitMcpBridge`
    * http4s bridge. The bridge source is emitted under `.jvm/src/main/scala`
    * (see `DomainScalaTranslator.emitMcpBridgeModules` + `asSbtModule`):
    *   - a `crossProject(CrossType.Pure)` (ScalaJS) build compiles `.jvm` via the
    *     JVM variant's base, so only the http4s dep + kind-projector are added —
    *     but JVM-only dependency scoping for ScalaJS is not handled yet, so it is
    *     intentionally skipped here;
    *   - a regular JVM-only project does NOT pick up `.jvm` automatically, so its
    *     `.jvm` source/resource dirs are wired in explicitly.
    * The bridge uses bifunctor underscore type-lambdas (`F[Throwable, _]`), hence
    * kind-projector with underscore placeholders, matching the runtime modules.
    */
  private def mcpBridgeSettings(idlVersion: String): String = {
    if (options.manifest.sbt.enableScalaJs) {
      "" // TODO: ScalaJS bridge support needs JVM-only dep scoping (.jvmSettings)
    } else {
      s"""
         |  .settings(
         |    Compile / unmanagedSourceDirectories += baseDirectory.value / ".jvm" / "src" / "main" / "scala",
         |    Compile / unmanagedResourceDirectories += baseDirectory.value / ".jvm" / "src" / "main" / "resources",
         |    libraryDependencies += "$idlcGroupId" %% "idealingua-v1-runtime-rpc-http4s" % "$idlVersion",
         |    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
         |      case Some((2, _)) => Seq(compilerPlugin("org.typelevel" % "kind-projector" % "0.13.4" cross CrossVersion.full))
         |      case _            => Seq.empty
         |    }),
         |    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
         |      case Some((2, _)) => Seq("-P:kind-projector:underscore-placeholders")
         |      case _            => Seq("-Xkind-projector:underscores")
         |    })
         |  )""".stripMargin
    }
  }
}
