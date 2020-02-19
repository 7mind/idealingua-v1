import $ivy.`io.7mind.izumi.sbt::sbtgen:0.0.47`
import izumi.sbtgen._
import izumi.sbtgen.model._

object Izumi {

  object V {
    val izumi = Version.VExpr("Izumi.version")
    // val collection_compat = Version.VExpr("V.collection_compat")
    val kind_projector = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.org_typelevel_kind_projector_version")
    val scalatest = Version.VExpr("V.scalatest")
    // val shapeless = Version.VExpr("V.shapeless")
    val cats = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.org_typelevel_cats_core_version")
    val cats_effect = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.org_typelevel_cats_effect_version")
    val zio = Version.VExpr("Izumi.Deps.fundamentals_bioJVM.dev_zio_zio_version")
    val zio_interop_cats = Version.VExpr("V.zio_interop_cats")
    // val circe = Version.VExpr("V.circe")
    // val circe_generic_extras = Version.VExpr("V.circe_generic_extras")
    // val circe_derivation = Version.VExpr("V.circe_derivation")
    // val circe_config = Version.VExpr("V.circe_config")
    // val jawn = Version.VExpr("V.jawn")
    val http4s = Version.VExpr("V.http4s")
    val scalameta = Version.VExpr("V.scalameta")
    val fastparse = Version.VExpr("V.fastparse")
    val scala_xml = Version.VExpr("V.scala_xml")
    val asynchttpclient = Version.VExpr("V.asynchttpclient")
    // val classgraph = Version.VExpr("V.classgraph")
    val slf4j = Version.VExpr("V.slf4j")
    val typesafe_config = Version.VExpr("V.typesafe_config")
    // val cglib_nodep = Version.VExpr("V.cglib_nodep")
    val scala_java_time = Version.VExpr("V.scala_java_time")
    // val docker_java = Version.VExpr("V.docker_java")
  }

  object PV {
    val izumi = Version.VExpr("PV.izumi")
    val sbt_mdoc = Version.VExpr("PV.sbt_mdoc")
    val sbt_paradox_material_theme = Version.VExpr("PV.sbt_paradox_material_theme")
    val sbt_ghpages = Version.VExpr("PV.sbt_ghpages")
    val sbt_site = Version.VExpr("PV.sbt_site")
    val sbt_unidoc = Version.VExpr("PV.sbt_unidoc")
    val sbt_scoverage = Version.VExpr("PV.sbt_scoverage")
    val sbt_pgp = Version.VExpr("PV.sbt_pgp")
    val sbt_assembly = Version.VExpr("PV.sbt_assembly")

    val scala_js_version = Version.VExpr("PV.scala_js_version")
    val crossproject_version = Version.VExpr("PV.crossproject_version")
    val scalajs_bundler_version = Version.VExpr("PV.scalajs_bundler_version")
  }

  def entrypoint(args: Seq[String]) = {
    Entrypoint.main(izumi, settings, Seq("-o", ".") ++ args)
  }

  val settings = GlobalSettings(
    groupId = "io.7mind.izumi",
    sbtVersion = "1.3.8",
    scalaJsVersion = PV.scala_js_version,
    crossProjectVersion = PV.crossproject_version,
    bundlerVersion = PV.scalajs_bundler_version,
  )

  object Deps {
     final val fundamentals_collections = Library("io.7mind.izumi", "fundamentals-collections", V.izumi, LibraryType.Auto)
     final val fundamentals_platform = Library("io.7mind.izumi", "fundamentals-platform", V.izumi, LibraryType.Auto)
     final val fundamentals_functional = Library("io.7mind.izumi", "fundamentals-functional", V.izumi, LibraryType.Auto)
     final val fundamentals_reflection = Library("io.7mind.izumi", "fundamentals-reflection", V.izumi, LibraryType.Auto)
     final val fundamentals_json_circe = Library("io.7mind.izumi", "fundamentals-json-circe", V.izumi, LibraryType.Auto)
     final val fundamentals_bio = Library("io.7mind.izumi", "fundamentals-bio", V.izumi, LibraryType.Auto)
     final val logstage_core = Library("io.7mind.izumi", "logstage-core", V.izumi, LibraryType.Auto)
     final val logstage_adapter_slf4j = Library("io.7mind.izumi", "logstage-adapter-slf4j", V.izumi, LibraryType.Auto)

