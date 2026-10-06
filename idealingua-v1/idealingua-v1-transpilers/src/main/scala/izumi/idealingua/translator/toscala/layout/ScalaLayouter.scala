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
  private val sbtVersion  = options.manifest.sbt.sbtVersion.getOrElse(MacroParameters.macroSetting("generated-sbt-version").getOrElse("1.8.0"))
  private val targetsSbt2 = sbtVersion.startsWith("2.")

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

        val renderer = new SbtRenderer(targetsSbt2)

        val projectName = "name" -> Assign(options.manifest.common.name, Scope.Project)

        val projectDeps = renderer.renderOp {
          val versionOp = if (options.manifest.sbt.enableScalaJs && !targetsSbt2) "%%%" else "%%"

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

            s"""lazy val `$id` = ${crossProject(id)}$depends$moduleSettings"""
        }

        val bundleId = naming.bundleId

        val root = {
          val rootId   = naming.pkgId
          val rootName = if (targetsSbt2) s"\n   ${renderer.renderOp(projectName)}," else ""
          val rootSettings = {
            s""".settings($rootName
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

        val resolvers = (idlVersion.endsWith("SNAPSHOT"), targetsSbt2) match {
          case (true, true)   => Seq("resolvers" -> Append(RawExpr("Resolver.sonatypeCentralSnapshots")))
          case (false, true)  => Seq.empty
          case (true, false)  => Seq("resolvers" -> Append(RawExpr("Opts.resolver.sonatypeSnapshots")))
          case (false, false) => Seq("resolvers" -> Append(RawExpr("Opts.resolver.sonatypeReleases")))
        }

        val docs = Seq(
          "publishArtifact" -> Assign(options.manifest.sbt.enableDocs.getOrElse(false), List(Scope.ThisBuild, Scope.Custom("packageDoc")))
        )

        val metadata = (if (targetsSbt2) Seq.empty else Seq(projectName)) ++ Seq(
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
              s"sbt.version = $sbtVersion",
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
                 |$sbt1OnlyPlugins""".stripMargin,
            )
          ),
        )

        projectModules ++ runtimeModules ++ sbtModules
    }
    Layouted(modules)
  }

  private def sbt1OnlyPlugins: String = {
    if (targetsSbt2) {
      ""
    } else {
      s"""
         |// https://scalacenter.github.io/scalajs-bundler/
         |addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "${MacroParameters.macroSetting("bundler-version").getOrElse("undefined-version")}")
         |
         |// https://github.com/scala-js/jsdependencies
         |addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "${MacroParameters.macroSetting("sbt-js-version").getOrElse("undefined-version")}")
         |""".stripMargin
    }
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
        m.copy(id = m.id.copy(path = Seq(pid) ++ Seq("src", "main", srcDir) ++ m.id.path))
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
    // List Scala 3 first so `scalaVersion := crossScalaVersions.value.head` defaults to Scala 3
    val ordered  = options.manifest.sbt.scalaVersions.sortBy(v => if (v.startsWith("3")) 0 else 1)
    val asString = ordered.map(v => s""""$v"""").mkString(", ")
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

}
