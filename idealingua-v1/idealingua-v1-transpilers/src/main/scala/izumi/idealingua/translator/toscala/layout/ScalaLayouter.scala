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
    def project(id: String): String = {
      if (options.manifest.sbt.enableScalaJs) {
        s"""(crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).in(file("$id")))"""

      } else {
        s"""(project in file("$id"))"""
      }
    }

    import SbtDslOp.*

    val modules = options.manifest.layout match {
      case ScalaProjectLayout.PLAIN =>
        withRuntime(options, outputs)

      case ScalaProjectLayout.SBT =>
        val projectModules = outputs.flatMap {
          out =>
            val did = out.typespace.domain.id

            asSbtModule(out.modules, did)
              .map(m => ExtendedModule.DomainModule(did, m))
        }

        val rtid = DomainId(idlcGroupId.split('.').toIndexedSeq, "irt")

        val runtimeModules = asSbtModule(toRuntimeModules(options).map(_.module), rtid)
          .map(m => ExtendedModule.RuntimeModule(m))

        val projects = outputs.map {
          out =>
            naming.projectId(out.typespace.domain.id) -> out
        }.toMap

        val projIds = projects.keys.toList.sorted

        val moduleSettings = crossModuleSettings

        val projDefs = projIds.map {
          id =>
            val d    = projects(id)
            val deps = d.typespace.domain.meta.directImports.map(i => s"`${naming.projectId(i.id)}`")

            val depends = if (deps.nonEmpty) {
              deps.mkString("\n  .dependsOn(\n    ", ",\n    ", "\n  )")
            } else {
              ""
            }

            s"""lazy val `$id` = ${project(id)}$depends$moduleSettings"""
        }

        val bundleId = naming.bundleId
        val rootId   = naming.pkgId

        val rootSettings =
          if (options.manifest.sbt.isCrossBuild) {
            s""".settings(
               |   crossScalaVersions := Nil
               |)""".stripMargin
          } else ""

        val root =
          s"""
             |lazy val `$rootId` = ${project(".")}
             |  .aggregate(
             |    ${(projIds ++ Seq(bundleId)).map(id => s"`$id`").mkString(",\n    ")}
             |  )$rootSettings
         """.stripMargin

        val allDeps = projIds.map(i => s"`$i`")
        val depends = if (allDeps.nonEmpty) {
          allDeps.mkString("\n  .dependsOn(\n    ", ",\n    ", "\n  )")
        } else ""

        val bundle = s"lazy val `$bundleId` = ${project(bundleId)}$depends$moduleSettings"

        val idlVersion = options.manifest.common.izumiVersion
        val deps = Seq(
          "libraryDependencies" -> Append(
            Seq(
              RawExpr(s""" "$idlcGroupId" %% "idealingua-v1-runtime-rpc-scala" % "$idlVersion" """),
              RawExpr(s""" "$idlcGroupId" %% "idealingua-v1-model" % "$idlVersion" """),
            )
          )
        )
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

        val renderer = new SbtRenderer()
        val keys     = (docs ++ metadata ++ resolvers ++ deps ++ circeDerivationWorkaround).map(renderer.renderOp)

        val content = keys ++ projDefs ++ Seq(bundle, root)

        val sbtScalaVersionModule = sbtVersionModule

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

        projectModules ++ runtimeModules ++ sbtModules ++ sbtScalaVersionModule
    }
    Layouted(modules)
  }

  private def asSbtModule(out: Seq[Module], did: DomainId): Seq[Module] = {
    out.map {
      m =>
        val pid = naming.projectId(did)
        m.copy(id = m.id.copy(path = Seq(pid, "src", "main", "scala") ++ m.id.path))
    }
  }

  private def sbtVersionModule: Seq[ExtendedModule.RuntimeModule] = {
    options.manifest.sbt.scalaVersions match {
      case v :: Nil =>
        Seq(ExtendedModule.RuntimeModule(Module(ModuleId(Seq.empty, "scalaVersion.sbt"), s"""scalaVersion in Global := "$v"""")))
      case _ => Seq.empty
    }
  }

  private def crossModuleSettings: String = {
    if (options.manifest.sbt.isCrossBuild) {
      s""".settings(
         |  $crossScalaVersionsSetting
         |  scalaVersion := crossScalaVersions.value.head,
         |  $scalacOptions
         |)""".stripMargin
    } else ""
  }

  private def crossScalaVersionsSetting: String = {
    val asString = options.manifest.sbt.scalaVersions.map(v => s"\"$v\"").mkString(", ")
    s"crossScalaVersions := Seq($asString),"
  }

  private def scalacOptions: String = {
    val versions    = options.manifest.sbt.scalaVersions
    val defaultCase = "case _ => Seq.empty"
    val perScalaVersionOptions =
      versions.flatMap {
        case v if v.startsWith("2") => Some(s"""case "$v" => Seq("-Xsource:3-cross")""".stripMargin)
        case _ => None
      }.appended(defaultCase).mkString("    ", "\n    ", "")

    s"""scalacOptions ++= { scalaVersion.value match {
       |$perScalaVersionOptions
       |  }},""".stripMargin
  }
}