     final val fundamentals_basics = Seq(
       fundamentals_collections,
       fundamentals_platform,
       fundamentals_functional,
     )
    // final val collection_compat = Library("org.scala-lang.modules", "scala-collection-compat", V.collection_compat, LibraryType.Auto)
    final val scalatest = Library("org.scalatest", "scalatest", V.scalatest, LibraryType.Auto) in Scope.Test.all

    final val cats_core = Library("org.typelevel", "cats-core", V.cats, LibraryType.Auto)
    final val cats_effect = Library("org.typelevel", "cats-effect", V.cats_effect, LibraryType.Auto)
    final val cats_all = Seq(
      cats_core,
      cats_effect,
    )

    final val zio_core = Library("dev.zio", "zio", V.zio, LibraryType.Auto)
    final val zio_interop_cats = Library("dev.zio", "zio-interop-cats", V.zio_interop_cats, LibraryType.Auto)
    final val zio_all = Seq(
      zio_core,
      zio_interop_cats,
    )

    final val typesafe_config = Library("com.typesafe", "config", V.typesafe_config, LibraryType.Invariant) in Scope.Compile.all

    final val scala_sbt = Library("org.scala-sbt", "sbt", Version.VExpr("sbtVersion.value"), LibraryType.Invariant)
    final val scala_reflect = Library("org.scala-lang", "scala-reflect", Version.VExpr("scalaVersion.value"), LibraryType.Invariant)
    final val scala_xml = Library("org.scala-lang.modules", "scala-xml", V.scala_xml, LibraryType.Auto) in Scope.Compile.all
    final val scalameta = Library("org.scalameta", "scalameta", V.scalameta, LibraryType.Auto) in Scope.Compile.all

    final val projector = Library("org.typelevel", "kind-projector", V.kind_projector, LibraryType.Invariant)
      .more(LibSetting.Raw("cross CrossVersion.full"))

    final val scala_java_time = Library("io.github.cquiroz", "scala-java-time", V.scala_java_time, LibraryType.Auto) in Scope.Compile.js

    final val slf4j_api = Library("org.slf4j", "slf4j-api", V.slf4j, LibraryType.Invariant) in Scope.Compile.jvm
    final val slf4j_simple = Library("org.slf4j", "slf4j-simple", V.slf4j, LibraryType.Invariant) in Scope.Test.jvm

    final val fastparse = Library("com.lihaoyi", "fastparse", V.fastparse, LibraryType.Auto) in Scope.Compile.all

    final val http4s_client = Seq(
      Library("org.http4s", "http4s-blaze-client", V.http4s, LibraryType.Auto),
    )

    val http4s_server = Seq(
      Library("org.http4s", "http4s-dsl", V.http4s, LibraryType.Auto),
      Library("org.http4s", "http4s-circe", V.http4s, LibraryType.Auto),
      Library("org.http4s", "http4s-blaze-server", V.http4s, LibraryType.Auto),
    )

    val http4s_all = (http4s_server ++ http4s_client)

    val asynchttpclient = Library("org.asynchttpclient", "async-http-client", V.asynchttpclient, LibraryType.Invariant)
  }

  import Deps._

  // DON'T REMOVE, these variables are read from CI build (build.sh)
  final val scala212 = ScalaVersion("2.12.10")
  final val scala213 = ScalaVersion("2.13.1")

  object Groups {
    final val fundamentals = Set(Group("fundamentals"))
    final val idealingua = Set(Group("idealingua"))
    final val docs = Set(Group("docs"))
  }

  object Targets {
    // switch order to use 2.13 in IDEA
    val targetScala = Seq(scala212, scala213)
//    val targetScala = Seq(scala213, scala212)
    private val jvmPlatform = PlatformEnv(
      platform = Platform.Jvm,
      language = targetScala,
    )
    private val jvmPlatformSbt = PlatformEnv(
      platform = Platform.Jvm,
      language = Seq(scala212),
      settings = Seq(
        "coverageEnabled" := false,
      ),
    )
    private val jsPlatform = PlatformEnv(
      platform = Platform.Js,
      language = targetScala,
      settings = Seq(
        "coverageEnabled" := false,
        "scalaJSModuleKind" in(SettingScope.Project, Platform.Js) := "ModuleKind.CommonJSModule".raw,
      ),
    )
    final val cross = Seq(jvmPlatform, jsPlatform)
    final val jvm = Seq(jvmPlatform)
    final val js = Seq(jsPlatform)
    final val jvmSbt = Seq(jvmPlatformSbt)
  }

  final val assemblyPluginJvm = Plugin("AssemblyPlugin", Platform.Jvm)
  final val assemblyPluginJs = Plugin("AssemblyPlugin", Platform.Js)

  object Projects {

    final val plugins = Plugins(
      Seq(Plugin("IzumiPlugin")),
      Seq(assemblyPluginJs, assemblyPluginJvm),
    )

    object root {
      final val id = ArtifactId("izumi")
      final val plugins = Plugins(
        enabled = Seq(Plugin("SbtgenVerificationPlugin")),
        disabled = Seq(Plugin("AssemblyPlugin")),
      )
      final val settings = Seq()

      final val sharedAggSettings = Seq(
        "crossScalaVersions" := Targets.targetScala.map(_.value),
        "scalaVersion" := "crossScalaVersions.value.head".raw,
      )

      final val rootSettings = Defaults.SharedOptions ++ Seq(
        "crossScalaVersions" := "Nil".raw,
        "scalaVersion" := Targets.targetScala.head.value,
        "organization" in SettingScope.Build := "io.7mind.izumi",
        "sonatypeProfileName" := "io.7mind",
        "sonatypeSessionName" := """s"[sbt-sonatype] ${name.value} ${version.value} ${java.util.UUID.randomUUID}"""".raw,
        "publishTo" in SettingScope.Build :=
          """
            |(if (!isSnapshot.value) {
            |    sonatypePublishToBundle.value
            |  } else {
            |    Some(Opts.resolver.sonatypeSnapshots)
            |})
            |""".stripMargin.raw,
        "credentials" in SettingScope.Build += """Credentials(file(".secrets/credentials.sonatype-nexus.properties"))""".raw,
        "homepage" in SettingScope.Build := """Some(url("https://izumi.7mind.io"))""".raw,
        "licenses" in SettingScope.Build := """Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))""".raw,
        "developers" in SettingScope.Build :=
          """List(
          Developer(id = "7mind", name = "Septimal Mind", url = url("https://github.com/7mind"), email = "team@7mind.io"),
        )""".raw,
        "scmInfo" in SettingScope.Build := """Some(ScmInfo(url("https://github.com/7mind/izumi"), "scm:git:https://github.com/7mind/izumi.git"))""".raw,
        "scalacOptions" in SettingScope.Build += s"""${"\"" * 3}-Xmacro-settings:scalatest-version=${V.scalatest}${"\"" * 3}""".raw,
      )

      final val sharedSettings = Defaults.SbtMeta ++ Seq(
        "testOptions" in SettingScope.Test += """Tests.Argument("-oDF")""".raw,
        //"testOptions" in (SettingScope.Test, Platform.Jvm) ++= s"""Seq(Tests.Argument("-u"), Tests.Argument(s"$${target.value}/junit-xml-$${scalaVersion.value}"))""".raw,
        "scalacOptions" ++= Seq(
          SettingKey(Some(scala212), None) := Defaults.Scala212Options,
          SettingKey(Some(scala213), None) := Defaults.Scala213Options,
          SettingKey.Default := Const.EmptySeq,
        ),
        "scalacOptions" ++= Seq(
          SettingKey(None, Some(true)) := Seq(
            "-opt:l:inline",
            "-opt-inline-from:izumi.**",
          ),
          SettingKey.Default := Const.EmptySeq
        ),
      )

    }


    object idealingua {
      final val id = ArtifactId("idealingua")
      final val basePath = Seq("idealingua-v1")

      final val model = ArtifactId("idealingua-v1-model")
      final val core = ArtifactId("idealingua-v1-core")
      final val runtimeRpcScala = ArtifactId("idealingua-v1-runtime-rpc-scala")
      final val testDefs = ArtifactId("idealingua-v1-test-defs")
      final val transpilers = ArtifactId("idealingua-v1-transpilers")
      final val runtimeRpcHttp4s = ArtifactId("idealingua-v1-runtime-rpc-http4s")
      final val runtimeRpcTypescript = ArtifactId("idealingua-v1-runtime-rpc-typescript")
      final val runtimeRpcCSharp = ArtifactId("idealingua-v1-runtime-rpc-csharp")
      final val runtimeRpcGo = ArtifactId("idealingua-v1-runtime-rpc-go")
      final val compiler = ArtifactId("idealingua-v1-compiler")
    }

    object docs {
      final val id = ArtifactId("doc")
      final val basePath = Seq("doc")

      final lazy val microsite = ArtifactId("microsite")
    }

  }

  final val forkTests = Seq(
    "fork" in(SettingScope.Test, Platform.Jvm) := true,
  )

  final val crossScalaSources = Seq(
    "unmanagedSourceDirectories" in SettingScope.Compile :=
      """(unmanagedSourceDirectories in Compile).value.flatMap {
        |  dir =>
        |   Seq(dir, file(dir.getPath + (CrossVersion.partialVersion(scalaVersion.value) match {
        |     case Some((2, 12)) => "_2.12"
        |     case Some((2, 13)) => "_2.13"
        |     case _             => "_3.0"
        |   })))
        |}""".stripMargin.raw,
  )


  final lazy val idealingua = Aggregate(
    name = Projects.idealingua.id,
    artifacts = Seq(
      Artifact(
        name = Projects.idealingua.model,
        libs = Deps.fundamentals_basics.map(_ in Scope.Compile.all),
        depends = Seq.empty,
      ),
      Artifact(
        name = Projects.idealingua.core,
        libs = Seq(fastparse) ++ Seq(Deps.fundamentals_reflection).map(_ in Scope.Compile.all),
        depends = Seq(Projects.idealingua.model).map(_ in Scope.Compile.all),
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcScala,
        libs = Seq(scala_reflect in Scope.Provided.all) ++ cats_all.map(_ in Scope.Compile.all) ++
          Seq(Deps.fundamentals_bio, Deps.fundamentals_json_circe).map(_ in Scope.Compile.all) ++
          zio_all.map(_ in Scope.Test.all),
        depends = Seq.empty,
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcHttp4s,
        libs = (http4s_all ++ Seq(asynchttpclient, Deps.logstage_core, Deps.logstage_adapter_slf4j)).map(_ in Scope.Compile.all),
        depends = Seq(Projects.idealingua.runtimeRpcScala).map(_ in Scope.Compile.all) ++
          Seq(Projects.idealingua.testDefs).map(_ in Scope.Test.jvm),
        platforms = Targets.jvm,
      ),
      Artifact(
        name = Projects.idealingua.transpilers,
        libs = Seq(scala_xml, scalameta) ++ Seq(Deps.fundamentals_bio, Deps.fundamentals_json_circe).map(_ in Scope.Compile.all),
        depends = Seq(Projects.idealingua.core, Projects.idealingua.runtimeRpcScala).map(_ in Scope.Compile.all) ++
          Seq(Projects.idealingua.testDefs, Projects.idealingua.runtimeRpcTypescript, Projects.idealingua.runtimeRpcGo, Projects.idealingua.runtimeRpcCSharp).map(_ in Scope.Test.jvm),
        settings = forkTests
      ),
      Artifact(
        name = Projects.idealingua.testDefs,
        libs = zio_all,
        depends = Seq(Projects.idealingua.runtimeRpcScala).map(_ in Scope.Compile.all),
        platforms = Targets.jvm,
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcTypescript,
        libs = Seq.empty,
        depends = Seq.empty,
        platforms = Targets.jvm,
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcGo,
        libs = Seq.empty,
        depends = Seq.empty,
        platforms = Targets.jvm,
      ),
      Artifact(
        name = Projects.idealingua.runtimeRpcCSharp,
        libs = Seq.empty,
        depends = Seq.empty,
        platforms = Targets.jvm,
      ),
      Artifact(
        name = Projects.idealingua.compiler,
        libs = Seq(typesafe_config),
        depends = Seq(
          Projects.idealingua.transpilers,
          Projects.idealingua.runtimeRpcScala,
          Projects.idealingua.runtimeRpcTypescript,
          Projects.idealingua.runtimeRpcGo,
          Projects.idealingua.runtimeRpcCSharp,
          Projects.idealingua.testDefs,
        ).map(_ in Scope.Compile.all),
        platforms = Targets.jvm,
        plugins = Plugins(Seq(assemblyPluginJvm)),
        settings = Seq(
          "mainClass" in SettingScope.Raw("assembly") := """Some("izumi.idealingua.compiler.CommandlineIDLCompiler")""".raw,
          "assemblyMergeStrategy" in SettingScope.Raw("assembly") :=
            """{
              |      // FIXME: workaround for https://github.com/zio/interop-cats/issues/16
              |      case path if path.contains("zio/BuildInfo$.class") =>
              |        MergeStrategy.last
              |      case p =>
              |        (assemblyMergeStrategy in assembly).value(p)
              |}""".stripMargin.raw,
          "artifact" in SettingScope.Raw("(Compile, assembly)") :=
            """{
              |      val art = (artifact in(Compile, assembly)).value
              |      art.withClassifier(Some("assembly"))
              |}""".stripMargin.raw,
          SettingDef.RawSettingDef("addArtifact(artifact in(Compile, assembly), assembly)")
        )
      ),
    ),
    pathPrefix = Projects.idealingua.basePath,
    groups = Groups.idealingua,
    defaultPlatforms = Targets.cross,
  )

  /*val all = Seq(fundamentals, distage, logstage)

  final lazy val docs = Aggregate(
    name = Projects.docs.id,
    artifacts = Seq(
      Artifact(
        name = Projects.docs.microsite,
        libs = (cats_all ++ zio_all ++ http4s_all).map(_ in Scope.Compile.all),
        depends = all.flatMap(_.artifacts).map(_.name in Scope.Compile.all).distinct,
        settings = Seq(
          "coverageEnabled" := false,
          "skip" in SettingScope.Raw("publish") := true,
          "DocKeys.prefix" :=
            """{if (isSnapshot.value) {
            "latest/snapshot"
          } else {
            "latest/release"
          }}""".raw,
          "previewFixedPort" := "Some(9999)".raw,
          "git.remoteRepo" := "git@github.com:7mind/izumi-microsite.git",
          "classLoaderLayeringStrategy" in SettingScope.Raw("Compile") := "ClassLoaderLayeringStrategy.Flat".raw,
          "mdocIn" := """baseDirectory.value / "src/main/tut"""".raw,
          "sourceDirectory" in SettingScope.Raw("Paradox") := "mdocOut.value".raw,
          "mdocExtraArguments" ++= Seq(" --no-link-hygiene"),
          "mappings" in SettingScope.Raw("SitePlugin.autoImport.makeSite") :=
            """{
            (mappings in SitePlugin.autoImport.makeSite)
              .dependsOn(mdoc.toTask(" "))
              .value
          }""".raw,
          "version" in SettingScope.Raw("Paradox") := "version.value".raw,

          SettingDef.RawSettingDef("ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox)"),
          SettingDef.RawSettingDef("addMappingsToSiteDir(mappings in(ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc)"),
          SettingDef.RawSettingDef("unidocProjectFilter in(ScalaUnidoc, unidoc) := inAggregates(`fundamentals-jvm`, transitive = true) || inAggregates(`distage-jvm`, transitive = true) || inAggregates(`logstage-jvm`, transitive = true)"),

          SettingDef.RawSettingDef(
            """paradoxMaterialTheme in Paradox ~= {
            _.withCopyright("7mind.io")
              .withRepository(uri("https://github.com/7mind/izumi"))
            //        .withColor("222", "434343")
          }"""),
          "siteSubdirName" in SettingScope.Raw("ScalaUnidoc") := """s"${DocKeys.prefix.value}/api"""".raw,
          "siteSubdirName" in SettingScope.Raw("Paradox") := """s"${DocKeys.prefix.value}/doc"""".raw,
          SettingDef.RawSettingDef(
            """paradoxProperties ++= Map(
            "scaladoc.izumi.base_url" -> s"/${DocKeys.prefix.value}/api/",
            "scaladoc.base_url" -> s"/${DocKeys.prefix.value}/api/",
            "izumi.version" -> version.value,
          )"""),
          SettingDef.RawSettingDef(
            """excludeFilter in ghpagesCleanSite :=
            new FileFilter {
              def accept(f: File): Boolean = {
                (f.toPath.startsWith(ghpagesRepository.value.toPath.resolve("latest")) && !f.toPath.startsWith(ghpagesRepository.value.toPath.resolve(DocKeys.prefix.value))) ||
                  (ghpagesRepository.value / "CNAME").getCanonicalPath == f.getCanonicalPath ||
                  (ghpagesRepository.value / ".nojekyll").getCanonicalPath == f.getCanonicalPath ||
                  (ghpagesRepository.value / "index.html").getCanonicalPath == f.getCanonicalPath ||
                  (ghpagesRepository.value / "README.md").getCanonicalPath == f.getCanonicalPath ||
                  f.toPath.startsWith((ghpagesRepository.value / "media").toPath) ||
                  f.toPath.startsWith((ghpagesRepository.value / "v0.5.50-SNAPSHOT").toPath)
              }
            }""")
        ),
        plugins = Plugins(
          enabled = Seq(
            Plugin("ScalaUnidocPlugin"),
            Plugin("ParadoxSitePlugin"),
            Plugin("SitePlugin"),
            Plugin("GhpagesPlugin"),
            Plugin("ParadoxMaterialThemePlugin"),
            Plugin("PreprocessPlugin"),
            Plugin("MdocPlugin")),
          disabled = Seq(Plugin("ScoverageSbtPlugin")))
      ),
    ),
    pathPrefix = Projects.docs.basePath,
    groups = Groups.docs,
    defaultPlatforms = Targets.jvm,
    dontIncludeInSuperAgg = true,
  )
*/

  val izumi: Project = Project(
    name = Projects.root.id,
    aggregates = Seq(
      idealingua,
    ),
    topLevelSettings = Projects.root.settings,
    sharedSettings = Projects.root.sharedSettings,
    sharedAggSettings = Projects.root.sharedAggSettings,
    rootSettings = Projects.root.rootSettings,
    imports = Seq.empty,
    globalLibs = Seq(
      ScopedLibrary(projector, FullDependencyScope(Scope.Compile, Platform.All), compilerPlugin = true),
      scalatest,
    ),
    rootPlugins = Projects.root.plugins,
    globalPlugins = Projects.plugins,
    pluginConflictRules = Map(assemblyPluginJvm.name -> true),
    appendPlugins = Defaults.SbtGenPlugins ++ Seq(
      SbtPlugin("com.eed3si9n", "sbt-assembly", PV.sbt_assembly),
      SbtPlugin("com.jsuereth", "sbt-pgp", PV.sbt_pgp),
      SbtPlugin("org.scoverage", "sbt-scoverage", PV.sbt_scoverage),
      SbtPlugin("com.eed3si9n", "sbt-unidoc", PV.sbt_unidoc),
      SbtPlugin("com.typesafe.sbt", "sbt-site", PV.sbt_site),
      SbtPlugin("com.typesafe.sbt", "sbt-ghpages", PV.sbt_ghpages),
      SbtPlugin("io.github.jonas", "sbt-paradox-material-theme", PV.sbt_paradox_material_theme),
      SbtPlugin("org.scalameta", "sbt-mdoc", PV.sbt_mdoc),
      SbtPlugin("io.7mind.izumi", "sbt-izumi-deps", PV.izumi),
    )
  )
}

